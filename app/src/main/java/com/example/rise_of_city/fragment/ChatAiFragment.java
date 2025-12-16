package com.example.rise_of_city.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.adapter.ChatMessageAdapter;
import com.example.rise_of_city.data.model.ChatMessage;
import com.example.rise_of_city.service.GeminiService;
import com.example.rise_of_city.utils.ApiKeyManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatAiFragment extends Fragment {
    private static final String TAG = "ChatAiFragment";
    private static final String CHAT_COLLECTION = "ai_chat_history";
    
    private RecyclerView rvChatMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnMic;
    private ImageButton btnMenu;
    private ChatMessageAdapter adapter;
    private GeminiService geminiService;
    private List<String> conversationHistory;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_ai, container, false);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        initViews(view);
        setupRecyclerView();
        setupGeminiService();
        setupClickListeners();
        
        // Load chat history từ Firestore
        loadChatHistory();
        
        return view;
    }
    
    private void initViews(View view) {
        rvChatMessages = view.findViewById(R.id.rvChatMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        btnMic = view.findViewById(R.id.btnMic);
        btnMenu = view.findViewById(R.id.btnMenu);
        conversationHistory = new ArrayList<>();
    }
    
    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter();
        rvChatMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChatMessages.setAdapter(adapter);
    }
    
    private void setupGeminiService() {
        String apiKey = ApiKeyManager.getApiKey(getContext());
        
        if (!ApiKeyManager.isApiKeyConfigured(getContext())) {
            Toast.makeText(getContext(), 
                "Vui lòng cấu hình Gemini API Key trong ApiKeyManager.java", 
                Toast.LENGTH_LONG).show();
            Log.e(TAG, "Gemini API Key chưa được cấu hình!");
        }
        
        geminiService = new GeminiService(apiKey);
    }
    
    private void setupClickListeners() {
        // Nút Send
        btnSend.setOnClickListener(v -> sendMessage());
        
        // Gửi tin nhắn khi nhấn Enter hoặc nút Send trên bàn phím
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                sendMessage();
                return true;
            }
            return false;
        });
        
        // Nút menu - refresh và xóa lịch sử
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> showMenu(v));
        }
        
        // Nút microphone (tính năng voice input - có thể phát triển sau)
        btnMic.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tính năng voice input đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }
    
    /**
     * Load chat history từ Firestore
     */
    private void loadChatHistory() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            sendWelcomeMessage();
            return;
        }
        
        adapter.clearMessages();
        conversationHistory.clear();
        
        db.collection(CHAT_COLLECTION)
                .document(user.getUid())
                .collection("messages")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Nếu chưa có lịch sử, gửi tin nhắn chào mừng
                        sendWelcomeMessage();
                        return;
                    }
                    
                    // Load messages từ Firestore
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String message = document.getString("message");
                        String type = document.getString("type");
                        Long timestamp = document.getLong("timestamp");
                        
                        if (message != null && type != null) {
                            ChatMessage.MessageType messageType = 
                                "USER".equals(type) ? ChatMessage.MessageType.USER : ChatMessage.MessageType.AI;
                            
                            ChatMessage chatMessage = new ChatMessage(message, messageType);
                            if (timestamp != null) {
                                chatMessage.setTimestamp(timestamp);
                            }
                            
                            adapter.addMessage(chatMessage);
                            
                            // Thêm vào conversation history để context
                            conversationHistory.add(message);
                        }
                    }
                    
                    scrollToBottom();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading chat history: ", e);
                    sendWelcomeMessage();
                });
    }
    
    /**
     * Lưu tin nhắn vào Firestore
     */
    private void saveMessageToFirestore(String message, ChatMessage.MessageType type) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return;
        }
        
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("message", message);
        messageData.put("type", type.name());
        messageData.put("timestamp", System.currentTimeMillis());
        
        db.collection(CHAT_COLLECTION)
                .document(user.getUid())
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Message saved: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving message: ", e);
                });
    }
    
    /**
     * Hiển thị dialog xác nhận xóa lịch sử
     */
    private void showClearHistoryDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa lịch sử chat")
                .setMessage("Bạn có chắc chắn muốn xóa toàn bộ lịch sử chat? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> clearChatHistory())
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    /**
     * Xóa toàn bộ lịch sử chat
     */
    private void clearChatHistory() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return;
        }
        
        db.collection(CHAT_COLLECTION)
                .document(user.getUid())
                .collection("messages")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    
                    adapter.clearMessages();
                    conversationHistory.clear();
                    sendWelcomeMessage();
                    Toast.makeText(getContext(), "Đã xóa lịch sử chat", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error clearing chat history: ", e);
                    Toast.makeText(getContext(), "Lỗi khi xóa lịch sử", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void sendWelcomeMessage() {
        String welcomeMessage = "Hello! I'm your AI English tutor. I'm here to help you practice English. " +
                "You can ask me questions, have conversations, or practice specific scenarios. How can I help you today?";
        
        ChatMessage aiMessage = new ChatMessage(welcomeMessage, ChatMessage.MessageType.AI);
        adapter.addMessage(aiMessage);
        scrollToBottom();
    }
    
    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        
        if (TextUtils.isEmpty(message)) {
            return;
        }
        
        // Thêm tin nhắn của user vào adapter
        ChatMessage userMessage = new ChatMessage(message, ChatMessage.MessageType.USER);
        adapter.addMessage(userMessage);
        conversationHistory.add(message);
        
        // Lưu tin nhắn user vào Firestore
        saveMessageToFirestore(message, ChatMessage.MessageType.USER);
        
        // Lưu tin nhắn user vào lịch sử
        etMessage.setText("");
        scrollToBottom();
        
        // Hiển thị loading message từ AI
        ChatMessage loadingMessage = new ChatMessage("", ChatMessage.MessageType.AI, true);
        adapter.addMessage(loadingMessage);
        adapter.setLastMessageLoading(true);
        scrollToBottom();
        
        // Gửi request đến Gemini API
        geminiService.sendMessage(message, conversationHistory, new GeminiService.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Cập nhật tin nhắn AI
                        adapter.updateLastMessage(response);
                        conversationHistory.add(response);
                        
                        // Lưu tin nhắn AI vào Firestore
                        saveMessageToFirestore(response, ChatMessage.MessageType.AI);
                        
                        scrollToBottom();
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        adapter.updateLastMessage("Xin lỗi, đã có lỗi xảy ra: " + error);
                        Log.e(TAG, "Gemini API Error: " + error);
                        Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
            
            @Override
            public void onPartialUpdate(String partialResponse) {
                // Update UI từng phần khi nhận được response (streaming-like)
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        adapter.updateLastMessage(partialResponse);
                        scrollToBottom();
                    });
                }
            }
            
            @Override
            public void onTruncated(String partialResponse, Runnable continueCallback) {
                // Tự động tiếp tục khi response bị cắt
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Cập nhật conversationHistory với phần đã nhận (tạm thời)
                        // Sẽ được cập nhật lại khi nhận được full response
                        String currentLastResponse = conversationHistory.isEmpty() ? "" : 
                            (conversationHistory.size() % 2 == 0 ? "" : conversationHistory.get(conversationHistory.size() - 1));
                        
                        // Update UI với phần đã nhận
                        adapter.updateLastMessage(partialResponse + "\n\n[Đang tiếp tục...]");
                        scrollToBottom();
                        
                        // Tự động tiếp tục sau 500ms để user thấy thông báo
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            continueCallback.run();
                        }, 500);
                    });
                } else {
                    continueCallback.run();
                }
            }
        });
    }
    
    /**
     * Hiển thị menu với các tùy chọn
     */
    private void showMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(getContext(), anchor);
        popupMenu.getMenu().add(0, 1, 0, "Làm mới");
        popupMenu.getMenu().add(0, 2, 0, "Xóa lịch sử");
        
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                // Refresh
                loadChatHistory();
                Toast.makeText(getContext(), "Đang tải lại lịch sử chat...", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == 2) {
                // Xóa lịch sử - hiển thị dialog xác nhận
                showClearHistoryDialog();
                return true;
            }
            return false;
        });
        
        popupMenu.show();
    }
    
    private void scrollToBottom() {
        if (rvChatMessages != null && adapter != null) {
            rvChatMessages.post(() -> {
                if (adapter.getItemCount() > 0) {
                    rvChatMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                }
            });
        }
    }
}

