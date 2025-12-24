package com.example.rise_of_city.data.model.learning.LessionQuiz;// LessonQuestion.java


import java.io.Serializable;
import java.util.List;

public class LessonQuestion implements Serializable {
    public enum Type {
        LECTURE,      // Màn hình 1: Đọc và tìm lỗi trong hợp đồng
        WORD_MATCH,   // Màn hình 2: Ghép từ Tiếng Anh - Tiếng Việt
        IMAGE_MATCH,  // Màn hình 3: Ghép hình ảnh với từ tương ứng
        GUESS,        // Màn hình 4: Sắp xếp chữ cái thành từ đúng
        GUESS_IMAGE   // Màn hình 5: Nhìn hình chọn từ đúng
    }

    public Type type;
    public String title;
    public String content;           // Văn bản cho LECTURE hoặc từ gốc cho GUESS
    public List<String> options;      // Danh sách từ/lựa chọn
    public List<String> optionsMatch; // Danh sách từ đối ứng cho WORD_MATCH
    public String correctAnswer;      // Đáp án đúng
    public int imageRes;              // Ảnh đơn cho GUESS_IMAGE
    public List<Integer> imageResources; // Danh sách ảnh cho IMAGE_MATCH

    // 1. Constructor cho LECTURE (Tìm lỗi trong văn bản)
    public static LessonQuestion createLecture(String title, String paragraph, List<String> wrongWords) {
        LessonQuestion q = new LessonQuestion();
        q.type = Type.LECTURE;
        q.title = title;
        q.content = paragraph;
        q.options = wrongWords; // Ở mode này, options đóng vai trò là danh sách từ sai
        return q;
    }

    // 2. Constructor cho WORD_MATCH (Ghép cặp từ - từ)
    public static LessonQuestion createWordMatch(String title, List<String> engList, List<String> vieList) {
        LessonQuestion q = new LessonQuestion();
        q.type = Type.WORD_MATCH;
        q.title = title;
        q.options = engList;
        q.optionsMatch = vieList;
        return q;
    }

    // 3. Constructor cho IMAGE_MATCH (Ghép cặp hình - từ)
    public static LessonQuestion createImageMatch(String title, List<Integer> images, List<String> words) {
        LessonQuestion q = new LessonQuestion();
        q.type = Type.IMAGE_MATCH;
        q.title = title;
        q.imageResources = images;
        q.options = words;
        return q;
    }

    // 4. Constructor cho GUESS (Sắp xếp chữ cái)
    public static LessonQuestion createGuess(String title, String scrambledText, String answer) {
        LessonQuestion q = new LessonQuestion();
        q.type = Type.GUESS;
        q.title = title;
        q.content = scrambledText; // Chuỗi hiển thị (ví dụ: n/g/E/h/l/i/s)
        q.correctAnswer = answer;   // Đáp án đúng (ví dụ: English)
        return q;
    }

    // 5. Constructor cho GUESS_IMAGE (Nhìn hình chọn đáp án)
    public static LessonQuestion createGuessImage(String title, int image, List<String> answers, String correct) {
        LessonQuestion q = new LessonQuestion();
        q.type = Type.GUESS_IMAGE;
        q.title = title;
        q.imageRes = image;
        q.options = answers;
        q.correctAnswer = correct;
        return q;
    }

    // Constructor mặc định riêng tư để buộc dùng các hàm static phía trên
    private LessonQuestion() {}
}