package com.example.rise_of_city.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

public class NotificationsFragment extends Fragment {
    
    private RecyclerView rvNotifications;
    private TextView tvEmptyState;
    private NotificationsAdapter adapter;
    
    private AppDatabase appDatabase;
    private ExecutorService executorService;
    private int currentUserId;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        
        // Initialize
        appDatabase = AppDatabase.getInstance(requireContext());
        executorService = Executors.newSingleThreadExecutor();
        
        SharedPreferences prefs = requireContext().getSharedPreferences("RiseOfCity_Prefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getInt("logged_user_id", -1);
        
        // Initialize views
        rvNotifications = view.findViewById(R.id.rv_notifications);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        
        // Setup RecyclerView
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationsAdapter(this::handleAccept, this::handleReject);
        rvNotifications.setAdapter(adapter);
        
        // Load notifications
        loadNotifications();
        
        return view;
    }
    
    private void loadNotifications() {
        if (currentUserId == -1) {
            showEmptyState("Vui lòng đăng nhập");
            return;
        }
        
        executorService.execute(() -> {
            List<Friend> pendingRequests = appDatabase.friendDao().getPendingRequests(currentUserId);
            List<FriendRequest> requests = new ArrayList<>();
            
            for (Friend friend : pendingRequests) {
                User requester = appDatabase.userDao().getUserById(friend.userId);
                
                if (requester != null) {
                    FriendRequest request = new FriendRequest();
                    request.friendId = friend.id;
                    request.userId = requester.id;
                    request.name = requester.fullName;
                    request.email = requester.email;
                    request.createdAt = friend.createdAt;
                    requests.add(request);
                }
            }
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (requests.isEmpty()) {
                        showEmptyState("Không có thông báo mới");
                    } else {
                        adapter.setRequests(requests);
                        tvEmptyState.setVisibility(View.GONE);
                        rvNotifications.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }
    
    private void handleAccept(FriendRequest request) {
        executorService.execute(() -> {
            Friend friend = appDatabase.friendDao().checkPendingRequest(currentUserId, request.userId);
            if (friend != null) {
                friend.status = "accepted";
                friend.updatedAt = System.currentTimeMillis();
                appDatabase.friendDao().updateFriend(friend);
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Đã chấp nhận lời mời kết bạn", Toast.LENGTH_SHORT).show();
                        loadNotifications();
                    });
                }
            }
        });
    }
    
    private void handleReject(FriendRequest request) {
        executorService.execute(() -> {
            appDatabase.friendDao().deleteFriend(request.friendId);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Đã từ chối lời mời kết bạn", Toast.LENGTH_SHORT).show();
                    loadNotifications();
                });
            }
        });
    }
    
    private void showEmptyState(String message) {
        tvEmptyState.setText(message);
        tvEmptyState.setVisibility(View.VISIBLE);
        rvNotifications.setVisibility(View.GONE);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    // Friend Request Model
    static class FriendRequest {
        int friendId;
        int userId;
        String name;
        String email;
        long createdAt;
    }
    
    // Notifications Adapter
    static class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
        private List<FriendRequest> requests = new ArrayList<>();
        private OnActionListener acceptListener;
        private OnActionListener rejectListener;
        
        interface OnActionListener {
            void onAction(FriendRequest request);
        }
        
        NotificationsAdapter(OnActionListener acceptListener, OnActionListener rejectListener) {
            this.acceptListener = acceptListener;
            this.rejectListener = rejectListener;
        }
        
        void setRequests(List<FriendRequest> requests) {
            this.requests = requests;
            notifyDataSetChanged();
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_friend_request, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FriendRequest request = requests.get(position);
            
            holder.tvName.setText(request.name != null ? request.name : "Người dùng");
            holder.tvMessage.setText("muốn kết bạn với bạn");
            
            String timeAgo = getTimeAgo(request.createdAt);
            holder.tvTime.setText(timeAgo);
            
            holder.btnAccept.setOnClickListener(v -> {
                if (acceptListener != null) {
                    acceptListener.onAction(request);
                }
            });
            
            holder.btnReject.setOnClickListener(v -> {
                if (rejectListener != null) {
                    rejectListener.onAction(request);
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return requests.size();
        }
        
        private String getTimeAgo(long timestamp) {
            long diff = System.currentTimeMillis() - timestamp;
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            
            if (days > 0) return days + " ngày trước";
            if (hours > 0) return hours + " giờ trước";
            if (minutes > 0) return minutes + " phút trước";
            return "Vừa xong";
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvMessage, tvTime;
            Button btnAccept, btnReject;
            
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_request_name);
                tvMessage = itemView.findViewById(R.id.tv_request_message);
                tvTime = itemView.findViewById(R.id.tv_request_time);
                btnAccept = itemView.findViewById(R.id.btn_accept);
                btnReject = itemView.findViewById(R.id.btn_reject);
            }
        }
    }
}

