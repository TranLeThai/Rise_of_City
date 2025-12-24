package com.example.rise_of_city.data.model.learning;

import java.util.List;

/**
 * Model cho Synonym/Antonym Quiz
 */
public class SynonymAntonymQuiz {
    private String id;
    private String word; // Từ gốc
    private String question; // Câu hỏi
    private List<String> options; // 4 options
    private int correctAnswer; // Index của đáp án đúng (0-3)
    private String type; // "synonym" hoặc "antonym"
    private String level; // "Beginner", "Intermediate", "Advanced"
    private int order;
    private String explanation; // Giải thích

    public SynonymAntonymQuiz() {
        // Required for Firebase
    }

    public SynonymAntonymQuiz(String id, String word, String question, List<String> options, int correctAnswer, String type, String level, int order, String explanation) {
        this.id = id;
        this.word = word;
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.type = type;
        this.level = level;
        this.order = order;
        this.explanation = explanation;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public int getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}

