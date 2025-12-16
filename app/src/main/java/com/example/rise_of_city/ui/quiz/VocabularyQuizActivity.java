package com.example.rise_of_city.ui.quiz;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.Vocabulary;
import com.example.rise_of_city.data.repository.BuildingProgressRepository;
import com.example.rise_of_city.data.repository.BuildingHarvestRepository;
import com.example.rise_of_city.data.repository.GoldRepository;
import com.example.rise_of_city.data.repository.LearningLogRepository;
import com.example.rise_of_city.data.repository.UserStatsRepository;
import com.example.rise_of_city.data.repository.VocabularyRepository;
import com.example.rise_of_city.ui.dialog.AnswerCorrectDialogFragment;
import com.example.rise_of_city.ui.dialog.AnswerWrongDialogFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VocabularyQuizActivity extends AppCompatActivity {

    private TextView tvQuestion;
    private ImageView ivIllustration;
    private Button btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4;
    private Button btnCheck;
    
    private Vocabulary correctVocabulary;
    private List<Vocabulary> wrongOptions;
    private String selectedAnswer;
    private boolean answerSelected = false;
    
    private VocabularyRepository vocabRepository;
    private BuildingProgressRepository buildingProgressRepo;
    private LearningLogRepository learningLogRepo;
    private GoldRepository goldRepo;
    private BuildingHarvestRepository harvestRepo;
    private String buildingId; // ID của building đang quiz (từ intent)
    
    // Phần thưởng sẽ được tính dựa trên level building (trong updateBuildingProgress)
    private int buildingLevel = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_quiz);

        // Lấy buildingId từ intent (nếu có)
        buildingId = getIntent().getStringExtra("buildingId");
        if (buildingId == null) {
            buildingId = "house"; // Default
        }

        // Khởi tạo repositories
        vocabRepository = VocabularyRepository.getInstance();
        buildingProgressRepo = BuildingProgressRepository.getInstance();
        learningLogRepo = LearningLogRepository.getInstance();
        goldRepo = GoldRepository.getInstance();
        harvestRepo = BuildingHarvestRepository.getInstance();
        
        // Load building level để tính phần thưởng
        loadBuildingLevel();

        initViews();
        loadQuizFromFirebase();
        setupAnswerButtons();
        setupCheckButton();
    }

    private void initViews() {
        ImageButton btnClose = findViewById(R.id.btn_close);
        tvQuestion = findViewById(R.id.tv_question);
        ivIllustration = findViewById(R.id.iv_illustration);
        btnAnswer1 = findViewById(R.id.btn_answer1);
        btnAnswer2 = findViewById(R.id.btn_answer2);
        btnAnswer3 = findViewById(R.id.btn_answer3);
        btnAnswer4 = findViewById(R.id.btn_answer4);
        btnCheck = findViewById(R.id.btn_check);

        btnClose.setOnClickListener(v -> finish());
    }

    /**
     * Load quiz từ Firebase
     */
    private void loadQuizFromFirebase() {
        // Hiển thị loading
        tvQuestion.setText("Đang tải câu hỏi...");
        btnCheck.setEnabled(false);
        
        // Lấy từ vựng theo buildingId (nếu có)
        vocabRepository.getQuizOptions(buildingId, new VocabularyRepository.OnQuizOptionsLoadedListener() {
            @Override
            public void onQuizOptionsLoaded(Vocabulary correctVocab, List<Vocabulary> wrongOpts) {
                correctVocabulary = correctVocab;
                wrongOptions = wrongOpts;
                
                // Setup câu hỏi
                setupQuestion();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(VocabularyQuizActivity.this, 
                    "Lỗi: " + error, Toast.LENGTH_LONG).show();
                // Fallback: dùng dữ liệu mẫu
                setupFallbackQuestion();
            }
        });
    }

    private void setupQuestion() {
        if (correctVocabulary == null || wrongOptions == null || wrongOptions.size() < 3) {
            setupFallbackQuestion();
            return;
        }
        
        // Tạo câu hỏi
        String question = "Từ nào có nghĩa là '" + correctVocabulary.getVietnamese() + "'?";
        tvQuestion.setText(question);
        
        // Load ảnh từ URL bằng Glide
        if (correctVocabulary.getImageUrl() != null && !correctVocabulary.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(correctVocabulary.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.vector_house) // Ảnh placeholder khi đang load
                    .error(R.drawable.vector_house) // Ảnh hiển thị khi lỗi
                    .into(ivIllustration);
        } else {
            // Nếu không có URL, dùng ảnh mặc định
            ivIllustration.setImageResource(R.drawable.vector_house);
        }
        
        // Tạo danh sách đáp án (1 đúng + 3 sai)
        List<String> answers = new ArrayList<>();
        answers.add(correctVocabulary.getEnglish().toUpperCase());
        answers.add(wrongOptions.get(0).getEnglish().toUpperCase());
        answers.add(wrongOptions.get(1).getEnglish().toUpperCase());
        answers.add(wrongOptions.get(2).getEnglish().toUpperCase());
        Collections.shuffle(answers);
        
        // Gán đáp án vào các buttons
        btnAnswer1.setText(answers.get(0));
        btnAnswer2.setText(answers.get(1));
        btnAnswer3.setText(answers.get(2));
        btnAnswer4.setText(answers.get(3));
        
        // Enable nút check
        btnCheck.setEnabled(true);
    }

    /**
     * Fallback: Dùng dữ liệu mẫu nếu không load được từ Firebase
     */
    private void setupFallbackQuestion() {
        String question = "Từ nào có nghĩa là 'Ngôi Nhà'?";
        tvQuestion.setText(question);
        ivIllustration.setImageResource(R.drawable.vector_house);
        
        List<String> answers = new ArrayList<>();
        answers.add("HOUSE");
        answers.add("MOUSE");
        answers.add("HORSE");
        answers.add("HOSE");
        Collections.shuffle(answers);
        
        btnAnswer1.setText(answers.get(0));
        btnAnswer2.setText(answers.get(1));
        btnAnswer3.setText(answers.get(2));
        btnAnswer4.setText(answers.get(3));
        
        // Tạo vocabulary mẫu
        correctVocabulary = new Vocabulary("HOUSE", "Ngôi nhà", null);
        
        btnCheck.setEnabled(true);
    }

    private void setupAnswerButtons() {
        View.OnClickListener answerClickListener = v -> {
            // Luôn cho phép người dùng thay đổi đáp án cho đến khi bấm KIỂM TRA
            // Reset tất cả buttons về trạng thái mặc định
            resetAnswerButtons();
            
            // Highlight button được chọn
            Button selectedBtn = (Button) v;
            selectedBtn.setBackgroundResource(R.drawable.bg_answer_button_selected);
            selectedAnswer = selectedBtn.getText().toString();
            answerSelected = true;
        };

        btnAnswer1.setOnClickListener(answerClickListener);
        btnAnswer2.setOnClickListener(answerClickListener);
        btnAnswer3.setOnClickListener(answerClickListener);
        btnAnswer4.setOnClickListener(answerClickListener);
    }

    private void resetAnswerButtons() {
        // Reset background và màu chữ
        int defaultTextColor = 0xFF424242; // Màu xám đậm mặc định
        btnAnswer1.setBackgroundResource(R.drawable.bg_answer_button);
        btnAnswer1.setTextColor(defaultTextColor);
        btnAnswer2.setBackgroundResource(R.drawable.bg_answer_button);
        btnAnswer2.setTextColor(defaultTextColor);
        btnAnswer3.setBackgroundResource(R.drawable.bg_answer_button);
        btnAnswer3.setTextColor(defaultTextColor);
        btnAnswer4.setBackgroundResource(R.drawable.bg_answer_button);
        btnAnswer4.setTextColor(defaultTextColor);
    }

    private void setupCheckButton() {
        btnCheck.setOnClickListener(v -> {
            if (!answerSelected) {
                Toast.makeText(this, "Vui lòng chọn một đáp án!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra đáp án
            String correctAnswerText = correctVocabulary != null ? 
                correctVocabulary.getEnglish().toUpperCase() : "HOUSE";
            
            boolean isCorrect = selectedAnswer.equals(correctAnswerText);
            
            // Ghi log học tập
            String vocabularyEnglish = correctVocabulary != null ? 
                correctVocabulary.getEnglish() : "HOUSE";
            learningLogRepo.logQuizAttempt(buildingId, isCorrect, vocabularyEnglish);
            
            if (isCorrect) {
                // Highlight đáp án đúng
                highlightCorrectAnswer();
                // Disable buttons
                disableAllButtons();
                // Cập nhật building progress (EXP để nâng cấp) và thưởng vàng
                updateBuildingProgress();
                // Hiển thị dialog đáp án đúng
                showCorrectAnswerDialog();
            } else {
                // Highlight đáp án đúng và sai
                highlightCorrectAnswer();
                // Disable buttons
                disableAllButtons();
                // Hiển thị dialog đáp án sai
                showWrongAnswerDialog();
            }
        });
    }

    /**
     * Load building level từ Firebase
     */
    private void loadBuildingLevel() {
        if (buildingId == null) return;
        
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;
        
        String userId = auth.getCurrentUser().getUid();
        String buildingPath = "users/" + userId + "/buildings/" + buildingId;
        
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .document(buildingPath)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long level = documentSnapshot.getLong("level");
                        if (level != null) {
                            buildingLevel = level.intValue();
                        }
                    }
                });
    }
    
    /**
     * Cập nhật building progress khi quiz đúng
     */
    private void updateBuildingProgress() {
        if (buildingProgressRepo != null && buildingId != null && harvestRepo != null) {
            // Tính phần thưởng dựa trên level building
            BuildingHarvestRepository.HarvestReward reward = harvestRepo.calculateHarvestReward(buildingLevel);
            
            // Thưởng EXP
            buildingProgressRepo.addExpToBuilding(buildingId, reward.expReward, 
                new BuildingProgressRepository.OnProgressUpdatedListener() {
                    @Override
                    public void onProgressUpdated(long level, int currentExp, int maxExp) {
                        // Progress đã được cập nhật trong Firebase
                        // Cập nhật số từ vựng đã học cho building này
                        updateVocabularyLearnedForBuilding();
                        
                        // Đánh dấu đã thu hoạch
                        harvestRepo.markAsHarvested(buildingId, new BuildingHarvestRepository.OnHarvestMarkedListener() {
                            @Override
                            public void onHarvestMarked() {
                                Log.d("VocabularyQuiz", "Harvest marked for building: " + buildingId);
                            }
                            
                            @Override
                            public void onError(String error) {
                                Log.e("VocabularyQuiz", "Error marking harvest: " + error);
                            }
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e("VocabularyQuiz", "Error updating building progress: " + error);
                    }
                });
            
            // Thưởng vàng
            rewardGold(reward.goldReward);
        }
    }
    
    /**
     * Cập nhật số từ vựng đã học cho building
     */
    private void updateVocabularyLearnedForBuilding() {
        if (buildingId != null && correctVocabulary != null) {
            UserStatsRepository statsRepo = UserStatsRepository.getInstance();
            statsRepo.calculateVocabularyLearnedByBuilding(buildingId, vocabularyLearned -> {
                // Vocabulary learned đã được cập nhật trong BuildingProgressRepository
            });
        }
    }
    
    /**
     * Thưởng vàng khi quiz đúng (vàng dùng để mở khóa building)
     */
    private void rewardGold(int goldAmount) {
        if (goldRepo != null) {
            goldRepo.addGold(goldAmount, new GoldRepository.OnGoldUpdatedListener() {
                @Override
                public void onGoldUpdated(int newGold) {
                    // Vàng đã được cập nhật
                    Log.d("VocabularyQuiz", "Gold rewarded: +" + goldAmount + " (Total: " + newGold + ")");
                }
                
                @Override
                public void onError(String error) {
                    Log.e("VocabularyQuiz", "Error rewarding gold: " + error);
                }
            });
        }
    }

    private void highlightCorrectAnswer() {
        String correctAnswerText = correctVocabulary != null ? 
            correctVocabulary.getEnglish().toUpperCase() : "HOUSE";
        
        // Highlight đáp án đúng với màu xanh và chữ xanh đậm
        int greenColor = 0xFF2E7D32; // Màu xanh đậm
        if (btnAnswer1.getText().toString().equals(correctAnswerText)) {
            btnAnswer1.setBackgroundResource(R.drawable.bg_answer_button_correct);
            btnAnswer1.setTextColor(greenColor);
        } else if (btnAnswer2.getText().toString().equals(correctAnswerText)) {
            btnAnswer2.setBackgroundResource(R.drawable.bg_answer_button_correct);
            btnAnswer2.setTextColor(greenColor);
        } else if (btnAnswer3.getText().toString().equals(correctAnswerText)) {
            btnAnswer3.setBackgroundResource(R.drawable.bg_answer_button_correct);
            btnAnswer3.setTextColor(greenColor);
        } else if (btnAnswer4.getText().toString().equals(correctAnswerText)) {
            btnAnswer4.setBackgroundResource(R.drawable.bg_answer_button_correct);
            btnAnswer4.setTextColor(greenColor);
        }

        // Highlight đáp án sai với màu đỏ và chữ đỏ đậm
        if (!selectedAnswer.equals(correctAnswerText)) {
            int redColor = 0xFFC62828; // Màu đỏ đậm
            if (btnAnswer1.getText().toString().equals(selectedAnswer)) {
                btnAnswer1.setBackgroundResource(R.drawable.bg_answer_button_wrong);
                btnAnswer1.setTextColor(redColor);
            } else if (btnAnswer2.getText().toString().equals(selectedAnswer)) {
                btnAnswer2.setBackgroundResource(R.drawable.bg_answer_button_wrong);
                btnAnswer2.setTextColor(redColor);
            } else if (btnAnswer3.getText().toString().equals(selectedAnswer)) {
                btnAnswer3.setBackgroundResource(R.drawable.bg_answer_button_wrong);
                btnAnswer3.setTextColor(redColor);
            } else if (btnAnswer4.getText().toString().equals(selectedAnswer)) {
                btnAnswer4.setBackgroundResource(R.drawable.bg_answer_button_wrong);
                btnAnswer4.setTextColor(redColor);
            }
        } else {
            // Nếu chọn đúng, chỉ highlight đáp án đúng với màu xanh
            // (đã xử lý ở trên)
        }
    }

    private void disableAllButtons() {
        btnAnswer1.setEnabled(false);
        btnAnswer2.setEnabled(false);
        btnAnswer3.setEnabled(false);
        btnAnswer4.setEnabled(false);
        btnCheck.setEnabled(false);
    }

    private void showCorrectAnswerDialog() {
        // Tính phần thưởng dựa trên level building
        BuildingHarvestRepository.HarvestReward reward = harvestRepo != null ? 
            harvestRepo.calculateHarvestReward(buildingLevel) : 
            new BuildingHarvestRepository.HarvestReward(20, 10);
        
        AnswerCorrectDialogFragment dialog = AnswerCorrectDialogFragment.newInstance(reward.expReward, reward.goldReward);
        dialog.setOnContinueClickListener(() -> {
            // Đóng activity và quay lại màn hình trước
            // Building progress đã được cập nhật trong updateBuildingProgress()
            // Vàng đã được thưởng trong rewardGold()
            finish();
        });
        dialog.show(getSupportFragmentManager(), "AnswerCorrectDialog");
    }

    private void showWrongAnswerDialog() {
        // Lấy nghĩa tiếng Việt từ vocabulary
        String englishWord = correctVocabulary != null ? 
            correctVocabulary.getEnglish() : "HOUSE";
        String vietnameseAnswer = correctVocabulary != null ? 
            correctVocabulary.getVietnamese() : getVietnameseTranslation(englishWord);
        
        AnswerWrongDialogFragment dialog = AnswerWrongDialogFragment.newInstance(
            englishWord, vietnameseAnswer);
        dialog.setOnUnderstoodClickListener(() -> {
            // Đóng activity và quay lại màn hình trước
            finish();
        });
        dialog.show(getSupportFragmentManager(), "AnswerWrongDialog");
    }

    private String getVietnameseTranslation(String englishWord) {
        // Fallback: Map English words to Vietnamese translations
        if (correctVocabulary != null) {
            return correctVocabulary.getVietnamese();
        }
        
        switch (englishWord.toUpperCase()) {
            case "HOUSE":
                return "Ngôi nhà";
            case "MOUSE":
                return "Chuột";
            case "HORSE":
                return "Ngựa";
            case "HOSE":
                return "Ống nước";
            default:
                return "";
        }
    }
}
