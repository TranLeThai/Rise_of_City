package com.example.rise_of_city.ui.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.local.UserBuilding;

import java.util.ArrayList;
import java.util.List;

public class UserBuildingAdapter extends RecyclerView.Adapter<UserBuildingAdapter.ViewHolder> {
    
    private List<UserBuilding> buildings;
    private Context context;
    
    public UserBuildingAdapter(Context context) {
        this.context = context;
        this.buildings = new ArrayList<>();
    }
    
    public void setBuildings(List<UserBuilding> buildings) {
        this.buildings = buildings;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_building, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserBuilding building = buildings.get(position);
        
        // Set building name
        String buildingName = getBuildingName(building.buildingId);
        holder.tvBuildingName.setText(buildingName);
        
        // Set building level
        holder.tvBuildingLevel.setText("Lv " + building.level);
        
        // Set building icon
        int iconResId = getBuildingIcon(building.buildingId);
        if (iconResId != 0) {
            holder.ivBuildingIcon.setImageResource(iconResId);
        }
    }
    
    @Override
    public int getItemCount() {
        return buildings.size();
    }
    
    private String getBuildingName(String buildingId) {
        switch (buildingId.toLowerCase()) {
            case "house":
                return "Nhà Ở";
            case "school":
                return "Trường Học";
            case "library":
                return "Thư Viện";
            case "park":
                return "Công Viên";
            case "bakery":
                return "Tiệm Bánh";
            case "farmer":
                return "Nông Trại";
            case "coffee":
                return "Quán Cà Phê";
            case "clothers":
                return "Cửa Hàng Quần Áo";
            default:
                return buildingId;
        }
    }
    
    private int getBuildingIcon(String buildingId) {
        try {
            String iconName = buildingId.toLowerCase() + "_icon";
            return context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
        } catch (Exception e) {
            return 0;
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBuildingIcon;
        TextView tvBuildingName;
        TextView tvBuildingLevel;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBuildingIcon = itemView.findViewById(R.id.iv_building_icon);
            tvBuildingName = itemView.findViewById(R.id.tv_building_name);
            tvBuildingLevel = itemView.findViewById(R.id.tv_building_level);
        }
    }
}

