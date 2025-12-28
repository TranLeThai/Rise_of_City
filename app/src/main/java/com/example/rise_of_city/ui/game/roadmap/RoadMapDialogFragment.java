package com.example.rise_of_city.ui.game.roadmap;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.rise_of_city.R;
import com.google.android.material.card.MaterialCardView;

public class RoadMapDialogFragment extends DialogFragment {

    private static final String ARG_LEVEL = "arg_current_level";
    private int mCurrentLevel = 1; // Mặc định level 1

    // Hàm tạo instance và truyền level vào
    public static RoadMapDialogFragment newInstance(int currentLevel) {
        RoadMapDialogFragment fragment = new RoadMapDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_LEVEL, currentLevel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Style full màn hình, không có title bar
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar_Fullscreen);

        if (getArguments() != null) {
            mCurrentLevel = getArguments().getInt(ARG_LEVEL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_road_map, container, false);

        // Nút đóng (bạn cần thêm nút đóng vào XML hoặc dùng nút back hệ thống)
        // view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());

        setupMapLogic(view);

        return view;
    }

    private void setupMapLogic(View view) {
        // --- 1. Ánh xạ các thành phần ---

        // Các đường nối (Lines)
        View line1 = view.findViewById(R.id.line_1);
        View line2 = view.findViewById(R.id.line_2);
        View line3 = view.findViewById(R.id.line_3);
        View line4 = view.findViewById(R.id.line_4);
        View line5 = view.findViewById(R.id.line_5);
        View line6 = view.findViewById(R.id.line_6);
        View line7 = view.findViewById(R.id.line_7);

        // Các Building (Include Layout)
        View buildingHouse = view.findViewById(R.id.building_house);
        View buildingFarm = view.findViewById(R.id.building_farm);
        View buildingPark = view.findViewById(R.id.building_park);
        View buildingSchool = view.findViewById(R.id.building_school);
        View buildingLibrary = view.findViewById(R.id.building_library);
        View buildingCafe = view.findViewById(R.id.building_cafe);
        View buildingClothers = view.findViewById(R.id.building_clothers);
        View buildingBakery = view.findViewById(R.id.building_bakery);

        // Cấu hình thông tin từng building
        setupBuilding(buildingHouse, "House", R.drawable.vector_house, R.drawable.vector_house_lock, 1, true);
        setupBuilding(buildingFarm, "Farm", R.drawable.vector_farmer, R.drawable.vector_farmer_lock, 2, mCurrentLevel >= 2);
        setupBuilding(buildingPark, "Park", R.drawable.vector_park, R.drawable.vector_park_lock, 3, mCurrentLevel >= 3);
        setupBuilding(buildingSchool, "School", R.drawable.vector_school, R.drawable.vector_school_lock, 4, mCurrentLevel >= 4);
        setupBuilding(buildingLibrary, "Library", R.drawable.vector_library, R.drawable.vector_library_lock, 5, mCurrentLevel >= 5);
        setupBuilding(buildingCafe, "Cafe", R.drawable.vector_coffee, R.drawable.vector_coffee_lock, 6, mCurrentLevel >= 6);
        setupBuilding(buildingClothers, "Clothes", R.drawable.vector_clothers, R.drawable.vector_clothers_lock, 7, mCurrentLevel >= 7);
        setupBuilding(buildingBakery, "Bakery", R.drawable.vector_bakery, R.drawable.vector_bakery_lock, 8, mCurrentLevel >= 8);

        // --- 2. Logic Hiển thị Line theo Level ---

        // Mặc định ẩn hết line nếu chưa đạt level
        // Line 1 nối House (Lv1) -> Farm (Lv2). Hiện khi >= Lv2
        line1.setVisibility(mCurrentLevel >= 2 ? View.VISIBLE : View.INVISIBLE);
        
        // Line 2 nối Farm (Lv2) -> Park (Lv3). Hiện khi >= Lv3
        line2.setVisibility(mCurrentLevel >= 3 ? View.VISIBLE : View.INVISIBLE);

        // Line 3 nối Park (Lv3) -> School (Lv4). Hiện khi >= Lv4
        line3.setVisibility(mCurrentLevel >= 4 ? View.VISIBLE : View.INVISIBLE);

        // Line 4 nối School (Lv4) -> Library (Lv5). Hiện khi >= Lv5
        line4.setVisibility(mCurrentLevel >= 5 ? View.VISIBLE : View.INVISIBLE);

        // Line 5 nối Library (Lv5) -> Cafe (Lv6). Hiện khi >= Lv6
        line5.setVisibility(mCurrentLevel >= 6 ? View.VISIBLE : View.INVISIBLE);

        // Line 6 nối Cafe (Lv6) -> Clothers (Lv7). Hiện khi >= Lv7
        line6.setVisibility(mCurrentLevel >= 7 ? View.VISIBLE : View.INVISIBLE);

        // Line 7 nối Clothers (Lv7) -> Bakery (Lv8). Hiện khi >= Lv8
        line7.setVisibility(mCurrentLevel >= 8 ? View.VISIBLE : View.INVISIBLE);
    }

    private void setupBuilding(View buildingView, String name, int iconResId, int iconLockedResId, int requiredLevel, boolean isUnlocked) {
        if (buildingView == null) return;

        MaterialCardView card = buildingView.findViewById(R.id.building_card);
        ImageView ivIcon = buildingView.findViewById(R.id.iv_building_icon);
        ImageView ivLock = buildingView.findViewById(R.id.iv_lock_icon);
        TextView tvName = buildingView.findViewById(R.id.tv_building_name);
        TextView tvProgress = buildingView.findViewById(R.id.tv_building_progress);

        tvName.setText(name);

        if (isUnlocked) {
            // Đã mở khóa
            card.setCardBackgroundColor(Color.parseColor("#E0E0E0")); // Hoặc màu background mong muốn khi unlock
            card.setStrokeColor(Color.parseColor("#B0B0B0"));
            ivIcon.setImageResource(iconResId);
            ivIcon.setColorFilter(null); // Xóa filter màu nếu có
            ivLock.setVisibility(View.GONE);
            tvName.setTextColor(Color.parseColor("#333333"));
            tvProgress.setVisibility(View.VISIBLE);
            tvProgress.setText("Level " + requiredLevel); // Hoặc hiển thị %
        } else {
            // Bị khóa
            card.setCardBackgroundColor(Color.parseColor("#EEEEEE"));
            card.setStrokeColor(Color.TRANSPARENT);
            // Dùng icon locked hoặc icon thường nhưng làm mờ
             if (iconLockedResId != 0) {
                 ivIcon.setImageResource(iconLockedResId);
             } else {
                 ivIcon.setImageResource(iconResId);
                 ivIcon.setColorFilter(Color.GRAY);
             }
            
            // Nếu muốn hiện ổ khóa đè lên
            // ivLock.setVisibility(View.VISIBLE);
            
            tvName.setTextColor(Color.GRAY);
            tvProgress.setVisibility(View.INVISIBLE);
        }
    }
}