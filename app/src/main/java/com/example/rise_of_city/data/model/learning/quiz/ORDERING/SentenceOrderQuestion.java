package com.example.rise_of_city.data.model.learning.quiz.ORDERING;

import com.example.rise_of_city.data.model.learning.quiz.BaseQuestion;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Model cho bài tập sắp xếp từ thành câu (Word Order).
 * Người chơi chọn các từ rời rạc để xếp thành câu hoàn chỉnh.
 */
public class SentenceOrderQuestion extends BaseQuestion {

    @SerializedName("content") // Lấy chuỗi "The family sits..." từ JSON
    private String correctSentence;

    // Biến này không có trong JSON, chúng ta sẽ tự tạo ra nó
    private List<String> words;

    public SentenceOrderQuestion(String id, String title, int order,
                                 List<String> words, String correctSentence) {
        super(id, title, order);
        this.words = words;
        this.correctSentence = correctSentence;
    }

    @Override
    public QuestionType getType() {
        return QuestionType.SENTENCEORDERING;
    }

    // --- ĐÂY LÀ PHẦN QUAN TRỌNG NHẤT ---
    public List<String> getWords() {
        // Nếu words chưa có (do JSON không có), ta tự cắt từ correctSentence ra
        if ((words == null || words.isEmpty()) && correctSentence != null) {
            // Tách câu thành mảng các từ dựa trên khoảng trắng
            String[] splitWords = correctSentence.split("\\s+");
            words = new ArrayList<>(Arrays.asList(splitWords));

            // Xáo trộn để người chơi sắp xếp
            Collections.shuffle(words);
        }
        return words;
    }

    public String getCorrectSentence() {
        return correctSentence;
    }
}