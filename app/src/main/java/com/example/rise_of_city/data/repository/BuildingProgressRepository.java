package com.example.rise_of_city.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository để quản lý tiến độ building trong Firebase
 */
public class BuildingProgressRepository {
    private static final String TAG = "BuildingProgressRepo";
    private static BuildingProgressRepository instance;
    
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    
    private BuildingProgressRepository() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    
    public static BuildingProgressRepository getInstance() {
        if (instance == null) {
            instance = new BuildingProgressRepository();
        }
        return instance;
    }
    
    /**
     * Cập nhật EXP cho building khi quiz đúng
     * @param buildingId ID của building (ví dụ: "house", "school")
     * @param expGained Số EXP nhận được
     */
    public void addExpToBuilding(String buildingId, int expGained, OnProgressUpdatedListener listener) {
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "User not logged in, cannot update progress");
            if (listener != null) {
                listener.onError("Người dùng chưa đăng nhập");
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String buildingPath = "users/" + userId + "/buildings/" + buildingId;
        
        // Lấy building hiện tại
        firestore.document(buildingPath).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Building đã tồn tại, cập nhật EXP
                        Long currentExp = documentSnapshot.getLong("currentExp");
                        Long maxExp = documentSnapshot.getLong("maxExp");
                        Long level = documentSnapshot.getLong("level");
                        
                        if (currentExp == null) currentExp = 0L;
                        if (maxExp == null) maxExp = 100L;
                        if (level == null) level = 1L;
                        
                        long newExp = currentExp + expGained;
                        long newLevel = level;
                        long newMaxExp = maxExp;
                        
                        // Kiểm tra level up
                        if (newExp >= maxExp) {
                            newLevel++;
                            newExp = 0;
                            newMaxExp = (long) (maxExp * 1.5);
                        }
                        
                        // Kiểm tra building completion (nếu đạt level 5)
                        Boolean currentCompleted = (Boolean) documentSnapshot.get("completed");
                        boolean shouldComplete = false;
                        if (newLevel >= 5 && (currentCompleted == null || !currentCompleted)) {
                            shouldComplete = true;
                        }
                        
                        // Lưu giá trị cuối cùng vào biến final để dùng trong lambda
                        final long finalLevel = newLevel;
                        final int finalExp = (int) newExp;
                        final int finalMaxExp = (int) newMaxExp;
                        
                        // Cập nhật
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("currentExp", newExp);
                        updates.put("level", newLevel);
                        updates.put("maxExp", newMaxExp);
                        
                        // Nếu đạt level 5, đánh dấu completed
                        if (shouldComplete) {
                            updates.put("completed", true);
                            updates.put("completedAt", System.currentTimeMillis());
                        }
                        
                        firestore.document(buildingPath).update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    // Cập nhật totalXP trong user_profiles sau khi EXP thay đổi
                                    updateTotalXPInUserProfile();
                                    if (listener != null) {
                                        listener.onProgressUpdated(finalLevel, finalExp, finalMaxExp);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating building progress: " + e.getMessage());
                                    if (listener != null) {
                                        listener.onError(e.getMessage());
                                    }
                                });
                    } else {
                        // Building chưa tồn tại, tạo mới
                        Map<String, Object> buildingData = new HashMap<>();
                        buildingData.put("id", buildingId);
                        buildingData.put("level", 1);
                        buildingData.put("currentExp", expGained);
                        buildingData.put("maxExp", 100);
                        buildingData.put("completed", false);
                        
                        firestore.document(buildingPath).set(buildingData)
                                .addOnSuccessListener(aVoid -> {
                                    // Cập nhật totalXP trong user_profiles sau khi tạo building mới
                                    updateTotalXPInUserProfile();
                                    if (listener != null) {
                                        listener.onProgressUpdated(1, expGained, 100);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error creating building: " + e.getMessage());
                                    if (listener != null) {
                                        listener.onError(e.getMessage());
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error reading building: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }
    
    /**
     * Unlock building (tạo building progress mới với giá trị mặc định)
     */
    public void unlockBuilding(String buildingId, OnProgressUpdatedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onError("Người dùng chưa đăng nhập");
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String buildingPath = "users/" + userId + "/buildings/" + buildingId;
        
        // Kiểm tra xem building đã tồn tại chưa
        firestore.document(buildingPath).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Building đã tồn tại (đã unlock rồi)
                        Long level = documentSnapshot.getLong("level");
                        Long currentExp = documentSnapshot.getLong("currentExp");
                        Long maxExp = documentSnapshot.getLong("maxExp");
                        
                        int levelInt = level != null ? level.intValue() : 1;
                        int currentExpInt = currentExp != null ? currentExp.intValue() : 0;
                        int maxExpInt = maxExp != null ? maxExp.intValue() : 100;
                        
                        if (listener != null) {
                            listener.onProgressUpdated(levelInt, currentExpInt, maxExpInt);
                        }
                        return;
                    }
                    
                    // Building chưa tồn tại, tạo mới với giá trị mặc định
                    Map<String, Object> buildingData = new HashMap<>();
                    buildingData.put("id", buildingId);
                    buildingData.put("level", 1);
                    buildingData.put("currentExp", 0);
                    buildingData.put("maxExp", 100);
                    buildingData.put("completed", false);
                    buildingData.put("unlockedAt", System.currentTimeMillis());
                    
                    firestore.document(buildingPath).set(buildingData)
                            .addOnSuccessListener(aVoid -> {
                                // Cập nhật totalXP trong user_profiles sau khi unlock building mới
                                updateTotalXPInUserProfile();
                                if (listener != null) {
                                    listener.onProgressUpdated(1, 0, 100);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error unlocking building: " + e.getMessage());
                                if (listener != null) {
                                    listener.onError(e.getMessage());
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking building existence: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }
    
    /**
     * Đánh dấu building đã hoàn thành
     */
    public void completeBuilding(String buildingId, OnProgressUpdatedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onError("Người dùng chưa đăng nhập");
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String buildingPath = "users/" + userId + "/buildings/" + buildingId;
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("completed", true);
        
        firestore.document(buildingPath).update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) {
                        listener.onProgressUpdated(0, 0, 0);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error completing building: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }
    
    /**
     * Lấy tất cả building progress của user
     */
    public void getAllBuildingProgress(OnAllBuildingsLoadedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onError("Người dùng chưa đăng nhập");
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String buildingsPath = "users/" + userId + "/buildings";
        
        firestore.collection(buildingsPath)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Map<String, Object>> buildingProgressMap = new HashMap<>();
                    
                    for (var document : queryDocumentSnapshots) {
                        buildingProgressMap.put(document.getId(), document.getData());
                    }
                    
                    if (listener != null) {
                        listener.onBuildingsLoaded(buildingProgressMap);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading building progress: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }
    
    /**
     * Lấy progress của một building cụ thể
     */
    public void getBuildingProgress(String buildingId, OnProgressLoadedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onError("Người dùng chưa đăng nhập");
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String buildingPath = "users/" + userId + "/buildings/" + buildingId;
        
        firestore.document(buildingPath).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long level = documentSnapshot.getLong("level");
                        Long currentExp = documentSnapshot.getLong("currentExp");
                        Long maxExp = documentSnapshot.getLong("maxExp");
                        
                        int levelInt = level != null ? level.intValue() : 1;
                        int currentExpInt = currentExp != null ? currentExp.intValue() : 0;
                        int maxExpInt = maxExp != null ? maxExp.intValue() : 100;
                        
                        if (listener != null) {
                            listener.onProgressLoaded(levelInt, currentExpInt, maxExpInt);
                        }
                    } else {
                        // Building chưa tồn tại, trả về giá trị mặc định
                        if (listener != null) {
                            listener.onProgressLoaded(1, 0, 100);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading building progress: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }
    
    /**
     * Lấy thông tin building từ collection buildings
     */
    public void getBuildingInfo(String buildingId, OnBuildingInfoLoadedListener listener) {
        firestore.collection("buildings")
                .document(buildingId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if (listener != null) {
                            listener.onBuildingInfoLoaded(documentSnapshot.getData());
                        }
                    } else {
                        if (listener != null) {
                            listener.onError("Building not found: " + buildingId);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading building info: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }
    
    public interface OnProgressUpdatedListener {
        void onProgressUpdated(long level, int currentExp, int maxExp);
        void onError(String error);
    }
    
    public interface OnAllBuildingsLoadedListener {
        void onBuildingsLoaded(Map<String, Map<String, Object>> buildingProgressMap);
        void onError(String error);
    }
    
    /**
     * Cập nhật số từ vựng đã học cho building
     * Và kiểm tra xem đã học hết từ vựng chưa (nếu có thì đánh dấu completed)
     */
    public void updateVocabularyLearned(String buildingId, int vocabularyLearned) {
        if (auth.getCurrentUser() == null) {
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String buildingPath = "users/" + userId + "/buildings/" + buildingId;
        
        // Lấy tổng số từ vựng của building và kiểm tra completion
        firestore.collection("buildings")
                .document(buildingId)
                .get()
                .addOnSuccessListener(buildingDoc -> {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("vocabularyLearned", vocabularyLearned);
                    
                    // Kiểm tra xem đã học hết từ vựng chưa
                    if (buildingDoc.exists()) {
                        Long vocabularyCount = buildingDoc.getLong("vocabularyCount");
                        if (vocabularyCount != null && vocabularyCount > 0) {
                            // Nếu đã học >= 80% từ vựng, coi như completed
                            // Hoặc nếu đã học hết 100%
                            double completionPercent = (vocabularyLearned * 100.0) / vocabularyCount.intValue();
                            
                            // Lấy trạng thái completed hiện tại
                            firestore.document(buildingPath)
                                    .get()
                                    .addOnSuccessListener(userBuildingDoc -> {
                                        Boolean currentCompleted = null;
                                        if (userBuildingDoc.exists()) {
                                            currentCompleted = userBuildingDoc.getBoolean("completed");
                                        }
                                        
                                        // Nếu chưa completed và đạt >= 80% từ vựng, đánh dấu completed
                                        if ((currentCompleted == null || !currentCompleted) && completionPercent >= 80.0) {
                                            updates.put("completed", true);
                                            updates.put("completedAt", System.currentTimeMillis());
                                        }
                                        
                                        // Cập nhật building
                                        firestore.document(buildingPath).update(updates)
                                                .addOnSuccessListener(aVoid -> {
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error updating vocabulary learned: " + e.getMessage());
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error reading building: " + e.getMessage());
                                    });
                        } else {
                            // Không có vocabularyCount, chỉ update vocabularyLearned
                            firestore.document(buildingPath).update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error updating vocabulary learned: " + e.getMessage());
                                    });
                        }
                    } else {
                        // Building info không tồn tại, chỉ update vocabularyLearned
                        firestore.document(buildingPath).update(updates)
                                .addOnSuccessListener(aVoid -> {
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating vocabulary learned: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error reading building info: " + e.getMessage());
                });
    }
    
    /**
     * Cập nhật totalXP trong user_profiles sau khi EXP thay đổi
     */
    private void updateTotalXPInUserProfile() {
        if (auth.getCurrentUser() == null) {
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String buildingsPath = "users/" + userId + "/buildings";
        
        // Tính tổng EXP từ tất cả buildings
        firestore.collection(buildingsPath)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalXP = 0;
                    
                    for (var doc : queryDocumentSnapshots) {
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
                    final int finalTotalXP = totalXP; // Tạo biến final để dùng trong lambda
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("totalXP", finalTotalXP);
                    
                    firestore.collection("user_profiles")
                            .document(userId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating total XP in user profile: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error calculating total XP: " + e.getMessage());
                });
    }
    
    public interface OnBuildingInfoLoadedListener {
        void onBuildingInfoLoaded(Map<String, Object> buildingInfo);
        void onError(String error);
    }
    
    public interface OnProgressLoadedListener {
        void onProgressLoaded(int level, int currentExp, int maxExp);
        void onError(String error);
    }
}

