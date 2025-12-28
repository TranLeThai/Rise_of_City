package com.example.rise_of_city.ui.viewmodel;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.UserBuilding;
import com.example.rise_of_city.data.local.UserBuildingDao;
import com.example.rise_of_city.data.model.game.Building;
import com.example.rise_of_city.data.model.game.Mission;
import com.example.rise_of_city.data.repository.GameRepository;
import com.example.rise_of_city.data.repository.GoldRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameViewModel extends ViewModel {

    private GameRepository repository;
    private GoldRepository goldRepo;

    // Room Database
    private AppDatabase database;
    private UserBuildingDao buildingDao;
    private int currentUserId = 1; // TODO: Thay bằng userId thực từ login (SharedPreferences hoặc ViewModel khác)
    private Context appContext; // Lưu Application context để sử dụng trong quest generator

    private MutableLiveData<Building> selectedBuilding = new MutableLiveData<>();
    private MutableLiveData<Map<String, Boolean>> buildingsLockStatus = new MutableLiveData<>();

    // Hệ thống nhiệm vụ
    private MutableLiveData<List<Mission>> activeMissions = new MutableLiveData<>(new ArrayList<>());
    private Handler missionHandler = new Handler(Looper.getMainLooper());
    private Runnable missionCheckRunnable;

    public void init(Context context) {
        if (repository == null) {
            repository = GameRepository.getInstance(context);
        }
        if (goldRepo == null) {
            goldRepo = GoldRepository.getInstance();
        }

        // Lưu Application context (safe để giữ trong ViewModel)
        appContext = context.getApplicationContext();

        // Khởi tạo Room Database
        database = AppDatabase.getInstance(context);
        buildingDao = database.userBuildingDao();

        // Bắt đầu hệ thống nhiệm vụ
        startMissionSystem();
        
        // Tạo quest ngay khi login (mỗi lần mở app)
        generateInitialQuestsOnLogin();
    }

    // Getter
    public LiveData<Building> getSelectedBuilding() { return selectedBuilding; }
    public LiveData<Map<String, Boolean>> getBuildingsLockStatus() { return buildingsLockStatus; }
    public LiveData<List<Mission>> getActiveMissions() { return activeMissions; }

    // ==================== HỆ THỐNG NHIỆM VỤ ====================

    private void startMissionSystem() {
        if (missionCheckRunnable != null) return;

        missionCheckRunnable = new Runnable() {
            @Override
            public void run() {
                checkAndGenerateMissions();
                checkMissionExpiration();
                // Random delay 10-15 phút (600000-900000 ms)
                long delay = 600000 + (long)(Math.random() * 300000);
                missionHandler.postDelayed(this, delay);
            }
        };
        missionHandler.post(missionCheckRunnable);
    }
    
    /**
     * Tạo quest ban đầu mỗi lần login
     * Sẽ tạo 2-3 quest đa dạng cho các building khác nhau
     */
    private void generateInitialQuestsOnLogin() {
        if (appContext == null) return;
        
        // Kiểm tra xem đã tạo quest cho session này chưa
        android.content.SharedPreferences prefs = appContext.getSharedPreferences("RiseOfCity_Prefs", Context.MODE_PRIVATE);
        long lastLoginTime = prefs.getLong("last_quest_generation_time", 0);
        long currentTime = System.currentTimeMillis();
        
        // Nếu đã tạo quest trong vòng 5 phút qua thì không tạo nữa (tránh spam)
        // NOTE: Đổi thành 3600000 (1 giờ) khi release production
        if (currentTime - lastLoginTime < 300000) { // 5 phút cho testing
            return;
        }
        
        // Lưu thời gian tạo quest
        prefs.edit().putLong("last_quest_generation_time", currentTime).apply();
        
        // Load danh sách building đã unlock
        new Thread(() -> {
            if (buildingDao == null) return;
            
            List<UserBuilding> ownedBuildings = buildingDao.getBuildingsForUser(currentUserId);
            List<String> allBuildingIds = Arrays.asList(
                "house", "school", "library", "park", "farmer",
                "coffee", "clothers", "bakery"
            );
            
            // Tạo danh sách building có thể tạo quest
            List<String> availableForQuest = new ArrayList<>();
            
            // Thêm các building đã unlock
            for (UserBuilding ub : ownedBuildings) {
                availableForQuest.add(ub.buildingId);
            }
            
            // Thêm một số building bị lock (để tạo quest học bài mở khóa)
            for (String id : allBuildingIds) {
                boolean isOwned = false;
                for (UserBuilding ub : ownedBuildings) {
                    if (ub.buildingId.equals(id)) {
                        isOwned = true;
                        break;
                    }
                }
                if (!isOwned && availableForQuest.size() < 6) {
                    availableForQuest.add(id);
                }
            }
            
            // Tạo 2-3 quest ngẫu nhiên
            int numQuests = 2 + new Random().nextInt(2); // 2 hoặc 3 quest
            List<Mission> newMissions = new ArrayList<>();
            
            for (int i = 0; i < Math.min(numQuests, availableForQuest.size()); i++) {
                // Chọn ngẫu nhiên một building (không trùng)
                String buildingId = availableForQuest.remove(new Random().nextInt(availableForQuest.size()));
                
                // Load building info
                Building mockBuilding = repository != null ? repository.getBuildingById(buildingId) : null;
                if (mockBuilding == null) continue;
                
                UserBuilding userBuilding = buildingDao.getBuilding(currentUserId, buildingId);
                boolean isLocked = userBuilding == null;
                int level = userBuilding != null ? userBuilding.level : 0;
                
                Building actualBuilding = new Building(
                    buildingId,
                    mockBuilding.getName(),
                    level,
                    0,
                    100 * (level + 1),
                    false,
                    isLocked,
                    mockBuilding.getRequiredLessonName()
                );
                
                // Generate quest với type đa dạng
                com.example.rise_of_city.data.repository.BuildingQuestGenerator questGenerator = 
                    new com.example.rise_of_city.data.repository.BuildingQuestGenerator(appContext);
                Mission mission = questGenerator.generateSmartQuest(buildingId, actualBuilding, null);
                
                if (mission != null) {
                    newMissions.add(mission);
                }
            }
            
            // Cập nhật danh sách quest
            if (!newMissions.isEmpty()) {
                postToMain(() -> {
                    activeMissions.setValue(newMissions);
                });
            }
        }).start();
    }

    private void checkAndGenerateMissions() {
        Map<String, Boolean> status = buildingsLockStatus.getValue();
        if (status == null || repository == null) return;

        List<String> unlockedIds = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : status.entrySet()) {
            if (!entry.getValue()) { // false = đã mở khóa
                unlockedIds.add(entry.getKey());
            }
        }

        if (!unlockedIds.isEmpty() && new Random().nextFloat() < 0.3f) {
            String randomBuildingId = unlockedIds.get(new Random().nextInt(unlockedIds.size()));
            Building building = repository.getBuildingById(randomBuildingId);
            
            if (building != null) {
                // Load building info from Room database
                new Thread(() -> {
                    UserBuilding userBuilding = buildingDao != null ? buildingDao.getBuilding(currentUserId, randomBuildingId) : null;
                    boolean isLocked = userBuilding == null;
                    int level = userBuilding != null ? userBuilding.level : 0;
                    
                    // Create building with actual data
                    Building actualBuilding = new Building(
                        randomBuildingId,
                        building.getName(),
                        level,
                        0,
                        100 * (level + 1),
                        false,
                        isLocked,
                        building.getRequiredLessonName()
                    );
                    
                    // Generate smart quest
                    if (appContext != null) {
                        com.example.rise_of_city.data.repository.BuildingQuestGenerator questGenerator = 
                            new com.example.rise_of_city.data.repository.BuildingQuestGenerator(appContext);
                        Mission newMission = questGenerator.generateSmartQuest(randomBuildingId, actualBuilding, null);
                        
                        if (newMission != null) {
                            postToMain(() -> {
                                List<Mission> current = new ArrayList<>(activeMissions.getValue() != null ? activeMissions.getValue() : new ArrayList<>());
                                current.add(newMission);
                                activeMissions.postValue(current);
                            });
                        }
                    }
                }).start();
            }
        }
    }

    private void checkMissionExpiration() {
        List<Mission> current = activeMissions.getValue();
        if (current == null || current.isEmpty()) return;

        long now = System.currentTimeMillis();
        List<Mission> toRemove = new ArrayList<>();
        boolean penaltyApplied = false;

        for (Mission mission : current) {
            if (!mission.isCompleted && (now - mission.startTime) > mission.durationMs) {
                applyPenalty(mission.goldPenalty);
                toRemove.add(mission);
                penaltyApplied = true;
            }
        }

        if (penaltyApplied) {
            current.removeAll(toRemove);
            activeMissions.postValue(new ArrayList<>(current));
        }
    }

    private void applyPenalty(int penalty) {
        if (goldRepo != null && appContext != null && penalty > 0) {
            goldRepo.spendGold(appContext, penalty, new GoldRepository.OnGoldUpdatedListener() {
                @Override
                public void onGoldUpdated(int newGold) {}
                @Override
                public void onError(String error) {}
            });
        }
    }

    private String getMissionTitleForBuilding(String id) {
        switch (id) {
            case "house": return "Sắp xếp nhà cửa";
            case "bakery": return "Nướng bánh mì nóng";
            case "coffee": return "Pha cà phê sáng";
            case "library": return "Sắp xếp sách";
            case "school": return "Soạn giáo án";
            default: return "Kiểm tra công trình " + id;
        }
    }

    public void completeMission(String missionId) {
        List<Mission> current = activeMissions.getValue();
        if (current == null) return;

        for (Mission m : current) {
            if (m.id.equals(missionId)) {
                m.isCompleted = true;
                // Remove completed mission from list (hoặc có thể giữ lại nhưng mark là completed)
                // Ở đây ta sẽ remove để không hiển thị nữa
                break;
            }
        }
        
        // Remove completed missions
        List<Mission> updatedList = new ArrayList<>();
        for (Mission m : current) {
            if (!m.isCompleted) {
                updatedList.add(m);
            }
        }
        activeMissions.postValue(updatedList);
    }

    // ==================== DỮ LIỆU CÔNG TRÌNH (ROOM) ====================

    public void loadBuildingById(String buildingId) {
        loadBuildingFromLocal(buildingId);
    }

    public void onBuildingClicked(String buildingId) {
        loadBuildingFromLocal(buildingId);
    }

    private void loadBuildingFromLocal(String buildingId) {
        if (buildingDao == null || buildingId == null || buildingId.isEmpty()) {
            fallbackToMockData(buildingId);
            return;
        }

        new Thread(() -> {
            Building mock = repository.getBuildingById(buildingId);
            if (mock == null) {
                postToMain(() -> fallbackToMockData(buildingId));
                return;
            }

            UserBuilding userBuilding = buildingDao.getBuilding(currentUserId, buildingId);
            boolean isLocked = userBuilding == null;
            int level = userBuilding != null ? userBuilding.level : 0;

            // TODO: Nếu muốn lưu exp, thêm field vào UserBuilding
            int currentExp = 0;
            int maxExp = 100 * (level + 1);

            boolean hasMission = false;
            List<Mission> missions = activeMissions.getValue();
            if (missions != null) {
                for (Mission m : missions) {
                    if (m.buildingId.equals(buildingId) && !m.isCompleted) {
                        hasMission = true;
                        break;
                    }
                }
            }

            Building building = new Building(
                    buildingId,
                    mock.getName(),
                    level,
                    currentExp,
                    maxExp,
                    hasMission,
                    isLocked,
                    mock.getRequiredLessonName()
            );

            postToMain(() -> selectedBuilding.setValue(building));
        }).start();
    }

    private void fallbackToMockData(String buildingId) {
        if (repository != null) {
            Building building = repository.getBuildingById(buildingId);
            if (building != null) {
                selectedBuilding.setValue(building);
            }
        }
    }

    public void closeMenu() {
        selectedBuilding.setValue(null);
    }

    public void loadAllBuildingsLockStatus() {
        if (buildingDao == null) return;

        final List<String> allBuildingIds = Arrays.asList(
                "house", "school", "library", "park", "farmer",
                "coffee", "clothers", "bakery"
        );

        new Thread(() -> {
            List<UserBuilding> owned = buildingDao.getBuildingsForUser(currentUserId);
            Map<String, Boolean> lockMap = new HashMap<>();

            for (String id : allBuildingIds) {
                boolean locked = true;
                for (UserBuilding ub : owned) {
                    if (ub.buildingId.equals(id)) {
                        locked = false;
                        break;
                    }
                }
                lockMap.put(id, locked);
            }

            postToMain(() -> buildingsLockStatus.setValue(lockMap));
        }).start();
    }

    public void unlockBuilding(String buildingId) {
        if (buildingDao == null) return;

        new Thread(() -> {
            UserBuilding existing = buildingDao.getBuilding(currentUserId, buildingId);
            if (existing == null) {
                UserBuilding newBuilding = new UserBuilding(currentUserId, buildingId, 1); // level 1 khi mới unlock
                buildingDao.insertOrUpdate(newBuilding); // cần method này trong DAO
            }

            postToMain(() -> {
                loadAllBuildingsLockStatus();
                loadBuildingFromLocal(buildingId);
            });
        }).start();
    }
    
    /**
     * Unlock building với cost vàng (50 vàng)
     * @param buildingId ID của building cần unlock
     * @param callback Callback để thông báo kết quả
     */
    public void unlockBuildingWithGold(String buildingId, UnlockCallback callback) {
        if (buildingDao == null || goldRepo == null) {
            if (callback != null) callback.onError("Database or gold repository not initialized");
            return;
        }
        
        final int UNLOCK_COST = 50; // Cost mở khóa = 50 vàng
        
        if (appContext == null) {
            if (callback != null) callback.onError("Context not initialized");
            return;
        }
        
        // Kiểm tra vàng trước
        goldRepo.checkCanUnlockBuilding(appContext, UNLOCK_COST, (canUnlock, currentGold, message) -> {
            if (!canUnlock) {
                if (callback != null) callback.onError(message);
                return;
            }
            
            // Trừ vàng
            goldRepo.spendGold(appContext, UNLOCK_COST, new GoldRepository.OnGoldUpdatedListener() {
                @Override
                public void onGoldUpdated(int newGold) {
                    // Unlock building
                    new Thread(() -> {
                        UserBuilding existing = buildingDao.getBuilding(currentUserId, buildingId);
                        if (existing == null) {
                            UserBuilding newBuilding = new UserBuilding(currentUserId, buildingId, 1);
                            buildingDao.insertOrUpdate(newBuilding);
                        }
                        
                        postToMain(() -> {
                            loadAllBuildingsLockStatus();
                            loadBuildingFromLocal(buildingId);
                            if (callback != null) callback.onSuccess(newGold);
                        });
                    }).start();
                }
                
                @Override
                public void onError(String error) {
                    if (callback != null) callback.onError(error);
                }
            });
        });
    }
    
    public interface UnlockCallback {
        void onSuccess(int newGold);
        void onError(String error);
    }

    private void postToMain(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    // ==================== CLEANUP ====================

    @Override
    protected void onCleared() {
        super.onCleared();
        if (missionHandler != null && missionCheckRunnable != null) {
            missionHandler.removeCallbacks(missionCheckRunnable);
        }
    }
}