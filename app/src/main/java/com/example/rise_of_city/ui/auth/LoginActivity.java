package com.example.rise_of_city.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rise_of_city.ui.main.MainActivity;
import com.example.rise_of_city.ui.assessment.KnowledgeSurveyActivity;
import com.example.rise_of_city.R;
//import com.example.rise_of_city.activities.customer.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword;
    private CheckBox checkBox;
    private Button logInBtn, signUpBtn;
    private MaterialButton btnGoogle, btnFacebook, btnApple;
    private TextView forgotPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "LoginActivity";

    // Launcher cho Google Sign-In
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                } catch (ApiException e) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (e.getStatusCode() == CommonStatusCodes.CANCELED) {
                        Toast.makeText(this, "Đăng nhập Google bị hủy", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Lỗi Google Sign-In: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    Log.e(TAG, "Google Sign-In failed: ", e);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupFirebase();
        setupGoogleSignIn();
        setupClickListeners();
        checkIfLoggedIn();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.email);
        edtPassword = findViewById(R.id.password);
        checkBox = findViewById(R.id.checkBox);
        logInBtn = findViewById(R.id.loginButton);
        signUpBtn = findViewById(R.id.signupButton);
        btnGoogle = findViewById(R.id.btn_google);
        btnFacebook = findViewById(R.id.btn_facebook);
        btnApple = findViewById(R.id.btn_apple);
        forgotPassword = findViewById(R.id.forgotPassword);

        // Safe lookup for progressBar: avoid direct reference to R.id.progressBar if it doesn't exist
        int pbId = getResources().getIdentifier("progressBar", "id", getPackageName());
        if (pbId != 0) {
            progressBar = findViewById(pbId);
        } else {
            progressBar = null; // leave null if layout has no progressBar
        }

        // Gạch chân text "Quên mật khẩu?" (only if view exists)
        if (forgotPassword != null) {
            SpannableString content = new SpannableString(forgotPassword.getText());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            forgotPassword.setText(content);
        }
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        // Firestore was removed; if you want to re-enable Firestore, add dependency and reintroduce db initialization here
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void checkIfLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                updateLastLogin(currentUser.getUid());
                // Kiểm tra khảo sát thay vì chuyển thẳng đến MainActivity
                checkSurveyCompletion(currentUser.getUid());
            } else {
                Toast.makeText(this, "Vui lòng xác thực email trước khi đăng nhập", Toast.LENGTH_LONG).show();
                mAuth.signOut();
            }
        }
    }

    private void signInWithGoogle() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        if (account == null) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Google account is null");
            Toast.makeText(this, "Không thể lấy thông tin tài khoản Google", Toast.LENGTH_SHORT).show();
            return;
        }

        String idToken = account.getIdToken();
        if (idToken == null) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            Log.e(TAG, "ID Token is null");
            Toast.makeText(this, "Không thể lấy ID Token từ Google", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "Google ID Token received");

        com.google.firebase.auth.AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Kiểm tra xem user đã tồn tại trong Firestore chưa
                            checkUserExistsAndHandleLogin(user);
                        }
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Lỗi không xác định";
                        Log.e(TAG, "Google auth failed: " + errorMsg, task.getException());
                        Toast.makeText(this, "Đăng nhập Google thất bại: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // If you have server-side or Firestore support, implement updateLastLogin to persist timestamp there.
    private void sendPasswordReset() {
        String email = edtEmail.getText().toString().trim();

        if (email.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email để đặt lại mật khẩu");
            edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                                "Đã gửi email đặt lại mật khẩu. Vui lòng kiểm tra hộp thư.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        String err = task.getException() != null ?
                                task.getException().getMessage() : "Lỗi không xác định";
                        Toast.makeText(LoginActivity.this,
                                "Không thể gửi email đặt lại: " + err,
                                Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Password reset failed: ", task.getException());
                    }
                });
    }

    private void loginUserWithEmail() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (!validateLoginInput(email, password)) {
            return;
        }

        // Special case - bypass Firebase (chỉ dùng cho testing)
        if (email.equals("tlt@gmail.com") && password.equals("Thai123")) {
            handleSpecialLogin(email);
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        logInBtn.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    logInBtn.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            handleSuccessfulLogin(user);
                        }
                    } else {
                        handleFailedLogin(task);
                    }
                });
    }

    private boolean validateLoginInput(String email, String password) {
        if (email.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            edtEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return false;
        }

        if (!checkBox.isChecked()) {
            Toast.makeText(this, "Vui lòng xác nhận không phải robot", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void handleSpecialLogin(String email) {
        Log.d(TAG, "Special login for: " + email);
        Toast.makeText(this, "Chủ nhân đăng nhập", Toast.LENGTH_SHORT).show();

        // Bỏ qua khảo sát cho special login
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void handleSuccessfulLogin(FirebaseUser user) {
        if (!user.isEmailVerified()) {
            Toast.makeText(this,
                    "Email chưa được xác thực. Vui lòng kiểm tra hộp thư.",
                    Toast.LENGTH_LONG).show();
        }

        updateLastLogin(user.getUid());
        updateUserNameIfMissing(user.getUid(), user.getDisplayName());

        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "User logged in: " + user.getEmail());

        // Kiểm tra xem người dùng đã hoàn thành khảo sát chưa
        checkSurveyCompletion(user.getUid());
    }
    
    private void checkUserExistsAndHandleLogin(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user_profiles")
                .document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        com.google.firebase.firestore.DocumentSnapshot document = task.getResult();
                        if (!document.exists()) {
                            // User mới, tạo profile
                            createUserProfileForGoogle(user);
                        } else {
                            // User đã tồn tại, xử lý đăng nhập bình thường
                            handleSuccessfulLogin(user);
                        }
                    } else {
                        // Lỗi, vẫn xử lý đăng nhập
                        handleSuccessfulLogin(user);
                    }
                });
    }
    
    private void createUserProfileForGoogle(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("name", user.getDisplayName() != null ? user.getDisplayName() : "");
        userData.put("email", user.getEmail() != null ? user.getEmail() : "");
        userData.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        userData.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        userData.put("lastLogin", com.google.firebase.firestore.FieldValue.serverTimestamp());
        userData.put("role", "customer");
        userData.put("isActive", true);
        userData.put("emailVerified", user.isEmailVerified());
        userData.put("courses", new HashMap<String, Object>());
        userData.put("phone", "");
        userData.put("address", "");
        userData.put("surveyCompleted", false);
        userData.put("surveyLevel", "");
        
        db.collection("user_profiles")
                .document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Google user profile created");
                    handleSuccessfulLogin(user);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating Google user profile: ", e);
                    handleSuccessfulLogin(user);
                });
    }
    
    private void checkSurveyCompletion(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user_profiles")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        com.google.firebase.firestore.DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Boolean surveyCompleted = document.getBoolean("surveyCompleted");
                            
                            // Nếu chưa hoàn thành khảo sát, chuyển đến màn hình khảo sát
                            if (surveyCompleted == null || !surveyCompleted) {
                                Intent intent = new Intent(LoginActivity.this, KnowledgeSurveyActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                // Đã hoàn thành khảo sát, chuyển đến MainActivity
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }
                        } else {
                            // Document không tồn tại, chuyển đến khảo sát
                            Intent intent = new Intent(LoginActivity.this, KnowledgeSurveyActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        // Lỗi khi đọc Firestore, vẫn chuyển đến MainActivity
                        Log.e(TAG, "Error checking survey completion: ", task.getException());
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                });
    }

    private void updateLastLogin(String userId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastLogin", System.currentTimeMillis());
        updates.put("updatedAt", System.currentTimeMillis());

        // No Firestore/Database write here: implement server-side logic or add Firesto
    }

    private void updateUserNameIfMissing(String uid, String displayName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("user_profiles")
                .document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Kiểm tra nếu chưa có trường name
                            if (!document.contains("name") || document.getString("name") == null || document.getString("name").isEmpty()) {
                                // Tạo tên từ email nếu không có displayName
                                String nameToSet = displayName;
                                if (nameToSet == null || nameToSet.isEmpty()) {
                                    FirebaseUser currentUser = mAuth.getCurrentUser();
                                    if (currentUser != null && currentUser.getEmail() != null) {
                                        String email = currentUser.getEmail();
                                        nameToSet = email.substring(0, email.indexOf("@"));
                                    }
                                }

                                // Cập nhật Firestore
                                final String finalNameToSet = nameToSet;
                                if (finalNameToSet != null && !finalNameToSet.isEmpty()) {
                                    db.collection("user_profiles")
                                            .document(uid)
                                            .update("name", finalNameToSet)
                                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Đã cập nhật tên cho user cũ: " + finalNameToSet))
                                            .addOnFailureListener(e -> Log.e(TAG, "Lỗi cập nhật tên: ", e));
                                }
                            }
                        }
                    }
                });
    }

    private void handleFailedLogin(Task<?> task) {
        String errorMsg = task.getException() != null ?
                task.getException().getMessage() : "Lỗi không xác định";

        Log.e(TAG, "Login failed: " + errorMsg, task.getException());

        String lowerMsg = errorMsg != null ? errorMsg.toLowerCase() : "";

        if (lowerMsg.contains("invalid-credential") || lowerMsg.contains("wrong-password")) {
            Toast.makeText(this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_LONG).show();
            edtPassword.setError("Mật khẩu không đúng");
            edtPassword.requestFocus();
        } else if (lowerMsg.contains("user-not-found")) {
            Toast.makeText(this, "Tài khoản không tồn tại", Toast.LENGTH_LONG).show();
            edtEmail.setError("Email chưa đăng ký");
            edtEmail.requestFocus();
        } else if (lowerMsg.contains("too-many-requests")) {
            Toast.makeText(this, "Quá nhiều lần thử. Vui lòng thử lại sau", Toast.LENGTH_LONG).show();
        } else if (lowerMsg.contains("network-error")) {
            Toast.makeText(this, "Lỗi kết nối mạng. Vui lòng kiểm tra internet", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Đăng nhập thất bại: " + errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void setupClickListeners() {
        logInBtn.setOnClickListener(v -> loginUserWithEmail());

        signUpBtn.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });

        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        btnFacebook.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng đăng nhập Facebook đang phát triển", Toast.LENGTH_SHORT).show();
            // TODO: Implement Facebook login
        });

        btnApple.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng đăng nhập Apple đang phát triển", Toast.LENGTH_SHORT).show();
            // TODO: Implement Apple login
        });

        if (forgotPassword != null) {
            forgotPassword.setOnClickListener(v -> sendPasswordReset());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        logInBtn.setEnabled(true);
    }
}
