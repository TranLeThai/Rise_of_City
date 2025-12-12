package com.example.rise_of_city; // Đổi thành package của bạn

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.fragment.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

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

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            }
            // Thêm các case khác nếu bạn có fragment Profile, Store, v.v.
            // else if (itemId == R.id.nav_profile) { ... }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });
    }
}