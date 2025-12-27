package com.example.rise_of_city.ui.quiz_fragment;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;

/**
 * Fragment cho màn hình Guess (Word Scramble)
 * Người dùng đoán từ từ các chữ cái bị xáo trộn
 */
public class GuessFragment extends Fragment {

    private TextView tvTitle;
    private TextView tvScrambledWord;
    private EditText etAnswer;
    private TextView tvTimer;
    private ProgressBar progressTimer;
    private ImageButton btnSettings;
    
    // Timer
    private Handler timerHandler;
    private Runnable timerRunnable;
    private int timeRemaining = 15; // 15 seconds
    private boolean timerRunning = false;
    
    // Quiz data
    private String correctAnswer = "English";
    private String scrambledWord = "n/g/E/h/l/i/s"; // Scrambled version

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guess, container, false);
        
        initViews(view);
        setupScrambledWord();
        setupAnswerInput();
        initTimer();
        startTimer();
        
        return view;
    }
    
    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tv_title);
        tvScrambledWord = view.findViewById(R.id.tv_scrambled_word);
        etAnswer = view.findViewById(R.id.et_answer);
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
    
    private void setupScrambledWord() {
        tvScrambledWord.setText(scrambledWord);
    }
    
    private void setupAnswerInput() {
        etAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String answer = s.toString().trim();
                // Auto-check when answer matches
                if (answer.equalsIgnoreCase(correctAnswer)) {
                    stopTimer();
                    Toast.makeText(getContext(), "Correct! The answer is " + correctAnswer, Toast.LENGTH_LONG).show();
                    etAnswer.setEnabled(false);
                }
            }
        });
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
                    etAnswer.setEnabled(false);
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
