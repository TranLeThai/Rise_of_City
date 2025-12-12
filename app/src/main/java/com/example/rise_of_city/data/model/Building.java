package com.example.rise_of_city.data.model;

public class Building {
    private String id;
    private String name;
    private int level;
    private int currentExp;
    private int maxExp;
    private boolean hasMission;

    public Building(String id, String name, int level, int currentExp, int maxExp, boolean hasMission)
    {
        this.id = id;
        this.name = name;
        this.level = level;
        this.currentExp = currentExp;
        this.maxExp = maxExp;
        this.hasMission = hasMission;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getCurrentExp() { return currentExp; }
    public int getMaxExp() { return maxExp; }
    public boolean isHasMission() { return hasMission; }
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
