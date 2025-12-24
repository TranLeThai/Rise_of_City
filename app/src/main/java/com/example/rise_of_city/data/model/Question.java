package com.example.rise_of_city.data.model;

// Xóa import của KnowledgeSurveyQuestion
// Import đúng enum Type của phần bài học/game
import com.example.rise_of_city.data.model.learning.LessionQuiz.LessonQuestion.Type;

public class Question {
    private int id;
    private Type type; // Đổi từ QuestionType sang Type
    private String data;

    // Constructor cập nhật tham số Type
    public Question(int id, Type type, String data) {
        this.id = id;
        this.type = type;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    // Getter trả về kiểu Type của Lesson
    public Type getType() {
        return type;
    }

    public String getData() {
        return data;
    }
}