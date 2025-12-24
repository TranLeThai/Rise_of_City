package com.example.rise_of_city.data.model.learning;

import java.util.List;

/**
 * Model cho Word Order Quiz (Sắp xếp từ thành câu)
 */
public class WordOrderQuiz {
    private String id;
    private List<String> words; // Danh sách các từ cần sắp xếp
    private List<Integer> correctOrder; // Thứ tự đúng của các từ (indices)
    private String level; // "Beginner", "Intermediate", "Advanced"
    private int order;
    private String explanation; // Giải thích

    public WordOrderQuiz() {
        // Required for Firebase
    }

    public WordOrderQuiz(String id, List<String> words, List<Integer> correctOrder, String level, int order, String explanation) {
        this.id = id;
        this.words = words;
        this.correctOrder = correctOrder;
        this.level = level;
        this.order = order;
        this.explanation = explanation;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<String> getWords() { return words; }
    public void setWords(List<String> words) { this.words = words; }

    public List<Integer> getCorrectOrder() { return correctOrder; }
    public void setCorrectOrder(List<Integer> correctOrder) { this.correctOrder = correctOrder; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}

