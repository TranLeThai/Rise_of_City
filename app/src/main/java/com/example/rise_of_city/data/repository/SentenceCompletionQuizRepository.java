package com.example.rise_of_city.data.repository;

import android.util.Log;
import com.example.rise_of_city.data.model.SentenceCompletionQuiz;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SentenceCompletionQuizRepository {
    private static final String TAG = "SentenceCompletionQuizRepository";
    private static SentenceCompletionQuizRepository instance;
    
    private FirebaseFirestore firestore;
    
    private SentenceCompletionQuizRepository() {
        firestore = FirebaseFirestore.getInstance();
    }
    
    public static SentenceCompletionQuizRepository getInstance() {
        if (instance == null) {
            instance = new SentenceCompletionQuizRepository();
        }
        return instance;
    }
    
    public interface OnSentenceCompletionQuizLoadedListener {
        void onQuizLoaded(SentenceCompletionQuiz quiz);
        void onError(String error);
    }
    
    /**
     * Lấy một sentence completion quiz ngẫu nhiên
     */
    public void getRandomQuiz(OnSentenceCompletionQuizLoadedListener listener) {
        firestore.collection("sentence_completion_quizzes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<SentenceCompletionQuiz> quizzes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        SentenceCompletionQuiz quiz = documentToSentenceCompletionQuiz(document);
                        if (quiz != null) {
                            quizzes.add(quiz);
                        }
                    }
                    
                    if (quizzes.isEmpty()) {
                        if (listener != null) {
                            listener.onError("No sentence completion quizzes found.");
                        }
                        return;
                    }
                    
                    Collections.shuffle(quizzes);
                    if (listener != null) {
                        listener.onQuizLoaded(quizzes.get(0));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading sentence completion quiz: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }
    
    private SentenceCompletionQuiz documentToSentenceCompletionQuiz(QueryDocumentSnapshot doc) {
        try {
            SentenceCompletionQuiz quiz = new SentenceCompletionQuiz();
            quiz.setId(doc.getId());
            
            Map<String, Object> data = doc.getData();
            if (data == null) return null;
            
            quiz.setSentence((String) data.get("sentence"));
            
            Object optionsObj = data.get("options");
            if (optionsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> options = (List<String>) optionsObj;
                quiz.setOptions(options);
            }
            
            Object correctAnswerObj = data.get("correctAnswer");
            if (correctAnswerObj instanceof Number) {
                quiz.setCorrectAnswer(((Number) correctAnswerObj).intValue());
            }
            
            quiz.setLevel((String) data.get("level"));
            
            Object orderObj = data.get("order");
            if (orderObj instanceof Number) {
                quiz.setOrder(((Number) orderObj).intValue());
            }
            
            quiz.setExplanation((String) data.get("explanation"));
            
            return quiz;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to SentenceCompletionQuiz: " + e.getMessage());
            return null;
        }
    }
}

