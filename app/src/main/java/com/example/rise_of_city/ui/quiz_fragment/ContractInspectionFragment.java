package com.example.rise_of_city.ui.quiz_fragment;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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
 * Fragment cho màn hình Thanh tra hợp đồng (Contract Inspection)
 * Màn hình kiểm tra lỗi trong hợp đồng bakery với 3 đáp án để sửa
 */
public class ContractInspectionFragment extends Fragment {

    private TextView tvTitle;
    private TextView tvDocumentText;
    private TextView tvTimer;
    private ProgressBar progressTimer;
    private Button btnAnswer1, btnAnswer2, btnAnswer3;
    private ImageButton btnSettings;
    private ImageButton btnTranslate;
    
    private String selectedAnswer;
    private boolean answerSelected = false;
    private boolean isVietnamese = false; // Trạng thái ngôn ngữ
    
    // Timer
    private Handler timerHandler;
    private Runnable timerRunnable;
    private int timeRemaining = 15; // 15 seconds
    private boolean timerRunning = false;
    
    // Quiz data
    private String correctAnswer = "Flour, are baking";
    private String[] options = {
        "Flour, are baking",
        "Flower, is baking",
        "Flour, is baking"
    };
    
    // Document text với lỗi - Tiếng Anh
    private String documentTextEN = "Dear Mayor,\n\nThe bakery project is ready for launch. We have imported enough [flower] to bake our signature bread. Currently, our professional chefs [is baking] the first batches. Please sign this document so we can open the doors to our citizens.";
    
    // Document text - Tiếng Việt
    private String documentTextVI = "Kính gửi Thị trưởng,\n\nDự án tiệm bánh đã sẵn sàng để khởi động. Chúng tôi đã nhập khẩu đủ [hoa] để nướng bánh mì đặc trưng của chúng tôi. Hiện tại, các đầu bếp chuyên nghiệp của chúng tôi [đang nướng] các lô đầu tiên. Vui lòng ký tài liệu này để chúng tôi có thể mở cửa cho công dân của chúng tôi.";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Set landscape orientation
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        
        View view = inflater.inflate(R.layout.fragment_contract_inspection, container, false);
        
        initViews(view);
        setupDocumentText();
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
        tvDocumentText = view.findViewById(R.id.tv_document_text);
        tvTimer = view.findViewById(R.id.tv_timer);
        progressTimer = view.findViewById(R.id.progress_timer);
        btnAnswer1 = view.findViewById(R.id.btn_answer1);
        btnAnswer2 = view.findViewById(R.id.btn_answer2);
        btnAnswer3 = view.findViewById(R.id.btn_answer3);
        btnSettings = view.findViewById(R.id.btn_settings);
        btnTranslate = view.findViewById(R.id.btn_translate);
        
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                // Settings action - có thể mở settings dialog
                Toast.makeText(getContext(), "Settings", Toast.LENGTH_SHORT).show();
            });
        }
        
        // Click vào speech bubble để chuyển đổi ngôn ngữ
        if (btnTranslate != null) {
            btnTranslate.setOnClickListener(v -> {
                isVietnamese = !isVietnamese;
                setupDocumentText();
            });
        }
        
        timerHandler = new Handler(Looper.getMainLooper());
    }
    
    private void setupDocumentText() {
        String documentText = isVietnamese ? documentTextVI : documentTextEN;
        
        // Tạo SpannableString để highlight các lỗi màu đỏ
        SpannableString spannable = new SpannableString(documentText);
        
        if (isVietnamese) {
            // Highlight [hoa] màu đỏ
            int flowerStart = documentText.indexOf("[hoa]");
            int flowerEnd = flowerStart + "[hoa]".length();
            if (flowerStart != -1) {
                spannable.setSpan(
                    new ForegroundColorSpan(Color.RED),
                    flowerStart,
                    flowerEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            
            // Highlight [đang nướng] màu đỏ
            int bakingStart = documentText.indexOf("[đang nướng]");
            int bakingEnd = bakingStart + "[đang nướng]".length();
            if (bakingStart != -1) {
                spannable.setSpan(
                    new ForegroundColorSpan(Color.RED),
                    bakingStart,
                    bakingEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        } else {
            // Highlight [flower] màu đỏ
            int flowerStart = documentText.indexOf("[flower]");
            int flowerEnd = flowerStart + "[flower]".length();
            if (flowerStart != -1) {
                spannable.setSpan(
                    new ForegroundColorSpan(Color.RED),
                    flowerStart,
                    flowerEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            
            // Highlight [is baking] màu đỏ
            int bakingStart = documentText.indexOf("[is baking]");
            int bakingEnd = bakingStart + "[is baking]".length();
            if (bakingStart != -1) {
                spannable.setSpan(
                    new ForegroundColorSpan(Color.RED),
                    bakingStart,
                    bakingEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }
        
        tvDocumentText.setText(spannable);
    }
    
    private void setupAnswerButtons() {
        // Set đáp án
        btnAnswer1.setText(options[0]);
        btnAnswer2.setText(options[1]);
        btnAnswer3.setText(options[2]);
        
        View.OnClickListener answerClickListener = v -> {
            // Reset tất cả buttons
            resetAnswerButtons();
            
            // Highlight button được chọn
            Button selectedBtn = (Button) v;
            selectedBtn.setBackgroundResource(R.drawable.bg_answer_overlay_selected);
            selectedAnswer = selectedBtn.getText().toString();
            answerSelected = true;
            
            // Tự động check đáp án khi chọn
            checkAnswer();
        };

        btnAnswer1.setOnClickListener(answerClickListener);
        btnAnswer2.setOnClickListener(answerClickListener);
        btnAnswer3.setOnClickListener(answerClickListener);
    }
    
    private void resetAnswerButtons() {
        btnAnswer1.setBackgroundResource(R.drawable.bg_answer_overlay);
        btnAnswer1.setTextColor(Color.WHITE);
        btnAnswer2.setBackgroundResource(R.drawable.bg_answer_overlay);
        btnAnswer2.setTextColor(Color.WHITE);
        btnAnswer3.setBackgroundResource(R.drawable.bg_answer_overlay);
        btnAnswer3.setTextColor(Color.WHITE);
    }
    
    private void checkAnswer() {
        if (!answerSelected) {
            return;
        }

        // Stop timer
        stopTimer();

        // Check answer
        boolean isCorrect = selectedAnswer.equals(correctAnswer);
        
        if (isCorrect) {
            disableAllButtons();
            Toast.makeText(getContext(), "Chính xác! Bạn đã sửa đúng các lỗi trong hợp đồng.", Toast.LENGTH_LONG).show();
        } else {
            disableAllButtons();
            Toast.makeText(getContext(), "Sai rồi! Đáp án đúng là: " + correctAnswer, Toast.LENGTH_LONG).show();
        }
    }
    
    private void disableAllButtons() {
        btnAnswer1.setEnabled(false);
        btnAnswer2.setEnabled(false);
        btnAnswer3.setEnabled(false);
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

