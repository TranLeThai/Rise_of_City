package com.example.rise_of_city.data.model.learning.quiz.CHOICE;

import com.example.rise_of_city.data.model.learning.quiz.BaseQuestion;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model hợp nhất cho tất cả các dạng bài tập trắc nghiệm (4 lựa chọn).
 * Bao gồm: Ngữ pháp, Điền từ vào câu, Từ đồng nghĩa/Trái nghĩa.
 */
public class ChoiceQuestion extends BaseQuestion {

    // Enum phụ để phân loại nội dung bên trong ChoiceQuestion
    public enum ChoiceType {
        GRAMMAR,        // Ngữ pháp
        COMPLETION,     // Hoàn thành câu
        SYNONYM,        // Từ đồng nghĩa
        ANTONYM         // Từ trái nghĩa
    }
    @SerializedName("subType")
    private ChoiceType subType;
    @SerializedName("question")// Loại trắc nghiệm cụ thể
    private String questionContent;  // Nội dung câu hỏi hoặc câu chứa chỗ trống
    private List<String> options;    // Danh sách 4 lựa chọn
    private int correctAnswerIndex;  // Vị trí đáp án đúng (0-3)
    private String explanation;      // Giải thích đáp án

    public ChoiceQuestion(String id, String title, int order,
                          ChoiceType subType,
                          String questionContent,
                          List<String> options,
                          int correctAnswerIndex,
                          String explanation) {
        super(id, title, order);
        this.subType = subType;
        this.questionContent = questionContent;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.explanation = explanation;
    }

    @Override
    public QuestionType getType() {
        // Trả về loại CHOICE chung cho tất cả
        return QuestionType.CHOICE;
    }

    // Getters
    public ChoiceType getSubType() { return subType; }
    public String getQuestionContent() { return questionContent; }
    public List<String> getOptions() { return options; }
    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
    public String getExplanation() { return explanation; }
}