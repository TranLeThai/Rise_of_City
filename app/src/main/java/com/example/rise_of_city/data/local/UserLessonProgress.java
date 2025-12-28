// UserLessonProgress.java
package com.example.rise_of_city.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "user_lesson_progress", primaryKeys = {"userId", "lessonName"})
public class UserLessonProgress {
    @NonNull
    public int userId;
    @NonNull
    public String lessonName;// ví dụ "House_lv1", "School_lv2"
    public int attemptsToday = 0;    // số lần thử hôm nay
    public long lastAttemptDate = 0; // timestamp ngày cuối cùng thử (để reset hàng ngày)
    public boolean completed = false; // đã hoàn thành bài học này chưa
}