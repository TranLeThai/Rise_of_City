// InGameActivity.java
package com.example.rise_of_city.ui.game.ingame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
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

import java.util.Map;

public class InGameActivity extends AppCompatActivity implements View.OnClickListener {

    private GameViewModel viewModel;
    private GoldRepository goldRepo;
    private TextView tvCoinCount;
    private ScrollView vScroll;
    private HorizontalScrollView hScroll;
    private View layoutMenu;
    private View fragmentContainer;

    private ImageView btnMenuRed, btnMissionList; // Đã khai báo

    private long backPressedTime;
    private Toast mToast;

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

        // --- BỔ SUNG: Ánh xạ nút Menu Đỏ để tránh NullPointerException ---
        btnMissionList = findViewById(R.id.mission);

        View bgMap = findViewById(R.id.img_map_background);
        if (bgMap != null) {
            bgMap.setOnClickListener(v -> viewModel.closeMenu());
        }
    }

    private void setupTopBarEvents() {
        if (btnMenuRed != null) {
            btnMenuRed.setOnClickListener(v -> {
                if (layoutMenu != null) layoutMenu.setVisibility(View.VISIBLE);
            });
        }

        if (btnMissionList != null) {
            btnMissionList.setOnClickListener(v -> {
                showMissionBoard();
            });
        }
    }

    private void showMissionBoard() {
        // Lấy danh sách nhiệm vụ từ ViewModel
        viewModel.getActiveMissions().observe(this, missions -> {
            if (missions == null || missions.isEmpty()) {
                Toast.makeText(this, "Hiện tại không có sự cố nào!", Toast.LENGTH_SHORT).show();
            } else {
                // Mở Dialog hiển thị danh sách
                MissionBoardDialog dialog = MissionBoardDialog.newInstance(missions);
                dialog.show(getSupportFragmentManager(), "MissionBoard");
            }
        });
    }
    private void setupObservers() {
        viewModel.getSelectedBuilding().observe(this, building -> {
            if (building != null) {
                if (building.isLocked()) {
                    showLockAreaDialog(building);
                    hideBuildingDetail();
                } else {
                    showBuildingDetail(building);
                }
                if (layoutMenu != null) layoutMenu.setVisibility(View.GONE);
            } else {
                hideBuildingDetail();
            }
        });

        viewModel.getBuildingsLockStatus().observe(this, this::updateBuildingImages);
        viewModel.loadAllBuildingsLockStatus();
    }

    private void showBuildingDetail(Building building) {
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
            BuildingDetailFragment detailFragment = BuildingDetailFragment.newInstance(building);

            // Đã có thể gọi vì đã thêm vào BuildingDetailFragment
            detailFragment.setOnUpgradeClickListener(b -> viewModel.loadBuildingFromFirebase(b.getId()));

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_unlock_container, detailFragment)
                    .commit();
        }
    }

    private void hideBuildingDetail() {
        if (fragmentContainer != null) fragmentContainer.setVisibility(View.GONE);
    }

    private void setupBuildingEvents() {
        int[] ids = {R.id.school, R.id.library, R.id.park, R.id.farmer, R.id.coffee, R.id.clothers, R.id.bakery, R.id.house};
        String[] tags = {"school", "library", "park", "farmer", "coffee", "clothers", "bakery", "house"};
        for (int i = 0; i < ids.length; i++) {
            View v = findViewById(ids[i]);
            if (v != null) {
                v.setTag(tags[i]);
                v.setOnClickListener(this);
            }
        }
    }

    private void showLockAreaDialog(Building building) {
        LockAreaDialogFragment dialog = LockAreaDialogFragment.newInstance(building.getRequiredLessonName(), building);
        dialog.setOnLearnNowClickListener(() -> {
            Intent intent = new Intent(this, LessonActivity.class);
            intent.putExtra("lessonName", building.getRequiredLessonName());
            intent.putExtra("mode", "STUDY_NEW");
            startActivity(intent);
        });
        dialog.setOnUnlockWithGoldClickListener(b -> {
            viewModel.unlockBuilding(b.getId());
            loadGold();
        });
        dialog.show(getSupportFragmentManager(), "LockAreaDialog");
    }

    private void hideSystemUI() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    private void setupDoubleBackToExit() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (fragmentContainer != null && fragmentContainer.getVisibility() == View.VISIBLE) {
                    hideBuildingDetail();
                    viewModel.closeMenu();
                    return;
                }
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
        goldRepo.getCurrentGold(gold -> {
            if (tvCoinCount != null) tvCoinCount.setText(String.valueOf(gold));
        });
    }

    private void scrollToCenter() {
        View bgMap = findViewById(R.id.img_map_background);
        if (vScroll != null && hScroll != null && bgMap != null) {
            int vCenter = (bgMap.getHeight() - vScroll.getHeight()) / 2;
            int hCenter = (bgMap.getWidth() - hScroll.getWidth()) / 2;
            vScroll.scrollTo(0, vCenter);
            hScroll.scrollTo(hCenter, 0);
        }
    }

    @Override
    public void onClick(View v) {
        String buildingId = (String) v.getTag();
        if (buildingId != null && viewModel != null) {
            viewModel.onBuildingClicked(buildingId);
        }
    }

    private void handleIntentBuildingId() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("building_id")) {
            String buildingId = intent.getStringExtra("building_id");
            if (viewModel != null && buildingId != null) {
                viewModel.loadBuildingById(buildingId);
            }
        }
    }

    private void updateBuildingImages(Map<String, Boolean> status) { }
}