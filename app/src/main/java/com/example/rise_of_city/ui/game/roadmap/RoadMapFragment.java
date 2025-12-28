package com.example.rise_of_city.ui.game.roadmap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.UserBuilding;
import com.example.rise_of_city.data.model.game.BuildingProgress;
import com.example.rise_of_city.ui.game.ingame.InGameActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoadMapFragment extends Fragment implements BuildingRoadMapAdapter.OnBuildingClickListener {

    private RecyclerView recyclerView;
    private BuildingRoadMapAdapter adapter;
    private ProgressBar progressBar;
    private AppDatabase database;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private int userId;
    
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
        database = AppDatabase.getInstance(requireContext());
        SharedPreferences prefs = requireContext().getSharedPreferences("RiseOfCity_Prefs", Context.MODE_PRIVATE);
        userId = prefs.getInt("logged_user_id", -1);
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
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }
    
    private void loadData() {
        if (userId == -1) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        executorService.execute(() -> {
            // Load buildings từ Room database
            List<UserBuilding> userBuildings = database.userBuildingDao().getBuildingsForUser(userId);
            
            // Convert sang Map để dễ xử lý
            Map<String, UserBuilding> buildingMap = new HashMap<>();
            for (UserBuilding ub : userBuildings) {
                buildingMap.put(ub.buildingId, ub);
            }
            
            // Build list BuildingProgress
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
                
                UserBuilding userBuilding = buildingMap.get(buildingId);
                if (userBuilding != null && userBuilding.level > 0) {
                    // Building đã có trong database và đã unlock (level > 0)
                    building.setLevel(userBuilding.level);
                    building.setCurrentExp(0); // Room database không lưu exp, để mặc định
                    building.setMaxExp(100);
                    building.setCompleted(userBuilding.level >= 5); // Level 5 = completed
                    building.setVocabularyLearned(0);
                    building.setLocked(false);
                    previousUnlocked = true;
                } else {
                    // Building chưa có trong database hoặc chưa unlock (level = 0)
                    building.setLevel(1);
                    building.setCurrentExp(0);
                    building.setMaxExp(100);
                    building.setCompleted(false);
                    building.setVocabularyLearned(0);
                    
                    if (i == 0) {
                        // House - check từ SharedPreferences hoặc database
                        // Nếu có house_lv1_unlocked trong SharedPreferences, unlock house
                        SharedPreferences prefs = requireContext().getSharedPreferences("RiseOfCity_Prefs", Context.MODE_PRIVATE);
                        boolean houseUnlocked = prefs.getBoolean("house_lv1_unlocked", false);
                        building.setLocked(!houseUnlocked);
                        previousUnlocked = houseUnlocked;
                    } else {
                        // Kiểm tra building trước đó
                        building.setLocked(!previousUnlocked);
                    }
                }
                
                // Nếu building này locked, các building sau cũng locked
                if (building.isLocked()) {
                    previousUnlocked = false;
                }
                
                buildings.add(building);
            }
            
            // Update UI on main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    adapter.setBuildings(buildings);
                });
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
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
