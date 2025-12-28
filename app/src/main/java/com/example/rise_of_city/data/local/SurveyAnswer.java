package com.example.rise_of_city.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "survey_answers")
public class SurveyAnswer implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public int questionIndex;
    public String questionTitle;
    public String questionType; // "EDUCATION_LEVEL", "FIND_ERROR", "MULTIPLE_CHOICE"
    public String userAnswer; // JSON string for selected errors or single answer
    public String correctAnswer; // JSON string for correct answers
    public boolean isCorrect;
    public long timestamp;

    public SurveyAnswer(int userId, int questionIndex, String questionTitle, String questionType, 
                       String userAnswer, String correctAnswer, boolean isCorrect, long timestamp) {
        this.userId = userId;
        this.questionIndex = questionIndex;
        this.questionTitle = questionTitle;
        this.questionType = questionType;
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
        this.isCorrect = isCorrect;
        this.timestamp = timestamp;
    }
}

