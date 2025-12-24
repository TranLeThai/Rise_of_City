// Lesson.java
package com.example.rise_of_city.data.model.learning;

import java.io.Serializable;

/**
 * Model cho Lesson (bài học do người dùng chia sẻ)
 */
public class Lesson implements Serializable {
    private String id;
    private String topicId; // ID của topic chứa lesson này
    private String title;
    private String content; // Nội dung bài học (HTML hoặc markdown)
    private String authorId; // ID người dùng tạo lesson
    private String authorName; // Tên người dùng
    private String authorAvatarUrl; // Avatar người dùng
    private long createdAt; // Timestamp khi tạo
    private long updatedAt; // Timestamp khi cập nhật
    private String status; // "pending", "approved", "rejected"
    private int viewCount; // Số lượt xem
    private int likeCount; // Số lượt thích
    private int commentCount; // Số bình luận
    private String level; // "Beginner", "Intermediate", "Advanced"
    private String imageUrl; // Ảnh minh họa (nếu có)
    private java.util.List<String> tags; // Tags để tìm kiếm

    public Lesson() {
        // Required no-argument constructor for Firebase
    }

    public Lesson(String id, String topicId, String title, String content, String authorId, String authorName) {
        this.id = id;
        this.topicId = topicId;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.status = "pending"; // Mặc định là pending, chờ admin approve
        this.viewCount = 0;
        this.likeCount = 0;
        this.commentCount = 0;
        this.level = "Beginner";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTopicId() { return topicId; }
    public void setTopicId(String topicId) { this.topicId = topicId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorAvatarUrl() { return authorAvatarUrl; }
    public void setAuthorAvatarUrl(String authorAvatarUrl) { this.authorAvatarUrl = authorAvatarUrl; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public java.util.List<String> getTags() { return tags; }
    public void setTags(java.util.List<String> tags) { this.tags = tags; }

    // Helper methods
    public boolean isApproved() {
        return "approved".equals(status);
    }

    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isRejected() {
        return "rejected".equals(status);
    }
}