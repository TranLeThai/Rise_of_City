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
import com.example.rise_of_city.data.model.Badge;
import com.example.rise_of_city.ui.auth.LoginActivity;
import com.example.rise_of_city.ui.dialog.BadgeUnlockDialogFragment;
import com.example.rise_of_city.ui.main.MainActivity;
import com.example.rise_of_city.utils.BadgeManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvUserName;
    private TextView tvLogout;
    private TextView tvStreak, tvTotalXP, tvBuildings;
    private View statsCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();

        // Tìm các view
        tvUserName = view.findViewById(R.id.tvUserName);
        tvLogout = view.findViewById(R.id.tvLogout);
        tvStreak = view.findViewById(R.id.tvStreak);
        tvTotalXP = view.findViewById(R.id.tvTotalXP);
        tvBuildings = view.findViewById(R.id.tvBuildings);
        statsCard = view.findViewById(R.id.statsCard);
        
        db = FirebaseFirestore.getInstance();

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

        // Nút settings
        view.findViewById(R.id.btnSettings).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.rise_of_city.ui.settings.SettingsActivity.class);
            startActivity(intent);
        });

        // Menu: Chỉnh sửa hồ sơ
        view.findViewById(R.id.cardEditProfile).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.example.rise_of_city.ui.profile.EditProfileActivity.class);
            startActivity(intent);
        });

        // Menu: Bộ sưu tập Huy hiệu
        view.findViewById(R.id.cardBadges).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new BadgeCollectionFragment())
                        .addToBackStack(null)
                        .commit();
            }
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
        loadUserStatistics();
        checkForNewBadges();

        return view;
    }
    
    private void checkForNewBadges() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        
        db.collection("user_profiles")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        BadgeManager.getInstance().checkAndUnlockBadges(
                            getContext(),
                            documentSnapshot,
                            badges -> {
                                // Show unlock dialog for each newly unlocked badge
                                if (!badges.isEmpty() && getActivity() != null) {
                                    for (Badge badge : badges) {
                                        BadgeUnlockDialogFragment dialog = 
                                            BadgeUnlockDialogFragment.newInstance(badge);
                                        dialog.show(getParentFragmentManager(), "BadgeUnlockDialog");
                                    }
                                }
                            }
                        );
                    }
                });
    }
    
    private void loadUserStatistics() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        
        db.collection("user_profiles")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Load streak
                        Long streak = documentSnapshot.getLong("streak");
                        if (streak != null) {
                            tvStreak.setText(streak + " Ngày");
                        } else {
                            tvStreak.setText("0 Ngày");
                        }
                        
                        // Load total XP
                        Long totalXP = documentSnapshot.getLong("totalXP");
                        if (totalXP != null) {
                            tvTotalXP.setText(String.valueOf(totalXP));
                        } else {
                            tvTotalXP.setText("0");
                        }
                        
                        // Load buildings count (completed buildings)
                        Map<String, Object> buildings = (Map<String, Object>) documentSnapshot.get("buildings");
                        int completedBuildings = 0;
                        if (buildings != null) {
                            for (Map.Entry<String, Object> entry : buildings.entrySet()) {
                                Map<String, Object> buildingData = (Map<String, Object>) entry.getValue();
                                Boolean isCompleted = (Boolean) buildingData.get("completed");
                                if (isCompleted != null && isCompleted) {
                                    completedBuildings++;
                                }
                            }
                        }
                        tvBuildings.setText(String.valueOf(completedBuildings));
                    } else {
                        // Set default values
                        tvStreak.setText("0 Ngày");
                        tvTotalXP.setText("0");
                        tvBuildings.setText("0");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileFragment", "Error loading statistics: ", e);
                    // Set default values on error
                    tvStreak.setText("0 Ngày");
                    tvTotalXP.setText("0");
                    tvBuildings.setText("0");
                });
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

