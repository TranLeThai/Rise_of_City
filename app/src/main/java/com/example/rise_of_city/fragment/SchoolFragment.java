package com.example.rise_of_city.fragment;

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
 * Fragment cho màn hình School Quiz
 * Quiz trong lớp học với background classroom
 */
public class SchoolFragment extends Fragment {

    private TextView tvTitle;
    private TextView tvQuestion;
    private Button btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4;
    private TextView tvTimer;
    private ProgressBar progressTimer;
    private ImageButton btnSettings;
    
    private String selectedAnswer;
    private boolean answerSelected = false;
    
    // Timer
    private Handler timerHandler;
    private Runnable timerRunnable;
    private int timeRemaining = 15; // 15 seconds
    private boolean timerRunning = false;
    
    // Quiz data
    private String correctAnswer = "A. Math";
    private String[] options = {"A. Math", "B. History", "C. English", "D. Music"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Set portrait orientation
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        
        View view = inflater.inflate(R.layout.fragment_school, container, false);
        
        initViews(view);
        setupQuestion();
        setupAnswerButtons();
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
        tvQuestion = view.findViewById(R.id.tv_question);
        btnAnswer1 = view.findViewById(R.id.btn_answer1);
        btnAnswer2 = view.findViewById(R.id.btn_answer2);
        btnAnswer3 = view.findViewById(R.id.btn_answer3);
        btnAnswer4 = view.findViewById(R.id.btn_answer4);
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
    
    private void setupQuestion() {
        tvQuestion.setText("Which subject do you study numbers in?");
        
        // Set answers
        btnAnswer1.setText(options[0]);
        btnAnswer2.setText(options[1]);
        btnAnswer3.setText(options[2]);
        btnAnswer4.setText(options[3]);
    }
    
    private void setupAnswerButtons() {
        View.OnClickListener answerClickListener = v -> {
            // Reset all buttons
            resetAnswerButtons();
            
            // Highlight selected button
            Button selectedBtn = (Button) v;
            selectedBtn.setBackgroundResource(R.drawable.bg_answer_green_selected);
            selectedAnswer = selectedBtn.getText().toString();
            answerSelected = true;
        };

        btnAnswer1.setOnClickListener(answerClickListener);
        btnAnswer2.setOnClickListener(answerClickListener);
        btnAnswer3.setOnClickListener(answerClickListener);
        btnAnswer4.setOnClickListener(answerClickListener);
    }
    
    private void resetAnswerButtons() {
        int defaultTextColor = 0xFF424242;
        btnAnswer1.setBackgroundResource(R.drawable.bg_answer_green);
        btnAnswer1.setTextColor(defaultTextColor);
        btnAnswer2.setBackgroundResource(R.drawable.bg_answer_green);
        btnAnswer2.setTextColor(defaultTextColor);
        btnAnswer3.setBackgroundResource(R.drawable.bg_answer_green);
        btnAnswer3.setTextColor(defaultTextColor);
        btnAnswer4.setBackgroundResource(R.drawable.bg_answer_green);
        btnAnswer4.setTextColor(defaultTextColor);
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
                    if (answerSelected) {
                        checkAnswer();
                    } else {
                        Toast.makeText(getContext(), "Hết thời gian!", Toast.LENGTH_SHORT).show();
                        stopTimer();
                    }
                }
            }
        };
    }
    
    private void checkAnswer() {
        boolean isCorrect = selectedAnswer.equals(correctAnswer);
        if (isCorrect) {
            Toast.makeText(getContext(), "Chính xác!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Sai rồi! Đáp án đúng là: " + correctAnswer, Toast.LENGTH_SHORT).show();
        }
        stopTimer();
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

