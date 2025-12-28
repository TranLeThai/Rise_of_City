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
import com.example.rise_of_city.data.repository.BuildingQuestGenerator;
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
    private int currentUserId = 1; // TODO: Lấy từ login thực tế
    private Context appContext;

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

        appContext = context.getApplicationContext();

        database = AppDatabase.getInstance(context);
        buildingDao = database.userBuildingDao();

        startMissionSystem();
        generateInitialQuestsOnLogin();
    }

    // Getter
    public LiveData<Building> getSelectedBuilding() { return selectedBuilding; }
    public LiveData<Map<String, Boolean>> getBuildingsLockStatus() { return buildingsLockStatus; }
    public LiveData<List<Mission>> getActiveMissions() { return activeMissions; }

    // ==================== HỆ THỐNG NHIỆM VỤ ====================

    private void startMissionSystem() {
        if (missionCheckRunnable != null) return;

        missionCheckRunnable = () -> {
            checkAndGenerateMissions();
            checkMissionExpiration();
            long delay = 600000 + (long)(Math.random() * 300000); // 10-15 phút
            missionHandler.postDelayed(missionCheckRunnable, delay);
        };
        missionHandler.post(missionCheckRunnable);
    }

    private void generateInitialQuestsOnLogin() {
        if (appContext == null) return;

        android.content.SharedPreferences prefs = appContext.getSharedPreferences("RiseOfCity_Prefs", Context.MODE_PRIVATE);
        long lastLoginTime = prefs.getLong("last_quest_generation_time", 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastLoginTime < 300000) return; // 5 phút test

        prefs.edit().putLong("last_quest_generation_time", currentTime).apply();

        new Thread(() -> {
            List<UserBuilding> ownedBuildings = buildingDao.getBuildingsForUser(currentUserId);
            List<String> allBuildingIds = Arrays.asList("house", "school", "library", "park", "farmer", "coffee", "clothers", "bakery");

            List<String> availableForQuest = new ArrayList<>();
            for (UserBuilding ub : ownedBuildings) {
                availableForQuest.add(ub.buildingId);
            }

            // Thêm một số building bị khóa để tạo quest học bài
            for (String id : allBuildingIds) {
                boolean isOwned = ownedBuildings.stream().anyMatch(ub -> ub.buildingId.equals(id));
                if (!isOwned && availableForQuest.size() < 6) {
                    availableForQuest.add(id);
                }
            }

            int numQuests = 2 + new Random().nextInt(2);
            List<Mission> newMissions = new ArrayList<>();

            BuildingQuestGenerator generator = new BuildingQuestGenerator(appContext);

            for (int i = 0; i < Math.min(numQuests, availableForQuest.size()); i++) {
                String buildingId = availableForQuest.remove(new Random().nextInt(availableForQuest.size()));
                Building mockBuilding = repository.getBuildingById(buildingId);
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

                generator.generateMissionsForBuilding(buildingId, actualBuilding, mission -> {
                    if (mission != null) {
                        postToMain(() -> {
                            List<Mission> current = new ArrayList<>(activeMissions.getValue() != null ? activeMissions.getValue() : new ArrayList<>());
                            current.add(mission);
                            activeMissions.postValue(current);
                        });
                    }
                });
            }
        }).start();
    }

    private void checkAndGenerateMissions() {
        Map<String, Boolean> status = buildingsLockStatus.getValue();
        if (status == null || repository == null || appContext == null) return;

        List<String> unlockedIds = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : status.entrySet()) {
            if (!entry.getValue()) { // false = đã mở khóa
                unlockedIds.add(entry.getKey());
            }
        }

        if (!unlockedIds.isEmpty() && new Random().nextFloat() < 0.3f) {
            String randomBuildingId = unlockedIds.get(new Random().nextInt(unlockedIds.size()));
            Building mockBuilding = repository.getBuildingById(randomBuildingId);
            if (mockBuilding == null) return;

            new Thread(() -> {
                UserBuilding userBuilding = buildingDao.getBuilding(currentUserId, randomBuildingId);
                boolean isLocked = userBuilding == null;
                int level = userBuilding != null ? userBuilding.level : 0;

                Building actualBuilding = new Building(
                        randomBuildingId,
                        mockBuilding.getName(),
                        level,
                        0,
                        100 * (level + 1),
                        false,
                        isLocked,
                        mockBuilding.getRequiredLessonName()
                );

                BuildingQuestGenerator generator = new BuildingQuestGenerator(appContext);
                generator.generateMissionsForBuilding(randomBuildingId, actualBuilding, mission -> {
                    if (mission != null) {
                        postToMain(() -> {
                            List<Mission> current = new ArrayList<>(activeMissions.getValue() != null ? activeMissions.getValue() : new ArrayList<>());
                            current.add(mission);
                            activeMissions.postValue(current);
                        });
                    }
                });
            }).start();
        }
    }

    private void checkMissionExpiration() {
        List<Mission> current = activeMissions.getValue();
        if (current == null || current.isEmpty()) return;

        long now = System.currentTimeMillis();
        List<Mission> toRemove = new ArrayList<>();

        for (Mission mission : current) {
            if (!mission.isCompleted && (now - mission.startTime) > mission.durationMs) {
                applyPenalty(mission.goldPenalty);
                toRemove.add(mission);
            }
        }

        if (!toRemove.isEmpty()) {
            current.removeAll(toRemove);
            activeMissions.postValue(new ArrayList<>(current));
        }
    }

    private void applyPenalty(int penalty) {
        if (goldRepo != null && appContext != null && penalty > 0) {
            goldRepo.addGold(appContext, -penalty, null);
        }
    }

    public void completeMission(String missionId) {
        List<Mission> current = activeMissions.getValue();
        if (current == null) return;

        List<Mission> updated = new ArrayList<>();
        for (Mission m : current) {
            if (m.id.equals(missionId)) {
                m.isCompleted = true;
            }
            if (!m.isCompleted) {
                updated.add(m);
            }
        }
        activeMissions.postValue(updated);
    }

    // ==================== DỮ LIỆU CÔNG TRÌNH ====================

    public void loadBuildingById(String buildingId) {
        loadBuildingFromLocal(buildingId);
    }

    public void onBuildingClicked(String buildingId) {
        loadBuildingFromLocal(buildingId);
    }

    private void loadBuildingFromLocal(String buildingId) {
        if (buildingDao == null) {
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
        Building building = repository.getBuildingById(buildingId);
        if (building != null) {
            selectedBuilding.setValue(building);
        }
    }

    public void closeMenu() {
        selectedBuilding.setValue(null);
    }

    public void loadAllBuildingsLockStatus() {
        if (buildingDao == null) return;

        final List<String> allIds = Arrays.asList("house", "school", "library", "park", "farmer", "coffee", "clothers", "bakery");

        new Thread(() -> {
            List<UserBuilding> owned = buildingDao.getBuildingsForUser(currentUserId);
            Map<String, Boolean> map = new HashMap<>();
            for (String id : allIds) {
                boolean locked = !owned.stream().anyMatch(ub -> ub.buildingId.equals(id));
                map.put(id, locked);
            }
            postToMain(() -> buildingsLockStatus.setValue(map));
        }).start();
    }

    public void unlockBuilding(String buildingId) {
        if (buildingDao == null) return;

        new Thread(() -> {
            UserBuilding existing = buildingDao.getBuilding(currentUserId, buildingId);
            if (existing == null) {
                UserBuilding newBuilding = new UserBuilding(currentUserId, buildingId, 1);
                buildingDao.insertOrUpdate(newBuilding);
            }

            postToMain(() -> {
                loadAllBuildingsLockStatus();
                loadBuildingFromLocal(buildingId);
            });
        }).start();
    }

    // Giữ nguyên để test
    public void unlockBuildingWithGold(String buildingId, UnlockCallback callback) {
        if (buildingDao == null || goldRepo == null || appContext == null) {
            if (callback != null) callback.onError("Hệ thống chưa sẵn sàng");
            return;
        }

        final int UNLOCK_COST = 50;

        goldRepo.hasEnoughGold(appContext, UNLOCK_COST, (enough, currentGold, message) -> {
            if (!enough) {
                if (callback != null) callback.onError(message);
                return;
            }

            goldRepo.addGold(appContext, -UNLOCK_COST, new GoldRepository.OnGoldUpdatedListener() {
                @Override
                public void onGoldUpdated(int newGold) {
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

    public void upgradeBuildingAfterLesson(String buildingId) {
        new Thread(() -> {
            UserBuilding ub = buildingDao.getBuilding(currentUserId, buildingId);
            if (ub == null) {
                ub = new UserBuilding(currentUserId, buildingId, 1);
            } else if (ub.level < 4) {
                ub.level++;
            }
            buildingDao.insertOrUpdate(ub);

            postToMain(() -> {
                loadAllBuildingsLockStatus();
                loadBuildingFromLocal(buildingId);
            });
        }).start();
    }

    public interface UnlockCallback {
        void onSuccess(int newGold);
        void onError(String error);
    }

    private void postToMain(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (missionHandler != null && missionCheckRunnable != null) {
            missionHandler.removeCallbacks(missionCheckRunnable);
        }
    }
}