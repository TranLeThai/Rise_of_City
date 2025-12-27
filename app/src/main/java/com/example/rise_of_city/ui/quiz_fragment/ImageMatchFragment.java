package com.example.rise_of_city.ui.quiz_fragment;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;

/**
 * Fragment cho màn hình Image-Match
 * Người dùng nối hình ảnh với text tương ứng
 */
public class ImageMatchFragment extends Fragment {

    private TextView tvTitle;
    private TextView tvTimer;
    private ProgressBar progressTimer;
    private ImageButton btnSettings;
    
    private Button btnImage1, btnImage2;
    private Button btnText1, btnText2;
    
    private Button selectedImage = null;
    private Button selectedText = null;
    
    private int matchedCount = 0;
    
    // Timer
    private Handler timerHandler;
    private Runnable timerRunnable;
    private int timeRemaining = 15; // 15 seconds
    private boolean timerRunning = false;
    
    // Matching pairs: Image index -> Text index
    private int[][] correctPairs = {
        {0, 0}, // Image 1 matches Text 1
        {1, 1}  // Image 2 matches Text 2
    };
    
    // Sample data
    private String[] imageLabels = {"Image 1", "Image 2"};
    private String[] textLabels = {"Text 1", "Text 2"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Set portrait orientation
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        
        View view = inflater.inflate(R.layout.fragment_image_match, container, false);
        
        initViews(view);
        setupButtons();
        initTimer();
        startTimer();
        
        return view;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Reset orientation when leaving
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
        stopTimer();
    }
    
    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tv_title);
        tvTimer = view.findViewById(R.id.tv_timer);
        progressTimer = view.findViewById(R.id.progress_timer);
        btnSettings = view.findViewById(R.id.btn_settings);
        
        btnImage1 = view.findViewById(R.id.btn_image1);
        btnImage2 = view.findViewById(R.id.btn_image2);
        btnText1 = view.findViewById(R.id.btn_text1);
        btnText2 = view.findViewById(R.id.btn_text2);
        
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Settings", Toast.LENGTH_SHORT).show();
            });
        }
        
        timerHandler = new Handler(Looper.getMainLooper());
    }
    
    private void setupButtons() {
        // Set labels
        btnImage1.setText(imageLabels[0]);
        btnImage2.setText(imageLabels[1]);
        btnText1.setText(textLabels[0]);
        btnText2.setText(textLabels[1]);
        
        // Image buttons click listeners
        btnImage1.setOnClickListener(v -> onImageSelected(btnImage1, 0));
        btnImage2.setOnClickListener(v -> onImageSelected(btnImage2, 1));
        
        // Text buttons click listeners
        btnText1.setOnClickListener(v -> onTextSelected(btnText1, 0));
        btnText2.setOnClickListener(v -> onTextSelected(btnText2, 1));
    }
    
    private void onImageSelected(Button button, int imageIndex) {
        // Reset all image buttons
        resetImageButtons();
        
        if (selectedImage == button) {
            // Click lại cùng button → bỏ chọn
            selectedImage = null;
        } else {
            // Select new image
            selectedImage = button;
            button.setBackgroundResource(R.drawable.bg_answer_green_selected);
            
            // Nếu đã chọn text, thử match ngay
            if (selectedText != null) {
                int textIndex = getTextIndex(selectedText);
                checkMatch(imageIndex, textIndex);
            }
        }
    }
    
    private void onTextSelected(Button button, int textIndex) {
        // Reset all text buttons
        resetTextButtons();
        
        if (selectedText == button) {
            // Click lại cùng button → bỏ chọn
            selectedText = null;
        } else {
            // Select new text
            selectedText = button;
            button.setBackgroundResource(R.drawable.bg_answer_green_selected);
            
            // Nếu đã chọn image, thử match ngay
            if (selectedImage != null) {
                int imageIndex = getImageIndex(selectedImage);
                checkMatch(imageIndex, textIndex);
            }
        }
    }
    
    private int getImageIndex(Button button) {
        if (button == btnImage1) return 0;
        if (button == btnImage2) return 1;
        return -1;
    }
    
    private int getTextIndex(Button button) {
        if (button == btnText1) return 0;
        if (button == btnText2) return 1;
        return -1;
    }
    
    private void resetImageButtons() {
        btnImage1.setBackgroundResource(R.drawable.bg_answer_green);
        btnImage2.setBackgroundResource(R.drawable.bg_answer_green);
    }
    
    private void resetTextButtons() {
        btnText1.setBackgroundResource(R.drawable.bg_answer_green);
        btnText2.setBackgroundResource(R.drawable.bg_answer_green);
    }
    
    private void checkMatch(int imageIndex, int textIndex) {
        // Check if it's a correct match
        boolean isCorrect = false;
        for (int[] pair : correctPairs) {
            if (pair[0] == imageIndex && pair[1] == textIndex) {
                isCorrect = true;
                break;
            }
        }
        
        if (isCorrect) {
            Toast.makeText(getContext(), "Correct match!", Toast.LENGTH_SHORT).show();
            matchedCount++;
            
            // Mark as matched (disable buttons)
            if (imageIndex == 0) btnImage1.setEnabled(false);
            if (imageIndex == 1) btnImage2.setEnabled(false);
            if (textIndex == 0) btnText1.setEnabled(false);
            if (textIndex == 1) btnText2.setEnabled(false);
            
            // Change background to green for matched
            if (imageIndex == 0) btnImage1.setBackgroundResource(R.drawable.bg_answer_button_correct);
            if (imageIndex == 1) btnImage2.setBackgroundResource(R.drawable.bg_answer_button_correct);
            if (textIndex == 0) btnText1.setBackgroundResource(R.drawable.bg_answer_button_correct);
            if (textIndex == 1) btnText2.setBackgroundResource(R.drawable.bg_answer_button_correct);
            
            // Clear selection
            selectedImage = null;
            selectedText = null;
            
            // Check completion
            checkCompletion();
        } else {
            Toast.makeText(getContext(), "Incorrect match. Try again!", Toast.LENGTH_SHORT).show();
            selectedImage = null;
            selectedText = null;
            resetImageButtons();
            resetTextButtons();
        }
    }
    
    private void checkCompletion() {
        if (matchedCount >= correctPairs.length) {
            stopTimer();
            Toast.makeText(getContext(), "All matches completed!", Toast.LENGTH_LONG).show();
        }
    }
    
    private void initTimer() {
        timeRemaining = 15;
        progressTimer.setMax(15);
        progressTimer.setProgress(15);
        updateTimerDisplay();
        
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (timeRemaining > 0 && timerRunning) {
                    timeRemaining--;
                    updateTimerDisplay();
                    timerHandler.postDelayed(this, 1000);
                } else if (timeRemaining == 0 && timerRunning) {
                    // Time's up
                    Toast.makeText(getContext(), "Hết thời gian!", Toast.LENGTH_SHORT).show();
                    stopTimer();
                }
            }
        };
    }
    
    private void updateTimerDisplay() {
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        String timeString = String.format("%02d:%02d", minutes, seconds);
        tvTimer.setText(timeString);
        progressTimer.setProgress(timeRemaining);
    }
    
    private void startTimer() {
        if (!timerRunning) {
            timerRunning = true;
            timerHandler.post(timerRunnable);
        }
    }
    
    private void stopTimer() {
        timerRunning = false;
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
    
}

