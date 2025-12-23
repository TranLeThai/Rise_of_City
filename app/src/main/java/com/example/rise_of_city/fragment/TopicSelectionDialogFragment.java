package com.example.rise_of_city.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.SearchTopic;
import com.example.rise_of_city.ui.quiz.VocabularyQuizActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog Fragment để chọn topic liên quan đến vocab word
 * Sau khi chọn topic, mở quiz activity
 * CHỈ DÙNG CHO VOCAB SCREEN
 */
public class TopicSelectionDialogFragment extends DialogFragment {

    private static final String ARG_BUILDING_ID = "building_id";
    private static final String ARG_VOCAB_WORD = "vocab_word";
    private static final String TAG = "TopicSelectionDialog";
    
    private LinearLayout topicsContainer;
    private ProgressBar progressBar;
    private TextView tvTitle;
    private TextView tvEmpty;
    private ImageButton btnClose;
    
    private String buildingId;
    private String vocabWord;
    private FirebaseFirestore firestore;
    private List<SearchTopic> topics;

    public static TopicSelectionDialogFragment newInstance(String buildingId, String vocabWord) {
        TopicSelectionDialogFragment fragment = new TopicSelectionDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BUILDING_ID, buildingId);
        args.putString(ARG_VOCAB_WORD, vocabWord);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set style để dialog không fullscreen, chỉ là dialog bình thường
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            // Đảm bảo dialog không fullscreen
            dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_topic_selection, container, false);
        
        Bundle args = getArguments();
        if (args != null) {
            buildingId = args.getString(ARG_BUILDING_ID);
            vocabWord = args.getString(ARG_VOCAB_WORD);
            Log.d(TAG, "Dialog created with buildingId: " + buildingId + ", vocabWord: " + vocabWord);
        } else {
            Log.w(TAG, "No arguments provided to dialog");
        }
        
        firestore = FirebaseFirestore.getInstance();
        topics = new ArrayList<>();
        
        topicsContainer = view.findViewById(R.id.topics_container);
        progressBar = view.findViewById(R.id.progress_bar);
        tvTitle = view.findViewById(R.id.tv_title);
        tvEmpty = view.findViewById(R.id.tv_empty);
        btnClose = view.findViewById(R.id.btn_close);
        
        if (topicsContainer == null) {
            Log.e(TAG, "topicsContainer is null in onCreateView!");
        }
        
        if (tvTitle != null && vocabWord != null) {
            tvTitle.setText("Chọn chủ đề cho: " + vocabWord);
        }
        
        // Close button
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }
        
        // Load topics from Firestore
        loadTopics();
        
        return view;
    }
    
    private void loadTopics() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (tvEmpty != null) {
            tvEmpty.setVisibility(View.GONE);
        }
        
        // Load topics from buildings collection (topics are buildings)
        if (buildingId != null && !buildingId.isEmpty()) {
            // Load building as topic
            firestore.collection("buildings")
                    .document(buildingId)
                    .get()
                    .addOnSuccessListener(buildingDoc -> {
                        if (buildingDoc.exists()) {
                            SearchTopic topic = createTopicFromBuilding(buildingDoc);
                            topics.add(topic);
                            displayTopics();
                        } else {
                            // Fallback: load related buildings
                            loadRelatedBuildings();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading building: " + e.getMessage());
                        loadRelatedBuildings();
                    });
        } else {
            // Load all buildings as topics
            loadRelatedBuildings();
        }
    }
    
    private void loadRelatedBuildings() {
        Log.d(TAG, "Loading related buildings...");
        // Load all buildings as topics
        firestore.collection("buildings")
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Loaded " + queryDocumentSnapshots.size() + " buildings");
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        SearchTopic topic = createTopicFromBuilding(doc);
                        topics.add(topic);
                        Log.d(TAG, "Added topic: " + topic.getTitle() + " (id: " + topic.getId() + ")");
                    }
                    displayTopics();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading buildings: " + e.getMessage(), e);
                    // Fallback: Create default topics if Firestore fails
                    createDefaultTopics();
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
    
    private void createDefaultTopics() {
        Log.d(TAG, "Creating default topics as fallback");
        // Create default topics if Firestore fails
        SearchTopic schoolTopic = new SearchTopic();
        schoolTopic.setId("school");
        schoolTopic.setTitle("School");
        schoolTopic.setDescription("Learn vocabulary about school");
        schoolTopic.setCategory("Building");
        schoolTopic.setLessonCount(10);
        topics.add(schoolTopic);
        
        SearchTopic homeTopic = new SearchTopic();
        homeTopic.setId("house");
        homeTopic.setTitle("Home");
        homeTopic.setDescription("Learn vocabulary about home");
        homeTopic.setCategory("Building");
        homeTopic.setLessonCount(10);
        topics.add(homeTopic);
        
        displayTopics();
    }
    
    private SearchTopic createTopicFromBuilding(com.google.firebase.firestore.DocumentSnapshot buildingDoc) {
        SearchTopic topic = new SearchTopic();
        topic.setId(buildingDoc.getId());
        topic.setTitle(buildingDoc.getString("name"));
        topic.setDescription(buildingDoc.getString("description"));
        topic.setCategory("Building");
        Long vocabCount = buildingDoc.getLong("vocabularyCount");
        topic.setLessonCount(vocabCount != null ? vocabCount.intValue() : 0);
        return topic;
    }
    
    private void displayTopics() {
        Log.d(TAG, "Displaying " + topics.size() + " topics");
        
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        
        if (topicsContainer == null) {
            Log.e(TAG, "topicsContainer is null!");
            return;
        }
        
        topicsContainer.removeAllViews();
        
        if (topics.isEmpty()) {
            Log.w(TAG, "No topics to display, creating default topics");
            createDefaultTopics();
            return;
        }
        
        if (tvEmpty != null) {
            tvEmpty.setVisibility(View.GONE);
        }
        
        for (SearchTopic topic : topics) {
            Button topicButton = createTopicButton(topic);
            topicsContainer.addView(topicButton);
            Log.d(TAG, "Added button for topic: " + topic.getTitle());
        }
    }
    
    private Button createTopicButton(SearchTopic topic) {
        Button button = new Button(getContext());
        button.setText(topic.getTitle());
        button.setTextSize(16);
        button.setTextColor(0xFFFFFFFF);
        button.setBackgroundResource(R.drawable.bg_vocab_button);
        button.setPadding(24, 16, 24, 16);
        button.setAllCaps(false);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 8, 8, 8);
        button.setLayoutParams(params);
        
        button.setOnClickListener(v -> {
            // Navigate to quiz activity với topic
            navigateToQuiz(topic);
        });
        
        return button;
    }
    
    private void navigateToQuiz(SearchTopic topic) {
        Log.d(TAG, "navigateToQuiz called with topic: " + topic.getTitle() + ", id: " + topic.getId());
        
        if (getContext() == null) {
            Log.e(TAG, "Context is null, cannot navigate to quiz");
            return;
        }
        
        dismiss();
        
        try {
            // Mở VocabularyQuizActivity với buildingId từ topic
            Intent intent = new Intent(getContext(), VocabularyQuizActivity.class);
            intent.putExtra("buildingId", topic.getId());
            intent.putExtra("quizType", "vocabulary");
            
            Log.d(TAG, "Starting VocabularyQuizActivity with buildingId: " + topic.getId());
            startActivity(intent);
            Log.d(TAG, "Activity started successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error starting quiz activity: " + e.getMessage(), e);
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), "Lỗi mở quiz: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }
}

