package com.example.rise_of_city.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "user_buildings", primaryKeys = {"userId", "buildingId"})
public class UserBuilding {
    @NonNull
    public int userId;

    @NonNull
    public String buildingId; // e.g., "school", "house"

    public int level; // Current level of the building, default 0

    public UserBuilding(int userId, @NonNull String buildingId, int level) {
        this.userId = userId;
        this.buildingId = buildingId;
        this.level = level;
    }
}
