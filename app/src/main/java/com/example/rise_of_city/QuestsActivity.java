package com.example.rise_of_city;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class QuestsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quests);

        RecyclerView recyclerView = findViewById(R.id.recyclerView_quests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sample data
        List<Quest> quests = new ArrayList<>();
        quests.add(new Quest("Hoàn thành tất cả nhiệm vụ", "+580 Coin, +800 XP", 0, 7, android.R.drawable.ic_menu_agenda));
        quests.add(new Quest("Bắt liên tục mực", "+150 Coin, +500 XP", 0, 3, android.R.drawable.star_on));
        quests.add(new Quest("Tặng vàng cho bạn", "+160 Coin, +600 XP", 0, 4, android.R.drawable.ic_menu_myplaces));

        QuestsAdapter adapter = new QuestsAdapter(quests);
        recyclerView.setAdapter(adapter);

        ImageButton closeButton = findViewById(R.id.button_close);
        closeButton.setOnClickListener(v -> finish());
    }
}
