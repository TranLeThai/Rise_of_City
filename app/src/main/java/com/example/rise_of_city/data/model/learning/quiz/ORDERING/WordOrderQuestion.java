package com.example.rise_of_city.data.model.learning.quiz.ORDERING;

import com.example.rise_of_city.data.model.learning.quiz.BaseQuestion;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WordOrderQuestion extends BaseQuestion {

    // Ánh xạ key "content" từ JSON (ví dụ: "kitchen") vào correctWord
    @SerializedName("content")
    private String correctWord;

    // Biến này không có trong JSON, chúng ta sẽ tự tạo ra
    private String scrambledLetters;

    public WordOrderQuestion(String id, String title, int order,
                             String scrambledLetters, String correctWord) {
        super(id, title, order);
        this.scrambledLetters = scrambledLetters;
        this.correctWord = correctWord;
    }

    @Override
    public QuestionType getType() {
        return QuestionType.WORDORDERING;
    }

    /**
     * Tự động tạo chuỗi xáo trộn nếu JSON không cung cấp.
     * Ví dụ: từ "apple" thành "p/e/a/l/p"
     */
    public String getScrambledLetters() {
        if ((scrambledLetters == null || scrambledLetters.isEmpty()) && correctWord != null) {
            // 1. Tách từ thành các chữ cái
            List<String> letters = new ArrayList<>(Arrays.asList(correctWord.split("")));

            // 2. Xáo trộn ngẫu nhiên
            Collections.shuffle(letters);

            // 3. Nối lại bằng dấu "/"
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < letters.size(); i++) {
                sb.append(letters.get(i));
                if (i < letters.size() - 1) sb.append("/");
            }
            scrambledLetters = sb.toString();
        }
        return scrambledLetters;
    }

    public String getCorrectWord() {
        return correctWord;
    }
}