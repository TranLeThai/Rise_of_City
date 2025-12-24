package com.example.rise_of_city.ui.profile;

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
import com.example.rise_of_city.data.model.user.Badge;
import com.example.rise_of_city.data.repository.UserStatsRepository;
import com.example.rise_of_city.ui.auth.LoginActivity;
import com.example.rise_of_city.ui.dialog.BadgeUnlockDialogFragment;
import com.example.rise_of_city.ui.main.MainActivity;
import com.example.rise_of_city.ui.profile.status.LevelStatusFragment;
import com.example.rise_of_city.utils.BadgeManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private UserStatsRepository statsRepository;
    private TextView tvUserName;
    private TextView tvLogout;
    private TextView tvStreak, tvTotalXP, tvBuildings;
    private View statsCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        statsRepository = UserStatsRepository.getInstance();

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
                if (previousId == R.id.nav_item_profile || previousId == R.id.nav_profile) {
                    // Nếu trước đó là profile, quay về Home
                    previousId = R.id.nav_item_home;
                }
                mainActivity.setSelectedNavItem(previousId);
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
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh statistics khi quay lại từ màn hình khác (ví dụ: sau khi thu hoạch/nâng cấp)
        loadUserStatistics();
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
        
        // Tính streak từ learning_logs
        statsRepository.calculateStreak(streak -> {
            tvStreak.setText(streak + " Ngày");
        });
        
        // Tính total XP từ buildings
        statsRepository.calculateTotalXP(totalXP -> {
            tvTotalXP.setText(String.valueOf(totalXP));
        });
        
        // Load buildings count (unlocked buildings - không locked)
        // Buildings được lưu ở subcollection: users/{userId}/buildings
        // Đếm tất cả buildings đã unlock (có trong collection = đã unlock)
        db.collection("users")
                .document(user.getUid())
                .collection("buildings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int unlockedBuildings = 0;
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            // Kiểm tra nếu building không bị locked
                            // Nếu có trong collection và không có field "locked" hoặc locked = false thì tính là unlocked
                            Boolean isLocked = doc.getBoolean("locked");
                            if (isLocked == null || !isLocked) {
                                unlockedBuildings++;
                            }
                        }
                    }
                    tvBuildings.setText(String.valueOf(unlockedBuildings));
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileFragment", "Error loading buildings: ", e);
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

