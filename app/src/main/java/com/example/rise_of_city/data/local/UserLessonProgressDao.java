// UserLessonProgressDao.java
package com.example.rise_of_city.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserLessonProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(UserLessonProgress progress);

    @Query("SELECT * FROM user_lesson_progress WHERE userId = :userId AND lessonName = :lessonName LIMIT 1")
    UserLessonProgress getProgress(int userId, String lessonName);

    @Query("SELECT * FROM user_lesson_progress WHERE userId = :userId")
    List<UserLessonProgress> getAllProgress(int userId);
}