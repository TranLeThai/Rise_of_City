package com.example.rise_of_city.data.repository;

import android.util.Log;
import com.example.rise_of_city.data.model.SynonymAntonymQuiz;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SynonymAntonymQuizRepository {
    private static final String TAG = "SynonymAntonymQuizRepository";
    private static SynonymAntonymQuizRepository instance;
    
    private FirebaseFirestore firestore;
    
    private SynonymAntonymQuizRepository() {
        firestore = FirebaseFirestore.getInstance();
    }
    
    public static SynonymAntonymQuizRepository getInstance() {
        if (instance == null) {
            instance = new SynonymAntonymQuizRepository();
        }
        return instance;
    }
    
    public interface OnSynonymAntonymQuizLoadedListener {
        void onQuizLoaded(SynonymAntonymQuiz quiz);
        void onError(String error);
    }
    
    /**
     * Lấy một synonym/antonym quiz ngẫu nhiên
     */
    public void getRandomQuiz(OnSynonymAntonymQuizLoadedListener listener) {
        firestore.collection("synonym_antonym_quizzes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<SynonymAntonymQuiz> quizzes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        SynonymAntonymQuiz quiz = documentToSynonymAntonymQuiz(document);
                        if (quiz != null) {
                            quizzes.add(quiz);
                        }
                    }
                    
                    if (quizzes.isEmpty()) {
                        if (listener != null) {
                            listener.onError("No synonym/antonym quizzes found.");
                        }
                        return;
                    }
                    
                    Collections.shuffle(quizzes);
                    if (listener != null) {
                        listener.onQuizLoaded(quizzes.get(0));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading synonym/antonym quiz: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }
    
    private SynonymAntonymQuiz documentToSynonymAntonymQuiz(QueryDocumentSnapshot doc) {
        try {
            SynonymAntonymQuiz quiz = new SynonymAntonymQuiz();
            quiz.setId(doc.getId());
            
            Map<String, Object> data = doc.getData();
            if (data == null) return null;
            
            quiz.setWord((String) data.get("word"));
            quiz.setQuestion((String) data.get("question"));
            
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
            
            quiz.setType((String) data.get("type"));
            quiz.setLevel((String) data.get("level"));
            
            Object orderObj = data.get("order");
            if (orderObj instanceof Number) {
                quiz.setOrder(((Number) orderObj).intValue());
            }
            
            quiz.setExplanation((String) data.get("explanation"));
            
            return quiz;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to SynonymAntonymQuiz: " + e.getMessage());
            return null;
        }
    }
}






