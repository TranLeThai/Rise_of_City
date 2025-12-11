package com.example.rise_of_city;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button questsButton = findViewById(R.id.button_quests);
        questsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QuestsActivity.class);
            startActivity(intent);
        });
    }
}
