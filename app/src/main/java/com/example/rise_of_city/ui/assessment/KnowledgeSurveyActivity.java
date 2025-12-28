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
import androidx.fragment.app.FragmentTransaction;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.SurveyAnswer;
import com.example.rise_of_city.ui.main.MainActivity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KnowledgeSurveyActivity extends AppCompatActivity {

    private TextView tvTitle, tvParagraph, tvProgress, tvInstruction, tvErrorCount;
    private ImageView imgHouse;
    private LinearLayout layoutOptions, layoutActionButtons;
    private Button btnSubmit, btnSkip;
    private int correctAnswers = 0;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    private List<KnowledgeSurveyQuestion> questionList;
    private int currentIdx = 0;
    private List<String> selectedErrors = new ArrayList<>();
    private KnowledgeSurveyQuestion currentQuestion;
    private List<SurveyAnswer> surveyAnswers = new ArrayList<>();
    private int userId;
    private int requiredErrorCount = 0;
    private SurveyAnswer currentAnswer; // Track current question's answer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knowledge_survey);

        db = AppDatabase.getInstance(this);
        userId = getSharedPreferences("RiseOfCity_Prefs", MODE_PRIVATE).getInt("logged_user_id", -1);
        
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
        tvErrorCount = findViewById(R.id.tvErrorCount);
        imgHouse = findViewById(R.id.imgHouseReference);
        layoutOptions = findViewById(R.id.layoutOptions);
        layoutActionButtons = findViewById(R.id.layoutActionButtons);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnSkip = findViewById(R.id.btnSkip);
    }

    private void displayQuestion(KnowledgeSurveyQuestion question) {
        currentQuestion = question;
        currentAnswer = null; // Reset current answer
        selectedErrors.clear();
        layoutOptions.removeAllViews();
        layoutActionButtons.setVisibility(View.GONE);
        tvErrorCount.setVisibility(View.GONE);
        btnSubmit.setEnabled(false);
        
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
                requiredErrorCount = question.wrongWords != null ? question.wrongWords.size() : 0;
                tvErrorCount.setText("Select " + requiredErrorCount + " error(s)");
                tvErrorCount.setVisibility(View.VISIBLE);
                layoutActionButtons.setVisibility(View.VISIBLE);
                setupInteractiveText(question);
                setupActionButtons(question);
                break;

            case MULTIPLE_CHOICE:
                tvInstruction.setText("Choose the correct answer");
                tvParagraph.setText(question.paragraph);
                layoutOptions.setVisibility(View.VISIBLE);
                setupOptionButtons(question);
                break;
        }
    }

    private void setupActionButtons(KnowledgeSurveyQuestion question) {
        btnSubmit.setOnClickListener(v -> {
            checkAndSubmitAnswer(question);
        });

        btnSkip.setOnClickListener(v -> {
            // Save as incorrect and move to next
            saveCurrentAnswer(question, new ArrayList<>(), false);
            moveToNext(false);
        });
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
                    saveCurrentAnswer(question, Arrays.asList(option), true);
                    moveToNext(true);
                } else {
                    boolean isCorrect = option.equals(question.correctAnswers);
                    saveCurrentAnswer(question, Arrays.asList(option), isCorrect);
                    if (isCorrect) {
                        btn.setBackgroundColor(Color.GREEN);
                        correctAnswers++;
                    } else {
                        btn.setBackgroundColor(Color.RED);
                    }
                    moveToNext(isCorrect);
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
            if (clean.isEmpty()) continue;
            
            int start = text.indexOf(word, pos);
            int end = start + word.length();
            pos = end;

            ssb.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    // Giới hạn số lượng từ được chọn bằng số lỗi cần tìm
                    if (selectedErrors.contains(clean)) {
                        selectedErrors.remove(clean);
                    } else {
                        if (selectedErrors.size() < requiredErrorCount) {
                            selectedErrors.add(clean);
                        } else {
                            Toast.makeText(KnowledgeSurveyActivity.this, 
                                "You can only select " + requiredErrorCount + " error(s)", 
                                Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    updateSubmitButton();
                    setupInteractiveText(question);
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

    private void updateSubmitButton() {
        btnSubmit.setEnabled(selectedErrors.size() == requiredErrorCount);
    }

    private void checkAndSubmitAnswer(KnowledgeSurveyQuestion question) {
        if (selectedErrors.size() != requiredErrorCount) {
            Toast.makeText(this, "Please select exactly " + requiredErrorCount + " error(s)", Toast.LENGTH_SHORT).show();
            return;
        }

        int correctCount = 0;
        for (String selected : selectedErrors) {
            if (question.wrongWords != null && question.wrongWords.contains(selected)) {
                correctCount++;
            }
        }

        boolean isCorrect = (correctCount == requiredErrorCount && selectedErrors.size() == requiredErrorCount);
        
        // Update or create current answer
        saveCurrentAnswer(question, new ArrayList<>(selectedErrors), isCorrect);
        
        // Show feedback and move to next (this is a survey, no retry allowed)
        if (isCorrect) {
            correctAnswers++;
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Incorrect.", Toast.LENGTH_SHORT).show();
        }
        
        // Highlight answers with feedback
        setupInteractiveTextWithFeedback(question, isCorrect);
        // Disable selection after submission
        btnSubmit.setEnabled(false);
        // Auto move to next after a delay (both correct and incorrect)
        new Handler().postDelayed(() -> {
            moveToNext(isCorrect);
        }, 1500);
    }

    private void setupInteractiveTextWithFeedback(KnowledgeSurveyQuestion question, boolean isCorrect) {
        String text = question.paragraph;
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        String[] words = text.split("\\s+");
        int pos = 0;

        for (String word : words) {
            final String clean = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
            if (clean.isEmpty()) continue;
            
            int start = text.indexOf(word, pos);
            int end = start + word.length();
            pos = end;

            final boolean isSelected = selectedErrors.contains(clean);
            final boolean isCorrectAnswer = question.wrongWords != null && question.wrongWords.contains(clean);

            ssb.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    // Disabled after submission
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    if (isSelected && isCorrectAnswer) {
                        ds.setColor(Color.GREEN);
                    } else if (isSelected && !isCorrectAnswer) {
                        ds.setColor(Color.RED);
                    } else if (!isSelected && isCorrectAnswer) {
                        ds.setColor(Color.BLUE);
                    } else {
                        ds.setColor(Color.BLACK);
                    }
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tvParagraph.setText(ssb);
        tvParagraph.setMovementMethod(null); // Disable clicking
    }

    private void saveCurrentAnswer(KnowledgeSurveyQuestion question, List<String> userAnswer, boolean isCorrect) {
        String userAnswerJson = gson.toJson(userAnswer);
        String correctAnswerJson = "";
        
        if (question.type == KnowledgeSurveyQuestion.QuestionType.FIND_ERROR) {
            correctAnswerJson = gson.toJson(question.wrongWords);
        } else if (question.type == KnowledgeSurveyQuestion.QuestionType.MULTIPLE_CHOICE) {
            correctAnswerJson = question.correctAnswers != null ? question.correctAnswers : "";
        } else if (question.type == KnowledgeSurveyQuestion.QuestionType.EDUCATION_LEVEL) {
            correctAnswerJson = userAnswerJson; // Education level has no correct answer
        }

        // Update existing answer or create new one
        if (currentAnswer == null) {
            currentAnswer = new SurveyAnswer(
                userId,
                currentIdx,
                question.title,
                question.type.name(),
                userAnswerJson,
                correctAnswerJson,
                isCorrect,
                System.currentTimeMillis()
            );
        } else {
            // Update existing answer
            currentAnswer.userAnswer = userAnswerJson;
            currentAnswer.isCorrect = isCorrect;
        }
    }
    
    private void finalizeCurrentAnswer() {
        if (currentAnswer != null) {
            // Remove old answer for this question if exists
            surveyAnswers.removeIf(answer -> answer.questionIndex == currentIdx);
            // Add current answer
            surveyAnswers.add(currentAnswer);
            currentAnswer = null;
        }
    }

    private void moveToNext(boolean wasCorrect) {
        // Finalize current answer before moving
        finalizeCurrentAnswer();
        
        new Handler().postDelayed(() -> {
            currentIdx++;
            if (currentIdx < questionList.size()) {
                displayQuestion(questionList.get(currentIdx));
            } else {
                finishSurvey();
            }
        }, wasCorrect ? 500 : 1000);
    }

    private void finishSurvey() {
        executorService.execute(() -> {
            // Delete old answers for this user
            if (userId != -1) {
                db.surveyAnswerDao().deleteSurveyAnswersByUserId(userId);
                
                // Save all answers to database
                for (SurveyAnswer answer : surveyAnswers) {
                    db.surveyAnswerDao().insertSurveyAnswer(answer);
                }
                
                // Update survey status and unlock house_lv1
                db.userDao().updateSurveyStatus(userId, true);
                
                // Unlock house building in Room database (level 1)
                com.example.rise_of_city.data.local.UserBuilding existingHouse = db.userBuildingDao().getBuilding(userId, "house");
                if (existingHouse == null) {
                    // House chưa tồn tại, tạo mới với level 1
                    com.example.rise_of_city.data.local.UserBuilding house = new com.example.rise_of_city.data.local.UserBuilding(userId, "house", 1);
                    db.userBuildingDao().insertOrUpdate(house);
                } else if (existingHouse.level == 0) {
                    // House tồn tại nhưng level = 0, update lên level 1
                    existingHouse.level = 1;
                    db.userBuildingDao().updateBuilding(existingHouse);
                }
            }

            runOnUiThread(() -> {
                // Save to SharedPreferences
                getSharedPreferences("RiseOfCity_Prefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("is_survey_completed", true)
                        .putBoolean("house_lv1_unlocked", true)
                        .apply();

                // Show result fragment
                showResultFragment();
            });
        });
    }

    private void showResultFragment() {
        SurveyResultFragment fragment = SurveyResultFragment.newInstance(surveyAnswers, correctAnswers, questionList.size());
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
