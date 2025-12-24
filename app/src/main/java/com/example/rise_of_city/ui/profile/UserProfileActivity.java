package com.example.rise_of_city.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.user.Badge;
import com.example.rise_of_city.data.repository.UserStatsRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";
    
    private String targetUserId;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    private ImageButton btnBack;
    private ImageView ivAvatar;
    private TextView tvUserName, tvUserLevel, tvUserStatus, tvJoinDate;
    private TextView tvStreak, tvTotalXP, tvCompletedBuildings, tvTotalVocabulary;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private View statsCard, badgesCard;
    private UserStatsRepository userStatsRepository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        
        // Get user ID from intent
        targetUserId = getIntent().getStringExtra("userId");
        if (targetUserId == null || targetUserId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userStatsRepository = UserStatsRepository.getInstance();
        
        initViews();
        setupClickListeners();
        loadUserProfile();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivAvatar = findViewById(R.id.ivAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserLevel = findViewById(R.id.tvUserLevel);
        tvUserStatus = findViewById(R.id.tvUserStatus);
        tvJoinDate = findViewById(R.id.tvJoinDate);
        tvStreak = findViewById(R.id.tvStreak);
        tvTotalXP = findViewById(R.id.tvTotalXP);
        tvCompletedBuildings = findViewById(R.id.tvCompletedBuildings);
        tvTotalVocabulary = findViewById(R.id.tvTotalVocabulary);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        statsCard = findViewById(R.id.statsCard);
        badgesCard = findViewById(R.id.badgesCard);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void loadUserProfile() {
        showLoading(true);
        hideEmptyState();
        
        db.collection("user_profiles")
                .document(targetUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false);
                    
                    if (documentSnapshot.exists()) {
                        displayUserInfo(documentSnapshot);
                    } else {
                        showEmptyState("Không tìm thấy thông tin người dùng");
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error loading user profile: ", e);
                    showEmptyState("Lỗi khi tải thông tin người dùng");
                    Toast.makeText(this, "Lỗi khi tải thông tin", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void displayUserInfo(DocumentSnapshot document) {
        // Chỉ hiển thị thông tin công khai, không hiển thị email, password, etc.
        String name = document.getString("name");
        String surveyLevel = document.getString("surveyLevel");
        String role = document.getString("role");
        Boolean isActive = document.getBoolean("isActive");
        
        // Hiển thị tên
        if (name != null && !name.isEmpty()) {
            tvUserName.setText(name);
        } else {
            tvUserName.setText("Người dùng");
        }
        
        // Hiển thị trình độ từ khảo sát
        if (surveyLevel != null && !surveyLevel.isEmpty()) {
            tvUserLevel.setText("Trình độ: " + surveyLevel);
            tvUserLevel.setVisibility(View.VISIBLE);
        } else {
            tvUserLevel.setText("Chưa hoàn thành khảo sát");
            tvUserLevel.setVisibility(View.VISIBLE);
        }
        
        // Hiển thị trạng thái
        if (isActive != null && isActive) {
            tvUserStatus.setText("Đang hoạt động");
            tvUserStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvUserStatus.setText("Không hoạt động");
            tvUserStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
        tvUserStatus.setVisibility(View.VISIBLE);
        
        // Hiển thị ngày tham gia (nếu có)
        Object createdAt = document.get("createdAt");
        if (createdAt != null) {
            // Có thể format date nếu cần
            tvJoinDate.setText("Đã tham gia");
            tvJoinDate.setVisibility(View.VISIBLE);
        } else {
            tvJoinDate.setVisibility(View.GONE);
        }
        
        // Avatar (nếu có)
        String avatarUrl = document.getString("avatarUrl");
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .circleCrop()
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
        }
        
        // Load stats (streak, totalXP, completed buildings, badges)
        loadUserStats(document);
        
        // Ẩn các thông tin nhạy cảm: email, phone, address, password, etc.
        // Không hiển thị những thông tin này trong profile công khai
    }
    
    private void loadUserStats(DocumentSnapshot document) {
        // Load streak
        Long streak = document.getLong("streak");
        if (streak != null && tvStreak != null) {
            tvStreak.setText(streak + " Ngày");
        } else if (tvStreak != null) {
            tvStreak.setText("0 Ngày");
        }
        
        // Load totalXP
        Long totalXP = document.getLong("totalXP");
        if (totalXP != null && tvTotalXP != null) {
            tvTotalXP.setText(String.valueOf(totalXP));
        } else if (tvTotalXP != null) {
            tvTotalXP.setText("0");
        }
        
        // Load totalVocabularyLearned
        Long totalVocab = document.getLong("totalVocabularyLearned");
        if (totalVocab != null && tvTotalVocabulary != null) {
            tvTotalVocabulary.setText(String.valueOf(totalVocab));
        } else if (tvTotalVocabulary != null) {
            tvTotalVocabulary.setText("0");
        }
        
        // Load completed buildings count
        db.collection("user_profiles")
                .document(targetUserId)
                .collection("buildings")
                .whereEqualTo("completed", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int completedCount = queryDocumentSnapshots.size();
                    if (tvCompletedBuildings != null) {
                        tvCompletedBuildings.setText(String.valueOf(completedCount));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading completed buildings: ", e);
                    if (tvCompletedBuildings != null) {
                        tvCompletedBuildings.setText("0");
                    }
                });
        
        // Load badges
        loadBadges();
    }
    
    private void loadBadges() {
        db.collection("user_profiles")
                .document(targetUserId)
                .collection("badges")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Badge> badges = new ArrayList<>();
                    for (QueryDocumentSnapshot badgeDoc : queryDocumentSnapshots) {
                        Badge badge = badgeDoc.toObject(Badge.class);
                        if (badge != null) {
                            badges.add(badge);
                        }
                    }
                    
                    // Display badges count or list
                    if (badgesCard != null) {
                        badgesCard.setVisibility(badges.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                    
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading badges: ", e);
                    if (badgesCard != null) {
                        badgesCard.setVisibility(View.GONE);
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
    }
    
    private void hideEmptyState() {
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
        }
    }
}

