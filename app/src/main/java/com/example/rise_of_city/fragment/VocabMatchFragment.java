package com.example.rise_of_city.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.Vocabulary;
import com.example.rise_of_city.data.repository.VocabularyRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment cho màn hình Vocab Match - Game match vocab related with topic
 * Flow: Click vocab word → Chọn topic → Mở quiz
 * CHỈ DÙNG CHO VOCAB SCREEN
 */
public class VocabMatchFragment extends Fragment {

    private LinearLayout vocabContainer;
    private LinearLayout schoolBox;
    private LinearLayout homeBox;
    private TextView tvSchoolLabel;
    private TextView tvHomeLabel;
    private ProgressBar progressBar;
    
    private List<String> vocabWords;
    private List<String> schoolWords;
    private List<String> homeWords;
    private List<Vocabulary> allVocabularies;
    private Map<String, String> wordToCategoryMap;
    private Map<String, Vocabulary> wordToVocabMap;
    
    private VocabularyRepository vocabRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vocab_match, container, false);
        
        vocabContainer = view.findViewById(R.id.vocab_container);
        schoolBox = view.findViewById(R.id.school_box);
        homeBox = view.findViewById(R.id.home_box);
        tvSchoolLabel = view.findViewById(R.id.tv_school_label);
        tvHomeLabel = view.findViewById(R.id.tv_home_label);
        progressBar = view.findViewById(R.id.progress_bar);
        
        vocabWords = new ArrayList<>();
        schoolWords = new ArrayList<>();
        homeWords = new ArrayList<>();
        allVocabularies = new ArrayList<>();
        wordToCategoryMap = new HashMap<>();
        wordToVocabMap = new HashMap<>();
        
        vocabRepository = VocabularyRepository.getInstance();
        
        // Load vocab data from Firestore
        loadVocabulariesFromFirestore();
        setupBoxes();
        
        return view;
    }
    
    private void loadVocabulariesFromFirestore() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        // Load vocabularies from Firestore
        vocabRepository.getVocabulariesFromFirestore(new VocabularyRepository.OnVocabulariesLoadedListener() {
            @Override
            public void onVocabulariesLoaded(List<Vocabulary> vocabularies) {
                allVocabularies = vocabularies;
                
                // Filter vocabularies by buildingId (school and house/home)
                List<Vocabulary> schoolVocabs = new ArrayList<>();
                List<Vocabulary> homeVocabs = new ArrayList<>();
                
                for (Vocabulary vocab : vocabularies) {
                    String buildingId = vocab.getBuildingId();
                    if (buildingId != null) {
                        String lowerBuildingId = buildingId.toLowerCase();
                        if (lowerBuildingId.contains("school") || lowerBuildingId.equals("school")) {
                            schoolVocabs.add(vocab);
                            wordToCategoryMap.put(vocab.getEnglish().toLowerCase(), "School");
                            wordToVocabMap.put(vocab.getEnglish().toLowerCase(), vocab);
                        } else if (lowerBuildingId.contains("house") || lowerBuildingId.contains("home") || 
                                   lowerBuildingId.equals("house") || lowerBuildingId.equals("home")) {
                            homeVocabs.add(vocab);
                            wordToCategoryMap.put(vocab.getEnglish().toLowerCase(), "Home");
                            wordToVocabMap.put(vocab.getEnglish().toLowerCase(), vocab);
                        }
                    }
                }
                
                // Select exactly 3 vocabularies for the game
                List<Vocabulary> selectedVocabs = new ArrayList<>();
                if (schoolVocabs.size() > 0 && homeVocabs.size() > 0) {
                    // Mix school and home vocabularies - ensure exactly 3 words
                    Collections.shuffle(schoolVocabs);
                    Collections.shuffle(homeVocabs);
                    
                    // Select 2 from one category and 1 from the other, or 1-1-1 mix
                    int schoolCount = Math.min(2, schoolVocabs.size());
                    int homeCount = Math.min(1, homeVocabs.size());
                    
                    if (schoolCount + homeCount < 3) {
                        // Adjust to get exactly 3
                        if (schoolVocabs.size() >= 3) {
                            schoolCount = 3;
                            homeCount = 0;
                        } else if (homeVocabs.size() >= 3) {
                            schoolCount = 0;
                            homeCount = 3;
                        } else {
                            schoolCount = Math.min(2, schoolVocabs.size());
                            homeCount = Math.min(3 - schoolCount, homeVocabs.size());
                        }
                    }
                    
                    selectedVocabs.addAll(schoolVocabs.subList(0, schoolCount));
                    selectedVocabs.addAll(homeVocabs.subList(0, homeCount));
                    
                    // Ensure exactly 3 words
                    if (selectedVocabs.size() > 3) {
                        selectedVocabs = selectedVocabs.subList(0, 3);
                    }
                    
                    Collections.shuffle(selectedVocabs);
                } else if (schoolVocabs.size() > 0) {
                    selectedVocabs.addAll(schoolVocabs.subList(0, Math.min(3, schoolVocabs.size())));
                } else if (homeVocabs.size() > 0) {
                    selectedVocabs.addAll(homeVocabs.subList(0, Math.min(3, homeVocabs.size())));
                }
                
                // Setup vocab words with loaded data
                setupVocabWords(selectedVocabs);
                
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e("VocabMatchFragment", "Error loading vocabularies: " + error);
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error, Toast.LENGTH_SHORT).show();
                
                // Fallback to default data
                setupVocabWordsWithDefault();
                
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
    
    private void setupVocabWords(List<Vocabulary> vocabularies) {
        vocabContainer.removeAllViews();
        vocabWords.clear();
        
        if (vocabularies == null || vocabularies.isEmpty()) {
            setupVocabWordsWithDefault();
            return;
        }
        
        // Only show exactly 3 vocab words
        int count = 0;
        for (Vocabulary vocab : vocabularies) {
            if (count >= 3) break;
            
            String word = vocab.getEnglish();
            if (word != null && !word.isEmpty()) {
                vocabWords.add(word.toLowerCase());
                TextView vocabButton = createVocabButton(word, vocab);
                vocabContainer.addView(vocabButton);
                count++;
            }
        }
    }
    
    private void setupVocabWordsWithDefault() {
        vocabContainer.removeAllViews();
        vocabWords.clear();
        
        // Default fallback data
        String[] words = {"kitchen", "lesson", "book"};
        wordToCategoryMap.put("kitchen", "Home");
        wordToCategoryMap.put("lesson", "School");
        wordToCategoryMap.put("book", "School");
        
        for (String word : words) {
            vocabWords.add(word);
            TextView vocabButton = createVocabButton(word, null);
            vocabContainer.addView(vocabButton);
        }
    }
    
    private TextView createVocabButton(String word, Vocabulary vocab) {
        TextView button = new TextView(getContext());
        button.setText(word);
        button.setPadding(16, 16, 16, 16);
        button.setTextSize(14);
        button.setTextColor(0xFFFFFFFF);
        button.setBackgroundResource(R.drawable.bg_vocab_button);
        button.setGravity(android.view.Gravity.CENTER);
        button.setAllCaps(false);
        button.setMaxLines(2);
        button.setEllipsize(android.text.TextUtils.TruncateAt.END);
        
        // Make buttons equal width and divide evenly (3 buttons)
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f  // Equal weight for each button
        );
        params.setMargins(4, 8, 4, 8);
        button.setLayoutParams(params);
        
        button.setOnClickListener(v -> {
            // Show topic selection dialog
            showTopicSelectionDialog(word, vocab);
        });
        
        return button;
    }
    
    private void showTopicSelectionDialog(String word, Vocabulary vocab) {
        Log.d("VocabMatchFragment", "showTopicSelectionDialog called for word: " + word);
        
        // Get buildingId from vocab
        String buildingId = vocab != null ? vocab.getBuildingId() : null;
        if (buildingId == null || buildingId.isEmpty()) {
            // Fallback: determine buildingId from word category
            String category = getCorrectCategory(word);
            if ("School".equals(category)) {
                buildingId = "school";
            } else if ("Home".equals(category)) {
                buildingId = "house";
            }
        }
        
        Log.d("VocabMatchFragment", "BuildingId: " + buildingId);
        
        // Check if fragment manager is available
        if (getParentFragmentManager() == null) {
            Log.e("VocabMatchFragment", "ParentFragmentManager is null!");
            Toast.makeText(getContext(), "Lỗi: Không thể mở dialog", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show topic selection dialog
        try {
            TopicSelectionDialogFragment dialog = TopicSelectionDialogFragment.newInstance(buildingId, word);
            dialog.show(getParentFragmentManager(), "TopicSelectionDialog");
            Log.d("VocabMatchFragment", "Dialog shown successfully");
        } catch (Exception e) {
            Log.e("VocabMatchFragment", "Error showing dialog: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Lỗi mở dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private String getCorrectCategory(String word) {
        return wordToCategoryMap.get(word.toLowerCase());
    }
    
    private void setupBoxes() {
        // Boxes chỉ để hiển thị category, không có action
        // Flow: Click vocab word → Topic selection → Quiz
        // Boxes không navigate đến screen khác
    }
}

