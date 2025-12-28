package com.example.rise_of_city.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;
import com.example.rise_of_city.data.repository.UserStatsRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.rise_of_city.ui.auth.LoginActivity;
import com.example.rise_of_city.ui.main.MainActivity;
import com.example.rise_of_city.ui.profile.status.LevelStatusFragment;

import java.util.Calendar;

public class ProfileFragment extends Fragment {

    private UserStatsRepository statsRepository;
    private TextView tvUserName;
    private TextView tvLogout;
    private TextView tvStreak, tvTotalXP, tvBuildings;
    private TextView tvNotifications, tvNotificationBadge;
    private View statsCard;
    private View cardNotifications;
    private View cardFriends;
    private RecyclerView rvFriends;
    private FriendsListAdapter friendsAdapter;
    private AppDatabase appDatabase;
    private ExecutorService executorService;

    // Constants consistent with LoginActivity and SignUpActivity
    private static final String PREF_NAME = "RiseOfCity_Prefs";
    private static final String KEY_USER_ID = "logged_user_id";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        statsRepository = UserStatsRepository.getInstance();
        appDatabase = AppDatabase.getInstance(requireContext());
        executorService = Executors.newSingleThreadExecutor();

        tvUserName = view.findViewById(R.id.tvUserName);
        tvLogout = view.findViewById(R.id.tvLogout);
        tvStreak = view.findViewById(R.id.tvStreak);
        tvTotalXP = view.findViewById(R.id.tvTotalXP);
        tvBuildings = view.findViewById(R.id.tvBuildings);
        statsCard = view.findViewById(R.id.statsCard);
        cardNotifications = view.findViewById(R.id.cardNotifications);
        tvNotifications = view.findViewById(R.id.tvNotifications);
        tvNotificationBadge = view.findViewById(R.id.tvNotificationBadge);
        cardFriends = view.findViewById(R.id.cardFriends);
        rvFriends = view.findViewById(R.id.rvFriends);
        
        // Setup RecyclerView cho danh sách bạn bè
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        friendsAdapter = new FriendsListAdapter(null, getContext(), -1);
        rvFriends.setAdapter(friendsAdapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.btnSettings).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.rise_of_city.ui.settings.SettingsActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.cardEditProfile).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.rise_of_city.ui.profile.EditProfileActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.cardLevelStatus).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LevelStatusFragment())
                    .addToBackStack(null)
                    .commit();
        });

        cardNotifications.setOnClickListener(v -> {
            // Navigate to notifications fragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new NotificationsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        tvLogout.setOnClickListener(v -> logout());

        loadUserInfo();
        loadNotifications();
        loadFriends();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserInfo();
        loadNotifications();
        loadFriends();
    }
    
    private void loadNotifications() {
        if (getActivity() == null) return;
        
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt(KEY_USER_ID, -1);
        
        if (userId == -1) return;
        
        executorService.execute(() -> {
            int pendingCount = appDatabase.friendDao().countPendingRequests(userId);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (pendingCount > 0) {
                        tvNotificationBadge.setVisibility(View.VISIBLE);
                        tvNotificationBadge.setText(String.valueOf(pendingCount));
                    } else {
                        tvNotificationBadge.setVisibility(View.GONE);
                    }
                });
            }
        });
    }
    
    private void loadFriends() {
        if (getActivity() == null) return;
        
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt(KEY_USER_ID, -1);
        
        if (userId == -1) return;
        
        executorService.execute(() -> {
            // Lấy danh sách bạn bè đã chấp nhận
            List<Friend> acceptedFriends = appDatabase.friendDao().getAcceptedFriends(userId);
            
            // Lấy thông tin User cho mỗi bạn bè
            List<User> friendUsers = new java.util.ArrayList<>();
            for (Friend friend : acceptedFriends) {
                int friendId = (friend.userId == userId) ? friend.friendId : friend.userId;
                User friendUser = appDatabase.userDao().getUserById(friendId);
                if (friendUser != null) {
                    friendUsers.add(friendUser);
                }
            }
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (rvFriends != null && getContext() != null) {
                        friendsAdapter = new FriendsListAdapter(friendUsers, getContext(), userId);
                        rvFriends.setAdapter(friendsAdapter);
                    }
                    
                    // Ẩn card nếu không có bạn bè
                    if (cardFriends != null) {
                        cardFriends.setVisibility(friendUsers.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                });
            }
        });
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    private void loadUserInfo() {
        if (getActivity() == null) return;

        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt(KEY_USER_ID, -1);

        if (userId != -1) {
            new GetUserTask().execute(userId);
        } else {
            tvUserName.setText("Chưa đăng nhập");
            tvStreak.setText("0 Ngày");
        }
    }

    private class GetUserTask extends AsyncTask<Integer, Void, User> {
        @Override
        protected User doInBackground(Integer... userIds) {
            if (appDatabase == null || appDatabase.userDao() == null) return null;
            
            User user = appDatabase.userDao().getUserById(userIds[0]);
            
            if (user != null) {
                // --- Xử lý Logic Streak (Chuỗi đăng nhập) ---
                long currentTime = System.currentTimeMillis();
                Calendar now = Calendar.getInstance();
                now.setTimeInMillis(currentTime);

                Calendar last = Calendar.getInstance();
                if (user.lastLoginTime > 0) {
                    last.setTimeInMillis(user.lastLoginTime);
                }

                boolean updated = false;

                if (user.lastLoginTime == 0) {
                    // Lần đầu đăng nhập
                    user.streakDays = 1;
                    user.lastLoginTime = currentTime;
                    updated = true;
                } else {
                    // Kiểm tra xem có phải cùng ngày không
                    boolean isSameDay = now.get(Calendar.YEAR) == last.get(Calendar.YEAR) &&
                                        now.get(Calendar.DAY_OF_YEAR) == last.get(Calendar.DAY_OF_YEAR);

                    if (!isSameDay) {
                        // Nếu không phải hôm nay, kiểm tra xem có phải hôm qua không
                        Calendar yesterday = (Calendar) now.clone();
                        yesterday.add(Calendar.DAY_OF_YEAR, -1);

                        boolean isYesterday = yesterday.get(Calendar.YEAR) == last.get(Calendar.YEAR) &&
                                              yesterday.get(Calendar.DAY_OF_YEAR) == last.get(Calendar.DAY_OF_YEAR);

                        if (isYesterday) {
                            // Đăng nhập liên tiếp
                            user.streakDays++;
                        } else {
                            // Đứt chuỗi (đăng nhập sau hơn 1 ngày)
                            user.streakDays = 1;
                        }
                        
                        // Cập nhật thời gian đăng nhập mới
                        user.lastLoginTime = currentTime;
                        updated = true;
                    }
                }

                if (updated) {
                    appDatabase.userDao().updateUser(user);
                }
            }
            return user;
        }

        @Override
        protected void onPostExecute(User user) {
            if (tvUserName != null && user != null) {
                tvUserName.setText("Xin chào " + (user.fullName != null ? user.fullName : "Thị trưởng"));
                if (tvStreak != null) {
                    tvStreak.setText(user.streakDays + " Ngày");
                }
            }
        }
    }

    private void logout() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}