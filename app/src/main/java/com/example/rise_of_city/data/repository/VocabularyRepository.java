package com.example.rise_of_city.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.rise_of_city.data.model.Vocabulary;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Repository để quản lý từ vựng từ Firebase
 */
public class VocabularyRepository {
    private static final String TAG = "VocabularyRepository";
    private static VocabularyRepository instance;
    
    private FirebaseDatabase realtimeDatabase;
    private FirebaseFirestore firestore;
    private DatabaseReference vocabRef;
    
    private VocabularyRepository() {
        realtimeDatabase = FirebaseDatabase.getInstance();
        firestore = FirebaseFirestore.getInstance();
        vocabRef = realtimeDatabase.getReference("vocabularies");
    }
    
    public static VocabularyRepository getInstance() {
        if (instance == null) {
            instance = new VocabularyRepository();
        }
        return instance;
    }
    
    /**
     * Lấy danh sách từ vựng từ Firebase Realtime Database
     */
    public void getVocabularies(OnVocabulariesLoadedListener listener) {
        vocabRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Vocabulary> vocabularies = new ArrayList<>();
                
                for (DataSnapshot vocabSnapshot : snapshot.getChildren()) {
                    Vocabulary vocab = vocabSnapshot.getValue(Vocabulary.class);
                    if (vocab != null && vocab.getEnglish() != null && vocab.getVietnamese() != null) {
                        vocabularies.add(vocab);
                    }
                }
                
                listener.onVocabulariesLoaded(vocabularies);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading vocabularies: " + error.getMessage());
                listener.onError(error.getMessage());
            }
        });
    }
    
    /**
     * Lấy danh sách từ vựng từ Firestore
     */
    public void getVocabulariesFromFirestore(OnVocabulariesLoadedListener listener) {
        firestore.collection("vocabularies")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Vocabulary> vocabularies = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Vocabulary vocab = document.toObject(Vocabulary.class);
                        if (vocab != null && vocab.getEnglish() != null && vocab.getVietnamese() != null) {
                            vocabularies.add(vocab);
                        }
                    }
                    
                    listener.onVocabulariesLoaded(vocabularies);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading vocabularies from Firestore: " + e.getMessage());
                    listener.onError(e.getMessage());
                });
    }
    
    /**
     * Lấy một từ vựng ngẫu nhiên từ danh sách
     */
    public void getRandomVocabulary(OnVocabularyLoadedListener listener) {
        getVocabularies(new OnVocabulariesLoadedListener() {
            @Override
            public void onVocabulariesLoaded(List<Vocabulary> vocabularies) {
                if (vocabularies.isEmpty()) {
                    listener.onError("Không có từ vựng nào");
                    return;
                }
                
                // Xáo trộn danh sách
                Collections.shuffle(vocabularies);
                listener.onVocabularyLoaded(vocabularies.get(0));
            }
            
            @Override
            public void onError(String error) {
                listener.onError(error);
            }
        });
    }
    
    /**
     * Lấy 4 từ vựng ngẫu nhiên (1 đúng + 3 sai) để làm đáp án
     * @param buildingId ID của building (null = lấy từ tất cả building)
     */
    public void getQuizOptions(String buildingId, OnQuizOptionsLoadedListener listener) {
        getVocabulariesByBuilding(buildingId, new OnVocabulariesLoadedListener() {
            @Override
            public void onVocabulariesLoaded(List<Vocabulary> vocabularies) {
                if (vocabularies.size() < 4) {
                    // Nếu không đủ từ vựng trong building này, lấy từ tất cả
                    getVocabulariesFromFirestore(new OnVocabulariesLoadedListener() {
                        @Override
                        public void onVocabulariesLoaded(List<Vocabulary> allVocabularies) {
                            createQuizOptions(allVocabularies, listener);
                        }
                        
                        @Override
                        public void onError(String error) {
                            listener.onError(error);
                        }
                    });
                    return;
                }
                
                createQuizOptions(vocabularies, listener);
            }
            
            @Override
            public void onError(String error) {
                listener.onError(error);
            }
        });
    }
    
    /**
     * Tạo quiz options từ danh sách từ vựng
     */
    private void createQuizOptions(List<Vocabulary> vocabularies, OnQuizOptionsLoadedListener listener) {
        if (vocabularies.size() < 4) {
            listener.onError("Không đủ từ vựng để tạo quiz");
            return;
        }
        
        // Xáo trộn danh sách
        Collections.shuffle(vocabularies);
        
        // Lấy từ vựng đúng (có ảnh)
        Vocabulary correctVocab = null;
        for (Vocabulary vocab : vocabularies) {
            if (vocab.isHasImage() && vocab.getImageUrl() != null) {
                correctVocab = vocab;
                break;
            }
        }
        
        if (correctVocab == null) {
            // Nếu không có từ nào có ảnh, lấy từ đầu tiên
            correctVocab = vocabularies.get(0);
        }
        
        // Lấy 3 từ vựng khác làm đáp án sai
        List<Vocabulary> wrongOptions = new ArrayList<>();
        for (Vocabulary vocab : vocabularies) {
            if (!vocab.getEnglish().equals(correctVocab.getEnglish()) && wrongOptions.size() < 3) {
                wrongOptions.add(vocab);
            }
        }
        
        listener.onQuizOptionsLoaded(correctVocab, wrongOptions);
    }
    
    /**
     * Lấy từ vựng theo buildingId từ Firestore
     */
    public void getVocabulariesByBuilding(String buildingId, OnVocabulariesLoadedListener listener) {
        if (buildingId == null || buildingId.isEmpty()) {
            // Nếu không có buildingId, lấy tất cả
            getVocabulariesFromFirestore(listener);
            return;
        }
        
        firestore.collection("vocabularies")
                .whereEqualTo("buildingId", buildingId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Vocabulary> vocabularies = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Vocabulary vocab = document.toObject(Vocabulary.class);
                        if (vocab != null && vocab.getEnglish() != null && vocab.getVietnamese() != null) {
                            vocabularies.add(vocab);
                        }
                    }
                    
                    listener.onVocabulariesLoaded(vocabularies);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading vocabularies by building: " + e.getMessage());
                    listener.onError(e.getMessage());
                });
    }
    
    // Interfaces
    public interface OnVocabulariesLoadedListener {
        void onVocabulariesLoaded(List<Vocabulary> vocabularies);
        void onError(String error);
    }
    
    public interface OnVocabularyLoadedListener {
        void onVocabularyLoaded(Vocabulary vocabulary);
        void onError(String error);
    }
    
    public interface OnQuizOptionsLoadedListener {
        void onQuizOptionsLoaded(Vocabulary correctVocab, List<Vocabulary> wrongOptions);
        void onError(String error);
    }
}

