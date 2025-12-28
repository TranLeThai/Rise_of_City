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

        // Khởi tạo Room Database
        database = AppDatabase.getInstance(context);
        buildingDao = database.userBuildingDao();

        // Bắt đầu hệ thống nhiệm vụ
        startMissionSystem();
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
                missionHandler.postDelayed(this, 300000); // 5 phút
            }
        };
        missionHandler.post(missionCheckRunnable);
    }

    private void checkAndGenerateMissions() {
        Map<String, Boolean> status = buildingsLockStatus.getValue();
        if (status == null) return;

        List<String> unlockedIds = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : status.entrySet()) {
            if (!entry.getValue()) { // false = đã mở khóa
                unlockedIds.add(entry.getKey());
            }
        }

        if (!unlockedIds.isEmpty() && new Random().nextFloat() < 0.3f) {
            String randomBuildingId = unlockedIds.get(new Random().nextInt(unlockedIds.size()));
            String missionTitle = getMissionTitleForBuilding(randomBuildingId);

            Mission newMission = new Mission(missionTitle, randomBuildingId, Mission.Type.RANDOM);
            List<Mission> current = new ArrayList<>(activeMissions.getValue());
            current.add(newMission);
            activeMissions.postValue(current);
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
        if (goldRepo != null) {
            goldRepo.addGold(-penalty, new GoldRepository.OnGoldUpdatedListener() {
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
                // Có thể cộng thưởng vàng ở đây nếu cần
                break;
            }
        }
        activeMissions.postValue(new ArrayList<>(current));
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