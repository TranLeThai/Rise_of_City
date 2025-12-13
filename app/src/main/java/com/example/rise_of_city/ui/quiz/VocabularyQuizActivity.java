package com.example.rise_of_city.ui.quiz;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rise_of_city.R;
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
    
    private String correctAnswer;
    private String selectedAnswer;
    private boolean answerSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_quiz);

        initViews();
        setupQuestion();
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

    private void setupQuestion() {
        // Ví dụ: Câu hỏi về từ "House"
        String question = "Từ nào có nghĩa là 'Ngôi Nhà'?";
        tvQuestion.setText(question);
        
        // Set hình ảnh minh họa (có thể lấy từ intent hoặc database)
        ivIllustration.setImageResource(R.drawable.vector_house);
        
        // Đáp án đúng
        correctAnswer = "HOUSE";
        
        // Tạo danh sách đáp án và xáo trộn
        List<String> answers = new ArrayList<>();
        answers.add("HOUSE");
        answers.add("MOUSE");
        answers.add("HORSE");
        answers.add("HOSE");
        Collections.shuffle(answers);
        
        // Gán đáp án vào các buttons
        btnAnswer1.setText(answers.get(0));
        btnAnswer2.setText(answers.get(1));
        btnAnswer3.setText(answers.get(2));
        btnAnswer4.setText(answers.get(3));
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
            if (selectedAnswer.equals(correctAnswer)) {
                // Highlight đáp án đúng
                highlightCorrectAnswer();
                // Disable buttons
                disableAllButtons();
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

    private void highlightCorrectAnswer() {
        // Highlight đáp án đúng với màu xanh và chữ xanh đậm
        int greenColor = 0xFF2E7D32; // Màu xanh đậm
        if (btnAnswer1.getText().toString().equals(correctAnswer)) {
            btnAnswer1.setBackgroundResource(R.drawable.bg_answer_button_correct);
            btnAnswer1.setTextColor(greenColor);
        } else if (btnAnswer2.getText().toString().equals(correctAnswer)) {
            btnAnswer2.setBackgroundResource(R.drawable.bg_answer_button_correct);
            btnAnswer2.setTextColor(greenColor);
        } else if (btnAnswer3.getText().toString().equals(correctAnswer)) {
            btnAnswer3.setBackgroundResource(R.drawable.bg_answer_button_correct);
            btnAnswer3.setTextColor(greenColor);
        } else if (btnAnswer4.getText().toString().equals(correctAnswer)) {
            btnAnswer4.setBackgroundResource(R.drawable.bg_answer_button_correct);
            btnAnswer4.setTextColor(greenColor);
        }

        // Highlight đáp án sai với màu đỏ và chữ đỏ đậm
        if (!selectedAnswer.equals(correctAnswer)) {
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
        AnswerCorrectDialogFragment dialog = AnswerCorrectDialogFragment.newInstance(20);
        dialog.setOnContinueClickListener(() -> {
            // Đóng activity và quay lại màn hình trước
            finish();
        });
        dialog.show(getSupportFragmentManager(), "AnswerCorrectDialog");
    }

    private void showWrongAnswerDialog() {
        // Map English answer to Vietnamese
        String vietnameseAnswer = getVietnameseTranslation(correctAnswer);
        AnswerWrongDialogFragment dialog = AnswerWrongDialogFragment.newInstance(correctAnswer, vietnameseAnswer);
        dialog.setOnUnderstoodClickListener(() -> {
            // Đóng activity và quay lại màn hình trước
            finish();
        });
        dialog.show(getSupportFragmentManager(), "AnswerWrongDialog");
    }

    private String getVietnameseTranslation(String englishWord) {
        // Map English words to Vietnamese translations
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
