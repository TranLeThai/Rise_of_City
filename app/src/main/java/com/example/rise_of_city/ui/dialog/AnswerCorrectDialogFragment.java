package com.example.rise_of_city.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.rise_of_city.R;

public class AnswerCorrectDialogFragment extends DialogFragment {

    private int experienceReward;
    private int goldReward;
    private String buttonText; // Custom button text

    public interface OnContinueClickListener {
        void onContinueClick();
    }

    private OnContinueClickListener onContinueClickListener;

    public static AnswerCorrectDialogFragment newInstance(int experienceReward) {
        return newInstance(experienceReward, 0);
    }
    
    public static AnswerCorrectDialogFragment newInstance(int experienceReward, int goldReward) {
        return newInstance(experienceReward, goldReward, null);
    }
    
    public static AnswerCorrectDialogFragment newInstance(int experienceReward, int goldReward, String buttonText) {
        AnswerCorrectDialogFragment fragment = new AnswerCorrectDialogFragment();
        Bundle args = new Bundle();
        args.putInt("experienceReward", experienceReward);
        args.putInt("goldReward", goldReward);
        if (buttonText != null) {
            args.putString("buttonText", buttonText);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            experienceReward = getArguments().getInt("experienceReward", 20);
            goldReward = getArguments().getInt("goldReward", 0);
            buttonText = getArguments().getString("buttonText", "TIẾP TỤC");
        } else {
            buttonText = "TIẾP TỤC";
        }
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_answer_correct, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvReward = view.findViewById(R.id.tv_reward);
        Button btnContinue = view.findViewById(R.id.btn_continue);

        // Set reward text - hiển thị cả EXP và vàng
        StringBuilder rewardText = new StringBuilder();
        rewardText.append("+" + experienceReward + " EXP");
        if (goldReward > 0) {
            rewardText.append(" • +" + goldReward + " Vàng");
        }
        tvReward.setText(rewardText.toString());
        
        // Set button text (mặc định là "TIẾP TỤC", có thể đổi thành "NEXT" cho quest)
        btnContinue.setText(buttonText);

        btnContinue.setOnClickListener(v -> {
            if (onContinueClickListener != null) {
                onContinueClickListener.onContinueClick();
            }
            dismiss();
        });
    }

    public void setOnContinueClickListener(OnContinueClickListener listener) {
        this.onContinueClickListener = listener;
    }
}
