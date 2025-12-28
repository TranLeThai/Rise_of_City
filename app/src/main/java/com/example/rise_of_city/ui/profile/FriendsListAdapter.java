package com.example.rise_of_city.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rise_of_city.R;
import com.example.rise_of_city.data.local.User;
import com.example.rise_of_city.ui.chat.ChatActivity;
import java.util.List;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.FriendViewHolder> {
    
    private List<User> friends;
    private Context context;
    private int currentUserId;
    
    public FriendsListAdapter(List<User> friends, Context context, int currentUserId) {
        this.friends = friends;
        this.context = context;
        this.currentUserId = currentUserId;
    }
    
    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = friends.get(position);
        
        holder.tvFriendName.setText(friend.fullName != null ? friend.fullName : "Người dùng");
        holder.tvFriendEmail.setText(friend.email != null ? friend.email : "");
        holder.tvFriendStreak.setText("🔥 " + friend.streakDays + " ngày");
        
        // Click để mở chat
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("other_user_id", friend.id);
            intent.putExtra("other_user_name", friend.fullName != null ? friend.fullName : "Người dùng");
            context.startActivity(intent);
        });
    }
    
    @Override
    public int getItemCount() {
        return friends != null ? friends.size() : 0;
    }
    
    public void updateFriends(List<User> newFriends) {
        this.friends = newFriends;
        notifyDataSetChanged();
    }
    
    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView tvFriendName;
        TextView tvFriendEmail;
        TextView tvFriendStreak;
        
        FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFriendName = itemView.findViewById(R.id.tv_friend_name);
            tvFriendEmail = itemView.findViewById(R.id.tv_friend_email);
            tvFriendStreak = itemView.findViewById(R.id.tv_friend_streak);
        }
    }
}

