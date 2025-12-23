package com.example.rise_of_city.data.model;

public class Quest {
    private String id;
    private String name;
    private int goldReward;
    private int xpReward;
    private int progress;
    private int maxProgress;
    private int iconResId; // Resource ID for the icon
    private String iconName; // Icon name string (from Firebase)
    private boolean completed;
    private boolean claimed;
    private String questType; // "complete_all_quests", "catch_ink", "give_gold", "reach_score", etc.
    private String targetBuildingId; // Optional: building ID for building-specific quests
    private String actionType; // "navigate_to_game", "navigate_to_quiz", etc.
    private String quizType; // "vocabulary", "grammar", "pronunciation", "reading", "listening", etc.
    private String lessonName; // Tên lesson (ví dụ: "Thì hiện tại đơn") - dùng cho grammar quiz

    // Constructor cho Firebase data
    public Quest(String id, String name, int goldReward, int xpReward, int progress, int maxProgress, 
                 int iconResId, String iconName, boolean completed, boolean claimed, String questType, 
                 String targetBuildingId, String actionType, String quizType, String lessonName) {
        this.id = id;
        this.name = name;
        this.goldReward = goldReward;
        this.xpReward = xpReward;
        this.progress = progress;
        this.maxProgress = maxProgress;
        this.iconResId = iconResId;
        this.iconName = iconName;
        this.completed = completed;
        this.claimed = claimed;
        this.questType = questType;
        this.targetBuildingId = targetBuildingId;
        this.actionType = actionType;
        this.quizType = quizType;
        this.lessonName = lessonName;
    }

    // Constructor cho backward compatibility với sample data
    public Quest(String name, int goldReward, int xpReward, int progress, int maxProgress, int iconResId) {
        this(null, name, goldReward, xpReward, progress, maxProgress, iconResId, null, false, false, null, null, null, "vocabulary", null);
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getGoldReward() {
        return goldReward;
    }

    public int getXpReward() {
        return xpReward;
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getIconName() {
        return iconName;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isClaimed() {
        return claimed;
    }

    public String getQuestType() {
        return questType;
    }

    public String getTargetBuildingId() {
        return targetBuildingId;
    }

    public String getActionType() {
        return actionType;
    }

    public String getQuizType() {
        return quizType;
    }

    public String getLessonName() {
        return lessonName;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        // Tự động update completed status
        if (progress >= maxProgress) {
            this.completed = true;
        }
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }
}
