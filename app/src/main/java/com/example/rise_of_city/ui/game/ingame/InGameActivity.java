package com.example.rise_of_city.ui.game.ingame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.game.Building;
import com.example.rise_of_city.data.repository.GoldRepository;
import com.example.rise_of_city.ui.game.building.BuildingDetailFragment;
import com.example.rise_of_city.ui.dialog.LockAreaDialogFragment;
import com.example.rise_of_city.ui.game.ingame.mission.MissionBoardDialog;
import com.example.rise_of_city.ui.lesson.LessonActivity;
import com.example.rise_of_city.ui.viewmodel.GameViewModel;

import java.util.HashMap;
import java.util.Map;

public class InGameActivity extends AppCompatActivity implements View.OnClickListener {

    private GameViewModel viewModel;
    private GoldRepository goldRepo;

    private TextView tvCoinCount;
    private ScrollView vScroll;
    private HorizontalScrollView hScroll;
    private View layoutMenu;
    private View fragmentContainer;

    // Đã khai báo và sẽ ánh xạ đúng
    private ImageView btnMenuRed;
    private ImageView btnMissionList;

    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);

        hideSystemUI();
        initViews();

        goldRepo = GoldRepository.getInstance();
        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        viewModel.init(this);

        setupDoubleBackToExit();
        setupBuildingEvents();
        setupTopBarEvents();
        setupObservers();

        loadGold();
        handleIntentBuildingId();

        // Cuộn đến giữa bản đồ sau khi layout xong
        if (vScroll != null && hScroll != null) {
            vScroll.post(this::scrollToCenter);
        }
    }

    private void initViews() {
        tvCoinCount = findViewById(R.id.count_coin);
        vScroll = findViewById(R.id.vertical_scroll);
        hScroll = findViewById(R.id.horizontal_scroll);
        layoutMenu = findViewById(R.id.layout_building_menu);
        fragmentContainer = findViewById(R.id.fragment_unlock_container);

        // ÁNH XẠ ĐÚNG CÁC NÚT TRÊN TOP BAR
        btnMenuRed = findViewById(R.id.btn_menu);        // <-- Thay ID thực tế nếu khác
        btnMissionList = findViewById(R.id.mission);         // Đã có ID này

        // Click nền bản đồ để đóng menu chi tiết
        View bgMap = findViewById(R.id.img_map_background);
        if (bgMap != null) {
            bgMap.setOnClickListener(v -> viewModel.closeMenu());
        }
    }

    private void setupTopBarEvents() {
        if (btnMenuRed != null) {
            btnMenuRed.setOnClickListener(v -> {
                if (layoutMenu != null) {
                    layoutMenu.setVisibility(View.VISIBLE);
                }
            });
        }

        if (btnMissionList != null) {
            btnMissionList.setOnClickListener(v -> showMissionBoard());
        }
    }

    private void showMissionBoard() {
        viewModel.getActiveMissions().observe(this, missions -> {
            if (missions == null || missions.isEmpty()) {
                Toast.makeText(this, "Hiện tại không có nhiệm vụ nào!", Toast.LENGTH_SHORT).show();
            } else {
                MissionBoardDialog dialog = MissionBoardDialog.newInstance(missions);
                dialog.show(getSupportFragmentManager(), "MissionBoard");
            }
        });
    }

    private void setupObservers() {
        // Quan sát công trình được chọn
        viewModel.getSelectedBuilding().observe(this, building -> {
            if (building != null) {
                if (building.isLocked()) {
                    showLockAreaDialog(building);
                    hideBuildingDetail();
                } else {
                    showBuildingDetail(building);
                }
                // Đóng menu chọn building nếu đang mở
                if (layoutMenu != null) {
                    layoutMenu.setVisibility(View.GONE);
                }
            } else {
                hideBuildingDetail();
            }
        });

        // Quan sát trạng thái khóa/mở của tất cả công trình để cập nhật hình ảnh (nếu cần)
        viewModel.getBuildingsLockStatus().observe(this, this::updateBuildingImages);

        // Tải trạng thái khóa lần đầu
        viewModel.loadAllBuildingsLockStatus();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload building status mỗi khi quay lại activity
        if (viewModel != null) {
            viewModel.loadAllBuildingsLockStatus();
        }
        loadGold();
    }

    private void showBuildingDetail(Building building) {
        if (fragmentContainer == null) return;

        fragmentContainer.setVisibility(View.VISIBLE);

        BuildingDetailFragment detailFragment = BuildingDetailFragment.newInstance(building);

        // Nếu BuildingDetailFragment có nút upgrade → reload lại thông tin building sau khi upgrade
        // (vì giờ dùng Room, không còn loadFromFirebase nữa → dùng loadBuildingFromLocal)
        detailFragment.setOnUpgradeClickListener(b -> {
            // Sau khi upgrade thành công, reload building để cập nhật level mới
            viewModel.loadBuildingById(b.getId());
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_unlock_container, detailFragment)
                .commit();
    }

    private void hideBuildingDetail() {
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.GONE);
        }
    }

    private void setupBuildingEvents() {
        int[] ids = {
                R.id.school, R.id.library, R.id.park, R.id.farmer,
                R.id.coffee, R.id.clothers, R.id.bakery, R.id.house
        };
        String[] tags = {
                "school", "library", "park", "farmer",
                "coffee", "clothers", "bakery", "house"
        };

        for (int i = 0; i < ids.length; i++) {
            View v = findViewById(ids[i]);
            if (v != null) {
                v.setTag(tags[i]);
                v.setOnClickListener(this);
            }
        }
    }

    private void showLockAreaDialog(Building building) {
        LockAreaDialogFragment dialog = LockAreaDialogFragment.newInstance(
                building.getRequiredLessonName(), building);

        dialog.setOnLearnNowClickListener(() -> {
            Intent intent = new Intent(this, LessonActivity.class);
            intent.putExtra("lessonName", building.getRequiredLessonName());
            intent.putExtra("mode", "STUDY_NEW");
            startActivity(intent);
        });

        dialog.setOnUnlockWithGoldClickListener(b -> {
            // Gọi unlock từ ViewModel với cost vàng
            viewModel.unlockBuildingWithGold(b.getId(), new com.example.rise_of_city.ui.viewmodel.GameViewModel.UnlockCallback() {
                @Override
                public void onSuccess(int newGold) {
                    // Cập nhật lại số vàng hiển thị
                    loadGold();
                    Toast.makeText(InGameActivity.this, "Đã mở khóa " + b.getName() + "! Vàng còn lại: " + newGold, Toast.LENGTH_SHORT).show();
                    
                    // Đóng dialog
                    dialog.dismiss();
                    
                    // Reload building để cập nhật trạng thái và hiển thị BuildingDetailFragment
                    // Sau một chút delay để dialog dismiss hoàn toàn
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        viewModel.loadBuildingById(b.getId());
                    }, 300);
                }
                
                @Override
                public void onError(String error) {
                    Toast.makeText(InGameActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show(getSupportFragmentManager(), "LockAreaDialog");
    }

    private void hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(
                getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    private void setupDoubleBackToExit() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Nếu đang mở chi tiết building → đóng nó trước
                if (fragmentContainer != null && fragmentContainer.getVisibility() == View.VISIBLE) {
                    hideBuildingDetail();
                    viewModel.closeMenu();
                    return;
                }

                // Double back to exit
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    finish();
                } else {
                    Toast.makeText(InGameActivity.this, "Bấm lần nữa để thoát", Toast.LENGTH_SHORT).show();
                    backPressedTime = System.currentTimeMillis();
                }
            }
        });
    }

    private void loadGold() {
        if (goldRepo != null) {
            goldRepo.getCurrentGold(this, gold -> {
                if (tvCoinCount != null) {
                    tvCoinCount.setText(String.valueOf(gold));
                }
            });
        }
    }

    private void scrollToCenter() {
        View bgMap = findViewById(R.id.img_map_background);
        if (vScroll != null && hScroll != null && bgMap != null) {
            int vCenter = (bgMap.getHeight() - vScroll.getHeight()) / 2;
            int hCenter = (bgMap.getWidth() - hScroll.getWidth()) / 2;
            vScroll.scrollTo(0, Math.max(vCenter, 0));
            hScroll.scrollTo(Math.max(hCenter, 0), 0);
        }
    }

    @Override
    public void onClick(View v) {
        String buildingId = (String) v.getTag();
        if (buildingId != null) {
            viewModel.onBuildingClicked(buildingId);
        }
    }

    private void handleIntentBuildingId() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("building_id")) {
            String buildingId = intent.getStringExtra("building_id");
            if (buildingId != null) {
                viewModel.loadBuildingById(buildingId);
            }
        }
    }

    // Có thể dùng để đổi hình ảnh building khi đã mở khóa (nếu bạn có 2 hình khác nhau)
    private void updateBuildingImages(Map<String, Boolean> status) {
        if (status == null) return;
        
        // Map building ID to view ID
        Map<String, Integer> buildingViewMap = new HashMap<>();
        buildingViewMap.put("school", R.id.school);
        buildingViewMap.put("library", R.id.library);
        buildingViewMap.put("park", R.id.park);
        buildingViewMap.put("farmer", R.id.farmer);
        buildingViewMap.put("coffee", R.id.coffee);
        buildingViewMap.put("clothers", R.id.clothers);
        buildingViewMap.put("bakery", R.id.bakery);
        buildingViewMap.put("house", R.id.house);
        
        // Cập nhật visibility/alpha cho từng building
        for (Map.Entry<String, Boolean> entry : status.entrySet()) {
            String buildingId = entry.getKey();
            boolean isLocked = entry.getValue();
            
            Integer viewId = buildingViewMap.get(buildingId);
            if (viewId != null) {
                View buildingView = findViewById(viewId);
                if (buildingView != null) {
                    if (isLocked) {
                        // Building bị khóa → chỉ làm mờ một chút, KHÔNG đổi màu đen
                        buildingView.setAlpha(0.5f);
                        // Xóa color filter để giữ nguyên màu gốc
                        if (buildingView instanceof ImageView) {
                            ((ImageView) buildingView).clearColorFilter();
                        }
                    } else {
                        // Building đã mở → hiển thị bình thường
                        buildingView.setAlpha(1.0f);
                        // Xóa color filter
                        if (buildingView instanceof ImageView) {
                            ((ImageView) buildingView).clearColorFilter();
                        }
                    }
                }
            }
        }
    }
}