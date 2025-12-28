package com.example.rise_of_city.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessageEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int senderId; // ID của người gửi
    public int receiverId; // ID của người nhận
    public String message;
    public String type; // "USER" hoặc "AI"
    public long timestamp;

    // Constructor mới với senderId và receiverId
    public ChatMessageEntity(int senderId, int receiverId, String message, String type, long timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
    }
    
    // Constructor cũ để backward compatibility (sẽ deprecated)
    @Deprecated
    public ChatMessageEntity(int userId, String message, String type, long timestamp) {
        this.senderId = userId;
        this.receiverId = userId; // Fallback
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
    }
}