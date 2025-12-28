package com.example.rise_of_city.data.model.learning.quiz.MATCHING;

import android.content.Context;
import com.example.rise_of_city.data.model.learning.quiz.BaseQuestion;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class MatchingIMGQuestion extends BaseQuestion {

    @SerializedName("images") // Ánh xạ từ "images" trong JSON
    private List<String> imageNames;

    @SerializedName("answers") // Ánh xạ từ "answers" trong JSON
    private List<String> words;

    public MatchingIMGQuestion(String id, String title, int order,
                               List<String> imageNames,
                               List<String> words) {
        super(id, title, order);
        this.imageNames = imageNames;
        this.words = words;
    }

    @Override
    public QuestionType getType() {
        return QuestionType.MATCHINGIMG;
    }

    public List<String> getImageNames() { return imageNames; }
    public List<String> getWords() { return words; }

    /**
     * Hàm quan trọng: Chuyển danh sách tên file (String) thành ID (Integer) để dùng cho ImageView
     */
    public List<Integer> getImageResIds(Context context) {
        List<Integer> resIds = new ArrayList<>();
        for (String name : imageNames) {
            // Loại bỏ đuôi .jpg nếu có
            String cleanName = name.contains(".") ? name.substring(0, name.lastIndexOf(".")) : name;
            int id = context.getResources().getIdentifier(cleanName.trim(), "drawable", context.getPackageName());
            resIds.add(id);
        }
        return resIds;
    }
}