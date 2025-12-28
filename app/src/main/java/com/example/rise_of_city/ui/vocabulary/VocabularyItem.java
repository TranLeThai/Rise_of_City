package com.example.rise_of_city.ui.vocabulary;

public class VocabularyItem {
    private String english;
    private String vietnamese;
    private String imageResourceName;
    private boolean hasImage;
    
    public VocabularyItem(String english, String vietnamese, String imageResourceName, boolean hasImage) {
        this.english = english;
        this.vietnamese = vietnamese;
        this.imageResourceName = imageResourceName;
        this.hasImage = hasImage;
    }
    
    public String getEnglish() {
        return english;
    }
    
    public String getVietnamese() {
        return vietnamese;
    }
    
    public String getImageResourceName() {
        return imageResourceName;
    }
    
    public boolean hasImage() {
        return hasImage;
    }
}

