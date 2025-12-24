package com.example.rise_of_city.data.model.user;

public class SearchUser {
    private String uid;
    private String name;
    private String email;
    private String surveyLevel;
    private String avatarUrl;
    private boolean isFriend;

    public SearchUser() {
    }

    public SearchUser(String uid, String name, String email, String surveyLevel) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.surveyLevel = surveyLevel;
        this.isFriend = false;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSurveyLevel() {
        return surveyLevel;
    }

    public void setSurveyLevel(String surveyLevel) {
        this.surveyLevel = surveyLevel;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }
}

