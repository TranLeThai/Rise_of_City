package com.example.rise_of_city.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.rise_of_city.data.model.learning.quiz.BaseQuestion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Quản lý trạng thái câu hỏi đã làm trong lesson
 * Lưu trữ câu hỏi đã làm (đúng/sai) để phân biệt học mới vs ôn lại
 */
public class LessonQuestionManager {
    private static final String TAG = "LessonQuestionManager";
    private static final String PREF_NAME = "RiseOfCity_Prefs";
    private static final String KEY_ANSWERED_QUESTIONS = "lesson_answered_"; // Prefix + lessonName
    private static final String KEY_CORRECT_QUESTIONS = "lesson_correct_"; // Prefix + lessonName
    
    private Context appContext;
    private SharedPreferences prefs;
    
    public LessonQuestionManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Đánh dấu câu hỏi đã làm (đúng hoặc sai)
     * @param lessonName Tên lesson (ví dụ: "House_lv1")
     * @param questionId ID của câu hỏi
     * @param isCorrect true nếu trả lời đúng
     */
    public void markQuestionAnswered(String lessonName, String questionId, boolean isCorrect) {
        // Chạy trên background thread để tránh lag
        new Thread(() -> {
            String answeredKey = KEY_ANSWERED_QUESTIONS + lessonName;
            String correctKey = KEY_CORRECT_QUESTIONS + lessonName;
            
            Set<String> answeredSet = prefs.getStringSet(answeredKey, new HashSet<>());
            Set<String> correctSet = prefs.getStringSet(correctKey, new HashSet<>());
            
            // Tạo copy mới để modify (StringSet trong SharedPreferences là immutable)
            Set<String> answeredSetCopy = new HashSet<>(answeredSet);
            Set<String> correctSetCopy = new HashSet<>(correctSet);
            
            answeredSetCopy.add(questionId);
            if (isCorrect) {
                correctSetCopy.add(questionId);
            } else {
                correctSetCopy.remove(questionId); // Xóa khỏi correct set nếu sai
            }
            
            prefs.edit()
                .putStringSet(answeredKey, answeredSetCopy)
                .putStringSet(correctKey, correctSetCopy)
                .apply();
            
            Log.d(TAG, "Marked question " + questionId + " as " + (isCorrect ? "correct" : "wrong") + " in lesson " + lessonName);
        }).start();
    }
    
    /**
     * Kiểm tra câu hỏi đã làm chưa
     */
    public boolean isQuestionAnswered(String lessonName, String questionId) {
        String answeredKey = KEY_ANSWERED_QUESTIONS + lessonName;
        Set<String> answeredSet = prefs.getStringSet(answeredKey, new HashSet<>());
        return answeredSet.contains(questionId);
    }
    
    /**
     * Kiểm tra câu hỏi đã trả lời đúng chưa
     */
    public boolean isQuestionCorrect(String lessonName, String questionId) {
        String correctKey = KEY_CORRECT_QUESTIONS + lessonName;
        Set<String> correctSet = prefs.getStringSet(correctKey, new HashSet<>());
        return correctSet.contains(questionId);
    }
    
    /**
     * Lọc danh sách câu hỏi cho REVIEW mode - chỉ lấy câu đã làm
     */
    public List<BaseQuestion> filterAnsweredQuestions(String lessonName, List<BaseQuestion> allQuestions) {
        List<BaseQuestion> answeredQuestions = new ArrayList<>();
        String answeredKey = KEY_ANSWERED_QUESTIONS + lessonName;
        Set<String> answeredSet = prefs.getStringSet(answeredKey, new HashSet<>());
        
        for (BaseQuestion question : allQuestions) {
            if (answeredSet.contains(question.getId())) {
                answeredQuestions.add(question);
            }
        }
        
        return answeredQuestions;
    }
    
    /**
     * Lọc danh sách câu hỏi cho STUDY_NEW mode - chỉ lấy câu CHƯA làm (câu mới)
     */
    public List<BaseQuestion> filterNewQuestions(String lessonName, List<BaseQuestion> allQuestions) {
        List<BaseQuestion> newQuestions = new ArrayList<>();
        String answeredKey = KEY_ANSWERED_QUESTIONS + lessonName;
        Set<String> answeredSet = prefs.getStringSet(answeredKey, new HashSet<>());
        
        for (BaseQuestion question : allQuestions) {
            // Chỉ thêm câu chưa làm (chưa có trong answeredSet)
            if (!answeredSet.contains(question.getId())) {
                newQuestions.add(question);
            }
        }
        
        return newQuestions;
    }
    
    /**
     * Lọc danh sách câu hỏi cho STUDY_NEW mode - chỉ lấy câu CHƯA làm ĐÚNG
     * (Các câu chưa làm hoặc đã làm SAI → có thể cộng vàng khi làm đúng)
     */
    public List<BaseQuestion> filterQuestionsForStudy(String lessonName, List<BaseQuestion> allQuestions) {
        List<BaseQuestion> studyQuestions = new ArrayList<>();
        String correctKey = KEY_CORRECT_QUESTIONS + lessonName;
        Set<String> correctSet = prefs.getStringSet(correctKey, new HashSet<>());
        
        for (BaseQuestion question : allQuestions) {
            // Chỉ thêm câu CHƯA làm đúng (chưa có trong correctSet)
            if (!correctSet.contains(question.getId())) {
                studyQuestions.add(question);
            }
        }
        
        return studyQuestions;
    }
    
    /**
     * Xóa tất cả trạng thái câu hỏi của một lesson (dùng khi reset)
     */
    public void clearLessonProgress(String lessonName) {
        String answeredKey = KEY_ANSWERED_QUESTIONS + lessonName;
        String correctKey = KEY_CORRECT_QUESTIONS + lessonName;
        
        prefs.edit()
            .remove(answeredKey)
            .remove(correctKey)
            .apply();
        
        Log.d(TAG, "Cleared progress for lesson: " + lessonName);
    }
}

