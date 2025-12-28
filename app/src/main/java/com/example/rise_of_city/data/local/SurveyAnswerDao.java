package com.example.rise_of_city.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SurveyAnswerDao {
    @Insert
    void insertSurveyAnswer(SurveyAnswer answer);

    @Query("SELECT * FROM survey_answers WHERE userId = :userId ORDER BY questionIndex ASC")
    List<SurveyAnswer> getSurveyAnswersByUserId(int userId);

    @Query("DELETE FROM survey_answers WHERE userId = :userId")
    void deleteSurveyAnswersByUserId(int userId);
}

