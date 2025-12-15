package com.example.rise_of_city.data.model;

import com.example.rise_of_city.R;

import java.io.Serializable;

public class Badge implements Serializable {
    public enum BadgeType {
        BEGINNER("Người mới bắt đầu", "Hoàn thành khảo sát kiến thức", "#4CAF50", R.drawable.badge_beginner),
        FIRST_LESSON("Học viên đầu tiên", "Hoàn thành bài học đầu tiên", "#2196F3", R.drawable.badge_lesson),
        STREAK_3("Chuỗi 3 ngày", "Học liên tiếp 3 ngày", "#FF9800", R.drawable.badge_streak),
        STREAK_7("Chuỗi 7 ngày", "Học liên tiếp 7 ngày", "#FF5722", R.drawable.badge_streak),
        STREAK_30("Chuỗi 30 ngày", "Học liên tiếp 30 ngày", "#E91E63", R.drawable.badge_streak),
        XP_100("100 XP", "Đạt 100 điểm kinh nghiệm", "#FFC107", R.drawable.badge_xp),
        XP_500("500 XP", "Đạt 500 điểm kinh nghiệm", "#FF9800", R.drawable.badge_xp),
        XP_1000("1000 XP", "Đạt 1000 điểm kinh nghiệm", "#FF5722", R.drawable.badge_xp),
        XP_5000("5000 XP", "Đạt 5000 điểm kinh nghiệm", "#F44336", R.drawable.badge_xp),
        SCHOOL_COMPLETE("Hoàn thành School", "Hoàn thành tất cả bài học tại School", "#4CAF50", R.drawable.badge_building),
        COFFEE_COMPLETE("Hoàn thành Coffee Shop", "Hoàn thành tất cả bài học tại Coffee Shop", "#795548", R.drawable.badge_building),
        PARK_COMPLETE("Hoàn thành Park", "Hoàn thành tất cả bài học tại Park", "#8BC34A", R.drawable.badge_building),
        HOUSE_COMPLETE("Hoàn thành House", "Hoàn thành tất cả bài học tại House", "#607D8B", R.drawable.badge_building),
        LIBRARY_COMPLETE("Hoàn thành Library", "Hoàn thành tất cả bài học tại Library", "#3F51B5", R.drawable.badge_building),
        PERFECT_SCORE("Điểm tuyệt đối", "Đạt điểm tuyệt đối trong một bài quiz", "#9C27B0", R.drawable.badge_perfect),
        VOCAB_MASTER("Bậc thầy từ vựng", "Học thuộc 100 từ vựng", "#00BCD4", R.drawable.badge_vocab),
        GRAMMAR_EXPERT("Chuyên gia ngữ pháp", "Hoàn thành tất cả bài ngữ pháp", "#FF9800", R.drawable.badge_building),
        SPEAKING_CHAMPION("Vô địch giao tiếp", "Hoàn thành 50 bài luyện nói", "#4CAF50", R.drawable.badge_streak),
        MASTER("Master", "Đạt trình độ Master trong khảo sát", "#FFD700", R.drawable.badge_master),
        LEGEND("Huyền thoại", "Hoàn thành tất cả nội dung", "#FF6B6B", R.drawable.badge_legend);
        
        private String name;
        private String description;
        private String color;
        private int iconResId;
        
        BadgeType(String name, String description, String color, int iconResId) {
            this.name = name;
            this.description = description;
            this.color = color;
            this.iconResId = iconResId;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getColor() { return color; }
        public int getIconResId() { return iconResId; }
    }
    
    private String id;
    private BadgeType type;
    private String name;
    private String description;
    private String color;
    private int iconResId;
    private boolean unlocked;
    private Long unlockedAt;
    private int progress; // Progress towards unlocking (0-100)
    private int targetValue; // Target value to unlock
    private int currentValue; // Current value towards target
    
    public Badge() {
    }
    
    public Badge(BadgeType type) {
        this.type = type;
        this.id = type.name();
        this.name = type.getName();
        this.description = type.getDescription();
        this.color = type.getColor();
        this.iconResId = type.getIconResId();
        this.unlocked = false;
        this.progress = 0;
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public BadgeType getType() { return type; }
    public void setType(BadgeType type) { 
        this.type = type;
        if (type != null) {
            this.name = type.getName();
            this.description = type.getDescription();
            this.color = type.getColor();
            this.iconResId = type.getIconResId();
        }
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
    
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    
    public Long getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(Long unlockedAt) { this.unlockedAt = unlockedAt; }
    
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    
    public int getTargetValue() { return targetValue; }
    public void setTargetValue(int targetValue) { this.targetValue = targetValue; }
    
    public int getCurrentValue() { return currentValue; }
    public void setCurrentValue(int currentValue) { this.currentValue = currentValue; }
}

