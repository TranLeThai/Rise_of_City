package com.example.rise_of_city.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;

public class RoadMapFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gắn layout fragment_road_map.xml vào code
        View view = inflater.inflate(R.layout.fragment_road_map, container, false);

        // Có thể thêm logic xử lý roadmap ở đây nếu cần

        return view;
    }
}

