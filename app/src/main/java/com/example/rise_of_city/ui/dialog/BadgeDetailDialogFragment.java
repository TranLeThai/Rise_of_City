package com.example.rise_of_city.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.user.Badge;

public class BadgeDetailDialogFragment extends DialogFragment {
    private static final String ARG_BADGE = "badge";
    private static final String ARG_CURRENT_VALUE = "currentValue";
    
    private Badge badge;
    private int currentValue;
    
    public static BadgeDetailDialogFragment newInstance(Badge badge, int currentValue) {
        BadgeDetailDialogFragment fragment = new BadgeDetailDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BADGE, badge);
        args.putInt(ARG_CURRENT_VALUE, currentValue);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Translucent_NoTitleBar);
        
        if (getArguments() != null) {
            badge = (Badge) getArguments().getSerializable(ARG_BADGE);
            currentValue = getArguments().getInt(ARG_CURRENT_VALUE, 0);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_badge_detail, container, false);
        
        if (badge == null) {
            dismiss();
            return view;
        }
        
        ImageView ivBadge = view.findViewById(R.id.ivBadge);
        TextView tvBadgeName = view.findViewById(R.id.tvBadgeName);
        TextView tvBadgeDescription = view.findViewById(R.id.tvBadgeDescription);
        TextView tvProgress = view.findViewById(R.id.tvProgress);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView tvUnlockCondition = view.findViewById(R.id.tvUnlockCondition);
        Button btnClose = view.findViewById(R.id.btnClose);
        android.widget.ImageButton btnCloseX = view.findViewById(R.id.btnCloseX);
        
        // Set badge icon
        if (badge.isUnlocked()) {
            ivBadge.setImageResource(badge.getIconResId());
            ivBadge.setAlpha(1.0f);
            ivBadge.clearColorFilter();
        } else {
            ivBadge.setImageResource(R.drawable.badge_locked);
            ivBadge.setAlpha(0.6f);
            ivBadge.clearColorFilter();
        }
        
        // Set badge info
        tvBadgeName.setText(badge.getName());
        tvBadgeDescription.setText(badge.getDescription());
        
        // Set progress
        if (badge.isUnlocked()) {
            tvProgress.setText("Đã đạt được!");
            progressBar.setProgress(100);
            tvUnlockCondition.setVisibility(View.GONE);
        } else {
            String progressText = String.format("%d%% (%d/%d)", 
                badge.getProgress(), 
                currentValue, 
                badge.getTargetValue());
            tvProgress.setText(progressText);
            progressBar.setProgress(badge.getProgress());
            
            // Show unlock condition
            String condition = getUnlockCondition(badge);
            tvUnlockCondition.setText(condition);
            tvUnlockCondition.setVisibility(View.VISIBLE);
        }
        
        btnClose.setOnClickListener(v -> dismiss());
        btnCloseX.setOnClickListener(v -> dismiss());
        
        return view;
    }
    
    private String getUnlockCondition(Badge badge) {
        if (badge.getType() == null) return "";
        
        switch (badge.getType()) {
            case BEGINNER:
            case FIRST_LESSON:
                return "Điều kiện: " + badge.getDescription();
            case STREAK_3:
                return "Điều kiện: Học liên tiếp 3 ngày\nHiện tại: " + currentValue + " ngày";
            case STREAK_7:
                return "Điều kiện: Học liên tiếp 7 ngày\nHiện tại: " + currentValue + " ngày";
            case STREAK_30:
                return "Điều kiện: Học liên tiếp 30 ngày\nHiện tại: " + currentValue + " ngày";
            case XP_100:
                return "Điều kiện: Đạt 100 điểm kinh nghiệm\nHiện tại: " + currentValue + " XP";
            case XP_500:
                return "Điều kiện: Đạt 500 điểm kinh nghiệm\nHiện tại: " + currentValue + " XP";
            case XP_1000:
                return "Điều kiện: Đạt 1000 điểm kinh nghiệm\nHiện tại: " + currentValue + " XP";
            case XP_5000:
                return "Điều kiện: Đạt 5000 điểm kinh nghiệm\nHiện tại: " + currentValue + " XP";
            case SCHOOL_COMPLETE:
                return "Điều kiện: Hoàn thành tất cả bài học tại School";
            case COFFEE_COMPLETE:
                return "Điều kiện: Hoàn thành tất cả bài học tại Coffee Shop";
            case PARK_COMPLETE:
                return "Điều kiện: Hoàn thành tất cả bài học tại Park";
            case HOUSE_COMPLETE:
                return "Điều kiện: Hoàn thành tất cả bài học tại House";
            case LIBRARY_COMPLETE:
                return "Điều kiện: Hoàn thành tất cả bài học tại Library";
            case PERFECT_SCORE:
            case VOCAB_MASTER:
            case GRAMMAR_EXPERT:
            case SPEAKING_CHAMPION:
                return "Điều kiện: " + badge.getDescription();
            case MASTER:
                return "Điều kiện: Đạt trình độ Advanced trong khảo sát";
            case LEGEND:
                return "Điều kiện: Hoàn thành tất cả nội dung trong game";
            default:
                return "Điều kiện: " + badge.getDescription();
        }
    }
}

