// BuildingDetailFragment.java (New file based on previous suggestion)
package com.example.rise_of_city.ui.game.building;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.game.Building;
import com.example.rise_of_city.data.model.game.Mission;
import com.example.rise_of_city.data.repository.BuildingUpgradeManager;
import com.example.rise_of_city.data.repository.GoldRepository;
import com.example.rise_of_city.ui.lesson.LessonActivity;
import com.example.rise_of_city.ui.viewmodel.GameViewModel;

import java.util.List;

public class BuildingDetailFragment extends Fragment {

    private Building building;
    private OnUpgradeClickListener upgradeListener;
    private BuildingUpgradeManager upgradeManager;
    private TextView tvStudyRequirement;
    private ImageView imgLessonStatus;
    private Button btnUpgrade;
    
    // Emergency/Mission UI
    private LinearLayout layoutActiveMission;
    private TextView tvMissionTitle;
    private TextView tvMissionDesc;
    private Button btnDoMission;
    private TextView tvNoMission;
    
    private GameViewModel gameViewModel;
    private GoldRepository goldRepository;
    private Mission activeMission; // Mission hiện tại cho building này
    
    private ActivityResultLauncher<Intent> lessonLauncher;

    public static BuildingDetailFragment newInstance(Building building) {
        BuildingDetailFragment fragment = new BuildingDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("building", building);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            building = (Building) getArguments().getSerializable("building");
        }
        upgradeManager = BuildingUpgradeManager.getInstance(requireContext());
        goldRepository = GoldRepository.getInstance();
        
        // Lấy GameViewModel từ Activity
        if (getActivity() != null) {
            gameViewModel = new ViewModelProvider(getActivity()).get(GameViewModel.class);
        }
        
        // Register Activity Result Launcher
        lessonLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        handleLessonResult(result.getData());
                    }
                }
            }
        );
    }
    
    /**
     * Xử lý kết quả trả về từ LessonActivity
     */
    private void handleLessonResult(Intent data) {
        // Check lại trạng thái lesson khi quay lại từ LessonActivity
        if (building != null) {
            updateQuestStatus();
            
            // Kiểm tra xem có mission ID trong result không (từ LessonActivity)
            // Nếu có, nghĩa là vừa hoàn thành review để giải quyết sự cố
            String completedMissionId = data.getStringExtra("completed_mission_id");
            if (completedMissionId != null && activeMission != null && activeMission.id.equals(completedMissionId)) {
                // Hoàn thành mission và cộng thưởng
                completeEmergencyMission();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_building_detail, container, false);

        // Ánh xạ views
        ImageView ivBuilding = view.findViewById(R.id.iv_building);
        TextView tvBuildingName = view.findViewById(R.id.tv_building_name);
        TextView tvLevel = view.findViewById(R.id.tv_level);
        Button btnStudy = view.findViewById(R.id.btnStudy);
        Button btnReview = view.findViewById(R.id.btnReview);
        btnUpgrade = view.findViewById(R.id.btnUpgrade);
        tvStudyRequirement = view.findViewById(R.id.tvStudyRequirement);
        imgLessonStatus = view.findViewById(R.id.imgLessonStatus);
        
        // Emergency/Mission views
        layoutActiveMission = view.findViewById(R.id.layoutActiveMission);
        tvMissionTitle = view.findViewById(R.id.tvMissionTitle);
        tvMissionDesc = view.findViewById(R.id.tvMissionDesc);
        btnDoMission = view.findViewById(R.id.btnDoMission);
        tvNoMission = view.findViewById(R.id.tvNoMission);

        // Hiển thị info building
        if (building != null) {
            tvBuildingName.setText(building.getName());
            tvLevel.setText("Level: " + building.getLevel());
            // Load image cho building (ví dụ: set src dựa trên building ID)
            if ("house".equals(building.getId())) {
                ivBuilding.setImageResource(R.drawable.vector_house);
            }
            
            // Update quest status
            updateQuestStatus();
        }
        
        // Setup mission/emergency observer
        setupMissionObserver();

        // Sự kiện Học bài (mode STUDY_NEW)
        btnStudy.setOnClickListener(v -> {
            startLessonActivity("STUDY_NEW");
        });

        // Sự kiện Ôn bài (mode REVIEW)
        btnReview.setOnClickListener(v -> {
            startLessonActivity("REVIEW");
        });
        
        // Sự kiện Reset Progress (DEBUG)
        Button btnResetProgress = view.findViewById(R.id.btnResetProgress);
        if (btnResetProgress != null) {
            btnResetProgress.setOnClickListener(v -> {
                resetLessonProgress();
            });
        }
        
        // Sự kiện Xử lý sự cố khẩn cấp
        if (btnDoMission != null) {
            btnDoMission.setOnClickListener(v -> {
                handleEmergencyMission();
            });
        }

        // Sự kiện Nâng cấp
        btnUpgrade.setOnClickListener(v -> {
            handleUpgrade();
        });

        return view;
    }
    
    /**
     * Setup observer để theo dõi active missions và hiển thị sự cố khẩn cấp
     */
    private void setupMissionObserver() {
        if (gameViewModel == null || building == null) {
            if (layoutActiveMission != null) layoutActiveMission.setVisibility(View.GONE);
            if (tvNoMission != null) tvNoMission.setVisibility(View.VISIBLE);
            return;
        }
        
        gameViewModel.getActiveMissions().observe(getViewLifecycleOwner(), missions -> {
            updateEmergencyUI(missions);
        });
    }
    
    /**
     * Cập nhật UI sự cố khẩn cấp dựa trên active missions
     */
    private void updateEmergencyUI(List<Mission> missions) {
        if (building == null) return;
        
        // Tìm mission active cho building này
        activeMission = null;
        if (missions != null && !missions.isEmpty()) {
            for (Mission mission : missions) {
                if (mission.buildingId.equals(building.getId()) && !mission.isCompleted) {
                    activeMission = mission;
                    break;
                }
            }
        }
        
        // Hiển thị/ẩn UI dựa trên có mission hay không
        if (activeMission != null) {
            // Có sự cố khẩn cấp
            if (layoutActiveMission != null) {
                layoutActiveMission.setVisibility(View.VISIBLE);
                if (tvMissionTitle != null) {
                    tvMissionTitle.setText("⚠️ SỰ CỐ KHẨN CẤP");
                }
                if (tvMissionDesc != null) {
                    tvMissionDesc.setText("Ôn lại bài để giải quyết sự cố!");
                }
            }
            if (tvNoMission != null) {
                tvNoMission.setVisibility(View.GONE);
            }
        } else {
            // Không có sự cố
            if (layoutActiveMission != null) {
                layoutActiveMission.setVisibility(View.GONE);
            }
            if (tvNoMission != null) {
                tvNoMission.setVisibility(View.VISIBLE);
            }
        }
    }
    
    /**
     * Xử lý khi bấm "XỬ LÝ NGAY" - mở lesson với mode REVIEW
     */
    private void handleEmergencyMission() {
        if (building == null) {
            Toast.makeText(getContext(), "Lỗi: Không có thông tin công trình", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (activeMission == null) {
            Toast.makeText(getContext(), "Không có sự cố nào cần xử lý", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Mở LessonActivity với mode REVIEW để ôn lại bài
        Intent intent = new Intent(getActivity(), LessonActivity.class);
        intent.putExtra("lessonName", building.getRequiredLessonName());
        intent.putExtra("mode", "REVIEW");
        intent.putExtra("building_id", building.getId());
        intent.putExtra("mission_id", activeMission.id); // Lưu mission ID để complete sau khi xong
        startActivity(intent);
    }

    private void updateQuestStatus() {
        if (building == null) return;
        
        String lessonName = building.getRequiredLessonName();
        boolean lessonCompleted = upgradeManager.isLessonCompleted(lessonName);
        boolean canUpgrade = upgradeManager.canUpgradeBuilding(building);
        
        // Update UI
        if (lessonCompleted) {
            imgLessonStatus.setImageResource(android.R.drawable.ic_menu_revert);
            imgLessonStatus.setColorFilter(getResources().getColor(android.R.color.holo_green_dark));
            tvStudyRequirement.setText("Đã hoàn thành bài học: " + lessonName);
        } else {
            imgLessonStatus.setImageResource(android.R.drawable.ic_delete);
            imgLessonStatus.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
            tvStudyRequirement.setText("Chưa hoàn thành bài học: " + lessonName);
        }
        
        // Enable/disable upgrade button
        btnUpgrade.setEnabled(canUpgrade && building.getLevel() < 5);
        if (building.getLevel() >= 5) {
            btnUpgrade.setText("ĐÃ ĐẠT LEVEL TỐI ĐA");
            btnUpgrade.setEnabled(false);
        }
    }

    private void startLessonActivity(String mode) {
        if (building == null) return;
        Intent intent = new Intent(getActivity(), LessonActivity.class);
        intent.putExtra("lessonName", building.getRequiredLessonName());
        intent.putExtra("mode", mode);
        intent.putExtra("building_id", building.getId());
        
        // Nếu là emergency mission, truyền mission_id
        if (activeMission != null && "REVIEW".equals(mode)) {
            intent.putExtra("mission_id", activeMission.id);
        }
        
        lessonLauncher.launch(intent);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh mission status khi resume
        if (gameViewModel != null && building != null) {
            // Force refresh bằng cách observe lại
            setupMissionObserver();
        }
    }
    
    /**
     * Hoàn thành mission khẩn cấp sau khi ôn lại bài xong
     */
    private void completeEmergencyMission() {
        if (activeMission == null || gameViewModel == null) return;
        
        // Complete mission trong ViewModel
        gameViewModel.completeMission(activeMission.id);
        
        // Cộng vàng thưởng
        if (goldRepository != null && getContext() != null) {
            goldRepository.addGold(getContext(), activeMission.goldReward, new GoldRepository.OnGoldUpdatedListener() {
                @Override
                public void onGoldUpdated(int newGold) {
                    Toast.makeText(getContext(), 
                        "✅ Đã giải quyết sự cố! +" + activeMission.goldReward + " vàng (Tổng: " + newGold + " vàng)", 
                        Toast.LENGTH_LONG).show();
                }
                
                @Override
                public void onError(String error) {
                    Toast.makeText(getContext(), "✅ Đã giải quyết sự cố!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // Reset active mission
        activeMission = null;
    }

    private void handleUpgrade() {
        if (building == null) return;
        
        if (building.getLevel() >= 5) {
            Toast.makeText(getContext(), "Building đã đạt level tối đa", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!upgradeManager.canUpgradeBuilding(building)) {
            Toast.makeText(getContext(), "Cần hoàn thành bài học trước khi nâng cấp", Toast.LENGTH_SHORT).show();
            return;
        }
        
        upgradeManager.upgradeBuilding(building.getId(), new BuildingUpgradeManager.OnUpgradeListener() {
            @Override
            public void onUpgradeSuccess(int newLevel) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Nâng cấp thành công! Level: " + newLevel, Toast.LENGTH_SHORT).show();
                            // Notify listener để reload building
                            if (upgradeListener != null) {
                                upgradeListener.onUpgradeClicked(building);
                            }
                            // Update UI
                            updateQuestStatus();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * Reset lesson progress (DEBUG - để test lại từ đầu)
     */
    private void resetLessonProgress() {
        if (building == null) return;
        
        String lessonName = building.getId().substring(0, 1).toUpperCase() + building.getId().substring(1) + "_lv" + building.getLevel();
        
        // Xác nhận trước khi reset
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Reset Progress")
            .setMessage("Bạn có chắc muốn xóa toàn bộ progress của bài học " + lessonName + "?\n\nSau khi reset, bạn sẽ được cộng vàng lại khi làm đúng các câu hỏi.")
            .setPositiveButton("Reset", (dialog, which) -> {
                com.example.rise_of_city.data.repository.LessonQuestionManager questionManager = 
                    new com.example.rise_of_city.data.repository.LessonQuestionManager(requireContext());
                questionManager.clearLessonProgress(lessonName);
                
                // Reset lesson completed status
                upgradeManager.clearLessonCompleted(lessonName);
                
                Toast.makeText(requireContext(), "Đã reset progress của " + lessonName, Toast.LENGTH_SHORT).show();
                
                // Update UI
                updateQuestStatus();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    public void setOnUpgradeClickListener(OnUpgradeClickListener listener) {
        this.upgradeListener = listener;
    }

    public interface OnUpgradeClickListener {
        void onUpgradeClicked(Building building);
    }
}
