package com.example.rise_of_city.ui.ingame;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.fragment.RoadMapDialogFragment;

public class MenuDialogFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_menu, container, false);

        // 1. Xử lý nút mở Road Map
        view.findViewById(R.id.btnRoadMap).setOnClickListener(v -> {
            dismiss(); // Đóng menu hiện tại
            showRoadMap(); // Mở màn hình Road Map
        });

        // 2. Xử lý nút Thoát
        view.findViewById(R.id.btnExitScreen).setOnClickListener(v -> {
            dismiss();
            if (getActivity() != null) {
                getActivity().finish(); // Thoát Activity cha
            }
        });

        return view;
    }

    private void showRoadMap() {
        // Giả sử user đang ở level 3 (bạn có thể lấy từ SharePreferences hoặc Database)
        int currentLevel = 3;

        RoadMapDialogFragment roadMapDialog = RoadMapDialogFragment.newInstance(currentLevel);
        roadMapDialog.show(getParentFragmentManager(), "RoadMapDialog");
    }
}