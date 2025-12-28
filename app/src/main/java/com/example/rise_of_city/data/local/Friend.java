package com.example.rise_of_city.data.local;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "friends")
public class Friend {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public int userId;           // ID người dùng hiện tại
    public int friendId;         // ID người bạn
    public String status;        // "pending", "accepted", "rejected"
    public int requesterId;      // ID người gửi lời mời
    public long createdAt;       // Thời gian tạo
    public long updatedAt;       // Thời gian cập nhật
    
    public Friend() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
    
    @Ignore
    public Friend(int userId, int friendId, String status, int requesterId) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
        this.requesterId = requesterId;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}

