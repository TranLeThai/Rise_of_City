package com.example.rise_of_city.ui.profile.status;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.repository.BuildingProgressRepository;

import java.util.Map;

public class LevelStatusFragment extends Fragment {

    // School
    private ProgressBar progressSchool;
    private TextView tvProgressSchool;
    
    // Coffee
    private ProgressBar progressCoffee;
    private TextView tvProgressCoffee;
    
    // Park
    private ProgressBar progressPark;
    private TextView tvProgressPark;
    
    // House
    private ProgressBar progressHouse;
    private TextView tvProgressHouse;
    
    private TextView tvSubtitle;
    private BuildingProgressRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_level_status, container, false);

        // Khởi tạo các view
        // Level 1: School
        progressSchool = view.findViewById(R.id.progressLevel1);
        tvProgressSchool = view.findViewById(R.id.tvProgressLevel1);
        
        // Level 2: Coffee
        progressCoffee = view.findViewById(R.id.progressLevel2);
        tvProgressCoffee = view.findViewById(R.id.tvProgressLevel2);
        
        // Level 3: Park
        progressPark = view.findViewById(R.id.progressLevel3);
        tvProgressPark = view.findViewById(R.id.tvProgressLevel3);
        
        // Level 4: House
        progressHouse = view.findViewById(R.id.progressLevel4);
        tvProgressHouse = view.findViewById(R.id.tvProgressLevel4);
        
        tvSubtitle = view.findViewById(R.id.tvSubtitle);
        repository = BuildingProgressRepository.getInstance();

        // Xử lý nút back
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        loadData();

        return view;
    }
    
    private void loadData() {
        // Tải dữ liệu thực tế từ Repository
        repository.getAllBuildingProgress(new BuildingProgressRepository.OnAllBuildingsLoadedListener() {
            @Override
            public void onBuildingsLoaded(Map<String, Map<String, Object>> buildingProgressMap) {
                if (getContext() == null) return;
                
                // Cập nhật từng building
                updateBuildingUI("school", buildingProgressMap, progressSchool, tvProgressSchool);
                updateBuildingUI("coffee", buildingProgressMap, progressCoffee, tvProgressCoffee);
                updateBuildingUI("park", buildingProgressMap, progressPark, tvProgressPark);
                updateBuildingUI("house", buildingProgressMap, progressHouse, tvProgressHouse);
                
                // Tính toán level tổng thể cho Subtitle (ví dụ đơn giản)
                int totalLevels = 0;
                if (buildingProgressMap.containsKey("house")) totalLevels++;
                if (buildingProgressMap.containsKey("school")) totalLevels++;
                if (buildingProgressMap.containsKey("park")) totalLevels++;
                if (buildingProgressMap.containsKey("coffee")) totalLevels++;
                
                String rank = "Newbie";
                if (totalLevels >= 4) rank = "Mayor";
                else if (totalLevels >= 2) rank = "Citizen";
                
                tvSubtitle.setText(rank);
            }
            
            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void updateBuildingUI(String buildingId, Map<String, Map<String, Object>> map, ProgressBar progressBar, TextView textView) {
        if (map.containsKey(buildingId)) {
            Map<String, Object> data = map.get(buildingId);
            Long level = (Long) data.get("level");
            Long currentExp = (Long) data.get("currentExp");
            Long maxExp = (Long) data.get("maxExp");
            
            // Tính % hoàn thành của level hiện tại
            int progress = 0;
            if (maxExp != null && maxExp > 0 && currentExp != null) {
                progress = (int) ((currentExp * 100) / maxExp);
            }
            
            // Có thể logic phức tạp hơn: Nếu level cao, progress = 100% của level trước đó
            // Ở đây hiển thị % EXP của level hiện tại
            
            progressBar.setProgress(progress);
            textView.setText(progress + "%");
        } else {
            progressBar.setProgress(0);
            textView.setText("0%");
        }
    }

    public void setSubtitle(String subtitle) {
        if (tvSubtitle != null) {
            tvSubtitle.setText(subtitle);
        }
    }
}