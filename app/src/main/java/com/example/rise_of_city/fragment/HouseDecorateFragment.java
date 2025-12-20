package com.example.rise_of_city.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;

/**
 * Fragment cho màn hình House Decorate
 * Hiển thị câu và cho phép trang trí phòng
 */
public class HouseDecorateFragment extends Fragment {

    private TextView tvTitle;
    private TextView tvSubtitle;
    private TextView tvSentence;
    private ImageButton btnSettings;
    private ImageButton btnSpeaker;
    private ImageView ivRoom;
    private ImageView ivFurniture1, ivFurniture2, ivFurniture3, ivFurniture4;
    private ImageButton btnArrowLeft, btnArrowRight;
    
    private boolean isSoundMuted = false;
    private int currentRoomIndex = 0;
    
    // Danh sách các phòng
    private int[] roomImages = {
        R.drawable.bedroom_full,      // Bedroom
        R.drawable.livingroom_full,   // Livingroom
        R.drawable.kitchen_full       // Kitchen
    };
    
    // Danh sách các vật dụng
    private int[] furnitureImages = {
        R.drawable.bedroom_object,    // Bed
        R.drawable.sofa1,             // Sofa
        R.drawable.chair,             // Chair
        R.drawable.table              // Table
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_house_decorate, container, false);
        
        initViews(view);
        setupSentence();
        setupRoom();
        setupFurniture();
        
        return view;
    }
    
    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tv_title);
        tvSubtitle = view.findViewById(R.id.tv_subtitle);
        tvSentence = view.findViewById(R.id.tv_sentence);
        btnSettings = view.findViewById(R.id.btn_settings);
        btnSpeaker = view.findViewById(R.id.btn_speaker);
        ivRoom = view.findViewById(R.id.iv_room);
        ivFurniture1 = view.findViewById(R.id.iv_furniture1);
        ivFurniture2 = view.findViewById(R.id.iv_furniture2);
        ivFurniture3 = view.findViewById(R.id.iv_furniture3);
        ivFurniture4 = view.findViewById(R.id.iv_furniture4);
        btnArrowLeft = view.findViewById(R.id.btn_arrow_left);
        btnArrowRight = view.findViewById(R.id.btn_arrow_right);
        
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Settings", Toast.LENGTH_SHORT).show();
            });
        }
        
        if (btnSpeaker != null) {
            btnSpeaker.setOnClickListener(v -> {
                isSoundMuted = !isSoundMuted;
                // Update icon based on mute state
                if (isSoundMuted) {
                    btnSpeaker.setImageResource(android.R.drawable.ic_lock_silent_mode);
                } else {
                    btnSpeaker.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
                }
            });
        }
        
        if (btnArrowLeft != null) {
            btnArrowLeft.setOnClickListener(v -> {
                showPreviousRoom();
            });
        }
        
        if (btnArrowRight != null) {
            btnArrowRight.setOnClickListener(v -> {
                showNextRoom();
            });
        }
    }
    
    private void setupRoom() {
        if (ivRoom != null && roomImages.length > 0) {
            try {
                ivRoom.setImageResource(roomImages[currentRoomIndex]);
            } catch (Exception e) {
                // Fallback nếu không tìm thấy hình ảnh
                ivRoom.setBackgroundColor(0xFFE8EAF6);
            }
        }
    }
    
    private void showPreviousRoom() {
        if (roomImages.length > 0) {
            currentRoomIndex = (currentRoomIndex - 1 + roomImages.length) % roomImages.length;
            setupRoom();
        }
    }
    
    private void showNextRoom() {
        if (roomImages.length > 0) {
            currentRoomIndex = (currentRoomIndex + 1) % roomImages.length;
            setupRoom();
        }
    }
    
    private void setupSentence() {
        String sentence = "The bed is next to the window";
        SpannableString spannable = new SpannableString(sentence);
        
        // Highlight "is" in green
        int isStart = sentence.indexOf("is");
        int isEnd = isStart + "is".length();
        if (isStart != -1) {
            spannable.setSpan(
                new ForegroundColorSpan(Color.GREEN),
                isStart,
                isEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        
        tvSentence.setText(spannable);
    }
    
    private void setupFurniture() {
        // Load hình ảnh furniture từ drawable
        try {
            if (ivFurniture1 != null && furnitureImages.length > 0) {
                ivFurniture1.setImageResource(furnitureImages[0]); // Bed
                ivFurniture1.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "Bed selected", Toast.LENGTH_SHORT).show();
                });
            }
            
            if (ivFurniture2 != null && furnitureImages.length > 1) {
                ivFurniture2.setImageResource(furnitureImages[1]); // Sofa
                ivFurniture2.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "Sofa selected", Toast.LENGTH_SHORT).show();
                });
            }
            
            if (ivFurniture3 != null && furnitureImages.length > 2) {
                ivFurniture3.setImageResource(furnitureImages[2]); // Chair
                ivFurniture3.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "Chair selected", Toast.LENGTH_SHORT).show();
                });
            }
            
            if (ivFurniture4 != null && furnitureImages.length > 3) {
                ivFurniture4.setImageResource(furnitureImages[3]); // Table
                ivFurniture4.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "Table selected", Toast.LENGTH_SHORT).show();
                });
            }
        } catch (Exception e) {
            // Fallback nếu không tìm thấy hình ảnh
            Toast.makeText(getContext(), "Error loading furniture images", Toast.LENGTH_SHORT).show();
        }
    }
}

