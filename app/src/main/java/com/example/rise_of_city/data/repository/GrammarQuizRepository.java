package com.example.rise_of_city.data.repository;

import android.util.Log;
import com.example.rise_of_city.data.model.learning.GrammarQuiz;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Repository để quản lý Grammar Quiz từ Firebase
 */
public class GrammarQuizRepository {
    private static final String TAG = "GrammarQuizRepository";
    private static GrammarQuizRepository instance;
    
    private FirebaseFirestore firestore;
    
    private GrammarQuizRepository() {
        firestore = FirebaseFirestore.getInstance();
    }
    
    public static GrammarQuizRepository getInstance() {
        if (instance == null) {
            instance = new GrammarQuizRepository();
        }
        return instance;
    }
    
    /**
     * Interface để listen khi load grammar quizzes
     */
    public interface OnGrammarQuizzesLoadedListener {
        void onQuizzesLoaded(List<GrammarQuiz> quizzes);
        void onError(String error);
    }
    
    /**
     * Lấy grammar quiz theo topicId (ví dụ: "thi_hien_tai_don")
     */
    public void getQuizzesByTopic(String topicId, OnGrammarQuizzesLoadedListener listener) {
        firestore.collection("grammar_quizzes")
                .whereEqualTo("topicId", topicId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<GrammarQuiz> quizzes = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        GrammarQuiz quiz = documentToGrammarQuiz(document);
                        if (quiz != null) {
                            quizzes.add(quiz);
                        }
                    }
                    
                    // Sort by order field in memory (avoiding Firestore composite index requirement)
                    quizzes.sort((q1, q2) -> Integer.compare(q1.getOrder(), q2.getOrder()));
                    
                    if (listener != null) {
                        listener.onQuizzesLoaded(quizzes);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading grammar quizzes: " + e.getMessage());
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }
    
    /**
     * Lấy một quiz ngẫu nhiên theo topicId
     */
    public void getRandomQuizByTopic(String topicId, OnGrammarQuizLoadedListener listener) {
        getQuizzesByTopic(topicId, new OnGrammarQuizzesLoadedListener() {
            @Override
            public void onQuizzesLoaded(List<GrammarQuiz> quizzes) {
                if (quizzes.isEmpty()) {
                    if (listener != null) {
                        listener.onError("Không có quiz nào cho topic này");
                    }
                    return;
                }
                
                // Xáo trộn và lấy 1 quiz ngẫu nhiên
                Collections.shuffle(quizzes);
                if (listener != null) {
                    listener.onQuizLoaded(quizzes.get(0));
                }
            }
            
            @Override
            public void onError(String error) {
                if (listener != null) {
                    listener.onError(error);
                }
            }
        });
    }
    
    /**
     * Interface để listen khi load một grammar quiz
     */
    public interface OnGrammarQuizLoadedListener {
        void onQuizLoaded(GrammarQuiz quiz);
        void onError(String error);
    }
    
    /**
     * Convert Firestore DocumentSnapshot to GrammarQuiz object
     */
    private GrammarQuiz documentToGrammarQuiz(QueryDocumentSnapshot doc) {
        try {
            GrammarQuiz quiz = new GrammarQuiz();
            quiz.setId(doc.getId());
            
            Map<String, Object> data = doc.getData();
            if (data == null) return null;
            
            quiz.setTopicId((String) data.get("topicId"));
            quiz.setTopicName((String) data.get("topicName"));
            quiz.setQuestion((String) data.get("question"));
            quiz.setExplanation((String) data.get("explanation"));
            quiz.setLevel((String) data.get("level"));
            
            // Options - List<String>
            Object optionsObj = data.get("options");
            if (optionsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> options = (List<String>) optionsObj;
                quiz.setOptions(options);
            }
            
            // CorrectAnswer - Long or Integer
            Object correctAnswerObj = data.get("correctAnswer");
            if (correctAnswerObj instanceof Number) {
                quiz.setCorrectAnswer(((Number) correctAnswerObj).intValue());
            }
            
            // Order - Long or Integer
            Object orderObj = data.get("order");
            if (orderObj instanceof Number) {
                quiz.setOrder(((Number) orderObj).intValue());
            }
            
            return quiz;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to grammar quiz: " + e.getMessage());
            return null;
        }
    }
}

