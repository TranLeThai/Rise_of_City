package com.example.rise_of_city.ui.quiz;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;

/**
 * Fragment cho màn hình Guess-Image
 * Người dùng đoán từ dựa trên hình ảnh
 */
public class GuessImageFragment extends Fragment {

    private TextView tvTitle;
    private ImageView ivImage;
    private EditText etAnswer1, etAnswer2, etAnswer3;
    private TextView tvTimer;
    private ProgressBar progressTimer;
    private ImageButton btnSettings;
    
    // Timer
    private Handler timerHandler;
    private Runnable timerRunnable;
    private int timeRemaining = 15; // 15 seconds
    private boolean timerRunning = false;
    
    // Quiz data
    private String correctAnswer = "Mountain";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guess_image, container, false);
        
        initViews(view);
        setupImage();
        setupAnswerInputs();
        initTimer();
        startTimer();
        
        return view;
    }
    
    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tv_title);
        ivImage = view.findViewById(R.id.iv_image);
        etAnswer1 = view.findViewById(R.id.et_answer1);
        etAnswer2 = view.findViewById(R.id.et_answer2);
        etAnswer3 = view.findViewById(R.id.et_answer3);
        tvTimer = view.findViewById(R.id.tv_timer);
        progressTimer = view.findViewById(R.id.progress_timer);
        btnSettings = view.findViewById(R.id.btn_settings);
        
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Settings", Toast.LENGTH_SHORT).show();
            });
        }
        
        timerHandler = new Handler(Looper.getMainLooper());
    }
    
    private void setupImage() {
        // Set placeholder image - có thể load từ resource hoặc URL
        // ivImage.setImageResource(R.drawable.placeholder_landscape);
        // Hoặc dùng placeholder icon
        ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
    }
    
    private void setupAnswerInputs() {
        // Setup text watchers for all answer fields
        TextWatcher answerWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkAnswer(s.toString().trim());
            }
        };
        
        etAnswer1.addTextChangedListener(answerWatcher);
        etAnswer2.addTextChangedListener(answerWatcher);
        etAnswer3.addTextChangedListener(answerWatcher);
    }
    
    private void checkAnswer(String answer) {
        if (answer.equalsIgnoreCase(correctAnswer)) {
            stopTimer();
            Toast.makeText(getContext(), "Correct! The answer is " + correctAnswer, Toast.LENGTH_LONG).show();
            // Disable all inputs
            etAnswer1.setEnabled(false);
            etAnswer2.setEnabled(false);
            etAnswer3.setEnabled(false);
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
                    Toast.makeText(getContext(), "Hết thời gian! Đáp án đúng là: " + correctAnswer, Toast.LENGTH_LONG).show();
                    stopTimer();
                    etAnswer1.setEnabled(false);
                    etAnswer2.setEnabled(false);
                    etAnswer3.setEnabled(false);
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
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTimer();
    }
}

