package com.example.rise_of_city.data.repository;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository để quản lý vàng (gold/coin) của người dùng
 * Vàng được dùng để mở khóa building mới
 */
public class GoldRepository {
    private static final String TAG = "GoldRepository";
    private static GoldRepository instance;
    
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    
    private GoldRepository() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    
    public static GoldRepository getInstance() {
        if (instance == null) {
            instance = new GoldRepository();
        }
        return instance;
    }
    
    /**
     * Lấy số vàng hiện tại của user
     */
    public void getCurrentGold(OnGoldLoadedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onGoldLoaded(0);
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        
        firestore.collection("user_profiles")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long gold = documentSnapshot.getLong("gold");
                        if (listener != null) {
                            listener.onGoldLoaded(gold != null ? gold.intValue() : 0);
                        }
                    } else {
                        // Nếu chưa có profile, tạo mới với 0 vàng
                        initializeGold(userId, listener);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading gold: " + e.getMessage());
                    if (listener != null) {
                        listener.onGoldLoaded(0);
                    }
                });
    }
    
    /**
     * Khởi tạo vàng cho user mới (mặc định 100 vàng để bắt đầu)
     */
    private void initializeGold(String userId, OnGoldLoadedListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("gold", 100); // Bắt đầu với 100 vàng
        
        firestore.collection("user_profiles")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onGoldLoaded(100);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error initializing gold: " + e.getMessage());
                    if (listener != null) {
                        listener.onGoldLoaded(0);
                    }
                });
    }
    
    /**
     * Thêm vàng cho user (khi quiz đúng, hoàn thành mission, etc.)
     */
    public void addGold(int amount, OnGoldUpdatedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onError("Người dùng chưa đăng nhập");
            }
            return;
        }
        
        if (amount <= 0) {
            if (listener != null) {
                listener.onError("Số vàng phải lớn hơn 0");
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        
        // Lấy vàng hiện tại
        getCurrentGold(currentGold -> {
            int newGold = currentGold + amount;
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("gold", newGold);
            
            firestore.collection("user_profiles")
                    .document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) {
                            listener.onGoldUpdated(newGold);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding gold: " + e.getMessage());
                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    });
        });
    }
    
    /**
     * Trừ vàng của user (khi mở khóa building)
     */
    public void spendGold(int amount, OnGoldUpdatedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onError("Người dùng chưa đăng nhập");
            }
            return;
        }
        
        if (amount <= 0) {
            if (listener != null) {
                listener.onError("Số vàng phải lớn hơn 0");
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        
        // Lấy vàng hiện tại
        getCurrentGold(currentGold -> {
            if (currentGold < amount) {
                if (listener != null) {
                    listener.onError("Không đủ vàng! Bạn cần " + amount + " vàng nhưng chỉ có " + currentGold);
                }
                return;
            }
            
            int newGold = currentGold - amount;
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("gold", newGold);
            
            firestore.collection("user_profiles")
                    .document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) {
                            listener.onGoldUpdated(newGold);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error spending gold: " + e.getMessage());
                        if (listener != null) {
                            listener.onError(e.getMessage());
                        }
                    });
        });
    }
    
    /**
     * Kiểm tra xem user có đủ vàng để mở khóa building không
     */
    public void checkCanUnlockBuilding(int requiredGold, OnCanUnlockListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onCanUnlock(false, 0, "Người dùng chưa đăng nhập");
            }
            return;
        }
        
        getCurrentGold(currentGold -> {
            boolean canUnlock = currentGold >= requiredGold;
            if (listener != null) {
                listener.onCanUnlock(canUnlock, currentGold, 
                    canUnlock ? "Có thể mở khóa" : "Không đủ vàng! Cần " + requiredGold + " vàng");
            }
        });
    }
    
    // Interfaces
    public interface OnGoldLoadedListener {
        void onGoldLoaded(int gold);
    }
    
    public interface OnGoldUpdatedListener {
        void onGoldUpdated(int newGold);
        void onError(String error);
    }
    
    public interface OnCanUnlockListener {
        void onCanUnlock(boolean canUnlock, int currentGold, String message);
    }
}

