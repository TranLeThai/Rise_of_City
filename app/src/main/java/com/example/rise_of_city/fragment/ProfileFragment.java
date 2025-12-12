package com.example.rise_of_city.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.MainActivity;
import com.example.rise_of_city.R;
import com.example.rise_of_city.auth.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private TextView tvUserName;
    private TextView tvLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();

        // Tìm các view
        tvUserName = view.findViewById(R.id.tvUserName);
        tvLogout = view.findViewById(R.id.tvLogout);

        // Nút back - quay lại fragment trước đó
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                // Quay lại fragment trước đó (mặc định là Home)
                int previousId = mainActivity.getPreviousSelectedItemId();
                if (previousId == R.id.nav_profile) {
                    // Nếu trước đó là profile, quay về Home
                    previousId = R.id.nav_home;
                }
                mainActivity.getBottomNavigationView().setSelectedItemId(previousId);
            }
        });

        // Nút settings (có thể thêm logic sau)
        view.findViewById(R.id.btnSettings).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Menu: Chỉnh sửa hồ sơ
        view.findViewById(R.id.cardEditProfile).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Menu: Bộ sưu tập Huy hiệu
        view.findViewById(R.id.cardBadges).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Nút đăng xuất
        tvLogout.setOnClickListener(v -> {
            logout();
        });

        // Load thông tin người dùng
        loadUserInfo();

        return view;
    }

    private void loadUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Hiển thị tên người dùng
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvUserName.setText(displayName);
            } else {
                // Nếu không có display name, dùng email
                String email = user.getEmail();
                if (email != null) {
                    String name = email.substring(0, email.indexOf("@"));
                    tvUserName.setText(name);
                }
            }
        } else {
            // Nếu chưa đăng nhập, chuyển về màn hình đăng nhập
            navigateToLogin();
        }
    }

    private void logout() {
        if (mAuth != null) {
            mAuth.signOut();
            Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}

