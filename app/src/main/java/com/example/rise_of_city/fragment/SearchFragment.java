package com.example.rise_of_city.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText etSearchInput;
    private ImageView ivBackArrow;
    private TextView tabFriends, tabTopics;
    private RecyclerView rvSearchResults;
    private SearchAdapter searchAdapter;
    private String currentTab = "friends"; // "friends" or "topics"
    private List<String> friendsList = new ArrayList<>();
    private List<String> topicsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize views
        etSearchInput = view.findViewById(R.id.et_search_input);
        ivBackArrow = view.findViewById(R.id.iv_back_arrow);
        tabFriends = view.findViewById(R.id.tab_friends);
        tabTopics = view.findViewById(R.id.tab_topics);
        rvSearchResults = view.findViewById(R.id.rv_search_results);

        // Initialize RecyclerView
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        searchAdapter = new SearchAdapter(new ArrayList<>());
        rvSearchResults.setAdapter(searchAdapter);

        // Initialize sample data
        initializeSampleData();

        // Setup listeners
        setupListeners();

        // Load initial data (Friends tab)
        switchTab("friends");

        return view;
    }

    private void initializeSampleData() {
        // Sample friends data
        for (int i = 0; i < 10; i++) {
            friendsList.add("Item " + i);
        }

        // Sample topics data
        for (int i = 0; i < 10; i++) {
            topicsList.add("Chủ đề " + i);
        }
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

        // Search input text change listener
        etSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterResults(s.toString());
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

        // Update results based on current tab
        filterResults(etSearchInput.getText().toString());
    }

    private void filterResults(String query) {
        List<String> dataList = currentTab.equals("friends") ? friendsList : topicsList;
        List<String> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(dataList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (String item : dataList) {
                if (item.toLowerCase().contains(lowerQuery)) {
                    filteredList.add(item);
                }
            }
        }

        searchAdapter.updateData(filteredList);
    }

    // RecyclerView Adapter
    private static class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
        private List<String> items;

        public SearchAdapter(List<String> items) {
            this.items = items;
        }

        public void updateData(List<String> newItems) {
            this.items = newItems;
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
            holder.tvItemName.setText(items.get(position));
            holder.itemView.setOnClickListener(v -> {
                // Handle item click
                Toast.makeText(v.getContext(), "Clicked: " + items.get(position), Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemName;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvItemName = itemView.findViewById(R.id.tv_item_name);
            }
        }
    }
}
