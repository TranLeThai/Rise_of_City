package com.example.rise_of_city.data.model.learning;

import java.io.Serializable;

/**
 * Model đại diện cho một từ vựng
 */
public class Vocabulary implements Serializable {
    private String english;
    private String vietnamese;
    private String imageUrl;
    private String imageFilename;
    private boolean hasImage;
    private String buildingId; // ID của building (house, school, library, etc.)

    // Constructor mặc định (cần cho Firebase)
    public Vocabulary() {
    }

    public Vocabulary(String english, String vietnamese, String imageUrl) {
        this.english = english;
        this.vietnamese = vietnamese;
        this.imageUrl = imageUrl;
        this.hasImage = imageUrl != null && !imageUrl.isEmpty();
    }

    // Getters and Setters
    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public String getVietnamese() {
        return vietnamese;
    }

    public void setVietnamese(String vietnamese) {
        this.vietnamese = vietnamese;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        this.hasImage = imageUrl != null && !imageUrl.isEmpty();
    }

    public String getImageFilename() {
        return imageFilename;
    }

    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }

    public boolean isHasImage() {
        return hasImage;
    }

    public void setHasImage(boolean hasImage) {
        this.hasImage = hasImage;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }
}

