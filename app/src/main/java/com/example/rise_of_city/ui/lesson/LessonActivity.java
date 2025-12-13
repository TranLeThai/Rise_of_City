package com.example.rise_of_city.ui.lesson;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rise_of_city.R;

public class LessonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        String lessonName = getIntent().getStringExtra("lessonName");
        if (lessonName == null) {
            lessonName = "Thì hiện tại đơn";
        }

        TextView tvLessonTitle = findViewById(R.id.tv_lesson_title);
        tvLessonTitle.setText(lessonName);

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }
}

