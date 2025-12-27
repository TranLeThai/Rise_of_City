package com.example.rise_of_city.ui.lesson;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button; // Thêm nút bắt đầu bài học
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.repository.LessonRepository;
import com.example.rise_of_city.ui.game.ingame.LessonActivity; // Import để chuyển sang màn hình Quiz

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LessonDetailActivity extends AppCompatActivity {
    private Lesson lesson;
    private LessonRepository lessonRepository;

    private ImageButton btnBack;
    private Button btnStartLesson; // Nút bắt đầu game/quiz
    private TextView tvLessonTitle, tvLessonAuthor, tvLessonDate, tvLessonLevel, tvLessonContent, tvLessonViews;
    private ScrollView scrollView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_detail);

        // Khởi tạo Repository
        lessonRepository = LessonRepository.getInstance();

        // Nhận dữ liệu bài học
        lesson = (Lesson) getIntent().getSerializableExtra("lesson");
        String topicId = getIntent().getStringExtra("topicId");

        initViews();
        setupListeners(topicId);

        if (lesson != null) {
            displayLesson(lesson);
            // Tăng lượt xem (không liên quan gì tới Survey)
            if (topicId != null && lesson.getId() != null) {
                lessonRepository.incrementViewCount(topicId, lesson.getId());
            }
        } else {
            // Nếu không có lesson, báo lỗi và thoát
            Toast.makeText(this, "Thưa Thị trưởng, không tìm thấy nội dung bài học!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnStartLesson = findViewById(R.id.btn_start_lesson); // Đảm bảo bạn đã thêm ID này vào XML
        tvLessonTitle = findViewById(R.id.tv_lesson_title);
        tvLessonAuthor = findViewById(R.id.tv_lesson_author);
        tvLessonDate = findViewById(R.id.tv_lesson_date);
        tvLessonLevel = findViewById(R.id.tv_lesson_level);
        tvLessonContent = findViewById(R.id.tv_lesson_content);
        tvLessonViews = findViewById(R.id.tv_lesson_views);
        scrollView = findViewById(R.id.scroll_view);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners(String topicId) {
        btnBack.setOnClickListener(v -> finish());

        // Chuyển sang màn hình chơi game (LessonActivity) đã sửa ở bước trước
        if (btnStartLesson != null) {
            btnStartLesson.setOnClickListener(v -> {
                Intent intent = new Intent(this, LessonActivity.class);
                // Bạn có thể truyền ID bài học hoặc danh sách câu hỏi qua đây
                intent.putExtra("lessonId", lesson.getId());
                startActivity(intent);
            });
        }
    }

    private void displayLesson(Lesson lesson) {
        showLoading(false);

        tvLessonTitle.setText(lesson.getTitle() != null ? lesson.getTitle() : "Chưa có tiêu đề");
        tvLessonAuthor.setText("Bởi: " + (lesson.getAuthorName() != null ? lesson.getAuthorName() : "Hệ thống"));
        tvLessonViews.setText(lesson.getViewCount() + " lượt xem");

        // Định dạng ngày tháng
        if (lesson.getCreatedAt() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvLessonDate.setText("Ngày đăng: " + sdf.format(new Date(lesson.getCreatedAt())));
        }

        // Cấp độ bài học
        if (lesson.getLevel() != null && !lesson.getLevel().isEmpty()) {
            tvLessonLevel.setText(lesson.getLevel());
            tvLessonLevel.setVisibility(View.VISIBLE);
        } else {
            tvLessonLevel.setVisibility(View.GONE);
        }

        // Xử lý nội dung Markdown/HTML đơn giản
        parseAndDisplayContent(lesson.getContent());
    }

    private void parseAndDisplayContent(String content) {
        if (content == null || content.isEmpty()) {
            tvLessonContent.setText("Nội dung đang được cập nhật...");
            return;
        }

        // Chuyển đổi Markdown cơ bản sang HTML
        String formattedContent = content
                .replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>") // Bold
                .replaceAll("### (.+)", "<h3>$1</h3>")        // Header 3
                .replaceAll("## (.+)", "<h2>$1</h2>")         // Header 2
                .replaceAll("# (.+)", "<h1>$1</h1>")          // Header 1
                .replace("\n", "<br>");                        // Xuống dòng

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvLessonContent.setText(Html.fromHtml(formattedContent, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvLessonContent.setText(Html.fromHtml(formattedContent));
        }
        tvLessonContent.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void showLoading(boolean show) {
        if (progressBar != null) progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (scrollView != null) scrollView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}