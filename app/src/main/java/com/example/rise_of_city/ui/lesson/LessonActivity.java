package com.example.rise_of_city.ui.lesson;

import android.content.Intent;
import android.content.pm.ActivityInfo;
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
    private final long TIME_LIMIT_BASE = 20000; // 20 giây cơ bản
    private long currentTimeLimit = TIME_LIMIT_BASE;

    // Tên file JSON (ví dụ: House_lv1.json, School_lv1.json, ...)
    private String lessonFileName;
    private String lessonName; // Tên lesson (không có .json)
    private String mode; // "STUDY_NEW" hoặc "REVIEW"
    private com.example.rise_of_city.data.repository.LessonQuestionManager questionManager;
    private com.example.rise_of_city.data.repository.GoldRepository goldRepository;
    private static final int GOLD_PER_CORRECT_ANSWER = 5; // 5 vàng mỗi câu đúng
    private static final int GOLD_FOR_COMPLETING_LESSON = 50; // 50 vàng khi hoàn thành toàn bộ bài học

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        initViews();

        // LẤY TÊN BÀI HỌC TỪ INTENT (key: "lessonName")
        lessonName = getIntent().getStringExtra("lessonName");
        
        // LẤY MODE TỪ INTENT (key: "mode")
        mode = getIntent().getStringExtra("mode");
        if (mode == null || mode.isEmpty()) {
            mode = "STUDY_NEW"; // Mặc định là học mới
        }

        // Fallback nếu không có (ví dụ test trực tiếp activity)
        if (lessonName == null || lessonName.isEmpty()) {
            lessonName = "House_lv1"; // bài mặc định
        }

        // Khởi tạo managers
        questionManager = new com.example.rise_of_city.data.repository.LessonQuestionManager(this);
        goldRepository = com.example.rise_of_city.data.repository.GoldRepository.getInstance();

        // LUÔN THÊM ĐUÔI .json ĐỂ ĐẢM BẢO LOAD ĐÚNG FILE TRONG ASSETS
        lessonFileName = lessonName + ".json";

        // Load dữ liệu bài học
        JsonReader jsonReader = new JsonReader(this);
        List<BaseQuestion> allQuestions = jsonReader.readLessonFromJson(lessonFileName);

        if (allQuestions != null && !allQuestions.isEmpty()) {
            // Lọc câu hỏi dựa trên mode
            if ("REVIEW".equals(mode)) {
                // REVIEW: Chỉ hiển thị câu đã làm (đúng hoặc sai)
                questionList = questionManager.filterAnsweredQuestions(lessonName, allQuestions);
                if (questionList.isEmpty()) {
                    Toast.makeText(this, "Bạn chưa làm câu nào để ôn lại!", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            } else {
                // STUDY_NEW: Chỉ hiển thị câu CHƯA làm ĐÚNG (có thể cộng vàng)
                // Bao gồm: câu chưa làm + câu đã làm SAI
                questionList = questionManager.filterQuestionsForStudy(lessonName, allQuestions);
                if (questionList.isEmpty()) {
                    // Đã hoàn thành tất cả câu hỏi
                    Toast.makeText(this, "Chúc mừng! Bạn đã hoàn thành tất cả câu hỏi!", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
            
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
            // Hoàn thành toàn bộ bài học
            finishLesson();
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
            // Set orientation dựa trên loại quiz
            // Chỉ LISTENING mới cần landscape, còn lại portrait
            if (question.getType() == BaseQuestion.QuestionType.LISTENING) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            
            loadFragment(fragment);
            lessonProgressBar.setProgress(index + 1);
            // Tính thời gian dựa trên loại câu hỏi
            calculateTimeLimit(question);
            startCountdown();
        }
    }

    /**
     * Tính toán thời gian giới hạn dựa trên loại và độ dài câu hỏi
     */
    private void calculateTimeLimit(BaseQuestion question) {
        currentTimeLimit = TIME_LIMIT_BASE; // Mặc định 20 giây
        
        if (question.getType() == BaseQuestion.QuestionType.INPUT) {
            WritingQuestion inputQuestion = (WritingQuestion) question;
            String questionText = inputQuestion.getSentence();
            if (questionText != null) {
                // Tính thời gian dựa trên số ký tự trong câu hỏi
                int charCount = questionText.length();
                // Mỗi 10 ký tự thêm 5 giây, tối đa thêm 30 giây
                long extraTime = Math.min((charCount / 10) * 5000, 30000);
                currentTimeLimit = TIME_LIMIT_BASE + extraTime;
            }
        } else if (question.getType() == BaseQuestion.QuestionType.SENTENCEORDERING) {
            SentenceOrderQuestion orderingQuestion = (SentenceOrderQuestion) question;
            String sentence = orderingQuestion.getCorrectSentence();
            if (sentence != null) {
                // Câu dài hơn cần thêm thời gian
                int wordCount = sentence.split("\\s+").length;
                // Mỗi 3 từ thêm 5 giây, tối đa thêm 25 giây
                long extraTime = Math.min((wordCount / 3) * 5000, 25000);
                currentTimeLimit = TIME_LIMIT_BASE + extraTime;
            }
        }
    }

    public void startCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(currentTimeLimit, 1000) {
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
        // Lưu trạng thái câu hỏi
        if (questionList != null && currentQuestionIndex < questionList.size()) {
            BaseQuestion question = questionList.get(currentQuestionIndex);
            
            // Kiểm tra xem câu này đã làm đúng trước đó chưa (TRƯỚC KHI mark)
            boolean wasCorrectBefore = questionManager.isQuestionCorrect(lessonName, question.getId());
            
            // Log để debug
            android.util.Log.d("LessonActivity", "Question " + question.getId() + " - wasCorrectBefore: " + wasCorrectBefore + ", mode: " + mode);
            
            // Lưu trạng thái câu hỏi (đánh dấu là đúng)
            questionManager.markQuestionAnswered(lessonName, question.getId(), true);
            
            // Cộng vàng chỉ khi STUDY_NEW (không cộng vàng khi REVIEW)
            if ("STUDY_NEW".equals(mode)) {
                // Chỉ cộng vàng nếu chưa làm đúng trước đó (lần đầu làm đúng)
                if (!wasCorrectBefore) {
                    android.util.Log.d("LessonActivity", "Adding gold for question " + question.getId());
                    goldRepository.addGold(LessonActivity.this, GOLD_PER_CORRECT_ANSWER, new com.example.rise_of_city.data.repository.GoldRepository.OnGoldUpdatedListener() {
                        @Override
                        public void onGoldUpdated(int newGold) {
                            Toast.makeText(LessonActivity.this, "Đúng rồi! +" + GOLD_PER_CORRECT_ANSWER + " vàng", Toast.LENGTH_SHORT).show();
                        }
                        
                        @Override
                        public void onError(String error) {
                            Toast.makeText(LessonActivity.this, "Đúng rồi!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    android.util.Log.d("LessonActivity", "Question " + question.getId() + " was already correct before, not adding gold");
                    Toast.makeText(this, "Đúng rồi!", Toast.LENGTH_SHORT).show();
                }
            } else {
                // REVIEW mode: không cộng vàng
                android.util.Log.d("LessonActivity", "REVIEW mode, not adding gold");
                Toast.makeText(this, "Đúng rồi!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Đúng rồi!", Toast.LENGTH_SHORT).show();
        }
        
        currentQuestionIndex++;
        displayQuestion(currentQuestionIndex);
    }

    public void handleWrongAnswer() {
        // Lưu trạng thái câu hỏi (sai)
        if (questionList != null && currentQuestionIndex < questionList.size()) {
            BaseQuestion question = questionList.get(currentQuestionIndex);
            questionManager.markQuestionAnswered(lessonName, question.getId(), false);
        }
        
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

    /**
     * Xử lý khi hoàn thành toàn bộ bài học
     */
    private void finishLesson() {
        // Mark lesson as completed
        if (lessonName != null) {
            com.example.rise_of_city.data.repository.BuildingUpgradeManager upgradeManager = 
                com.example.rise_of_city.data.repository.BuildingUpgradeManager.getInstance(this);
            upgradeManager.markLessonCompleted(lessonName);
        }
        
        // Kiểm tra xem có mission_id không (từ quest)
        String missionId = getIntent().getStringExtra("mission_id");
        
        // Cộng vàng khi hoàn thành bài học
        if ("STUDY_NEW".equals(mode)) {
            // Mode học mới: cộng vàng cho việc hoàn thành bài học
            goldRepository.addGold(this, GOLD_FOR_COMPLETING_LESSON, new com.example.rise_of_city.data.repository.GoldRepository.OnGoldUpdatedListener() {
                @Override
                public void onGoldUpdated(int newGold) {
                    Toast.makeText(LessonActivity.this, 
                        "Hoàn thành bài học! +" + GOLD_FOR_COMPLETING_LESSON + " vàng (Tổng: " + newGold + " vàng)\nCông trình đã sẵn sàng nâng cấp.", 
                        Toast.LENGTH_LONG).show();
                }
                
                @Override
                public void onError(String error) {
                    Toast.makeText(LessonActivity.this, 
                        "Hoàn thành bài học! Công trình đã sẵn sàng nâng cấp.", 
                        Toast.LENGTH_LONG).show();
                }
            });
        } else if (missionId != null) {
            // Mode REVIEW từ quest: Không cộng vàng ở đây (sẽ cộng ở MissionDialogFragment)
            Toast.makeText(this, "Hoàn thành quiz! Nhận thưởng từ nhiệm vụ...", Toast.LENGTH_LONG).show();
        } else {
            // Mode REVIEW thông thường: không cộng vàng
            Toast.makeText(this, "Hoàn thành bài học!", Toast.LENGTH_LONG).show();
        }
        
        // Set result to indicate lesson completed
        Intent resultIntent = new Intent();
        resultIntent.putExtra("lesson_completed", true);
        
        // Nếu có mission_id, trả về để complete mission (đã lấy ở trên)
        if (missionId != null) {
            resultIntent.putExtra("completed_mission_id", missionId);
        }
        
        setResult(RESULT_OK, resultIntent);
        
        // Delay một chút để user kịp thấy toast, rồi mới finish
        new android.os.Handler().postDelayed(() -> finish(), 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}