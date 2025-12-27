package com.example.rise_of_city.ui.quiz_fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;

public class WordMatchFragment extends Fragment {

    private LinearLayout layoutLeft, layoutRight;
    private View selectedLeftView = null;
    private View selectedRightView = null;
    private String selectedLeftWord = "";
    private String selectedRightWord = "";

    // Dữ liệu mẫu
    private String[][] wordPairs = {
            {"Classroom", "Lớp học"},
            {"Library", "Thư viện"},
            {"Chalk", "Phấn"},
            {"School bag", "Cặp sách"}
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_word_match, container, false);
        layoutLeft = view.findViewById(R.id.layoutLeft);
        layoutRight = view.findViewById(R.id.layoutRight);

        setupGame();
        return view;
    }

    private void setupGame() {
        // Tạo cột trái
        for (String[] pair : wordPairs) {
            addItem(layoutLeft, pair[0], true);
        }

        // Tạo cột phải (trong thực tế nên xáo trộn mảng này)
        for (String[] pair : wordPairs) {
            addItem(layoutRight, pair[1], false);
        }
    }

    private void addItem(LinearLayout container, String word, boolean isLeft) {
        View itemView = getLayoutInflater().inflate(R.layout.item_word_match, container, false);
        TextView tv = itemView.findViewById(R.id.tvWord);
        View dot = itemView.findViewById(R.id.dot);
        tv.setText(word);

        // Đảo vị trí chấm đỏ nếu ở cột phải để hướng vào giữa
        if (!isLeft) {
            container.removeView(dot);
            ((LinearLayout) itemView).addView(dot, 0);
        }

        itemView.setOnClickListener(v -> handleSelection(itemView, word, isLeft));
        container.addView(itemView);
    }

    private void handleSelection(View v, String word, boolean isLeft) {
        if (isLeft) {
            if (selectedLeftView != null) selectedLeftView.setBackgroundResource(R.drawable.bg_word_item);
            selectedLeftView = v;
            selectedLeftWord = word;
            v.setBackgroundColor(Color.YELLOW); // Highlight khi chọn
        } else {
            if (selectedRightView != null) selectedRightView.setBackgroundResource(R.drawable.bg_word_item);
            selectedRightView = v;
            selectedRightWord = word;
            v.setBackgroundColor(Color.YELLOW);
        }

        checkMatch();
    }

    private void checkMatch() {
        if (!selectedLeftWord.isEmpty() && !selectedRightWord.isEmpty()) {
            boolean isCorrect = false;
            for (String[] pair : wordPairs) {
                if (pair[0].equals(selectedLeftWord) && pair[1].equals(selectedRightWord)) {
                    isCorrect = true;
                    break;
                }
            }

            if (isCorrect) {
                // Thành công: Ẩn hoặc đổi màu xanh
                selectedLeftView.setVisibility(View.INVISIBLE);
                selectedRightView.setVisibility(View.INVISIBLE);
                Toast.makeText(getContext(), "Chính xác!", Toast.LENGTH_SHORT).show();
            } else {
                // Sai: Reset màu
                selectedLeftView.setBackgroundResource(R.drawable.bg_word_item);
                selectedRightView.setBackgroundResource(R.drawable.bg_word_item);
            }

            // Reset biến tạm
            selectedLeftWord = "";
            selectedRightWord = "";
            selectedLeftView = null;
            selectedRightView = null;
        }
    }
}