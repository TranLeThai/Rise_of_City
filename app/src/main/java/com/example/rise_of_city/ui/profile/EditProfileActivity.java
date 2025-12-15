package com.example.rise_of_city.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rise_of_city.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    
    private EditText edtName, edtPhone, edtAddress;
    private Button btnSave;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
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
        
        db.collection("user_profiles")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false);
                    
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String phone = documentSnapshot.getString("phone");
                        String address = documentSnapshot.getString("address");
                        
                        if (name != null) edtName.setText(name);
                        if (phone != null) edtPhone.setText(phone);
                        if (address != null) edtAddress.setText(address);
                    } else {
                        // Load from Auth if Firestore doesn't have data
                        String displayName = currentUser.getDisplayName();
                        if (displayName != null) {
                            edtName.setText(displayName);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error loading profile: ", e);
                    // Load from Auth as fallback
                    String displayName = currentUser.getDisplayName();
                    if (displayName != null) {
                        edtName.setText(displayName);
                    }
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
        
        showLoading(true);
        btnSave.setEnabled(false);
        
        // Update Firebase Auth display name
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        
        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update Firestore
                        updateFirestoreProfile(name, phone, address);
                    } else {
                        showLoading(false);
                        btnSave.setEnabled(true);
                        Log.e(TAG, "Error updating Auth profile: ", task.getException());
                        Toast.makeText(this, "Lỗi cập nhật tên", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void updateFirestoreProfile(String name, String phone, String address) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("address", address);
        updates.put("updatedAt", System.currentTimeMillis());
        
        db.collection("user_profiles")
                .document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Đã cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    btnSave.setEnabled(true);
                    Log.e(TAG, "Error updating Firestore profile: ", e);
                    Toast.makeText(this, "Lỗi cập nhật hồ sơ", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}

