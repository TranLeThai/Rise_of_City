package com.example.rise_of_city.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditProfileActivity extends AppCompatActivity {
    
    private EditText edtName, edtPhone, edtAddress;
    private Button btnSave;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    
    private AppDatabase db;
    private int userId;
    private User currentUser;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        
        db = AppDatabase.getInstance(this);

        // Lấy ID người dùng từ SharedPreferences (giống LoginActivity)
        SharedPreferences prefs = getSharedPreferences("RiseOfCity_Prefs", Context.MODE_PRIVATE);
        userId = prefs.getInt("logged_user_id", -1);
        
        // Kiểm tra nếu chưa đăng nhập qua Room
        if (userId == -1) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupClickListeners();
        loadUserProfile();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProfile());
    }
    
    private void loadUserProfile() {
        showLoading(true);
        executorService.execute(() -> {
            // Lấy thông tin user từ Room Database
            currentUser = db.userDao().getUserById(userId);
            
            runOnUiThread(() -> {
                showLoading(false);
                if (currentUser != null) {
                    if (currentUser.fullName != null) edtName.setText(currentUser.fullName);
                    if (currentUser.phone != null) edtPhone.setText(currentUser.phone);
                    if (currentUser.address != null) edtAddress.setText(currentUser.address);
                } else {
                    Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }
    
    private void saveProfile() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        
        if (name.isEmpty()) {
            edtName.setError("Vui lòng nhập tên");
            edtName.requestFocus();
            return;
        }
        
        if (currentUser == null) return;
        
        showLoading(true);
        btnSave.setEnabled(false);
        
        // Cập nhật thông tin vào object User hiện tại
        currentUser.fullName = name;
        currentUser.phone = phone;
        currentUser.address = address;
        
        executorService.execute(() -> {
            // Lưu vào Database
            db.userDao().updateUser(currentUser);
            
            runOnUiThread(() -> {
                showLoading(false);
                btnSave.setEnabled(true);
                Toast.makeText(this, "Đã cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}