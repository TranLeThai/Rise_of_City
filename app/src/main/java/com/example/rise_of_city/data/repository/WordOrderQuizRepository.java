package com.example.rise_of_city.data.repository;

import android.util.Log;
import com.example.rise_of_city.data.model.learning.WordOrderQuiz;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WordOrderQuizRepository {
    private static final String TAG = "WordOrderQuizRepository";
    private static WordOrderQuizRepository instance;
    
    private FirebaseFirestore firestore;
    
    private WordOrderQuizRepository() {
        firestore = FirebaseFirestore.getInstance();
    }
    
    public static WordOrderQuizRepository getInstance() {
        if (instance == null) {
            instance = new WordOrderQuizRepository();
        }
        return instance;
    }
    
    public interface OnWordOrderQuizLoadedListener {
        void onQuizLoaded(WordOrderQuiz quiz);
        void onError(String error);
    }
    
    /**
     * Lấy một word order quiz ngẫu nhiên
     */
    public void getRandomQuiz(OnWordOrderQuizLoadedListener listener) {
        firestore.collection("word_order_quizzes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<WordOrderQuiz> quizzes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        WordOrderQuiz quiz = documentToWordOrderQuiz(document);
                        if (quiz != null) {
                            quizzes.add(quiz);
                        }
                    }
                    
                    if (quizzes.isEmpty()) {
                        if (listener != null) {
                            listener.onError("No word order quizzes found.");
                        }
                        return;
                    }
                    
                    Collections.shuffle(quizzes);
                    if (listener != null) {
                        listener.onQuizLoaded(quizzes.get(0));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading word order quiz: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }
    
    private WordOrderQuiz documentToWordOrderQuiz(QueryDocumentSnapshot doc) {
        try {
            WordOrderQuiz quiz = new WordOrderQuiz();
            quiz.setId(doc.getId());
            
            Map<String, Object> data = doc.getData();
            if (data == null) return null;
            
            Object wordsObj = data.get("words");
            if (wordsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> words = (List<String>) wordsObj;
                quiz.setWords(words);
            }
            
            Object correctOrderObj = data.get("correctOrder");
            if (correctOrderObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<?> correctOrderRaw = (List<?>) correctOrderObj;
                List<Integer> correctOrder = new ArrayList<>();
                for (Object item : correctOrderRaw) {
                    if (item instanceof Number) {
                        correctOrder.add(((Number) item).intValue());
                    }
                }
                quiz.setCorrectOrder(correctOrder);
            }
            
            quiz.setLevel((String) data.get("level"));
            
            Object orderObj = data.get("order");
            if (orderObj instanceof Number) {
                quiz.setOrder(((Number) orderObj).intValue());
            }
            
            quiz.setExplanation((String) data.get("explanation"));
            
            return quiz;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to WordOrderQuiz: " + e.getMessage());
            return null;
        }
    }
}

