package com.example.rise_of_city.ui.vocabulary;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.Vocabulary;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VocabularyListActivity extends AppCompatActivity {
    
    private RecyclerView rvVocabulary;
    private VocabularyAdapter adapter;
    private TextView tvEmptyState;
    private AppDatabase appDatabase;
    private ExecutorService executorService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_list);
        
        // Get topic info from intent
        String topicId = getIntent().getStringExtra("topic_id");
        String topicTitle = getIntent().getStringExtra("topic_title");
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(topicTitle != null ? topicTitle : "Từ vựng");
        }
        
        // Initialize database
        appDatabase = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        
        // Initialize views
        rvVocabulary = findViewById(R.id.rv_vocabulary);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        
        // Setup RecyclerView
        rvVocabulary.setLayoutManager(new LinearLayoutManager(this));
        
        // Load vocabulary from Room DB
        loadVocabulary(topicId);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    private void loadVocabulary(String topicId) {
        if (topicId == null) {
            showEmptyState("Không tìm thấy chủ đề");
            return;
        }
        
        executorService.execute(() -> {
            List<Vocabulary> vocabularies = appDatabase.vocabularyDao().getVocabulariesByTopic(topicId);
            
            runOnUiThread(() -> {
                if (vocabularies == null || vocabularies.isEmpty()) {
                    showEmptyState("Chưa có từ vựng nào. Vui lòng import từ vựng trước.");
                } else {
                    List<VocabularyItem> vocabularyList = convertToVocabularyItems(vocabularies);
                    adapter = new VocabularyAdapter(vocabularyList, this);
                    rvVocabulary.setAdapter(adapter);
                    tvEmptyState.setVisibility(android.view.View.GONE);
                    rvVocabulary.setVisibility(android.view.View.VISIBLE);
                }
            });
        });
    }
    
    private List<VocabularyItem> convertToVocabularyItems(List<Vocabulary> vocabularies) {
        List<VocabularyItem> items = new ArrayList<>();
        
        for (Vocabulary vocab : vocabularies) {
            String imageResourceName = null;
            boolean hasImage = false;
            
            if (vocab.imageName != null && !vocab.imageName.isEmpty()) {
                // Clean image name (remove extension)
                imageResourceName = vocab.imageName.contains(".") 
                    ? vocab.imageName.substring(0, vocab.imageName.lastIndexOf(".")) 
                    : vocab.imageName;
                hasImage = true;
            }
            
            items.add(new VocabularyItem(
                vocab.english,
                vocab.vietnamese,
                imageResourceName,
                hasImage
            ));
        }
        
        return items;
    }
    
    private void showEmptyState(String message) {
        tvEmptyState.setText(message);
        tvEmptyState.setVisibility(android.view.View.VISIBLE);
        rvVocabulary.setVisibility(android.view.View.GONE);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

