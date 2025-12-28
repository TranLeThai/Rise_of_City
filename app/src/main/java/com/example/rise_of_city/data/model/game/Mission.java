package com.example.rise_of_city.data.model.game;

import java.io.Serializable;
import java.util.UUID;

public class Mission implements Serializable {
    public enum Type { DAILY, EMERGENCY }

    public String id;
    public String title;
    public String buildingId; // Chỉ dùng cho EMERGENCY
    public long startTime;
    public long durationMs = 12 * 60 * 60 * 1000; // 12 tiếng cho EMERGENCY
    public int goldPenalty = 50; // Phạt cho EMERGENCY
    public int goldReward = 100;
    public Type type;
    public boolean isCompleted = false;

    public Mission(String title, String buildingId, Type type) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.buildingId = buildingId;
        this.type = type;
        this.startTime = System.currentTimeMillis();
    }
}