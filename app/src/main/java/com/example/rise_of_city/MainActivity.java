package com.example.rise_of_city;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.rise_of_city.fragment.HomeFragment; // Import Fragment của bạn

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
        Button questsButton = findViewById(R.id.button_quests);
        // Kiểm tra null để tránh crash nếu layout quên chưa thêm nút này
        if (questsButton != null) {
            questsButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, QuestsActivity.class);
                startActivity(intent);
            });
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}