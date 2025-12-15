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
                if (mainActivity.getBottomNavigationView() != null) {
                    mainActivity.getBottomNavigationView().setSelectedItemId(R.id.nav_home);
                }
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
        // Search in topics collection (you may need to create this collection)
        // For now, we'll search in a hypothetical "topics" collection
        db.collection("topics")
                .whereGreaterThanOrEqualTo("title", query)
                .whereLessThanOrEqualTo("title", query + "\uf8ff")
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<SearchTopic> topics = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        SearchTopic topic = new SearchTopic();
                        topic.setId(document.getId());
                        topic.setTitle(document.getString("title"));
                        topic.setDescription(document.getString("description"));
                        topic.setLevel(document.getString("level"));
                        topic.setCategory(document.getString("category"));
                        
                        Object lessonCountObj = document.get("lessonCount");
                        if (lessonCountObj != null) {
                            topic.setLessonCount(((Long) lessonCountObj).intValue());
                        }
                        
                        topic.setImageUrl(document.getString("imageUrl"));
                        topics.add(topic);
                    }

                    // Also search by description if needed
                    if (topics.size() < 20) {
                        searchTopicsByDescription(query, topics);
                    } else {
                        updateTopicResults(topics);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching topics: ", e);
                    // If topics collection doesn't exist, create sample topics
                    createSampleTopics(query);
                });
    }

    private void searchTopicsByDescription(String query, List<SearchTopic> existingTopics) {
        db.collection("topics")
                .whereGreaterThanOrEqualTo("description", query)
                .whereLessThanOrEqualTo("description", query + "\uf8ff")
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        boolean isDuplicate = false;
                        for (SearchTopic existing : existingTopics) {
                            if (existing.getId().equals(document.getId())) {
                                isDuplicate = true;
                                break;
                            }
                        }

                        if (!isDuplicate) {
                            SearchTopic topic = new SearchTopic();
                            topic.setId(document.getId());
                            topic.setTitle(document.getString("title"));
                            topic.setDescription(document.getString("description"));
                            topic.setLevel(document.getString("level"));
                            topic.setCategory(document.getString("category"));
                            
                            Object lessonCountObj = document.get("lessonCount");
                            if (lessonCountObj != null) {
                                topic.setLessonCount(((Long) lessonCountObj).intValue());
                            }
                            
                            topic.setImageUrl(document.getString("imageUrl"));
                            existingTopics.add(topic);
                        }
                    }
                    updateTopicResults(existingTopics);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error searching topics by description: ", e);
                    updateTopicResults(existingTopics);
                });
    }

    private void createSampleTopics(String query) {
        // Create sample topics based on game buildings and learning skills
        List<SearchTopic> sampleTopics = new ArrayList<>();
        
        // Topics gắn với các Building trong game
        // School building topics
        SearchTopic[] gameTopics = {
            // School Building - Grammar & Basics
            new SearchTopic("school_grammar_basics", "Ngữ pháp cơ bản", 
                "Học các quy tắc ngữ pháp cơ bản tại School", "Beginner", "School"),
            new SearchTopic("school_present_tense", "Thì hiện tại đơn", 
                "Học cách sử dụng thì hiện tại đơn", "Beginner", "School"),
            new SearchTopic("school_past_tense", "Thì quá khứ đơn", 
                "Học cách sử dụng thì quá khứ đơn", "Beginner", "School"),
            new SearchTopic("school_future_tense", "Thì tương lai", 
                "Học cách diễn đạt tương lai", "Intermediate", "School"),
            new SearchTopic("school_conditionals", "Câu điều kiện", 
                "Học các loại câu điều kiện", "Intermediate", "School"),
            
            // Coffee Shop Building - Conversation & Speaking
            new SearchTopic("coffee_daily_conversation", "Hội thoại hàng ngày", 
                "Luyện nói các tình huống giao tiếp hàng ngày", "Beginner", "Coffee Shop"),
            new SearchTopic("coffee_ordering", "Đặt hàng và mua sắm", 
                "Học cách đặt hàng tại quán cà phê, nhà hàng", "Beginner", "Coffee Shop"),
            new SearchTopic("coffee_small_talk", "Trò chuyện xã giao", 
                "Luyện kỹ năng small talk trong môi trường công sở", "Intermediate", "Coffee Shop"),
            new SearchTopic("coffee_business_meeting", "Họp và thuyết trình", 
                "Kỹ năng giao tiếp trong môi trường kinh doanh", "Advanced", "Coffee Shop"),
            
            // Park Building - Vocabulary & Nature
            new SearchTopic("park_nature_vocab", "Từ vựng về thiên nhiên", 
                "Học từ vựng về cây cối, động vật, môi trường", "Beginner", "Park"),
            new SearchTopic("park_activities", "Hoạt động ngoài trời", 
                "Từ vựng và câu về các hoạt động thể thao, giải trí", "Intermediate", "Park"),
            new SearchTopic("park_environment", "Môi trường và bảo vệ", 
                "Học về môi trường và cách bảo vệ thiên nhiên", "Advanced", "Park"),
            
            // House Building - Daily Life & Home
            new SearchTopic("house_family", "Gia đình và mối quan hệ", 
                "Từ vựng về gia đình, bạn bè, các mối quan hệ", "Beginner", "House"),
            new SearchTopic("house_daily_routine", "Thói quen hàng ngày", 
                "Học cách mô tả các hoạt động hàng ngày", "Beginner", "House"),
            new SearchTopic("house_home_maintenance", "Bảo trì nhà cửa", 
                "Từ vựng về sửa chữa, trang trí nhà cửa", "Intermediate", "House"),
            
            // Library Building - Reading & Writing
            new SearchTopic("library_reading_basics", "Đọc hiểu cơ bản", 
                "Luyện kỹ năng đọc hiểu văn bản đơn giản", "Beginner", "Library"),
            new SearchTopic("library_writing_emails", "Viết email", 
                "Học cách viết email chuyên nghiệp", "Intermediate", "Library"),
            new SearchTopic("library_essay_writing", "Viết luận", 
                "Kỹ năng viết bài luận học thuật", "Advanced", "Library"),
            
            // General Skills
            new SearchTopic("general_pronunciation", "Phát âm", 
                "Luyện phát âm chuẩn và ngữ điệu", "Beginner", "General"),
            new SearchTopic("general_listening", "Luyện nghe", 
                "Cải thiện kỹ năng nghe hiểu", "Intermediate", "General"),
            new SearchTopic("general_idioms", "Thành ngữ và cụm từ", 
                "Học các thành ngữ, cụm từ thông dụng", "Advanced", "General")
        };

        String lowerQuery = query.toLowerCase();
        for (SearchTopic topic : gameTopics) {
            // Set lesson count based on level
            if (topic.getLevel().equals("Beginner")) {
                topic.setLessonCount(5);
            } else if (topic.getLevel().equals("Intermediate")) {
                topic.setLessonCount(8);
            } else {
                topic.setLessonCount(12);
            }
            
            // Filter by query
            if (query.isEmpty() || 
                topic.getTitle().toLowerCase().contains(lowerQuery) ||
                topic.getDescription().toLowerCase().contains(lowerQuery) ||
                topic.getCategory().toLowerCase().contains(lowerQuery) ||
                topic.getLevel().toLowerCase().contains(lowerQuery)) {
                sampleTopics.add(topic);
            }
        }

        updateTopicResults(sampleTopics);
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
        db.collection("topics")
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<SearchTopic> topics = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        SearchTopic topic = new SearchTopic();
                        topic.setId(document.getId());
                        topic.setTitle(document.getString("title"));
                        topic.setDescription(document.getString("description"));
                        topic.setLevel(document.getString("level"));
                        topic.setCategory(document.getString("category"));
                        
                        Object lessonCountObj = document.get("lessonCount");
                        if (lessonCountObj != null) {
                            topic.setLessonCount(((Long) lessonCountObj).intValue());
                        }
                        
                        topic.setImageUrl(document.getString("imageUrl"));
                        topics.add(topic);
                    }

                    if (topics.isEmpty()) {
                        // Create sample topics
                        createSampleTopics("");
                    } else {
                        updateTopicResults(topics);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading topics: ", e);
                    // Create sample topics if collection doesn't exist
                    createSampleTopics("");
                });
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
