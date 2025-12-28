package com.example.rise_of_city.data.model.learning.quiz.MATCHING;

import com.example.rise_of_city.data.model.learning.quiz.BaseQuestion;
import com.google.gson.annotations.SerializedName; // Thêm dòng này
import java.util.List;

public class MatchingTextQuestion extends BaseQuestion {

    @SerializedName("leftSide") // Ánh xạ từ "leftSide" trong JSON
    private List<String> englishWords;

    @SerializedName("rightSide") // Ánh xạ từ "rightSide" trong JSON
    private List<String> vietnameseWords;

    public MatchingTextQuestion(String id, String title, int order,
                                List<String> englishWords,
                                List<String> vietnameseWords) {
        super(id, title, order);
        this.englishWords = englishWords;
        this.vietnameseWords = vietnameseWords;
    }

    @Override
    public QuestionType getType() {
        return QuestionType.MATCHINGTEXT;
    }

    // Getters
    public List<String> getEnglishWords() { return englishWords; }
    public List<String> getVietnameseWords() { return vietnameseWords; }
}