package com.example.rise_of_city.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;

/**
 * New Screen Fragment - Hiển thị danh sách các màn hình mới và cho phép chuyển đổi
 * Màn hình đầu tiên: Contract Inspection (Thanh tra hợp đồng)
 */
public class NewScreenFragment extends Fragment {

    private LinearLayout menuContractInspection;
    private LinearLayout menuQuiz;
    private LinearLayout menuWordMatch;
    private LinearLayout menuImageMatch;
    private LinearLayout menuGuess;
    private LinearLayout menuGuessImage;
    private LinearLayout menuBakeryCoffee;
    private LinearLayout menuHouseDecorate;
    private LinearLayout menuSchool;
    private LinearLayout btnHideMenu;
    private View indicatorContractInspection;
    private View indicatorQuiz;
    private View indicatorWordMatch;
    private View indicatorImageMatch;
    private View indicatorGuess;
    private View indicatorGuessImage;
    private View indicatorBakeryCoffee;
    private View indicatorHouseDecorate;
    private View indicatorSchool;
    private View sidebarMenu;
    private ImageButton btnToggleMenu;
    private FrameLayout fragmentContainer;
    private ConstraintLayout rootLayout;
    
    private String currentScreen = "contract_inspection"; // Mặc định là Contract Inspection
    private boolean isMenuVisible = true; // Menu mặc định hiển thị

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_screen, container, false);
        return view;
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupMenuListeners();
        
        // Hiển thị Contract Inspection làm màn hình đầu tiên
        if (savedInstanceState == null) {
            showScreen("contract_inspection");
        } else {
            // Restore current screen from saved state
            currentScreen = savedInstanceState.getString("current_screen", "contract_inspection");
            isMenuVisible = savedInstanceState.getBoolean("is_menu_visible", true);
            showScreen(currentScreen);
            // Restore menu visibility
            if (!isMenuVisible) {
                // Apply constraint để fragment container chiếm toàn bộ
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(rootLayout);
                constraintSet.connect(R.id.fragment_container_new_screen, ConstraintSet.START, 
                    ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.applyTo(rootLayout);
                
                sidebarMenu.setVisibility(View.GONE);
                btnToggleMenu.setImageResource(android.R.drawable.ic_menu_revert);
            }
        }
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("current_screen", currentScreen);
        outState.putBoolean("is_menu_visible", isMenuVisible);
    }
    
    private void initViews(View view) {
        menuContractInspection = view.findViewById(R.id.menu_contract_inspection);
        menuQuiz = view.findViewById(R.id.menu_quiz);
        menuWordMatch = view.findViewById(R.id.menu_word_match);
        menuImageMatch = view.findViewById(R.id.menu_image_match);
        menuGuess = view.findViewById(R.id.menu_guess);
        menuGuessImage = view.findViewById(R.id.menu_guess_image);
        menuBakeryCoffee = view.findViewById(R.id.menu_bakery_coffee);
        menuHouseDecorate = view.findViewById(R.id.menu_house_decorate);
        menuSchool = view.findViewById(R.id.menu_school);
        btnHideMenu = view.findViewById(R.id.btn_hide_menu);
        ImageButton btnCloseMenu = view.findViewById(R.id.btn_close_menu);
        indicatorContractInspection = view.findViewById(R.id.indicator_contract_inspection);
        indicatorQuiz = view.findViewById(R.id.indicator_quiz);
        indicatorWordMatch = view.findViewById(R.id.indicator_word_match);
        indicatorImageMatch = view.findViewById(R.id.indicator_image_match);
        indicatorGuess = view.findViewById(R.id.indicator_guess);
        indicatorGuessImage = view.findViewById(R.id.indicator_guess_image);
        indicatorBakeryCoffee = view.findViewById(R.id.indicator_bakery_coffee);
        indicatorHouseDecorate = view.findViewById(R.id.indicator_house_decorate);
        indicatorSchool = view.findViewById(R.id.indicator_school);
        sidebarMenu = view.findViewById(R.id.sidebar_menu);
        btnToggleMenu = view.findViewById(R.id.btn_toggle_menu);
        fragmentContainer = view.findViewById(R.id.fragment_container_new_screen);
        rootLayout = (ConstraintLayout) view;
        
        // Nút X để đóng menu
        if (btnCloseMenu != null) {
            btnCloseMenu.setOnClickListener(v -> hideMenuCompletely());
        }
    }
    
    private void setupMenuListeners() {
        menuContractInspection.setOnClickListener(v -> showScreen("contract_inspection"));
        menuQuiz.setOnClickListener(v -> showScreen("quiz"));
        menuWordMatch.setOnClickListener(v -> showScreen("word_match"));
        menuImageMatch.setOnClickListener(v -> showScreen("image_match"));
        menuGuess.setOnClickListener(v -> showScreen("guess"));
        menuGuessImage.setOnClickListener(v -> showScreen("guess_image"));
        menuBakeryCoffee.setOnClickListener(v -> showScreen("bakery_coffee"));
        menuHouseDecorate.setOnClickListener(v -> showScreen("house_decorate"));
        menuSchool.setOnClickListener(v -> showScreen("school"));
        
        // Toggle menu visibility - từ button trong menu
        btnHideMenu.setOnClickListener(v -> hideMenuCompletely());
        
        // Toggle menu visibility - từ button ở góc trên
        btnToggleMenu.setOnClickListener(v -> {
            if (isMenuVisible) {
                hideMenuCompletely();
            } else {
                showMenu();
            }
        });
    }
    
    private void toggleMenu() {
        if (isMenuVisible) {
            hideMenuCompletely();
        } else {
            showMenu();
        }
    }
    
    private void hideMenuCompletely() {
        isMenuVisible = false;
        
        // Ẩn menu hoàn toàn bằng cách set width = 0
        ViewGroup.LayoutParams menuParams = sidebarMenu.getLayoutParams();
        menuParams.width = 0;
        sidebarMenu.setLayoutParams(menuParams);
        sidebarMenu.setVisibility(View.GONE);
        
        // Đặt constraint: fragment container chiếm toàn bộ màn hình
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(rootLayout);
        constraintSet.clear(R.id.fragment_container_new_screen, ConstraintSet.START);
        constraintSet.connect(R.id.fragment_container_new_screen, ConstraintSet.START, 
            ConstraintSet.PARENT_ID, ConstraintSet.START);
        constraintSet.applyTo(rootLayout);
        
        // Đổi icon thành arrow (để hiện menu lại)
        btnToggleMenu.setImageResource(android.R.drawable.ic_menu_revert);
        
        // Force layout update
        if (getView() != null) {
            getView().post(() -> {
                rootLayout.requestLayout();
                rootLayout.invalidate();
            });
        }
    }
    
    private void showMenu() {
        isMenuVisible = true;
        
        // Hiện menu bằng cách restore width
        ViewGroup.LayoutParams menuParams = sidebarMenu.getLayoutParams();
        menuParams.width = (int) (200 * getResources().getDisplayMetrics().density); // 200dp
        sidebarMenu.setLayoutParams(menuParams);
        sidebarMenu.setVisibility(View.VISIBLE);
        
        // Đặt constraint: fragment container bắt đầu sau sidebar
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(rootLayout);
        constraintSet.clear(R.id.fragment_container_new_screen, ConstraintSet.START);
        constraintSet.connect(R.id.fragment_container_new_screen, ConstraintSet.START, 
            R.id.sidebar_menu, ConstraintSet.END);
        constraintSet.applyTo(rootLayout);
        
        // Đổi icon thành menu (để ẩn)
        btnToggleMenu.setImageResource(android.R.drawable.ic_menu_more);
        
        // Force layout update
        if (getView() != null) {
            getView().post(() -> {
                rootLayout.requestLayout();
                rootLayout.invalidate();
            });
        }
    }
    
    private void showScreen(String screenName) {
        currentScreen = screenName;
        
        // Update indicators
        updateIndicators(screenName);
        
        // Replace fragment
        Fragment fragment = null;
        if ("contract_inspection".equals(screenName)) {
            fragment = new ContractInspectionFragment();
        } else if ("quiz".equals(screenName)) {
            fragment = new QuizFragment();
        } else if ("word_match".equals(screenName)) {
            fragment = new MatchingFragment();
        } else if ("image_match".equals(screenName)) {
            fragment = new ImageMatchFragment();
        } else if ("guess".equals(screenName)) {
            fragment = new GuessFragment();
        } else if ("guess_image".equals(screenName)) {
            fragment = new GuessImageFragment();
        } else if ("bakery_coffee".equals(screenName)) {
            fragment = new BakeryCoffeeFragment();
        } else if ("house_decorate".equals(screenName)) {
            fragment = new HouseDecorateFragment();
        } else if ("school".equals(screenName)) {
            fragment = new SchoolFragment();
        }
        
        if (fragment != null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_new_screen, fragment)
                    .commit();
        }
    }
    
    private void updateIndicators(String screenName) {
        // Hide all indicators
        indicatorContractInspection.setVisibility(View.GONE);
        indicatorQuiz.setVisibility(View.GONE);
        indicatorWordMatch.setVisibility(View.GONE);
        indicatorImageMatch.setVisibility(View.GONE);
        indicatorGuess.setVisibility(View.GONE);
        indicatorGuessImage.setVisibility(View.GONE);
        indicatorBakeryCoffee.setVisibility(View.GONE);
        indicatorHouseDecorate.setVisibility(View.GONE);
        indicatorSchool.setVisibility(View.GONE);
        
        // Show selected indicator
        if ("contract_inspection".equals(screenName)) {
            indicatorContractInspection.setVisibility(View.VISIBLE);
        } else if ("quiz".equals(screenName)) {
            indicatorQuiz.setVisibility(View.VISIBLE);
        } else if ("word_match".equals(screenName)) {
            indicatorWordMatch.setVisibility(View.VISIBLE);
        } else if ("image_match".equals(screenName)) {
            indicatorImageMatch.setVisibility(View.VISIBLE);
        } else if ("guess".equals(screenName)) {
            indicatorGuess.setVisibility(View.VISIBLE);
        } else if ("guess_image".equals(screenName)) {
            indicatorGuessImage.setVisibility(View.VISIBLE);
        } else if ("bakery_coffee".equals(screenName)) {
            indicatorBakeryCoffee.setVisibility(View.VISIBLE);
        } else if ("house_decorate".equals(screenName)) {
            indicatorHouseDecorate.setVisibility(View.VISIBLE);
        } else if ("school".equals(screenName)) {
            indicatorSchool.setVisibility(View.VISIBLE);
        }
    }
}
