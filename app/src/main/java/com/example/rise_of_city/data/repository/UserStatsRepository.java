package com.example.rise_of_city.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository để tính toán và quản lý thống kê người dùng
 */
public class UserStatsRepository {
    private static final String TAG = "UserStatsRepository";
    private static UserStatsRepository instance;
    
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    
    private UserStatsRepository() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    
    public static UserStatsRepository getInstance() {
        if (instance == null) {
            instance = new UserStatsRepository();
        }
        return instance;
    }
    
    /**
     * Tính số ngày đăng nhập liên tiếp (streak) từ learning_logs
     */
    public void calculateStreak(OnStreakCalculatedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onStreakCalculated(0);
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        
        // Lấy learning_logs của user
        firestore.collection("user_profiles")
                .document(userId)
                .collection("learning_logs")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(30) // Lấy 30 ngày gần nhất
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int streak = calculateStreakFromLogs(queryDocumentSnapshots);
                    
                    // Cập nhật streak vào user_profiles
                    updateStreakInProfile(userId, streak);
                    
                    if (listener != null) {
                        listener.onStreakCalculated(streak);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error calculating streak: " + e.getMessage());
                    // Fallback: lấy từ user_profiles nếu có
                    getStreakFromProfile(userId, listener);
                });
    }
    
    /**
     * Tính streak từ learning logs
     */
    private int calculateStreakFromLogs(com.google.firebase.firestore.QuerySnapshot snapshot) {
        if (snapshot == null || snapshot.isEmpty()) {
            return 0;
        }
        
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        int streak = 0;
        int expectedDay = 0; // 0 = today, 1 = yesterday, etc.
        
        for (QueryDocumentSnapshot doc : snapshot) {
            Long timestamp = doc.getLong("timestamp");
            if (timestamp == null) continue;
            
            Calendar logDate = Calendar.getInstance();
            logDate.setTimeInMillis(timestamp);
            logDate.set(Calendar.HOUR_OF_DAY, 0);
            logDate.set(Calendar.MINUTE, 0);
            logDate.set(Calendar.SECOND, 0);
            logDate.set(Calendar.MILLISECOND, 0);
            
            long daysDiff = (today.getTimeInMillis() - logDate.getTimeInMillis()) / (1000 * 60 * 60 * 24);
            
            if (daysDiff == expectedDay) {
                streak++;
                expectedDay++;
            } else if (daysDiff > expectedDay) {
                // Có khoảng trống, streak bị gián đoạn
                break;
            }
        }
        
        return streak;
    }
    
    /**
     * Lấy streak từ user_profiles (fallback)
     */
    private void getStreakFromProfile(String userId, OnStreakCalculatedListener listener) {
        firestore.collection("user_profiles")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long streak = documentSnapshot.getLong("streak");
                        if (listener != null) {
                            listener.onStreakCalculated(streak != null ? streak.intValue() : 0);
                        }
                    } else {
                        if (listener != null) {
                            listener.onStreakCalculated(0);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting streak from profile: " + e.getMessage());
                    if (listener != null) {
                        listener.onStreakCalculated(0);
                    }
                });
    }
    
    /**
     * Cập nhật streak vào user_profiles
     */
    private void updateStreakInProfile(String userId, int streak) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("streak", streak);
        updates.put("lastStreakUpdate", System.currentTimeMillis());
        
        firestore.collection("user_profiles")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Streak updated: " + streak);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating streak: " + e.getMessage());
                });
    }
    
    /**
     * Tính tổng EXP từ tất cả buildings
     */
    public void calculateTotalXP(OnTotalXPCalculatedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onTotalXPCalculated(0);
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        // Sửa path: buildings được lưu ở users/{userId}/buildings (không phải user_profiles)
        String buildingsPath = "users/" + userId + "/buildings";
        
        firestore.collection(buildingsPath)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalXP = 0;
                    
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Long currentExp = doc.getLong("currentExp");
                        Long level = doc.getLong("level");
                        
                        if (currentExp != null) {
                            totalXP += currentExp.intValue();
                        }
                        
                        // Thêm EXP từ level (mỗi level = 100 EXP base)
                        if (level != null && level > 0) {
                            totalXP += (level.intValue() - 1) * 100; // Level 1 = 0, Level 2 = 100, etc.
                        }
                    }
                    
                    // Cập nhật totalXP vào user_profiles
                    updateTotalXPInProfile(userId, totalXP);
                    
                    if (listener != null) {
                        listener.onTotalXPCalculated(totalXP);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error calculating total XP: " + e.getMessage());
                    // Fallback: lấy từ user_profiles
                    getTotalXPFromProfile(userId, listener);
                });
    }
    
    /**
     * Lấy totalXP từ user_profiles (fallback)
     */
    private void getTotalXPFromProfile(String userId, OnTotalXPCalculatedListener listener) {
        firestore.collection("user_profiles")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long totalXP = documentSnapshot.getLong("totalXP");
                        if (listener != null) {
                            listener.onTotalXPCalculated(totalXP != null ? totalXP.intValue() : 0);
                        }
                    } else {
                        if (listener != null) {
                            listener.onTotalXPCalculated(0);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting total XP: " + e.getMessage());
                    if (listener != null) {
                        listener.onTotalXPCalculated(0);
                    }
                });
    }
    
    /**
     * Cập nhật totalXP vào user_profiles
     */
    private void updateTotalXPInProfile(String userId, int totalXP) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("totalXP", totalXP);
        
        firestore.collection("user_profiles")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Total XP updated: " + totalXP);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating total XP: " + e.getMessage());
                });
    }
    
    /**
     * Tính số quiz đã làm và số quiz đúng từ learning_logs
     */
    public void calculateQuizStats(OnQuizStatsCalculatedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onQuizStatsCalculated(0, 0);
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        
        firestore.collection("user_profiles")
                .document(userId)
                .collection("learning_logs")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalQuizzes = 0;
                    int correctQuizzes = 0;
                    
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Boolean passed = doc.getBoolean("passed");
                        if (passed != null) {
                            totalQuizzes++;
                            if (passed) {
                                correctQuizzes++;
                            }
                        }
                    }
                    
                    if (listener != null) {
                        listener.onQuizStatsCalculated(totalQuizzes, correctQuizzes);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error calculating quiz stats: " + e.getMessage());
                    if (listener != null) {
                        listener.onQuizStatsCalculated(0, 0);
                    }
                });
    }
    
    /**
     * Tính số từ vựng đã học theo building (từ learning_logs với passed = true)
     */
    public void calculateVocabularyLearnedByBuilding(String buildingId, OnVocabularyLearnedCalculatedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onVocabularyLearnedCalculated(0);
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        
        firestore.collection("user_profiles")
                .document(userId)
                .collection("learning_logs")
                .whereEqualTo("buildingId", buildingId)
                .whereEqualTo("passed", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Đếm số từ vựng unique (không trùng lặp)
                    java.util.Set<String> uniqueVocabularies = new java.util.HashSet<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String vocab = doc.getString("vocabularyEnglish");
                        if (vocab != null) {
                            uniqueVocabularies.add(vocab.toLowerCase());
                        }
                    }
                    
                    int vocabularyLearned = uniqueVocabularies.size();
                    
                    // Cập nhật vào building progress
                    BuildingProgressRepository.getInstance().updateVocabularyLearned(buildingId, vocabularyLearned);
                    
                    if (listener != null) {
                        listener.onVocabularyLearnedCalculated(vocabularyLearned);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error calculating vocabulary learned: " + e.getMessage());
                    if (listener != null) {
                        listener.onVocabularyLearnedCalculated(0);
                    }
                });
    }
    
    /**
     * Tính tổng số từ vựng đã học (tất cả buildings)
     */
    public void calculateTotalVocabularyLearned(OnVocabularyLearnedCalculatedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onVocabularyLearnedCalculated(0);
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        
        firestore.collection("user_profiles")
                .document(userId)
                .collection("learning_logs")
                .whereEqualTo("passed", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Đếm số từ vựng unique (không trùng lặp)
                    java.util.Set<String> uniqueVocabularies = new java.util.HashSet<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String vocab = doc.getString("vocabularyEnglish");
                        if (vocab != null) {
                            uniqueVocabularies.add(vocab.toLowerCase());
                        }
                    }
                    
                    int totalVocabularyLearned = uniqueVocabularies.size();
                    
                    // Cập nhật vào user_profiles
                    updateTotalVocabularyLearnedInProfile(userId, totalVocabularyLearned);
                    
                    if (listener != null) {
                        listener.onVocabularyLearnedCalculated(totalVocabularyLearned);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error calculating total vocabulary learned: " + e.getMessage());
                    if (listener != null) {
                        listener.onVocabularyLearnedCalculated(0);
                    }
                });
    }
    
    /**
     * Cập nhật totalVocabularyLearned vào user_profiles
     */
    private void updateTotalVocabularyLearnedInProfile(String userId, int totalVocabularyLearned) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("totalVocabularyLearned", totalVocabularyLearned);
        
        firestore.collection("user_profiles")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Total vocabulary learned updated: " + totalVocabularyLearned);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating total vocabulary learned: " + e.getMessage());
                });
    }
    
    // Interfaces
    public interface OnStreakCalculatedListener {
        void onStreakCalculated(int streak);
    }
    
    public interface OnTotalXPCalculatedListener {
        void onTotalXPCalculated(int totalXP);
    }
    
    public interface OnQuizStatsCalculatedListener {
        void onQuizStatsCalculated(int totalQuizzes, int correctQuizzes);
    }
    
    public interface OnVocabularyLearnedCalculatedListener {
        void onVocabularyLearnedCalculated(int vocabularyLearned);
    }
}

