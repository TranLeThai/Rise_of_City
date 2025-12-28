package com.example.rise_of_city.ui.lesson;

import android.app.AlertDialog;
import android.content.Intent;
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
import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.UserLessonProgress;
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
import com.example.rise_of_city.data.repository.GoldRepository;
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

    private TextView tvTimer;
    private ProgressBar lessonProgressBar;
    private ImageView[] ivHearts;
    private ImageButton btnSettings;

    private int currentHearts = 3;
    private int currentQuestionIndex = 0;
    private List<BaseQuestion> questionList;
    private CountDownTimer countDownTimer;
    private final long TIME_LIMIT = 20000;

    private String currentLessonName;
    private AppDatabase database;
    private GoldRepository goldRepository;

    private static final int MAX_FREE_ATTEMPTS = 3;
    private static final int RETRY_COST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        database = AppDatabase.getInstance(this);
        goldRepository = GoldRepository.getInstance();

        initViews();

        currentLessonName = getIntent().getStringExtra("lessonName");
        if (currentLessonName == null || currentLessonName.isEmpty()) {
            Toast.makeText(this, "Không có bài học được chọn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Kiểm tra lượt chơi trước khi load bài học
        checkDailyAttemptsAndProceed();
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
    }

    /**
     * Kiểm tra lượt chơi hôm nay trước khi load bài học
     */
    private void checkDailyAttemptsAndProceed() {
        new Thread(() -> {
            int userId = 1;
            long today = System.currentTimeMillis() / (1000L * 3600 * 24);

            UserLessonProgress progress = database.userLessonProgressDao()
                    .getProgress(userId, currentLessonName);

            if (progress == null || progress.lastAttemptDate != today) {
                if (progress == null) {
                    progress = new UserLessonProgress();
                    progress.userId = userId;
                    progress.lessonName = currentLessonName;
                }
                progress.attemptsToday = 0;
                progress.lastAttemptDate = today;
                progress.completed = false;
                database.userLessonProgressDao().insertOrUpdate(progress);
            }

            if (progress.completed) {
                runOnUiThread(this::loadLessonAndStart);
                return;
            }

            if (progress.attemptsToday >= MAX_FREE_ATTEMPTS) {
                final UserLessonProgress finalProgress = progress; // Fix cảnh báo Lint
                runOnUiThread(() -> showOutOfAttemptsDialog(finalProgress));
                return;
            }

            runOnUiThread(this::loadLessonAndStart);
        }).start();
    }

    private void showOutOfAttemptsDialog(UserLessonProgress progress) {
        new AlertDialog.Builder(this)
                .setTitle("Hết lượt thử miễn phí")
                .setMessage("Bạn đã dùng hết " + MAX_FREE_ATTEMPTS + " lần thử miễn phí hôm nay cho bài học này.\n\nTrả " + RETRY_COST + " vàng để thử lại ngay?")
                .setPositiveButton("Trả vàng", (d, w) -> {
                    goldRepository.addGold(this, -RETRY_COST, new GoldRepository.OnGoldUpdatedListener() {
                        @Override
                        public void onGoldUpdated(int newGold) {
                            progress.attemptsToday = 0;
                            database.userLessonProgressDao().insertOrUpdate(progress);
                            loadLessonAndStart();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(LessonActivity.this, error, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                })
                .setNegativeButton("Thoát", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    private void loadLessonAndStart() {
        String lessonFileName = currentLessonName + ".json";
        JsonReader jsonReader = new JsonReader(this);
        questionList = jsonReader.readLessonFromJson(lessonFileName);

        if (questionList != null && !questionList.isEmpty()) {
            lessonProgressBar.setMax(questionList.size());
            displayQuestion(0);
        } else {
            Toast.makeText(this, "Không thể tải bài học: " + lessonFileName, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void displayQuestion(int index) {
        if (index >= questionList.size()) {
            markLessonCompleted(true);
            return;
        }

        BaseQuestion question = questionList.get(index);
        Fragment fragment = null;

        switch (question.getType()) {
            case LECTURE: fragment = LectureFragment.newInstance((LectureQuestion) question); break;
            case CHOICE: fragment = ChoiceFragment.newInstance((ChoiceQuestion) question); break;
            case MATCHINGTEXT: fragment = MatchingTextFragment.newInstance((MatchingTextQuestion) question); break;
            case MATCHINGIMG: fragment = MatchingIMGFragment.newInstance((MatchingIMGQuestion) question); break;
            case INPUT: fragment = WritingFragment.newInstance((WritingQuestion) question); break;
            case DECISION: fragment = TrueFalseFragment.newInstance((TrueFalseQuestion) question); break;
            case SENTENCEORDERING: fragment = OrderingFragment.newInstance((SentenceOrderQuestion) question); break;
            case WORDORDERING: fragment = OrderingFragment.newInstance((WordOrderQuestion) question); break;
            case LISTENING: fragment = ListeningFragment.newInstance((ListeningQuestion) question); break;
            default:
                Toast.makeText(this, "Loại câu hỏi không hỗ trợ!", Toast.LENGTH_SHORT).show();
                break;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            lessonProgressBar.setProgress(index + 1);
            startCountdown();
        }
    }

    public void startCountdown() {
        if (countDownTimer != null) countDownTimer.cancel();
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
            ivHearts[currentHearts].setImageResource(R.drawable.ic_heart_empty);

            if (currentHearts == 0) {
                markLessonCompleted(false);
            } else {
                currentQuestionIndex++;
                displayQuestion(currentQuestionIndex);
            }
        }
    }

    private void markLessonCompleted(boolean success) {
        new Thread(() -> {
            int userId = 1;
            long today = System.currentTimeMillis() / (1000L * 3600 * 24);

            UserLessonProgress progress = database.userLessonProgressDao()
                    .getProgress(userId, currentLessonName);

            if (progress == null) {
                progress = new UserLessonProgress();
                progress.userId = userId;
                progress.lessonName = currentLessonName;
                progress.attemptsToday = 0;
                progress.lastAttemptDate = today;
            }

            if (progress.lastAttemptDate != today) {
                progress.attemptsToday = 0;
                progress.lastAttemptDate = today;
            }

            progress.attemptsToday++;

            if (success) {
                progress.completed = true;
                goldRepository.addGold(this, 50, null);
            }

            database.userLessonProgressDao().insertOrUpdate(progress);

            // === SỬA Ở ĐÂY: Tạo các biến final để dùng trong lambda ===
            final UserLessonProgress finalProgress = progress;
            final boolean finalSuccess = success;
            final String finalLessonName = currentLessonName;

            runOnUiThread(() -> {
                Intent result = new Intent();
                result.putExtra("completed_lesson", finalLessonName);
                result.putExtra("success", finalSuccess);
                setResult(RESULT_OK, result);

                if (finalSuccess) {
                    Toast.makeText(LessonActivity.this, "Hoàn thành bài học! Công trình được nâng cấp.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    if (finalProgress.attemptsToday <= MAX_FREE_ATTEMPTS) {
                        Toast.makeText(LessonActivity.this,
                                "Thất bại! Còn " + (MAX_FREE_ATTEMPTS - finalProgress.attemptsToday + 1) + " lần miễn phí hôm nay.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        // Hiển thị dialog trả vàng khi hết lượt
                        new AlertDialog.Builder(LessonActivity.this)
                                .setTitle("Hết lượt thử miễn phí")
                                .setMessage("Bạn đã dùng hết " + MAX_FREE_ATTEMPTS + " lần miễn phí hôm nay.\n\nTrả " + RETRY_COST + " vàng để thử lại ngay?")
                                .setPositiveButton("Trả vàng", (d, w) -> {
                                    goldRepository.addGold(LessonActivity.this, -RETRY_COST, new GoldRepository.OnGoldUpdatedListener() {
                                        @Override
                                        public void onGoldUpdated(int newGold) {
                                            finalProgress.attemptsToday = 0;
                                            database.userLessonProgressDao().insertOrUpdate(finalProgress);
                                            currentQuestionIndex = 0;
                                            currentHearts = 3;
                                            for (ImageView h : ivHearts) h.setImageResource(R.drawable.ic_heart_filled);
                                            displayQuestion(0);
                                        }

                                        @Override
                                        public void onError(String error) {
                                            Toast.makeText(LessonActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                })
                                .setNegativeButton("Thoát", (d, w) -> finish())
                                .setCancelable(false)
                                .show();
                    }
                }
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}