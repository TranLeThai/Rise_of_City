package com.example.rise_of_city.data.model.learning.quiz.ORDERING;

import com.example.rise_of_city.data.model.learning.quiz.BaseQuestion;

/**
 * Model cho bài tập sắp xếp chữ cái (Scrambled Letters).
 * Ví dụ: E/n/g/l/i/s/h -> English.
 */
public class WordOrderQuestion extends BaseQuestion {

    private String scrambledLetters; // Chuỗi chữ cái xáo trộn (ví dụ: "E/n/g/l/i/s/h")
    private String correctWord;      // Từ hoàn chỉnh đúng (ví dụ: "English")

    public WordOrderQuestion(String id, String title, int order,
                             String scrambledLetters, String correctWord) {
        super(id, title, order);
        this.scrambledLetters = scrambledLetters;
        this.correctWord = correctWord;
    }

    @Override
    public QuestionType getType() {
        return QuestionType.WORDORDERING; // Thuộc nhóm sắp xếp
    }

    public String getScrambledLetters() { return scrambledLetters; }
    public String getCorrectWord() { return correctWord; }
}