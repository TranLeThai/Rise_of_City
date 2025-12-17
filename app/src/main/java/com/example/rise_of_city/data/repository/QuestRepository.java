package com.example.rise_of_city.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.rise_of_city.data.model.Quest;
import com.example.rise_of_city.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository để quản lý quests từ Firebase
 */
public class QuestRepository {
    private static final String TAG = "QuestRepository";
    private static QuestRepository instance;
    
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    
    private QuestRepository() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    
    public static QuestRepository getInstance() {
        if (instance == null) {
            instance = new QuestRepository();
        }
        return instance;
    }
    
    /**
     * Lấy tất cả quests của user từ Firebase
     * Structure: users/{userId}/quests/{questId}
     */
    public void getAllQuests(OnQuestsLoadedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onError("Người dùng chưa đăng nhập");
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String questsPath = "users/" + userId + "/quests";
        
        firestore.collection(questsPath)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Quest> quests = new ArrayList<>();
                    
                    // List các quest ID cũ không liên quan đến tiếng Anh, cần filter out
                    List<String> deprecatedQuestIds = new ArrayList<>();
                    deprecatedQuestIds.add("quest_catch_ink"); // Quest "Bắt liên tục mực" 
                    deprecatedQuestIds.add("quest_reach_score"); // Quest "Đạt điểm chơi ngay" - game, không phải tiếng Anh
                    deprecatedQuestIds.add("quest_shoot_bullet"); // Quest "Bắn một viên đạn" - game, không phải tiếng Anh
                    deprecatedQuestIds.add("quest_give_gold"); // Quest "Tặng vàng cho bạn" - không liên quan đến tiếng Anh
                    deprecatedQuestIds.add("quest_complete_all"); // Quest "Hoàn thành tất cả nhiệm vụ" - không phải quiz
                    deprecatedQuestIds.add("quest_pronunciation"); // Quest "Thực hành phát âm 5 từ" - bỏ vì không thể phát âm trên giả lập
                    deprecatedQuestIds.add("quest_pronunciation_10"); // Quest "Thực hành phát âm 10 từ" - bỏ vì không thể phát âm trên giả lập
                    
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Skip các quest đã deprecated (không liên quan đến tiếng Anh)
                        if (deprecatedQuestIds.contains(doc.getId())) {
                            // Xóa quest deprecated khỏi Firebase
                            doc.getReference().delete();
                            continue;
                        }
                        
                        Quest quest = documentToQuest(doc);
                        if (quest != null) {
                            quests.add(quest);
                        }
                    }
                    
                    // Nếu chưa có quests, khởi tạo quests mặc định
                    if (quests.isEmpty()) {
                        initializeDefaultQuests(userId, listener);
                    } else {
                        if (listener != null) {
                            listener.onQuestsLoaded(quests);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading quests: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }
    
    /**
     * Khởi tạo quests mặc định cho user mới
     */
    private void initializeDefaultQuests(String userId, OnQuestsLoadedListener listener) {
        List<Map<String, Object>> defaultQuests = new ArrayList<>();
        
        // Quest 1: Hoàn thành bài quiz Thì hiện tại đơn (Grammar)
        Map<String, Object> quest1 = new HashMap<>();
        quest1.put("id", "quest_grammar_present_simple");
        quest1.put("name", "Hoàn thành bài quiz Thì hiện tại đơn");
        quest1.put("goldReward", 200);
        quest1.put("xpReward", 300);
        quest1.put("progress", 0);
        quest1.put("maxProgress", 1);
        quest1.put("iconResId", "ic_chat");
        quest1.put("completed", false);
        quest1.put("claimed", false);
        quest1.put("questType", "complete_grammar_quiz");
        quest1.put("actionType", "navigate_to_quiz");
        quest1.put("quizType", "grammar");
        quest1.put("lessonName", "Thì hiện tại đơn");
        defaultQuests.add(quest1);
        
        // Quest 2: Hoàn thành bài quiz ngữ pháp - Thì quá khứ đơn (Grammar)
        Map<String, Object> quest2 = new HashMap<>();
        quest2.put("id", "quest_grammar_past_simple");
        quest2.put("name", "Hoàn thành bài quiz Thì quá khứ đơn");
        quest2.put("goldReward", 180);
        quest2.put("xpReward", 280);
        quest2.put("progress", 0);
        quest2.put("maxProgress", 1);
        quest2.put("iconResId", "ic_chat");
        quest2.put("completed", false);
        quest2.put("claimed", false);
        quest2.put("questType", "complete_grammar_quiz");
        quest2.put("actionType", "navigate_to_quiz");
        quest2.put("quizType", "grammar");
        quest2.put("lessonName", "Thì quá khứ đơn");
        defaultQuests.add(quest2);
        
        // Quest 4: Hoàn thành bài quiz Thì hiện tại tiếp diễn (Grammar)
        Map<String, Object> quest4 = new HashMap<>();
        quest4.put("id", "quest_grammar_present_continuous");
        quest4.put("name", "Hoàn thành bài quiz Thì hiện tại tiếp diễn");
        quest4.put("goldReward", 200);
        quest4.put("xpReward", 300);
        quest4.put("progress", 0);
        quest4.put("maxProgress", 1);
        quest4.put("iconResId", "ic_apple");
        quest4.put("completed", false);
        quest4.put("claimed", false);
        quest4.put("questType", "complete_grammar_quiz");
        quest4.put("actionType", "navigate_to_quiz");
        quest4.put("quizType", "grammar");
        quest4.put("lessonName", "Thì hiện tại tiếp diễn");
        defaultQuests.add(quest4);
        
        // Quest 5: Hoàn thành bài đọc hiểu (khác với vocabulary quiz)
        Map<String, Object> quest6_reading = new HashMap<>();
        quest6_reading.put("id", "quest_reading");
        quest6_reading.put("name", "Hoàn thành bài đọc hiểu");
        quest6_reading.put("goldReward", 220);
        quest6_reading.put("xpReward", 350);
        quest6_reading.put("progress", 0);
        quest6_reading.put("maxProgress", 1);
        quest6_reading.put("iconResId", "ic_apple");
        quest6_reading.put("completed", false);
        quest6_reading.put("claimed", false);
        quest6_reading.put("questType", "complete_reading");
        quest6_reading.put("actionType", "navigate_to_quiz");
        quest6_reading.put("quizType", "reading");
        defaultQuests.add(quest6_reading);
        
        // Quest 7: Điền từ vào chỗ trống (Writing quiz)
        Map<String, Object> quest7_writing = new HashMap<>();
        quest7_writing.put("id", "quest_writing");
        quest7_writing.put("name", "Điền từ vào chỗ trống");
        quest7_writing.put("goldReward", 180);
        quest7_writing.put("xpReward", 250);
        quest7_writing.put("progress", 0);
        quest7_writing.put("maxProgress", 5);
        quest7_writing.put("iconResId", "ic_apple");
        quest7_writing.put("completed", false);
        quest7_writing.put("claimed", false);
        quest7_writing.put("questType", "complete_writing_quiz");
        quest7_writing.put("actionType", "navigate_to_quiz");
        quest7_writing.put("quizType", "writing");
        defaultQuests.add(quest7_writing);
        
        // Quest 8: Hoàn thành câu (Sentence Completion quiz)
        Map<String, Object> quest8_sentence = new HashMap<>();
        quest8_sentence.put("id", "quest_sentence_completion");
        quest8_sentence.put("name", "Hoàn thành câu");
        quest8_sentence.put("goldReward", 200);
        quest8_sentence.put("xpReward", 280);
        quest8_sentence.put("progress", 0);
        quest8_sentence.put("maxProgress", 3);
        quest8_sentence.put("iconResId", "ic_chat");
        quest8_sentence.put("completed", false);
        quest8_sentence.put("claimed", false);
        quest8_sentence.put("questType", "complete_sentence_completion_quiz");
        quest8_sentence.put("actionType", "navigate_to_quiz");
        quest8_sentence.put("quizType", "sentence_completion");
        defaultQuests.add(quest8_sentence);
        
        // Quest 9: Sắp xếp từ thành câu (Word Order quiz)
        Map<String, Object> quest9_wordorder = new HashMap<>();
        quest9_wordorder.put("id", "quest_word_order");
        quest9_wordorder.put("name", "Sắp xếp từ thành câu");
        quest9_wordorder.put("goldReward", 220);
        quest9_wordorder.put("xpReward", 300);
        quest9_wordorder.put("progress", 0);
        quest9_wordorder.put("maxProgress", 3);
        quest9_wordorder.put("iconResId", "ic_chat");
        quest9_wordorder.put("completed", false);
        quest9_wordorder.put("claimed", false);
        quest9_wordorder.put("questType", "complete_word_order_quiz");
        quest9_wordorder.put("actionType", "navigate_to_quiz");
        quest9_wordorder.put("quizType", "word_order");
        defaultQuests.add(quest9_wordorder);
        
        // Quest 10: Từ đồng nghĩa/trái nghĩa (Synonym/Antonym quiz)
        Map<String, Object> quest10_synonym = new HashMap<>();
        quest10_synonym.put("id", "quest_synonym_antonym");
        quest10_synonym.put("name", "Từ đồng nghĩa/trái nghĩa");
        quest10_synonym.put("goldReward", 190);
        quest10_synonym.put("xpReward", 270);
        quest10_synonym.put("progress", 0);
        quest10_synonym.put("maxProgress", 5);
        quest10_synonym.put("iconResId", "ic_apple");
        quest10_synonym.put("completed", false);
        quest10_synonym.put("claimed", false);
        quest10_synonym.put("questType", "complete_synonym_antonym_quiz");
        quest10_synonym.put("actionType", "navigate_to_quiz");
        quest10_synonym.put("quizType", "synonym_antonym");
        defaultQuests.add(quest10_synonym);
        
        // Lưu tất cả quests vào Firebase
        String questsPath = "users/" + userId + "/quests";
        List<Quest> quests = new ArrayList<>();
        
        for (Map<String, Object> questData : defaultQuests) {
            String questId = (String) questData.get("id");
            firestore.collection(questsPath).document(questId).set(questData);
            
            // Convert to Quest object
            Quest quest = mapToQuest(questId, questData);
            if (quest != null) {
                quests.add(quest);
            }
        }
        
        if (listener != null) {
            listener.onQuestsLoaded(quests);
        }
    }
    
    /**
     * Cập nhật progress của quest
     */
    public void updateQuestProgress(String questId, int progress, OnQuestUpdatedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onError("Người dùng chưa đăng nhập");
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String questPath = "users/" + userId + "/quests/" + questId;
        
        firestore.document(questPath).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        if (listener != null) {
                            listener.onError("Quest không tồn tại");
                        }
                        return;
                    }
                    
                    int maxProgress = documentSnapshot.getLong("maxProgress") != null ? 
                                     documentSnapshot.getLong("maxProgress").intValue() : 1;
                    boolean completed = progress >= maxProgress;
                    boolean wasCompleted = Boolean.TRUE.equals(documentSnapshot.getBoolean("completed"));
                    
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("progress", progress);
                    updates.put("completed", completed);
                    
                    // Nếu quest vừa được hoàn thành, lưu thời gian hoàn thành
                    if (completed && !wasCompleted) {
                        updates.put("completedAt", System.currentTimeMillis());
                    }
                    
                    firestore.document(questPath).update(updates)
                            .addOnSuccessListener(aVoid -> {
                                if (listener != null) {
                                    listener.onQuestUpdated(questId, progress, completed);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating quest progress: " + e.getMessage());
                                if (listener != null) {
                                    listener.onError(e.getMessage());
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error reading quest: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }
    
    /**
     * Tăng progress của quest lên 1
     */
    public void incrementQuestProgress(String questId, OnQuestUpdatedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onError("Người dùng chưa đăng nhập");
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String questPath = "users/" + userId + "/quests/" + questId;
        
        firestore.document(questPath).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        if (listener != null) {
                            listener.onError("Quest không tồn tại");
                        }
                        return;
                    }
                    
                    int currentProgress = documentSnapshot.getLong("progress") != null ? 
                                         documentSnapshot.getLong("progress").intValue() : 0;
                    int maxProgress = documentSnapshot.getLong("maxProgress") != null ? 
                                     documentSnapshot.getLong("maxProgress").intValue() : 1;
                    
                    int newProgress = Math.min(currentProgress + 1, maxProgress);
                    boolean completed = newProgress >= maxProgress;
                    boolean wasCompleted = Boolean.TRUE.equals(documentSnapshot.getBoolean("completed"));
                    
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("progress", newProgress);
                    updates.put("completed", completed);
                    
                    if (completed && !wasCompleted) {
                        updates.put("completedAt", System.currentTimeMillis());
                    }
                    
                    firestore.document(questPath).update(updates)
                            .addOnSuccessListener(aVoid -> {
                                if (listener != null) {
                                    listener.onQuestUpdated(questId, newProgress, completed);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error incrementing quest progress: " + e.getMessage());
                                if (listener != null) {
                                    listener.onError(e.getMessage());
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error reading quest: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }
    
    /**
     * Claim reward của quest (nhận vàng và XP)
     */
    public void claimQuestReward(String questId, OnQuestRewardClaimedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onError("Người dùng chưa đăng nhập");
            }
            return;
        }
        
        String userId = auth.getCurrentUser().getUid();
        String questPath = "users/" + userId + "/quests/" + questId;
        
        firestore.document(questPath).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        if (listener != null) {
                            listener.onError("Quest không tồn tại");
                        }
                        return;
                    }
                    
                    boolean completed = Boolean.TRUE.equals(documentSnapshot.getBoolean("completed"));
                    boolean claimed = Boolean.TRUE.equals(documentSnapshot.getBoolean("claimed"));
                    
                    if (!completed) {
                        if (listener != null) {
                            listener.onError("Quest chưa hoàn thành");
                        }
                        return;
                    }
                    
                    if (claimed) {
                        if (listener != null) {
                            listener.onError("Reward đã được nhận");
                        }
                        return;
                    }
                    
                    int goldReward = documentSnapshot.getLong("goldReward") != null ? 
                                    documentSnapshot.getLong("goldReward").intValue() : 0;
                    int xpReward = documentSnapshot.getLong("xpReward") != null ? 
                                  documentSnapshot.getLong("xpReward").intValue() : 0;
                    
                    // Đánh dấu quest đã claim
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("claimed", true);
                    updates.put("claimedAt", System.currentTimeMillis());
                    
                    firestore.document(questPath).update(updates)
                            .addOnSuccessListener(aVoid -> {
                                // Thêm vàng cho user
                                GoldRepository goldRepo = GoldRepository.getInstance();
                                goldRepo.addGold(goldReward, new GoldRepository.OnGoldUpdatedListener() {
                                    @Override
                                    public void onGoldUpdated(int newGold) {
                                        // TODO: Add XP to user profile if needed
                                        // For now, we'll just update gold
                                        if (listener != null) {
                                            listener.onRewardClaimed(questId, goldReward, xpReward);
                                        }
                                    }
                                    
                                    @Override
                                    public void onError(String error) {
                                        // Rollback claim status if gold update fails
                                        Map<String, Object> rollback = new HashMap<>();
                                        rollback.put("claimed", false);
                                        firestore.document(questPath).update(rollback);
                                        
                                        if (listener != null) {
                                            listener.onError("Lỗi khi cập nhật vàng: " + error);
                                        }
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error claiming quest reward: " + e.getMessage());
                                if (listener != null) {
                                    listener.onError(e.getMessage());
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error reading quest: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }
    
    /**
     * Convert Firestore DocumentSnapshot to Quest object
     */
    private Quest documentToQuest(DocumentSnapshot doc) {
        try {
            String id = doc.getId();
            String name = doc.getString("name");
            Long goldReward = doc.getLong("goldReward");
            Long xpReward = doc.getLong("xpReward");
            Long progress = doc.getLong("progress");
            Long maxProgress = doc.getLong("maxProgress");
            String iconResIdStr = doc.getString("iconResId");
            Boolean completed = doc.getBoolean("completed");
            Boolean claimed = doc.getBoolean("claimed");
            String questType = doc.getString("questType");
            String targetBuildingId = doc.getString("targetBuildingId");
            String actionType = doc.getString("actionType");
            String quizType = doc.getString("quizType");
            String lessonName = doc.getString("lessonName");
            
            // iconResId will be set in Activity/Adapter with context
            int iconResId = 0;
            
            return new Quest(
                id,
                name != null ? name : "",
                goldReward != null ? goldReward.intValue() : 0,
                xpReward != null ? xpReward.intValue() : 0,
                progress != null ? progress.intValue() : 0,
                maxProgress != null ? maxProgress.intValue() : 1,
                iconResId,
                iconResIdStr,
                completed != null ? completed : false,
                claimed != null ? claimed : false,
                questType,
                targetBuildingId,
                actionType,
                quizType != null ? quizType : "vocabulary",
                lessonName
            );
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to quest: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert Map to Quest object
     */
    private Quest mapToQuest(String id, Map<String, Object> data) {
        try {
            String name = (String) data.get("name");
            Long goldReward = data.get("goldReward") instanceof Long ? 
                            (Long) data.get("goldReward") : 
                            ((Integer) data.get("goldReward")).longValue();
            Long xpReward = data.get("xpReward") instanceof Long ? 
                           (Long) data.get("xpReward") : 
                           ((Integer) data.get("xpReward")).longValue();
            Long progress = data.get("progress") instanceof Long ? 
                           (Long) data.get("progress") : 
                           ((Integer) data.get("progress")).longValue();
            Long maxProgress = data.get("maxProgress") instanceof Long ? 
                              (Long) data.get("maxProgress") : 
                              ((Integer) data.get("maxProgress")).longValue();
            String iconResIdStr = (String) data.get("iconResId");
            Boolean completed = (Boolean) data.get("completed");
            Boolean claimed = (Boolean) data.get("claimed");
            String questType = (String) data.get("questType");
            String targetBuildingId = (String) data.get("targetBuildingId");
            String actionType = (String) data.get("actionType");
            String quizType = (String) data.get("quizType");
            String lessonName = (String) data.get("lessonName");
            
            // iconResId will be set in Activity/Adapter with context
            int iconResId = 0;
            
            return new Quest(
                id,
                name != null ? name : "",
                goldReward != null ? goldReward.intValue() : 0,
                xpReward != null ? xpReward.intValue() : 0,
                progress != null ? progress.intValue() : 0,
                maxProgress != null ? maxProgress.intValue() : 1,
                iconResId,
                iconResIdStr,
                completed != null ? completed : false,
                claimed != null ? claimed : false,
                questType,
                targetBuildingId,
                actionType,
                quizType != null ? quizType : "vocabulary",
                lessonName
            );
        } catch (Exception e) {
            Log.e(TAG, "Error converting map to quest: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert icon resource name string to drawable resource ID using context
     */
    public static int getIconResourceId(Context context, String iconName) {
        if (iconName == null || iconName.isEmpty()) {
            return 0;
        }
        
        // Map string names to drawable resource IDs
        return context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
    }
    
    // Interfaces
    public interface OnQuestsLoadedListener {
        void onQuestsLoaded(List<Quest> quests);
        void onError(String error);
    }
    
    public interface OnQuestUpdatedListener {
        void onQuestUpdated(String questId, int progress, boolean completed);
        void onError(String error);
    }
    
    public interface OnQuestRewardClaimedListener {
        void onRewardClaimed(String questId, int goldReward, int xpReward);
        void onError(String error);
    }
}

