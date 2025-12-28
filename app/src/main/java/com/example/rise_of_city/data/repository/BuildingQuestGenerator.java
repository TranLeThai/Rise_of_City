package com.example.rise_of_city.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.UserBuilding;
import com.example.rise_of_city.data.local.UserBuildingDao;
import com.example.rise_of_city.data.model.game.Building;
import com.example.rise_of_city.data.model.game.Mission;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Generator tạo mission thông minh dựa trên trạng thái building
 * - Daily mission: ôn bài, làm quiz kiếm vàng (luôn có)
 * - Emergency mission: sự cố khẩn cấp (có thời hạn, phạt nếu quá hạn)
 */
public class BuildingQuestGenerator {
    private static final String TAG = "BuildingQuestGenerator";

    private AppDatabase database;
    private UserBuildingDao buildingDao;
    private ExecutorService executor;
    private int currentUserId = 1; // TODO: lấy từ login thực tế (SharedPreferences hoặc ViewModel)

    public BuildingQuestGenerator(Context context) {
        database = AppDatabase.getInstance(context.getApplicationContext());
        buildingDao = database.userBuildingDao();
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Tạo mission phù hợp cho building
     * - Nếu building đã unlock: tạo Daily mission (kiếm vàng)
     * - Random tạo Emergency mission (sự cố khẩn cấp) với xác suất thấp
     */
    public void generateMissionsForBuilding(String buildingId, Building staticBuilding, OnMissionGeneratedListener listener) {
        executor.execute(() -> {
            try {
                UserBuilding userBuilding = buildingDao.getBuilding(currentUserId, buildingId);

                // Nếu building chưa unlock → không tạo mission nào
                if (userBuilding == null) {
                    listener.onGenerated(null);
                    return;
                }

                Mission mission = null;

                // 30% cơ hội tạo Emergency mission (sự cố khẩn cấp)
                if (Math.random() < 0.3) {
                    mission = createEmergencyMission(staticBuilding);
                } else {
                    // Ngược lại tạo Daily mission thông thường
                    mission = createDailyMission(staticBuilding);
                }

                listener.onGenerated(mission);

            } catch (Exception e) {
                Log.e(TAG, "Error generating mission for " + buildingId, e);
                listener.onGenerated(null);
            }
        });
    }

    /**
     * Tạo Daily mission (luôn có, không phạt, thưởng vàng)
     */
    private Mission createDailyMission(Building building) {
        String[] titles = getDailyQuestTitles(building.getId(), building.getName());
        String title = titles[(int) (Math.random() * titles.length)];

        Mission mission = new Mission(title, building.getId(), Mission.Type.DAILY);
        mission.goldReward = 80 + (int) (Math.random() * 71); // 80-150 vàng
        mission.goldPenalty = 0; // Không phạt
        mission.durationMs = 24 * 60 * 60 * 1000; // 24 giờ (có thể để vô hạn nếu muốn)

        return mission;
    }

    /**
     * Tạo Emergency mission (có thời hạn, có phạt nếu quá hạn)
     */
    private Mission createEmergencyMission(Building building) {
        String[] titles = getEmergencyTitles(building.getId(), building.getName());
        String title = titles[(int) (Math.random() * titles.length)];

        Mission mission = new Mission(title, building.getId(), Mission.Type.EMERGENCY);
        mission.goldReward = 150 + (int) (Math.random() * 101); // 150-250 vàng
        mission.goldPenalty = 50; // Phạt 50 vàng nếu quá hạn
        mission.durationMs = 12 * 60 * 60 * 1000; // 12 giờ

        return mission;
    }

    // Tiêu đề cho Daily mission
    private String[] getDailyQuestTitles(String buildingId, String buildingName) {
        return new String[]{
                "🌟 Ôn tập về " + buildingName + " hôm nay",
                "📚 Làm quiz " + buildingName + " - Kiếm vàng!",
                "✅ Kiểm tra kiến thức về " + buildingName
        };
    }

    // Tiêu đề cho Emergency mission
    private String[] getEmergencyTitles(String buildingId, String buildingName) {
        return new String[]{
                "⚠️ Sự cố khẩn cấp tại " + buildingName + "!",
                "🔥 Cần xử lý ngay vấn đề ở " + buildingName,
                "🚨 Báo động đỏ: " + buildingName + " gặp sự cố!"
        };
    }

    // Callback để trả mission về ViewModel
    public interface OnMissionGeneratedListener {
        void onGenerated(Mission mission);
    }
}