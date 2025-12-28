package com.example.rise_of_city.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.User;
import com.example.rise_of_city.data.local.UserDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoldRepository {

    private static final String TAG = "GoldRepository";
    private static final String PREF_NAME = "RiseOfCity_Prefs";
    private static final String KEY_USER_ID = "logged_user_id";

    private static GoldRepository instance;
    private final ExecutorService executor;

    private GoldRepository() {
        executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized GoldRepository getInstance() {
        if (instance == null) {
            instance = new GoldRepository();
        }
        return instance;
    }

    private int getCurrentUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public void getCurrentGold(@NonNull Context context, @NonNull OnGoldLoadedListener listener) {
        int userId = getCurrentUserId(context);
        if (userId == -1) {
            listener.onGoldLoaded(0);
            return;
        }

        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                UserDao userDao = db.userDao();
                User user = userDao.getUserById(userId);
                int gold = (user != null) ? user.gold : 0;
                new android.os.Handler(context.getMainLooper()).post(() -> listener.onGoldLoaded(gold));
            } catch (Exception e) {
                Log.e(TAG, "Error loading gold", e);
                new android.os.Handler(context.getMainLooper()).post(() -> listener.onGoldLoaded(0));
            }
        });
    }

    public void addGold(@NonNull Context context, int amount, OnGoldUpdatedListener listener) {
        int userId = getCurrentUserId(context);
        if (userId == -1) {
            if (listener != null) listener.onError("Người dùng chưa đăng nhập");
            return;
        }

        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                UserDao userDao = db.userDao();
                User user = userDao.getUserById(userId);

                if (user == null) {
                    if (listener != null) {
                        postToMain(context, () -> listener.onError("Không tìm thấy người dùng"));
                    }
                    return;
                }

                if (amount < 0 && user.gold < Math.abs(amount)) {
                    String error = "Không đủ vàng! Cần " + Math.abs(amount) + " nhưng chỉ có " + user.gold;
                    if (listener != null) {
                        postToMain(context, () -> listener.onError(error));
                    }
                    return;
                }

                int newGold = user.gold + amount;
                if (newGold < 0) newGold = 0;

                user.gold = newGold;
                userDao.updateUser(user);

                Log.d(TAG, (amount > 0 ? "Added" : "Spent") + " " + Math.abs(amount) + " gold → New total: " + newGold);

                if (listener != null) {
                    final int finalNewGold = newGold; // Sửa lỗi lambda
                    postToMain(context, () -> listener.onGoldUpdated(finalNewGold));
                }

            } catch (Exception e) {
                Log.e(TAG, "Error updating gold", e);
                if (listener != null) {
                    postToMain(context, () -> listener.onError(e.getMessage()));
                }
            }
        });
    }

    public void hasEnoughGold(@NonNull Context context, int required, OnCheckGoldListener listener) {
        getCurrentGold(context, current -> {
            boolean enough = current >= required;
            String message = enough ? "Đủ vàng" : "Không đủ vàng (cần " + required + ", có " + current + ")";
            if (listener != null) listener.onResult(enough, current, message);
        });
    }

    private void postToMain(Context context, Runnable runnable) {
        new android.os.Handler(context.getMainLooper()).post(runnable);
    }

    public interface OnGoldLoadedListener {
        void onGoldLoaded(int gold);
    }

    public interface OnGoldUpdatedListener {
        void onGoldUpdated(int newGold);
        void onError(String error);
    }

    public interface OnCheckGoldListener {
        void onResult(boolean enough, int currentGold, String message);
    }
}