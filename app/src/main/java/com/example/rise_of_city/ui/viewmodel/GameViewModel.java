package com.example.rise_of_city.ui.viewmodel;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.rise_of_city.data.model.Building;
import com.example.rise_of_city.data.repository.GameRepository;
import com.example.rise_of_city.data.repository.BuildingProgressRepository;

public class GameViewModel extends ViewModel {

    private GameRepository repository;
    private BuildingProgressRepository progressRepository;

    // LiveData để UI lắng nghe
    private MutableLiveData<Building> selectedBuilding = new MutableLiveData<>();
    private MutableLiveData<java.util.Map<String, Boolean>> buildingsLockStatus = new MutableLiveData<>();

    // --- KHỞI TẠO ---
    // Vì ViewModel mặc định không nhận Context, ta cần hàm init này
    public void init(Context context) {
        if (repository == null) {
            repository = GameRepository.getInstance(context);
        }
        if (progressRepository == null) {
            progressRepository = BuildingProgressRepository.getInstance();
        }
    }

    // --- GETTER CHO UI ---
    public LiveData<Building> getSelectedBuilding() {
        return selectedBuilding;
    }
    
    public LiveData<java.util.Map<String, Boolean>> getBuildingsLockStatus() {
        return buildingsLockStatus;
    }

    // --- XỬ LÝ SỰ KIỆN ---

    // Khi người dùng click vào tòa nhà
    // Sử dụng Firebase data thay vì mock data để đảm bảo consistency với roadmap
    public void onBuildingClicked(String buildingId) {
        // Load từ Firebase để có data chính xác (giống như roadmap)
        loadBuildingFromFirebase(buildingId);
    }
    
    /**
     * Load building từ Firebase khi navigate từ roadmap hoặc click vào building
     * Sử dụng BuildingProgressRepository để lấy data thực tế từ Firebase
     */
    public void loadBuildingFromFirebase(String buildingId) {
        if (progressRepository == null || buildingId == null || buildingId.isEmpty()) {
            // Fallback về mock data nếu không có repository
            fallbackToMockData(buildingId);
            return;
        }
        
        // Load building info từ collection buildings
        progressRepository.getBuildingInfo(buildingId, new BuildingProgressRepository.OnBuildingInfoLoadedListener() {
            @Override
            public void onBuildingInfoLoaded(java.util.Map<String, Object> buildingInfo) {
                // Kiểm tra xem building có tồn tại trong user progress không
                // Nếu không có, nghĩa là building chưa unlock
                progressRepository.getAllBuildingProgress(new BuildingProgressRepository.OnAllBuildingsLoadedListener() {
                    @Override
                    public void onBuildingsLoaded(java.util.Map<String, java.util.Map<String, Object>> buildingProgressMap) {
                        // Kiểm tra xem building có trong user progress không
                        boolean isUnlocked = buildingProgressMap.containsKey(buildingId);
                        
                        // Load user progress cho building này
                        progressRepository.getBuildingProgress(buildingId, new BuildingProgressRepository.OnProgressLoadedListener() {
                            @Override
                            public void onProgressLoaded(int level, int currentExp, int maxExp) {
                                // Tạo Building object từ data Firebase
                                String buildingName = (String) buildingInfo.get("name");
                                if (buildingName == null) {
                                    buildingName = buildingId; // Fallback
                                }
                                
                                // Kiểm tra locked status: locked nếu không có trong Firebase user progress
                                boolean isLocked = !isUnlocked;
                                
                                boolean hasMission = Boolean.TRUE.equals(buildingInfo.get("hasMission"));
                                String requiredLessonName = (String) buildingInfo.get("requiredLessonName");
                                
                                Building building = new Building(
                                    buildingId,
                                    buildingName,
                                    level,
                                    currentExp,
                                    maxExp,
                                    hasMission,
                                    isLocked,
                                    requiredLessonName
                                );
                                
                                selectedBuilding.setValue(building);
                            }
                            
                            @Override
                            public void onError(String error) {
                                // Nếu không load được progress, coi như locked
                                fallbackToMockData(buildingId);
                            }
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        // Nếu không load được building progress, fallback về mock data
                        fallbackToMockData(buildingId);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                // Nếu không load được building info, fallback về mock data
                fallbackToMockData(buildingId);
            }
        });
    }
    
    /**
     * Fallback về mock data khi không load được từ Firebase
     */
    private void fallbackToMockData(String buildingId) {
        if (repository != null) {
            Building building = repository.getBuildingById(buildingId);
            if (building != null) {
                selectedBuilding.setValue(building);
            }
        }
    }

    // Đóng menu popup
    public void closeMenu() {
        selectedBuilding.setValue(null);
    }
    
    /**
     * Load tất cả buildings và kiểm tra locked status từ Firebase
     * Dùng để update visual state của buildings trên map
     */
    public void loadAllBuildingsLockStatus() {
        if (progressRepository == null) {
            return;
        }
        
        // Danh sách tất cả building IDs
        final java.util.List<String> allBuildingIds = java.util.Arrays.asList(
            "house", "school", "library", "park", "farmer", 
            "coffee", "clothers", "bakery"
        );
        
        // Lấy tất cả building progress của user
        progressRepository.getAllBuildingProgress(new BuildingProgressRepository.OnAllBuildingsLoadedListener() {
            @Override
            public void onBuildingsLoaded(java.util.Map<String, java.util.Map<String, Object>> buildingProgressMap) {
                // Map để lưu lock status của từng building
                final java.util.Map<String, Boolean> lockStatusMap = new java.util.HashMap<>();
                final java.util.concurrent.atomic.AtomicInteger loadedCount = new java.util.concurrent.atomic.AtomicInteger(0);
                
                // Load building info cho từng building
                for (String buildingId : allBuildingIds) {
                    progressRepository.getBuildingInfo(buildingId, new BuildingProgressRepository.OnBuildingInfoLoadedListener() {
                        @Override
                        public void onBuildingInfoLoaded(java.util.Map<String, Object> buildingInfo) {
                            // CHỈ unlock nếu building có trong Firebase user progress
                            // Nếu có trong buildingProgressMap, nghĩa là đã unlock
                            boolean isUnlocked = buildingProgressMap.containsKey(buildingId);
                            
                            if (isUnlocked) {
                                // Building đã unlock (có trong Firebase user progress)
                                lockStatusMap.put(buildingId, false);
                            } else {
                                // Building chưa unlock (không có trong Firebase user progress)
                                lockStatusMap.put(buildingId, true);
                            }
                            
                            // Nếu đã load xong tất cả buildings, update LiveData
                            int count = loadedCount.incrementAndGet();
                            if (count == allBuildingIds.size()) {
                                buildingsLockStatus.setValue(lockStatusMap);
                            }
                        }
                        
                        @Override
                        public void onError(String error) {
                            // Nếu không load được building info, kiểm tra xem có trong progress không
                            boolean isUnlocked = buildingProgressMap.containsKey(buildingId);
                            lockStatusMap.put(buildingId, !isUnlocked); // Locked nếu không có trong progress
                            
                            int count = loadedCount.incrementAndGet();
                            if (count == allBuildingIds.size()) {
                                buildingsLockStatus.setValue(lockStatusMap);
                            }
                        }
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                // Nếu không load được progress, TẤT CẢ buildings đều locked
                java.util.Map<String, Boolean> defaultMap = new java.util.HashMap<>();
                for (String buildingId : allBuildingIds) {
                    defaultMap.put(buildingId, true); // Tất cả đều locked
                }
                buildingsLockStatus.setValue(defaultMap);
            }
        });
    }
    
    /**
     * Unlock building với vàng
     */
    public void unlockBuilding(String buildingId) {
        if (progressRepository == null) {
            return;
        }
        
        progressRepository.unlockBuilding(buildingId, new BuildingProgressRepository.OnProgressUpdatedListener() {
            @Override
            public void onProgressUpdated(long level, int currentExp, int maxExp) {
                // Building đã được unlock, reload lock status để update UI
                loadAllBuildingsLockStatus();
                // Reload building để update selected building state
                loadBuildingFromFirebase(buildingId);
            }
            
            @Override
            public void onError(String error) {
                // Error handling
            }
        });
    }
}