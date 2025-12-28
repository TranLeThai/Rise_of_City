package com.example.rise_of_city.ui.game.ingame.mission;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.game.Mission;
import com.example.rise_of_city.ui.dialog.MissionDialogFragment;
import com.example.rise_of_city.ui.lesson.LessonActivity;
import com.example.rise_of_city.ui.viewmodel.GameViewModel;

import java.util.ArrayList;
import java.util.List;

public class MissionBoardDialog extends DialogFragment {

    private GameViewModel gameViewModel;
    private List<Mission> dailyMissions;
    private List<Mission> emergencyMissions;

    private RecyclerView rvDaily, rvEmergency;
    private LinearLayout layoutEmergencySection;
    private TextView tvNoEmergency;

    public static MissionBoardDialog newInstance(List<Mission> missions) {
        return new MissionBoardDialog();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_mission_board, container, false);

        // Ánh xạ
        rvDaily = view.findViewById(R.id.rvDailyMissions);
        rvEmergency = view.findViewById(R.id.rvEmergencyMissions);
        layoutEmergencySection = view.findViewById(R.id.layoutEmergencySection);
        tvNoEmergency = view.findViewById(R.id.tvNoEmergency);

        rvDaily.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEmergency.setLayoutManager(new LinearLayoutManager(getContext()));

        // ViewModel
        gameViewModel = new ViewModelProvider(requireActivity()).get(GameViewModel.class);

        // Observe active missions
        gameViewModel.getActiveMissions().observe(getViewLifecycleOwner(), missions -> {
            dailyMissions = new ArrayList<>();
            emergencyMissions = new ArrayList<>();

            for (Mission m : missions) {
                if (m.type == Mission.Type.DAILY) {
                    dailyMissions.add(m);
                } else if (m.type == Mission.Type.EMERGENCY) {
                    emergencyMissions.add(m);
                }
            }

            // Setup adapter daily
            MissionAdapter dailyAdapter = new MissionAdapter(dailyMissions, this::handleMissionClick);
            rvDaily.setAdapter(dailyAdapter);

            // Setup adapter emergency
            MissionAdapter emergencyAdapter = new MissionAdapter(emergencyMissions, this::handleMissionClick);
            rvEmergency.setAdapter(emergencyAdapter);

            // Ẩn/hiện section emergency
            if (emergencyMissions.isEmpty()) {
                layoutEmergencySection.setVisibility(View.GONE);
                tvNoEmergency.setVisibility(View.VISIBLE);
            } else {
                layoutEmergencySection.setVisibility(View.VISIBLE);
                tvNoEmergency.setVisibility(View.GONE);
            }
        });

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());

        return view;
    }

    private void handleMissionClick(Mission mission) {
        if (mission.type == Mission.Type.EMERGENCY) {
            MissionDialogFragment dialog = MissionDialogFragment.newInstance(mission);
            dialog.show(getParentFragmentManager(), "MissionDetail");
        } else { // DAILY
            // Xử lý daily mission: Mở Lesson hoặc activity tương ứng
            Intent intent = new Intent(getActivity(), LessonActivity.class);
            intent.putExtra("lessonName", "Daily_lesson"); // Bài học hàng ngày chung
            intent.putExtra("mode", "REVIEW"); // Ví dụ ôn từ vựng
            startActivity(intent);
        }
        dismiss();
    }
}