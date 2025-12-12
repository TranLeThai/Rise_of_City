package com.example.rise_of_city.data.repository;

import android.content.Context;
import com.example.rise_of_city.data.model.Building;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameRepository {

    private static GameRepository instance;

    // Dùng Map để tìm kiếm nhanh bằng ID (VD: "school", "library")
    private Map<String, Building> buildingMap;
    // Dùng List nếu cần hiển thị danh sách (VD: RoadMap)
    private List<Building> buildingList;

    // Singleton Pattern
    public static GameRepository getInstance(Context context) {
        if (instance == null) {
            instance = new GameRepository(context);
        }
        return instance;
    }

    private GameRepository(Context context) {
        // Khởi tạo dữ liệu khi Repository được tạo ra
        initMockData();
    }

    private void initMockData() {
        buildingMap = new HashMap<>();
        buildingList = new ArrayList<>();

        // Thêm dữ liệu giả lập (Khớp với các ID trong InGameActivity)
        addBuilding(new Building("school", "Trường học", 5, 80, 100, true));
        addBuilding(new Building("library", "Thư viện", 2, 20, 150, false));
        addBuilding(new Building("park", "Công viên", 1, 0, 100, false));
        addBuilding(new Building("farmer", "Nông trại", 3, 50, 200, true));

        // Thêm các nhà khác cho khớp với InGameActivity
        addBuilding(new Building("coffee", "Tiệm Cafe", 2, 40, 120, true));
        addBuilding(new Building("clothers", "Shop Quần Áo", 4, 60, 180, false));
        addBuilding(new Building("bakery", "Tiệm Bánh", 3, 45, 130, true));
        addBuilding(new Building("house", "Nhà Của Tôi", 1, 100, 100, true));
    }

    // Hàm phụ trợ để thêm vào cả Map và List
    private void addBuilding(Building b) {
        buildingMap.put(b.getId(), b);
        buildingList.add(b);
    }

    // --- CÁC HÀM CUNG CẤP DỮ LIỆU CHO VIEWMODEL ---

    // 1. Lấy thông tin 1 tòa nhà cụ thể
    public Building getBuildingById(String id) {
        return buildingMap.get(id);
    }

    // 2. Lấy danh sách toàn bộ (Dùng cho RoadMap sau này)
    public List<Building> getAllBuildings() {
        return buildingList;
    }
}