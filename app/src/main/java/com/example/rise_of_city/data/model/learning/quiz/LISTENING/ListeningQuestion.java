package com.example.rise_of_city.data.model.learning.quiz.LISTENING;

import com.example.rise_of_city.data.model.learning.quiz.BaseQuestion;

import java.util.List;

/**
 * Model cho bài tập Nghe (Listening Quiz).
 * Người chơi nghe âm thanh và chọn đáp án đúng từ 4 lựa chọn.
 */
public class ListeningQuestion extends BaseQuestion {

    private String audioPath;        // Tên file âm thanh trong thư mục raw/assets
    private String content;          // Nội dung đề của đoạn âm thanh
    private List<String> options;    // 4 lựa chọn (có thể là văn bản hoặc tên file ảnh)
    private int correctAnswerIndex;  // Vị trí đáp án đúng (0-3)
    private String transcript;       // Nội dung văn bản của đoạn âm thanh (để hiển thị sau khi trả lời)

    public ListeningQuestion(String id, String title, int order,
                             String audioPath, String content, List<String> options,
                             int correctAnswerIndex, String transcript) {
        super(id, title, order);
        this.audioPath = audioPath;
        this.content = content; // Thêm dòng này
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
        this.transcript = transcript;
    }

    @Override
    public QuestionType getType() {
        // Trả về loại LISTENING chuyên biệt
        return QuestionType.LISTENING;
    }

    // Getters
    public String getAudioPath() { return audioPath; }
    public String getContent() { return content;}
    public List<String> getOptions() { return options; }
    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
    public String getTranscript() { return transcript; }

}