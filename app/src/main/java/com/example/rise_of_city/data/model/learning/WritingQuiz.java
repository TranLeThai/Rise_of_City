package com.example.rise_of_city.data.model.learning;

/**
 * Model cho Writing Quiz (Fill in the blank)
 */
public class WritingQuiz {
    private String id;
    private String sentence; // Câu có chỗ trống, ví dụ: "I ___ to school every day."
    private String answer; // Đáp án đúng
    private String level; // "Beginner", "Intermediate", "Advanced"
    private int order;
    private String hint; // Gợi ý

    public WritingQuiz() {
        // Required for Firebase
    }

    public WritingQuiz(String id, String sentence, String answer, String level, int order, String hint) {
        this.id = id;
        this.sentence = sentence;
        this.answer = answer;
        this.level = level;
        this.order = order;
        this.hint = hint;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSentence() { return sentence; }
    public void setSentence(String sentence) { this.sentence = sentence; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }
}

