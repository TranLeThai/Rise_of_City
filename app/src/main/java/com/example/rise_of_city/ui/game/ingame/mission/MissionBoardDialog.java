package com.example.rise_of_city.ui.game.ingame.mission;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.game.Mission;
import com.example.rise_of_city.ui.dialog.MissionAdapter;

import java.util.List;

public class MissionBoardDialog extends DialogFragment {

    private List<Mission> missions;

    public static MissionBoardDialog newInstance(List<Mission> missions) {
        MissionBoardDialog fragment = new MissionBoardDialog();
        fragment.missions = missions;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_mission_board, container, false);

        RecyclerView rv = view.findViewById(R.id.rvMissions);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        // Thiết lập Adapter - hiển thị MissionDialogFragment khi click
        MissionAdapter adapter = new MissionAdapter(missions, mission -> {
            // Hiển thị dialog chi tiết mission với thông tin building
            com.example.rise_of_city.ui.dialog.MissionDialogFragment dialog = 
                com.example.rise_of_city.ui.dialog.MissionDialogFragment.newInstance(mission);
            dialog.show(getParentFragmentManager(), "MissionDetail");
            dismiss(); // Đóng mission board
        });
        rv.setAdapter(adapter);

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());

        return view;
    }
}