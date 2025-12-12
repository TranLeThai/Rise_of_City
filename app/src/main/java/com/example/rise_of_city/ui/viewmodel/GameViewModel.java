package com.example.rise_of_city.ui.viewmodel;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.rise_of_city.data.model.Building;
import com.example.rise_of_city.data.repository.GameRepository;

public class GameViewModel extends ViewModel {

    private GameRepository repository;

    // LiveData để UI lắng nghe
    private MutableLiveData<Building> selectedBuilding = new MutableLiveData<>();

    // --- KHỞI TẠO ---
    // Vì ViewModel mặc định không nhận Context, ta cần hàm init này
    public void init(Context context) {
        if (repository == null) {
            repository = GameRepository.getInstance(context);
        }
    }

    // --- GETTER CHO UI ---
    public LiveData<Building> getSelectedBuilding() {
        return selectedBuilding;
    }

    // --- XỬ LÝ SỰ KIỆN ---

    // Khi người dùng click vào tòa nhà
    public void onBuildingClicked(String buildingId) {
        if (repository != null) {
            // Hỏi Repository xem có nhà này không
            Building building = repository.getBuildingById(buildingId);

            // Cập nhật giá trị -> InGameActivity sẽ tự nhận được thông báo
            selectedBuilding.setValue(building);
        }
    }

    // Đóng menu popup
    public void closeMenu() {
        selectedBuilding.setValue(null);
    }
}