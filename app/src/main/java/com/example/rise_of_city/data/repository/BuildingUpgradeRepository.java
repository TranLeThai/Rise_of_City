package com.example.rise_of_city.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.UserBuilding;
import com.example.rise_of_city.data.local.UserBuildingDao;

/**
 * Repository quản lý nâng cấp building (offline - dùng Room Database)
 * - Nâng cấp bằng vàng
 * - Tăng level (tối đa 4)
 * - Tính toán chi phí và lợi ích theo level
 */
public class BuildingUpgradeRepository {

    private static final String TAG = "BuildingUpgradeRepo";
    private static final int BASE_UPGRADE_COST = 100; // Chi phí nâng cấp cơ bản (có thể điều chỉnh)

    private static BuildingUpgradeRepository instance;
    private final GoldRepository goldRepository;

    private BuildingUpgradeRepository() {
        goldRepository = GoldRepository.getInstance();
    }

    public static synchronized BuildingUpgradeRepository getInstance() {
        if (instance == null) {
            instance = new BuildingUpgradeRepository();
        }
        return instance;
    }

    /**
     * Tính chi phí nâng cấp dựa trên level hiện tại
     * Ví dụ: level 1 → 100, level 2 → 200, level 3 → 300...
     */
    public int calculateUpgradeCost(int currentLevel) {
        return BASE_UPGRADE_COST * (currentLevel + 1);
    }

    /**
     * Tính lợi ích sau khi nâng cấp
     */
    public UpgradeBenefits calculateUpgradeBenefits(int newLevel) {
        int expPerHour = 10 * newLevel;
        int goldPerHour = 5 * newLevel;
        int harvestExpReward = 50 * newLevel;
        int harvestGoldReward = 20 * newLevel;

        return new UpgradeBenefits(expPerHour, goldPerHour, harvestExpReward, harvestGoldReward);
    }

    /**
     * Nâng cấp building (offline - dùng Room)
     */
    public void upgradeBuilding(@NonNull Context context,
                                String buildingId,
                                int currentLevel,
                                OnUpgradeListener listener) {

        if (currentLevel >= 4) {
            if (listener != null) {
                listener.onError("Công trình đã đạt cấp tối đa (level 4)");
            }
            return;
        }

        int upgradeCost = calculateUpgradeCost(currentLevel);
        int newLevel = currentLevel + 1;

        // Kiểm tra đủ vàng không
        goldRepository.hasEnoughGold(context, upgradeCost, (enough, currentGold, message) -> {
            if (!enough) {
                if (listener != null) {
                    listener.onError(message);
                }
                return;
            }

            // Trừ vàng
            goldRepository.addGold(context, -upgradeCost, new GoldRepository.OnGoldUpdatedListener() {
                @Override
                public void onGoldUpdated(int newGold) {
                    // Cập nhật level trong Room Database
                    updateBuildingLevelInRoom(context, buildingId, newLevel, listener);
                }

                @Override
                public void onError(String error) {
                    if (listener != null) {
                        listener.onError("Trừ vàng thất bại: " + error);
                    }
                }
            });
        });
    }

    /**
     * Cập nhật level building trong Room Database
     */
    private void updateBuildingLevelInRoom(Context context,
                                           String buildingId,
                                           int newLevel,
                                           OnUpgradeListener listener) {

        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                UserBuildingDao dao = db.userBuildingDao();

                // Giả sử userId hiện tại là 1 (sau này lấy từ login)
                int userId = 1;

                UserBuilding userBuilding = dao.getBuilding(userId, buildingId);
                if (userBuilding == null) {
                    if (listener != null) {
                        postToMain(context, () -> listener.onError("Không tìm thấy công trình"));
                    }
                    return;
                }

                userBuilding.level = newLevel;
                dao.insertOrUpdate(userBuilding); // cần method này trong DAO

                UpgradeBenefits benefits = calculateUpgradeBenefits(newLevel);

                Log.d(TAG, "Nâng cấp thành công " + buildingId + " lên level " + newLevel);

                if (listener != null) {
                    postToMain(context, () -> listener.onUpgradeSuccess(newLevel, benefits));
                }

            } catch (Exception e) {
                Log.e(TAG, "Lỗi nâng cấp building trong Room: " + e.getMessage(), e);
                if (listener != null) {
                    postToMain(context, () -> listener.onError("Lỗi hệ thống: " + e.getMessage()));
                }
            }
        }).start();
    }

    private void postToMain(Context context, Runnable runnable) {
        new android.os.Handler(context.getMainLooper()).post(runnable);
    }

    // ============================================
    // Data class và Interface
    // ============================================

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

    public interface OnUpgradeListener {
        void onUpgradeSuccess(int newLevel, UpgradeBenefits benefits);
        void onError(String error);
    }
}