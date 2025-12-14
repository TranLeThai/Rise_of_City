package com.example.rise_of_city.ui.quest;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.Quest;

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
        quests.add(new Quest("Hoàn thành tất cả nhiệm vụ", 580, 800, 0, 7, R.drawable.ic_apple));
        quests.add(new Quest("Bắt liên tục mực", 150, 500, 0, 3, R.drawable.ic_chat));
        quests.add(new Quest("Tặng vàng cho bạn", 160, 600, 0, 4, R.drawable.gold_icon));
        quests.add(new Quest("Đạt điểm chơi ngay", 120, 100, 0, 3000, R.drawable.ic_apple));
        quests.add(new Quest("Bắn một viên đạn và đạt điểm", 125, 200, 0, 100, R.drawable.ic_apple));


        QuestsAdapter adapter = new QuestsAdapter(quests);
        recyclerView.setAdapter(adapter);

        ImageButton closeButton = findViewById(R.id.button_close);
        closeButton.setOnClickListener(v -> finish());
    }
}
