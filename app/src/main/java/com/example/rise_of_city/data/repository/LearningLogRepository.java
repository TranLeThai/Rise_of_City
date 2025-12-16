package com.example.rise_of_city.data.repository;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository để ghi log học tập vào Firestore
 */
public class LearningLogRepository {
    private static final String TAG = "LearningLogRepository";
    private static LearningLogRepository instance;
    
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    
    private LearningLogRepository() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    
    public static LearningLogRepository getInstance() {
        if (instance == null) {
            instance = new LearningLogRepository();
        }
        return instance;
    }
    
    /**
     * Ghi log khi user làm quiz
     * @param buildingId ID của building
     * @param passed true nếu quiz đúng, false nếu sai
     * @param vocabularyEnglish Từ vựng đã quiz
     */
    public void logQuizAttempt(String buildingId, boolean passed, String vocabularyEnglish) {
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "User not logged in, cannot log quiz attempt");
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String learningLogsPath = "user_profiles/" + userId + "/learning_logs";
        
        Map<String, Object> logData = new HashMap<>();
        logData.put("buildingId", buildingId);
        logData.put("passed", passed);
        logData.put("vocabularyEnglish", vocabularyEnglish);
        logData.put("timestamp", System.currentTimeMillis());
        
        firestore.collection(learningLogsPath)
                .add(logData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Learning log created: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating learning log: " + e.getMessage());
                });
    }
}

