package com.example.rise_of_city.data.repository;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository để quản lý thu hoạch building
 * Thu hoạch có cooldown và phần thưởng rõ ràng
 */
public class BuildingHarvestRepository {
    private static final String TAG = "BuildingHarvestRepo";
    public static final long HARVEST_COOLDOWN_MS = 60 * 60 * 1000; // 1 giờ
    private static final int BASE_EXP_REWARD = 20; // EXP cơ bản
    private static final int BASE_GOLD_REWARD = 10; // Vàng cơ bản
    
    private static BuildingHarvestRepository instance;
    
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    
    private BuildingHarvestRepository() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    
    public static BuildingHarvestRepository getInstance() {
        if (instance == null) {
            instance = new BuildingHarvestRepository();
        }
        return instance;
    }
    
    /**
     * Kiểm tra xem building có thể thu hoạch không (đã hết cooldown chưa)
     */
    public void canHarvest(String buildingId, OnCanHarvestListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onCanHarvest(false, 0, "Người dùng chưa đăng nhập");
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String buildingPath = "users/" + userId + "/buildings/" + buildingId;
        
        firestore.document(buildingPath)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // Building chưa tồn tại, có thể thu hoạch ngay
                        if (listener != null) {
                            listener.onCanHarvest(true, 0, "Có thể thu hoạch");
                        }
                        return;
                    }
                    
                    Long lastHarvestTime = documentSnapshot.getLong("lastHarvestTime");
                    if (lastHarvestTime == null) {
                        // Chưa từng thu hoạch, có thể thu hoạch ngay
                        if (listener != null) {
                            listener.onCanHarvest(true, 0, "Có thể thu hoạch");
                        }
                        return;
                    }
                    
                    long currentTime = System.currentTimeMillis();
                    long timeSinceLastHarvest = currentTime - lastHarvestTime;
                    long remainingCooldown = HARVEST_COOLDOWN_MS - timeSinceLastHarvest;
                    
                    if (remainingCooldown <= 0) {
                        // Đã hết cooldown
                        if (listener != null) {
                            listener.onCanHarvest(true, 0, "Có thể thu hoạch");
                        }
                    } else {
                        // Còn cooldown
                        long remainingMinutes = remainingCooldown / (60 * 1000);
                        if (listener != null) {
                            listener.onCanHarvest(false, remainingMinutes, 
                                "Còn " + remainingMinutes + " phút nữa mới có thể thu hoạch");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking harvest status: " + e.getMessage());
                    if (listener != null) {
                        listener.onCanHarvest(false, 0, "Lỗi: " + e.getMessage());
                    }
                });
    }
    
    /**
     * Tính phần thưởng thu hoạch dựa trên level building
     */
    public HarvestReward calculateHarvestReward(int buildingLevel) {
        // EXP và Vàng tăng theo level
        int expReward = BASE_EXP_REWARD * buildingLevel;
        int goldReward = BASE_GOLD_REWARD * buildingLevel;
        
        return new HarvestReward(expReward, goldReward);
    }
    
    /**
     * Đánh dấu đã thu hoạch (lưu thời gian thu hoạch)
     */
    public void markAsHarvested(String buildingId, OnHarvestMarkedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onError("Người dùng chưa đăng nhập");
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String buildingPath = "users/" + userId + "/buildings/" + buildingId;
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastHarvestTime", System.currentTimeMillis());
        
        firestore.document(buildingPath)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Harvest time marked for building: " + buildingId);
                    if (listener != null) {
                        listener.onHarvestMarked();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error marking harvest: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }
    
    /**
     * Lấy thời gian cooldown còn lại (tính bằng phút)
     */
    public void getRemainingCooldown(String buildingId, OnCooldownCalculatedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onCooldownCalculated(0);
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String buildingPath = "users/" + userId + "/buildings/" + buildingId;
        
        firestore.document(buildingPath)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        if (listener != null) {
                            listener.onCooldownCalculated(0);
                        }
                        return;
                    }
                    
                    Long lastHarvestTime = documentSnapshot.getLong("lastHarvestTime");
                    if (lastHarvestTime == null) {
                        if (listener != null) {
                            listener.onCooldownCalculated(0);
                        }
                        return;
                    }
                    
                    long currentTime = System.currentTimeMillis();
                    long timeSinceLastHarvest = currentTime - lastHarvestTime;
                    long remainingCooldown = HARVEST_COOLDOWN_MS - timeSinceLastHarvest;
                    
                    if (remainingCooldown <= 0) {
                        if (listener != null) {
                            listener.onCooldownCalculated(0);
                        }
                    } else {
                        long remainingMinutes = remainingCooldown / (60 * 1000);
                        if (listener != null) {
                            listener.onCooldownCalculated(remainingMinutes);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting cooldown: " + e.getMessage());
                    if (listener != null) {
                        listener.onCooldownCalculated(0);
                    }
                });
    }
    
    // Data class cho phần thưởng
    public static class HarvestReward {
        public final int expReward;
        public final int goldReward;
        
        public HarvestReward(int expReward, int goldReward) {
            this.expReward = expReward;
            this.goldReward = goldReward;
        }
    }
    
    // Interfaces
    public interface OnCanHarvestListener {
        void onCanHarvest(boolean canHarvest, long remainingMinutes, String message);
    }
    
    public interface OnHarvestMarkedListener {
        void onHarvestMarked();
        void onError(String error);
    }
    
    public interface OnCooldownCalculatedListener {
        void onCooldownCalculated(long remainingMinutes);
    }
}

