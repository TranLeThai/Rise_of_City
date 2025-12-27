package com.example.rise_of_city.ui.game.ingame;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.Question;
// Đảm bảo chỉ sử dụng Type từ LessonQuestion
import com.example.rise_of_city.ui.quiz_fragment.GuessFragment;
import com.example.rise_of_city.ui.quiz_fragment.GuessImageFragment;
import com.example.rise_of_city.ui.quiz_fragment.ImageMatchFragment;
import com.example.rise_of_city.ui.quiz_fragment.LectureFragment;
import com.example.rise_of_city.ui.quiz_fragment.WordMatchFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LessonActivity extends AppCompatActivity {
    private List<Question> questionList = new ArrayList<>();
    private int currentIndex = 0;
    private CountDownTimer countDownTimer;
    private TextView tvTimer;
    private ProgressBar lessonProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        tvTimer = findViewById(R.id.tvTimer);
        lessonProgressBar = findViewById(R.id.lessonProgressBar);

        setupQuestionList();
        displayQuestion(currentIndex);
    }

    private void setupQuestionList() {
        // 1. Lấy danh sách các loại câu hỏi dành riêng cho bài học (LECTURE, WORD_MATCH,...)
        Type[] types = Type.values();
        Random random = new Random();

        // 2. Tạo 10 câu hỏi ngẫu nhiên bằng cách sử dụng đúng Enum Type của bài học
        for (int i = 1; i <= 10; i++) {
            Type randomType = types[random.nextInt(types.length)];
            // Quan trọng: Đối tượng Question phải nhận kiểu dữ liệu là Type
            questionList.add(new Question(i, randomType, "Nội dung bài học " + i));
        }

        // Cập nhật độ dài tối đa cho ProgressBar
        if (lessonProgressBar != null) {
            lessonProgressBar.setMax(questionList.size());
        }
    }

    public void displayQuestion(int index) {
        if (index >= questionList.size()) {
            Toast.makeText(this, "Chúc mừng Thị trưởng đã hoàn thành bài học!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Question question = questionList.get(index);
        Fragment fragment = null;

        // 3. Sử dụng switch-case dựa trên enum Type của bài học
        switch (question.getType()) {
            case LECTURE:
                fragment = new LectureFragment();
                break;
            case WORD_MATCH:
                fragment = new WordMatchFragment();
                break;
            case IMAGE_MATCH:
                fragment = new ImageMatchFragment();
                break;
            case GUESS:
                fragment = new GuessFragment();
                break;
            case GUESS_IMAGE:
                fragment = new GuessImageFragment();
                break;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }

        updateUI(index);
        startTimer();
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText(String.format("00:%02d", millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                nextQuestion();
            }
        }.start();
    }

    public void nextQuestion() {
        currentIndex++;
        displayQuestion(currentIndex);
    }

    private void updateUI(int index) {
        if (lessonProgressBar != null) {
            lessonProgressBar.setProgress(index + 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}