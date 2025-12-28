package com.example.rise_of_city.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserBuildingDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<UserBuilding> buildings);

    @Update
    void updateBuilding(UserBuilding building);

    @Query("SELECT * FROM user_buildings WHERE userId = :userId")
    List<UserBuilding> getBuildingsForUser(int userId);

    @Query("SELECT * FROM user_buildings WHERE userId = :userId AND buildingId = :buildingId LIMIT 1")
    UserBuilding getBuilding(int userId, String buildingId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(UserBuilding building);
}
