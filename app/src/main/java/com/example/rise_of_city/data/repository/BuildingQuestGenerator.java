package com.example.rise_of_city.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.UserBuilding;
import com.example.rise_of_city.data.local.UserBuildingDao;
import com.example.rise_of_city.data.model.game.Building;
import com.example.rise_of_city.data.model.game.Mission;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Generator để tạo quest/mission hợp lý dựa trên thông tin building
 */
public class BuildingQuestGenerator {
    private static final String TAG = "BuildingQuestGenerator";
    
    private AppDatabase database;
    private UserBuildingDao buildingDao;
    private BuildingUpgradeManager upgradeManager;
    private ExecutorService executorService;
    private int userId;
    
    public enum QuestType {
        QUIZ_FOR_GOLD  // Nhiệm vụ làm quiz để kiếm vàng
    }
    
    public BuildingQuestGenerator(Context context) {
        database = AppDatabase.getInstance(context.getApplicationContext());
        buildingDao = database.userBuildingDao();
        upgradeManager = BuildingUpgradeManager.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
        
        android.content.SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences("RiseOfCity_Prefs", Context.MODE_PRIVATE);
        userId = prefs.getInt("logged_user_id", -1);
    }
    
    /**
     * Tạo quest đơn giản: làm quiz từ building để kiếm vàng
     * Chỉ tạo quest cho building đã unlock
     */
    public Mission generateSmartQuest(String buildingId, Building building, QuestType preferredType) {
        if (building == null) {
            Log.w(TAG, "Building is null, cannot generate quest");
            return null;
        }
        
        // Nếu building bị khóa, không tạo quest
        if (building.isLocked()) {
            Log.d(TAG, "Building " + buildingId + " is locked, skip quest generation");
            return null;
        }
        
        // Tạo quest làm quiz để kiếm vàng
        return createQuizForGoldQuest(building);
    }
    
    /**
     * Tạo quest làm quiz để kiếm vàng
     */
    private Mission createQuizForGoldQuest(Building building) {
        // Tạo tiêu đề đa dạng dựa trên building
        String[] questTitles = getQuestTitlesForBuilding(building.getId(), building.getName());
        String title = questTitles[(int)(Math.random() * questTitles.length)];
        
        Mission mission = new Mission(title, building.getId(), Mission.Type.RANDOM);
        
        // Phần thưởng vàng ngẫu nhiên từ 80-150
        mission.goldReward = 80 + (int)(Math.random() * 71);
        
        // Không có penalty (hoặc penalty nhỏ)
        mission.goldPenalty = 0;
        
        // Thời gian 24 giờ
        mission.durationMs = 24 * 60 * 60 * 1000;
        
        return mission;
    }
    
    /**
     * Lấy danh sách tiêu đề quest đa dạng cho từng building
     */
    private String[] getQuestTitlesForBuilding(String buildingId, String buildingName) {
        switch (buildingId) {
            case "house":
                return new String[]{
                    "🏠 Làm quiz về Nhà Cửa - Kiếm vàng!",
                    "🏠 Trả lời câu hỏi về Nhà Cửa",
                    "🏠 Ôn tập kiến thức Nhà Cửa"
                };
            case "bakery":
                return new String[]{
                    "🍞 Làm quiz về Tiệm Bánh - Kiếm vàng!",
                    "🍞 Trả lời câu hỏi về Tiệm Bánh",
                    "🍞 Ôn tập kiến thức Tiệm Bánh"
                };
            case "school":
                return new String[]{
                    "🏫 Làm quiz về Trường Học - Kiếm vàng!",
                    "🏫 Trả lời câu hỏi về Trường Học",
                    "🏫 Ôn tập kiến thức Trường Học"
                };
            case "library":
                return new String[]{
                    "📚 Làm quiz về Thư Viện - Kiếm vàng!",
                    "📚 Trả lời câu hỏi về Thư Viện",
                    "📚 Ôn tập kiến thức Thư Viện"
                };
            case "park":
                return new String[]{
                    "🌳 Làm quiz về Công Viên - Kiếm vàng!",
                    "🌳 Trả lời câu hỏi về Công Viên",
                    "🌳 Ôn tập kiến thức Công Viên"
                };
            case "coffee":
                return new String[]{
                    "☕ Làm quiz về Tiệm Cafe - Kiếm vàng!",
                    "☕ Trả lời câu hỏi về Tiệm Cafe",
                    "☕ Ôn tập kiến thức Tiệm Cafe"
                };
            case "farmer":
                return new String[]{
                    "🌾 Làm quiz về Nông Trại - Kiếm vàng!",
                    "🌾 Trả lời câu hỏi về Nông Trại",
                    "🌾 Ôn tập kiến thức Nông Trại"
                };
            case "clothers":
                return new String[]{
                    "👕 Làm quiz về Shop Quần Áo - Kiếm vàng!",
                    "👕 Trả lời câu hỏi về Shop Quần Áo",
                    "👕 Ôn tập kiến thức Shop Quần Áo"
                };
            default:
                return new String[]{
                    "💰 Làm quiz về " + buildingName + " - Kiếm vàng!",
                    "💰 Trả lời câu hỏi về " + buildingName,
                    "💰 Ôn tập kiến thức " + buildingName
                };
        }
    }
    
    /**
     * Lấy thông tin chi tiết về quest để hiển thị
     */
    public QuestInfo getQuestInfo(String buildingId, Building building) {
        String lessonName = building.getRequiredLessonName();
        
        QuestInfo info = new QuestInfo();
        info.questType = QuestType.QUIZ_FOR_GOLD;
        info.buildingName = building.getName();
        info.buildingLevel = building.getLevel();
        info.requiredLessonName = lessonName;
        info.isLocked = building.isLocked();
        
        // Description cho quiz quest
        info.description = "Làm quiz về " + building.getName() + " để kiếm vàng!";
        info.actionText = "Làm Quiz Ngay";
        
        return info;
    }
    
    public static class QuestInfo {
        public QuestType questType;
        public String buildingName;
        public int buildingLevel;
        public String requiredLessonName;
        public boolean isLocked;
        public String description;
        public String actionText;
    }
}

