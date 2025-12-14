package com.example.rise_of_city.ui.test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rise_of_city.R;
import com.example.rise_of_city.ui.quiz.VocabularyQuizActivity;

public class TestVocabularyQuizActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_vocabulary_quiz);

        Button btnTest = findViewById(R.id.btn_test_quiz);
        btnTest.setOnClickListener(v -> {
            Intent intent = new Intent(this, VocabularyQuizActivity.class);
            startActivity(intent);
        });
    }
}
