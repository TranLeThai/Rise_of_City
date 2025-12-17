package com.example.rise_of_city.ui.quest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.Quest;
import com.example.rise_of_city.data.repository.QuestRepository;
import com.example.rise_of_city.ui.ingame.InGameActivity;

import java.util.List;

public class QuestsActivity extends AppCompatActivity {
    
    private static final String TAG = "QuestsActivity";
    private QuestRepository questRepository;
    private QuestsAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quests);

        questRepository = QuestRepository.getInstance();
        
        recyclerView = findViewById(R.id.recyclerView_quests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load quests from Firebase
        loadQuests();

        ImageButton closeButton = findViewById(R.id.button_close);
        closeButton.setOnClickListener(v -> finish());
    }
    
    private void loadQuests() {
        questRepository.getAllQuests(new QuestRepository.OnQuestsLoadedListener() {
            @Override
            public void onQuestsLoaded(List<Quest> quests) {
                // Convert icon names to resource IDs
                for (Quest quest : quests) {
                    if (quest.getIconName() != null && !quest.getIconName().isEmpty()) {
                        int iconResId = QuestRepository.getIconResourceId(QuestsActivity.this, quest.getIconName());
                        quest.setIconResId(iconResId);
                    }
                }
                
                // Update adapter
                if (adapter == null) {
                    adapter = new QuestsAdapter(quests, QuestsActivity.this);
                    recyclerView.setAdapter(adapter);
                } else {
                    adapter.updateQuests(quests);
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading quests: " + error);
                Toast.makeText(QuestsActivity.this, "Lỗi khi tải nhiệm vụ: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload quests when returning to this activity (progress may have changed)
        loadQuests();
    }
}
