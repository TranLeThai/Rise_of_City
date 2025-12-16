package com.example.rise_of_city.ui.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.rise_of_city.data.model.BuildingProgress;
import com.example.rise_of_city.data.repository.BuildingProgressRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel cho RoadMapFragment
 */
public class RoadMapViewModel extends ViewModel {
    private static final String TAG = "RoadMapViewModel";
    
    private BuildingProgressRepository repository;
    private MutableLiveData<List<BuildingProgress>> buildingsLiveData;
    private MutableLiveData<Integer> totalCompletedBuildings;
    private MutableLiveData<String> errorMessage;
    
    // Danh sách building IDs theo thứ tự trong roadmap
    private static final String[] BUILDING_ORDER = {
        "house", "coffee", "library", "park", "school", "bakery", "farmer", "clothers"
    };
    
    // Mapping building ID -> tên hiển thị
    private static final Map<String, String> BUILDING_NAMES = new HashMap<String, String>() {{
        put("house", "Nhà Của Tôi");
        put("coffee", "Tiệm Cafe");
        put("library", "Thư Viện");
        put("park", "Công Viên");
        put("school", "Trường Học");
        put("bakery", "Tiệm Bánh");
        put("farmer", "Nông Trại");
        put("clothers", "Shop Quần Áo");
    }};
    
    public RoadMapViewModel() {
        repository = BuildingProgressRepository.getInstance();
        buildingsLiveData = new MutableLiveData<>();
        totalCompletedBuildings = new MutableLiveData<>(0);
        errorMessage = new MutableLiveData<>();
    }
    
    public LiveData<List<BuildingProgress>> getBuildings() {
        return buildingsLiveData;
    }
    
    public LiveData<Integer> getTotalCompletedBuildings() {
        return totalCompletedBuildings;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Load tất cả building progress từ Firebase
     */
    public void loadBuildingProgress() {
        repository.getAllBuildingProgress(new BuildingProgressRepository.OnAllBuildingsLoadedListener() {
            @Override
            public void onBuildingsLoaded(Map<String, Map<String, Object>> buildingProgressMap) {
                // Load thông tin building từ collection buildings
                loadBuildingInfo(buildingProgressMap);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading building progress: " + error);
                errorMessage.setValue(error);
                // Tạo dữ liệu mẫu nếu không load được
                createDefaultBuildings();
            }
        });
    }
    
    /**
     * Load thông tin building và kết hợp với progress
     */
    private void loadBuildingInfo(Map<String, Map<String, Object>> buildingProgressMap) {
        List<BuildingProgress> buildings = new ArrayList<>();
        // Sử dụng array để có thể thay đổi giá trị trong inner class
        final int[] completedCount = {0};
        final int[] loadedCount = {0};
        final int totalBuildings = BUILDING_ORDER.length;
        
        for (String buildingId : BUILDING_ORDER) {
            repository.getBuildingInfo(buildingId, new BuildingProgressRepository.OnBuildingInfoLoadedListener() {
                @Override
                public void onBuildingInfoLoaded(Map<String, Object> buildingInfo) {
                    // Lấy progress của user (nếu có)
                    Map<String, Object> progress = buildingProgressMap.get(buildingId);
                    
                    BuildingProgress buildingProgress = createBuildingProgress(
                        buildingId, buildingInfo, progress
                    );
                    
                    buildings.add(buildingProgress);
                    
                    if (buildingProgress.isCompleted()) {
                        completedCount[0]++;
                    }
                    
                    loadedCount[0]++;
                    
                    // Khi load xong tất cả, sort và update LiveData
                    if (loadedCount[0] == totalBuildings) {
                        // Sort buildings: progress first, then by difficulty
                        sortBuildings(buildings);
                        buildingsLiveData.setValue(buildings);
                        totalCompletedBuildings.setValue(completedCount[0]);
                    }
                }
                
                @Override
                public void onError(String error) {
                    Log.w(TAG, "Error loading building info for " + buildingId + ": " + error);
                    // Tạo building mặc định
                    BuildingProgress defaultBuilding = createDefaultBuilding(buildingId);
                    buildings.add(defaultBuilding);
                    
                    loadedCount[0]++;
                    if (loadedCount[0] == totalBuildings) {
                        // Sort buildings: progress first, then by difficulty
                        sortBuildings(buildings);
                        buildingsLiveData.setValue(buildings);
                        totalCompletedBuildings.setValue(completedCount[0]);
                    }
                }
            });
        }
    }
    
    /**
     * Tạo BuildingProgress từ building info và user progress
     */
    private BuildingProgress createBuildingProgress(String buildingId, 
                                                    Map<String, Object> buildingInfo,
                                                    Map<String, Object> userProgress) {
        String buildingName = BUILDING_NAMES.getOrDefault(buildingId, buildingId);
        if (buildingInfo != null && buildingInfo.containsKey("name")) {
            buildingName = (String) buildingInfo.get("name");
        }
        
        int vocabularyCount = 0;
        if (buildingInfo != null && buildingInfo.containsKey("vocabularyCount")) {
            Object count = buildingInfo.get("vocabularyCount");
            if (count instanceof Long) {
                vocabularyCount = ((Long) count).intValue();
            } else if (count instanceof Integer) {
                vocabularyCount = (Integer) count;
            }
        }
        
        // Lấy progress từ user
        int level = 1;
        int currentExp = 0;
        int maxExp = 100;
        boolean completed = false;
        boolean locked = false;
        
        if (userProgress != null) {
            if (userProgress.containsKey("level")) {
                Object lvl = userProgress.get("level");
                if (lvl instanceof Long) {
                    level = ((Long) lvl).intValue();
                } else if (lvl instanceof Integer) {
                    level = (Integer) lvl;
                }
            }
            
            if (userProgress.containsKey("currentExp")) {
                Object exp = userProgress.get("currentExp");
                if (exp instanceof Long) {
                    currentExp = ((Long) exp).intValue();
                } else if (exp instanceof Integer) {
                    currentExp = (Integer) exp;
                }
            }
            
            if (userProgress.containsKey("maxExp")) {
                Object max = userProgress.get("maxExp");
                if (max instanceof Long) {
                    maxExp = ((Long) max).intValue();
                } else if (max instanceof Integer) {
                    maxExp = (Integer) max;
                }
            }
            
            if (userProgress.containsKey("completed")) {
                completed = Boolean.TRUE.equals(userProgress.get("completed"));
            }
        }
        
        // Logic: Building đầu tiên (house) không bị khóa, các building khác cần hoàn thành building trước
        if (!buildingId.equals("house")) {
            // Kiểm tra building trước đã completed chưa
            int currentIndex = -1;
            for (int i = 0; i < BUILDING_ORDER.length; i++) {
                if (BUILDING_ORDER[i].equals(buildingId)) {
                    currentIndex = i;
                    break;
                }
            }
            
            if (currentIndex > 0) {
                String previousBuildingId = BUILDING_ORDER[currentIndex - 1];
                // TODO: Kiểm tra previous building completed (có thể cache trong ViewModel)
                // Tạm thời: building thứ 2 trở đi bị locked nếu chưa có progress
                if (userProgress == null) {
                    locked = true;
                }
            }
        }
        
        BuildingProgress buildingProgress = new BuildingProgress(
            buildingId, buildingName, level, currentExp, maxExp, completed, locked
        );
        buildingProgress.setVocabularyCount(vocabularyCount);
        // TODO: Tính vocabularyLearned từ quiz history
        
        return buildingProgress;
    }
    
    /**
     * Tạo building mặc định
     */
    private BuildingProgress createDefaultBuilding(String buildingId) {
        String buildingName = BUILDING_NAMES.getOrDefault(buildingId, buildingId);
        boolean locked = !buildingId.equals("house");
        
        return new BuildingProgress(buildingId, buildingName, 1, 0, 100, false, locked);
    }
    
    /**
     * Tạo danh sách building mặc định (fallback)
     */
    private void createDefaultBuildings() {
        List<BuildingProgress> buildings = new ArrayList<>();
        for (String buildingId : BUILDING_ORDER) {
            buildings.add(createDefaultBuilding(buildingId));
        }
        sortBuildings(buildings);
        buildingsLiveData.setValue(buildings);
        totalCompletedBuildings.setValue(0);
    }
    
    /**
     * Sắp xếp buildings: 
     * 1. Buildings đã có progress (unlocked/completed) trước
     * 2. Sau đó theo độ khó (vocabulary count - ít = dễ hơn)
     */
    private void sortBuildings(List<BuildingProgress> buildings) {
        buildings.sort((b1, b2) -> {
            // 1. Ưu tiên buildings đã có progress (unlocked hoặc completed)
            boolean b1HasProgress = !b1.isLocked() || b1.getCurrentExp() > 0 || b1.isCompleted();
            boolean b2HasProgress = !b2.isLocked() || b2.getCurrentExp() > 0 || b2.isCompleted();
            
            if (b1HasProgress && !b2HasProgress) {
                return -1; // b1 trước
            }
            if (!b1HasProgress && b2HasProgress) {
                return 1; // b2 trước
            }
            
            // 2. Nếu cùng có progress hoặc cùng không có, sort theo độ khó (vocabulary count)
            // Ít từ vựng = dễ hơn = hiển thị trước
            int vocab1 = b1.getVocabularyCount();
            int vocab2 = b2.getVocabularyCount();
            
            if (vocab1 != vocab2) {
                return Integer.compare(vocab1, vocab2); // Tăng dần (dễ -> khó)
            }
            
            // 3. Nếu cùng vocabulary count, giữ nguyên thứ tự BUILDING_ORDER
            int index1 = -1, index2 = -1;
            for (int i = 0; i < BUILDING_ORDER.length; i++) {
                if (BUILDING_ORDER[i].equals(b1.getBuildingId())) {
                    index1 = i;
                }
                if (BUILDING_ORDER[i].equals(b2.getBuildingId())) {
                    index2 = i;
                }
            }
            return Integer.compare(index1, index2);
        });
    }
}

