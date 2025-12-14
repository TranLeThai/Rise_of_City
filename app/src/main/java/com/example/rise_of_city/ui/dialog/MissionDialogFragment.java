package com.example.rise_of_city.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.rise_of_city.R;

public class MissionDialogFragment extends DialogFragment {

    private String missionText = "Mission Text";
    private String missionTitle = "Mission random";
    private OnAcceptClickListener onAcceptClickListener;
    private OnDenyClickListener onDenyClickListener;

    public interface OnAcceptClickListener {
        void onAcceptClick();
    }

    public interface OnDenyClickListener {
        void onDenyClick();
    }

    public static MissionDialogFragment newInstance(String missionTitle, String missionText) {
        MissionDialogFragment fragment = new MissionDialogFragment();
        Bundle args = new Bundle();
        args.putString("missionTitle", missionTitle);
        args.putString("missionText", missionText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            missionTitle = getArguments().getString("missionTitle", "Mission random");
            missionText = getArguments().getString("missionText", "Mission Text");
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
        return inflater.inflate(R.layout.dialog_mission, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        TextView tvMissionTitle = view.findViewById(R.id.tv_mission_title);
        TextView tvMissionText = view.findViewById(R.id.tv_mission_text);
        Button btnAccept = view.findViewById(R.id.btn_accept);
        Button btnDeny = view.findViewById(R.id.btn_deny);

        // Set texts
        tvMissionTitle.setText(missionTitle);
        tvMissionText.setText(missionText);

        // Button Back - Quay lại in game screen
        btnBack.setOnClickListener(v -> {
            dismiss();
        });

        // Button Accept
        btnAccept.setOnClickListener(v -> {
            if (onAcceptClickListener != null) {
                onAcceptClickListener.onAcceptClick();
            }
            dismiss();
        });

        // Button Deny
        btnDeny.setOnClickListener(v -> {
            if (onDenyClickListener != null) {
                onDenyClickListener.onDenyClick();
            }
            dismiss();
        });
    }

    public void setOnAcceptClickListener(OnAcceptClickListener listener) {
        this.onAcceptClickListener = listener;
    }

    public void setOnDenyClickListener(OnDenyClickListener listener) {
        this.onDenyClickListener = listener;
    }
}

