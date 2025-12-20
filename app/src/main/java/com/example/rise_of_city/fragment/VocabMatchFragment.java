package com.example.rise_of_city.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment cho màn hình Vocab Match - Game match vocab related with topic
 * Người dùng kéo từ vựng vào đúng category box
 */
public class VocabMatchFragment extends Fragment {

    private LinearLayout vocabContainer;
    private LinearLayout schoolBox;
    private LinearLayout homeBox;
    private TextView tvSchoolLabel;
    private TextView tvHomeLabel;
    
    private List<String> vocabWords;
    private List<String> schoolWords;
    private List<String> homeWords;
    
    // Correct matches: word -> category
    private String[][] correctMatches = {
        {"kitchen", "Home"},
        {"lesson", "School"},
        {"book", "School"}
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vocab_match, container, false);
        
        vocabContainer = view.findViewById(R.id.vocab_container);
        schoolBox = view.findViewById(R.id.school_box);
        homeBox = view.findViewById(R.id.home_box);
        tvSchoolLabel = view.findViewById(R.id.tv_school_label);
        tvHomeLabel = view.findViewById(R.id.tv_home_label);
        
        vocabWords = new ArrayList<>();
        schoolWords = new ArrayList<>();
        homeWords = new ArrayList<>();
        
        setupVocabWords();
        setupBoxes();
        
        return view;
    }
    
    private void setupVocabWords() {
        vocabContainer.removeAllViews();
        
        String[] words = {"kitchen", "lesson", "book"};
        vocabWords.clear();
        
        for (String word : words) {
            vocabWords.add(word);
            TextView vocabButton = createVocabButton(word);
            vocabContainer.addView(vocabButton);
        }
    }
    
    private TextView createVocabButton(String word) {
        TextView button = new TextView(getContext());
        button.setText(word);
        button.setPadding(32, 16, 32, 16);
        button.setTextSize(16);
        button.setTextColor(0xFFFFFFFF);
        button.setBackgroundResource(R.drawable.bg_vocab_button);
        button.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
        params.setMargins(8, 8, 8, 8);
        button.setLayoutParams(params);
        
        button.setOnClickListener(v -> {
            // Show options to select category
            showCategorySelection(word, button);
        });
        
        return button;
    }
    
    private void showCategorySelection(String word, TextView vocabButton) {
        // Remove word from vocab container
        vocabContainer.removeView(vocabButton);
        vocabWords.remove(word);
        
        // Show dialog or directly add to category
        // For simplicity, we'll add to the first available box
        // In a real implementation, you might show a dialog or use drag-and-drop
        
        // Check which category this word belongs to
        String correctCategory = getCorrectCategory(word);
        
        if ("School".equals(correctCategory)) {
            addWordToBox(word, schoolBox, schoolWords);
        } else if ("Home".equals(correctCategory)) {
            addWordToBox(word, homeBox, homeWords);
        }
        
        // Check if all words are matched
        checkCompletion();
    }
    
    private String getCorrectCategory(String word) {
        for (String[] match : correctMatches) {
            if (match[0].equals(word)) {
                return match[1];
            }
        }
        return "";
    }
    
    private void addWordToBox(String word, LinearLayout box, List<String> wordList) {
        TextView wordView = new TextView(getContext());
        wordView.setText(word);
        wordView.setPadding(16, 12, 16, 12);
        wordView.setTextSize(14);
        wordView.setTextColor(0xFF000000);
        wordView.setBackgroundResource(R.drawable.bg_word_in_box);
        wordView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) wordView.getLayoutParams();
        params.setMargins(4, 4, 4, 4);
        wordView.setLayoutParams(params);
        
        wordList.add(word);
        box.addView(wordView);
        
        // Check if correct
        String correctCategory = getCorrectCategory(word);
        boolean isCorrect = (correctCategory.equals("School") && box == schoolBox) ||
                           (correctCategory.equals("Home") && box == homeBox);
        
        if (isCorrect) {
            Toast.makeText(getContext(), "Correct! " + word + " belongs to " + correctCategory, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Incorrect! Try again", Toast.LENGTH_SHORT).show();
            // Remove and put back
            box.removeView(wordView);
            wordList.remove(word);
            vocabWords.add(word);
            TextView vocabButton = createVocabButton(word);
            vocabContainer.addView(vocabButton);
        }
    }
    
    private void setupBoxes() {
        schoolBox.setOnClickListener(v -> {
            // Box click handler if needed
        });
        
        homeBox.setOnClickListener(v -> {
            // Box click handler if needed
        });
    }
    
    private void checkCompletion() {
        if (vocabWords.isEmpty()) {
            // Check if all words are in correct boxes
            boolean allCorrect = true;
            for (String[] match : correctMatches) {
                String word = match[0];
                String category = match[1];
                
                boolean wordInCorrectBox = false;
                if ("School".equals(category)) {
                    wordInCorrectBox = schoolWords.contains(word);
                } else if ("Home".equals(category)) {
                    wordInCorrectBox = homeWords.contains(word);
                }
                
                if (!wordInCorrectBox) {
                    allCorrect = false;
                    break;
                }
            }
            
            if (allCorrect) {
                Toast.makeText(getContext(), "Congratulations! All words matched correctly!", Toast.LENGTH_LONG).show();
            }
        }
    }
}

