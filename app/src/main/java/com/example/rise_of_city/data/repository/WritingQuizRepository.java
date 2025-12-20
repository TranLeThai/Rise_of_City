package com.example.rise_of_city.data.repository;

import android.util.Log;
import com.example.rise_of_city.data.model.WritingQuiz;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WritingQuizRepository {
    private static final String TAG = "WritingQuizRepository";
    private static WritingQuizRepository instance;
    
    private FirebaseFirestore firestore;
    
    private WritingQuizRepository() {
        firestore = FirebaseFirestore.getInstance();
    }
    
    public static WritingQuizRepository getInstance() {
        if (instance == null) {
            instance = new WritingQuizRepository();
        }
        return instance;
    }
    
    public interface OnWritingQuizLoadedListener {
        void onQuizLoaded(WritingQuiz quiz);
        void onError(String error);
    }
    
    /**
     * Lấy một writing quiz ngẫu nhiên
     */
    public void getRandomQuiz(OnWritingQuizLoadedListener listener) {
        firestore.collection("writing_quizzes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<WritingQuiz> quizzes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        WritingQuiz quiz = documentToWritingQuiz(document);
                        if (quiz != null) {
                            quizzes.add(quiz);
                        }
                    }
                    
                    if (quizzes.isEmpty()) {
                        if (listener != null) {
                            listener.onError("No writing quizzes found.");
                        }
                        return;
                    }
                    
                    Collections.shuffle(quizzes);
                    if (listener != null) {
                        listener.onQuizLoaded(quizzes.get(0));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading writing quiz: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }
    
    private WritingQuiz documentToWritingQuiz(QueryDocumentSnapshot doc) {
        try {
            WritingQuiz quiz = new WritingQuiz();
            quiz.setId(doc.getId());
            
            Map<String, Object> data = doc.getData();
            if (data == null) return null;
            
            quiz.setSentence((String) data.get("sentence"));
            quiz.setAnswer((String) data.get("answer"));
            quiz.setLevel((String) data.get("level"));
            
            Object orderObj = data.get("order");
            if (orderObj instanceof Number) {
                quiz.setOrder(((Number) orderObj).intValue());
            }
            
            quiz.setHint((String) data.get("hint"));
            
            return quiz;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to WritingQuiz: " + e.getMessage());
            return null;
        }
    }
}




