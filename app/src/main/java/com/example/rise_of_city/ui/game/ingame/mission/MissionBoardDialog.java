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
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // Thiết lập Adapter (Bạn cần tạo MissionAdapter)
        MissionAdapter adapter = new MissionAdapter(missions, mission -> {
            // Xử lý khi bấm vào làm nhiệm vụ (Mở com.example.rise_of_city.ui.lesson.LessonActivity)
            dismiss();
        });
        rv.setAdapter(adapter);

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());

        return view;
    }
}