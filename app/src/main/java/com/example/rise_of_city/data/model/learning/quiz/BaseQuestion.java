package com.example.rise_of_city.data.model.learning.quiz;

import java.io.Serializable;

/**
 * Lớp cơ sở cho tất cả các loại câu hỏi trong hệ thống bài học.
 * Giúp thống nhất quản lý dữ liệu và đa hình hóa các Fragment.
 */
public abstract class BaseQuestion implements Serializable {

    public abstract QuestionType getType();

    // Enum thống nhất tất cả các dạng bài tập trong dự án
    public enum QuestionType {
        LECTURE,          // Đọc văn bản/Hợp đồng
        MATCHINGTEXT,         // Nối cặp (Từ-Từ, Hình-Từ)
        MATCHINGIMG,
        CHOICE,           // Trắc nghiệm (Grammar, Synonym)
        SENTENCEORDERING,
        WORDORDERING,     // Sắp xếp (Word Order, Scramble)
        INPUT,            // Nhập liệu (Writing, Guess Image)
        DECISION,         // Quyết định Đúng/Sai (Tư duy Thị trưởng)
        LISTENING         // Nghe hiểu (Dạng bài mới bổ sung)
    }

    protected String id;
    protected String title;
    protected int order; // Thứ tự xuất hiện của câu hỏi trong bài học

    public BaseQuestion(String id, String title, int order) {
        this.id = id;
        this.title = title;
        this.order = order;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}