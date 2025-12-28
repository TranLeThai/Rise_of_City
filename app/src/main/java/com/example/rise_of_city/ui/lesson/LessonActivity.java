package com.example.rise_of_city.ui.lesson;

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
import com.example.rise_of_city.data.model.learning.JsonReader;
import com.example.rise_of_city.data.model.learning.quiz.BaseQuestion;
import com.example.rise_of_city.data.model.learning.quiz.CHOICE.ChoiceQuestion;
import com.example.rise_of_city.data.model.learning.quiz.DECISION.TrueFalseQuestion;
import com.example.rise_of_city.data.model.learning.quiz.INPUT.WritingQuestion;
import com.example.rise_of_city.data.model.learning.quiz.LISTENING.ListeningQuestion;
import com.example.rise_of_city.data.model.learning.quiz.MATCHING.MatchingIMGQuestion;
import com.example.rise_of_city.data.model.learning.quiz.MATCHING.MatchingTextQuestion;
import com.example.rise_of_city.data.model.learning.quiz.ORDERING.SentenceOrderQuestion;
import com.example.rise_of_city.data.model.learning.quiz.ORDERING.WordOrderQuestion;
import com.example.rise_of_city.data.model.learning.quiz.TEXT_INTERACT.LectureQuestion;
import com.example.rise_of_city.ui.quiz_fragment.CHOICE.ChoiceFragment;
import com.example.rise_of_city.ui.quiz_fragment.DECISION.TrueFalseFragment;
import com.example.rise_of_city.ui.quiz_fragment.INPUT.WritingFragment;
import com.example.rise_of_city.ui.quiz_fragment.LISTENING.ListeningFragment;
import com.example.rise_of_city.ui.quiz_fragment.MATCHING.MatchingTextFragment;
import com.example.rise_of_city.ui.quiz_fragment.MATCHING.MatchingIMGFragment;
import com.example.rise_of_city.ui.quiz_fragment.ORDERING.OrderingFragment;
import com.example.rise_of_city.ui.quiz_fragment.TEXT_INTERACT.LectureFragment;

import java.util.List;

public class LessonActivity extends AppCompatActivity {

    // UI Components
    private TextView tvTimer;
    private ProgressBar lessonProgressBar;
    private ImageView[] ivHearts;
    private ImageButton btnSettings;

    // Game Logic State
    private int currentHearts = 3;
    private int currentQuestionIndex = 0;
    private List<BaseQuestion> questionList;
    private CountDownTimer countDownTimer;
    private final long TIME_LIMIT = 20000; // 20 giây mỗi câu

    // Tên file JSON (ví dụ: House_lv1.json, School_lv1.json, ...)
    private String lessonFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        initViews();

        // LẤY TÊN BÀI HỌC TỪ INTENT (key: "lessonName")
        String lessonName = getIntent().getStringExtra("lessonName");

        // Fallback nếu không có (ví dụ test trực tiếp activity)
        if (lessonName == null || lessonName.isEmpty()) {
            lessonName = "House_lv1"; // bài mặc định
        }

        // LUÔN THÊM ĐUÔI .json ĐỂ ĐẢM BẢO LOAD ĐÚNG FILE TRONG ASSETS
        lessonFileName = lessonName + ".json";

        // Load dữ liệu bài học
        JsonReader jsonReader = new JsonReader(this);
        questionList = jsonReader.readLessonFromJson(lessonFileName);

        if (questionList != null && !questionList.isEmpty()) {
            setupGame();
            displayQuestion(currentQuestionIndex);
        } else {
            Toast.makeText(this, "Không thể tải dữ liệu bài học: " + lessonFileName, Toast.LENGTH_LONG).show();
            finish(); // Thoát activity nếu lỗi
        }
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

        btnSettings.setOnClickListener(v ->
                Toast.makeText(this, "Mở bảng cài đặt thành phố...", Toast.LENGTH_SHORT).show());
    }

    private void setupGame() {
        lessonProgressBar.setMax(questionList.size());
        lessonProgressBar.setProgress(1);
        startCountdown();
    }

    private void displayQuestion(int index) {
        if (index >= questionList.size()) {
            Toast.makeText(this, "Hoàn thành bài học! Công trình đã sẵn sàng nâng cấp.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        BaseQuestion question = questionList.get(index);
        Fragment fragment = null;

        switch (question.getType()) {
            case LECTURE:
                fragment = LectureFragment.newInstance((LectureQuestion) question);
                break;
            case CHOICE:
                fragment = ChoiceFragment.newInstance((ChoiceQuestion) question);
                break;
            case MATCHINGTEXT:
                fragment = MatchingTextFragment.newInstance((MatchingTextQuestion) question);
                break;
            case MATCHINGIMG:
                fragment = MatchingIMGFragment.newInstance((MatchingIMGQuestion) question);
                break;
            case INPUT:
                fragment = WritingFragment.newInstance((WritingQuestion) question);
                break;
            case DECISION:
                fragment = TrueFalseFragment.newInstance((TrueFalseQuestion) question);
                break;
            case SENTENCEORDERING:
                fragment = OrderingFragment.newInstance((SentenceOrderQuestion) question);
                break;
            case WORDORDERING:
                fragment = OrderingFragment.newInstance((WordOrderQuestion) question);
                break;
            case LISTENING:
                fragment = ListeningFragment.newInstance((ListeningQuestion) question);
                break;
            default:
                Toast.makeText(this, "Loại câu hỏi không hỗ trợ!", Toast.LENGTH_SHORT).show();
                break;
        }

        if (fragment != null) {
            loadFragment(fragment);
            lessonProgressBar.setProgress(index + 1);
            startCountdown();
        }
    }

    public void startCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(TIME_LIMIT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                handleWrongAnswer();
            }
        }.start();
    }

    public void handleCorrectAnswer() {
        currentQuestionIndex++;
        Toast.makeText(this, "Đúng rồi!", Toast.LENGTH_SHORT).show();
        displayQuestion(currentQuestionIndex);
    }

    public void handleWrongAnswer() {
        if (currentHearts > 0) {
            currentHearts--;
            ivHearts[currentHearts].setImageResource(R.drawable.ic_heart_empty); // cần có icon này

            if (currentHearts == 0) {
                Toast.makeText(this, "Hết tim! Bài học thất bại.", Toast.LENGTH_LONG).show();
                finish();
            } else {
                currentQuestionIndex++;
                Toast.makeText(this, "Sai rồi! Tiếp tục nào.", Toast.LENGTH_SHORT).show();
                displayQuestion(currentQuestionIndex);
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
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}