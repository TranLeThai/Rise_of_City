package com.example.rise_of_city.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.local.ChatMessageEntity;

import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    
    private List<ChatMessageEntity> messages;
    private int currentUserId;
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    
    public ChatAdapter(List<ChatMessageEntity> messages, int currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }
    
    public void updateMessages(List<ChatMessageEntity> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            // Sent message (right)
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_message_sent, parent, false);
        } else {
            // Received message (left)
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_message_received, parent, false);
        }
        return new MessageViewHolder(view);
    }
    
    @Override
    public int getItemViewType(int position) {
        ChatMessageEntity message = messages.get(position);
        // Kiểm tra senderId để xác định tin nhắn là sent (0) hay received (1)
        return message.senderId == currentUserId ? 0 : 1;
    }
    
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        if (position < 0 || position >= messages.size()) {
            return;
        }
        
        ChatMessageEntity message = messages.get(position);
        holder.tvMessage.setText(message.message);
        
        // Format timestamp using cached formatter
        String time = TIME_FORMAT.format(new Date(message.timestamp));
        holder.tvTime.setText(time);
    }
    
    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < messages.size()) {
            return messages.get(position).id;
        }
        return RecyclerView.NO_ID;
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvTime;
        
        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}

