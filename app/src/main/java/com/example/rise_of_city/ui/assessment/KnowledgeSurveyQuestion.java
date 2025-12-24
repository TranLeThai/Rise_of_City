package com.example.rise_of_city.ui.assessment;

import java.util.List;

public class KnowledgeSurveyQuestion {
    public enum QuestionType { EDUCATION_LEVEL, FIND_ERROR, MULTIPLE_CHOICE }

    public String title;
    public String paragraph;
    public List<String> wrongWords;
    public List<String> options; // Thêm danh sách lựa chọn cho ABCD
    public String correctAnswers; // Đáp án đúng cho Multiple Choice
    public int imageRes;
    public QuestionType type;

    // Constructor cập nhật
    public KnowledgeSurveyQuestion(String title, String paragraph, List<String> wrongWords, List<String> options, String correctAnswers, int imageRes, QuestionType type) {
        this.title = title;
        this.paragraph = paragraph;
        this.wrongWords = wrongWords;
        this.options = options;
        this.correctAnswers = correctAnswers;
        this.imageRes = imageRes;
        this.type = type;
    }
}
