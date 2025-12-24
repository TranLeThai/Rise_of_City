package com.example.rise_of_city.data.model.game;

import java.io.Serializable;

/**
 * Model đại diện cho tiến độ của một building
 */
public class BuildingProgress implements Serializable {
    private String buildingId;
    private String buildingName;
    private int level;
    private int currentExp;
    private int maxExp;
    private boolean completed;
    private boolean locked;
    private int vocabularyCount; // Tổng số từ vựng của building
    private int vocabularyLearned; // Số từ vựng đã học
    
    public BuildingProgress() {
    }
    
    public BuildingProgress(String buildingId, String buildingName, int level, 
                           int currentExp, int maxExp, boolean completed, boolean locked) {
        this.buildingId = buildingId;
        this.buildingName = buildingName;
        this.level = level;
        this.currentExp = currentExp;
        this.maxExp = maxExp;
        this.completed = completed;
        this.locked = locked;
    }
    
    // Getters and Setters
    public String getBuildingId() {
        return buildingId;
    }
    
    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }
    
    public String getBuildingName() {
        return buildingName;
    }
    
    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public int getCurrentExp() {
        return currentExp;
    }
    
    public void setCurrentExp(int currentExp) {
        this.currentExp = currentExp;
    }
    
    public int getMaxExp() {
        return maxExp;
    }
    
    public void setMaxExp(int maxExp) {
        this.maxExp = maxExp;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    public boolean isLocked() {
        return locked;
    }
    
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    
    public int getVocabularyCount() {
        return vocabularyCount;
    }
    
    public void setVocabularyCount(int vocabularyCount) {
        this.vocabularyCount = vocabularyCount;
    }
    
    public int getVocabularyLearned() {
        return vocabularyLearned;
    }
    
    public void setVocabularyLearned(int vocabularyLearned) {
        this.vocabularyLearned = vocabularyLearned;
    }
    
    /**
     * Tính phần trăm hoàn thành EXP
     */
    public int getExpProgressPercent() {
        if (maxExp == 0) return 0;
        return (int) ((currentExp * 100) / maxExp);
    }
    
    /**
     * Tính phần trăm hoàn thành từ vựng
     */
    public int getVocabularyProgressPercent() {
        if (vocabularyCount == 0) return 0;
        return (int) ((vocabularyLearned * 100) / vocabularyCount);
    }
    
    /**
     * Kiểm tra building có available không (không locked, chưa completed)
     */
    public boolean isAvailable() {
        return !locked && !completed;
    }
}

