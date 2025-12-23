package com.example.rise_of_city.data.model;

/**
 * Model cho Grammar Quiz Question
 */
public class GrammarQuiz {
    private String id;
    private String topicId;
    private String topicName;
    private String question;
    private java.util.List<String> options; // 4 options
    private int correctAnswer; // Index của đáp án đúng (0-3)
    private String explanation;
    private String level; // "Beginner", "Intermediate", "Advanced"
    private int order;

    public GrammarQuiz() {
        // Required for Firebase
    }

    public GrammarQuiz(String id, String topicId, String topicName, String question, 
                      java.util.List<String> options, int correctAnswer, String explanation, 
                      String level, int order) {
        this.id = id;
        this.topicId = topicId;
        this.topicName = topicName;
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.level = level;
        this.order = order;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTopicId() { return topicId; }
    public void setTopicId(String topicId) { this.topicId = topicId; }

    public String getTopicName() { return topicName; }
    public void setTopicName(String topicName) { this.topicName = topicName; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public java.util.List<String> getOptions() { return options; }
    public void setOptions(java.util.List<String> options) { this.options = options; }

    public int getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}

