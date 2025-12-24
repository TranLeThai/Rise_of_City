package com.example.rise_of_city.data.model.game;

import java.io.Serializable;
import java.util.UUID;

public class Mission implements Serializable {
    public enum Type { DAILY, RANDOM }

    public String id;
    public String title;
    public String buildingId;
    public long startTime;
    public long durationMs = 12 * 60 * 60 * 1000; // 12 tiếng
    public int goldPenalty = 50; // Phạt 50 vàng nếu quá hạn
    public int goldReward = 100;
    public Type type;
    public boolean isCompleted = false;

    // Sửa constructor để tự tạo ID ngẫu nhiên, giải quyết lỗi tham số
    public Mission(String title, String buildingId, Type type) {
        this.id = java.util.UUID.randomUUID().toString(); // Tự tạo ID thay vì truyền vào
        this.title = title;
        this.buildingId = buildingId;
        this.type = type;
        this.startTime = System.currentTimeMillis();
    }
}