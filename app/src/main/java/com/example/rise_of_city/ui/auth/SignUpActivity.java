package com.example.rise_of_city.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rise_of_city.R;
import com.example.rise_of_city.ui.assessment.KnowledgeSurveyActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword, edtConfirmPassword, edtFullName;
    private Button signUp;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Tìm view
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtSignEmail);
        edtPassword = findViewById(R.id.editSignPassword);
        edtConfirmPassword = findViewById(R.id.edtPasswordConfirm);
        signUp = findViewById(R.id.SignUpBtn);

        // Nút back - Navigate về màn hình đăng nhập
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Đóng màn hình đăng ký sau khi mở màn hình đăng nhập
        });

        signUp.setOnClickListener(e -> CreateUser());
    }

    private void CreateUser() {
        // Lấy dữ liệu
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();
        String fullName = edtFullName.getText().toString().trim();


        // VALIDATION NÂNG CAO
        if (!validateInputs(email, password, confirmPassword, fullName)) {
            return;
        }

        // Hiển thị progress bar
        signUp.setEnabled(false);

        // Tạo người dùng trong Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Cập nhật thông tin tên trong Auth
                            updateUserProfile(user, fullName); // Thêm dòng này

                            // Lưu thông tin vào Firestore
                            saveUserToFirestore(user.getUid(), email, fullName); // Thêm fullName
                        }
                    } else {
                        signUp.setEnabled(true);
                        String err = task.getException() != null ?
                                task.getException().getMessage() : "Lỗi không xác định";
                        Toast.makeText(this, "Đăng ký thất bại: " + err, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Đăng ký thất bại: ", task.getException());
                    }
                });
    }

    // VALIDATION CHI TIẾT
    private boolean validateInputs(String email, String password, String confirmPassword, String fullName) {
        // Validate email
        if (email.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return false;
        }

        if (fullName.isEmpty()) {
            edtFullName.setError("Vui lòng nhập họ và tên");
            edtFullName.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return false;
        }

        // Validate mật khẩu
        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu phải có ít nhất 6 kí tự");
            edtPassword.requestFocus();
            return false;
        }

        // Check password strength
        PasswordValidationResult validationResult = validatePasswordStrength(password);
        if (!validationResult.isValid) {
            Toast.makeText(this, validationResult.message, Toast.LENGTH_LONG).show();
            return false;
        }


        // Validate xác nhận mật khẩu
        if (confirmPassword.isEmpty()) {
            edtConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            edtConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu không khớp");
            edtConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private PasswordValidationResult validatePasswordStrength(String password) {
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[^a-zA-Z0-9].*");

        StringBuilder message = new StringBuilder("Mật khẩu cần có: ");
        boolean isValid = true;

        if (!hasUpper) {
            message.append("chữ hoa, ");
            isValid = false;
        }
        if (!hasLower) {
            message.append("chữ thường, ");
            isValid = false;
        }
        if (!hasDigit) {
            message.append("số, ");
            isValid = false;
        }
        if (!hasSpecial) {
            message.append("ký tự đặc biệt");
            isValid = false;
        }

        if (!isValid) {
            if (message.toString().endsWith(", ")) {
                message.delete(message.length() - 2, message.length());
            }
        }

        return new PasswordValidationResult(isValid, message.toString());
    }

    private void updateUserProfile(FirebaseUser user, String fullName) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User profile updated with name: " + fullName);
                    } else {
                        Log.e(TAG, "Failed to update user profile.");
                    }
                });
    }

    private void saveUserToFirestore(String uid, String email, String fullName) {
        // Tạo dữ liệu user với validation
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid); // Lưu cả UID trong document
        userData.put("name", fullName);
        userData.put("email", email);
        userData.put("createdAt", FieldValue.serverTimestamp()); // Dùng server timestamp
        userData.put("updatedAt", FieldValue.serverTimestamp());
        userData.put("lastLogin", FieldValue.serverTimestamp());
        userData.put("role", "customer");
        userData.put("isActive", true);
        userData.put("emailVerified", false);
        userData.put("courses", new HashMap<String, Object>()); // Khởi tạo empty courses
        userData.put("phone", "");
        userData.put("address", "");
        userData.put("surveyCompleted", false); // Đánh dấu chưa hoàn thành khảo sát
        userData.put("surveyLevel", ""); // Trình độ sẽ được cập nhật sau khi hoàn thành khảo sát

        // Lưu vào Firestore với UID làm Document ID
        db.collection("user_profiles")
                .document(uid) // DÙNG UID LÀM DOCUMENT ID
                .set(userData)
                .addOnCompleteListener(task -> {
                    signUp.setEnabled(true);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "User profile saved to Firestore with UID: " + uid);
                        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                        // Gửi email xác thực
                        sendEmailVerification();

                        // Chuyển đến màn hình khảo sát kiến thức
                        Intent intent = new Intent(SignUpActivity.this, KnowledgeSurveyActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.e(TAG, "Failed to save user profile: ", task.getException());
                        Toast.makeText(this, "Lỗi lưu thông tin. Vui lòng thử lại.", Toast.LENGTH_LONG).show();

                        // Rollback: xóa user khỏi Auth nếu lưu Firestore thất bại
                        mAuth.getCurrentUser().delete();
                    }
                });
    }

    private void sendEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this,
                                    "Đã gửi email xác thực. Vui lòng kiểm tra hộp thư.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    // Helper class for password validation
    private static class PasswordValidationResult {
        boolean isValid;
        String message;

        PasswordValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }
    }
}