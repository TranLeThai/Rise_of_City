package com.example.rise_of_city.ui.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.game.Building;
import com.example.rise_of_city.data.model.game.Mission;
import com.example.rise_of_city.data.repository.BuildingQuestGenerator;
import com.example.rise_of_city.data.repository.GameRepository;
import com.example.rise_of_city.data.repository.GoldRepository;
import com.example.rise_of_city.ui.lesson.LessonActivity;
import com.example.rise_of_city.ui.viewmodel.GameViewModel;

public class MissionDialogFragment extends DialogFragment {

    private Mission mission;
    private Building building;
    private BuildingQuestGenerator questGenerator;
    private OnAcceptClickListener onAcceptClickListener;
    private OnDenyClickListener onDenyClickListener;
    private GameViewModel gameViewModel;
    private GoldRepository goldRepository;
    private ActivityResultLauncher<Intent> lessonLauncher;

    public interface OnAcceptClickListener {
        void onAcceptClick(Mission mission);
    }

    public interface OnDenyClickListener {
        void onDenyClick(Mission mission);
    }

    // Cập nhật: Nhận trực tiếp đối tượng Mission để lấy dữ liệu phạt/thưởng
    public static MissionDialogFragment newInstance(Mission mission) {
        MissionDialogFragment fragment = new MissionDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("mission_data", mission);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mission = (Mission) getArguments().getSerializable("mission_data");
        }
        // Thiết lập theme full screen hoặc không khung
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        
        if (getContext() != null) {
            questGenerator = new BuildingQuestGenerator(getContext());
            goldRepository = GoldRepository.getInstance();
            
            // Load building info
            GameRepository repo = GameRepository.getInstance(getContext());
            if (mission != null && repo != null) {
                building = repo.getBuildingById(mission.buildingId);
            }
        }
        
        // Khởi tạo ViewModel
        if (getActivity() != null) {
            gameViewModel = new ViewModelProvider(getActivity()).get(GameViewModel.class);
        }
        
        // Khởi tạo launcher để nhận kết quả từ LessonActivity
        lessonLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    boolean lessonCompleted = result.getData().getBooleanExtra("lesson_completed", false);
                    String completedMissionId = result.getData().getStringExtra("completed_mission_id");
                    
                    if (lessonCompleted && completedMissionId != null && mission != null && mission.id.equals(completedMissionId)) {
                        // Hoàn thành mission
                        completeMission();
                    }
                }
            }
        );
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_mission, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        TextView tvMissionTitle = view.findViewById(R.id.tv_mission_title);
        TextView tvMissionText = view.findViewById(R.id.tv_mission_text);
        Button btnAccept = view.findViewById(R.id.btn_accept);
        Button btnDeny = view.findViewById(R.id.btn_deny);

        if (mission != null && building != null && questGenerator != null) {
            // Lấy thông tin quest chi tiết
            BuildingQuestGenerator.QuestInfo questInfo = questGenerator.getQuestInfo(mission.buildingId, building);
            
            // Hiển thị nhiệm vụ làm quiz
            tvMissionTitle.setText("💰 NHIỆM VỤ: " + mission.title);
            
            // Tạo description đơn giản cho quiz
            StringBuilder detailText = new StringBuilder();
            detailText.append("🏗️ Công trình: ").append(building.getName()).append("\n");
            detailText.append("📚 Chủ đề: ").append(building.getRequiredLessonName()).append("\n\n");
            
            detailText.append("📝 Nội dung:\n");
            detailText.append("Làm quiz về ").append(building.getName()).append(" để kiếm vàng!\n\n");
            
            detailText.append("⏰ Thời hạn: 24 tiếng\n");
            detailText.append("💰 Phần thưởng: ").append(mission.goldReward).append(" Vàng\n");
            
            if (mission.goldPenalty > 0) {
                detailText.append("⚠️ Hình phạt: Trừ ").append(mission.goldPenalty).append(" Vàng nếu quá hạn\n");
            }
            
            tvMissionText.setText(detailText.toString());
            
            // Update button text
            btnAccept.setText("LÀM QUIZ NGAY");
        } else if (mission != null) {
            // Fallback nếu không có building info
            tvMissionTitle.setText("💰 NHIỆM VỤ: " + mission.title);
            
            String detailText = "Làm quiz để kiếm vàng!\n\n" +
                    "• Thời hạn: 24 tiếng.\n" +
                    "• Phần thưởng: " + mission.goldReward + " Vàng.";
            tvMissionText.setText(detailText);
            btnAccept.setText("LÀM QUIZ NGAY");
        }

        // Đóng thông báo
        btnBack.setOnClickListener(v -> dismiss());

        // Chấp nhận làm nhiệm vụ
        btnAccept.setOnClickListener(v -> {
            if (mission != null && building != null) {
                // Điều hướng đến LessonActivity hoặc BuildingDetail tùy theo quest type
                handleAcceptMission();
            }
            if (onAcceptClickListener != null && mission != null) {
                onAcceptClickListener.onAcceptClick(mission);
            }
            dismiss();
        });

        // Từ chối (Bỏ qua sự cố nhưng vẫn tốn 12h để tự phục hồi hoặc chờ phạt)
        btnDeny.setOnClickListener(v -> {
            if (onDenyClickListener != null && mission != null) {
                onDenyClickListener.onDenyClick(mission);
            }
            dismiss();
        });
    }
    
    private void handleAcceptMission() {
        if (building == null) return;
        
        // Luôn mở LessonActivity ở mode REVIEW (làm quiz)
        Intent intent = new Intent(getActivity(), LessonActivity.class);
        intent.putExtra("lessonName", building.getRequiredLessonName());
        intent.putExtra("building_id", building.getId());
        intent.putExtra("mode", "REVIEW");  // Luôn là REVIEW để làm quiz
        intent.putExtra("mission_id", mission.id);  // Truyền mission ID để track hoàn thành
        
        // Launch với launcher để nhận kết quả
        lessonLauncher.launch(intent);
    }
    
    /**
     * Hoàn thành mission sau khi làm xong quiz
     */
    private void completeMission() {
        if (mission == null) return;
        
        // Complete mission trong ViewModel
        if (gameViewModel != null) {
            gameViewModel.completeMission(mission.id);
        }
        
        // Cộng vàng thưởng
        if (goldRepository != null && getContext() != null) {
            goldRepository.addGold(getContext(), mission.goldReward, new GoldRepository.OnGoldUpdatedListener() {
                @Override
                public void onGoldUpdated(int newGold) {
                    Toast.makeText(getContext(), 
                        "✅ Hoàn thành nhiệm vụ! +" + mission.goldReward + " vàng (Tổng: " + newGold + " vàng)", 
                        Toast.LENGTH_LONG).show();
                }
                
                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "✅ Hoàn thành nhiệm vụ!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // Đóng dialog
        dismiss();
    }

    public void setOnAcceptClickListener(OnAcceptClickListener listener) {
        this.onAcceptClickListener = listener;
    }

    public void setOnDenyClickListener(OnDenyClickListener listener) {
        this.onDenyClickListener = listener;
    }
}
