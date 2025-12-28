package com.example.rise_of_city.ui.quiz_fragment.MATCHING;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.learning.quiz.MATCHING.MatchingTextQuestion;
import com.example.rise_of_city.ui.lesson.LessonActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchingTextFragment extends Fragment {
    private MatchingTextQuestion question;
    private LinearLayout layoutLeft, layoutRight;
    private Button btnCheckAll;

    private String currentSelectedLeft = null;
    private Map<String, String> userPairs = new HashMap<>(); // <Từ Tiếng Anh, Từ Tiếng Việt đã nối>

    private Map<String, Button> leftButtons = new HashMap<>();
    private Map<String, Button> rightButtons = new HashMap<>();

    // --- ĐÂY LÀ HÀM BỊ THIẾU KHIẾN LESSONACTIVITY BÁO LỖI ---
    public static MatchingTextFragment newInstance(MatchingTextQuestion question) {
        MatchingTextFragment fragment = new MatchingTextFragment();
        Bundle args = new Bundle();
        args.putSerializable("data", question);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_matching_text, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            question = (MatchingTextQuestion) getArguments().getSerializable("data");
        }

        layoutLeft = view.findViewById(R.id.layoutLeft);
        layoutRight = view.findViewById(R.id.layoutRight);
        btnCheckAll = view.findViewById(R.id.btnCheckAll);

        if (question != null) {
            setupGame();
        }

        btnCheckAll.setOnClickListener(v -> validateAll());
    }

    private void setupGame() {
        layoutLeft.removeAllViews();
        layoutRight.removeAllViews();
        userPairs.clear();

        // Xáo trộn danh sách hiển thị để tạo thử thách
        List<String> leftList = new ArrayList<>(question.getEnglishWords());
        List<String> rightList = new ArrayList<>(question.getVietnameseWords());
        Collections.shuffle(leftList);
        Collections.shuffle(rightList);

        // Tạo cột bên trái (Tiếng Anh)
        for (String word : leftList) {
            Button btn = new Button(getContext());
            btn.setText(word);
            btn.setOnClickListener(v -> selectLeftWord(word, btn));
            layoutLeft.addView(btn);
            leftButtons.put(word, btn);
        }

        // Tạo cột bên phải (Tiếng Việt)
        for (String word : rightList) {
            Button btn = new Button(getContext());
            btn.setText(word);
            btn.setOnClickListener(v -> selectRightWord(word, btn));
            layoutRight.addView(btn);
            rightButtons.put(word, btn);
        }
    }

    private void selectLeftWord(String word, Button btn) {
        // Reset màu các nút bên trái
        for (Button b : leftButtons.values()) b.setBackgroundTintList(null);
        currentSelectedLeft = word;
        btn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.blue_selected));
    }

    private void selectRightWord(String word, Button btn) {
        if (currentSelectedLeft == null) {
            Toast.makeText(getContext(), "Hãy chọn từ Tiếng Anh trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu cặp nối
        userPairs.put(currentSelectedLeft, word);

        // Cập nhật UI: Highlight nút bên phải đã nối
        btn.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.blue_selected));
        Toast.makeText(getContext(), "Đã nối: " + currentSelectedLeft + " - " + word, Toast.LENGTH_SHORT).show();
    }

    private void validateAll() {
        if (userPairs.size() < question.getEnglishWords().size()) {
            Toast.makeText(getContext(), "Vui lòng nối hết các cặp từ!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean allCorrect = true;
        for (int i = 0; i < question.getEnglishWords().size(); i++) {
            String eng = question.getEnglishWords().get(i);
            String vie = question.getVietnameseWords().get(i);

            // Kiểm tra xem từ Tiếng Anh 'eng' có được nối với 'vie' không
            if (!vie.equals(userPairs.get(eng))) {
                allCorrect = false;
                break;
            }
        }

        LessonActivity activity = (LessonActivity) getActivity();
        if (activity == null) return;

        if (allCorrect) {
            btnCheckAll.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.green_correct));
            new android.os.Handler().postDelayed(activity::handleCorrectAnswer, 500);
        } else {
            btnCheckAll.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.red_wrong));
            activity.handleWrongAnswer();
            Toast.makeText(getContext(), "Nối sai rồi, thưa Thị trưởng!", Toast.LENGTH_SHORT).show();
            new android.os.Handler().postDelayed(this::resetGame, 800);
        }
    }

    private void resetGame() {
        btnCheckAll.setBackgroundTintList(null);
        setupGame();
    }
}