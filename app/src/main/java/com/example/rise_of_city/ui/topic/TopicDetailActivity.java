package com.example.rise_of_city.ui.topic;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.SearchTopic;
import com.example.rise_of_city.ui.ingame.InGameActivity;
import com.example.rise_of_city.ui.lesson.LessonActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class TopicDetailActivity extends AppCompatActivity {
    private static final String TAG = "TopicDetailActivity";
    
    private String topicId;
    private SearchTopic topic;
    private FirebaseFirestore db;
    
    private ImageButton btnBack;
    private ImageView ivTopicImage;
    private TextView tvTopicTitle, tvTopicDescription, tvTopicLevel, tvTopicCategory, tvLessonCount;
    private Button btnStartLearning;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_detail);
        
        // Get topic from intent
        topicId = getIntent().getStringExtra("topicId");
        topic = (SearchTopic) getIntent().getSerializableExtra("topic");
        
        if (topicId == null && topic == null) {
            Toast.makeText(this, "Không tìm thấy thông tin chủ đề", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        db = FirebaseFirestore.getInstance();
        
        initViews();
        setupClickListeners();
        
        if (topic != null) {
            displayTopicInfo(topic);
        } else {
            loadTopicDetail();
        }
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivTopicImage = findViewById(R.id.ivTopicImage);
        tvTopicTitle = findViewById(R.id.tvTopicTitle);
        tvTopicDescription = findViewById(R.id.tvTopicDescription);
        tvTopicLevel = findViewById(R.id.tvTopicLevel);
        tvTopicCategory = findViewById(R.id.tvTopicCategory);
        tvLessonCount = findViewById(R.id.tvLessonCount);
        btnStartLearning = findViewById(R.id.btnStartLearning);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnStartLearning.setOnClickListener(v -> {
            if (topic == null) {
                Toast.makeText(this, "Đang tải thông tin chủ đề...", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Navigate based on category/building
            String category = topic.getCategory();
            if (category != null) {
                switch (category) {
                    case "School":
                    case "Coffee Shop":
                    case "Park":
                    case "House":
                    case "Library":
                        // Navigate to InGameActivity and highlight the building
                        navigateToBuilding(category);
                        break;
                    case "General":
                    default:
                        // Navigate to lesson list
                        navigateToLessons();
                        break;
                }
            } else {
                // Default: navigate to lessons
                navigateToLessons();
            }
        });
    }
    
    private void navigateToBuilding(String buildingName) {
        Intent intent = new Intent(this, InGameActivity.class);
        intent.putExtra("buildingName", buildingName);
        intent.putExtra("topicId", topicId != null ? topicId : topic.getId());
        startActivity(intent);
        finish();
    }
    
    private void navigateToLessons() {
        // Navigate to lesson activity with topic filter
        Intent intent = new Intent(this, LessonActivity.class);
        intent.putExtra("topicId", topicId != null ? topicId : topic.getId());
        intent.putExtra("topicTitle", topic.getTitle());
        startActivity(intent);
    }
    
    private void loadTopicDetail() {
        showLoading(true);
        hideEmptyState();
        
        db.collection("topics")
                .document(topicId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false);
                    
                    if (documentSnapshot.exists()) {
                        topic = documentFromSnapshot(documentSnapshot);
                        displayTopicInfo(topic);
                    } else {
                        showEmptyState("Không tìm thấy thông tin chủ đề");
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error loading topic detail: ", e);
                    showEmptyState("Lỗi khi tải thông tin chủ đề");
                    Toast.makeText(this, "Lỗi khi tải thông tin", Toast.LENGTH_SHORT).show();
                });
    }
    
    private SearchTopic documentFromSnapshot(DocumentSnapshot document) {
        SearchTopic topic = new SearchTopic();
        topic.setId(document.getId());
        topic.setTitle(document.getString("title"));
        topic.setDescription(document.getString("description"));
        topic.setLevel(document.getString("level"));
        topic.setCategory(document.getString("category"));
        
        Object lessonCountObj = document.get("lessonCount");
        if (lessonCountObj != null) {
            topic.setLessonCount(((Long) lessonCountObj).intValue());
        }
        
        topic.setImageUrl(document.getString("imageUrl"));
        return topic;
    }
    
    private void displayTopicInfo(SearchTopic topic) {
        // Title
        if (topic.getTitle() != null) {
            tvTopicTitle.setText(topic.getTitle());
        }
        
        // Description
        if (topic.getDescription() != null) {
            tvTopicDescription.setText(topic.getDescription());
            tvTopicDescription.setVisibility(View.VISIBLE);
        } else {
            tvTopicDescription.setVisibility(View.GONE);
        }
        
        // Level
        if (topic.getLevel() != null && !topic.getLevel().isEmpty()) {
            tvTopicLevel.setText("Trình độ: " + topic.getLevel());
            tvTopicLevel.setVisibility(View.VISIBLE);
        } else {
            tvTopicLevel.setVisibility(View.GONE);
        }
        
        // Category/Building
        if (topic.getCategory() != null && !topic.getCategory().isEmpty()) {
            String categoryText = "Khu vực: " + getCategoryDisplayName(topic.getCategory());
            tvTopicCategory.setText(categoryText);
            tvTopicCategory.setVisibility(View.VISIBLE);
        } else {
            tvTopicCategory.setVisibility(View.GONE);
        }
        
        // Lesson Count
        if (topic.getLessonCount() > 0) {
            tvLessonCount.setText(topic.getLessonCount() + " bài học");
            tvLessonCount.setVisibility(View.VISIBLE);
        } else {
            tvLessonCount.setVisibility(View.GONE);
        }
        
        // Image (if available)
        if (topic.getImageUrl() != null && !topic.getImageUrl().isEmpty()) {
            // TODO: Load image using Glide or Picasso
            // Glide.with(this).load(topic.getImageUrl()).into(ivTopicImage);
        } else {
            // Set default image based on category
            setDefaultImage(topic.getCategory());
        }
    }
    
    private String getCategoryDisplayName(String category) {
        switch (category) {
            case "School": return "Trường học";
            case "Coffee Shop": return "Quán cà phê";
            case "Park": return "Công viên";
            case "House": return "Nhà ở";
            case "Library": return "Thư viện";
            case "General": return "Chung";
            default: return category;
        }
    }
    
    private void setDefaultImage(String category) {
        // Set default image based on category
        // You can add specific drawable resources for each building
        int defaultIcon = android.R.drawable.ic_menu_info_details;
        if (category != null) {
            switch (category) {
                case "School":
                    defaultIcon = android.R.drawable.ic_menu_agenda;
                    break;
                case "Coffee Shop":
                    defaultIcon = android.R.drawable.ic_menu_always_landscape_portrait;
                    break;
                case "Park":
                    defaultIcon = android.R.drawable.ic_menu_view;
                    break;
                case "House":
                    defaultIcon = android.R.drawable.ic_menu_myplaces;
                    break;
                case "Library":
                    defaultIcon = android.R.drawable.ic_menu_sort_by_size;
                    break;
            }
        }
        ivTopicImage.setImageResource(defaultIcon);
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
    }
    
    private void hideEmptyState() {
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
        }
    }
}

