package com.example.rise_of_city.data.model.learning.quiz.MATCHING;

import com.example.rise_of_city.data.model.learning.quiz.BaseQuestion;

import java.util.List;

/**
 * Model cho dạng bài MATCHINGIMG (Nối Hình ảnh - Từ vựng).
 * Người chơi sẽ nối hình ảnh ở cột trái với từ tiếng Anh ở cột phải.
 */
public class MatchingIMGQuestion extends BaseQuestion {

    private List<Integer> imageResIds; // Danh sách ID tài nguyên ảnh (R.drawable...)
    private List<String> words;        // Danh sách từ tiếng Anh tương ứng

    /**
     * Constructor khởi tạo câu hỏi nối hình.
     * @param imageResIds: Danh sách ID ảnh (Ví dụ: R.drawable.house, R.drawable.tree)
     * @param words: Danh sách từ đúng theo thứ tự ảnh (Ví dụ: "House", "Tree")
     */
    public MatchingIMGQuestion(String id, String title, int order,
                               List<Integer> imageResIds,
                               List<String> words) {
        super(id, title, order);
        this.imageResIds = imageResIds;
        this.words = words;
    }

    @Override
    public QuestionType getType() {
        return QuestionType.MATCHINGIMG; // Trả về danh tính MATCHINGIMG
    }

    // Getters
    public List<Integer> getImageResIds() { return imageResIds; }
    public List<String> getWords() { return words; }
}