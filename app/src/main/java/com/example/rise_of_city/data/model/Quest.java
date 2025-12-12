package com.example.rise_of_city.data.model;

public class Quest {
    private String name;
    private String rewards;
    private int progress;
    private int maxProgress;
    private int iconResId; // Resource ID for the icon

    public Quest(String name, String rewards, int progress, int maxProgress, int iconResId) {
        this.name = name;
        this.rewards = rewards;
        this.progress = progress;
        this.maxProgress = maxProgress;
        this.iconResId = iconResId;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getRewards() {
        return rewards;
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
}
