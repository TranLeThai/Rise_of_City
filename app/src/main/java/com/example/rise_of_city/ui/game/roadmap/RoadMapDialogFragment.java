package com.example.rise_of_city.ui.game.roadmap;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.game.BuildingProgress;
import com.example.rise_of_city.data.repository.BuildingProgressRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RoadMapDialogFragment extends DialogFragment implements BuildingRoadMapAdapter.OnBuildingClickListener {

    private static final String ARG_LEVEL = "arg_current_level";
    private int mCurrentLevel = 1;
    
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
    
    // Số từ vựng giả định (thực tế nên lấy từ database)
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

    public static RoadMapDialogFragment newInstance(int currentLevel) {
        RoadMapDialogFragment fragment = new RoadMapDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LEVEL, currentLevel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar_Fullscreen);

        if (getArguments() != null) {
            mCurrentLevel = getArguments().getInt(ARG_LEVEL);
        }
        
        repository = BuildingProgressRepository.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sử dụng layout mới chứa RecyclerView
        View view = inflater.inflate(R.layout.fragment_roadmap_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_roadmap);
        progressBar = view.findViewById(R.id.progressBar);
        
        setupRecyclerView();
        loadData();

        return view;
    }
    
    private void setupRecyclerView() {
        adapter = new BuildingRoadMapAdapter(new ArrayList<>());
        adapter.setOnBuildingClickListener(this);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        // Reverse layout để item đầu tiên (House) ở dưới cùng, giống như leo núi
        // layoutManager.setReverseLayout(true); 
        // layoutManager.setStackFromEnd(true);
        
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }
    
    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        
        repository.getAllBuildingProgress(new BuildingProgressRepository.OnAllBuildingsLoadedListener() {
            @Override
            public void onBuildingsLoaded(Map<String, Map<String, Object>> buildingProgressMap) {
                List<BuildingProgress> buildings = new ArrayList<>();
                boolean previousUnlocked = true; // House luôn mở
                
                for (int i = 0; i < BUILDING_ORDER.length; i++) {
                    String buildingId = BUILDING_ORDER[i];
                    String name = BUILDING_NAMES.getOrDefault(buildingId, buildingId);
                    int vocabCount = VOCABULARY_COUNTS.getOrDefault(buildingId, 0);
                    
                    BuildingProgress building = new BuildingProgress();
                    building.setBuildingId(buildingId);
                    building.setBuildingName(name);
                    building.setVocabularyCount(vocabCount);
                    
                    // Kiểm tra xem user có dữ liệu cho building này chưa
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
                        
                        // Đã có dữ liệu nghĩa là đã unlock
                        building.setLocked(false);
                    } else {
                        // Chưa có dữ liệu
                        building.setLevel(1);
                        building.setCurrentExp(0);
                        building.setMaxExp(100);
                        building.setCompleted(false);
                        building.setVocabularyLearned(0);
                        
                        // Logic khóa: Mở khóa nếu building trước đó đã completed hoặc đạt level nhất định
                        // Ở đây đơn giản hóa: Mở khóa theo thứ tự, cái trước mở thì cái sau mới hiện (nhưng bị lock)
                        // Hoặc logic: Cái trước completed -> Cái sau unlocked
                        
                        if (i == 0) {
                            building.setLocked(false); // House luôn mở
                        } else {
                            // Kiểm tra building trước đó
                            String prevBuildingId = BUILDING_ORDER[i-1];
                            boolean prevCompleted = false;
                            
                            if (buildingProgressMap.containsKey(prevBuildingId)) {
                                Map<String, Object> prevData = buildingProgressMap.get(prevBuildingId);
                                Boolean pCompleted = (Boolean) prevData.get("completed");
                                prevCompleted = pCompleted != null && pCompleted;
                                
                                // Hoặc check level > 1
                                Long pLevel = (Long) prevData.get("level");
                                if (pLevel != null && pLevel > 1) prevCompleted = true; 
                            }
                            
                            building.setLocked(!prevCompleted);
                        }
                    }
                    
                    buildings.add(building);
                }
                
                progressBar.setVisibility(View.GONE);
                adapter.setBuildings(buildings);
            }
            
            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBuildingClick(BuildingProgress building) {
        // Xử lý khi click vào building
        if (building.isLocked()) {
            Toast.makeText(getContext(), "Cần hoàn thành công trình trước đó!", Toast.LENGTH_SHORT).show();
        } else {
            // Mở chi tiết building hoặc game
             Toast.makeText(getContext(), "Chọn: " + building.getBuildingName(), Toast.LENGTH_SHORT).show();
             // TODO: Navigate to InGameActivity or BuildingDetail
        }
    }
}