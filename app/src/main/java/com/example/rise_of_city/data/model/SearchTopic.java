package com.example.rise_of_city.data.model;

import java.io.Serializable;

public class SearchTopic implements Serializable {
    private String id;
    private String title;
    private String description;
    private String level;
    private String category;
    private int lessonCount;
    private String imageUrl;

    public SearchTopic() {
    }

    public SearchTopic(String id, String title, String description, String level) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.level = level;
    }

    public SearchTopic(String id, String title, String description, String level, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.level = level;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getLessonCount() {
        return lessonCount;
    }

    public void setLessonCount(int lessonCount) {
        this.lessonCount = lessonCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

