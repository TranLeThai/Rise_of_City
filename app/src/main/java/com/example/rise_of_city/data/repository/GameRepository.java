package com.example.rise_of_city.data.repository;

import android.content.Context;
import com.example.rise_of_city.data.model.game.Building;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GameRepository - Lưu trữ dữ liệu TĨNH (static) của các công trình trong game.
 *
 * Chỉ chứa:
 * - Tên công trình
 * - requiredLessonName (tên file JSON bài học để mở khóa hoặc nâng cấp)
 * - hasMission mặc định (nếu muốn gán sẵn mission cho một số building)
 *
 * KHÔNG chứa:
 * - level, currentExp, maxExp, isLocked → Những thứ này lấy từ Room Database (UserBuilding)
 */
public class GameRepository {

    private static GameRepository instance;

    private Map<String, Building> buildingMap;
    private List<Building> buildingList;

    // Singleton
    public static GameRepository getInstance(Context context) {
        if (instance == null) {
            instance = new GameRepository(context);
        }
        return instance;
    }

    private GameRepository(Context context) {
        initStaticData();
    }

    private void initStaticData() {
        buildingMap = new HashMap<>();
        buildingList = new ArrayList<>();

        // ===================================================================
        // THÔNG TIN TĨNH CỦA TỪNG CÔNG TRÌNH
        // ===================================================================

        // Nhà Cửa - Đã mở khóa từ đầu (thường là building đầu tiên)
        addBuilding(new Building(
                "house",
                "Nhà Của Tôi",
                0, 0, 100,           // level, exp, maxExp sẽ được override từ Room
                false,               // hasMission mặc định (có thể có mission random sau)
                false,               // isLocked sẽ được tính từ Room
                "House_lv1"          // Tên file JSON bài học để nâng cấp hoặc ôn lại
        ));

        // Trường học
        addBuilding(new Building("school", "Trường Học",
                0, 0, 100, false, true, "School_lv1"));

        // Thư viện
        addBuilding(new Building("library", "Thư Viện",
                0, 0, 100, false, true, "Library_lv1"));

        // Công viên - dùng Park_lv1.json (sẽ tạo)
        addBuilding(new Building("park", "Công Viên",
                0, 0, 100, false, true, "Park_lv1"));

        // Nông trại - dùng House_lv1.json tạm thời
        addBuilding(new Building("farmer", "Nông Trại",
                0, 0, 100, true, true, "House_lv1"));

        // Tiệm Cafe - dùng Bakery_lv1.json
        addBuilding(new Building("coffee", "Tiệm Cafe",
                0, 0, 100, true, true, "Bakery_lv1"));

        // Shop Quần Áo - dùng House_lv1.json tạm thời
        addBuilding(new Building("clothers", "Shop Quần Áo",
                0, 0, 100, false, true, "House_lv1"));

        // Tiệm Bánh
        addBuilding(new Building("bakery", "Tiệm Bánh",
                0, 0, 100, true, true, "Bakery_lv1"));
    }

    private void addBuilding(Building b) {
        buildingMap.put(b.getId(), b);
        buildingList.add(b);
    }

    // ================================================
    // CÁC HÀM CUNG CẤP DỮ LIỆU CHO VIEWMODEL
    // ================================================

    /**
     * Lấy thông tin tĩnh của một công trình theo ID.
     * ViewModel sẽ dùng cái này để lấy tên + requiredLessonName,
     * rồi kết hợp với dữ liệu từ Room để tạo Building hoàn chỉnh.
     */
    public Building getBuildingById(String id) {
        return buildingMap.get(id);
    }

    /**
     * Lấy danh sách tất cả công trình (dùng cho RoadMap, danh sách building sau này)
     */
    public List<Building> getAllBuildings() {
        return new ArrayList<>(buildingList); // trả về copy để tránh modify ngoài ý muốn
    }

    /**
     * (Tùy chọn) Lấy requiredLessonName trực tiếp nếu chỉ cần tên bài học
     */
    public String getRequiredLessonName(String buildingId) {
        Building b = buildingMap.get(buildingId);
        return b != null ? b.getRequiredLessonName() : null;
    }
}