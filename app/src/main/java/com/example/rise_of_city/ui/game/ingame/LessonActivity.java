package com.example.rise_of_city.ui.game.ingame;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.ui.quiz_fragment.TEXT_INTERACT.LectureFragment;


public class LessonActivity extends AppCompatActivity {

    // UI Components
    private TextView tvTimer;
    private ProgressBar lessonProgressBar;
    private ImageView[] ivHearts;
    private ImageButton btnSettings;

    // Game Logic State
    private int currentHearts = 3;
    private int currentProgress = 1;
    private final int MAX_PROGRESS = 10;
    private CountDownTimer countDownTimer;
    private final long TIME_LIMIT = 20000; // 20 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        initViews();
        setupGame();

        // Chạy thử Fragment Lecture đầu tiên
        loadFragment(new LectureFragment());
    }

    private void initViews() {
        tvTimer = findViewById(R.id.tvTimer);
        lessonProgressBar = findViewById(R.id.lessonProgressBar);
        btnSettings = findViewById(R.id.btnSettings);

        ivHearts = new ImageView[]{
                findViewById(R.id.ivHeart1),
                findViewById(R.id.ivHeart2),
                findViewById(R.id.ivHeart3)
        };

        btnSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Mở bảng cài đặt thành phố...", Toast.LENGTH_SHORT).show();
            // Logic mở Dialog Settings có thể thêm ở đây
        });
    }

    private void setupGame() {
        lessonProgressBar.setMax(MAX_PROGRESS);
        lessonProgressBar.setProgress(currentProgress);
        startCountdown();
    }

    /**
     * Bắt đầu đếm ngược 20 giây cho mỗi câu hỏi
     */
    public void startCountdown() {
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(TIME_LIMIT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                handleWrongAnswer(); // Hết giờ tính là một câu sai
                Toast.makeText(LessonActivity.this, "Hết thời gian thanh tra!", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    /**
     * Xử lý khi Thị trưởng trả lời đúng
     */
    public void handleCorrectAnswer() {
        if (currentProgress < MAX_PROGRESS) {
            currentProgress++;
            lessonProgressBar.setProgress(currentProgress);
            Toast.makeText(this, "Hợp đồng hợp lệ! +1 Tiến độ", Toast.LENGTH_SHORT).show();

            // Tải câu hỏi tiếp theo (Ở đây là load lại fragment để test)
            loadFragment(new LectureFragment());
            startCountdown();
        } else {
            Toast.makeText(this, "Chúc mừng Thị trưởng đã hoàn thành bài học!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Xử lý khi trả lời sai hoặc hết giờ
     */
    public void handleWrongAnswer() {
        if (currentHearts > 0) {
            currentHearts--;
            ivHearts[currentHearts].setImageResource(R.drawable.ic_heart_empty); // Đổi sang icon tim rỗng

            if (currentHearts == 0) {
                Toast.makeText(this, "Thành phố đình trệ! Bạn đã hết lượt.", Toast.LENGTH_LONG).show();
                finish(); // Kết thúc màn chơi
            } else {
                // Chuyển câu tiếp theo dù sai để tránh kẹt
                loadFragment(new LectureFragment());
                startCountdown();
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}