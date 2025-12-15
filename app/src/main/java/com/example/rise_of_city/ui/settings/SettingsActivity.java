package com.example.rise_of_city.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rise_of_city.R;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "RiseOfCitySettings";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_SOUND = "sound_enabled";
    private static final String KEY_VIBRATION = "vibration_enabled";
    private static final String KEY_LANGUAGE = "language";
    
    private Switch switchNotifications;
    private Switch switchSound;
    private Switch switchVibration;
    private TextView tvLanguage;
    private TextView tvAbout;
    private TextView tvPrivacy;
    private TextView tvTerms;
    private TextView tvLogout;
    
    private SharedPreferences prefs;
    private FirebaseAuth mAuth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        initViews();
        setupClickListeners();
        loadSettings();
    }
    
    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchSound = findViewById(R.id.switchSound);
        switchVibration = findViewById(R.id.switchVibration);
        tvLanguage = findViewById(R.id.tvLanguage);
        tvAbout = findViewById(R.id.tvAbout);
        tvPrivacy = findViewById(R.id.tvPrivacy);
        tvTerms = findViewById(R.id.tvTerms);
        tvLogout = findViewById(R.id.tvLogout);
        
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void setupClickListeners() {
        // Notification switch
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_NOTIFICATIONS, isChecked);
        });
        
        // Sound switch
        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_SOUND, isChecked);
        });
        
        // Vibration switch
        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting(KEY_VIBRATION, isChecked);
        });
        
        // Language
        tvLanguage.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        });
        
        // About
        tvAbout.setOnClickListener(v -> {
            Toast.makeText(this, "Rise of City v1.0\nỨng dụng học tiếng Anh thú vị", Toast.LENGTH_LONG).show();
        });
        
        // Privacy Policy
        tvPrivacy.setOnClickListener(v -> {
            Toast.makeText(this, "Chính sách bảo mật đang được cập nhật", Toast.LENGTH_SHORT).show();
        });
        
        // Terms of Service
        tvTerms.setOnClickListener(v -> {
            Toast.makeText(this, "Điều khoản sử dụng đang được cập nhật", Toast.LENGTH_SHORT).show();
        });
        
        // Logout
        tvLogout.setOnClickListener(v -> {
            logout();
        });
    }
    
    private void loadSettings() {
        // Load notification setting
        boolean notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true);
        switchNotifications.setChecked(notificationsEnabled);
        
        // Load sound setting
        boolean soundEnabled = prefs.getBoolean(KEY_SOUND, true);
        switchSound.setChecked(soundEnabled);
        
        // Load vibration setting
        boolean vibrationEnabled = prefs.getBoolean(KEY_VIBRATION, true);
        switchVibration.setChecked(vibrationEnabled);
        
        // Load language
        String language = prefs.getString(KEY_LANGUAGE, "Tiếng Việt");
        tvLanguage.setText("Ngôn ngữ: " + language);
    }
    
    private void saveSetting(String key, boolean value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    
    private void logout() {
        if (mAuth != null) {
            mAuth.signOut();
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            finish();
            // Navigate to login - handled by MainActivity or LoginActivity
        }
    }
}

