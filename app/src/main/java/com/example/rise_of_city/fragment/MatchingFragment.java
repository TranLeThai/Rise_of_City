package com.example.rise_of_city.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.ui.matching.MatchingLineView;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fragment cho màn hình Matching Exercise
 * Người dùng nối từ tiếng Anh với từ tiếng Việt tương ứng
 */
public class MatchingFragment extends Fragment {

    private RecyclerView rvLeftWords, rvRightWords;
    private MatchingLineView matchingLineView;
    private WordAdapter leftAdapter, rightAdapter;
    private Button btnNext;
    
    private List<WordItem> leftWords;
    private List<WordItem> rightWords;
    
    private WordItem selectedLeftWord = null;
    private WordItem selectedRightWord = null;
    
    private int matchedCount = 0;
    
    // Matching pairs: English -> Vietnamese
    private String[][] correctPairs = {
        {"Classroom", "Lớp học"},
        {"Library", "Thư viện"},
        {"Chalk", "Phấn"},
        {"Schoolbag", "Cặp sách"}
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matching, container, false);

        rvLeftWords = view.findViewById(R.id.rv_left_words);
        rvRightWords = view.findViewById(R.id.rv_right_words);
        matchingLineView = view.findViewById(R.id.matching_line_view);
        btnNext = view.findViewById(R.id.btn_next);

        setupWords();
        setupRecyclerViews();
        
        btnNext.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.rise_of_city.ui.main.MainActivity) {
                // Navigate to Quiz Selection screen first
                QuizSelectionFragment quizFragment = new QuizSelectionFragment();
                Bundle args = new Bundle();
                args.putString("quiz_type", "both");
                quizFragment.setArguments(args);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, quizFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    private void setupWords() {
        leftWords = new ArrayList<>();
        rightWords = new ArrayList<>();
        
        // Create English words (left column) - 24 items
        String[] englishWords = {"Classroom", "Library", "Chalk", "Schoolbag"};
        for (int i = 0; i < 24; i++) {
            String word = englishWords[i % 4];
            // Red dots: positions 0,1,2,3,4,9,14,19,20
            boolean isRed = (i <= 4) || (i == 9) || (i == 14) || (i == 19) || (i == 20);
            leftWords.add(new WordItem(word, isRed, i));
        }
        
        // Create Vietnamese words (right column) - 24 items
        String[] vietnameseWords = {"Thư viện", "Phấn", "Lớp học", "Cặp sách"};
        for (int i = 0; i < 24; i++) {
            String word = vietnameseWords[i % 4];
            rightWords.add(new WordItem(word, true, i)); // All red dots
        }
        
        // Don't shuffle - keep order as in image
    }

    private void setupRecyclerViews() {
        // Left column adapter
        leftAdapter = new WordAdapter(leftWords, true);
        rvLeftWords.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLeftWords.setAdapter(leftAdapter);
        
        // Right column adapter
        rightAdapter = new WordAdapter(rightWords, false);
        rvRightWords.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRightWords.setAdapter(rightAdapter);
    }

    private void onWordSelected(WordItem word, boolean isLeft) {
        if (isLeft) {
            if (selectedLeftWord == word) {
                // Click lại cùng từ → bỏ chọn
                selectedLeftWord = null;
                leftAdapter.notifyDataSetChanged();
            } else {
                selectedLeftWord = word;
                leftAdapter.notifyDataSetChanged();
                
                // Nếu đã chọn từ bên phải, thử match ngay
                if (selectedRightWord != null) {
                    checkMatch(selectedLeftWord, selectedRightWord);
                } else {
                    rightAdapter.notifyDataSetChanged();
                }
            }
        } else {
            if (selectedRightWord == word) {
                // Click lại cùng từ → bỏ chọn
                selectedRightWord = null;
                rightAdapter.notifyDataSetChanged();
            } else {
                selectedRightWord = word;
                rightAdapter.notifyDataSetChanged();
                
                // Nếu đã chọn từ bên trái, thử match ngay
                if (selectedLeftWord != null) {
                    checkMatch(selectedLeftWord, selectedRightWord);
                } else {
                    leftAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void checkMatch(WordItem leftWord, WordItem rightWord) {
        // Check if it's a correct match
        boolean isCorrect = false;
        for (String[] pair : correctPairs) {
            if (pair[0].equals(leftWord.word) && pair[1].equals(rightWord.word)) {
                isCorrect = true;
                break;
            }
        }
        
        if (isCorrect) {
            // Draw line
            drawLine(leftWord, rightWord);
            Toast.makeText(getContext(), "Correct match!", Toast.LENGTH_SHORT).show();
            
            // Mark as matched
            leftWord.matched = true;
            rightWord.matched = true;
            matchedCount++;
            
            // Check if all matches are complete
            checkCompletion();
            
            // Clear selection
            selectedLeftWord = null;
            selectedRightWord = null;
            leftAdapter.notifyDataSetChanged();
            rightAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getContext(), "Incorrect match. Try again!", Toast.LENGTH_SHORT).show();
            selectedLeftWord = null;
            selectedRightWord = null;
            leftAdapter.notifyDataSetChanged();
            rightAdapter.notifyDataSetChanged();
        }
    }

    private void checkCompletion() {
        // Check if all correct pairs are matched
        if (matchedCount >= correctPairs.length) {
            btnNext.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "All matches completed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawLine(WordItem leftWord, WordItem rightWord) {
        // Post to ensure views are laid out
        matchingLineView.post(() -> {
            // Wait for RecyclerView to layout
            rvLeftWords.post(() -> {
                rvRightWords.post(() -> {
                    // Find the actual views by searching through visible children
                    View leftView = findViewForWord(rvLeftWords, leftWord);
                    View rightView = findViewForWord(rvRightWords, rightWord);
                    
                    if (leftView != null && rightView != null) {
                        // Get positions
                        int[] leftPos = new int[2];
                        int[] rightPos = new int[2];
                        int[] lineViewPos = new int[2];
                        
                        leftView.getLocationInWindow(leftPos);
                        rightView.getLocationInWindow(rightPos);
                        matchingLineView.getLocationInWindow(lineViewPos);
                        
                        // Calculate positions relative to matchingLineView
                        // Start from right edge of left item (where dot is)
                        float startX = leftPos[0] + leftView.getWidth() - lineViewPos[0];
                        float startY = leftPos[1] + leftView.getHeight() / 2f - lineViewPos[1];
                        
                        // End at left edge of right item (where dot is)
                        float endX = rightPos[0] - lineViewPos[0];
                        float endY = rightPos[1] + rightView.getHeight() / 2f - lineViewPos[1];
                        
                        matchingLineView.addConnection(startX, startY, endX, endY);
                    }
                });
            });
        });
    }
    
    private View findViewForWord(RecyclerView recyclerView, WordItem wordItem) {
        // Search through all visible child views
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            // Get the ViewHolder
            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(child);
            if (holder instanceof WordAdapter.WordViewHolder) {
                WordAdapter.WordViewHolder wordHolder = (WordAdapter.WordViewHolder) holder;
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (recyclerView == rvLeftWords && position < leftWords.size()) {
                        if (leftWords.get(position) == wordItem) {
                            return child;
                        }
                    } else if (recyclerView == rvRightWords && position < rightWords.size()) {
                        if (rightWords.get(position) == wordItem) {
                            return child;
                        }
                    }
                }
            }
        }
        return null;
    }

    // Word Item Model
    private static class WordItem {
        String word;
        boolean isRedDot;
        int index;
        boolean matched = false;

        WordItem(String word, boolean isRedDot, int index) {
            this.word = word;
            this.isRedDot = isRedDot;
            this.index = index;
        }
    }

    // Word Adapter
    private class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {
        private List<WordItem> words;
        private boolean isLeftColumn;

        WordAdapter(List<WordItem> words, boolean isLeftColumn) {
            this.words = words;
            this.isLeftColumn = isLeftColumn;
        }

        @NonNull
        @Override
        public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_match_word, parent, false);
            return new WordViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
            WordItem item = words.get(position);
            holder.tvWord.setText(item.word);
            
            // Set dot color
            if (item.isRedDot) {
                holder.dot.setBackgroundResource(R.drawable.dot_red);
            } else {
                holder.dot.setBackgroundResource(R.drawable.dot_pink);
            }
            
            // Highlight if selected
            if ((isLeftColumn && selectedLeftWord == item) || 
                (!isLeftColumn && selectedRightWord == item)) {
                holder.cardWord.setCardBackgroundColor(0xFFCE93D8);
            } else if (item.matched) {
                holder.cardWord.setCardBackgroundColor(0xFFC8E6C9); // Green for matched
            } else {
                holder.cardWord.setCardBackgroundColor(0xFFE1BEE7);
            }
            
            holder.itemView.setOnClickListener(v -> {
                onWordSelected(item, isLeftColumn);
            });
        }

        @Override
        public int getItemCount() {
            return words.size();
        }

        class WordViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardWord;
            TextView tvWord;
            View dot;

            WordViewHolder(@NonNull View itemView) {
                super(itemView);
                cardWord = itemView.findViewById(R.id.card_word);
                tvWord = itemView.findViewById(R.id.tv_word);
                dot = itemView.findViewById(R.id.dot);
            }
        }
    }
}
