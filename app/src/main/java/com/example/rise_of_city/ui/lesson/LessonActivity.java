package com.example.rise_of_city.ui.lesson;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.adapter.LessonAdapter;
import com.example.rise_of_city.data.model.Lesson;
import com.example.rise_of_city.data.repository.LessonRepository;

import java.util.ArrayList;
import java.util.List;

public class LessonActivity extends AppCompatActivity {
    private static final String TAG = "LessonActivity";
    
    private String topicId;
    private String topicTitle;
    private LessonRepository lessonRepository;
    
    private ImageButton btnBack;
    private TextView tvTopicTitle;
    private RecyclerView rvLessons;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private LessonAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        
        // Get topic info from intent
        topicId = getIntent().getStringExtra("topicId");
        topicTitle = getIntent().getStringExtra("topicTitle");
        
        
        if (topicId == null || topicId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin chủ đề", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        lessonRepository = LessonRepository.getInstance();
        
        initViews();
        setupRecyclerView();
        loadLessons();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvTopicTitle = findViewById(R.id.tv_topic_title);
        rvLessons = findViewById(R.id.rv_lessons);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        
        if (tvTopicTitle != null) {
            tvTopicTitle.setText(topicTitle != null ? topicTitle : "Bài học");
        }
        
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void setupRecyclerView() {
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LessonAdapter(new ArrayList<>(), lesson -> {
            // Navigate to lesson detail
            Intent intent = new Intent(LessonActivity.this, LessonDetailActivity.class);
            intent.putExtra("lesson", lesson);
            intent.putExtra("topicId", topicId);
            startActivity(intent);
        });
        rvLessons.setAdapter(adapter);
    }
    
    private void loadLessons() {
        showLoading(true);
        hideEmptyState();
        
        lessonRepository.getLessonsByTopic(topicId, new LessonRepository.OnLessonsLoadedListener() {
            @Override
            public void onLessonsLoaded(List<Lesson> lessons) {
                showLoading(false);
                if (lessons.isEmpty()) {
                    showEmptyState("Chưa có bài học nào cho chủ đề này");
                } else {
                    hideEmptyState();
                    adapter.updateLessons(lessons);
                }
            }
            
            @Override
            public void onError(String error) {
                showLoading(false);
                Log.e(TAG, "Error loading lessons: " + error);
                showEmptyState("Lỗi khi tải danh sách bài học");
                Toast.makeText(LessonActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    private void showEmptyState(String message) {
        if (tvEmptyState != null) {
            tvEmptyState.setText(message);
            tvEmptyState.setVisibility(View.VISIBLE);
        }
        if (rvLessons != null) {
            rvLessons.setVisibility(View.GONE);
        }
    }
    
    private void hideEmptyState() {
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
        }
        if (rvLessons != null) {
            rvLessons.setVisibility(View.VISIBLE);
        }
    }
}

