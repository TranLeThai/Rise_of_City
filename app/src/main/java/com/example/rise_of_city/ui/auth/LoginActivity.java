package com.example.rise_of_city.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.User;
import com.example.rise_of_city.ui.main.MainActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword;
    private Button logInBtn, signUpBtn;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = AppDatabase.getInstance(this);
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.email);
        edtPassword = findViewById(R.id.password);
        logInBtn = findViewById(R.id.loginButton);
        signUpBtn = findViewById(R.id.signupButton);
    }

    private void setupClickListeners() {
        logInBtn.setOnClickListener(v -> loginUser());
        signUpBtn.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (!validateInput(email, password)) return;

        executorService.execute(() -> {
            User user = db.userDao().login(email, password);

            runOnUiThread(() -> {
                if (user != null) {
                    Toast.makeText(this, "Chào mừng " + user.fullName, Toast.LENGTH_SHORT).show();

                    // LƯU TRẠNG THÁI TỪ DATABASE VÀO PREFS ĐỂ ĐIỀU HƯỚNG
                    SharedPreferences prefs = getSharedPreferences("RiseOfCity_Prefs", MODE_PRIVATE);
                    prefs.edit()
                            .putInt("logged_user_id", user.id)
                            .putBoolean("is_survey_completed", user.surveyCompleted) // Trạng thái thực tế từ Room
                            .apply();

                    // LUÔN LUÔN VÀO MAIN TRƯỚC
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Email hoặc mật khẩu không chính xác", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            return false;
        }
        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            return false;
        }
        return true;
    }
}