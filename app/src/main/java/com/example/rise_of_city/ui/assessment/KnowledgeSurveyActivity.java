package com.example.rise_of_city.ui.assessment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KnowledgeSurveyActivity extends AppCompatActivity {

    private TextView tvTitle, tvParagraph, tvProgress, tvInstruction;
    private ImageView imgHouse;
    private LinearLayout layoutOptions;
    private int totalScore = 0;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private List<KnowledgeSurveyQuestion> questionList;
    private int currentIdx = 0;
    private List<String> selectedErrors = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knowledge_survey);

        db = AppDatabase.getInstance(this);
        initViews();
        questionList = KnowledgeSurveyProvider.getHouseQuestions();

        if (!questionList.isEmpty()) {
            displayQuestion(questionList.get(currentIdx));
        }
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvQuestionTitle);
        tvParagraph = findViewById(R.id.tvParagraph);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvProgress = findViewById(R.id.tvProgress);
        imgHouse = findViewById(R.id.imgHouseReference);
        layoutOptions = findViewById(R.id.layoutOptions);
    }

    private void displayQuestion(KnowledgeSurveyQuestion question) {
        selectedErrors.clear();
        layoutOptions.removeAllViews();
        tvTitle.setText(question.title);
        imgHouse.setImageResource(question.imageRes);
        tvProgress.setText((currentIdx + 1) + " / " + questionList.size());

        switch (question.type) {
            case EDUCATION_LEVEL:
                tvInstruction.setText("Select your background");
                tvParagraph.setText(question.paragraph);
                layoutOptions.setVisibility(View.VISIBLE);
                setupOptionButtons(question);
                break;

            case FIND_ERROR:
                tvInstruction.setText("Tap on the logic errors in the text");
                layoutOptions.setVisibility(View.GONE);
                setupInteractiveText(question);
                break;

            case MULTIPLE_CHOICE:
                tvInstruction.setText("Choose the correct answer");
                tvParagraph.setText(question.paragraph);
                layoutOptions.setVisibility(View.VISIBLE);
                setupOptionButtons(question);
                break;
        }
    }

    private void setupOptionButtons(KnowledgeSurveyQuestion question) {
        for (String option : question.options) {
            Button btn = new Button(this);
            btn.setText(option);
            btn.setAllCaps(false);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 12);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> {
                if (question.type == KnowledgeSurveyQuestion.QuestionType.EDUCATION_LEVEL) {
                    btn.setBackgroundColor(Color.LTGRAY);
                    totalScore++;
                    moveToNext();
                } else {
                    if (option.equals(question.correctAnswers)) {
                        btn.setBackgroundColor(Color.GREEN);
                        totalScore++;
                        moveToNext();
                    } else {
                        btn.setBackgroundColor(Color.RED);
                        Toast.makeText(this, "Wrong answer, try again!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            layoutOptions.addView(btn);
        }
    }

    private void setupInteractiveText(KnowledgeSurveyQuestion question) {
        String text = question.paragraph;
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        String[] words = text.split("\\s+");
        int pos = 0;

        for (String word : words) {
            final String clean = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
            int start = text.indexOf(word, pos);
            int end = start + word.length();
            pos = end;

            ssb.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    if (selectedErrors.contains(clean)) selectedErrors.remove(clean);
                    else selectedErrors.add(clean);
                    setupInteractiveText(question);
                    checkFindErrorResult(question);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    ds.setColor(selectedErrors.contains(clean) ? Color.RED : Color.BLACK);
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tvParagraph.setText(ssb);
        tvParagraph.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void checkFindErrorResult(KnowledgeSurveyQuestion q) {
        int correctCount = 0;
        for (String s : selectedErrors) {
            if (q.wrongWords.contains(s)) correctCount++;
        }
        if (correctCount == q.wrongWords.size() && selectedErrors.size() == correctCount) {
            Toast.makeText(this, "Correct! Moving on...", Toast.LENGTH_SHORT).show();
            moveToNext();
        }
    }

    private void moveToNext() {
        new Handler().postDelayed(() -> {
            currentIdx++;
            if (currentIdx < questionList.size()) {
                displayQuestion(questionList.get(currentIdx));
            } else {
                finishSurvey();
            }
        }, 1000);
    }

    private void finishSurvey() {
        int userId = getSharedPreferences("RiseOfCity_Prefs", MODE_PRIVATE).getInt("logged_user_id", -1);

        executorService.execute(() -> {
            // 1. Cập nhật trạng thái vào Database
            if (userId != -1) {
                db.userDao().updateSurveyStatus(userId, true);
            }

            runOnUiThread(() -> {
                // 2. Lưu trạng thái vào SharedPreferences (SỬA LẠI TÊN PREFS CHO KHỚP)
                getSharedPreferences("RiseOfCity_Prefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("is_survey_completed", true)
                        .putBoolean("house_lv1_unlocked", true)
                        .apply();

                // 3. Quay về MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("SHOW_SURVEY_DIALOG", true);
                intent.putExtra("SCORE", totalScore);
                intent.putExtra("TOTAL", questionList.size());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        });
    }
}