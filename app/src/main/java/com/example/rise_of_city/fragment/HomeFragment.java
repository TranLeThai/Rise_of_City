package com.example.rise_of_city.fragment;

import com.example.rise_of_city.InGameActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gắn layout fragment_home.xml vào code
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button btnPlay = view.findViewById(R.id.btn_play);

        // Sự kiện bấm nút Play -> Chuyển sang InGameActivity
        btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), InGameActivity.class);
            startActivity(intent);
        });

        return view;
    }
}