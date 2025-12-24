package com.example.rise_of_city.ui.viewmodel;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.rise_of_city.data.model.game.Building;
import com.example.rise_of_city.data.model.game.Mission;
import com.example.rise_of_city.data.repository.GameRepository;
import com.example.rise_of_city.data.repository.BuildingProgressRepository;
import com.example.rise_of_city.data.repository.GoldRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class GameViewModel extends ViewModel {

    private GameRepository repository;
    private BuildingProgressRepository progressRepository;
    private GoldRepository goldRepo;

    private MutableLiveData<Building> selectedBuilding = new MutableLiveData<>();
    private MutableLiveData<Map<String, Boolean>> buildingsLockStatus = new MutableLiveData<>();

    // --- NEW: LiveData cho hệ thống nhiệm vụ ---
    private MutableLiveData<List<Mission>> activeMissions = new MutableLiveData<>(new ArrayList<>());
    private Handler missionHandler = new Handler(Looper.getMainLooper());
    private Runnable missionCheckRunnable;

    public void init(Context context) {
        if (repository == null) {
            repository = GameRepository.getInstance(context);
        }
        if (progressRepository == null) {
            progressRepository = BuildingProgressRepository.getInstance();
        }
        if (goldRepo == null) {
            goldRepo = GoldRepository.getInstance();
        }

        // Bắt đầu hệ thống nhiệm vụ tự động
        startMissionSystem();
    }

    public LiveData<Building> getSelectedBuilding() { return selectedBuilding; }
    public LiveData<Map<String, Boolean>> getBuildingsLockStatus() { return buildingsLockStatus; }
    public LiveData<List<Mission>> getActiveMissions() { return activeMissions; }

    // --- LOGIC NHIỆM VỤ MỚI ---

    private void startMissionSystem() {
        if (missionCheckRunnable != null) return;

        missionCheckRunnable = new Runnable() {
            @Override
            public void run() {
                checkAndGenerateMissions();
                checkMissionExpiration();
                // Kiểm tra mỗi 5 phút (300,000ms)
                missionHandler.postDelayed(this, 300000);
            }
        };
        missionHandler.post(missionCheckRunnable);
    }

    private void checkAndGenerateMissions() {
        Map<String, Boolean> status = buildingsLockStatus.getValue();
        if (status == null) return;

        List<String> unlockedIds = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : status.entrySet()) {
            if (!entry.getValue()) { // Nếu value là false nghĩa là isLocked = false -> đã mở khóa
                unlockedIds.add(entry.getKey());
            }
        }

        // Tỉ lệ 30% xuất hiện nhiệm vụ random nếu đã có ít nhất 1 nhà mở khóa
        if (!unlockedIds.isEmpty() && new Random().nextFloat() < 0.3) {
            String randomBuildingId = unlockedIds.get(new Random().nextInt(unlockedIds.size()));
            String missionTitle = getMissionTitleForBuilding(randomBuildingId);

            Mission newMission = new Mission(missionTitle, randomBuildingId, Mission.Type.RANDOM);
            List<Mission> currentMissions = activeMissions.getValue();
            if (currentMissions != null) {
                currentMissions.add(newMission);
                activeMissions.setValue(currentMissions);
            }
        }
    }

    private void checkMissionExpiration() {
        List<Mission> currentMissions = activeMissions.getValue();
        if (currentMissions == null || currentMissions.isEmpty()) return;

        long currentTime = System.currentTimeMillis();
        List<Mission> toRemove = new ArrayList<>();
        boolean penaltyApplied = false;

        for (Mission mission : currentMissions) {
            if (!mission.isCompleted && (currentTime - mission.startTime) > mission.durationMs) {
                // QUÁ 12 TIẾNG -> PHẠT
                applyPenalty(mission.goldPenalty);
                toRemove.add(mission);
                penaltyApplied = true;
            }
        }

        if (penaltyApplied) {
            currentMissions.removeAll(toRemove);
            activeMissions.setValue(currentMissions);
        }
    }

    private void applyPenalty(int penalty) {
        if (goldRepo != null) {
            goldRepo.addGold(-penalty, new GoldRepository.OnGoldUpdatedListener() {
                @Override
                public void onGoldUpdated(int newGold) {
                    // Cập nhật UI tiền tệ nếu cần
                }
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
                // Cộng thưởng vàng qua GoldRepo...
                break;
            }
        }
        activeMissions.setValue(current);
    }

    // --- GIỮ NGUYÊN CÁC CHỨC NĂNG CŨ ---

    public void loadBuildingById(String buildingId) {
        loadBuildingFromFirebase(buildingId);
    }

    public void onBuildingClicked(String buildingId) {
        loadBuildingFromFirebase(buildingId);
    }

    public void loadBuildingFromFirebase(String buildingId) {
        if (progressRepository == null || buildingId == null || buildingId.isEmpty()) {
            fallbackToMockData(buildingId);
            return;
        }

        progressRepository.getBuildingInfo(buildingId, new BuildingProgressRepository.OnBuildingInfoLoadedListener() {
            @Override
            public void onBuildingInfoLoaded(Map<String, Object> buildingInfo) {
                progressRepository.getAllBuildingProgress(new BuildingProgressRepository.OnAllBuildingsLoadedListener() {
                    @Override
                    public void onBuildingsLoaded(Map<String, Map<String, Object>> buildingProgressMap) {
                        boolean isUnlocked = buildingProgressMap.containsKey(buildingId);

                        progressRepository.getBuildingProgress(buildingId, new BuildingProgressRepository.OnProgressLoadedListener() {
                            @Override
                            public void onProgressLoaded(int level, int currentExp, int maxExp) {
                                String buildingName = (String) buildingInfo.get("name");
                                if (buildingName == null) buildingName = buildingId;

                                boolean isLocked = !isUnlocked;
                                boolean hasMission = false; // Mặc định false, logic nhiệm vụ giờ quản lý tập trung

                                // Kiểm tra xem công trình này có đang có nhiệm vụ trong list Active không
                                List<Mission> missions = activeMissions.getValue();
                                if (missions != null) {
                                    for (Mission m : missions) {
                                        if (m.buildingId.equals(buildingId)) {
                                            hasMission = true;
                                            break;
                                        }
                                    }
                                }

                                String requiredLessonName = (String) buildingInfo.get("requiredLessonName");

                                Building building = new Building(
                                        buildingId, buildingName, level, currentExp, maxExp,
                                        hasMission, isLocked, requiredLessonName
                                );

                                selectedBuilding.setValue(building);
                            }

                            @Override
                            public void onError(String error) { fallbackToMockData(buildingId); }
                        });
                    }
                    @Override
                    public void onError(String error) { fallbackToMockData(buildingId); }
                });
            }
            @Override
            public void onError(String error) { fallbackToMockData(buildingId); }
        });
    }

    private void fallbackToMockData(String buildingId) {
        if (repository != null) {
            Building building = repository.getBuildingById(buildingId);
            if (building != null) {
                selectedBuilding.setValue(building);
            }
        }
    }

    public void closeMenu() { selectedBuilding.setValue(null); }

    public void loadAllBuildingsLockStatus() {
        if (progressRepository == null) return;

        final List<String> allBuildingIds = Arrays.asList(
                "house", "school", "library", "park", "farmer",
                "coffee", "clothers", "bakery"
        );

        progressRepository.getAllBuildingProgress(new BuildingProgressRepository.OnAllBuildingsLoadedListener() {
            @Override
            public void onBuildingsLoaded(Map<String, Map<String, Object>> buildingProgressMap) {
                final Map<String, Boolean> lockStatusMap = new HashMap<>();
                final AtomicInteger loadedCount = new AtomicInteger(0);

                for (String buildingId : allBuildingIds) {
                    progressRepository.getBuildingInfo(buildingId, new BuildingProgressRepository.OnBuildingInfoLoadedListener() {
                        @Override
                        public void onBuildingInfoLoaded(Map<String, Object> buildingInfo) {
                            boolean isUnlocked = buildingProgressMap.containsKey(buildingId);
                            lockStatusMap.put(buildingId, !isUnlocked);

                            if (loadedCount.incrementAndGet() == allBuildingIds.size()) {
                                buildingsLockStatus.setValue(lockStatusMap);
                            }
                        }
                        @Override
                        public void onError(String error) {
                            boolean isUnlocked = buildingProgressMap.containsKey(buildingId);
                            lockStatusMap.put(buildingId, !isUnlocked);
                            if (loadedCount.incrementAndGet() == allBuildingIds.size()) {
                                buildingsLockStatus.setValue(lockStatusMap);
                            }
                        }
                    });
                }
            }
            @Override
            public void onError(String error) {
                Map<String, Boolean> defaultMap = new HashMap<>();
                for (String buildingId : allBuildingIds) defaultMap.put(buildingId, true);
                buildingsLockStatus.setValue(defaultMap);
            }
        });
    }

    public void unlockBuilding(String buildingId) {
        if (progressRepository == null) return;
        progressRepository.unlockBuilding(buildingId, new BuildingProgressRepository.OnProgressUpdatedListener() {
            @Override
            public void onProgressUpdated(long level, int currentExp, int maxExp) {
                loadAllBuildingsLockStatus();
                loadBuildingFromFirebase(buildingId);
            }
            @Override
            public void onError(String error) {}
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (missionHandler != null && missionCheckRunnable != null) {
            missionHandler.removeCallbacks(missionCheckRunnable);
        }
    }
}