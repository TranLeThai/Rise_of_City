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
import com.example.rise_of_city.data.repository.BuildingHarvestRepository;
import com.example.rise_of_city.data.repository.BuildingUpgradeRepository;
import com.example.rise_of_city.data.repository.BuildingProgressRepository;
import com.example.rise_of_city.data.repository.GoldRepository;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class UnlockFragment extends Fragment {

    private Building building;
    private TextView tvBuildingName;
    private TextView tvLevel;
    private TextView tvProduction;
    private TextView tvHarvestReward;
    private TextView tvCooldown;
    private TextView tvUpgradeCost;
    private TextView tvUpgradeBenefits;
    private ImageView ivBuilding;
    private Button btnHarvest;
    private Button btnUpgrade;
    
    private BuildingHarvestRepository harvestRepo;
    private BuildingUpgradeRepository upgradeRepo;
    private GoldRepository goldRepo;
    private Handler handler;
    private Runnable cooldownUpdater;
    private Long lastHarvestTime; // Cache last harvest time để tính toán cooldown local

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

        // Initialize repositories
        harvestRepo = BuildingHarvestRepository.getInstance();
        upgradeRepo = BuildingUpgradeRepository.getInstance();
        goldRepo = GoldRepository.getInstance();
        handler = new Handler(Looper.getMainLooper());
        
        // Initialize views
        ImageButton btnClose = view.findViewById(R.id.btn_close);
        tvBuildingName = view.findViewById(R.id.tv_building_name);
        tvLevel = view.findViewById(R.id.tv_level);
        tvProduction = view.findViewById(R.id.tv_production);
        tvHarvestReward = view.findViewById(R.id.tv_harvest_reward);
        tvCooldown = view.findViewById(R.id.tv_cooldown);
        tvUpgradeCost = view.findViewById(R.id.tv_upgrade_cost);
        tvUpgradeBenefits = view.findViewById(R.id.tv_upgrade_benefits);
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
        // Load last harvest time trước khi start updater
        loadLastHarvestTime();
        startCooldownUpdater();

        // Button listeners
        if (btnHarvest != null) {
            btnHarvest.setOnClickListener(v -> {
                handleHarvest();
            });
        }

        if (btnUpgrade != null) {
            btnUpgrade.setOnClickListener(v -> {
                handleUpgrade();
            });
        }

        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data khi fragment được hiển thị lại (ví dụ: quay lại từ quiz)
        if (building != null) {
            loadLastHarvestTime();
            updateBuildingInfo();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Dừng cooldown updater khi fragment bị destroy
        if (cooldownUpdater != null) {
            handler.removeCallbacks(cooldownUpdater);
        }
    }

    private void updateBuildingInfo() {
        if (building != null) {
            if (tvBuildingName != null) {
                tvBuildingName.setText(building.getName());
            }
            if (tvLevel != null) {
                tvLevel.setText("LV. " + building.getLevel());
            }
            
            // Tính toán production dựa trên level
            int expPerHour = 10 * building.getLevel();
            int goldPerHour = 5 * building.getLevel();
            if (tvProduction != null) {
                tvProduction.setText("Sản xuất: " + expPerHour + " EXP/giờ • " + goldPerHour + " Vàng/giờ");
            }

            // Tính phần thưởng thu hoạch
            BuildingHarvestRepository.HarvestReward reward = harvestRepo.calculateHarvestReward(building.getLevel());
            if (tvHarvestReward != null) {
                tvHarvestReward.setText("Phần thưởng: +" + reward.expReward + " EXP • +" + reward.goldReward + " Vàng");
            }

            // Tính chi phí và lợi ích nâng cấp
            int upgradeCost = upgradeRepo.calculateUpgradeCost(building.getLevel());
            BuildingUpgradeRepository.UpgradeBenefits benefits = upgradeRepo.calculateUpgradeBenefits(building.getLevel() + 1);
            if (tvUpgradeCost != null) {
                tvUpgradeCost.setText("Chi phí: " + upgradeCost + " Vàng");
            }
            if (tvUpgradeBenefits != null) {
                tvUpgradeBenefits.setText("Sau nâng cấp: " + benefits.expPerHour + " EXP/giờ • " + benefits.goldPerHour + " Vàng/giờ");
            }

            // Set building image dựa trên building ID
            int imageResource = getBuildingImageResource(building.getId());
            if (imageResource != 0 && ivBuilding != null) {
                ivBuilding.setImageResource(imageResource);
            }
            
            // Cập nhật trạng thái nút thu hoạch
            updateHarvestButton();
        }
    }
    
    /**
     * Cập nhật trạng thái nút thu hoạch (enabled/disabled và text)
     */
    private void updateHarvestButton() {
        if (building == null || harvestRepo == null || btnHarvest == null) return;
        
        // Nếu có cache lastHarvestTime, tính toán local để update nhanh hơn
        if (lastHarvestTime != null) {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastHarvest = currentTime - lastHarvestTime;
            long remainingCooldown = BuildingHarvestRepository.HARVEST_COOLDOWN_MS - timeSinceLastHarvest;
            
            if (remainingCooldown <= 0) {
                // Đã hết cooldown
                btnHarvest.setEnabled(true);
                btnHarvest.setAlpha(1.0f);
                btnHarvest.setText("Thu Hoạch");
                if (tvCooldown != null) {
                    tvCooldown.setText("Sẵn sàng thu hoạch!");
                    tvCooldown.setTextColor(0xFF4CAF50); // Màu xanh
                }
            } else {
                // Còn cooldown - hiển thị timer MM:SS
                long remainingSeconds = remainingCooldown / 1000;
                long remainingMinutes = remainingSeconds / 60;
                long remainingSecs = remainingSeconds % 60;
                
                btnHarvest.setEnabled(false);
                btnHarvest.setAlpha(0.6f);
                btnHarvest.setText("Đang chờ...");
                if (tvCooldown != null) {
                    tvCooldown.setText(String.format("Còn %d:%02d", remainingMinutes, remainingSecs));
                    tvCooldown.setTextColor(0xFFFF9800); // Màu cam
                }
            }
            return;
        }
        
        // Nếu chưa có cache, load từ Firebase (chỉ lần đầu)
        harvestRepo.canHarvest(building.getId(), (canHarvest, remainingMinutes, message) -> {
            if (btnHarvest == null) return;
            if (canHarvest) {
                btnHarvest.setEnabled(true);
                btnHarvest.setAlpha(1.0f);
                btnHarvest.setText("Thu Hoạch");
                if (tvCooldown != null) {
                    tvCooldown.setText("Sẵn sàng thu hoạch!");
                    tvCooldown.setTextColor(0xFF4CAF50); // Màu xanh
                }
            } else {
                btnHarvest.setEnabled(false);
                btnHarvest.setAlpha(0.6f);
                btnHarvest.setText("Đang chờ...");
                if (tvCooldown != null) {
                    tvCooldown.setText("Còn " + remainingMinutes + " phút");
                    tvCooldown.setTextColor(0xFFFF9800); // Màu cam
                }
            }
            // Cache lastHarvestTime để lần sau tính toán local
            loadLastHarvestTime();
        });
    }
    
    /**
     * Load last harvest time từ Firebase để cache
     */
    private void loadLastHarvestTime() {
        if (building == null || harvestRepo == null) return;
        
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;
        
        String userId = auth.getCurrentUser().getUid();
        String buildingPath = "users/" + userId + "/buildings/" + building.getId();
        
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .document(buildingPath)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long lastHarvest = documentSnapshot.getLong("lastHarvestTime");
                        if (lastHarvest != null) {
                            lastHarvestTime = lastHarvest;
                        }
                    }
                });
    }
    
    /**
     * Bắt đầu cập nhật cooldown mỗi giây (để hiển thị timer chính xác)
     */
    private void startCooldownUpdater() {
        // Load last harvest time lần đầu
        loadLastHarvestTime();
        
        cooldownUpdater = new Runnable() {
            @Override
            public void run() {
                updateHarvestButton();
                // Cập nhật lại sau 1 giây (để timer MM:SS cập nhật mượt mà)
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(cooldownUpdater);
    }
    
    /**
     * Xử lý thu hoạch
     */
    private void handleHarvest() {
        if (building == null) return;
        
        harvestRepo.canHarvest(building.getId(), (canHarvest, remainingMinutes, message) -> {
            if (!canHarvest) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Tính phần thưởng
            BuildingHarvestRepository.HarvestReward reward = harvestRepo.calculateHarvestReward(building.getLevel());
            
            // Mở quiz để thu hoạch
            if (onHarvestClickListener != null) {
                onHarvestClickListener.onHarvestClick(building);
            } else {
                // Fallback: Thưởng trực tiếp (nếu không có quiz)
                rewardHarvestDirectly(reward);
            }
        });
    }
    
    /**
     * Thưởng thu hoạch trực tiếp (khi quiz đúng)
     */
    public void rewardHarvestDirectly(BuildingHarvestRepository.HarvestReward reward) {
        if (building == null) return;
        
        // Thưởng EXP
        BuildingProgressRepository.getInstance().addExpToBuilding(
            building.getId(), 
            reward.expReward,
            new BuildingProgressRepository.OnProgressUpdatedListener() {
                @Override
                public void onProgressUpdated(long level, int currentExp, int maxExp) {
                    // EXP đã được cập nhật
                }
                
                @Override
                public void onError(String error) {
                    Log.e("UnlockFragment", "Error adding EXP: " + error);
                }
            }
        );
        
        // Thưởng vàng
        goldRepo.addGold(reward.goldReward, new GoldRepository.OnGoldUpdatedListener() {
            @Override
            public void onGoldUpdated(int newGold) {
                        // Đánh dấu đã thu hoạch
                harvestRepo.markAsHarvested(building.getId(), new BuildingHarvestRepository.OnHarvestMarkedListener() {
                    @Override
                    public void onHarvestMarked() {
                        // Cập nhật cache lastHarvestTime
                        lastHarvestTime = System.currentTimeMillis();
                        Toast.makeText(getContext(), 
                            "Thu hoạch thành công! +" + reward.expReward + " EXP • +" + reward.goldReward + " Vàng",
                            Toast.LENGTH_LONG).show();
                        updateHarvestButton();
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e("UnlockFragment", "Error marking harvest: " + error);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Xử lý nâng cấp
     */
    private void handleUpgrade() {
        if (building == null) return;
        
        int upgradeCost = upgradeRepo.calculateUpgradeCost(building.getLevel());
        
        // Kiểm tra đủ vàng
        goldRepo.checkCanUnlockBuilding(upgradeCost, (canUpgrade, currentGold, message) -> {
            if (!canUpgrade) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Xác nhận nâng cấp
            new android.app.AlertDialog.Builder(getContext())
                .setTitle("Nâng cấp " + building.getName())
                .setMessage("Bạn có muốn nâng cấp lên Level " + (building.getLevel() + 1) + "?\n\n" +
                           "Chi phí: " + upgradeCost + " Vàng\n\n" +
                           "Lợi ích:\n" +
                           "• Sản xuất tăng: " + (10 * (building.getLevel() + 1)) + " EXP/giờ\n" +
                           "• Phần thưởng thu hoạch tăng")
                .setPositiveButton("Nâng cấp", (dialog, which) -> {
                    upgradeRepo.upgradeBuilding(building.getId(), building.getLevel(), 
                        new BuildingUpgradeRepository.OnUpgradeListener() {
                            @Override
                            public void onUpgradeSuccess(int newLevel, BuildingUpgradeRepository.UpgradeBenefits benefits) {
                                // Cập nhật building level
                                building = new Building(
                                    building.getId(),
                                    building.getName(),
                                    newLevel,
                                    0,
                                    (int)(100 * Math.pow(1.5, newLevel - 1)),
                                    building.isHasMission()
                                );
                                
                                updateBuildingInfo();
                                Toast.makeText(getContext(), 
                                    "Nâng cấp thành công! Level " + newLevel,
                                    Toast.LENGTH_SHORT).show();
                                
                                // Thông báo cho activity
                                if (onUpgradeClickListener != null) {
                                    onUpgradeClickListener.onUpgradeClick(building);
                                }
                            }
                            
                            @Override
                            public void onError(String error) {
                                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
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
