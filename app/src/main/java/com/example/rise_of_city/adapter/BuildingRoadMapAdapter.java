package com.example.rise_of_city.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.BuildingProgress;
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
                ivStatusIcon.setImageResource(android.R.drawable.ic_lock_lock);
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
            
            // Icon building (có thể thay đổi theo buildingId)
            int iconRes = getBuildingIcon(building.getBuildingId());
            ivBuildingIcon.setImageResource(iconRes);
            
            // Zigzag positioning: trái phải trái phải (chia đều 2 bên)
            // Position 0, 2, 4... = left (0.25)
            // Position 1, 3, 5... = right (0.75)
            float horizontalBias;
            
            boolean isLeft = (position % 2 == 0);
            if (isLeft) {
                // Left side
                horizontalBias = 0.25f;
            } else {
                // Right side
                horizontalBias = 0.75f;
            }
            
            // Áp dụng bias cho card
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cardBuilding.getLayoutParams();
            params.horizontalBias = horizontalBias;
            cardBuilding.setLayoutParams(params);
            
            // Connector line (ẩn cho item cuối)
            if (position == buildings.size() - 1) {
                viewConnector.setVisibility(View.GONE);
            } else {
                viewConnector.setVisibility(View.VISIBLE);
                
                // Đợi layout xong để tính toán chính xác tọa độ
                itemView.post(() -> {
                    if (itemView.getWidth() == 0 || itemView.getHeight() == 0) {
                        return; // Chưa layout xong
                    }
                    
                    // Tính toán điểm bắt đầu: TÂM DƯỚI CÙNG của card building hiện tại
                    float cardCenterX = cardBuilding.getX() + cardBuilding.getWidth() / 2f;
                    float cardBottomY = cardBuilding.getY() + cardBuilding.getHeight();
                    
                    // Chuyển sang relative coordinates (0.0 - 1.0) so với itemView
                    float startXRelative = cardCenterX / itemView.getWidth();
                    float startYRelative = cardBottomY / itemView.getHeight();
                    
                    // Tính toán điểm kết thúc: TÂM CỦA card building tiếp theo
                    boolean nextIsLeft = ((position + 1) % 2 == 0);
                    float endXRelative = nextIsLeft ? 0.25f : 0.75f;
                    
                    // Lấy density để convert dp sang pixels
                    float density = itemView.getContext().getResources().getDisplayMetrics().density;
                    float cardCenterYInPixels = 40f * density; // 40dp = tâm card từ top (80dp / 2)
                    
                    float itemHeight = itemView.getHeight();
                    
                    // Tính toán điểm kết thúc: Tâm của card tiếp theo
                    // Card tiếp theo nằm ở item tiếp theo, với tâm ở 40dp từ top của item tiếp theo
                    // Vì item tiếp theo nằm ngay dưới item hiện tại, tâm card tiếp theo
                    // sẽ ở vị trí: bottom của item hiện tại + 40dp
                    // Relative với item hiện tại = 1.0 + (40dp / itemHeight)
                    // Vì đã bật clipChildren="false", line có thể vẽ vượt quá bounds
                    float endYRelative = 1.0f + (cardCenterYInPixels / itemHeight);
                    
                    // Đảm bảo endYRelative hợp lệ và nằm dưới startY
                    if (endYRelative <= startYRelative) {
                        // Nếu endY không dưới startY, đặt nó ở dưới startY
                        endYRelative = Math.max(1.0f, startYRelative + 0.1f);
                    }
                    
                    // Debug log để kiểm tra (có thể xóa sau khi test xong)
                    android.util.Log.d("ConnectorLine", String.format(
                        "Position %d: start(%.3f, %.3f) -> end(%.3f, %.3f), itemHeight=%.1fpx, cardCenterY=%.1fpx",
                        position, startXRelative, startYRelative, endXRelative, endYRelative, itemHeight, cardCenterYInPixels
                    ));
                    
                    // Set điểm bắt đầu và kết thúc cho connector line
                    viewConnector.setStartPoint(startXRelative, startYRelative);
                    viewConnector.setEndPoint(endXRelative, endYRelative);
                });
                
                // Màu connector: xanh nếu building tiếp theo available, xám nếu locked
                if (position + 1 < buildings.size()) {
                    BuildingProgress nextBuilding = buildings.get(position + 1);
                    if (nextBuilding.isLocked()) {
                        viewConnector.setLineColor(0xFFE0E0E0); // Grey
                    } else {
                        viewConnector.setLineColor(0xFFB2EBF2); // Light blue
                    }
                }
            }
            
            // Click listener
            cardBuilding.setOnClickListener(v -> {
                if (listener != null && !building.isLocked()) {
                    listener.onBuildingClick(building);
                }
            });
        }
        
        private int getBuildingIcon(String buildingId) {
            switch (buildingId) {
                case "house":
                    return android.R.drawable.ic_menu_myplaces;
                case "coffee":
                    return android.R.drawable.ic_menu_call;
                case "library":
                    return android.R.drawable.ic_menu_sort_by_size;
                case "park":
                    return android.R.drawable.ic_menu_gallery;
                case "school":
                    return android.R.drawable.ic_menu_edit;
                case "bakery":
                    return android.R.drawable.ic_menu_recent_history;
                case "farmer":
                    return android.R.drawable.ic_menu_compass;
                case "clothers":
                    return android.R.drawable.ic_menu_view;
                default:
                    return android.R.drawable.ic_menu_myplaces;
            }
        }
    }
    
    public interface OnBuildingClickListener {
        void onBuildingClick(BuildingProgress building);
    }
}

