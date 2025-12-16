package com.example.rise_of_city.data.repository;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository để quản lý nâng cấp building
 * Nâng cấp bằng vàng, tăng production rate và phần thưởng thu hoạch
 */
public class BuildingUpgradeRepository {
    private static final String TAG = "BuildingUpgradeRepo";
    private static final int BASE_UPGRADE_COST = 50; // Chi phí nâng cấp cơ bản
    
    private static BuildingUpgradeRepository instance;
    
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    
    private BuildingUpgradeRepository() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    
    public static BuildingUpgradeRepository getInstance() {
        if (instance == null) {
            instance = new BuildingUpgradeRepository();
        }
        return instance;
    }
    
    /**
     * Tính chi phí nâng cấp dựa trên level hiện tại
     */
    public int calculateUpgradeCost(int currentLevel) {
        // Chi phí tăng theo level: 50 * (level + 1)
        return BASE_UPGRADE_COST * (currentLevel + 1);
    }
    
    /**
     * Tính lợi ích sau khi nâng cấp
     */
    public UpgradeBenefits calculateUpgradeBenefits(int newLevel) {
        // Production rate tăng theo level
        int expPerHour = 10 * newLevel;
        int goldPerHour = 5 * newLevel;
        
        // Harvest reward tăng theo level
        int harvestExpReward = 20 * newLevel;
        int harvestGoldReward = 10 * newLevel;
        
        return new UpgradeBenefits(expPerHour, goldPerHour, harvestExpReward, harvestGoldReward);
    }
    
    /**
     * Nâng cấp building
     */
    public void upgradeBuilding(String buildingId, int currentLevel, OnUpgradeListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onError("Người dùng chưa đăng nhập");
            }
            return;
        }
        
        int upgradeCost = calculateUpgradeCost(currentLevel);
        int newLevel = currentLevel + 1;
        
        // Kiểm tra đủ vàng không
        GoldRepository goldRepo = GoldRepository.getInstance();
        goldRepo.checkCanUnlockBuilding(upgradeCost, (canUpgrade, currentGold, message) -> {
            if (!canUpgrade) {
                if (listener != null) {
                    listener.onError(message);
                }
                return;
            }
            
            // Trừ vàng
            goldRepo.spendGold(upgradeCost, new GoldRepository.OnGoldUpdatedListener() {
                @Override
                public void onGoldUpdated(int newGold) {
                    // Cập nhật level building
                    updateBuildingLevel(buildingId, newLevel, listener);
                }
                
                @Override
                public void onError(String error) {
                    if (listener != null) {
                        listener.onError(error);
                    }
                }
            });
        });
    }
    
    /**
     * Cập nhật level building trong Firebase
     */
    private void updateBuildingLevel(String buildingId, int newLevel, OnUpgradeListener listener) {
        String userId = auth.getCurrentUser().getUid();
        String buildingPath = "users/" + userId + "/buildings/" + buildingId;
        
        // Tính maxExp mới (tăng theo level)
        int newMaxExp = (int) (100 * Math.pow(1.5, newLevel - 1));
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("level", newLevel);
        updates.put("currentExp", 0); // Reset EXP khi nâng cấp
        updates.put("maxExp", newMaxExp);
        
        firestore.document(buildingPath)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    UpgradeBenefits benefits = calculateUpgradeBenefits(newLevel);
                    if (listener != null) {
                        listener.onUpgradeSuccess(newLevel, benefits);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error upgrading building: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }
    
    // Data class cho lợi ích nâng cấp
    public static class UpgradeBenefits {
        public final int expPerHour;
        public final int goldPerHour;
        public final int harvestExpReward;
        public final int harvestGoldReward;
        
        public UpgradeBenefits(int expPerHour, int goldPerHour, int harvestExpReward, int harvestGoldReward) {
            this.expPerHour = expPerHour;
            this.goldPerHour = goldPerHour;
            this.harvestExpReward = harvestExpReward;
            this.harvestGoldReward = harvestGoldReward;
        }
    }
    
    // Interface
    public interface OnUpgradeListener {
        void onUpgradeSuccess(int newLevel, UpgradeBenefits benefits);
        void onError(String error);
    }
}

