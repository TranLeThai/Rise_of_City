package com.example.rise_of_city.ui.game.roadmap;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.game.BuildingProgress;
import com.example.rise_of_city.data.repository.BuildingProgressRepository;
import com.example.rise_of_city.ui.game.ingame.InGameActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoadMapFragment extends Fragment implements BuildingRoadMapAdapter.OnBuildingClickListener {

    private RecyclerView recyclerView;
    private BuildingRoadMapAdapter adapter;
    private ProgressBar progressBar;
    private BuildingProgressRepository repository;
    
    // Thứ tự building trong map
    private static final String[] BUILDING_ORDER = {
        "house", "farm", "park", "school", "library", "coffee", "clothers", "bakery"
    };
    
    // Tên hiển thị
    private static final Map<String, String> BUILDING_NAMES = Map.of(
        "house", "Nhà ở",
        "farm", "Nông trại",
        "park", "Công viên",
        "school", "Trường học",
        "library", "Thư viện",
        "coffee", "Quán Cafe",
        "clothers", "Shop Quần Áo",
        "bakery", "Tiệm Bánh"
    );
    
    // Số từ vựng giả định
    private static final Map<String, Integer> VOCABULARY_COUNTS = Map.of(
        "house", 50,
        "farm", 60,
        "park", 45,
        "school", 120,
        "library", 200,
        "coffee", 80,
        "clothers", 90,
        "bakery", 75
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = BuildingProgressRepository.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sử dụng chung layout danh sách với Dialog
        View view = inflater.inflate(R.layout.fragment_roadmap_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_roadmap);
        progressBar = view.findViewById(R.id.progressBar);
        
        setupRecyclerView();
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
    
    private void setupRecyclerView() {
        adapter = new BuildingRoadMapAdapter(new ArrayList<>());
        adapter.setOnBuildingClickListener(this);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        // Reverse layout để item đầu tiên (House) ở dưới cùng nếu muốn hiệu ứng leo núi
        // layoutManager.setReverseLayout(true); 
        // layoutManager.setStackFromEnd(true);
        
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }
    
    private void loadData() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        repository.getAllBuildingProgress(new BuildingProgressRepository.OnAllBuildingsLoadedListener() {
            @Override
            public void onBuildingsLoaded(Map<String, Map<String, Object>> buildingProgressMap) {
                if (getContext() == null) return;

                List<BuildingProgress> buildings = new ArrayList<>();
                
                for (int i = 0; i < BUILDING_ORDER.length; i++) {
                    String buildingId = BUILDING_ORDER[i];
                    String name = BUILDING_NAMES.getOrDefault(buildingId, buildingId);
                    int vocabCount = VOCABULARY_COUNTS.getOrDefault(buildingId, 0);
                    
                    BuildingProgress building = new BuildingProgress();
                    building.setBuildingId(buildingId);
                    building.setBuildingName(name);
                    building.setVocabularyCount(vocabCount);
                    
                    if (buildingProgressMap.containsKey(buildingId)) {
                        Map<String, Object> data = buildingProgressMap.get(buildingId);
                        
                        Long level = (Long) data.get("level");
                        Long currentExp = (Long) data.get("currentExp");
                        Long maxExp = (Long) data.get("maxExp");
                        Boolean completed = (Boolean) data.get("completed");
                        Long vocabLearned = (Long) data.get("vocabularyLearned");
                        
                        building.setLevel(level != null ? level.intValue() : 1);
                        building.setCurrentExp(currentExp != null ? currentExp.intValue() : 0);
                        building.setMaxExp(maxExp != null ? maxExp.intValue() : 100);
                        building.setCompleted(completed != null && completed);
                        building.setVocabularyLearned(vocabLearned != null ? vocabLearned.intValue() : 0);
                        
                        building.setLocked(false);
                    } else {
                        building.setLevel(1);
                        building.setCurrentExp(0);
                        building.setMaxExp(100);
                        building.setCompleted(false);
                        building.setVocabularyLearned(0);
                        
                        if (i == 0) {
                            building.setLocked(false);
                        } else {
                            String prevBuildingId = BUILDING_ORDER[i-1];
                            boolean prevCompleted = false;
                            
                            if (buildingProgressMap.containsKey(prevBuildingId)) {
                                Map<String, Object> prevData = buildingProgressMap.get(prevBuildingId);
                                Boolean pCompleted = (Boolean) prevData.get("completed");
                                prevCompleted = pCompleted != null && pCompleted;
                                
                                Long pLevel = (Long) prevData.get("level");
                                if (pLevel != null && pLevel > 1) prevCompleted = true; 
                            }
                            building.setLocked(!prevCompleted);
                        }
                    }
                    buildings.add(building);
                }
                
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                adapter.setBuildings(buildings);
            }
            
            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBuildingClick(BuildingProgress building) {
        if (building.isLocked()) {
            Toast.makeText(getContext(), "Cần hoàn thành công trình trước đó!", Toast.LENGTH_SHORT).show();
        } else {
            // Chuyển sang InGameActivity
            Intent intent = new Intent(getActivity(), InGameActivity.class);
            intent.putExtra("building_id", building.getBuildingId());
            startActivity(intent);
        }
    }
}