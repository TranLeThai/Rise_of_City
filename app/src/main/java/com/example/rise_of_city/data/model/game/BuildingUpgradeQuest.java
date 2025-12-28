package com.example.rise_of_city.data.model.game;

import java.io.Serializable;

/**
 * Quest để nâng cấp building
 * Quest này yêu cầu người chơi hoàn thành lesson trước khi có thể upgrade building
 */
public class BuildingUpgradeQuest implements Serializable {
    public String buildingId;
    public String buildingName;
    public String requiredLessonName;
    public int targetLevel; // Level muốn nâng cấp lên (ví dụ: từ level 1 lên 2)
    public boolean lessonCompleted; // Đã hoàn thành lesson chưa
    public boolean questCompleted; // Đã hoàn thành quest (upgrade xong) chưa
    
    public BuildingUpgradeQuest(String buildingId, String buildingName, String requiredLessonName, int targetLevel) {
        this.buildingId = buildingId;
        this.buildingName = buildingName;
        this.requiredLessonName = requiredLessonName;
        this.targetLevel = targetLevel;
        this.lessonCompleted = false;
        this.questCompleted = false;
    }
    
    /**
     * Kiểm tra điều kiện để upgrade building
     * @return true nếu đã hoàn thành lesson và chưa upgrade xong
     */
    public boolean canUpgrade() {
        return lessonCompleted && !questCompleted;
    }
    
    /**
     * Lấy message hiển thị trạng thái quest
     */
    public String getStatusMessage() {
        if (questCompleted) {
            return "Đã nâng cấp " + buildingName + " lên level " + targetLevel;
        } else if (lessonCompleted) {
            return "Đã hoàn thành bài học. Có thể nâng cấp " + buildingName;
        } else {
            return "Chưa hoàn thành bài học: " + requiredLessonName;
        }
    }
}

