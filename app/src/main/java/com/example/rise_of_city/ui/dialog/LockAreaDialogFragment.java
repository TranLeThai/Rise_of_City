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

public class LockAreaDialogFragment extends DialogFragment {

    private String lessonName = "Thì hiện tại đơn";
    private OnLearnNowClickListener onLearnNowClickListener;
    private OnCloseClickListener onCloseClickListener;

    public interface OnLearnNowClickListener {
        void onLearnNowClick();
    }

    public interface OnCloseClickListener {
        void onCloseClick();
    }

    public static LockAreaDialogFragment newInstance(String lessonName) {
        LockAreaDialogFragment fragment = new LockAreaDialogFragment();
        Bundle args = new Bundle();
        args.putString("lessonName", lessonName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            lessonName = getArguments().getString("lessonName", "Thì hiện tại đơn");
        }
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
        return inflater.inflate(R.layout.dialog_lock_area, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvDescription = view.findViewById(R.id.tv_description);
        Button btnLearnNow = view.findViewById(R.id.btn_learn_now);
        TextView tvClose = view.findViewById(R.id.tv_close);

        // Set description với lesson name
        String description = "Hoàn thành bài học '" + lessonName + "' để mở khóa cửa hàng này.";
        tvDescription.setText(description);

        // Button Học Ngay
        btnLearnNow.setOnClickListener(v -> {
            if (onLearnNowClickListener != null) {
                onLearnNowClickListener.onLearnNowClick();
            }
            dismiss();
        });

        // Button Đóng
        tvClose.setOnClickListener(v -> {
            if (onCloseClickListener != null) {
                onCloseClickListener.onCloseClick();
            }
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            getDialog().getWindow().setLayout(width, height);
            // Thêm shadow/elevation cho dialog
            getDialog().getWindow().setElevation(8f);
        }
    }

    public void setOnLearnNowClickListener(OnLearnNowClickListener listener) {
        this.onLearnNowClickListener = listener;
    }

    public void setOnCloseClickListener(OnCloseClickListener listener) {
        this.onCloseClickListener = listener;
    }
}

