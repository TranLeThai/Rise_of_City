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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.game.Building;
import com.example.rise_of_city.data.model.game.Mission;
import com.example.rise_of_city.data.repository.BuildingUpgradeRepository;
import com.example.rise_of_city.data.repository.GoldRepository;
import com.example.rise_of_city.ui.lesson.LessonActivity;
import com.example.rise_of_city.ui.viewmodel.GameViewModel;

import java.util.List;

public class BuildingDetailFragment extends Fragment {

    private Building building;
    private GameViewModel gameViewModel;
    private GoldRepository goldRepository;
    private BuildingUpgradeRepository upgradeRepository;

    // UI Elements
    private TextView tvBuildingName, tvLevel, tvStudyRequirement;
    private ImageView ivBuilding, imgLessonStatus;
    private Button btnStudy, btnReview, btnUpgrade;
    private LinearLayout layoutActiveMission;
    private TextView tvMissionTitle, tvMissionDesc, tvNoMission;
    private Button btnDoMission;

    // Mission hiện tại (nếu có)
    private Mission activeMission;

    // Launcher để nhận kết quả từ LessonActivity
    private ActivityResultLauncher<Intent> lessonLauncher;

    // Listener để thông báo khi upgrade thành công (cho InGameActivity reload)
    private OnUpgradeClickListener upgradeClickListener;

    public interface OnUpgradeClickListener {
        void onUpgradeClicked(Building building);
    }

    public void setOnUpgradeClickListener(OnUpgradeClickListener listener) {
        this.upgradeClickListener = listener;
    }

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

        goldRepository = GoldRepository.getInstance();
        upgradeRepository = BuildingUpgradeRepository.getInstance();

        if (getActivity() != null) {
            gameViewModel = new ViewModelProvider(getActivity()).get(GameViewModel.class);
        }

        // Sửa lỗi launcher: dùng -1 thay vì ActivityResult.RESULT_OK (hoặc import đúng)
        lessonLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == -1 && result.getData() != null) { // -1 = RESULT_OK
                        handleLessonResult(result.getData());
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_building_detail, container, false);

        initViews(view);
        setupClickListeners();

        if (building != null) {
            displayBuildingInfo();
            updateLessonStatus();
        }

        setupMissionObserver();

        return view;
    }

    private void initViews(View view) {
        ivBuilding = view.findViewById(R.id.iv_building);
        tvBuildingName = view.findViewById(R.id.tv_building_name);
        tvLevel = view.findViewById(R.id.tv_level);

        btnStudy = view.findViewById(R.id.btnStudy);
        btnReview = view.findViewById(R.id.btnReview);
        btnUpgrade = view.findViewById(R.id.btnUpgrade);

        imgLessonStatus = view.findViewById(R.id.imgLessonStatus);
        tvStudyRequirement = view.findViewById(R.id.tvStudyRequirement);

        layoutActiveMission = view.findViewById(R.id.layoutActiveMission);
        tvMissionTitle = view.findViewById(R.id.tvMissionTitle);
        tvMissionDesc = view.findViewById(R.id.tvMissionDesc);
        btnDoMission = view.findViewById(R.id.btnDoMission);
        tvNoMission = view.findViewById(R.id.tvNoMission);
    }

    private void displayBuildingInfo() {
        if (building == null) return;

        tvBuildingName.setText(building.getName());
        tvLevel.setText("Cấp độ " + building.getLevel());

        int imageRes = getResources().getIdentifier("vector_" + building.getId(), "drawable", requireContext().getPackageName());
        if (imageRes != 0) {
            ivBuilding.setImageResource(imageRes);
        } else {
            ivBuilding.setImageResource(R.drawable.vector_bakery);
        }
    }

    private void setupClickListeners() {
        btnStudy.setOnClickListener(v -> startLesson("STUDY_NEW"));
        btnReview.setOnClickListener(v -> startLesson("REVIEW"));
        btnDoMission.setOnClickListener(v -> handleEmergencyMission());
        btnUpgrade.setOnClickListener(v -> handleUpgrade());
    }

    private void startLesson(String mode) {
        if (building == null) return;

        Intent intent = new Intent(getActivity(), LessonActivity.class);
        intent.putExtra("lessonName", building.getRequiredLessonName());
        intent.putExtra("mode", mode);

        if (activeMission != null && "REVIEW".equals(mode)) {
            intent.putExtra("mission_id", activeMission.id);
        }

        lessonLauncher.launch(intent);
    }

    private void handleEmergencyMission() {
        if (activeMission == null) {
            Toast.makeText(getContext(), "Không có sự cố nào cần xử lý", Toast.LENGTH_SHORT).show();
            return;
        }
        startLesson("REVIEW");
    }

    private void handleUpgrade() {
        if (building == null) return;

        if (building.getLevel() >= 4) {
            Toast.makeText(getContext(), "Công trình đã đạt cấp tối đa (Level 4)", Toast.LENGTH_SHORT).show();
            return;
        }

        upgradeRepository.upgradeBuilding(
                requireContext(),
                building.getId(),
                building.getLevel(),
                new BuildingUpgradeRepository.OnUpgradeListener() {
                    @Override
                    public void onUpgradeSuccess(int newLevel, BuildingUpgradeRepository.UpgradeBenefits benefits) {
                        Toast.makeText(getContext(), "Nâng cấp thành công! Level " + newLevel, Toast.LENGTH_LONG).show();

                        // Gọi listener để InGameActivity reload building
                        if (upgradeClickListener != null) {
                            upgradeClickListener.onUpgradeClicked(building);
                        }

                        // Reload từ ViewModel để cập nhật UI ngay
                        gameViewModel.loadBuildingById(building.getId());
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupMissionObserver() {
        if (gameViewModel == null) return;

        gameViewModel.getActiveMissions().observe(getViewLifecycleOwner(), this::updateEmergencyUI);
    }

    private void updateEmergencyUI(List<Mission> missions) {
        activeMission = null;

        if (missions != null && building != null) {
            for (Mission m : missions) {
                if (m.buildingId.equals(building.getId()) && !m.isCompleted && m.type == Mission.Type.EMERGENCY) {
                    activeMission = m;
                    break;
                }
            }
        }

        if (activeMission != null) {
            layoutActiveMission.setVisibility(View.VISIBLE);
            tvNoMission.setVisibility(View.GONE);
            tvMissionTitle.setText("⚠️ SỰ CỐ KHẨN CẤP");
            tvMissionDesc.setText("Ôn lại bài để giải quyết sự cố!");
        } else {
            layoutActiveMission.setVisibility(View.GONE);
            tvNoMission.setVisibility(View.VISIBLE);
        }
    }

    private void updateLessonStatus() {
        if (building == null) return;

        boolean canUpgrade = building.getLevel() < 4;

        if (canUpgrade) {
            imgLessonStatus.setImageResource(android.R.drawable.checkbox_on_background);
            imgLessonStatus.setColorFilter(getResources().getColor(android.R.color.holo_green_dark));
            tvStudyRequirement.setText("Đã đủ điều kiện nâng cấp");
        } else {
            imgLessonStatus.setImageResource(android.R.drawable.ic_delete);
            imgLessonStatus.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
            tvStudyRequirement.setText("Đã đạt cấp tối đa");
        }

        btnUpgrade.setEnabled(canUpgrade);
    }

    private void handleLessonResult(Intent data) {
        String completedLesson = data.getStringExtra("completed_lesson");
        boolean success = data.getBooleanExtra("success", false);

        if (success && completedLesson != null && building != null) {
            if (completedLesson.equals(building.getRequiredLessonName())) {
                gameViewModel.loadBuildingById(building.getId());
                Toast.makeText(getContext(), "Bài học hoàn thành! Công trình được nâng cấp.", Toast.LENGTH_LONG).show();
            }

            String missionId = data.getStringExtra("mission_id");
            if (missionId != null && activeMission != null && activeMission.id.equals(missionId)) {
                gameViewModel.completeMission(missionId);
                Toast.makeText(getContext(), "Sự cố đã được giải quyết!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (building != null) {
            updateLessonStatus();
        }
    }
}