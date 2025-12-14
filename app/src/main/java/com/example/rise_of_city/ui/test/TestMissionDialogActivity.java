package com.example.rise_of_city.ui.test;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.rise_of_city.R;
import com.example.rise_of_city.ui.dialog.MissionDialogFragment;

public class TestMissionDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mission_dialog);

        Button btnShowDialog = findViewById(R.id.btn_show_mission_dialog);
        
        btnShowDialog.setOnClickListener(v -> {
            showMissionDialog();
        });
    }

    private void showMissionDialog() {
        MissionDialogFragment dialog = MissionDialogFragment.newInstance(
            "Mission random",
            "Hoàn thành 10 câu hỏi về thì hiện tại đơn để nhận được 100 coin và 50 XP!"
        );
        
        dialog.setOnAcceptClickListener(() -> {
            Toast.makeText(this, "Đã chấp nhận mission!", Toast.LENGTH_SHORT).show();
            // TODO: Xử lý logic khi chấp nhận mission
        });
        
        dialog.setOnDenyClickListener(() -> {
            Toast.makeText(this, "Đã từ chối mission!", Toast.LENGTH_SHORT).show();
            // TODO: Xử lý logic khi từ chối mission
        });
        
        FragmentManager fm = getSupportFragmentManager();
        dialog.show(fm, "MissionDialog");
    }
}

