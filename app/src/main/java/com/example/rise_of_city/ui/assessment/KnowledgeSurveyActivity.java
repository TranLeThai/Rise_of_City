package com.example.rise_of_city.ui.assessment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rise_of_city.R;
import com.example.rise_of_city.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnowledgeSurveyActivity extends AppCompatActivity {
    private static final String TAG = "KnowledgeSurveyActivity";
    
    private TextView tvQuestion, tvQuestionNumber;
    private RadioGroup radioGroup;
    private RadioButton rbOption1, rbOption2, rbOption3, rbOption4;
    private Button btnNext, btnPrevious;
    private ProgressBar progressBar;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    
    private List<SurveyQuestion> questions;
    private int currentQuestionIndex = 0;
    private Map<Integer, Integer> userAnswers = new HashMap<>(); // questionIndex -> selectedAnswerIndex
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knowledge_survey);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        initViews();
        initQuestions();
        setupClickListeners();
        displayQuestion(0);
    }
    
    private void initViews() {
        tvQuestion = findViewById(R.id.tvQuestion);
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        radioGroup = findViewById(R.id.radioGroup);
        rbOption1 = findViewById(R.id.rbOption1);
        rbOption2 = findViewById(R.id.rbOption2);
        rbOption3 = findViewById(R.id.rbOption3);
        rbOption4 = findViewById(R.id.rbOption4);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void initQuestions() {
        questions = new ArrayList<>();
        
        // Câu hỏi 1: Beginner level
        questions.add(new SurveyQuestion(
            "Chọn câu đúng:",
            new String[]{"I am student", "I is a student", "I am a student", "I are a student"},
            2 // Đáp án đúng là index 2
        ));
        
        // Câu hỏi 2: Beginner level
        questions.add(new SurveyQuestion(
            "What is the past tense of 'go'?",
            new String[]{"goed", "went", "gone", "goes"},
            1
        ));
        
        // Câu hỏi 3: Intermediate level
        questions.add(new SurveyQuestion(
            "Choose the correct sentence:",
            new String[]{
                "If I was you, I would study harder",
                "If I were you, I would study harder",
                "If I am you, I will study harder",
                "If I be you, I would study harder"
            },
            1
        ));
        
        // Câu hỏi 4: Intermediate level
        questions.add(new SurveyQuestion(
            "What does 'procrastinate' mean?",
            new String[]{"To work quickly", "To delay or postpone", "To finish early", "To plan ahead"},
            1
        ));
        
        // Câu hỏi 5: Advanced level
        questions.add(new SurveyQuestion(
            "Choose the correct form: 'Neither the students nor the teacher _____ present.'",
            new String[]{"was", "were", "is", "are"},
            0
        ));
        
        // Câu hỏi 6: Advanced level
        questions.add(new SurveyQuestion(
            "What is the meaning of 'ubiquitous'?",
            new String[]{"Rare", "Present everywhere", "Expensive", "Difficult"},
            1
        ));
        
        // Câu hỏi 7: Beginner level
        questions.add(new SurveyQuestion(
            "How do you say 'Xin chào' in English?",
            new String[]{"Goodbye", "Hello", "Thank you", "Please"},
            1
        ));
        
        // Câu hỏi 8: Intermediate level
        questions.add(new SurveyQuestion(
            "Complete: 'I have been studying English _____ 5 years.'",
            new String[]{"for", "since", "during", "in"},
            0
        ));
        
        // Câu hỏi 9: Advanced level
        questions.add(new SurveyQuestion(
            "Which sentence uses the subjunctive mood correctly?",
            new String[]{
                "I suggest that he goes to the doctor",
                "I suggest that he go to the doctor",
                "I suggest that he went to the doctor",
                "I suggest that he is going to the doctor"
            },
            1
        ));
        
        // Câu hỏi 10: Mixed level
        questions.add(new SurveyQuestion(
            "What is the passive voice of: 'They built this house in 2020'?",
            new String[]{
                "This house was built in 2020",
                "This house is built in 2020",
                "This house built in 2020",
                "This house was build in 2020"
            },
            0
        ));
    }
    
    private void setupClickListeners() {
        btnNext.setOnClickListener(v -> {
            if (radioGroup.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Vui lòng chọn một đáp án", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Lưu câu trả lời
            int selectedIndex = getSelectedAnswerIndex();
            userAnswers.put(currentQuestionIndex, selectedIndex);
            
            if (currentQuestionIndex < questions.size() - 1) {
                currentQuestionIndex++;
                displayQuestion(currentQuestionIndex);
            } else {
                // Hoàn thành khảo sát
                completeSurvey();
            }
        });
        
        btnPrevious.setOnClickListener(v -> {
            if (currentQuestionIndex > 0) {
                // Lưu câu trả lời hiện tại trước khi chuyển
                int selectedIndex = getSelectedAnswerIndex();
                if (selectedIndex != -1) {
                    userAnswers.put(currentQuestionIndex, selectedIndex);
                }
                
                currentQuestionIndex--;
                displayQuestion(currentQuestionIndex);
            }
        });
    }
    
    private int getSelectedAnswerIndex() {
        int checkedId = radioGroup.getCheckedRadioButtonId();
        if (checkedId == rbOption1.getId()) return 0;
        if (checkedId == rbOption2.getId()) return 1;
        if (checkedId == rbOption3.getId()) return 2;
        if (checkedId == rbOption4.getId()) return 3;
        return -1;
    }
    
    private void displayQuestion(int index) {
        SurveyQuestion question = questions.get(index);
        
        // Cập nhật số câu hỏi
        tvQuestionNumber.setText(String.format("Câu %d/%d", index + 1, questions.size()));
        
        // Hiển thị câu hỏi
        tvQuestion.setText(question.getQuestion());
        
        // Hiển thị các đáp án
        rbOption1.setText(question.getOptions()[0]);
        rbOption2.setText(question.getOptions()[1]);
        rbOption3.setText(question.getOptions()[2]);
        rbOption4.setText(question.getOptions()[3]);
        
        // Khôi phục câu trả lời đã chọn (nếu có)
        if (userAnswers.containsKey(index)) {
            int savedAnswer = userAnswers.get(index);
            switch (savedAnswer) {
                case 0: radioGroup.check(rbOption1.getId()); break;
                case 1: radioGroup.check(rbOption2.getId()); break;
                case 2: radioGroup.check(rbOption3.getId()); break;
                case 3: radioGroup.check(rbOption4.getId()); break;
            }
        } else {
            radioGroup.clearCheck();
        }
        
        // Cập nhật nút
        btnPrevious.setVisibility(index > 0 ? View.VISIBLE : View.GONE);
        btnNext.setText(index == questions.size() - 1 ? "Hoàn thành" : "Tiếp theo");
        
        // Cập nhật progress bar
        int progress = (int) (((index + 1) / (float) questions.size()) * 100);
        progressBar.setProgress(progress);
    }
    
    private void completeSurvey() {
        // Tính điểm và xác định trình độ
        int score = calculateScore();
        String level = determineLevel(score);
        
        // Lưu kết quả vào Firestore
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Lỗi: Người dùng chưa đăng nhập", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        btnNext.setEnabled(false);
        btnNext.setText("Đang lưu...");
        
        Map<String, Object> surveyData = new HashMap<>();
        surveyData.put("surveyCompleted", true);
        surveyData.put("surveyScore", score);
        surveyData.put("surveyLevel", level);
        surveyData.put("totalQuestions", questions.size());
        surveyData.put("surveyCompletedAt", System.currentTimeMillis());
        
        // Lưu vào user_profiles
        db.collection("user_profiles")
                .document(user.getUid())
                .update(surveyData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Survey completed. Level: " + level + ", Score: " + score);
                    Toast.makeText(this, 
                            "Khảo sát hoàn thành! Trình độ của bạn: " + level, 
                            Toast.LENGTH_LONG).show();
                    
                    // Chuyển đến MainActivity
                    Intent intent = new Intent(KnowledgeSurveyActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnNext.setEnabled(true);
                    btnNext.setText("Hoàn thành");
                    Log.e(TAG, "Error saving survey results: ", e);
                    Toast.makeText(this, "Lỗi lưu kết quả khảo sát. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                });
    }
    
    private int calculateScore() {
        int correctAnswers = 0;
        for (int i = 0; i < questions.size(); i++) {
            Integer userAnswer = userAnswers.get(i);
            if (userAnswer != null && userAnswer == questions.get(i).getCorrectAnswer()) {
                correctAnswers++;
            }
        }
        return correctAnswers;
    }
    
    private String determineLevel(int score) {
        int totalQuestions = questions.size();
        double percentage = (score / (double) totalQuestions) * 100;
        
        // Phân loại dựa trên điểm số
        // Beginner: 0-40%
        // Intermediate: 41-70%
        // Advanced: 71-100%
        
        if (percentage <= 40) {
            return "Beginner";
        } else if (percentage <= 70) {
            return "Intermediate";
        } else {
            return "Advanced";
        }
    }
    
    // Inner class để lưu trữ câu hỏi
    private static class SurveyQuestion {
        private String question;
        private String[] options;
        private int correctAnswer;
        
        public SurveyQuestion(String question, String[] options, int correctAnswer) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
        
        public String getQuestion() {
            return question;
        }
        
        public String[] getOptions() {
            return options;
        }
        
        public int getCorrectAnswer() {
            return correctAnswer;
        }
    }
}

