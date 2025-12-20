package com.example.rise_of_city.ui.main; // Đổi thành package của bạn

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.fragment.HomeFragment;
import com.example.rise_of_city.fragment.ChatAiFragment;
import com.example.rise_of_city.fragment.RoadMapFragment;
import com.example.rise_of_city.fragment.SearchFragment;
import com.example.rise_of_city.fragment.ProfileFragment;
import com.example.rise_of_city.fragment.NewScreenFragment;
import com.example.rise_of_city.fragment.VocabMatchFragment;

public class MainActivity extends AppCompatActivity {

    private LinearLayout bottomNav;
    private int currentSelectedItemId = R.id.nav_item_home;
    private int previousSelectedItemId = R.id.nav_item_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);

        // Đặt Fragment mặc định là HomeFragment khi mở app
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        // Setup click listeners cho các nav items
        setupNavigationItems();
        
        // Highlight item đầu tiên (Home)
        setSelectedItem(R.id.nav_item_home);
    }

    private void setupNavigationItems() {
        // Home
        LinearLayout navHome = findViewById(R.id.nav_item_home);
        navHome.setOnClickListener(v -> navigateToFragment(R.id.nav_item_home, new HomeFragment()));

        // Search/Friends
        LinearLayout navSearch = findViewById(R.id.nav_item_search);
        navSearch.setOnClickListener(v -> navigateToFragment(R.id.nav_item_search, new SearchFragment()));

        // Roadmap
        LinearLayout navExplore = findViewById(R.id.nav_item_explore);
        navExplore.setOnClickListener(v -> navigateToFragment(R.id.nav_item_explore, new RoadMapFragment()));

        // Chat AI
        LinearLayout navChat = findViewById(R.id.nav_item_chat);
        navChat.setOnClickListener(v -> navigateToFragment(R.id.nav_item_chat, new ChatAiFragment()));

        // Profile
        LinearLayout navProfile = findViewById(R.id.nav_item_profile);
        navProfile.setOnClickListener(v -> navigateToFragment(R.id.nav_item_profile, new ProfileFragment()));

        // New Screen
        LinearLayout navNewScreen = findViewById(R.id.nav_item_new_screen);
        navNewScreen.setOnClickListener(v -> navigateToFragment(R.id.nav_item_new_screen, new NewScreenFragment()));

        // Vocab Match
        LinearLayout navVocabMatch = findViewById(R.id.nav_item_vocab_match);
        navVocabMatch.setOnClickListener(v -> navigateToFragment(R.id.nav_item_vocab_match, new VocabMatchFragment()));
    }

    private void navigateToFragment(int itemId, Fragment fragment) {
        // Lưu lại item được chọn trước đó
        if (currentSelectedItemId != itemId) {
            previousSelectedItemId = currentSelectedItemId;
        }
        
        // Cập nhật selected item
        setSelectedItem(itemId);
        currentSelectedItemId = itemId;

        // Chuyển fragment
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    private void setSelectedItem(int itemId) {
        // Reset tất cả items về trạng thái không được chọn
        resetAllItems();

        // Set item được chọn
        LinearLayout selectedItem = findViewById(itemId);
        if (selectedItem != null) {
            ImageView icon = null;
            TextView text = null;

            if (itemId == R.id.nav_item_home) {
                icon = findViewById(R.id.nav_icon_home);
                text = findViewById(R.id.nav_text_home);
            } else if (itemId == R.id.nav_item_search) {
                icon = findViewById(R.id.nav_icon_search);
                text = findViewById(R.id.nav_text_search);
            } else if (itemId == R.id.nav_item_explore) {
                icon = findViewById(R.id.nav_icon_explore);
                text = findViewById(R.id.nav_text_explore);
            } else if (itemId == R.id.nav_item_chat) {
                icon = findViewById(R.id.nav_icon_chat);
                text = findViewById(R.id.nav_text_chat);
            } else if (itemId == R.id.nav_item_profile) {
                icon = findViewById(R.id.nav_icon_profile);
                text = findViewById(R.id.nav_text_profile);
            } else if (itemId == R.id.nav_item_new_screen) {
                icon = findViewById(R.id.nav_icon_new_screen);
                text = findViewById(R.id.nav_text_new_screen);
            } else if (itemId == R.id.nav_item_vocab_match) {
                icon = findViewById(R.id.nav_icon_vocab_match);
                text = findViewById(R.id.nav_text_vocab_match);
            }

            if (icon != null) {
                icon.setColorFilter(ContextCompat.getColor(this, R.color.cyan_light));
            }
            if (text != null) {
                text.setTextColor(ContextCompat.getColor(this, R.color.cyan_light));
            }
        }
    }

    private void resetAllItems() {
        // Reset Home
        ImageView iconHome = findViewById(R.id.nav_icon_home);
        TextView textHome = findViewById(R.id.nav_text_home);
        if (iconHome != null) iconHome.setColorFilter(Color.WHITE);
        if (textHome != null) textHome.setTextColor(Color.WHITE);

        // Reset Search
        ImageView iconSearch = findViewById(R.id.nav_icon_search);
        TextView textSearch = findViewById(R.id.nav_text_search);
        if (iconSearch != null) iconSearch.setColorFilter(Color.WHITE);
        if (textSearch != null) textSearch.setTextColor(Color.WHITE);

        // Reset Explore
        ImageView iconExplore = findViewById(R.id.nav_icon_explore);
        TextView textExplore = findViewById(R.id.nav_text_explore);
        if (iconExplore != null) iconExplore.setColorFilter(Color.WHITE);
        if (textExplore != null) textExplore.setTextColor(Color.WHITE);

        // Reset Chat
        ImageView iconChat = findViewById(R.id.nav_icon_chat);
        TextView textChat = findViewById(R.id.nav_text_chat);
        if (iconChat != null) iconChat.setColorFilter(Color.WHITE);
        if (textChat != null) textChat.setTextColor(Color.WHITE);

        // Reset Profile
        ImageView iconProfile = findViewById(R.id.nav_icon_profile);
        TextView textProfile = findViewById(R.id.nav_text_profile);
        if (iconProfile != null) iconProfile.setColorFilter(Color.WHITE);
        if (textProfile != null) textProfile.setTextColor(Color.WHITE);

        // Reset New Screen
        ImageView iconNewScreen = findViewById(R.id.nav_icon_new_screen);
        TextView textNewScreen = findViewById(R.id.nav_text_new_screen);
        if (iconNewScreen != null) iconNewScreen.setColorFilter(Color.WHITE);
        if (textNewScreen != null) textNewScreen.setTextColor(Color.WHITE);

        // Reset Vocab Match
        ImageView iconVocabMatch = findViewById(R.id.nav_icon_vocab_match);
        TextView textVocabMatch = findViewById(R.id.nav_text_vocab_match);
        if (iconVocabMatch != null) iconVocabMatch.setColorFilter(Color.WHITE);
        if (textVocabMatch != null) textVocabMatch.setTextColor(Color.WHITE);
    }

    // Method để quay lại fragment trước đó
    public void navigateToPreviousFragment() {
        if (bottomNav != null && previousSelectedItemId != 0) {
            Fragment fragment = null;
            if (previousSelectedItemId == R.id.nav_item_home) {
                fragment = new HomeFragment();
            } else if (previousSelectedItemId == R.id.nav_item_search) {
                fragment = new SearchFragment();
            } else if (previousSelectedItemId == R.id.nav_item_explore) {
                fragment = new RoadMapFragment();
            } else if (previousSelectedItemId == R.id.nav_item_chat) {
                fragment = new ChatAiFragment();
            } else if (previousSelectedItemId == R.id.nav_item_profile) {
                fragment = new ProfileFragment();
            } else if (previousSelectedItemId == R.id.nav_item_new_screen) {
                fragment = new NewScreenFragment();
            } else if (previousSelectedItemId == R.id.nav_item_vocab_match) {
                fragment = new VocabMatchFragment();
            }
            
            if (fragment != null) {
                navigateToFragment(previousSelectedItemId, fragment);
            }
        }
    }

    // Getter để lấy previous fragment ID
    public int getPreviousSelectedItemId() {
        return previousSelectedItemId;
    }

    // Method để set selected item (thay thế cho getBottomNavigationView().setSelectedItemId())
    public void setSelectedNavItem(int itemId) {
        Fragment fragment = null;
        
        if (itemId == R.id.nav_item_home || itemId == R.id.nav_home) {
            fragment = new HomeFragment();
            itemId = R.id.nav_item_home;
        } else if (itemId == R.id.nav_item_search || itemId == R.id.nav_search) {
            fragment = new SearchFragment();
            itemId = R.id.nav_item_search;
        } else if (itemId == R.id.nav_item_explore || itemId == R.id.nav_explore) {
            fragment = new RoadMapFragment();
            itemId = R.id.nav_item_explore;
        } else if (itemId == R.id.nav_item_chat || itemId == R.id.nav_chat) {
            fragment = new ChatAiFragment();
            itemId = R.id.nav_item_chat;
        } else if (itemId == R.id.nav_item_profile || itemId == R.id.nav_profile) {
            fragment = new ProfileFragment();
            itemId = R.id.nav_item_profile;
        } else if (itemId == R.id.nav_item_new_screen || itemId == R.id.nav_new_screen) {
            fragment = new NewScreenFragment();
            itemId = R.id.nav_item_new_screen;
        } else if (itemId == R.id.nav_item_vocab_match) {
            fragment = new VocabMatchFragment();
            itemId = R.id.nav_item_vocab_match;
        }
        
        if (fragment != null) {
            navigateToFragment(itemId, fragment);
        }
    }
}