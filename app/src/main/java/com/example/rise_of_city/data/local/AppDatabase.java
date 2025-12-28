package com.example.rise_of_city.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, ChatMessageEntity.class}, version = 3) // Tăng version lên 3
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract ChatDao chatDao(); // Thêm DAO cho chat

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