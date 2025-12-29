package com.example.rise_of_city.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.User;
import com.example.rise_of_city.data.local.Vocabulary;
import com.example.rise_of_city.data.model.user.SearchTopic;
import com.example.rise_of_city.data.model.user.SearchUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    
    private AppDatabase appDatabase;
    private int currentUserId = -1;
    private ExecutorService executorService;
    
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize Room Database
        appDatabase = AppDatabase.getInstance(requireContext());
        executorService = Executors.newSingleThreadExecutor();
        
        // Get current user ID from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("RiseOfCity_Prefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getInt("logged_user_id", -1);

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
        searchAdapter = new SearchAdapter(new ArrayList<>(), currentTab, getContext());
        rvSearchResults.setAdapter(searchAdapter);

        // Setup listeners
        setupListeners();

        // Load initial data (Friends tab)
        switchTab("friends");

        return view;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
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
        searchAdapter = new SearchAdapter(new ArrayList<>(), currentTab, getContext());
        rvSearchResults.setAdapter(searchAdapter);

        // Perform search with current query
        performSearch(etSearchInput.getText().toString());
    }

    private void performSearch(String query) {
        // Chỉ kiểm tra đăng nhập cho tab "Bạn Bè"
        if (currentTab.equals("friends") && currentUserId == -1) {
            showEmptyState("Vui lòng đăng nhập để tìm kiếm bạn bè");
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
        executorService.execute(() -> {
            try {
                // Tìm kiếm users trong Room Database
                List<User> allUsers = appDatabase.userDao().getAllUsers();
                Log.d(TAG, "Found " + allUsers.size() + " users in database");
                
                List<SearchUser> searchUsers = new ArrayList<>();
                String lowerQuery = query.toLowerCase();
                
                if (allUsers == null || allUsers.isEmpty()) {
                    Log.d(TAG, "No users found in database");
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            showEmptyState("Không có người dùng nào trong hệ thống");
                        });
                    }
                    return;
                }
                
                for (User user : allUsers) {
                    // Skip current user
                    if (user.id == currentUserId) {
                        continue;
                    }
                    
                    // Search by name or email
                    boolean matchesName = user.fullName != null && user.fullName.toLowerCase().contains(lowerQuery);
                    boolean matchesEmail = user.email != null && user.email.toLowerCase().contains(lowerQuery);
                    
                    if (matchesName || matchesEmail) {
                        SearchUser searchUser = new SearchUser();
                        searchUser.setUid(String.valueOf(user.id));
                        searchUser.setName(user.fullName != null ? user.fullName : "Người dùng");
                        searchUser.setEmail(user.email);
                        searchUser.setSurveyLevel(user.surveyCompleted ? "Đã khảo sát" : "Chưa khảo sát");
                        searchUser.setFriend(false);
                        searchUsers.add(searchUser);
                    }
                }
                
                Log.d(TAG, "Found " + searchUsers.size() + " matching users");
                
                // Update UI on main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> updateUserResults(searchUsers));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error searching users: ", e);
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        showEmptyState("Lỗi khi tìm kiếm người dùng: " + e.getMessage());
                    });
                }
            }
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
        // Tìm kiếm trong các chủ đề từ vựng từ Room Database
        executorService.execute(() -> {
            try {
                List<SearchTopic> topics = new ArrayList<>();
                String lowerQuery = query.toLowerCase();
                
                // Lấy danh sách tất cả topics từ Vocabulary database
                List<String> allTopicIds = appDatabase.vocabularyDao().getAllTopics();
                
                // Map topic ID sang tên hiển thị
                java.util.Map<String, String> topicNames = new java.util.HashMap<>();
                topicNames.put("house", "Nhà Ở");
                topicNames.put("school", "Trường Học");
                topicNames.put("library", "Thư Viện");
                topicNames.put("park", "Công Viên");
                topicNames.put("bakery", "Tiệm Bánh");
                topicNames.put("coffee", "Quán Cafe");
                topicNames.put("farm", "Nông Trại");
                topicNames.put("clothes", "Shop Quần Áo");

                java.util.Map<String, String> topicDescriptions = new java.util.HashMap<>();
                topicDescriptions.put("house", "Học từ vựng về ngôi nhà và nội thất");
                topicDescriptions.put("school", "Học từ vựng về trường học và lớp học");
                topicDescriptions.put("library", "Học từ vựng về thư viện và sách");
                topicDescriptions.put("park", "Học từ vựng về công viên và thiên nhiên");
                topicDescriptions.put("bakery", "Học từ vựng về tiệm bánh và đồ ăn");
                topicDescriptions.put("coffee", "Học từ vựng về quán cà phê");
                topicDescriptions.put("farm", "Học từ vựng về nông trại và nông nghiệp");
                topicDescriptions.put("clothes", "Học từ vựng về quần áo và thời trang");
                
                // Nếu không có topics trong DB, sử dụng danh sách mặc định
                if (allTopicIds.isEmpty()) {
                    allTopicIds.add("house");
                    allTopicIds.add("school");
                    allTopicIds.add("library");
                    allTopicIds.add("park");
                    allTopicIds.add("bakery");
                    allTopicIds.add("coffee");
                    allTopicIds.add("farm");
                    allTopicIds.add("clothes");
                }
                
                for (String topicId : allTopicIds) {
                    // Lấy số lượng từ vựng thực tế
                    int vocabCount = appDatabase.vocabularyDao().countVocabulariesByTopic(topicId);
                    Log.d(TAG, "Topic: " + topicId + " has " + vocabCount + " vocabularies");
                    
                    // Lấy từ vựng có hình ảnh đầu tiên để hiển thị
                    List<Vocabulary> vocabulariesWithImages = appDatabase.vocabularyDao().getVocabulariesWithImages(topicId);
                    String imageName = null;
                    if (!vocabulariesWithImages.isEmpty()) {
                        Vocabulary firstVocab = vocabulariesWithImages.get(0);
                        imageName = firstVocab.imageName;
                        Log.d(TAG, "Found image for topic " + topicId + ": " + imageName);
                        // Remove extension để dùng làm drawable resource name
                        if (imageName != null && imageName.contains(".")) {
                            imageName = imageName.substring(0, imageName.lastIndexOf("."));
                        }
                    } else {
                        Log.w(TAG, "No images found for topic: " + topicId);
                    }
                    
                    // Fallback to icon nếu không có hình ảnh
                    if (imageName == null || imageName.isEmpty()) {
                        imageName = topicId + "_icon";
                    }
                    
                    String title = topicNames.getOrDefault(topicId, topicId);
                    String description = topicDescriptions.getOrDefault(topicId, "Học từ vựng về " + title);
                    
                    // Check if matches query
                    if (query.isEmpty() || 
                        title.toLowerCase().contains(lowerQuery) ||
                        description.toLowerCase().contains(lowerQuery) ||
                        topicId.toLowerCase().contains(lowerQuery)) {
                        
                        SearchTopic topic = new SearchTopic();
                        topic.setId(topicId);
                        topic.setTitle(title);
                        topic.setDescription(description);
                        topic.setCategory("Từ vựng");
                        topic.setImageUrl(imageName);
                        topic.setLevel("Phổ thông");
                        topic.setLessonCount(vocabCount);
                        
                        topics.add(topic);
                    }
                }
                
                // Update UI on main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> updateTopicResults(topics));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error searching topics: ", e);
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        showEmptyState("Lỗi khi tìm kiếm chủ đề: " + e.getMessage());
                    });
                }
            }
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
        // Kiểm tra đăng nhập trước khi load users
        if (currentUserId == -1) {
            showEmptyState("Vui lòng đăng nhập để xem danh sách bạn bè");
            return;
        }
        
        showLoading(true);
        executorService.execute(() -> {
            try {
                List<User> allUsers = appDatabase.userDao().getAllUsers();
                Log.d(TAG, "Loading all users: " + (allUsers != null ? allUsers.size() : 0) + " users found");
                
                List<SearchUser> searchUsers = new ArrayList<>();
                
                if (allUsers != null && !allUsers.isEmpty()) {
                    for (User user : allUsers) {
                        // Skip current user
                        if (user.id == currentUserId) {
                            continue;
                        }
                        
                        SearchUser searchUser = new SearchUser();
                        searchUser.setUid(String.valueOf(user.id));
                        searchUser.setName(user.fullName != null ? user.fullName : "Người dùng");
                        searchUser.setEmail(user.email);
                        searchUser.setSurveyLevel(user.surveyCompleted ? "Đã khảo sát" : "Chưa khảo sát");
                        searchUser.setFriend(false);
                        searchUsers.add(searchUser);
                    }
                }
                
                Log.d(TAG, "Loaded " + searchUsers.size() + " users (excluding current user)");
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (searchUsers.isEmpty()) {
                            showEmptyState("Không có người dùng nào trong hệ thống");
                        } else {
                            updateUserResults(searchUsers);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading users: ", e);
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        showEmptyState("Lỗi khi tải danh sách người dùng: " + e.getMessage());
                    });
                }
            }
        });
    }

    private void loadAllTopics() {
        showLoading(true);
        hideEmptyState();
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
        private Context context;

        public SearchAdapter(List<Object> items, String currentTab, Context context) {
            this.items = items;
            this.currentTab = currentTab;
            this.context = context;
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
                holder.bindUser(user, context);
            } else if (currentTab.equals("topics") && items.get(position) instanceof SearchTopic) {
                SearchTopic topic = (SearchTopic) items.get(position);
                holder.bindTopic(topic, context);
            }

            holder.itemView.setOnClickListener(v -> {
                if (currentTab.equals("friends") && items.get(position) instanceof SearchUser) {
                    SearchUser user = (SearchUser) items.get(position);
                    // Navigate to local user profile (Room Database)
                    android.content.Intent intent = new android.content.Intent(v.getContext(), 
                        com.example.rise_of_city.ui.profile.LocalUserProfileActivity.class);
                    intent.putExtra("user_id", Integer.parseInt(user.getUid()));
                    v.getContext().startActivity(intent);
                } else if (currentTab.equals("topics") && items.get(position) instanceof SearchTopic) {
                    SearchTopic topic = (SearchTopic) items.get(position);
                    // Navigate to vocabulary list
                    android.content.Intent intent = new android.content.Intent(v.getContext(), 
                        com.example.rise_of_city.ui.vocabulary.VocabularyListActivity.class);
                    intent.putExtra("topic_id", topic.getId());
                    intent.putExtra("topic_title", topic.getTitle());
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
            ImageView ivItemIcon;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvItemName = itemView.findViewById(R.id.tv_item_name);
                tvItemSubtitle = itemView.findViewById(R.id.tv_item_subtitle);
                ivItemIcon = itemView.findViewById(R.id.iv_item_icon);
            }

            void bindUser(SearchUser user, Context context) {
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
                
                // Hide icon for users
                if (ivItemIcon != null) {
                    ivItemIcon.setVisibility(View.GONE);
                }
            }

            void bindTopic(SearchTopic topic, Context context) {
                tvItemName.setText(topic.getTitle() != null ? topic.getTitle() : "Chủ đề");
                String subtitle = "";
                if (topic.getDescription() != null) {
                    subtitle = topic.getDescription();
                }
                if (topic.getLevel() != null && !topic.getLevel().isEmpty()) {
                    subtitle += subtitle.isEmpty() ? topic.getLevel() : " • " + topic.getLevel();
                }
                tvItemSubtitle.setText(subtitle);
                tvItemSubtitle.setVisibility(View.VISIBLE);
                
                // Show icon for topics
                if (ivItemIcon != null && context != null) {
                    ivItemIcon.setVisibility(View.VISIBLE);
                    
                    // Load icon based on image name from topic
                    String imageName = topic.getImageUrl();
                    if (imageName != null && !imageName.isEmpty()) {
                        // Try to load image by name
                        int drawableId = getDrawableIdFromName(context, imageName);
                        if (drawableId != 0) {
                            ivItemIcon.setImageResource(drawableId);
                            Log.d("SearchFragment", "Loaded image: " + imageName + " for topic: " + topic.getId());
                        } else {
                            // Try with topic ID + _icon as fallback
                            String fallbackName = topic.getId() + "_icon";
                            drawableId = getDrawableIdFromName(context, fallbackName);
                            if (drawableId != 0) {
                                ivItemIcon.setImageResource(drawableId);
                            } else {
                                // Default icon
                                ivItemIcon.setImageResource(R.drawable.ic_book);
                                Log.w("SearchFragment", "Could not load image: " + imageName + " for topic: " + topic.getId());
                            }
                        }
                    } else {
                        // Try topic ID + _icon
                        String fallbackName = topic.getId() + "_icon";
                        int drawableId = getDrawableIdFromName(context, fallbackName);
                        if (drawableId != 0) {
                            ivItemIcon.setImageResource(drawableId);
                        } else {
                            ivItemIcon.setImageResource(R.drawable.ic_book);
                        }
                    }
                }
            }
            
            private int getDrawableIdFromName(Context context, String name) {
                try {
                    // Remove any path separators and ensure clean name
                    String cleanName = name.replace("/", "_").replace("\\", "_");
                    // Remove extension if present
                    if (cleanName.contains(".")) {
                        cleanName = cleanName.substring(0, cleanName.lastIndexOf("."));
                    }
                    // Convert to lowercase for resource lookup
                    cleanName = cleanName.toLowerCase();
                    int resId = context.getResources().getIdentifier(cleanName, "drawable", context.getPackageName());
                    if (resId == 0) {
                        // Try with original case
                        resId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
                    }
                    return resId;
                } catch (Exception e) {
                    Log.e("SearchFragment", "Error getting drawable ID for: " + name, e);
                    return 0;
                }
            }
        }
    }
}
