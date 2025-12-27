package com.example.rise_of_city.data.model.learning.quiz.ORDERING;

import com.example.rise_of_city.data.model.learning.quiz.BaseQuestion;

import java.util.List;

/**
 * Model cho bài tập sắp xếp từ thành câu (Word Order).
 * Người chơi chọn các từ rời rạc để xếp thành câu hoàn chỉnh.
 */
public class SentenceOrderQuestion extends BaseQuestion {

    private List<String> words;      // Danh sách các từ bị xáo trộn
    private String correctSentence;  // Câu hoàn chỉnh đúng

    public SentenceOrderQuestion(String id, String title, int order,
                                 List<String> words, String correctSentence) {
        super(id, title, order);
        this.words = words;
        this.correctSentence = correctSentence;
    }

    @Override
    public QuestionType getType() {
        return QuestionType.SENTENCEORDERING; // Thuộc nhóm sắp xếp
    }

    public List<String> getWords() { return words; }
    public String getCorrectSentence() { return correctSentence; }
}