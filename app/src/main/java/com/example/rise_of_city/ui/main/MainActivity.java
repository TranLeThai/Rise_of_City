package com.example.rise_of_city.ui.main; // Đổi thành package của bạn

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.fragment.HomeFragment;
import com.example.rise_of_city.fragment.ChatAiFragment;
import com.example.rise_of_city.fragment.RoadMapFragment;
import com.example.rise_of_city.fragment.SearchFragment;
import com.example.rise_of_city.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private int previousSelectedItemId = R.id.nav_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);

        // Đặt Fragment mặc định là HomeFragment khi mở app
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // Xử lý sự kiện khi bấm vào menu dưới đáy
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            // Giả sử menu của bạn có id là nav_home (bạn cần check file menu xml)
            int itemId = item.getItemId();
            
            // Lưu lại item được chọn trước đó (trước khi thay đổi)
            int currentSelectedId = bottomNav.getSelectedItemId();
            if (currentSelectedId != itemId && currentSelectedId != 0) {
                previousSelectedItemId = currentSelectedId;
            }

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_search) {
                selectedFragment = new SearchFragment();
            } else if (itemId == R.id.nav_explore) {
                selectedFragment = new RoadMapFragment();
            } else if (itemId == R.id.nav_chat) {
                selectedFragment = new ChatAiFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }

    // Method để quay lại fragment trước đó
    public void navigateToPreviousFragment() {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(previousSelectedItemId);
        }
    }

    // Getter để lấy previous fragment ID
    public int getPreviousSelectedItemId() {
        return previousSelectedItemId;
    }

    // Getter để lấy BottomNavigationView
    public BottomNavigationView getBottomNavigationView() {
        return bottomNav;
    }
}