package com.example.rise_of_city.ui.main;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.ui.assessment.KnowledgeSurveyActivity;
import com.example.rise_of_city.ui.game.ingame.HomeFragment;
import com.example.rise_of_city.ui.game.roadmap.RoadMapFragment;
import com.example.rise_of_city.ui.profile.SearchFragment;
import com.example.rise_of_city.ui.profile.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private LinearLayout bottomNav;
    private int currentSelectedItemId = R.id.nav_item_home;
    private int previousSelectedItemId = R.id.nav_item_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- LOGIC MỚI: KIỂM TRA SURVEY LẦN ĐẦU ---
        // Phải kiểm tra trước khi setContentView để tránh hiện giao diện Main rồi mới chuyển
        SharedPreferences prefs = getSharedPreferences("RiseOfCity_Prefs", MODE_PRIVATE);
        boolean isSurveyCompleted = prefs.getBoolean("is_survey_completed", false);

        if (!isSurveyCompleted) {
            // Nếu chưa hoàn thành survey, chuyển hướng ngay và đóng MainActivity
            Intent intent = new Intent(this, KnowledgeSurveyActivity.class);
            startActivity(intent);
            finish(); // Quan trọng: Đóng Main để user không quay lại được bằng nút Back
            return;
        }

        // --- GIỮ NGUYÊN TOÀN BỘ CODE CŨ CỦA BẠN BÊN DƯỚI ---
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);

        // Đặt Fragment mặc định là HomeFragment khi mở app
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // Setup click listeners cho các nav items
        setupNavigationItems();

        // Highlight item đầu tiên (Home)
        setSelectedItem(R.id.nav_item_home);

        // Kiểm tra thông báo khi lần đầu Activity được tạo (Dành cho lúc vừa làm xong survey quay về)
        checkSurveyIntent(getIntent());
    }

    /**
     * GIỮ NGUYÊN: Xử lý Intent mới khi Activity đang chạy (SingleTop/ClearTop)
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Cập nhật intent mới
        checkSurveyIntent(intent);
    }

    private void checkSurveyIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra("SHOW_SURVEY_DIALOG", false)) {
            showResultDialog();
        }
    }

    // GIỮ NGUYÊN: Hàm hiển thị Dialog kết quả
    private void showResultDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_survey_result_dialog);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        int score = getIntent().getIntExtra("SCORE", 0);
        int total = getIntent().getIntExtra("TOTAL", 10);

        TextView tvScore = dialog.findViewById(R.id.tvDialogScore);
        if (tvScore != null) {
            tvScore.setText("Bạn đã trả lời đúng " + score + "/" + total + " câu");
        }

        View rootLayout = dialog.findViewById(R.id.dialog_root_layout);
        if (rootLayout != null) {
            rootLayout.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    // GIỮ NGUYÊN: Setup click listeners
    private void setupNavigationItems() {
        LinearLayout navHome = findViewById(R.id.nav_item_home);
        navHome.setOnClickListener(v -> navigateToFragment(R.id.nav_item_home, new HomeFragment()));

        LinearLayout navSearch = findViewById(R.id.nav_item_search);
        navSearch.setOnClickListener(v -> navigateToFragment(R.id.nav_item_search, new SearchFragment()));

        LinearLayout navExplore = findViewById(R.id.nav_item_explore);
        navExplore.setOnClickListener(v -> navigateToFragment(R.id.nav_item_explore, new RoadMapFragment()));


        LinearLayout navProfile = findViewById(R.id.nav_item_profile);
        navProfile.setOnClickListener(v -> navigateToFragment(R.id.nav_item_profile, new ProfileFragment()));
    }

    // GIỮ NGUYÊN: Chuyển fragment
    private void navigateToFragment(int itemId, Fragment fragment) {
        if (currentSelectedItemId != itemId) {
            previousSelectedItemId = currentSelectedItemId;
        }

        setSelectedItem(itemId);
        currentSelectedItemId = itemId;

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    // GIỮ NGUYÊN: Thay đổi UI khi chọn item
    private void setSelectedItem(int itemId) {
        resetAllItems();

        LinearLayout selectedItem = findViewById(itemId);
        if (selectedItem != null) {
            ImageView icon = null;
            TextView text = null;

            if (itemId == R.id.nav_item_home) {
                icon = findViewById(R.id.nav_icon_home);
                text = findViewById(R.id.nav_text_home);
            } else if (itemId == R.id.nav_item_search) {
                icon = findViewById(R.id.nav_icon_search);
                text = findViewById(R.id.nav_text_search);
            } else if (itemId == R.id.nav_item_explore) {
                icon = findViewById(R.id.nav_icon_explore);
                text = findViewById(R.id.nav_text_explore);
            } else if (itemId == R.id.nav_item_chat) {
                icon = findViewById(R.id.nav_icon_chat);
                text = findViewById(R.id.nav_text_chat);
            } else if (itemId == R.id.nav_item_profile) {
                icon = findViewById(R.id.nav_icon_profile);
                text = findViewById(R.id.nav_text_profile);
            }

            if (icon != null) {
                icon.setColorFilter(ContextCompat.getColor(this, R.color.cyan_light));
            }
            if (text != null) {
                text.setTextColor(ContextCompat.getColor(this, R.color.cyan_light));
            }
        }
    }

    // GIỮ NGUYÊN: Reset toàn bộ UI thanh điều hướng
    private void resetAllItems() {
        int[] icons = {R.id.nav_icon_home, R.id.nav_icon_search, R.id.nav_icon_explore,
                R.id.nav_icon_chat, R.id.nav_icon_profile};
        int[] texts = {R.id.nav_text_home, R.id.nav_text_search, R.id.nav_text_explore,
                R.id.nav_text_chat, R.id.nav_text_profile};

        for (int id : icons) {
            ImageView img = findViewById(id);
            if (img != null) img.setColorFilter(Color.WHITE);
        }
        for (int id : texts) {
            TextView txt = findViewById(id);
            if (txt != null) txt.setTextColor(Color.WHITE);
        }
    }

    // GIỮ NGUYÊN: Quay lại fragment trước đó
    public void navigateToPreviousFragment() {
        if (bottomNav != null && previousSelectedItemId != 0) {
            Fragment fragment = null;
            if (previousSelectedItemId == R.id.nav_item_home) {
                fragment = new HomeFragment();
            } else if (previousSelectedItemId == R.id.nav_item_search) {
                fragment = new SearchFragment();
            } else if (previousSelectedItemId == R.id.nav_item_explore) {
                fragment = new RoadMapFragment();
            } else if (previousSelectedItemId == R.id.nav_item_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                navigateToFragment(previousSelectedItemId, fragment);
            }
        }
    }

    // GIỮ NGUYÊN: Getter
    public int getPreviousSelectedItemId() {
        return previousSelectedItemId;
    }

    // GIỮ NGUYÊN: Set item được chọn thủ công
    public void setSelectedNavItem(int itemId) {
        Fragment fragment = null;
        if (itemId == R.id.nav_item_home || itemId == R.id.nav_home) {
            fragment = new HomeFragment();
            itemId = R.id.nav_item_home;
        } else if (itemId == R.id.nav_item_search || itemId == R.id.nav_search) {
            fragment = new SearchFragment();
            itemId = R.id.nav_item_search;
        } else if (itemId == R.id.nav_item_explore || itemId == R.id.nav_explore) {
            fragment = new RoadMapFragment();
            itemId = R.id.nav_item_explore;
        } else if (itemId == R.id.nav_item_profile || itemId == R.id.nav_profile) {
            fragment = new ProfileFragment();
            itemId = R.id.nav_item_profile;
        }

        if (fragment != null) {
            navigateToFragment(itemId, fragment);
        }
    }
}