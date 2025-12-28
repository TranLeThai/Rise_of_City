package com.example.rise_of_city.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.User;
import com.example.rise_of_city.data.local.UserDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository để quản lý vàng (gold/coin) của người dùng
 * Vàng được dùng để mở khóa building mới
 */
public class GoldRepository {
    private static final String TAG = "GoldRepository";
    private static final String PREF_NAME = "RiseOfCity_Prefs";
    private static final String KEY_USER_ID = "logged_user_id";
    private static GoldRepository instance;
    
    private ExecutorService executorService;
    
    private GoldRepository() {
        executorService = Executors.newSingleThreadExecutor();
    }
    
    public static GoldRepository getInstance() {
        if (instance == null) {
            instance = new GoldRepository();
        }
        return instance;
    }
    
    /**
     * Lấy userId từ SharedPreferences
     */
    private int getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_USER_ID, -1);
    }
    
    /**
     * Lấy số vàng hiện tại của user
     */
    public void getCurrentGold(Context context, OnGoldLoadedListener listener) {
        int userId = getUserId(context);
        if (userId == -1) {
            if (listener != null) {
                listener.onGoldLoaded(0);
            }
            return;
        }
        
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                UserDao userDao = db.userDao();
                User user = userDao.getUserById(userId);
                
                if (user != null) {
                    int gold = user.gold;
                    if (listener != null) {
                        // Run on main thread for UI updates
                        new android.os.Handler(context.getMainLooper()).post(() -> {
                            listener.onGoldLoaded(gold);
                        });
                    }
                } else {
                    Log.e(TAG, "User not found with id: " + userId);
                    if (listener != null) {
                        new android.os.Handler(context.getMainLooper()).post(() -> {
                            listener.onGoldLoaded(0);
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading gold: " + e.getMessage(), e);
                if (listener != null) {
                    new android.os.Handler(context.getMainLooper()).post(() -> {
                        listener.onGoldLoaded(0);
                    });
                }
            }
        });
    }
    
    /**
     * Thêm vàng cho user (khi quiz đúng, hoàn thành mission, etc.)
     */
    public void addGold(Context context, int amount, OnGoldUpdatedListener listener) {
        int userId = getUserId(context);
        if (userId == -1) {
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
        
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                UserDao userDao = db.userDao();
                User user = userDao.getUserById(userId);
                
                if (user != null) {
                    int newGold = user.gold + amount;
                    user.gold = newGold;
                    userDao.updateUser(user);
                    
                    Log.d(TAG, "Added " + amount + " gold. New total: " + newGold);
                    
                    if (listener != null) {
                        new android.os.Handler(context.getMainLooper()).post(() -> {
                            listener.onGoldUpdated(newGold);
                        });
                    }
                } else {
                    String error = "Không tìm thấy user với id: " + userId;
                    Log.e(TAG, error);
                    if (listener != null) {
                        new android.os.Handler(context.getMainLooper()).post(() -> {
                            listener.onError(error);
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error adding gold: " + e.getMessage(), e);
                if (listener != null) {
                    new android.os.Handler(context.getMainLooper()).post(() -> {
                        listener.onError(e.getMessage());
                    });
                }
            }
        });
    }
    
    /**
     * Trừ vàng của user (khi mở khóa building)
     */
    public void spendGold(Context context, int amount, OnGoldUpdatedListener listener) {
        int userId = getUserId(context);
        if (userId == -1) {
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
        
        executorService.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                UserDao userDao = db.userDao();
                User user = userDao.getUserById(userId);
                
                if (user != null) {
                    if (user.gold < amount) {
                        String error = "Không đủ vàng! Bạn cần " + amount + " vàng nhưng chỉ có " + user.gold;
                        if (listener != null) {
                            new android.os.Handler(context.getMainLooper()).post(() -> {
                                listener.onError(error);
                            });
                        }
                        return;
                    }
                    
                    int newGold = user.gold - amount;
                    user.gold = newGold;
                    userDao.updateUser(user);
                    
                    Log.d(TAG, "Spent " + amount + " gold. New total: " + newGold);
                    
                    if (listener != null) {
                        new android.os.Handler(context.getMainLooper()).post(() -> {
                            listener.onGoldUpdated(newGold);
                        });
                    }
                } else {
                    String error = "Không tìm thấy user với id: " + userId;
                    Log.e(TAG, error);
                    if (listener != null) {
                        new android.os.Handler(context.getMainLooper()).post(() -> {
                            listener.onError(error);
                        });
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error spending gold: " + e.getMessage(), e);
                if (listener != null) {
                    new android.os.Handler(context.getMainLooper()).post(() -> {
                        listener.onError(e.getMessage());
                    });
                }
            }
        });
    }
    
    /**
     * Kiểm tra xem user có đủ vàng để mở khóa building không
     */
    public void checkCanUnlockBuilding(Context context, int requiredGold, OnCanUnlockListener listener) {
        int userId = getUserId(context);
        if (userId == -1) {
            if (listener != null) {
                listener.onCanUnlock(false, 0, "Người dùng chưa đăng nhập");
            }
            return;
        }
        
        getCurrentGold(context, currentGold -> {
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

