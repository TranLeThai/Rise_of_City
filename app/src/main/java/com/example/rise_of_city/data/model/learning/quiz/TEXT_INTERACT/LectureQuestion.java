package com.example.rise_of_city.data.model.learning.quiz.TEXT_INTERACT;
import com.example.rise_of_city.data.model.learning.quiz.BaseQuestion;

import java.io.Serializable;
import java.util.List;

/**
 * Model cho dạng bài LECTURE (Đọc văn bản và sửa lỗi).
 * Phù hợp cho các màn hình "Thanh tra hợp đồng" hoặc "Sửa văn bản".
 */
public class LectureQuestion extends BaseQuestion {

    private String contentEnglish;     // Đoạn văn tiếng Anh chứa 2 từ sai
    private String contentVietnamese;  // Đoạn văn tiếng Việt hoàn chỉnh (Dùng cho nút Translate)
    private List<WrongWordInfo> wrongWordList; // Danh sách thông tin về 2 lỗi sai

    public LectureQuestion(String id, String title, int order,
                           String contentEnglish,
                           String contentVietnamese,
                           List<WrongWordInfo> wrongWordList) {
        super(id, title, order);
        this.contentEnglish = contentEnglish;
        this.contentVietnamese = contentVietnamese;
        this.wrongWordList = wrongWordList;
    }

    @Override
    public QuestionType getType() {
        return QuestionType.LECTURE; // Luôn trả về loại LECTURE
    }

    // Inner class để chứa chi tiết về từng lỗi sai
    public static class WrongWordInfo implements Serializable {
        private String originalWrongWord; // Từ đang bị viết sai trong đoạn văn
        private List<String> options;     // 3 lựa chọn để thay thế (1 đúng, 2 sai)
        private String correctAnswer;     // Đáp án đúng thực tế

        public WrongWordInfo(String originalWrongWord, List<String> options, String correctAnswer) {
            this.originalWrongWord = originalWrongWord;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }

        // Getters
        public String getOriginalWrongWord() { return originalWrongWord; }
        public List<String> getOptions() { return options; }
        public String getCorrectAnswer() { return correctAnswer; }
    }

    // Getters cho LectureQuestion
    public String getContentEnglish() { return contentEnglish; }
    public String getContentVietnamese() { return contentVietnamese; }
    public List<WrongWordInfo> getWrongWordList() { return wrongWordList; }
}