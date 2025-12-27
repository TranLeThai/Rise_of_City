package com.example.rise_of_city.data.model.learning.quiz;

import java.util.List;

/**
 * Model cho dạng bài MATCHINGTEXT (Nối từ Tiếng Anh - Tiếng Việt).
 * Dữ liệu bao gồm 2 danh sách đối ứng nhau.
 */
public class MatchingTextQuestion extends BaseQuestion {

    private List<String> englishWords;    // Danh sách 4 từ tiếng Anh (Cột trái)
    private List<String> vietnameseWords; // Danh sách 4 từ tiếng Việt (Cột phải)

    /**
     * Constructor khởi tạo câu hỏi nối từ.
     * Lưu ý: Trong file Excel, bạn nên để cặp từ đúng nằm cùng vị trí index.
     * Ví dụ: englishWords.get(0) là nghĩa của vietnameseWords.get(0).
     * Việc xáo trộn (shuffle) sẽ được xử lý ở tầng Fragment để hiển thị.
     */
    public MatchingTextQuestion(String id, String title, int order,
                                List<String> englishWords,
                                List<String> vietnameseWords) {
        super(id, title, order);
        this.englishWords = englishWords;
        this.vietnameseWords = vietnameseWords;
    }

    @Override
    public QuestionType getType() {
        return QuestionType.MATCHINGTEXT; // Trả về danh tính MATCHINGTEXT
    }

    // Getters
    public List<String> getEnglishWords() {
        return englishWords;
    }

    public List<String> getVietnameseWords() {
        return vietnameseWords;
    }
}