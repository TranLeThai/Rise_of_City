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
    public boolean surveyCompleted = false;
    public int gold = 0;
    public int xp = 0;
}