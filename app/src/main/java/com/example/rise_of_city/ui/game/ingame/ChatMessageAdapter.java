package com.example.rise_of_city.ui.game.ingame;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.user.ChatMessage;
import com.example.rise_of_city.utils.MarkdownHelper;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {
    private List<ChatMessage> messages;
    
    public ChatMessageAdapter() {
        this.messages = new ArrayList<>();
    }
    
    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
    
    public void updateLastMessage(String text) {
        if (!messages.isEmpty()) {
            ChatMessage lastMessage = messages.get(messages.size() - 1);
            lastMessage.setMessage(text);
            lastMessage.setLoading(false);
            notifyItemChanged(messages.size() - 1);
        }
    }
    
    public void setLastMessageLoading(boolean loading) {
        if (!messages.isEmpty()) {
            ChatMessage lastMessage = messages.get(messages.size() - 1);
            lastMessage.setLoading(loading);
            notifyItemChanged(messages.size() - 1);
        }
    }
    
    public void clearMessages() {
        messages.clear();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.bind(message);
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private CardView cardUserMessage;
        private CardView cardAiMessage;
        private TextView tvUserMessage;
        private TextView tvAiMessage;
        private ProgressBar progressLoading;
        private ImageButton btnCopy;
        
        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardUserMessage = itemView.findViewById(R.id.cardUserMessage);
            cardAiMessage = itemView.findViewById(R.id.cardAiMessage);
            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
            tvAiMessage = itemView.findViewById(R.id.tvAiMessage);
            progressLoading = itemView.findViewById(R.id.progressLoading);
            btnCopy = itemView.findViewById(R.id.btnCopy);
        }
        
        void bind(ChatMessage message) {
            if (message.getType() == ChatMessage.MessageType.USER) {
                // Hiển thị user message
                cardUserMessage.setVisibility(View.VISIBLE);
                cardAiMessage.setVisibility(View.GONE);
                tvUserMessage.setText(message.getMessage());
            } else {
                // Hiển thị AI message
                cardUserMessage.setVisibility(View.GONE);
                cardAiMessage.setVisibility(View.VISIBLE);
                
                // Parse và hiển thị markdown
                String messageText = message.getMessage();
                if (messageText != null && !messageText.isEmpty()) {
                    // Hiển thị với markdown formatting
                    tvAiMessage.setText(MarkdownHelper.parseMarkdown(messageText));
                } else {
                    tvAiMessage.setText("");
                }
                
                // Hiển thị loading indicator nếu đang tải
                if (message.isLoading()) {
                    progressLoading.setVisibility(View.VISIBLE);
                    btnCopy.setVisibility(View.GONE);
                    tvAiMessage.setText("Đang suy nghĩ...");
                } else {
                    progressLoading.setVisibility(View.GONE);
                    // Hiển thị nút copy cho AI message
                    if (messageText != null && !messageText.isEmpty()) {
                        btnCopy.setVisibility(View.VISIBLE);
                        btnCopy.setOnClickListener(v -> {
                            // Copy text (plain text, không có markdown)
                            String plainText = MarkdownHelper.removeMarkdown(messageText);
                            ClipboardManager clipboard = (ClipboardManager) 
                                itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("AI Response", plainText);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(itemView.getContext(), "Đã sao chép!", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        btnCopy.setVisibility(View.GONE);
                    }
                }
            }
        }
    }
}

