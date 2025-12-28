package com.example.rise_of_city.data.model.learning.quiz.DECISION;

import android.content.Context;

import com.example.rise_of_city.data.model.learning.quiz.BaseQuestion;
import com.google.gson.annotations.SerializedName;

/**
 * Model cho bài tập True/False có kèm hình ảnh.
 * Người chơi nhìn hình, đọc câu miêu tả và chọn True hoặc False.
 */
public class TrueFalseQuestion extends BaseQuestion {

    private String imagePath;
    @SerializedName("statement")// Tên file ảnh trong thư mục drawable
    private String descriptionEnglish;   // Câu miêu tả bằng tiếng Anh
    @SerializedName("answer")
    private boolean isCorrect;           // Đáp án đúng
    private String explanation;          // Giải thích sau khi trả lời

    public TrueFalseQuestion(String id, String title, int order,
                             String imagePath,
                             String descriptionEnglish,
                             boolean isCorrect,
                             String explanation) {
        super(id, title, order);
        this.imagePath = imagePath;
        this.descriptionEnglish = descriptionEnglish;
        this.isCorrect = isCorrect;
        this.explanation = explanation;
    }

    @Override
    public QuestionType getType() {
        return QuestionType.DECISION;
    }

    // Getters
    public String getImagePath() { return imagePath; }
    public String getDescriptionEnglish() { return descriptionEnglish; }
    public boolean isCorrect() { return isCorrect; }
    public String getExplanation() { return explanation; }
    public int getImageResId(Context context) {
        // Xử lý nếu imagePath có đuôi file (ví dụ: "living_room.jpg" -> "living_room")
        String resourceName = imagePath;
        if (resourceName.contains(".")) {
            resourceName = resourceName.substring(0, resourceName.lastIndexOf("."));
        }
        return context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
    }
}