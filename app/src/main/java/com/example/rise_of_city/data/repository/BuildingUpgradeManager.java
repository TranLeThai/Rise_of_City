package com.example.rise_of_city.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.UserBuilding;
import com.example.rise_of_city.data.local.UserBuildingDao;
import com.example.rise_of_city.data.model.game.Building;
import com.example.rise_of_city.data.model.game.BuildingUpgradeQuest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Quản lý upgrade building và quest upgrade
 * Sử dụng Room database để lưu trữ building level
 */
public class BuildingUpgradeManager {
    private static final String TAG = "BuildingUpgradeManager";
    private static final String PREF_NAME = "RiseOfCity_Prefs";
    private static final String KEY_LESSON_COMPLETED = "lesson_completed_"; // Prefix cho lesson completed
    private static final String KEY_UPGRADE_QUESTS = "upgrade_quests";
    
    private static BuildingUpgradeManager instance;
    private AppDatabase database;
    private UserBuildingDao buildingDao;
    private ExecutorService executorService;
    private Gson gson;
    private int userId;
    private Context appContext;
    
    private BuildingUpgradeManager(Context context) {
        this.appContext = context.getApplicationContext();
        database = AppDatabase.getInstance(appContext);
        buildingDao = database.userBuildingDao();
        executorService = Executors.newSingleThreadExecutor();
        gson = new Gson();
        
        SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        userId = prefs.getInt("logged_user_id", -1);
    }
    
    public static synchronized BuildingUpgradeManager getInstance(Context context) {
        if (instance == null) {
            instance = new BuildingUpgradeManager(context);
        }
        return instance;
    }
    
    /**
     * Đánh dấu lesson đã hoàn thành
     */
    public void markLessonCompleted(String lessonName) {
        SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_LESSON_COMPLETED + lessonName, true).apply();
        Log.d(TAG, "Marked lesson completed: " + lessonName);
    }
    
    /**
     * Kiểm tra lesson đã hoàn thành chưa
     */
    public boolean isLessonCompleted(String lessonName) {
        SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_LESSON_COMPLETED + lessonName, false);
    }
    
    /**
     * Xóa trạng thái lesson completed (dùng để reset progress)
     */
    public void clearLessonCompleted(String lessonName) {
        SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_LESSON_COMPLETED + lessonName).apply();
        Log.d(TAG, "Cleared lesson completed status: " + lessonName);
    }
    
    /**
     * Tạo upgrade quest cho building
     */
    public BuildingUpgradeQuest createUpgradeQuest(Building building) {
        String lessonName = building.getRequiredLessonName();
        int targetLevel = building.getLevel() + 1;
        boolean lessonCompleted = isLessonCompleted(lessonName);
        
        BuildingUpgradeQuest quest = new BuildingUpgradeQuest(
            building.getId(),
            building.getName(),
            lessonName,
            targetLevel
        );
        quest.lessonCompleted = lessonCompleted;
        
        return quest;
    }
    
    /**
     * Kiểm tra điều kiện để upgrade building
     */
    public boolean canUpgradeBuilding(Building building) {
        if (building.getLevel() >= 5) {
            return false; // Max level
        }
        
        String lessonName = building.getRequiredLessonName();
        return isLessonCompleted(lessonName);
    }
    
    /**
     * Upgrade building lên level tiếp theo
     */
    public void upgradeBuilding(String buildingId, OnUpgradeListener listener) {
        if (userId == -1) {
            if (listener != null) {
                listener.onError("Người dùng chưa đăng nhập");
            }
            return;
        }
        
        executorService.execute(() -> {
            try {
                UserBuilding userBuilding = buildingDao.getBuilding(userId, buildingId);
                if (userBuilding == null) {
                    // Building chưa unlock, tạo mới với level 1
                    userBuilding = new UserBuilding(userId, buildingId, 1);
                    buildingDao.insertOrUpdate(userBuilding);
                    if (listener != null) {
                        listener.onUpgradeSuccess(1);
                    }
                    return;
                }
                
                // Kiểm tra max level
                if (userBuilding.level >= 5) {
                    if (listener != null) {
                        listener.onError("Building đã đạt level tối đa");
                    }
                    return;
                }
                
                // Upgrade level
                int newLevel = userBuilding.level + 1;
                userBuilding.level = newLevel;
                buildingDao.updateBuilding(userBuilding);
                
                Log.d(TAG, "Upgraded building " + buildingId + " to level " + newLevel);
                
                if (listener != null) {
                    listener.onUpgradeSuccess(newLevel);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error upgrading building: " + e.getMessage());
                if (listener != null) {
                    listener.onError(e.getMessage());
                }
            }
        });
    }
    
    public interface OnUpgradeListener {
        void onUpgradeSuccess(int newLevel);
        void onError(String error);
    }
    
    /**
     * Update userId nếu cần
     */
    public void updateUserId(int userId) {
        this.userId = userId;
    }
}

