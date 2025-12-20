package com.example.rise_of_city.ui.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.ui.settings.SettingsActivity;

/**
 * Dialog hiển thị màn hình Settings khi người dùng bấm dừng/pause
 */
public class PauseSettingsDialogFragment extends DialogFragment {

    public static PauseSettingsDialogFragment newInstance() {
        return new PauseSettingsDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set style để dialog full screen hoặc gần full screen
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_NoTitleBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_pause_settings, container, false);

        Button btnExit = view.findViewById(R.id.btn_exit);
        Button btnSettings = view.findViewById(R.id.btn_settings);
        Button btnContinue = view.findViewById(R.id.btn_continue);

        // Exit button - Thoát khỏi activity hiện tại
        btnExit.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
            dismiss();
        });

        // Settings button - Mở màn hình Settings
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            dismiss();
        });

        // Continue button - Tiếp tục, đóng dialog
        btnContinue.setOnClickListener(v -> {
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set dialog size to almost full screen
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
    }
}
