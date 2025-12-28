package com.example.rise_of_city.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, ChatMessageEntity.class, UserBuilding.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract ChatDao chatDao(); 
    public abstract UserBuildingDao userBuildingDao(); // Thêm DAO cho UserBuilding

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "rise_of_city_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}