package com.example.rise_of_city.ui.game.roadmap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView; // SỬA LỖI: Đổi import

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.UserBuilding;
import com.example.rise_of_city.ui.game.ingame.InGameActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoadMapFragment extends Fragment {

    private AppDatabase appDatabase;
    private int currentUserId;
    private View view;

    // Maps để liên kết buildingId với resources
    private final Map<String, Integer> buildingViewIds = new HashMap<>();
    private final Map<String, String> buildingNames = new HashMap<>();
    private final Map<String, Integer> buildingIcons = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_road_map, container, false);

        appDatabase = AppDatabase.getInstance(requireContext());

        SharedPreferences prefs = requireContext().getSharedPreferences("RiseOfCity_Prefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getInt("logged_user_id", -1);

        initializeBuildingMaps();
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load lại dữ liệu mỗi khi fragment được hiển thị
        loadUserBuildings();
    }

    private void initializeBuildingMaps() {
        buildingViewIds.put("house", R.id.building_house);
        buildingViewIds.put("farm", R.id.building_farm);
        buildingViewIds.put("park", R.id.building_park);
        buildingViewIds.put("school", R.id.building_school);
        buildingViewIds.put("library", R.id.building_library);
        buildingViewIds.put("coffee_shop", R.id.building_cafe);
        buildingViewIds.put("clothers", R.id.building_clothers);
        buildingViewIds.put("bakery", R.id.building_bakery);

        // SỬA LỖI: Tách các dòng put
        buildingNames.put("house", "Nhà ở");
        buildingNames.put("farm", "Nông trại");
        buildingNames.put("park", "Công viên");
        buildingNames.put("school", "Trường học");
        buildingNames.put("library", "Thư viện");
        buildingNames.put("coffee_shop", "Quán Cà Phê");
        buildingNames.put("clothers", "Shop Quần Áo");
        buildingNames.put("bakery", "Tiệm Bánh");

        buildingIcons.put("house", R.drawable.ic_house);
        buildingIcons.put("farm", R.drawable.ic_launcher_foreground); // Thay bằng icon thật
        buildingIcons.put("park", R.drawable.ic_launcher_foreground);
        buildingIcons.put("school", R.drawable.ic_launcher_foreground);
        buildingIcons.put("library", R.drawable.ic_launcher_foreground);
        buildingIcons.put("coffee_shop", R.drawable.ic_launcher_foreground);
        buildingIcons.put("clothers", R.drawable.ic_launcher_foreground);
        buildingIcons.put("bakery", R.drawable.ic_launcher_foreground);
    }

    private void loadUserBuildings() {
        if (currentUserId != -1) {
            new GetUserBuildingsTask().execute(currentUserId);
        } else {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
        }
    }

    private class GetUserBuildingsTask extends AsyncTask<Integer, Void, List<UserBuilding>> {
        @Override
        protected List<UserBuilding> doInBackground(Integer... userIds) {
            return appDatabase.userBuildingDao().getBuildingsForUser(userIds[0]);
        }

        @Override
        protected void onPostExecute(List<UserBuilding> userBuildings) {
            if (userBuildings != null && !userBuildings.isEmpty()) {
                updateRoadmapUI(userBuildings);
            } else {
                // Có thể hiển thị thông báo lỗi hoặc loading state ở đây
            }
        }
    }

    private void updateRoadmapUI(List<UserBuilding> userBuildings) {
        // Convert list to map for easy lookup
        Map<String, UserBuilding> buildingMap = new HashMap<>();
        for (UserBuilding b : userBuildings) {
            buildingMap.put(b.buildingId, b);
        }

        for (String buildingId : buildingViewIds.keySet()) {
            View buildingItemView = view.findViewById(buildingViewIds.get(buildingId));
            if (buildingItemView == null) continue;

            UserBuilding buildingData = buildingMap.get(buildingId);
            if (buildingData == null) continue;

            // Ánh xạ các view con
            MaterialCardView cardView = buildingItemView.findViewById(R.id.building_card); // SỬA LỖI: dùng đúng kiểu MaterialCardView
            ImageView ivIcon = buildingItemView.findViewById(R.id.iv_building_icon);
            ImageView ivLock = buildingItemView.findViewById(R.id.iv_lock_icon);
            TextView tvName = buildingItemView.findViewById(R.id.tv_building_name);
            TextView tvProgress = buildingItemView.findViewById(R.id.tv_building_progress);

            // Set thông tin cơ bản
            tvName.setText(buildingNames.get(buildingId));
            ivIcon.setImageResource(buildingIcons.get(buildingId));

            // Tính toán và hiển thị tiến độ
            int level = buildingData.level;
            int maxLevel = 3;
            int progressPercent = (int) (((float) level / maxLevel) * 100);
            tvProgress.setText(progressPercent + "% - LV " + level);

            // Logic hiển thị trạng thái (khóa, mở, hoàn thành)
            if (level == maxLevel) {
                // Hoàn thành
                ivLock.setVisibility(View.VISIBLE);
                ivLock.setImageResource(R.drawable.ic_check_circle);
                cardView.setCardBackgroundColor(Color.parseColor("#C8E6C9")); // Màu xanh lá nhạt
                cardView.setStrokeColor(Color.parseColor("#4CAF50")); // Màu xanh lá đậm
                buildingItemView.setOnClickListener(null);
            } else if (level > 0) {
                // Đang tiến hành (đã xây)
                ivLock.setVisibility(View.GONE);
                cardView.setCardBackgroundColor(Color.parseColor("#FFF9C4")); // Màu vàng nhạt
                cardView.setStrokeColor(Color.parseColor("#FBC02D")); // Màu vàng đậm
                setBuildingClickListener(buildingItemView, buildingId);
            } else {
                // Bị khóa (level 0)
                ivLock.setVisibility(View.VISIBLE);
                ivLock.setImageResource(R.drawable.ic_lock);
                cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5")); // Màu xám nhạt
                cardView.setStrokeColor(Color.parseColor("#BDBDBD")); // Màu xám
                 buildingItemView.setOnClickListener(v -> Toast.makeText(getContext(), "Hãy hoàn thành các cấp độ trước!", Toast.LENGTH_SHORT).show());

                // TODO: Thêm logic phức tạp hơn, ví dụ nhà đầu tiên luôn được mở
                if(buildingId.equals("house")){
                     ivLock.setVisibility(View.GONE);
                     setBuildingClickListener(buildingItemView, buildingId);
                }
            }
        }
    }

    private void setBuildingClickListener(View view, String buildingId) {
        view.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), InGameActivity.class);
            intent.putExtra("building_id", buildingId);
            startActivity(intent);
        });
    }
}