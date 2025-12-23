package com.example.rise_of_city.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.SearchTopic;
import com.example.rise_of_city.data.model.SearchUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private static final long SEARCH_DELAY_MS = 500; // Debounce delay

    private EditText etSearchInput;
    private ImageView ivBackArrow;
    private TextView tabFriends, tabTopics;
    private RecyclerView rvSearchResults;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    
    private SearchAdapter searchAdapter;
    private String currentTab = "friends"; // "friends" or "topics"
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize views
        etSearchInput = view.findViewById(R.id.et_search_input);
        ivBackArrow = view.findViewById(R.id.iv_back_arrow);
        tabFriends = view.findViewById(R.id.tab_friends);
        tabTopics = view.findViewById(R.id.tab_topics);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);

        // Initialize RecyclerView
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        searchAdapter = new SearchAdapter(new ArrayList<>(), currentTab);
        rvSearchResults.setAdapter(searchAdapter);

        // Setup listeners
        setupListeners();

        // Load initial data (Friends tab)
        switchTab("friends");

        return view;
    }

    private void setupListeners() {
        // Back arrow click - navigate to home
        ivBackArrow.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.rise_of_city.ui.main.MainActivity) {
                com.example.rise_of_city.ui.main.MainActivity mainActivity = 
                    (com.example.rise_of_city.ui.main.MainActivity) getActivity();
                mainActivity.setSelectedNavItem(R.id.nav_item_home);
            }
        });

        // Tab click listeners
        tabFriends.setOnClickListener(v -> switchTab("friends"));
        tabTopics.setOnClickListener(v -> switchTab("topics"));

        // Search input text change listener with debounce
        etSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel previous search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Schedule new search with delay
                searchRunnable = () -> performSearch(s.toString());
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void switchTab(String tab) {
        currentTab = tab;

        // Update tab appearance
        if (tab.equals("friends")) {
            tabFriends.setTextColor(0xFF000000); // Black
            tabTopics.setTextColor(0xFF999999); // Gray
            tabFriends.setTextSize(17);
            tabTopics.setTextSize(16);
        } else {
            tabTopics.setTextColor(0xFF000000); // Black
            tabFriends.setTextColor(0xFF999999); // Gray
            tabTopics.setTextSize(17);
            tabFriends.setTextSize(16);
        }

        // Update adapter type
        searchAdapter = new SearchAdapter(new ArrayList<>(), currentTab);
        rvSearchResults.setAdapter(searchAdapter);

        // Perform search with current query
        performSearch(etSearchInput.getText().toString());
    }

    private void performSearch(String query) {
        if (currentUser == null) {
            showEmptyState("Vui lòng đăng nhập để tìm kiếm");
            return;
        }

        String trimmedQuery = query.trim();
        
        if (trimmedQuery.isEmpty()) {
            // Load all data when query is empty
            if (currentTab.equals("friends")) {
                loadAllUsers();
            } else {
                loadAllTopics();
            }
            return;
        }

        // Show loading
        showLoading(true);
        hideEmptyState();

        if (currentTab.equals("friends")) {
            searchUsers(trimmedQuery);
        } else {
            searchTopics(trimmedQuery);
        }
    }

    private void searchUsers(String query) {
        db.collection("user_profiles")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<SearchUser> users = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Skip current user
                        if (document.getId().equals(currentUser.getUid())) {
                            continue;
                        }

                        SearchUser user = new SearchUser();
                        user.setUid(document.getId());
                        user.setName(document.getString("name"));
                        user.setEmail(document.getString("email"));
                        user.setSurveyLevel(document.getString("surveyLevel"));
                        user.setAvatarUrl(document.getString("avatarUrl"));
                        
                        // Check if already friend (you can implement friend list later)
                        user.setFriend(false);
                        
                        users.add(user);
                    }

                    // Also search by email
                    if (users.size() < 20) {
                        searchUsersByEmail(query, users);
                    } else {
                        updateUserResults(users);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching users: ", e);
                    showLoading(false);
                    showEmptyState("Lỗi khi tìm kiếm người dùng");
                });
    }

    private void searchUsersByEmail(String query, List<SearchUser> existingUsers) {
        db.collection("user_profiles")
                .whereGreaterThanOrEqualTo("email", query)
                .whereLessThanOrEqualTo("email", query + "\uf8ff")
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Skip current user and duplicates
                        if (document.getId().equals(currentUser.getUid())) {
                            continue;
                        }

                        boolean isDuplicate = false;
                        for (SearchUser existing : existingUsers) {
                            if (existing.getUid().equals(document.getId())) {
                                isDuplicate = true;
                                break;
                            }
                        }

                        if (!isDuplicate) {
                            SearchUser user = new SearchUser();
                            user.setUid(document.getId());
                            user.setName(document.getString("name"));
                            user.setEmail(document.getString("email"));
                            user.setSurveyLevel(document.getString("surveyLevel"));
                            user.setAvatarUrl(document.getString("avatarUrl"));
                            user.setFriend(false);
                            existingUsers.add(user);
                        }
                    }
                    updateUserResults(existingUsers);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching users by email: ", e);
                    updateUserResults(existingUsers);
                });
    }

    private void updateUserResults(List<SearchUser> users) {
        showLoading(false);
        if (users.isEmpty()) {
            showEmptyState("Không tìm thấy người dùng nào");
        } else {
            hideEmptyState();
        }
        searchAdapter.updateUserData(users);
    }

    private void searchTopics(String query) {
        // Search topics có 2 loại:
        // 1. Buildings (chủ đề học từ vựng qua quiz) - tìm trong buildings collection
        // 2. Lesson Topics (chủ đề học bài học do người dùng chia sẻ) - tìm trong topics collection
        List<SearchTopic> topics = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        // 1. Search in topics collection (chủ đề học tập do người dùng chia sẻ)
        db.collection("topics")
                .whereEqualTo("status", "approved") // Chỉ lấy topics đã được approve
                .get()
                .addOnSuccessListener(topicsSnapshot -> {
                    for (QueryDocumentSnapshot topicDoc : topicsSnapshot) {
                        String topicTitle = topicDoc.getString("title");
                        String topicDesc = topicDoc.getString("description");
                        String topicId = topicDoc.getId();
                        
                        // Get lesson count - CHỈ HIỂN THỊ TOPICS CÓ LESSONS
                        Long lessonCount = topicDoc.getLong("lessonCount");
                        int lessonCountInt = lessonCount != null ? lessonCount.intValue() : 0;
                        
                        // Bỏ qua topics không có lessons
                        if (lessonCountInt == 0) {
                            continue;
                        }
                        
                        // Check if matches query
                        if (query.isEmpty() || 
                            (topicTitle != null && topicTitle.toLowerCase().contains(lowerQuery)) ||
                            (topicDesc != null && topicDesc.toLowerCase().contains(lowerQuery))) {
                            
                            SearchTopic topic = new SearchTopic();
                            topic.setId(topicId);
                            topic.setTitle(topicTitle != null ? topicTitle : topicId);
                            topic.setDescription(topicDesc != null ? topicDesc : "");
                            topic.setCategory("Lesson"); // Đánh dấu là lesson topic
                            
                            topic.setLessonCount(lessonCountInt);
                            
                            // Get level
                            String level = topicDoc.getString("level");
                            topic.setLevel(level != null ? level : "Beginner");
                            
                            topics.add(topic);
                        }
                    }
                    
                    // 2. Also search in buildings collection (nếu cần)
                    if (topics.size() < 20) {
                        searchBuildingsAsTopics(query, topics);
                    } else {
                        updateTopicResults(topics);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching topics: ", e);
                    // Fallback: search buildings only
                    searchBuildingsAsTopics(query, new ArrayList<>());
                });
    }
    
    private void searchBuildingsAsTopics(String query, List<SearchTopic> existingTopics) {
        String lowerQuery = query.toLowerCase();
        
        // Search in buildings collection
        db.collection("buildings")
                .get()
                .addOnSuccessListener(buildingsSnapshot -> {
                    List<SearchTopic> topics = new ArrayList<>(existingTopics);
                    
                    for (QueryDocumentSnapshot buildingDoc : buildingsSnapshot) {
                        String buildingName = buildingDoc.getString("name");
                        String buildingDesc = buildingDoc.getString("description");
                        String buildingId = buildingDoc.getId();
                        
                        // Check if matches query
                        if (query.isEmpty() || 
                            (buildingName != null && buildingName.toLowerCase().contains(lowerQuery)) ||
                            (buildingDesc != null && buildingDesc.toLowerCase().contains(lowerQuery))) {
                            
                            SearchTopic topic = new SearchTopic();
                            topic.setId(buildingId);
                            topic.setTitle(buildingName != null ? buildingName : buildingId);
                            topic.setDescription(buildingDesc != null ? buildingDesc : "");
                            topic.setCategory("Building");
                            
                            // Get vocabulary count
                            Long vocabCount = buildingDoc.getLong("vocabularyCount");
                            topic.setLessonCount(vocabCount != null ? vocabCount.intValue() : 0);
                            
                            // Set level based on vocabulary count
                            if (vocabCount != null) {
                                if (vocabCount < 20) {
                                    topic.setLevel("Beginner");
                                } else if (vocabCount < 50) {
                                    topic.setLevel("Intermediate");
                                } else {
                                    topic.setLevel("Advanced");
                                }
                            } else {
                                topic.setLevel("Beginner");
                            }
                            
                            topics.add(topic);
                        }
                    }
                    
                    // 2. Also search in vocabularies for more specific results
                    if (topics.size() < 20) {
                        searchVocabulariesAsTopics(query, topics);
                    } else {
                        updateTopicResults(topics);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching buildings: ", e);
                    // Fallback: search vocabularies directly
                    searchVocabulariesAsTopics(query, new ArrayList<>());
                });
    }
    
    private void searchVocabulariesAsTopics(String query, List<SearchTopic> existingTopics) {
        String lowerQuery = query.toLowerCase();
        
        // Search vocabularies and group by buildingId
        db.collection("vocabularies")
                .limit(100) // Limit to avoid too many reads
                .get()
                .addOnSuccessListener(vocabSnapshot -> {
                    // Group vocabularies by buildingId
                    java.util.Map<String, List<String>> buildingVocabs = new java.util.HashMap<>();
                    
                    for (QueryDocumentSnapshot vocabDoc : vocabSnapshot) {
                        String buildingId = vocabDoc.getString("buildingId");
                        String english = vocabDoc.getString("english");
                        String vietnamese = vocabDoc.getString("vietnamese");
                        
                        if (buildingId == null) buildingId = "house";
                        
                        // Check if matches query
                        if (query.isEmpty() || 
                            (english != null && english.toLowerCase().contains(lowerQuery)) ||
                            (vietnamese != null && vietnamese.toLowerCase().contains(lowerQuery))) {
                            
                            if (!buildingVocabs.containsKey(buildingId)) {
                                buildingVocabs.put(buildingId, new ArrayList<>());
                            }
                            buildingVocabs.get(buildingId).add(english != null ? english : "");
                        }
                    }
                    
                    // Create topics from vocabulary groups
                    for (java.util.Map.Entry<String, List<String>> entry : buildingVocabs.entrySet()) {
                        String buildingId = entry.getKey();
                        List<String> vocabs = entry.getValue();
                        
                        // Check if already exists
                        boolean exists = false;
                        for (SearchTopic existing : existingTopics) {
                            if (existing.getId().equals(buildingId)) {
                                exists = true;
                                break;
                            }
                        }
                        
                        if (!exists && !vocabs.isEmpty()) {
                            // Get building info
                            db.collection("buildings").document(buildingId).get()
                                    .addOnSuccessListener(buildingDoc -> {
                                        SearchTopic topic = new SearchTopic();
                                        topic.setId(buildingId);
                                        
                                        if (buildingDoc.exists()) {
                                            topic.setTitle(buildingDoc.getString("name"));
                                            topic.setDescription(buildingDoc.getString("description"));
                                        } else {
                                            topic.setTitle("Từ vựng " + buildingId);
                                            topic.setDescription("Từ vựng về " + buildingId);
                                        }
                                        
                                        topic.setCategory("Vocabulary");
                                        topic.setLessonCount(vocabs.size());
                                        topic.setLevel(vocabs.size() < 20 ? "Beginner" : "Intermediate");
                                        
                                        existingTopics.add(topic);
                                        
                                        if (existingTopics.size() >= 20) {
                                            updateTopicResults(existingTopics);
                                        }
                                    });
                        }
                    }
                    
                    updateTopicResults(existingTopics);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching vocabularies: ", e);
                    updateTopicResults(existingTopics);
                });
    }


    private void updateTopicResults(List<SearchTopic> topics) {
        showLoading(false);
        if (topics.isEmpty()) {
            showEmptyState("Không tìm thấy chủ đề nào");
        } else {
            hideEmptyState();
        }
        searchAdapter.updateTopicData(topics);
    }

    private void loadAllUsers() {
        showLoading(true);
        db.collection("user_profiles")
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<SearchUser> users = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Skip current user
                        if (document.getId().equals(currentUser.getUid())) {
                            continue;
                        }

                        SearchUser user = new SearchUser();
                        user.setUid(document.getId());
                        user.setName(document.getString("name"));
                        user.setEmail(document.getString("email"));
                        user.setSurveyLevel(document.getString("surveyLevel"));
                        user.setAvatarUrl(document.getString("avatarUrl"));
                        user.setFriend(false);
                        users.add(user);
                    }

                    updateUserResults(users);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading users: ", e);
                    showLoading(false);
                    showEmptyState("Lỗi khi tải danh sách người dùng");
                });
    }

    private void loadAllTopics() {
        showLoading(true);
        // Load all buildings as topics
        searchTopics("");
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmptyState(String message) {
        if (tvEmptyState != null) {
            tvEmptyState.setText(message);
            tvEmptyState.setVisibility(View.VISIBLE);
        }
        if (rvSearchResults != null) {
            rvSearchResults.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
        }
        if (rvSearchResults != null) {
            rvSearchResults.setVisibility(View.VISIBLE);
        }
    }

    // RecyclerView Adapter
    private static class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
        private List<Object> items;
        private String currentTab;

        public SearchAdapter(List<Object> items, String currentTab) {
            this.items = items;
            this.currentTab = currentTab;
        }

        public void updateUserData(List<SearchUser> users) {
            this.items = new ArrayList<>(users);
            notifyDataSetChanged();
        }

        public void updateTopicData(List<SearchTopic> topics) {
            this.items = new ArrayList<>(topics);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_search_result, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (currentTab.equals("friends") && items.get(position) instanceof SearchUser) {
                SearchUser user = (SearchUser) items.get(position);
                holder.bindUser(user);
            } else if (currentTab.equals("topics") && items.get(position) instanceof SearchTopic) {
                SearchTopic topic = (SearchTopic) items.get(position);
                holder.bindTopic(topic);
            }

            holder.itemView.setOnClickListener(v -> {
                if (currentTab.equals("friends") && items.get(position) instanceof SearchUser) {
                    SearchUser user = (SearchUser) items.get(position);
                    // Navigate to user profile
                    android.content.Intent intent = new android.content.Intent(v.getContext(), 
                        com.example.rise_of_city.ui.profile.UserProfileActivity.class);
                    intent.putExtra("userId", user.getUid());
                    v.getContext().startActivity(intent);
                } else if (currentTab.equals("topics") && items.get(position) instanceof SearchTopic) {
                    SearchTopic topic = (SearchTopic) items.get(position);
                    // Navigate to topic detail
                    android.content.Intent intent = new android.content.Intent(v.getContext(), 
                        com.example.rise_of_city.ui.topic.TopicDetailActivity.class);
                    intent.putExtra("topicId", topic.getId());
                    intent.putExtra("topic", topic);
                    v.getContext().startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemName;
            TextView tvItemSubtitle;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvItemName = itemView.findViewById(R.id.tv_item_name);
                tvItemSubtitle = itemView.findViewById(R.id.tv_item_subtitle);
            }

            void bindUser(SearchUser user) {
                tvItemName.setText(user.getName() != null ? user.getName() : "Người dùng");
                String subtitle = "";
                if (user.getEmail() != null) {
                    subtitle = user.getEmail();
                }
                if (user.getSurveyLevel() != null && !user.getSurveyLevel().isEmpty()) {
                    subtitle += subtitle.isEmpty() ? user.getSurveyLevel() : " • " + user.getSurveyLevel();
                }
                tvItemSubtitle.setText(subtitle);
                tvItemSubtitle.setVisibility(View.VISIBLE);
            }

            void bindTopic(SearchTopic topic) {
                tvItemName.setText(topic.getTitle() != null ? topic.getTitle() : "Chủ đề");
                String subtitle = "";
                if (topic.getDescription() != null) {
                    subtitle = topic.getDescription();
                }
                if (topic.getLevel() != null && !topic.getLevel().isEmpty()) {
                    subtitle += subtitle.isEmpty() ? topic.getLevel() : " • " + topic.getLevel();
                }
                if (topic.getLessonCount() > 0) {
                    subtitle += subtitle.isEmpty() ? topic.getLessonCount() + " bài học" 
                        : " • " + topic.getLessonCount() + " bài học";
                }
                tvItemSubtitle.setText(subtitle);
                tvItemSubtitle.setVisibility(View.VISIBLE);
            }
        }
    }
}
