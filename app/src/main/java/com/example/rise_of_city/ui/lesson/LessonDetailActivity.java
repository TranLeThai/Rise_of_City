package com.example.rise_of_city.ui.lesson;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.Lesson;
import com.example.rise_of_city.data.repository.LessonRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LessonDetailActivity extends AppCompatActivity {
    private static final String TAG = "LessonDetailActivity";
    
    private Lesson lesson;
    private LessonRepository lessonRepository;
    
    private ImageButton btnBack;
    private TextView tvLessonTitle;
    private TextView tvLessonAuthor;
    private TextView tvLessonDate;
    private TextView tvLessonLevel;
    private TextView tvLessonContent;
    private TextView tvLessonViews;
    private ScrollView scrollView;
    private ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_detail);
        
        // Get lesson from intent
        lesson = (Lesson) getIntent().getSerializableExtra("lesson");
        String lessonId = getIntent().getStringExtra("lessonId");
        String topicId = getIntent().getStringExtra("topicId");
        
        if (lesson == null && (lessonId == null || topicId == null)) {
            Toast.makeText(this, "Không tìm thấy thông tin bài học", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        lessonRepository = LessonRepository.getInstance();
        
        initViews();
        setupListeners();
        
        if (lesson != null) {
            displayLesson(lesson);
            // Increment view count
            if (topicId != null && lesson.getId() != null) {
                lessonRepository.incrementViewCount(topicId, lesson.getId());
            }
        } else {
            // Load lesson from Firestore
            loadLesson(topicId, lessonId);
        }
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvLessonTitle = findViewById(R.id.tv_lesson_title);
        tvLessonAuthor = findViewById(R.id.tv_lesson_author);
        tvLessonDate = findViewById(R.id.tv_lesson_date);
        tvLessonLevel = findViewById(R.id.tv_lesson_level);
        tvLessonContent = findViewById(R.id.tv_lesson_content);
        tvLessonViews = findViewById(R.id.tv_lesson_views);
        scrollView = findViewById(R.id.scroll_view);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void loadLesson(String topicId, String lessonId) {
        showLoading(true);
        // Note: We need to load the lesson from Firestore
        // For now, show error if lesson is not passed
        showLoading(false);
        Toast.makeText(this, "Vui lòng thử lại", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    private void displayLesson(Lesson lesson) {
        showLoading(false);
        
        // Title
        if (lesson.getTitle() != null) {
            tvLessonTitle.setText(lesson.getTitle());
        }
        
        // Author
        String authorName = lesson.getAuthorName() != null ? lesson.getAuthorName() : "Ẩn danh";
        tvLessonAuthor.setText("Bởi: " + authorName);
        
        // Date
        if (lesson.getCreatedAt() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateStr = sdf.format(new Date(lesson.getCreatedAt()));
            tvLessonDate.setText("Ngày đăng: " + dateStr);
        }
        
        // Level
        if (lesson.getLevel() != null && !lesson.getLevel().isEmpty()) {
            tvLessonLevel.setText(lesson.getLevel());
            tvLessonLevel.setVisibility(View.VISIBLE);
        } else {
            tvLessonLevel.setVisibility(View.GONE);
        }
        
        // Views
        tvLessonViews.setText(lesson.getViewCount() + " lượt xem");
        
        // Content - Parse markdown/HTML
        if (lesson.getContent() != null && !lesson.getContent().isEmpty()) {
            String content = lesson.getContent();
            
            // Convert markdown-style headers to HTML (multiline)
            String[] lines = content.split("\n");
            StringBuilder htmlContent = new StringBuilder();
            
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    htmlContent.append("<br/>");
                    continue;
                }
                
                // Headers
                if (line.startsWith("### ")) {
                    htmlContent.append("<h3>").append(line.substring(4)).append("</h3>");
                } else if (line.startsWith("## ")) {
                    htmlContent.append("<h2>").append(line.substring(3)).append("</h2>");
                } else if (line.startsWith("# ")) {
                    htmlContent.append("<h1>").append(line.substring(2)).append("</h1>");
                } else {
                    // Convert bold **text** to <b>text</b>
                    String processedLine = line.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
                    // Convert italic *text* to <i>text</i> (but not if it's part of **)
                    processedLine = processedLine.replaceAll("(?<!\\*)\\*([^*]+?)\\*(?!\\*)", "<i>$1</i>");
                    htmlContent.append("<p>").append(processedLine).append("</p>");
                }
            }
            
            // Display as HTML
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                tvLessonContent.setText(Html.fromHtml(htmlContent.toString(), Html.FROM_HTML_MODE_LEGACY));
            } else {
                tvLessonContent.setText(Html.fromHtml(htmlContent.toString()));
            }
            
            tvLessonContent.setMovementMethod(LinkMovementMethod.getInstance());
            tvLessonContent.setVisibility(View.VISIBLE);
        } else {
            tvLessonContent.setText("Nội dung bài học đang được cập nhật...");
            tvLessonContent.setVisibility(View.VISIBLE);
        }
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (scrollView != null) {
            scrollView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}

