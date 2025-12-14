package com.example.rise_of_city.ui.test;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.rise_of_city.R;
import com.example.rise_of_city.ui.dialog.LockAreaDialogFragment;

public class TestLockDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_lock_dialog);

        Button btnShowDialog = findViewById(R.id.btn_show_dialog);
        
        btnShowDialog.setOnClickListener(v -> {
            showLockDialog();
        });
    }

    private void showLockDialog() {
        LockAreaDialogFragment dialog = LockAreaDialogFragment.newInstance("Thì hiện tại đơn");
        
        dialog.setOnLearnNowClickListener(() -> {
            // Xử lý khi bấm "Học Ngay"
            // TODO: Navigate to lesson screen
        });
        
        dialog.setOnCloseClickListener(() -> {
            // Xử lý khi bấm "Đóng"
        });
        
        FragmentManager fm = getSupportFragmentManager();
        dialog.show(fm, "LockAreaDialog");
    }
}

