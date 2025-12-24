package com.example.rise_of_city.ui.game.building;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;

/**
 * Fragment cho màn hình Bakery, Coffee
 * Hiển thị hình ảnh bakery và các từ vựng liên quan
 */
public class BakeryCoffeeFragment extends Fragment {

    private TextView tvTitle;
    private ImageView ivBakeryImage;
    private ImageButton btnSettings;
    private Button btnNext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bakery_coffee, container, false);
        
        initViews(view);
        setupImage();
        
        return view;
    }
    
    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tv_title);
        ivBakeryImage = view.findViewById(R.id.iv_bakery_image);
        btnSettings = view.findViewById(R.id.btn_settings);
        btnNext = view.findViewById(R.id.btn_next);
        
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Settings", Toast.LENGTH_SHORT).show();
            });
        }
        
        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Next", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void setupImage() {
        // Load hình ảnh bakery/kitchen với chef từ drawable
        // Sử dụng bakerchef.png (hình ảnh kitchen với chef)
        try {
            ivBakeryImage.setImageResource(R.drawable.bakerchef);
        } catch (Exception e) {
            // Fallback nếu không tìm thấy hình ảnh
            try {
                ivBakeryImage.setImageResource(R.drawable.kitchen_full);
            } catch (Exception e2) {
                try {
                    ivBakeryImage.setImageResource(R.drawable.kitchen);
                } catch (Exception e3) {
                    ivBakeryImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
        }
        
        // Thêm click listener cho chef image để mở chat dialog
        ivBakeryImage.setOnClickListener(v -> {
            showChefChatDialog();
        });
    }
    
    private void showChefChatDialog() {
        ChefChatDialogFragment dialog = new ChefChatDialogFragment();
        dialog.show(getParentFragmentManager(), "ChefChatDialog");
    }
}

