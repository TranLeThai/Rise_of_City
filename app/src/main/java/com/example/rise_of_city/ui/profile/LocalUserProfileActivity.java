package com.example.rise_of_city.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.Friend;
import com.example.rise_of_city.data.local.User;
import com.example.rise_of_city.data.local.UserBuilding;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalUserProfileActivity extends AppCompatActivity {
    
    private int userId;
    private int currentUserId;
    private AppDatabase appDatabase;
    private ExecutorService executorService;
    
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private View contentLayout;
    private TextView tvEmptyState;
    
    // User info views
    private TextView tvUserName;
    private TextView tvStreak;
    private RecyclerView rvRoadmap;
    private Button btnAddFriend;
    private Button btnChat;
    
    private UserBuildingAdapter buildingAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_user_profile);
        
        // Get user ID from intent
        userId = getIntent().getIntExtra("user_id", -1);
        
        // Get current user ID
        SharedPreferences prefs = getSharedPreferences("RiseOfCity_Prefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getInt("logged_user_id", -1);
        
        // Initialize database
        appDatabase = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        
        // Initialize views
        initViews();
        
        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Hồ sơ người dùng");
        }
        
        // Setup RecyclerView
        rvRoadmap.setLayoutManager(new GridLayoutManager(this, 3));
        buildingAdapter = new UserBuildingAdapter(this);
        rvRoadmap.setAdapter(buildingAdapter);
        
        // Setup button listeners
        setupButtons();
        
        // Load user data
        if (userId != -1) {
            loadUserData();
            loadUserBuildings();
        } else {
            showEmptyState("Không tìm thấy thông tin người dùng");
        }
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        contentLayout = findViewById(R.id.content_layout);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        
        tvUserName = findViewById(R.id.tv_user_name);
        tvStreak = findViewById(R.id.tv_streak);
        rvRoadmap = findViewById(R.id.rv_roadmap);
        btnAddFriend = findViewById(R.id.btn_add_friend);
        btnChat = findViewById(R.id.btn_chat);
    }
    
    private void setupButtons() {
        // Check friendship status
        checkFriendshipStatus();
        
        btnAddFriend.setOnClickListener(v -> {
            sendFriendRequest();
        });
        
        btnChat.setOnClickListener(v -> {
            // Check if users are friends before allowing chat
            executorService.execute(() -> {
                Friend friendship = appDatabase.friendDao().checkFriendship(currentUserId, userId);
                
                runOnUiThread(() -> {
                    if (friendship == null) {
                        Toast.makeText(this, "Bạn cần kết bạn trước khi nhắn tin", Toast.LENGTH_SHORT).show();
                    } else {
                        // Open chat activity
                        android.content.Intent intent = new android.content.Intent(this, 
                            com.example.rise_of_city.ui.chat.ChatActivity.class);
                        intent.putExtra("other_user_id", userId);
                        intent.putExtra("other_user_name", tvUserName.getText().toString());
                        startActivity(intent);
                    }
                });
            });
        });
    }
    
    private void checkFriendshipStatus() {
        executorService.execute(() -> {
            // Kiểm tra đã là bạn bè chưa
            Friend friendship = appDatabase.friendDao().checkFriendship(currentUserId, userId);
            
            // Kiểm tra đã gửi lời mời chưa
            Friend pendingRequest = appDatabase.friendDao().checkPendingRequest(currentUserId, userId);
            
            runOnUiThread(() -> {
                if (friendship != null) {
                    // Đã là bạn bè
                    btnAddFriend.setText("Bạn bè");
                    btnAddFriend.setEnabled(false);
                    btnAddFriend.setAlpha(0.5f);
                } else if (pendingRequest != null) {
                    // Đã gửi lời mời
                    if (pendingRequest.requesterId == currentUserId) {
                        btnAddFriend.setText("Đã gửi lời mời");
                    } else {
                        btnAddFriend.setText("Chấp nhận lời mời");
                    }
                    btnAddFriend.setEnabled(true);
                } else {
                    // Chưa kết bạn
                    btnAddFriend.setText("Kết bạn");
                    btnAddFriend.setEnabled(true);
                }
            });
        });
    }
    
    private void sendFriendRequest() {
        if (currentUserId == -1 || userId == -1) {
            Toast.makeText(this, "Lỗi: Không xác định được người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        executorService.execute(() -> {
            // Kiểm tra lại trạng thái
            Friend existing = appDatabase.friendDao().checkPendingRequest(currentUserId, userId);
            
            if (existing != null) {
                if (existing.requesterId == userId) {
                    // Người kia đã gửi lời mời cho mình -> Chấp nhận luôn
                    existing.status = "accepted";
                    existing.updatedAt = System.currentTimeMillis();
                    appDatabase.friendDao().updateFriend(existing);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Đã chấp nhận lời mời kết bạn", Toast.LENGTH_SHORT).show();
                        checkFriendshipStatus();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Đã gửi lời mời trước đó", Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                // Gửi lời mời mới
                Friend newFriend = new Friend(currentUserId, userId, "pending", currentUserId);
                appDatabase.friendDao().insertFriend(newFriend);
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "Đã gửi lời mời kết bạn", Toast.LENGTH_SHORT).show();
                    checkFriendshipStatus();
                });
            }
        });
    }
    
    private void loadUserData() {
        showLoading(true);
        
        executorService.execute(() -> {
            User user = appDatabase.userDao().getUserById(userId);
            
            runOnUiThread(() -> {
                showLoading(false);
                
                if (user != null) {
                    displayUserData(user);
                } else {
                    showEmptyState("Không tìm thấy thông tin người dùng");
                }
            });
        });
    }
    
    private void loadUserBuildings() {
        executorService.execute(() -> {
            List<UserBuilding> buildings = appDatabase.userBuildingDao().getBuildingsForUser(userId);
            
            runOnUiThread(() -> {
                if (buildings != null && !buildings.isEmpty()) {
                    buildingAdapter.setBuildings(buildings);
                }
            });
        });
    }
    
    private void displayUserData(User user) {
        contentLayout.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
        
        // Set user name
        tvUserName.setText(user.fullName != null ? user.fullName : "Người dùng");
        
        // Set streak
        tvStreak.setText(user.streakDays + " ngày liên tiếp");
        
        // Update toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(user.fullName != null ? user.fullName : "Người dùng");
        }
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void showEmptyState(String message) {
        contentLayout.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.VISIBLE);
        tvEmptyState.setText(message);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}

