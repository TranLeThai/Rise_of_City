package com.example.rise_of_city.data.model;

import java.io.Serializable;

public class Building implements Serializable {
    private String id;
    private String name;
    private int level;
    private int currentExp;
    private int maxExp;
    private boolean hasMission;
    private boolean isLocked;
    private String requiredLessonName; // Tên bài học cần hoàn thành để mở khóa

    public Building(String id, String name, int level, int currentExp, int maxExp, boolean hasMission)
    {
        this.id = id;
        this.name = name;
        this.level = level;
        this.currentExp = currentExp;
        this.maxExp = maxExp;
        this.hasMission = hasMission;
        this.isLocked = false;
        this.requiredLessonName = null;
    }

    public Building(String id, String name, int level, int currentExp, int maxExp, boolean hasMission, boolean isLocked, String requiredLessonName)
    {
        this.id = id;
        this.name = name;
        this.level = level;
        this.currentExp = currentExp;
        this.maxExp = maxExp;
        this.hasMission = hasMission;
        this.isLocked = isLocked;
        this.requiredLessonName = requiredLessonName;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getCurrentExp() { return currentExp; }
    public int getMaxExp() { return maxExp; }
    public boolean isHasMission() { return hasMission; }
    public boolean isLocked() { return isLocked; }
    public String getRequiredLessonName() { return requiredLessonName != null ? requiredLessonName : "Thì hiện tại đơn"; }
    // Logic nghiệp vụ nhỏ có thể để ở đây
    public void addExp(int amount) {
        this.currentExp += amount;
        if (this.currentExp >= maxExp) {
            this.level++;
            this.currentExp = 0;
            this.maxExp *= 1.5; // Ví dụ: Tăng exp yêu cầu lên 1.5 lần
        }
    }
}
