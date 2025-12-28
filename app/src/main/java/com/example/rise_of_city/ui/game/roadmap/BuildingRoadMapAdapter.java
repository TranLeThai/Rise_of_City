package com.example.rise_of_city.ui.game.roadmap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.game.BuildingProgress;
import com.example.rise_of_city.ui.view.ConnectorLineView;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Adapter cho RecyclerView hiển thị building trong Roadmap
 */
public class BuildingRoadMapAdapter extends RecyclerView.Adapter<BuildingRoadMapAdapter.BuildingViewHolder> {
    
    private List<BuildingProgress> buildings;
    private OnBuildingClickListener listener;
    
    public BuildingRoadMapAdapter(List<BuildingProgress> buildings) {
        this.buildings = buildings;
    }
    
    public void setBuildings(List<BuildingProgress> buildings) {
        this.buildings = buildings;
        notifyDataSetChanged();
    }
    
    public void setOnBuildingClickListener(OnBuildingClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public BuildingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_roadmap_building_zigzag, parent, false);
        return new BuildingViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BuildingViewHolder holder, int position) {
        BuildingProgress building = buildings.get(position);
        holder.bind(building, position, this);
    }
    
    @Override
    public int getItemCount() {
        return buildings != null ? buildings.size() : 0;
    }
    
    class BuildingViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardBuilding;
        private ImageView ivBuildingIcon;
        private ImageView ivStatusIcon;
        private View viewGreenDot;
        private TextView tvBuildingName;
        private TextView tvLevel;
        private TextView tvProgress;
        private TextView tvVocabularyProgress;
        private LinearLayout llProgressInfo;
        private ConnectorLineView viewConnector;
        
        BuildingViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBuilding = itemView.findViewById(R.id.card_building);
            ivBuildingIcon = itemView.findViewById(R.id.iv_building_icon);
            ivStatusIcon = itemView.findViewById(R.id.iv_status_icon);
            viewGreenDot = itemView.findViewById(R.id.view_green_dot);
            tvBuildingName = itemView.findViewById(R.id.tv_building_name);
            tvLevel = itemView.findViewById(R.id.tv_level);
            tvProgress = itemView.findViewById(R.id.tv_progress);
            tvVocabularyProgress = itemView.findViewById(R.id.tv_vocabulary_progress);
            llProgressInfo = itemView.findViewById(R.id.ll_progress_info);
            viewConnector = itemView.findViewById(R.id.view_connector);
        }
        
        void bind(BuildingProgress building, int position, BuildingRoadMapAdapter adapter) {
            // Tên building
            tvBuildingName.setText(building.getBuildingName());
            
            // Level
            tvLevel.setText("Level " + building.getLevel());
            
            // Progress EXP
            tvProgress.setText(building.getCurrentExp() + "/" + building.getMaxExp() + " EXP");
            
            // Vocabulary progress
            if (building.getVocabularyCount() > 0) {
                tvVocabularyProgress.setText(
                    building.getVocabularyLearned() + "/" + building.getVocabularyCount() + " từ vựng"
                );
                tvVocabularyProgress.setVisibility(View.VISIBLE);
            } else {
                tvVocabularyProgress.setVisibility(View.GONE);
            }
            
            // Hiển thị progress info nếu có progress
            if (building.getCurrentExp() > 0 || building.getVocabularyLearned() > 0) {
                llProgressInfo.setVisibility(View.VISIBLE);
            } else {
                llProgressInfo.setVisibility(View.GONE);
            }
            
            // Trạng thái building
            if (building.isCompleted()) {
                // Completed - màu xanh lá
                cardBuilding.setCardBackgroundColor(0xFF4CAF50);
                cardBuilding.setStrokeColor(0xFF4CAF50);
                ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
                ivStatusIcon.setColorFilter(0xFFFFFFFF);
                ivStatusIcon.setVisibility(View.VISIBLE);
                viewGreenDot.setVisibility(View.VISIBLE);
                ivBuildingIcon.setColorFilter(0xFFFFFFFF);
            } else if (building.isLocked()) {
                // Locked - màu xám
                cardBuilding.setCardBackgroundColor(0xFFE0E0E0);
                cardBuilding.setStrokeColor(0xFFE0E0E0);
                ivStatusIcon.setImageResource(R.drawable.ic_lock); // Sử dụng icon lock của dự án
                ivStatusIcon.setColorFilter(0xFF9E9E9E);
                ivStatusIcon.setVisibility(View.VISIBLE);
                viewGreenDot.setVisibility(View.GONE);
                ivBuildingIcon.setColorFilter(0xFF9E9E9E);
            } else {
                // Available - màu xanh dương
                cardBuilding.setCardBackgroundColor(0xFF3FD0F1);
                cardBuilding.setStrokeColor(0xFFB2EBF2);
                ivStatusIcon.setVisibility(View.GONE);
                viewGreenDot.setVisibility(View.VISIBLE);
                ivBuildingIcon.setColorFilter(0xFFFFFFFF);
            }
            
            // Icon building
            int iconRes = getBuildingIcon(building.getBuildingId());
            ivBuildingIcon.setImageResource(iconRes);
            
            // Zigzag positioning: trái phải trái phải (chia đều 2 bên)
            float horizontalBias;
            boolean isLeft = (position % 2 == 0);
            if (isLeft) {
                horizontalBias = 0.25f;
            } else {
                horizontalBias = 0.75f;
            }
            
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cardBuilding.getLayoutParams();
            params.horizontalBias = horizontalBias;
            cardBuilding.setLayoutParams(params);
            
            // Connector line
            if (position == buildings.size() - 1) {
                viewConnector.setVisibility(View.GONE);
            } else {
                viewConnector.setVisibility(View.VISIBLE);
                
                itemView.post(() -> {
                    if (itemView.getWidth() == 0 || itemView.getHeight() == 0) return;
                    
                    float cardCenterX = cardBuilding.getX() + cardBuilding.getWidth() / 2f;
                    float cardBottomY = cardBuilding.getY() + cardBuilding.getHeight();
                    
                    float startXRelative = cardCenterX / itemView.getWidth();
                    float startYRelative = cardBottomY / itemView.getHeight();
                    
                    boolean nextIsLeft = ((position + 1) % 2 == 0);
                    float endXRelative = nextIsLeft ? 0.25f : 0.75f;
                    
                    float density = itemView.getContext().getResources().getDisplayMetrics().density;
                    float cardCenterYInPixels = 40f * density;
                    
                    float itemHeight = itemView.getHeight();
                    float endYRelative = 1.0f + (cardCenterYInPixels / itemHeight);
                    
                    if (endYRelative <= startYRelative) {
                        endYRelative = Math.max(1.0f, startYRelative + 0.1f);
                    }
                    
                    viewConnector.setStartPoint(startXRelative, startYRelative);
                    viewConnector.setEndPoint(endXRelative, endYRelative);
                });
                
                if (position + 1 < buildings.size()) {
                    BuildingProgress nextBuilding = buildings.get(position + 1);
                    if (nextBuilding.isLocked()) {
                        viewConnector.setLineColor(0xFFE0E0E0);
                    } else {
                        viewConnector.setLineColor(0xFFB2EBF2);
                    }
                }
            }
            
            cardBuilding.setOnClickListener(v -> {
                if (listener != null && !building.isLocked()) {
                    listener.onBuildingClick(building);
                }
            });
        }
        
        private int getBuildingIcon(String buildingId) {
            switch (buildingId) {
                case "house": return R.drawable.vector_house;
                case "coffee": return R.drawable.vector_coffee;
                case "library": return R.drawable.vector_library;
                case "park": return R.drawable.vector_park;
                case "school": return R.drawable.vector_school;
                case "bakery": return R.drawable.vector_bakery;
                case "farm": return R.drawable.vector_farmer; // Farm = Farmer
                case "clothers": return R.drawable.vector_clothers;
                default: return R.drawable.ic_house;
            }
        }
    }
    
    public interface OnBuildingClickListener {
        void onBuildingClick(BuildingProgress building);
    }
}