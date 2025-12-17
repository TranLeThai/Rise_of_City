package com.example.rise_of_city.data.model;

import java.util.List;

/**
 * Model cho Sentence Completion Quiz
 */
public class SentenceCompletionQuiz {
    private String id;
    private String sentence; // Câu có chỗ trống, ví dụ: "I'm tired ___ I worked hard."
    private List<String> options; // 4 options
    private int correctAnswer; // Index của đáp án đúng (0-3)
    private String level; // "Beginner", "Intermediate", "Advanced"
    private int order;
    private String explanation; // Giải thích

    public SentenceCompletionQuiz() {
        // Required for Firebase
    }

    public SentenceCompletionQuiz(String id, String sentence, List<String> options, int correctAnswer, String level, int order, String explanation) {
        this.id = id;
        this.sentence = sentence;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.level = level;
        this.order = order;
        this.explanation = explanation;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSentence() { return sentence; }
    public void setSentence(String sentence) { this.sentence = sentence; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public int getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}

