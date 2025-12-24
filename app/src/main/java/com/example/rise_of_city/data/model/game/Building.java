package com.example.rise_of_city.data.model.game;

import java.io.Serializable;

public class Building implements Serializable {
    private String id;
    private String name;
    private int level;
    private int currentExp;
    private int maxExp;
    private boolean hasMission;
    private boolean isLocked;
    private String requiredLessonName;

    public Building(String id, String name, int level, int currentExp, int maxExp, boolean hasMission) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.currentExp = currentExp;
        this.maxExp = maxExp;
        this.hasMission = hasMission;
        this.isLocked = false;
        this.requiredLessonName = null;
    }

    public Building(String id, String name, int level, int currentExp, int maxExp, boolean hasMission, boolean isLocked, String requiredLessonName) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.currentExp = currentExp;
        this.maxExp = maxExp;
        this.hasMission = hasMission;
        this.isLocked = isLocked;
        this.requiredLessonName = requiredLessonName;
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getCurrentExp() { return currentExp; }
    public int getMaxExp() { return maxExp; }
    public boolean isHasMission() { return hasMission; }
    public boolean isLocked() { return isLocked; }
    public String getRequiredLessonName() {
        return requiredLessonName != null ? requiredLessonName : "Kiến thức cơ bản";
    }

    // --- Setters (Sửa lỗi BuildingDetail) ---
    public void setLevel(int level) { this.level = level; }
    public void setLocked(boolean locked) { this.isLocked = locked; }
    public void setHasMission(boolean hasMission) { this.hasMission = hasMission; }
    public void setRequiredLessonName(String name) { this.requiredLessonName = name; }

    public void addExp(int amount) {
        this.currentExp += amount;
        if (this.currentExp >= maxExp) {
            this.level++;
            this.currentExp = 0;
            this.maxExp *= 1.5;
        }
    }
}