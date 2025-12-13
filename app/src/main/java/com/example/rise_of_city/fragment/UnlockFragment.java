package com.example.rise_of_city.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.Building;

public class UnlockFragment extends Fragment {

    private Building building;
    private TextView tvBuildingName;
    private TextView tvLevel;
    private TextView tvProduction;
    private ImageView ivBuilding;
    private Button btnHarvest;
    private Button btnUpgrade;

    // Interface cho callbacks
    public interface OnHarvestClickListener {
        void onHarvestClick(Building building);
    }

    public interface OnUpgradeClickListener {
        void onUpgradeClick(Building building);
    }

    private OnHarvestClickListener onHarvestClickListener;
    private OnUpgradeClickListener onUpgradeClickListener;

    public static UnlockFragment newInstance(Building building) {
        UnlockFragment fragment = new UnlockFragment();
        Bundle args = new Bundle();
        args.putSerializable("building", building);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unlock, container, false);

        // Get building from arguments
        if (getArguments() != null) {
            building = (Building) getArguments().getSerializable("building");
        }

        // Default building nếu không có
        if (building == null) {
            building = new Building("library", "Thư Viện", 5, 0, 100, false);
        }

        // Initialize views
        ImageButton btnClose = view.findViewById(R.id.btn_close);
        tvBuildingName = view.findViewById(R.id.tv_building_name);
        tvLevel = view.findViewById(R.id.tv_level);
        tvProduction = view.findViewById(R.id.tv_production);
        ivBuilding = view.findViewById(R.id.iv_building);
        btnHarvest = view.findViewById(R.id.btn_harvest);
        btnUpgrade = view.findViewById(R.id.btn_upgrade);
        
        // Button X (Close) - đóng fragment
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                // Đóng fragment và quay lại màn hình game
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        // Set building data
        updateBuildingInfo();

        // Button listeners
        btnHarvest.setOnClickListener(v -> {
            if (onHarvestClickListener != null) {
                onHarvestClickListener.onHarvestClick(building);
            } else {
                Toast.makeText(getContext(), "Thu hoạch: " + building.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        btnUpgrade.setOnClickListener(v -> {
            if (onUpgradeClickListener != null) {
                onUpgradeClickListener.onUpgradeClick(building);
            } else {
                Toast.makeText(getContext(), "Nâng cấp: " + building.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void updateBuildingInfo() {
        if (building != null) {
            tvBuildingName.setText(building.getName());
            tvLevel.setText("LV. " + building.getLevel());
            
            // Tính toán production dựa trên level (ví dụ: level * 10)
            int productionRate = building.getLevel() * 10;
            tvProduction.setText("Sản xuất: " + productionRate + " Kinh nghiệm/giờ");

            // Set building image dựa trên building ID
            int imageResource = getBuildingImageResource(building.getId());
            if (imageResource != 0) {
                ivBuilding.setImageResource(imageResource);
            }
        }
    }

    private int getBuildingImageResource(String buildingId) {
        // Map building ID to drawable resource
        switch (buildingId.toLowerCase()) {
            case "library":
            case "thư viện":
                return R.drawable.vector_library;
            case "school":
            case "trường học":
                return R.drawable.vector_school;
            case "coffee":
            case "cà phê":
                return R.drawable.vector_coffee;
            case "bakery":
            case "tiệm bánh":
                return R.drawable.vector_bakery;
            case "farmer":
            case "nông dân":
                return R.drawable.vector_farmer;
            case "house":
            case "nhà":
                return R.drawable.vector_house;
            case "park":
            case "công viên":
                return R.drawable.vector_park;
            case "clothers":
            case "quần áo":
                return R.drawable.vector_clothers;
            default:
                return R.drawable.vector_library;
        }
    }

    public void setBuilding(Building building) {
        this.building = building;
        if (getView() != null) {
            updateBuildingInfo();
        }
    }

    public void setOnHarvestClickListener(OnHarvestClickListener listener) {
        this.onHarvestClickListener = listener;
    }

    public void setOnUpgradeClickListener(OnUpgradeClickListener listener) {
        this.onUpgradeClickListener = listener;
    }
}
