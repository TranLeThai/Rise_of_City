package com.example.rise_of_city.data.model.learning.quiz.INPUT;

import com.example.rise_of_city.data.model.learning.quiz.BaseQuestion;

/**
 * Model cho bài tập Viết (Writing/Fill in the blank).
 * Người chơi tự nhập câu trả lời vào ô trống.
 */
public class WritingQuestion extends BaseQuestion {

    private String sentence;      // Câu hỏi hoặc câu có chỗ trống (Ví dụ: "I ___ to school.")
    private String correctAnswer; // Đáp án đúng duy nhất
    private String hint;          // Gợi ý cho người chơi khi gặp khó khăn

    public WritingQuestion(String id, String title, int order,
                           String sentence, String correctAnswer, String hint) {
        super(id, title, order);
        this.sentence = sentence;
        this.correctAnswer = correctAnswer;
        this.hint = hint;
    }

    @Override
    public QuestionType getType() {
        // Thuộc nhóm INPUT (Nhập liệu)
        return QuestionType.INPUT;
    }

    // Getters
    public String getSentence() { return sentence; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getHint() { return hint; }
}