package com.example.rise_of_city.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.ui.auth.LoginActivity;
import com.example.rise_of_city.ui.main.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

        // Menu: Mức độ hoàn thành
        view.findViewById(R.id.cardLevelStatus).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                // Navigate đến LevelStatusFragment
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LevelStatusFragment())
                        .addToBackStack(null)
                        .commit();
            }
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
            // Ưu tiên hiển thị tên từ Firestore (nếu có)
            loadUserNameFromFirestore(user.getUid(), user);
        } else {
            navigateToLogin();
        }
    }

    private void loadUserNameFromFirestore(String uid, FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("user_profiles")
                .document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Ưu tiên 1: Lấy tên từ Firestore
                            String name = document.getString("name");
                            if (name != null && !name.isEmpty()) {
                                tvUserName.setText(name);
                                return;
                            }
                        }

                        // Ưu tiên 2: Lấy displayName từ Firebase Auth
                        String displayName = user.getDisplayName();
                        if (displayName != null && !displayName.isEmpty()) {
                            tvUserName.setText(displayName);
                            return;
                        }

                        // Ưu tiên 3: Lấy từ email
                        String email = user.getEmail();
                        if (email != null) {
                            String nameFromEmail = email.substring(0, email.indexOf("@"));
                            tvUserName.setText(nameFromEmail);
                        }
                    } else {
                        // Nếu không lấy được từ Firestore, dùng thông tin từ Auth
                        String displayName = user.getDisplayName();
                        if (displayName != null && !displayName.isEmpty()) {
                            tvUserName.setText(displayName);
                        } else {
                            String email = user.getEmail();
                            if (email != null) {
                                String name = email.substring(0, email.indexOf("@"));
                                tvUserName.setText(name);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileFragment", "Error loading user name: ", e);
                    // Fallback: dùng email
                    String email = user.getEmail();
                    if (email != null) {
                        String name = email.substring(0, email.indexOf("@"));
                        tvUserName.setText(name);
                    }
                });
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

