package com.example.rise_of_city.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
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
 * Fragment cho màn hình Quiz Selection - TextGame1
 * Người dùng click vào chỗ trống để hiển thị options và chọn từ
 */
public class QuizSelectionFragment extends Fragment {

    private TextView tvEnglishText;
    private TextView tvVietnameseText;
    private Button btnNext;
    private View cardVietnamese;
    private View icLang1;

    // Original text data (template)
    private String originalEnglishText = "Dear Mayor, The bakery project is ready for launch. We have imported enough [flower] to bake our signature bread. Currently, our professional chefs [is baking] the first batches. Please sign this document so we can open the doors to our citizens.";
    private String originalVietnameseText = "Kính gửi Thị trưởng, Dự án vector_bakery đã sẵn sàng hoạt động. Chúng tôi đã nhập đủ số lượng [bột mì] cần thiết để làm bánh mì. Hiện tại, các đầu bếp [đang] nướng những mẻ bánh đầu tiên. Ngài hãy ký duyệt để chúng tôi có thể khai trương cửa hàng.";
    
    // Current text state (updated as user selects answers)
    private String currentEnglishText;
    private String currentVietnameseText;

    // Blank information
    private List<BlankInfo> blanks = new ArrayList<>();

    // Correct answers
    private String correctAnswer1 = "Flour";
    private String correctAnswer2 = "are baking";

    // Selected answers
    private String selectedAnswer1 = null;
    private String selectedAnswer2 = null;

    // Highlight markers
    private String highlightStart = "[";
    private String highlightEnd = "]";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz_selection, container, false);

        tvEnglishText = view.findViewById(R.id.tv_english_text);
        tvVietnameseText = view.findViewById(R.id.tv_vietnamese_text);
        btnNext = view.findViewById(R.id.btn_next);
        cardVietnamese = view.findViewById(R.id.card_vietnamese);
        icLang1 = view.findViewById(R.id.ic_lang_1);

        // Initialize current text state
        currentEnglishText = originalEnglishText;
        currentVietnameseText = originalVietnameseText;

        // Setup blanks
        setupBlanks();
        setupTexts();
        
        // Setup click listener for translation icon
        icLang1.setOnClickListener(v -> {
            toggleVietnameseTranslation();
        });

        btnNext.setOnClickListener(v -> {
            if (selectedAnswer1 != null && selectedAnswer2 != null) {
                boolean allCorrect = selectedAnswer1.equals(correctAnswer1) && selectedAnswer2.equals(correctAnswer2);
                if (allCorrect) {
                    Toast.makeText(getContext(), "Perfect! All answers are correct!", Toast.LENGTH_SHORT).show();
                    navigateToNextScreen();
                } else {
                    Toast.makeText(getContext(), "Please check your answers!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Please select all answers!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void setupBlanks() {
        blanks.clear();
        
        // Blank 1: [flower] in English, [bột mì] in Vietnamese
        BlankInfo blank1 = new BlankInfo();
        blank1.englishMarker = "[flower]";
        blank1.vietnameseMarker = "[bột mì]";
        blank1.options = new String[]{"Flower", "Flour", "Fluor"};
        blank1.correctAnswer = "Flour";
        blank1.blankIndex = 1;
        blanks.add(blank1);
        
        // Blank 2: [is baking] in English, [đang] in Vietnamese
        BlankInfo blank2 = new BlankInfo();
        blank2.englishMarker = "[is baking]";
        blank2.vietnameseMarker = "[đang]";
        blank2.options = new String[]{"baking", "are baking", "is baking"};
        blank2.correctAnswer = "are baking";
        blank2.blankIndex = 2;
        blanks.add(blank2);
    }

    private void setupTexts() {
        // Setup English text with clickable highlights
        SpannableString englishSpannable = createClickableHighlightedText(currentEnglishText, true);
        tvEnglishText.setText(englishSpannable);
        tvEnglishText.setMovementMethod(LinkMovementMethod.getInstance());

        // Setup Vietnamese text with clickable highlights
        SpannableString vietnameseSpannable = createClickableHighlightedText(currentVietnameseText, false);
        tvVietnameseText.setText(vietnameseSpannable);
        tvVietnameseText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private SpannableString createClickableHighlightedText(String text, boolean isEnglish) {
        SpannableString spannable = new SpannableString(text);
        
        // Find and highlight all [text] patterns in red and make them clickable
        int startIndex = 0;
        int blankIndex = 0;
        
        while (true) {
            int start = text.indexOf(highlightStart, startIndex);
            if (start == -1) break;
            
            int end = text.indexOf(highlightEnd, start);
            if (end == -1) break;
            
            // Find which blank this is
            String marker = text.substring(start, end + 1);
            BlankInfo blank = findBlankByMarker(marker, isEnglish);
            
            if (blank != null) {
                // Highlight in red
                spannable.setSpan(
                    new ForegroundColorSpan(Color.RED),
                    start,
                    end + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                
                // Make clickable
                final BlankInfo finalBlank = blank;
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        showOptionsDialog(finalBlank);
                    }
                };
                spannable.setSpan(
                    clickableSpan,
                    start,
                    end + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            
            startIndex = end + 1;
        }
        
        return spannable;
    }

    private BlankInfo findBlankByMarker(String marker, boolean isEnglish) {
        for (BlankInfo blank : blanks) {
            if (isEnglish && blank.englishMarker.equals(marker)) {
                return blank;
            } else if (!isEnglish && blank.vietnameseMarker.equals(marker)) {
                return blank;
            }
        }
        return null;
    }

    private void showOptionsDialog(BlankInfo blank) {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_blank_options);
        
        LinearLayout optionsContainer = dialog.findViewById(R.id.options_container);
        optionsContainer.removeAllViews();
        
        // Create option buttons
        for (String option : blank.options) {
            Button optionButton = new Button(getContext());
            optionButton.setText(option);
            optionButton.setTextColor(Color.BLACK);
            optionButton.setBackgroundResource(R.drawable.bg_quiz_button);
            optionButton.setPadding(16, 12, 16, 12);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 8, 8, 8);
            optionButton.setLayoutParams(params);
            
            optionButton.setOnClickListener(v -> {
                selectAnswer(option, blank);
                dialog.dismiss();
            });
            
            optionsContainer.addView(optionButton);
        }
        
        dialog.show();
    }

    private void selectAnswer(String answer, BlankInfo blank) {
        boolean isCorrect = answer.equals(blank.correctAnswer);
        
        // Update selected answer
        if (blank.blankIndex == 1) {
            selectedAnswer1 = answer;
        } else {
            selectedAnswer2 = answer;
        }
        
        // Update text with selected answer
        if (blank.blankIndex == 1) {
            // Update English text
            currentEnglishText = currentEnglishText.replace(blank.englishMarker, answer);
            
            // Update Vietnamese text
            String vietnameseAnswer = answer.equals("Flour") ? "bột mì" : answer;
            currentVietnameseText = currentVietnameseText.replace(blank.vietnameseMarker, vietnameseAnswer);
        } else {
            // Update English text
            currentEnglishText = currentEnglishText.replace(blank.englishMarker, answer);
            
            // Update Vietnamese text
            String vietnameseAnswer = answer.equals("are baking") ? "đang" : answer;
            currentVietnameseText = currentVietnameseText.replace(blank.vietnameseMarker, vietnameseAnswer);
        }
        
        // Refresh text display
        setupTexts();
        
        // Show feedback
        if (isCorrect) {
            Toast.makeText(getContext(), "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Incorrect! Try again.", Toast.LENGTH_SHORT).show();
        }
        
        // Check if both answers are selected and correct
        if (selectedAnswer1 != null && selectedAnswer2 != null) {
            if (selectedAnswer1.equals(correctAnswer1) && selectedAnswer2.equals(correctAnswer2)) {
                btnNext.setVisibility(View.VISIBLE);
            }
        }
    }

    private void toggleVietnameseTranslation() {
        if (cardVietnamese.getVisibility() == View.VISIBLE) {
            cardVietnamese.setVisibility(View.GONE);
        } else {
            cardVietnamese.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToNextScreen() {
        // Navigate to Furniture Drag screen (final screen)
        if (getActivity() instanceof com.example.rise_of_city.ui.main.MainActivity) {
            FurnitureDragFragment furnitureFragment = new FurnitureDragFragment();
            Bundle args = new Bundle();
            args.putString("room_type", "bedroom");
            furnitureFragment.setArguments(args);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, furnitureFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    // Data class for blank information
    private static class BlankInfo {
        String englishMarker;
        String vietnameseMarker;
        String[] options;
        String correctAnswer;
        int blankIndex;
    }
}
