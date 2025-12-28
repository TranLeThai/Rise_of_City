package com.example.rise_of_city.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessageEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId; // ID của người dùng (liên kết với bảng users)
    public String message;
    public String type; // "USER" hoặc "AI"
    public long timestamp;

    public ChatMessageEntity(int userId, String message, String type, long timestamp) {
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
    }
}