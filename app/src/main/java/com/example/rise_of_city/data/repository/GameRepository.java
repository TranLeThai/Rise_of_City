package com.example.rise_of_city.data.repository;

import android.content.Context;
import com.example.rise_of_city.data.model.game.Building;
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
        // Buildings unlocked (không bị khóa) - để test fragment Unlock
        addBuilding(new Building("house", "Nhà Của Tôi", 1, 100, 100, true, false, null));
        addBuilding(new Building("library", "Thư Viện", 5, 50, 100, false, false, null)); // Unlocked để test
        addBuilding(new Building("school", "Trường học", 3, 75, 150, true, false, null)); // Unlocked để test
        
        // Buildings locked (bị khóa - cần hoàn thành bài học)
        addBuilding(new Building("park", "Công viên", 0, 0, 100, false, true, "Thì tương lai đơn"));
        addBuilding(new Building("farmer", "Nông trại", 0, 0, 200, true, true, "Thì hiện tại tiếp diễn"));
        addBuilding(new Building("coffee", "Tiệm Cafe", 0, 0, 120, true, true, "Thì quá khứ tiếp diễn"));
        addBuilding(new Building("clothers", "Shop Quần Áo", 0, 0, 180, false, true, "Thì tương lai tiếp diễn"));
        addBuilding(new Building("bakery", "Tiệm Bánh", 0, 0, 130, true, true, "Thì hiện tại hoàn thành"));
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
