package com.example.rise_of_city.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignUpActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword, edtConfirmPassword, edtFullName;
    private Button signUpBtn;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        db = AppDatabase.getInstance(this);
        initViews();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        signUpBtn.setOnClickListener(v -> registerUser());
    }

    private void initViews() {
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtSignEmail);
        edtPassword = findViewById(R.id.editSignPassword);
        edtConfirmPassword = findViewById(R.id.edtPasswordConfirm);
        signUpBtn = findViewById(R.id.SignUpBtn);
    }

    private void registerUser() {
        // QUAN TRỌNG: trim() password để tránh lưu khoảng trắng thừa
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPass = edtConfirmPassword.getText().toString().trim();
        String fullName = edtFullName.getText().toString().trim();

        if (!validateInputs(email, password, confirmPass, fullName)) return;

        executorService.execute(() -> {
            User existingUser = db.userDao().getUserByEmail(email);

            if (existingUser != null) {
                runOnUiThread(() -> Toast.makeText(this, "Email này đã được sử dụng", Toast.LENGTH_SHORT).show());
            } else {
                User newUser = new User();
                newUser.email = email;
                newUser.password = password;
                newUser.fullName = fullName;
                newUser.surveyCompleted = false;

                db.userDao().registerUser(newUser);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();
                    // Quay lại màn hình đăng nhập
                    finish();
                });
            }
        });
    }

    private boolean validateInputs(String email, String password, String confirm, String name) {
        if (name.isEmpty()) { edtFullName.setError("Nhập tên"); return false; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { edtEmail.setError("Email sai"); return false; }
        if (password.length() < 6) { edtPassword.setError("Mật khẩu ít nhất 6 ký tự"); return false; }
        if (!password.equals(confirm)) { edtConfirmPassword.setError("Mật khẩu không khớp"); return false; }
        return true;
    }
}