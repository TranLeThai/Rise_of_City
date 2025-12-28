package com.example.rise_of_city.ui.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.ChatMessageEntity;
import com.example.rise_of_city.data.local.Friend;
import com.example.rise_of_city.data.local.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    
    private static final String TAG = "ChatActivity";
    
    private int currentUserId;
    private int otherUserId;
    private String otherUserName;
    
    private AppDatabase appDatabase;
    private ExecutorService executorService;
    
    private Toolbar toolbar;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvEmptyState;
    
    private ChatAdapter chatAdapter;
    private List<ChatMessageEntity> messages;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        // Get user IDs from intent
        otherUserId = getIntent().getIntExtra("other_user_id", -1);
        otherUserName = getIntent().getStringExtra("other_user_name");
        
        // Get current user ID
        SharedPreferences prefs = getSharedPreferences("RiseOfCity_Prefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getInt("logged_user_id", -1);
        
        if (currentUserId == -1 || otherUserId == -1) {
            Toast.makeText(this, "Lỗi: Không xác định được người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize database
        appDatabase = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        
        // Initialize views
        initViews();
        
        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(otherUserName != null ? otherUserName : "Chat");
        }
        
        // Setup RecyclerView
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(chatAdapter);
        rvMessages.setHasFixedSize(true);
        rvMessages.setItemViewCacheSize(20);
        
        // Setup send button
        btnSend.setOnClickListener(v -> sendMessage());
        
        // Setup enter key to send message
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
        
        // Load messages
        loadMessages();
        
        // Check if users are friends
        checkFriendship();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvMessages = findViewById(R.id.rv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        tvEmptyState = findViewById(R.id.tv_empty_state);
    }
    
    private void checkFriendship() {
        executorService.execute(() -> {
            Friend friendship = appDatabase.friendDao().checkFriendship(currentUserId, otherUserId);
            
            runOnUiThread(() -> {
                if (friendship == null || !"accepted".equals(friendship.status)) {
                    Toast.makeText(this, "Bạn cần kết bạn trước khi nhắn tin", Toast.LENGTH_SHORT).show();
                    etMessage.setEnabled(false);
                    btnSend.setEnabled(false);
                    etMessage.setHint("Bạn cần kết bạn trước khi nhắn tin");
                } else {
                    etMessage.setEnabled(true);
                    btnSend.setEnabled(true);
                    etMessage.setHint("Nhập tin nhắn...");
                }
            });
        });
    }
    
    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        
        if (messageText.isEmpty()) {
            return;
        }
        
        // Kiểm tra lại friendship trước khi gửi
        executorService.execute(() -> {
            Friend friendship = appDatabase.friendDao().checkFriendship(currentUserId, otherUserId);
            
            if (friendship == null || !"accepted".equals(friendship.status)) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Bạn cần kết bạn trước khi nhắn tin", Toast.LENGTH_SHORT).show();
                    etMessage.setEnabled(false);
                    btnSend.setEnabled(false);
                });
                return;
            }
            
            // Disable send button while sending
            runOnUiThread(() -> btnSend.setEnabled(false));
            
            try {
                long timestamp = System.currentTimeMillis();
                
                // Tạo message với senderId và receiverId đúng
                ChatMessageEntity message = new ChatMessageEntity(
                    currentUserId,  // senderId
                    otherUserId,    // receiverId
                    messageText,
                    "USER",
                    timestamp
                );
                
                // Chỉ insert 1 message duy nhất với senderId và receiverId
                appDatabase.chatDao().insertMessage(message);
                
                runOnUiThread(() -> {
                    etMessage.setText("");
                    btnSend.setEnabled(true);
                    // Add message to list and notify adapter
                    messages.add(message);
                    int position = messages.size() - 1;
                    chatAdapter.notifyItemInserted(position);
                    rvMessages.post(() -> {
                        if (position >= 0 && position < messages.size()) {
                            rvMessages.smoothScrollToPosition(position);
                        }
                    });
                });
            } catch (Exception e) {
                Log.e(TAG, "Error sending message", e);
                runOnUiThread(() -> {
                    btnSend.setEnabled(true);
                    Toast.makeText(this, "Lỗi khi gửi tin nhắn", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void loadMessages() {
        executorService.execute(() -> {
            try {
                // Kiểm tra friendship trước khi load messages
                Friend friendship = appDatabase.friendDao().checkFriendship(currentUserId, otherUserId);
                
                if (friendship == null || !"accepted".equals(friendship.status)) {
                    runOnUiThread(() -> {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("Bạn cần kết bạn trước khi xem tin nhắn");
                        rvMessages.setVisibility(View.GONE);
                    });
                    return;
                }
                
                // Load conversation giữa 2 users (chỉ tin nhắn giữa họ)
                List<ChatMessageEntity> conversationMessages = appDatabase.chatDao().getConversation(currentUserId, otherUserId);
                
                runOnUiThread(() -> {
                    messages.clear();
                    messages.addAll(conversationMessages);
                    chatAdapter.notifyDataSetChanged();
                    
                    if (messages.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setText("Chưa có tin nhắn nào");
                        rvMessages.setVisibility(View.GONE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                        rvMessages.setVisibility(View.VISIBLE);
                        // Scroll to bottom after layout
                        rvMessages.post(() -> {
                            if (messages.size() > 0) {
                                rvMessages.scrollToPosition(messages.size() - 1);
                            }
                        });
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading messages", e);
                runOnUiThread(() -> {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText("Lỗi khi tải tin nhắn");
                    rvMessages.setVisibility(View.GONE);
                });
            }
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}

