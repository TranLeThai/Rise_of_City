package com.example.rise_of_city;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rise_of_city.R;

public class InGameActivity extends AppCompatActivity implements View.OnClickListener {

    // Khai báo các biến cho các tòa nhà
    private ImageView imgSchool, imgLibrary, imgPark, imgFarmer, imgCoffee, imgClothers, imgBakery, imgHouse, imgTree;
    private ScrollView vScroll;
    private HorizontalScrollView hScroll;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);

        // Ánh xạ ID từ XML sang Java
        imgSchool = findViewById(R.id.school);
        imgLibrary = findViewById(R.id.library);
        imgPark = findViewById(R.id.park);
        imgFarmer = findViewById(R.id.farmer);
        imgCoffee = findViewById(R.id.coffee);
        imgClothers = findViewById(R.id.clothers);
        imgBakery = findViewById(R.id.bakery);
        imgHouse = findViewById(R.id.house);
        imgTree = findViewById(R.id.tree);

        // Gán sự kiện click cho từng tòa nhà
        imgSchool.setOnClickListener(this);
        imgLibrary.setOnClickListener(this);
        imgPark.setOnClickListener(this);
        imgFarmer.setOnClickListener(this);
        imgCoffee.setOnClickListener(this);
        imgClothers.setOnClickListener(this);
        imgBakery.setOnClickListener(this);
        imgHouse.setOnClickListener(this);

        vScroll = findViewById(R.id.vertical_scroll);
        hScroll = findViewById(R.id.horizontal_scroll);
        if (vScroll != null && hScroll != null) {
            vScroll.post(this::scrollToCenter);
        }
    }
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
            mapWidth = (int) (1500 * density);
            View content = hScroll.getChildAt(0);
            mapHeight = content.getHeight();
        }

        int xTarget = (mapWidth - screenWidth) / 2;
        int yTarget = (mapHeight - screenHeight) / 2;

        if (xTarget < 0) xTarget = 0;
        if (yTarget < 0) yTarget = 0;

        hScroll.scrollTo(xTarget, 0);
        vScroll.scrollTo(0, yTarget);
    }

    @Override
    public void onClick(View v) {
        // Lấy ID của view được click để xử lý riêng
        int id = v.getId();

        if (id == R.id.school) {
            handleBuildingClick("Trường học", true); // Ví dụ: true là đã mở khóa
        } else if (id == R.id.library) {
            handleBuildingClick("Thư viện", false);
        } else if (id == R.id.park) {
            handleBuildingClick("Công viên", false);
        } else if (id == R.id.farmer) {
            handleBuildingClick("Nông trại", false);
        } else if (id == R.id.coffee) {
            handleBuildingClick("Quán Cà phê", false);
        } else if (id == R.id.clothers) {
            handleBuildingClick("Shop quần áo", false);
        } else if (id == R.id.bakery) {
            handleBuildingClick("Tiệm bánh", false);
        } else if (id == R.id.house) {
            handleBuildingClick("Nhà ở", true);
        }
    }

    // Hàm xử lý logic chung
    private void handleBuildingClick(String buildingName, boolean isUnlocked) {
        if (isUnlocked) {
            Toast.makeText(this, "Đang vào khu vực: " + buildingName, Toast.LENGTH_SHORT).show();
            // TODO: Chuyển sang Activity bài học tương ứng ở đây
        } else {
            Toast.makeText(this, buildingName + " hiện đang bị khóa!", Toast.LENGTH_SHORT).show();
        }
    }
}