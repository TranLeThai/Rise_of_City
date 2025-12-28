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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.game.Building;
import com.example.rise_of_city.data.model.game.Mission;
import com.example.rise_of_city.data.repository.GameRepository;
import com.example.rise_of_city.data.repository.GoldRepository;
import com.example.rise_of_city.ui.lesson.LessonActivity;
import com.example.rise_of_city.ui.viewmodel.GameViewModel;

public class MissionDialogFragment extends DialogFragment {

    private Mission mission;
    private Building building;
    private GameViewModel gameViewModel;
    private GoldRepository goldRepository;
    private ActivityResultLauncher<Intent> lessonLauncher;

    public static MissionDialogFragment newInstance(Mission mission) {
        MissionDialogFragment fragment = new MissionDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("mission_data", mission);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.DialogTheme);

        if (getArguments() != null) {
            mission = (Mission) getArguments().getSerializable("mission_data");
        }

        goldRepository = GoldRepository.getInstance();

        if (getActivity() != null) {
            gameViewModel = new ViewModelProvider(getActivity()).get(GameViewModel.class);
        }

        lessonLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == -1 && result.getData() != null) {
                        handleLessonResult(result.getData());
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_mission, container, false);

        ImageButton btnClose = view.findViewById(R.id.btnClose);
        TextView tvTitle = view.findViewById(R.id.tvMissionTitle);
        TextView tvDescription = view.findViewById(R.id.tvMissionDesc);
        TextView tvReward = view.findViewById(R.id.tvReward);
        TextView tvPenalty = view.findViewById(R.id.tvPenalty);
        Button btnAccept = view.findViewById(R.id.btnAccept);
        Button btnDeny = view.findViewById(R.id.btnDeny);

        // Hiển thị thông tin mission
        if (mission != null) {
            tvTitle.setText(mission.title);

            if (mission.type == Mission.Type.EMERGENCY) {
                tvDescription.setText("Ôn lại bài học để giải quyết sự cố khẩn cấp!");
                tvPenalty.setVisibility(View.VISIBLE);
                tvPenalty.setText("Phạt nếu quá hạn: -" + mission.goldPenalty + " vàng");
            } else {
                tvDescription.setText("Hoàn thành để nhận thưởng hàng ngày!");
                tvPenalty.setVisibility(View.GONE);
            }

            tvReward.setText("Thưởng: +" + mission.goldReward + " vàng");
        }

        // Load tên building nếu là emergency
        if (mission != null && mission.type == Mission.Type.EMERGENCY && getContext() != null) {
            GameRepository repo = GameRepository.getInstance(getContext());
            building = repo.getBuildingById(mission.buildingId);
        }

        btnClose.setOnClickListener(v -> dismiss());
        btnDeny.setOnClickListener(v -> dismiss());
        btnAccept.setOnClickListener(v -> handleAcceptMission());

        return view;
    }

    private void handleAcceptMission() {
        if (mission == null) {
            dismiss();
            return;
        }

        String lessonName;
        if (mission.type == Mission.Type.EMERGENCY && building != null) {
            lessonName = building.getRequiredLessonName();
        } else {
            lessonName = "Daily_lesson"; // Có thể mở rộng thành nhiều bài daily khác nhau
        }

        Intent intent = new Intent(getActivity(), LessonActivity.class);
        intent.putExtra("lessonName", lessonName);
        intent.putExtra("mode", "REVIEW");
        intent.putExtra("mission_id", mission.id); // Quan trọng: để hoàn thành mission sau

        lessonLauncher.launch(intent);
    }

    private void handleLessonResult(Intent data) {
        boolean success = data.getBooleanExtra("success", false);
        String returnedMissionId = data.getStringExtra("mission_id");

        if (success && returnedMissionId != null
                && mission != null && mission.id.equals(returnedMissionId)) {
            completeMission();
        }
    }

    private void completeMission() {
        if (gameViewModel == null || mission == null) return;

        gameViewModel.completeMission(mission.id);

        goldRepository.addGold(getContext(), mission.goldReward, new GoldRepository.OnGoldUpdatedListener() {
            @Override
            public void onGoldUpdated(int newGold) {
                Toast.makeText(getContext(),
                        "Hoàn thành nhiệm vụ!\n+ " + mission.goldReward + " vàng",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Hoàn thành nhiệm vụ!", Toast.LENGTH_SHORT).show();
            }
        });

        dismiss();
    }
}