package com.example.rise_of_city.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.ui.quiz.VocabularyQuizActivity;

/**
 * Fragment hiển thị quiz component - màn hình đầu tiên của New Screen
 */
public class QuizFragment extends Fragment {

    private TextView tvQuestion;
    private Button btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4;
    private Button btnCheck;
    private Button btnNext;
    private TextView tvTimer;
    private ProgressBar progressTimer;
    private ImageView ivFeedback1, ivFeedback2, ivFeedback3, ivFeedback4;
    
    private String selectedAnswer;
    private boolean answerSelected = false;
    
    // Timer
    private Handler timerHandler;
    private Runnable timerRunnable;
    private int timeRemaining = 15; // 15 seconds
    private boolean timerRunning = false;
    
    // Sample quiz data
    private String correctAnswer = "A. Math";
    private String[] options = {"A. Math", "B. History", "C. English", "D. Music"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);
        
        initViews(view);
        setupQuestion();
        setupAnswerButtons();
        setupCheckButton();
        initTimer();
        startTimer();
        
        return view;
    }
    
    private void initViews(View view) {
        ImageButton btnClose = view.findViewById(R.id.btn_close);
        tvQuestion = view.findViewById(R.id.tv_question);
        btnAnswer1 = view.findViewById(R.id.btn_answer1);
        btnAnswer2 = view.findViewById(R.id.btn_answer2);
        btnAnswer3 = view.findViewById(R.id.btn_answer3);
        btnAnswer4 = view.findViewById(R.id.btn_answer4);
        btnCheck = view.findViewById(R.id.btn_check);
        btnNext = view.findViewById(R.id.btn_next);
        tvTimer = view.findViewById(R.id.tv_timer);
        progressTimer = view.findViewById(R.id.progress_timer);
        ivFeedback1 = view.findViewById(R.id.iv_feedback1);
        ivFeedback2 = view.findViewById(R.id.iv_feedback2);
        ivFeedback3 = view.findViewById(R.id.iv_feedback3);
        ivFeedback4 = view.findViewById(R.id.iv_feedback4);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                // Navigate back or close
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }
        
        // Setup Next button
        btnNext.setOnClickListener(v -> {
            // Navigate to full quiz activity or next screen
            Intent intent = new Intent(getContext(), VocabularyQuizActivity.class);
            startActivity(intent);
        });
        
        timerHandler = new Handler(Looper.getMainLooper());
    }
    
    private void setupQuestion() {
        // Reset UI state
        hideNextButton();
        resetAnswerButtons();
        enableAllButtons();
        answerSelected = false;
        
        // Set question
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
            
            // Show feedback immediately
            showAnswerFeedback(selectedBtn);
        };

        btnAnswer1.setOnClickListener(answerClickListener);
        btnAnswer2.setOnClickListener(answerClickListener);
        btnAnswer3.setOnClickListener(answerClickListener);
        btnAnswer4.setOnClickListener(answerClickListener);
    }
    
    private void showAnswerFeedback(Button selectedBtn) {
        // Hide all feedback icons first
        hideAllFeedbackIcons();
        
        // Determine if answer is correct
        boolean isCorrect = selectedAnswer.equals(correctAnswer);
        
        // Show feedback icon for selected button
        ImageView feedbackIcon = null;
        if (selectedBtn == btnAnswer1) {
            feedbackIcon = ivFeedback1;
        } else if (selectedBtn == btnAnswer2) {
            feedbackIcon = ivFeedback2;
        } else if (selectedBtn == btnAnswer3) {
            feedbackIcon = ivFeedback3;
        } else if (selectedBtn == btnAnswer4) {
            feedbackIcon = ivFeedback4;
        }
        
        if (feedbackIcon != null) {
            feedbackIcon.setVisibility(View.VISIBLE);
            if (isCorrect) {
                feedbackIcon.setImageResource(R.drawable.ic_check_large);
            } else {
                feedbackIcon.setImageResource(R.drawable.ic_close_large);
            }
        }
    }
    
    private void hideAllFeedbackIcons() {
        ivFeedback1.setVisibility(View.GONE);
        ivFeedback2.setVisibility(View.GONE);
        ivFeedback3.setVisibility(View.GONE);
        ivFeedback4.setVisibility(View.GONE);
    }

    private void resetAnswerButtons() {
        int defaultTextColor = 0xFF424242; // Màu đen cho chữ trên nền xanh lá nhạt
        btnAnswer1.setBackgroundResource(R.drawable.bg_answer_green);
        btnAnswer1.setTextColor(defaultTextColor);
        btnAnswer2.setBackgroundResource(R.drawable.bg_answer_green);
        btnAnswer2.setTextColor(defaultTextColor);
        btnAnswer3.setBackgroundResource(R.drawable.bg_answer_green);
        btnAnswer3.setTextColor(defaultTextColor);
        btnAnswer4.setBackgroundResource(R.drawable.bg_answer_green);
        btnAnswer4.setTextColor(defaultTextColor);
        
        hideAllFeedbackIcons();
    }
    
    private void setupCheckButton() {
        btnCheck.setOnClickListener(v -> {
            if (!answerSelected) {
                Toast.makeText(getContext(), "Vui lòng chọn một đáp án!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Stop timer
            stopTimer();

            // Check answer
            boolean isCorrect = selectedAnswer.equals(correctAnswer);
            
            if (isCorrect) {
                disableAllButtons();
                showNextButton();
                Toast.makeText(getContext(), "Chính xác!", Toast.LENGTH_SHORT).show();
            } else {
                disableAllButtons();
                showNextButton();
                Toast.makeText(getContext(), "Sai rồi! Đáp án đúng là: " + correctAnswer, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void disableAllButtons() {
        btnAnswer1.setEnabled(false);
        btnAnswer2.setEnabled(false);
        btnAnswer3.setEnabled(false);
        btnAnswer4.setEnabled(false);
        btnCheck.setEnabled(false);
    }
    
    private void enableAllButtons() {
        btnAnswer1.setEnabled(true);
        btnAnswer2.setEnabled(true);
        btnAnswer3.setEnabled(true);
        btnAnswer4.setEnabled(true);
        btnCheck.setEnabled(true);
    }
    
    private void showNextButton() {
        btnCheck.setVisibility(View.GONE);
        btnNext.setVisibility(View.VISIBLE);
    }
    
    private void hideNextButton() {
        btnCheck.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.GONE);
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
                        btnCheck.performClick();
                    } else {
                        Toast.makeText(getContext(), "Hết thời gian!", Toast.LENGTH_SHORT).show();
                        stopTimer();
                    }
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

