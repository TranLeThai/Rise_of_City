// BuildingDetailFragment.java (New file based on previous suggestion)
package com.example.rise_of_city.ui.game.building;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.game.Building;
import com.example.rise_of_city.ui.lesson.LessonActivity;  // Import LessonActivity

public class BuildingDetailFragment extends Fragment {

    private Building building;
    private OnUpgradeClickListener upgradeListener;

    public static BuildingDetailFragment newInstance(Building building) {
        BuildingDetailFragment fragment = new BuildingDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("building", building);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            building = (Building) getArguments().getSerializable("building");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_building_detail, container, false);

        // Ánh xạ views
        ImageView ivBuilding = view.findViewById(R.id.iv_building);
        TextView tvBuildingName = view.findViewById(R.id.tv_building_name);
        TextView tvLevel = view.findViewById(R.id.tv_level);
        Button btnStudy = view.findViewById(R.id.btnStudy);
        Button btnReview = view.findViewById(R.id.btnReview);  // Button mới cho Ôn bài
        Button btnUpgrade = view.findViewById(R.id.btnUpgrade);

        // Hiển thị info building
        if (building != null) {
            tvBuildingName.setText(building.getName());
            tvLevel.setText("Level: " + building.getLevel());
            // Load image cho building (ví dụ: set src dựa trên building ID)
            if ("house".equals(building.getId())) {
                ivBuilding.setImageResource(R.drawable.vector_house);
            }
            // ... (tương tự cho các building khác)
        }

        // Sự kiện Học bài (mode STUDY_NEW)
        btnStudy.setOnClickListener(v -> {
            startLessonActivity("STUDY_NEW");
        });

        // Sự kiện Ôn bài (mode REVIEW)
        btnReview.setOnClickListener(v -> {
            startLessonActivity("REVIEW");
        });

        // Sự kiện Nâng cấp (gọi listener từ InGameActivity)
        btnUpgrade.setOnClickListener(v -> {
            if (upgradeListener != null) {
                upgradeListener.onUpgradeClicked(building);
            }
        });

        return view;
    }

    private void startLessonActivity(String mode) {
        if (building == null) return;
        Intent intent = new Intent(getActivity(), LessonActivity.class);
        intent.putExtra("lessonName", building.getRequiredLessonName());  // Tên lesson, ví dụ "House Vocabulary"
        intent.putExtra("mode", mode);  // STUDY_NEW hoặc REVIEW
        intent.putExtra("building_id", building.getId());  // Để load lesson cụ thể
        startActivity(intent);
    }

    public void setOnUpgradeClickListener(OnUpgradeClickListener listener) {
        this.upgradeListener = listener;
    }

    public interface OnUpgradeClickListener {
        void onUpgradeClicked(Building building);
    }
}