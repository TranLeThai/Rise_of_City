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
import com.example.rise_of_city.data.model.game.Building;
import com.example.rise_of_city.data.repository.GoldRepository;

public class LockAreaDialogFragment extends DialogFragment {

    private String lessonName = "Thì hiện tại đơn";
    private Building building; // Building cần unlock
    private OnLearnNowClickListener onLearnNowClickListener;
    private OnCloseClickListener onCloseClickListener;
    private OnUnlockWithGoldClickListener onUnlockWithGoldClickListener;

    public interface OnLearnNowClickListener {
        void onLearnNowClick();
    }

    public interface OnCloseClickListener {
        void onCloseClick();
    }
    
    public interface OnUnlockWithGoldClickListener {
        void onUnlockWithGoldClick(Building building);
    }

    public static LockAreaDialogFragment newInstance(String lessonName) {
        return newInstance(lessonName, null);
    }
    
    public static LockAreaDialogFragment newInstance(String lessonName, Building building) {
        LockAreaDialogFragment fragment = new LockAreaDialogFragment();
        Bundle args = new Bundle();
        args.putString("lessonName", lessonName);
        args.putSerializable("building", building);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            lessonName = getArguments().getString("lessonName", "Thì hiện tại đơn");
            building = (Building) getArguments().getSerializable("building");
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
        Button btnUnlockWithGold = view.findViewById(R.id.btn_unlock_with_gold);
        Button btnLearnNow = view.findViewById(R.id.btn_learn_now);
        TextView tvClose = view.findViewById(R.id.tv_close);

        // Set description với lesson name
        String description = "Hoàn thành bài học '" + lessonName + "' để mở khóa cửa hàng này.\n\nHoặc mở khóa ngay bằng vàng!";
        tvDescription.setText(description);

        // Kiểm tra vàng hiện tại và cập nhật button
        final int UNLOCK_COST = 50; // Chi phí mở khóa
        GoldRepository goldRepo = GoldRepository.getInstance();
        if (getContext() != null) {
            goldRepo.getCurrentGold(getContext(), currentGold -> {
                if (currentGold >= UNLOCK_COST) {
                    btnUnlockWithGold.setText("Mở Khóa (" + UNLOCK_COST + " Vàng)");
                    btnUnlockWithGold.setEnabled(true);
                } else {
                    btnUnlockWithGold.setText("Không đủ vàng! (Cần " + UNLOCK_COST + " Vàng)");
                    btnUnlockWithGold.setEnabled(false);
                    btnUnlockWithGold.setAlpha(0.6f);
                }
            });
        }

        // Button Mở Khóa Bằng Vàng
        btnUnlockWithGold.setOnClickListener(v -> {
            if (building != null && onUnlockWithGoldClickListener != null && getContext() != null) {
                goldRepo.checkCanUnlockBuilding(getContext(), UNLOCK_COST, (canUnlock, currentGold, message) -> {
                    if (canUnlock) {
                        goldRepo.spendGold(getContext(), UNLOCK_COST, new GoldRepository.OnGoldUpdatedListener() {
                            @Override
                            public void onGoldUpdated(int newGold) {
                                onUnlockWithGoldClickListener.onUnlockWithGoldClick(building);
                                dismiss();
                            }
                            
                            @Override
                            public void onError(String error) {
                                android.widget.Toast.makeText(getContext(), error, android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

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
    
    public void setOnUnlockWithGoldClickListener(OnUnlockWithGoldClickListener listener) {
        this.onUnlockWithGoldClickListener = listener;
    }
}

