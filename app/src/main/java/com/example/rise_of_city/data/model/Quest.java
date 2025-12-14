package com.example.rise_of_city.data.model;

public class Quest {
    private String name;
    private int goldReward;
    private int xpReward;
    private int progress;
    private int maxProgress;
    private int iconResId; // Resource ID for the icon

    public Quest(String name, int goldReward, int xpReward, int progress, int maxProgress, int iconResId) {
        this.name = name;
        this.goldReward = goldReward;
        this.xpReward = xpReward;
        this.progress = progress;
        this.maxProgress = maxProgress;
        this.iconResId = iconResId;
    }

    // Getters
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
}
