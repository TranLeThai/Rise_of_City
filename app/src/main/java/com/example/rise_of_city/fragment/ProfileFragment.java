package com.example.rise_of_city.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Tạo layout đơn giản cho ProfileFragment (chưa có design)
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Logic profile sẽ được thêm sau khi có design

        return view;
    }
}

