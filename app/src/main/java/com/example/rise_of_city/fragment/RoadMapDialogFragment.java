package com.example.rise_of_city.fragment;
import android.app.Dialog;
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
        View line1to2 = view.findViewById(R.id.line_1_to_2);
        View line2to3 = view.findViewById(R.id.line_2_to_3);
        View line3to4 = view.findViewById(R.id.line_3_to_4);

        // Các CardView (Nút tròn)
        MaterialCardView card1 = view.findViewById(R.id.btnStage1);
        MaterialCardView card2 = view.findViewById(R.id.btnStage2);
        MaterialCardView card3 = view.findViewById(R.id.btnStage3);
        MaterialCardView card4 = view.findViewById(R.id.btnStage4);
        MaterialCardView card5 = view.findViewById(R.id.btnStage5);
        MaterialCardView card6 = view.findViewById(R.id.btnStage6);

        // Các dấu tick xanh (Checkmarks) - Giả sử bạn đặt ID là check1, check2... trong XML
        // Ở đây tôi dùng findViewWithTag hoặc logic ẩn hiện dựa trên cha của nó nếu bạn chưa đặt ID
        // Để đơn giản, tôi sẽ xử lý màu sắc của CardView để thể hiện trạng thái

        // --- 2. Logic Hiển thị theo Level ---

        // Màu sắc định nghĩa
        int colorUnlocked = Color.parseColor("#3FD0F1"); // Màu xanh sáng
        int colorLocked = Color.parseColor("#E0E0E0");   // Màu xám
        int colorStrokeUnlocked = Color.parseColor("#B2EBF2");

        // RESET MẶC ĐỊNH: Ẩn hết các đường nối trước khi tính toán
        line1to2.setVisibility(View.INVISIBLE);
        line2to3.setVisibility(View.INVISIBLE);
        line3to4.setVisibility(View.INVISIBLE);

        // --- STAGE 1 (Luôn mở) ---
        unlockStage(card1, true); // Luôn sáng

        // --- STAGE 2 ---
        if (mCurrentLevel >= 2) {
            line1to2.setVisibility(View.VISIBLE); // Hiện đường nối
            unlockStage(card2, true);             // Mở khóa Card 2
        } else {
            lockStage(card2);
        }

        // --- STAGE 3 ---
        if (mCurrentLevel >= 3) {
            line2to3.setVisibility(View.VISIBLE);
            unlockStage(card3, true);
        } else {
            lockStage(card3);
        }

        // --- STAGE 4 (The Park - Special) ---
        if (mCurrentLevel >= 4) {
            line3to4.setVisibility(View.VISIBLE);
            // Stage 4 có thiết kế riêng (nền trắng viền xanh) nên ta xử lý riêng
            card4.setStrokeColor(colorUnlocked);
            card4.setCardBackgroundColor(Color.WHITE);
        } else {
            // Nếu chưa đến level 4, làm nó xám đi
            card4.setStrokeColor(colorLocked);
            card4.setCardBackgroundColor(colorLocked);
        }

        // --- STAGE 5 & 6 (Ví dụ chưa mở) ---
        if (mCurrentLevel >= 5) unlockStage(card5, true); else lockStage(card5);
        if (mCurrentLevel >= 6) unlockStage(card6, true); else lockStage(card6);
    }

    // Hàm phụ trợ: Mở khóa Stage (Đổi màu xanh)
    private void unlockStage(MaterialCardView card, boolean isCompleted) {
        if (card == null) return;
        card.setCardBackgroundColor(Color.parseColor("#3FD0F1")); // Nền xanh
        card.setStrokeColor(Color.parseColor("#B2EBF2"));         // Viền nhạt

        // Bạn có thể tìm ImageView bên trong CardView để đổi icon nếu cần
        // ImageView icon = (ImageView) card.getChildAt(0);
        // icon.setColorFilter(Color.WHITE);
    }

    // Hàm phụ trợ: Khóa Stage (Đổi màu xám)
    private void lockStage(MaterialCardView card) {
        if (card == null) return;
        card.setCardBackgroundColor(Color.parseColor("#E0E0E0")); // Nền xám
        card.setStrokeColor(Color.TRANSPARENT);                   // Không viền

        // Làm mờ icon bên trong
        if (card.getChildCount() > 0 && card.getChildAt(0) instanceof ImageView) {
            ImageView icon = (ImageView) card.getChildAt(0);
            icon.setColorFilter(Color.GRAY); // Icon màu xám
        }
    }
}