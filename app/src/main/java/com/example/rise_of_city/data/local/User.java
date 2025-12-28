package com.example.rise_of_city.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String fullName;
    public String email;
    public String password;
    public String phone;
    public String address;
    public boolean surveyCompleted = false;
    public int gold = 0;
    public int xp = 0;
    
    // Thêm các trường cho tính năng điểm danh (Streak)
    public long lastLoginTime = 0; // Thời gian đăng nhập gần nhất (timestamp)
    public int streakDays = 0;     // Số ngày liên tiếp
}