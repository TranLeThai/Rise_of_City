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
    
    // Thứ tự building trong roadmap - logic phát triển thành phố hợp lý
    // Roadmap được thiết kế theo trình tự tự nhiên của việc xây dựng cộng đồng:
    // 1. Cơ sở hạ tầng cơ bản (nhà ở)
    // 2. Giáo dục và kiến thức (trường học, thư viện)
    // 3. Giải trí và thư giãn (công viên)
    // 4. Sản xuất và kinh tế (nông trại)
    // 5. Dịch vụ và thương mại (tiệm bánh, quán cafe, shop quần áo)
    private static final String[] BUILDING_ORDER = {
        "house",        // 🏠 Nhà ở - Nơi an cư lạc nghiệp, nền tảng của mọi cộng đồng
        "school",       // 🏫 Trường học - Giáo dục và phát triển tri thức
        "library",      // 📚 Thư viện - Nghiên cứu và học tập nâng cao
        "park",         // 🌳 Công viên - Giải trí và thư giãn cho cộng đồng
        "farm",         // 🌾 Nông trại - Sản xuất lương thực, đảm bảo an ninh thực phẩm
        "bakery",       // 🥖 Tiệm bánh - Dịch vụ ăn uống cơ bản
        "coffee",       // ☕ Quán cafe - Nơi giao lưu văn hóa và xã hội
        "clothers"      // 👕 Shop quần áo - Thương mại và thời trang
    };
    
    // Tên hiển thị
    private static final Map<String, String> BUILDING_NAMES = Map.of(
        "house", "Nhà ở",
        "school", "Trường học",
        "library", "Thư viện",
        "park", "Công viên",
        "farm", "Nông trại",
        "bakery", "Tiệm Bánh",
        "coffee", "Quán Cafe",
        "clothers", "Shop Quần Áo"
    );
    
    // Số từ vựng giả định
    private static final Map<String, Integer> VOCABULARY_COUNTS = Map.of(
        "house", 50,      // Nhà ở - cơ bản
        "school", 120,    // Trường học - nhiều từ vựng học thuật
        "library", 200,   // Thư viện - nhiều từ vựng sách vở
        "park", 45,       // Công viên - ít từ vựng hơn
        "farm", 60,       // Nông trại - từ vựng nông nghiệp
        "bakery", 75,     // Tiệm bánh - từ vựng thực phẩm
        "coffee", 80,     // Quán cafe - từ vựng đồ uống
        "clothers", 90    // Shop quần áo - từ vựng thời trang
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
                    // ✅ Building đã unlock (có trong database với level > 0)
                    building.setLevel(userBuilding.level);
                    building.setCurrentExp(0);
                    building.setMaxExp(100);
                    building.setCompleted(userBuilding.level >= 4); // Level 4 = completed
                    building.setVocabularyLearned(0);
                    building.setLocked(false); // Đã unlock - hiển thị xanh dương

                    previousUnlocked = true; // Cho phép building sau unlock
                } else {
                    // 🔒 Building chưa unlock
                    building.setLevel(1);
                    building.setCurrentExp(0);
                    building.setMaxExp(100);
                    building.setCompleted(false);
                    building.setVocabularyLearned(0);

                    // Logic unlock theo thứ tự roadmap
                    if (i == 0) {
                        // 🏠 House - luôn có thể unlock đầu tiên
                        building.setLocked(false); // House luôn available để unlock
                        previousUnlocked = true;
                    } else {
                        // Kiểm tra building trước đã completed chưa
                        String prevBuildingId = BUILDING_ORDER[i-1];
                        UserBuilding prevBuilding = buildingMap.get(prevBuildingId);

                        if (prevBuilding != null && prevBuilding.level >= 4) {
                            // Building trước đã completed - cho phép unlock building này
                            building.setLocked(false); // Available để unlock
                            previousUnlocked = true;
                        } else {
                            // Building trước chưa completed - locked
                            building.setLocked(true); // Locked - hiển thị xám
                            previousUnlocked = false;
                        }
                    }
                }

                    buildings.add(building);
                }

                // Sort buildings: unlocked buildings lên đầu, locked buildings xuống cuối
                sortBuildingsByUnlockStatus(buildings);

                // Update UI on main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        adapter.setBuildings(buildings);

                        // Scroll to top để xem buildings đã unlock
                        if (recyclerView != null) {
                            recyclerView.post(() -> {
                                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                                if (layoutManager != null) {
                                    layoutManager.scrollToPositionWithOffset(0, 0);
                                }
                            });
                        }
                    });
                }
        });
    }

    /**
     * Sort buildings: unlocked buildings lên đầu, locked buildings xuống cuối
     */
    private void sortBuildingsByUnlockStatus(List<BuildingProgress> buildings) {
        if (buildings == null || buildings.size() <= 1) return;

        buildings.sort((b1, b2) -> {
            // Rule 1: Unlocked buildings (không locked) lên trước
            if (!b1.isLocked() && b2.isLocked()) return -1;  // b1 unlocked, b2 locked → b1 lên trước
            if (b1.isLocked() && !b2.isLocked()) return 1;   // b1 locked, b2 unlocked → b2 lên trước

            // Rule 2: Nếu cùng trạng thái, ưu tiên completed buildings
            if (b1.isCompleted() && !b2.isCompleted()) return -1;
            if (!b1.isCompleted() && b2.isCompleted()) return 1;

            // Rule 3: Nếu cùng trạng thái completed, ưu tiên level cao hơn
            if (!b1.isLocked() && !b2.isLocked()) {
                return Integer.compare(b2.getLevel(), b1.getLevel()); // Level cao hơn lên trước
            }

            // Rule 4: Nếu đều locked, ưu tiên building có thể unlock sớm hơn
            if (b1.isLocked() && b2.isLocked()) {
                return Integer.compare(b1.getLevel(), b2.getLevel()); // Level thấp hơn lên trước (dễ unlock hơn)
            }

            return 0;
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
