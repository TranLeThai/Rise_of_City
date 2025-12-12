package com.example.rise_of_city;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rise_of_city.R;

public class InGameActivity extends AppCompatActivity implements View.OnClickListener {

    // Khai báo các biến cho các tòa nhà
    private ImageView imgSchool, imgLibrary, imgPark, imgFarmer, imgCoffee, imgClothers, imgBakery, imgHouse, imgTree;

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

        // imgTree có thể chỉ để trang trí, không cần click, hoặc thêm nếu muốn
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