package com.example.rise_of_city.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Chắc chắn version là 4 và có UserBuilding.class
@Database(entities = {User.class, ChatMessageEntity.class, UserBuilding.class, SurveyAnswer.class, Friend.class, Vocabulary.class}, version = 8, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract ChatDao chatDao(); 
    public abstract UserBuildingDao userBuildingDao(); // Đảm bảo phương thức này tồn tại
    public abstract SurveyAnswerDao surveyAnswerDao();
    public abstract FriendDao friendDao();
    public abstract VocabularyDao vocabularyDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "rise_of_city_db")
                    // Xóa và tạo lại database nếu schema thay đổi
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}