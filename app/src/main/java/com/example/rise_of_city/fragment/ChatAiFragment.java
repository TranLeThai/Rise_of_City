package com.example.rise_of_city.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;

public class ChatAiFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Gắn layout fragment_chat_ai.xml vào code
        View view = inflater.inflate(R.layout.fragment_chat_ai, container, false);

        // Có thể thêm logic xử lý chat AI ở đây nếu cần

        return view;
    }
}

