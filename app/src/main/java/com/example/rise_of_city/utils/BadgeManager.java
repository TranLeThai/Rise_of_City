package com.example.rise_of_city.utils;

import android.content.Context;
import android.util.Log;

import com.example.rise_of_city.data.model.Badge;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BadgeManager {
    private static final String TAG = "BadgeManager";
    private static BadgeManager instance;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    private BadgeManager() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }
    
    public static BadgeManager getInstance() {
        if (instance == null) {
            instance = new BadgeManager();
        }
        return instance;
    }
    
    /**
     * Check and unlock badges based on user progress
     */
    public void checkAndUnlockBadges(Context context, DocumentSnapshot userDoc, OnBadgeUnlockedListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || userDoc == null) return;
        
        List<Badge> newlyUnlocked = new ArrayList<>();
        
        // Get current user stats
        Long totalXP = userDoc.getLong("totalXP");
        Long streak = userDoc.getLong("streak");
        Boolean surveyCompleted = userDoc.getBoolean("surveyCompleted");
        String surveyLevel = userDoc.getString("surveyLevel");
        Map<String, Object> buildings = (Map<String, Object>) userDoc.get("buildings");
        Map<String, Object> currentBadges = (Map<String, Object>) userDoc.get("badges");
        
        // Initialize badges if not exists
        if (currentBadges == null) {
            currentBadges = new HashMap<>();
        }
        
        totalXP = totalXP != null ? totalXP : 0L;
        streak = streak != null ? streak : 0L;
        
        // Check each badge type
        for (Badge.BadgeType type : Badge.BadgeType.values()) {
            String badgeId = type.name();
            boolean alreadyUnlocked = currentBadges.containsKey(badgeId) && 
                ((Map<String, Object>) currentBadges.get(badgeId)).get("unlocked") != null &&
                (Boolean) ((Map<String, Object>) currentBadges.get(badgeId)).get("unlocked");
            
            if (alreadyUnlocked) continue;
            
            boolean shouldUnlock = false;
            
            switch (type) {
                case BEGINNER:
                    shouldUnlock = surveyCompleted != null && surveyCompleted;
                    break;
                    
                case STREAK_3:
                    shouldUnlock = streak >= 3;
                    break;
                    
                case STREAK_7:
                    shouldUnlock = streak >= 7;
                    break;
                    
                case STREAK_30:
                    shouldUnlock = streak >= 30;
                    break;
                    
                case XP_100:
                    shouldUnlock = totalXP >= 100;
                    break;
                    
                case XP_500:
                    shouldUnlock = totalXP >= 500;
                    break;
                    
                case XP_1000:
                    shouldUnlock = totalXP >= 1000;
                    break;
                    
                case XP_5000:
                    shouldUnlock = totalXP >= 5000;
                    break;
                    
                case SCHOOL_COMPLETE:
                    shouldUnlock = isBuildingCompleted(buildings, "school");
                    break;
                    
                case COFFEE_COMPLETE:
                    shouldUnlock = isBuildingCompleted(buildings, "coffee_shop");
                    break;
                    
                case PARK_COMPLETE:
                    shouldUnlock = isBuildingCompleted(buildings, "park");
                    break;
                    
                case HOUSE_COMPLETE:
                    shouldUnlock = isBuildingCompleted(buildings, "house");
                    break;
                    
                case LIBRARY_COMPLETE:
                    shouldUnlock = isBuildingCompleted(buildings, "library");
                    break;
                    
                case MASTER:
                    shouldUnlock = "Advanced".equals(surveyLevel);
                    break;
                    
                // Add more conditions for other badges
            }
            
            if (shouldUnlock) {
                unlockBadge(user.getUid(), type, newlyUnlocked);
            }
        }
        
        // Notify listener if any badges were unlocked
        if (!newlyUnlocked.isEmpty() && listener != null) {
            listener.onBadgesUnlocked(newlyUnlocked);
        }
    }
    
    private boolean isBuildingCompleted(Map<String, Object> buildings, String buildingId) {
        if (buildings == null) return false;
        Map<String, Object> building = (Map<String, Object>) buildings.get(buildingId);
        if (building == null) return false;
        Boolean completed = (Boolean) building.get("completed");
        return completed != null && completed;
    }
    
    private void unlockBadge(String userId, Badge.BadgeType type, List<Badge> newlyUnlocked) {
        Badge badge = new Badge(type);
        badge.setUnlocked(true);
        badge.setUnlockedAt(System.currentTimeMillis());
        
        // Save to Firestore
        Map<String, Object> badgeData = new HashMap<>();
        badgeData.put("name", badge.getName());
        badgeData.put("description", badge.getDescription());
        badgeData.put("color", badge.getColor());
        badgeData.put("unlocked", true);
        badgeData.put("unlockedAt", System.currentTimeMillis());
        badgeData.put("type", type.name());
        
        db.collection("user_profiles")
                .document(userId)
                .update("badges." + type.name(), badgeData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Badge unlocked: " + type.getName());
                    newlyUnlocked.add(badge);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error unlocking badge: ", e);
                });
    }
    
    /**
     * Get all badges with their unlock status
     */
    public void getAllBadges(String userId, OnBadgesLoadedListener listener) {
        db.collection("user_profiles")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<Badge> badges = new ArrayList<>();
                    Map<String, Object> userBadges = (Map<String, Object>) documentSnapshot.get("badges");
                    
                    // Create all badge types
                    for (Badge.BadgeType type : Badge.BadgeType.values()) {
                        Badge badge = new Badge(type);
                        
                        // Check if unlocked
                        if (userBadges != null && userBadges.containsKey(type.name())) {
                            Map<String, Object> badgeData = (Map<String, Object>) userBadges.get(type.name());
                            if (badgeData != null) {
                                Boolean unlocked = (Boolean) badgeData.get("unlocked");
                                badge.setUnlocked(unlocked != null && unlocked);
                                
                                Long unlockedAt = (Long) badgeData.get("unlockedAt");
                                badge.setUnlockedAt(unlockedAt);
                            }
                        }
                        
                        // Calculate progress
                        calculateProgress(badge, documentSnapshot);
                        
                        badges.add(badge);
                    }
                    
                    if (listener != null) {
                        listener.onBadgesLoaded(badges);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading badges: ", e);
                    if (listener != null) {
                        listener.onBadgesLoaded(new ArrayList<>());
                    }
                });
    }
    
    private void calculateProgress(Badge badge, DocumentSnapshot userDoc) {
        Long totalXP = userDoc.getLong("totalXP");
        Long streak = userDoc.getLong("streak");
        Boolean surveyCompleted = userDoc.getBoolean("surveyCompleted");
        String surveyLevel = userDoc.getString("surveyLevel");
        Map<String, Object> buildings = (Map<String, Object>) userDoc.get("buildings");
        
        totalXP = totalXP != null ? totalXP : 0L;
        streak = streak != null ? streak : 0L;
        
        int progress = 0;
        int target = 100;
        int currentValue = 0;
        
        switch (badge.getType()) {
            case BEGINNER:
                progress = (surveyCompleted != null && surveyCompleted) ? 100 : 0;
                currentValue = progress > 0 ? 1 : 0;
                target = 1;
                break;
            case STREAK_3:
                currentValue = streak.intValue();
                progress = Math.min(100, (int) (streak * 100 / 3));
                target = 3;
                break;
            case STREAK_7:
                currentValue = streak.intValue();
                progress = Math.min(100, (int) (streak * 100 / 7));
                target = 7;
                break;
            case STREAK_30:
                currentValue = streak.intValue();
                progress = Math.min(100, (int) (streak * 100 / 30));
                target = 30;
                break;
            case XP_100:
                currentValue = totalXP.intValue();
                progress = Math.min(100, (int) (totalXP * 100 / 100));
                target = 100;
                break;
            case XP_500:
                currentValue = totalXP.intValue();
                progress = Math.min(100, (int) (totalXP * 100 / 500));
                target = 500;
                break;
            case XP_1000:
                currentValue = totalXP.intValue();
                progress = Math.min(100, (int) (totalXP * 100 / 1000));
                target = 1000;
                break;
            case XP_5000:
                currentValue = totalXP.intValue();
                progress = Math.min(100, (int) (totalXP * 100 / 5000));
                target = 5000;
                break;
            case MASTER:
                progress = "Advanced".equals(surveyLevel) ? 100 : 0;
                currentValue = progress > 0 ? 1 : 0;
                target = 1;
                break;
            case SCHOOL_COMPLETE:
            case COFFEE_COMPLETE:
            case PARK_COMPLETE:
            case HOUSE_COMPLETE:
            case LIBRARY_COMPLETE:
                // Calculate building completion
                String buildingId = getBuildingIdFromBadgeType(badge.getType());
                boolean completed = isBuildingCompleted(buildings, buildingId);
                progress = completed ? 100 : 0;
                currentValue = completed ? 1 : 0;
                target = 1;
                break;
        }
        
        badge.setProgress(progress);
        badge.setTargetValue(target);
        badge.setCurrentValue(currentValue);
    }
    
    private String getBuildingIdFromBadgeType(Badge.BadgeType type) {
        switch (type) {
            case SCHOOL_COMPLETE: return "school";
            case COFFEE_COMPLETE: return "coffee_shop";
            case PARK_COMPLETE: return "park";
            case HOUSE_COMPLETE: return "house";
            case LIBRARY_COMPLETE: return "library";
            default: return "";
        }
    }
    
    public interface OnBadgeUnlockedListener {
        void onBadgesUnlocked(List<Badge> badges);
    }
    
    public interface OnBadgesLoadedListener {
        void onBadgesLoaded(List<Badge> badges);
    }
}

