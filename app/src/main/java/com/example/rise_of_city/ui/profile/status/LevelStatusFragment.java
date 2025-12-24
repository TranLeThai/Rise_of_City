package com.example.rise_of_city.ui.profile.status;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;

public class LevelStatusFragment extends Fragment {

    private ProgressBar progressLevel1, progressLevel2, progressLevel3, progressLevel4;
    private TextView tvSubtitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_level_status, container, false);

        // Khởi tạo các view
        progressLevel1 = view.findViewById(R.id.progressLevel1);
        progressLevel2 = view.findViewById(R.id.progressLevel2);
        progressLevel3 = view.findViewById(R.id.progressLevel3);
        progressLevel4 = view.findViewById(R.id.progressLevel4);
        tvSubtitle = view.findViewById(R.id.tvSubtitle);

        // Xử lý nút back - quay lại ProfileFragment
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        // Có thể thêm logic để cập nhật progress từ database hoặc ViewModel ở đây
        // Ví dụ: updateProgressFromData();

        return view;
    }

    // Method để cập nhật progress (có thể gọi từ bên ngoài)
    public void updateProgress(int level1, int level2, int level3, int level4) {
        if (progressLevel1 != null) progressLevel1.setProgress(level1);
        if (progressLevel2 != null) progressLevel2.setProgress(level2);
        if (progressLevel3 != null) progressLevel3.setProgress(level3);
        if (progressLevel4 != null) progressLevel4.setProgress(level4);
    }

    // Method để cập nhật subtitle
    public void setSubtitle(String subtitle) {
        if (tvSubtitle != null) {
            tvSubtitle.setText(subtitle);
        }
    }
}

