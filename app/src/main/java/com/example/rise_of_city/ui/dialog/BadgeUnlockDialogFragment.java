package com.example.rise_of_city.ui.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.Badge;

public class BadgeUnlockDialogFragment extends DialogFragment {
    private static final String ARG_BADGE = "badge";
    
    private Badge badge;
    
    public static BadgeUnlockDialogFragment newInstance(Badge badge) {
        BadgeUnlockDialogFragment fragment = new BadgeUnlockDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BADGE, badge);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Translucent_NoTitleBar);
        
        if (getArguments() != null) {
            badge = (Badge) getArguments().getSerializable(ARG_BADGE);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_badge_unlock, container, false);
        
        if (badge == null) {
            dismiss();
            return view;
        }
        
        ImageView ivBadge = view.findViewById(R.id.ivBadge);
        TextView tvBadgeName = view.findViewById(R.id.tvBadgeName);
        TextView tvBadgeDescription = view.findViewById(R.id.tvBadgeDescription);
        Button btnClose = view.findViewById(R.id.btnClose);
        
        // Set badge info
        ivBadge.setImageResource(badge.getIconResId());
        tvBadgeName.setText(badge.getName());
        tvBadgeDescription.setText(badge.getDescription());
        
        // Set color tint if possible
        try {
            int color = android.graphics.Color.parseColor(badge.getColor());
            ivBadge.setColorFilter(color);
        } catch (Exception e) {
            // Use default color
        }
        
        // Animate badge icon
        animateBadgeIcon(ivBadge);
        
        // Close button
        btnClose.setOnClickListener(v -> dismiss());
        
        // Auto dismiss after 3 seconds
        view.postDelayed(() -> {
            if (isAdded() && getDialog() != null && getDialog().isShowing()) {
                dismiss();
            }
        }, 3000);
        
        return view;
    }
    
    private void animateBadgeIcon(ImageView icon) {
        // Scale animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(icon, "scaleX", 0f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(icon, "scaleY", 0f, 1.2f, 1f);
        scaleX.setDuration(600);
        scaleY.setDuration(600);
        scaleX.setInterpolator(new DecelerateInterpolator());
        scaleY.setInterpolator(new DecelerateInterpolator());
        
        // Rotation animation
        ObjectAnimator rotation = ObjectAnimator.ofFloat(icon, "rotation", 0f, 360f);
        rotation.setDuration(800);
        rotation.setInterpolator(new DecelerateInterpolator());
        
        // Start animations
        scaleX.start();
        scaleY.start();
        rotation.start();
        
        // Pulsing effect
        icon.postDelayed(() -> {
            ObjectAnimator pulse = ObjectAnimator.ofFloat(icon, "alpha", 1f, 0.7f, 1f);
            pulse.setDuration(1000);
            pulse.setRepeatCount(ObjectAnimator.INFINITE);
            pulse.start();
        }, 600);
    }
}

