package com.example.rise_of_city.ui.game.ingame;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.repository.GoldRepository;

public class HomeFragment extends Fragment {

    private TextView tvCoinValue;
    private GoldRepository goldRepo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gắn layout fragment_home.xml vào code
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button btnPlay = view.findViewById(R.id.btn_play);
        Button btnStartGame = view.findViewById(R.id.btn_start_game);
        ImageView missionIcon = view.findViewById(R.id.mission);
        tvCoinValue = view.findViewById(R.id.tv_coin_value);
        
        // Khởi tạo GoldRepository và load vàng
        goldRepo = GoldRepository.getInstance();
        loadGold();

        // Sự kiện bấm nút Play (overlay trên đảo) -> Chuyển sang InGameActivity
        btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), InGameActivity.class);
            startActivity(intent);
        });

        // Sự kiện bấm nút Start Game -> Chuyển sang InGameActivity
        btnStartGame.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), InGameActivity.class);
            startActivity(intent);
        });

        // Sự kiện bấm icon mission -> Chuyển sang QuestsActivity

        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh vàng khi quay lại
        loadGold();
    }
    
    private void loadGold() {
        if (goldRepo != null && tvCoinValue != null && getContext() != null) {
            goldRepo.getCurrentGold(getContext(), gold -> {
                tvCoinValue.setText(String.valueOf(gold));
            });
        }
    }
}