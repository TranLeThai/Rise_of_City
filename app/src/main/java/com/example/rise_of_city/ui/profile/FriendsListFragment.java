package com.example.rise_of_city.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.Friend;
import com.example.rise_of_city.data.local.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FriendsListFragment extends Fragment {
    
    private RecyclerView rvFriends;
    private TextView tvEmptyState;
    private FriendsAdapter adapter;
    
    private AppDatabase appDatabase;
    private ExecutorService executorService;
    private int currentUserId;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_list, container, false);
        
        // Initialize
        appDatabase = AppDatabase.getInstance(requireContext());
        executorService = Executors.newSingleThreadExecutor();
        
        SharedPreferences prefs = requireContext().getSharedPreferences("RiseOfCity_Prefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getInt("logged_user_id", -1);
        
        // Initialize views
        rvFriends = view.findViewById(R.id.rv_friends);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        
        // Setup RecyclerView
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FriendsAdapter(getContext());
        rvFriends.setAdapter(adapter);
        
        // Load friends
        loadFriends();
        
        return view;
    }
    
    private void loadFriends() {
        if (currentUserId == -1) {
            showEmptyState("Vui lòng đăng nhập");
            return;
        }
        
        executorService.execute(() -> {
            List<Friend> friends = appDatabase.friendDao().getAcceptedFriends(currentUserId);
            List<FriendItem> friendItems = new ArrayList<>();
            
            for (Friend friend : friends) {
                // Lấy ID của người bạn (không phải mình)
                int friendUserId = friend.userId == currentUserId ? friend.friendId : friend.userId;
                User user = appDatabase.userDao().getUserById(friendUserId);
                
                if (user != null) {
                    FriendItem item = new FriendItem();
                    item.userId = user.id;
                    item.name = user.fullName;
                    item.email = user.email;
                    item.streak = user.streakDays;
                    friendItems.add(item);
                }
            }
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (friendItems.isEmpty()) {
                        showEmptyState("Chưa có bạn bè nào");
                    } else {
                        adapter.setFriends(friendItems);
                        tvEmptyState.setVisibility(View.GONE);
                        rvFriends.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }
    
    private void showEmptyState(String message) {
        tvEmptyState.setText(message);
        tvEmptyState.setVisibility(View.VISIBLE);
        rvFriends.setVisibility(View.GONE);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    // Friend Item Model
    static class FriendItem {
        int userId;
        String name;
        String email;
        int streak;
    }
    
    // Friends Adapter
    static class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {
        private List<FriendItem> friends = new ArrayList<>();
        private Context context;
        
        FriendsAdapter(Context context) {
            this.context = context;
        }
        
        void setFriends(List<FriendItem> friends) {
            this.friends = friends;
            notifyDataSetChanged();
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_friend, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FriendItem friend = friends.get(position);
            
            holder.tvName.setText(friend.name != null ? friend.name : "Người dùng");
            holder.tvEmail.setText(friend.email != null ? friend.email : "");
            holder.tvStreak.setText("🔥 " + friend.streak + " ngày");
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, LocalUserProfileActivity.class);
                intent.putExtra("user_id", friend.userId);
                context.startActivity(intent);
            });
        }
        
        @Override
        public int getItemCount() {
            return friends.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvEmail, tvStreak;
            
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_friend_name);
                tvEmail = itemView.findViewById(R.id.tv_friend_email);
                tvStreak = itemView.findViewById(R.id.tv_friend_streak);
            }
        }
    }
}

