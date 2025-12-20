package com.example.rise_of_city.fragment;

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

/**
 * Fragment cho màn hình Bakery, Coffee
 * Hiển thị hình ảnh bakery và các từ vựng liên quan
 */
public class BakeryCoffeeFragment extends Fragment {

    private TextView tvTitle;
    private ImageView ivBakeryImage;
    private TextView tvTimer;
    private ProgressBar progressTimer;
    private ImageButton btnSettings;
    private Button btnNext;
    
    // Timer
    private Handler timerHandler;
    private Runnable timerRunnable;
    private int timeRemaining = 15; // 15 seconds
    private boolean timerRunning = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bakery_coffee, container, false);
        
        initViews(view);
        setupImage();
        initTimer();
        startTimer();
        
        return view;
    }
    
    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tv_title);
        ivBakeryImage = view.findViewById(R.id.iv_bakery_image);
        tvTimer = view.findViewById(R.id.tv_timer);
        progressTimer = view.findViewById(R.id.progress_timer);
        btnSettings = view.findViewById(R.id.btn_settings);
        btnNext = view.findViewById(R.id.btn_next);
        
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Settings", Toast.LENGTH_SHORT).show();
            });
        }
        
        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Next", Toast.LENGTH_SHORT).show();
            });
        }
        
        timerHandler = new Handler(Looper.getMainLooper());
    }
    
    private void setupImage() {
        // Load hình ảnh bakery/kitchen với chef từ drawable
        // Sử dụng bakerchef.png (hình ảnh kitchen với chef)
        try {
            ivBakeryImage.setImageResource(R.drawable.bakerchef);
        } catch (Exception e) {
            // Fallback nếu không tìm thấy hình ảnh
            try {
                ivBakeryImage.setImageResource(R.drawable.kitchen_full);
            } catch (Exception e2) {
                try {
                    ivBakeryImage.setImageResource(R.drawable.kitchen);
                } catch (Exception e3) {
                    ivBakeryImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
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
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTimer();
    }
}

