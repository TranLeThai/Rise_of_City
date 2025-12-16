package com.example.rise_of_city.ui.ingame; // Đổi package nếu cần

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.Building;
import com.example.rise_of_city.fragment.UnlockFragment;
import com.example.rise_of_city.ui.dialog.LockAreaDialogFragment;
import com.example.rise_of_city.ui.dialog.MissionDialogFragment;
import com.example.rise_of_city.ui.lesson.LessonActivity;
import com.example.rise_of_city.ui.quiz.VocabularyQuizActivity;
import com.example.rise_of_city.ui.viewmodel.GameViewModel;
import com.example.rise_of_city.data.repository.GoldRepository;

public class InGameActivity extends AppCompatActivity implements View.OnClickListener {

    // 1. Khai báo ViewModel
    private GameViewModel viewModel;
    private GoldRepository goldRepo;
    private TextView tvCoinCount;

    // 2. Các biến View cơ bản
    private ScrollView vScroll;
    private HorizontalScrollView hScroll;

    // 3. Các biến cho Menu tương tác (Popup)
    private View layoutMenu;
    private TextView tvName, tvLevel, tvProgressText;
    private ProgressBar pbProgress;
    private Button btnUpgrade, btnMission;

    // Biến lưu view vừa bấm để tính toạ độ hiện menu
    private View currentClickedView;
    private long backPressedTime;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);

        hideSystemUI();
        setupDoubleBackToExit();

        View btnMenu = findViewById(R.id.btnOpenMenu); // Nút menu của bạn trên màn hình game
        btnMenu.setOnClickListener(v -> {
            MenuDialogFragment menuDialog = new MenuDialogFragment();
            menuDialog.show(getSupportFragmentManager(), "MenuDialog");
        });

        // --- KHỞI TẠO VIEWMODEL ---
        viewModel = new ViewModelProvider(this).get(GameViewModel.class);

        viewModel.init(this);
        
        // --- KHỞI TẠO GOLD REPOSITORY ---
        goldRepo = GoldRepository.getInstance();
        tvCoinCount = findViewById(R.id.count_coin);

        // --- ÁNH XẠ VIEW & SETUP ---
        initViews();
        setupBuildingEvents();
        
        // Load vàng
        loadGold();
        
        // --- XỬ LÝ BUILDING ID TỪ INTENT (Khi navigate từ Roadmap) ---
        handleIntentBuildingId();

        // --- LẮNG NGHE DỮ LIỆU (OBSERVE) ---
        // Đây là trái tim của MVVM: Khi dữ liệu thay đổi, hàm này tự chạy
        viewModel.getSelectedBuilding().observe(this, building -> {
            if (building != null) {
                // Kiểm tra nếu building bị khóa -> hiển thị dialog locked
                if (building.isLocked()) {
                    showLockAreaDialog(building);
                    // Đóng menu nếu đang mở
                    layoutMenu.setVisibility(View.GONE);
                    hideUnlockFragment();
                } else {
                    // Có dữ liệu và không bị khóa -> Hiện UnlockFragment (thay vì menu popup nhỏ)
                    showUnlockFragment(building);
                    // Ẩn menu popup cũ
                    layoutMenu.setVisibility(View.GONE);
                }
            } else {
                // Dữ liệu null -> Ẩn menu và fragment
                layoutMenu.setVisibility(View.GONE);
                hideUnlockFragment();
            }
        });

        // Bấm ra nền map thì đóng menu
        findViewById(R.id.img_map_background).setOnClickListener(v -> viewModel.closeMenu());

        // --- XỬ LÝ SCROLL MAP (Giữ nguyên logic cũ) ---
        vScroll = findViewById(R.id.vertical_scroll);
        hScroll = findViewById(R.id.horizontal_scroll);
        if (vScroll != null && hScroll != null) {
            vScroll.post(this::scrollToCenter);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh vàng khi quay lại
        loadGold();
    }
    
    private void loadGold() {
        if (goldRepo != null && tvCoinCount != null) {
            goldRepo.getCurrentGold(gold -> {
                tvCoinCount.setText(String.valueOf(gold));
            });
        }
    }
    
    /**
     * Xử lý buildingId từ intent khi navigate từ Roadmap
     * Tự động select building và hiển thị detail
     */
    private void handleIntentBuildingId() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("buildingId")) {
            String buildingId = intent.getStringExtra("buildingId");
            if (buildingId != null && !buildingId.isEmpty()) {
                // Đợi một chút để đảm bảo viewModel đã init xong và buildings đã load
                // Tự động select building sau khi layout xong
                getWindow().getDecorView().post(() -> {
                    // Tìm view building tương ứng và click vào nó
                    int viewId = getBuildingViewId(buildingId);
                    if (viewId != 0) {
                        View buildingView = findViewById(viewId);
                        if (buildingView != null) {
                            // Scroll đến building trước
                            scrollToBuilding(buildingView);
                            // Sau đó click vào building
                            buildingView.postDelayed(() -> {
                                viewModel.onBuildingClicked(buildingId);
                            }, 300); // Delay 300ms để scroll xong
                        } else {
                            // Nếu không tìm thấy view, thử select trực tiếp
                            viewModel.onBuildingClicked(buildingId);
                        }
                    } else {
                        // Nếu không tìm thấy viewId, thử select trực tiếp
                        viewModel.onBuildingClicked(buildingId);
                    }
                });
            }
        }
    }
    
    /**
     * Lấy view ID của building dựa trên buildingId
     */
    private int getBuildingViewId(String buildingId) {
        switch (buildingId) {
            case "house":
                return R.id.house;
            case "school":
                return R.id.school;
            case "library":
                return R.id.library;
            case "coffee":
                return R.id.coffee;
            case "bakery":
                return R.id.bakery;
            case "farmer":
                return R.id.farmer;
            case "park":
                return R.id.park;
            case "clothers":
                return R.id.clothers;
            default:
                return 0;
        }
    }
    
    /**
     * Scroll đến building view
     */
    private void scrollToBuilding(View buildingView) {
        if (buildingView == null || hScroll == null || vScroll == null) {
            return;
        }
        
        // Tính toán vị trí của building trong scroll view
        int[] location = new int[2];
        buildingView.getLocationOnScreen(location);
        
        // Scroll horizontal đến building
        int scrollX = location[0] - (hScroll.getWidth() / 2) + (buildingView.getWidth() / 2);
        hScroll.smoothScrollTo(Math.max(0, scrollX), 0);
        
        // Scroll vertical đến building
        int scrollY = location[1] - (vScroll.getHeight() / 2) + (buildingView.getHeight() / 2);
        vScroll.smoothScrollTo(0, Math.max(0, scrollY));
    }

    private void hideSystemUI() {
        Window window = getWindow();
        View decorView = window.getDecorView();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, decorView);
        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        }
    }

    private void setupDoubleBackToExit() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Kiểm tra nếu UnlockFragment đang hiển thị -> đóng fragment trước
                View fragmentContainer = findViewById(R.id.fragment_unlock_container);
                if (fragmentContainer != null && fragmentContainer.getVisibility() == View.VISIBLE) {
                    hideUnlockFragment();
                    viewModel.closeMenu(); // Reset selected building
                    return;
                }
                
                // Kiểm tra nếu menu popup đang hiển thị -> đóng menu
                if (layoutMenu != null && layoutMenu.getVisibility() == View.VISIBLE) {
                    layoutMenu.setVisibility(View.GONE);
                    viewModel.closeMenu();
                    return;
                }
                
                // Kiểm tra: Nếu thời gian hiện tại ít hơn thời gian bấm trước đó + 2000ms (2 giây)
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    // Hủy Toast đang hiện để giao diện sạch sẽ
                    if (mToast != null) mToast.cancel();

                    finish();
                } else {
                    mToast = Toast.makeText(InGameActivity.this, "Bấm lần nữa để thoát", Toast.LENGTH_SHORT);
                    mToast.show();

                    backPressedTime = System.currentTimeMillis();
                }
            }
        });
    }

    private void initViews() {
        // Ánh xạ các thành phần của Menu Popup
        layoutMenu = findViewById(R.id.layout_building_menu);
        tvName = findViewById(R.id.tv_menu_name);
        tvLevel = findViewById(R.id.tv_menu_level);
        pbProgress = findViewById(R.id.pb_menu_progress);
        tvProgressText = findViewById(R.id.tv_menu_progress_text);
        btnUpgrade = findViewById(R.id.btn_menu_upgrade);
        btnMission = findViewById(R.id.btn_menu_mission);

        // Sự kiện nút bấm trong menu
        if (btnMission != null) {
            btnMission.setOnClickListener(v -> {
                // Lấy building hiện tại từ ViewModel
                Building currentBuilding = viewModel.getSelectedBuilding().getValue();
                if (currentBuilding != null) {
                    showMissionDialog(currentBuilding);
                }
            });
        }

        // Icon mission (clipboard) ở góc trên trái - click để hiển thị mission dialog
        ImageView ivMission = findViewById(R.id.mission);
        if (ivMission != null) {
            ivMission.setOnClickListener(v -> {
                // Hiển thị mission dialog với mission random
                showRandomMissionDialog();
            });
        }
    }

    // Hàm helper để gán sự kiện click và ID cho từng tòa nhà
    private void setupBuildingEvents() {
        // Tham số thứ 2 ("school", "library") phải khớp với ID trong ViewModel
        setupBuilding(R.id.school, "school");
        setupBuilding(R.id.library, "library");
        setupBuilding(R.id.park, "park");
        setupBuilding(R.id.farmer, "farmer");
        setupBuilding(R.id.coffee, "coffee");
        setupBuilding(R.id.clothers, "clothers");
        setupBuilding(R.id.bakery, "bakery");
        setupBuilding(R.id.house, "house");
        // imgTree để trang trí, không cần setup
    }

    private void setupBuilding(int viewId, String buildingIdTag) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setTag(buildingIdTag); // Lưu ID chuỗi vào Tag để dùng sau
            view.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        // Lấy cái ID chuỗi ("school", "park"...) từ Tag ra
        String buildingId = (String) v.getTag();

        if (buildingId != null) {
            currentClickedView = v; // Lưu lại view để tí nữa tính toạ độ
            // Gửi thông báo sang ViewModel
            viewModel.onBuildingClicked(buildingId);
        }
    }

    // Hàm hiển thị Menu (Chỉ chạy khi ViewModel báo có dữ liệu)
    private void showBuildingMenu(Building data) {
        // 1. Đổ dữ liệu vào UI (Giữ nguyên)
        tvName.setText(data.getName());
        tvLevel.setText("Level: " + data.getLevel());
        pbProgress.setMax(data.getMaxExp());
        pbProgress.setProgress(data.getCurrentExp());
        tvProgressText.setText(data.getCurrentExp() + "/" + data.getMaxExp());

        if (data.isHasMission()) {
            btnUpgrade.setVisibility(View.GONE);
            btnMission.setVisibility(View.VISIBLE);
        } else {
            btnUpgrade.setVisibility(View.VISIBLE);
            btnMission.setVisibility(View.GONE);
        }

        // 2. TÍNH TOÁN VỊ TRÍ THÔNG MINH
        if (currentClickedView != null) {
            layoutMenu.setVisibility(View.VISIBLE);

            layoutMenu.post(() -> {
                // --- A. Lấy thông số cần thiết ---
                int scrollX = hScroll.getScrollX();
                int scrollY = vScroll.getScrollY();
                int menuW = layoutMenu.getWidth();
                int menuH = layoutMenu.getHeight();
                int buildingW = currentClickedView.getWidth();
                int buildingH = currentClickedView.getHeight();

                // Lấy kích thước màn hình
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screenW = displayMetrics.widthPixels;
                int screenH = displayMetrics.heightPixels;

                // --- B. Tính toán toạ độ thực tế của nhà trên màn hình ---
                // (Toạ độ map - Khoảng đã cuộn = Toạ độ trên màn hình)
                float buildingScreenX = currentClickedView.getX() - scrollX;
                float buildingScreenY = currentClickedView.getY() - scrollY;

                // --- C. Xử lý chiều Dọc (Y): Trên hay Dưới? ---
                // Mặc định: Hiển thị Ở TRÊN
                float finalY = buildingScreenY - menuH + 20; // +20 để đè nhẹ lên nóc

                // KIỂM TRA: Nếu menu bị trôi ra khỏi mép trên (y < 50px chừa chỗ cho thanh trạng thái)
                if (finalY < 50) {
                    // -> ĐỔI: Hiển thị XUỐNG DƯỚI chân nhà
                    finalY = buildingScreenY + buildingH - 20;
                }

                // (Optional) Kiểm tra nếu hiển thị bên dưới mà bị tràn đáy màn hình
                // thì ép nó nằm sát đáy (nhưng trường hợp này ít xảy ra hơn)
                if (finalY + menuH > screenH) {
                    finalY = screenH - menuH - 20;
                }

                // --- D. Xử lý chiều Ngang (X): Trái hay Phải? ---
                // Mặc định: Căn giữa
                float finalX = buildingScreenX + (buildingW / 2f) - (menuW / 2f);

                // KIỂM TRA:
                // 1. Nếu bị tràn sang trái (X < 0) -> Gán bằng mép trái (+10px padding)
                if (finalX < 10) {
                    finalX = 10;
                }
                // 2. Nếu bị tràn sang phải -> Gán bằng mép phải
                else if (finalX + menuW > screenW) {
                    finalX = screenW - menuW - 10;
                }

                // --- E. Chốt vị trí ---
                layoutMenu.setX(finalX);
                layoutMenu.setY(finalY);
            });
        }
    }

    // Hàm căn giữa bản đồ (Giữ nguyên từ code cũ)
    private void scrollToCenter() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        ImageView imgMap = findViewById(R.id.img_map_background);
        int mapWidth = imgMap.getWidth();
        int mapHeight = imgMap.getHeight();

        if (mapWidth == 0) {
            float density = getResources().getDisplayMetrics().density;
            mapWidth = (int) (1500 * density); // Đảm bảo số này khớp XML

            // Sửa lỗi findViewById ở đây bằng cách dùng hScroll
            if (hScroll.getChildCount() > 0) {
                View content = hScroll.getChildAt(0);
                mapHeight = content.getHeight();
            }
        }

        int xTarget = (mapWidth - screenWidth) / 2;
        int yTarget = (mapHeight - screenHeight) / 2;

        if (xTarget < 0) xTarget = 0;
        if (yTarget < 0) yTarget = 0;

        hScroll.scrollTo(xTarget, 0);
        vScroll.scrollTo(0, yTarget);
    }

    // Hiển thị dialog khi building bị khóa
    private void showLockAreaDialog(Building building) {
        LockAreaDialogFragment dialog = LockAreaDialogFragment.newInstance(building.getRequiredLessonName(), building);
        
        dialog.setOnLearnNowClickListener(() -> {
            // Chuyển sang màn hình học
            navigateToLessonScreen(building.getRequiredLessonName());
        });
        
        dialog.setOnUnlockWithGoldClickListener(unlockedBuilding -> {
            // Mở khóa building bằng vàng
            unlockBuildingWithGold(unlockedBuilding);
        });
        
        dialog.setOnCloseClickListener(() -> {
            // Đóng dialog và reset selected building
            viewModel.closeMenu();
        });
        
        dialog.show(getSupportFragmentManager(), "LockAreaDialog");
    }
    
    /**
     * Mở khóa building bằng vàng
     */
    private void unlockBuildingWithGold(Building building) {
        // TODO: Implement logic unlock building trong ViewModel/Repository
        // Ví dụ: viewModel.unlockBuilding(building.getId());
        android.widget.Toast.makeText(this, "Đã mở khóa " + building.getName() + " bằng vàng!", 
            android.widget.Toast.LENGTH_SHORT).show();
        // Refresh building list
        viewModel.init(this);
    }

    // Chuyển sang màn hình học
    private void navigateToLessonScreen(String lessonName) {
        Intent intent = new Intent(this, LessonActivity.class);
        intent.putExtra("lessonName", lessonName);
        startActivity(intent);
    }

    // Chuyển sang màn hình Vocabulary Quiz
    private void navigateToVocabularyQuiz() {
        Intent intent = new Intent(this, VocabularyQuizActivity.class);
        // Truyền buildingId để update progress đúng building
        Building currentBuilding = viewModel.getSelectedBuilding().getValue();
        if (currentBuilding != null) {
            intent.putExtra("buildingId", currentBuilding.getId());
        }
        startActivity(intent);
    }

    // Hiển thị mission dialog khi bấm nút mission
    private void showMissionDialog(Building building) {
        // Tạo mission text dựa trên building
        String missionText = "Hoàn thành bài học về '" + building.getName() + "' để nhận được phần thưởng!";
        String missionTitle = "Mission " + building.getName();
        
        MissionDialogFragment dialog = MissionDialogFragment.newInstance(missionTitle, missionText);
        
        dialog.setOnAcceptClickListener(() -> {
            // Khi chấp nhận mission, chuyển sang màn hình quiz từ vựng
            navigateToVocabularyQuiz();
        });
        
        dialog.setOnDenyClickListener(() -> {
            Toast.makeText(this, "Đã từ chối mission!", Toast.LENGTH_SHORT).show();
            // Đóng building menu
            viewModel.closeMenu();
        });
        
        dialog.show(getSupportFragmentManager(), "MissionDialog");
    }

    // Hiển thị UnlockFragment khi click vào building đã unlock
    private void showUnlockFragment(Building building) {
        View fragmentContainer = findViewById(R.id.fragment_unlock_container);
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
            
            UnlockFragment unlockFragment = UnlockFragment.newInstance(building);
            
            // Set callbacks
            unlockFragment.setOnHarvestClickListener(b -> {
                // Khi click Thu Hoạch, mở quiz từ vựng để thu hoạch
                // Sau khi quiz đúng, phần thưởng sẽ được thưởng trong VocabularyQuizActivity
                navigateToVocabularyQuiz();
                // Đóng fragment sau khi mở quiz
                hideUnlockFragment();
            });
            
            unlockFragment.setOnUpgradeClickListener(b -> {
                // Nâng cấp đã được xử lý trong UnlockFragment
                // Refresh building data sau khi nâng cấp
                viewModel.init(this);
            });
            
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_unlock_container, unlockFragment)
                    .commit();
        }
    }
    
    // Ẩn UnlockFragment
    private void hideUnlockFragment() {
        View fragmentContainer = findViewById(R.id.fragment_unlock_container);
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.GONE);
        }
    }

    // Hiển thị mission dialog random khi click vào icon mission (clipboard)
    private void showRandomMissionDialog() {
        // Tạo mission text ngẫu nhiên
        String[] missions = {
            "Hoàn thành 10 câu hỏi về thì hiện tại đơn để nhận được 100 coin và 50 XP!",
            "Trả lời đúng 5 câu liên tiếp về từ vựng để nhận được 80 coin!",
            "Hoàn thành bài tập ngữ pháp để nhận được 120 coin và 75 XP!",
            "Thực hành phát âm 15 từ để nhận được 90 coin và 60 XP!"
        };
        
        String randomMissionText = missions[(int) (Math.random() * missions.length)];
        String missionTitle = "Mission random";
        
        MissionDialogFragment dialog = MissionDialogFragment.newInstance(missionTitle, randomMissionText);
        
        dialog.setOnAcceptClickListener(() -> {
            // Khi chấp nhận mission random, chuyển sang màn hình quiz từ vựng
            navigateToVocabularyQuiz();
        });
        
        dialog.setOnDenyClickListener(() -> {
            Toast.makeText(this, "Đã từ chối mission!", Toast.LENGTH_SHORT).show();
        });
        
        dialog.show(getSupportFragmentManager(), "RandomMissionDialog");
    }
}