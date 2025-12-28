package com.example.rise_of_city.ui.game.roadmap;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.game.BuildingProgress;
import com.example.rise_of_city.ui.game.ingame.InGameActivity;

import java.util.ArrayList;
import java.util.List;

public class RoadMapFragment extends Fragment {

    private BuildingRoadMapAdapter adapter;
    private RecyclerView rvBuildings;
    private ProgressBar pbLoading;
    private TextView tvEmpty;
    private TextView tvLevelName;
    private TextView tvProgressText;
    private ProgressBar pbLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_road_map, container, false);

        initViews(view);
        setupRecyclerView();

        return view;
    }

    private void initViews(View view) {
        rvBuildings = view.findViewById(R.id.rv_buildings);
        pbLoading = view.findViewById(R.id.pb_loading);
        tvEmpty = view.findViewById(R.id.tv_empty);
        tvLevelName = view.findViewById(R.id.tvLevelName);
        tvProgressText = view.findViewById(R.id.tv_progress_text);
        pbLevel = view.findViewById(R.id.pbLevel);
    }

    private void setupRecyclerView() {
        adapter = new BuildingRoadMapAdapter(new ArrayList<>());
        adapter.setOnBuildingClickListener(building -> {
            // Navigate to InGameActivity và focus vào building đó
            Intent intent = new Intent(getContext(), InGameActivity.class);
            intent.putExtra("buildingId", building.getBuildingId());
            startActivity(intent);
        });

        rvBuildings.setLayoutManager(new LinearLayoutManager(getContext()));
        rvBuildings.setAdapter(adapter);
    }

    private void updateHeader(List<BuildingProgress> buildings) {
        int totalBuildings = buildings.size();
        int completedCount = 0;
        int totalExp = 0;
        int totalMaxExp = 0;

        for (BuildingProgress building : buildings) {
            if (building.isCompleted()) {
                completedCount++;
            }
            totalExp += building.getCurrentExp();
            totalMaxExp += building.getMaxExp();
        }

        // Update level name (có thể tính dựa trên total level)
        tvLevelName.setText("LEVEL: " + getLevelName(completedCount, totalBuildings));

        // Update progress bar
        if (totalMaxExp > 0) {
            int progressPercent = (totalExp * 100) / totalMaxExp;
            pbLevel.setMax(100);
            pbLevel.setProgress(progressPercent);
        }

        // Update progress text
        tvProgressText.setText(completedCount + "/" + totalBuildings + " Thử thách hoàn thành");
    }

    private String getLevelName(int completedCount, int totalBuildings) {
        if (completedCount == 0) {
            return "BEGINNER";
        } else if (completedCount < totalBuildings / 3) {
            return "BEGINNER";
        } else if (completedCount < totalBuildings * 2 / 3) {
            return "INTERMEDIATE";
        } else if (completedCount < totalBuildings) {
            return "ADVANCED";
        } else {
            return "MASTER";
        }
    }
}
