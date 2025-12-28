package com.example.rise_of_city.ui.quiz_fragment.MATCHING;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.learning.quiz.MATCHING.MatchingIMGQuestion;
import com.example.rise_of_city.ui.lesson.LessonActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchingIMGFragment extends Fragment {
    private MatchingIMGQuestion question;
    private LinearLayout layoutImages, layoutWords;
    private Button btnCheckAll;

    private String currentSelectedImageName = null; // Lưu tên file thay vì ResID
    private Map<String, String> userPairs = new HashMap<>(); // <Tên ảnh, Từ đã nối>

    private Map<String, View> imgViews = new HashMap<>();
    private Map<String, Button> wordButtons = new HashMap<>();

    // ... newInstance giữ nguyên ...

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            question = (MatchingIMGQuestion) getArguments().getSerializable("data");
        }

        layoutImages = view.findViewById(R.id.layoutImages);
        layoutWords = view.findViewById(R.id.layoutWords);
        btnCheckAll = view.findViewById(R.id.btnCheckAll);

        if (question != null) {
            setupGame();
        }
        btnCheckAll.setOnClickListener(v -> validateAll());
    }

    private void setupGame() {
        layoutImages.removeAllViews();
        layoutWords.removeAllViews();
        userPairs.clear();

        // 1. Lấy danh sách tên ảnh và từ vựng
        List<String> imageNames = new ArrayList<>(question.getImageNames());
        List<String> words = new ArrayList<>(question.getWords());

        // 2. Xáo trộn độc lập để tạo thử thách cho Thị trưởng
        Collections.shuffle(imageNames);
        Collections.shuffle(words);

        // 3. Hiển thị ảnh
        for (String imgName : imageNames) {
            ImageView iv = new ImageView(getContext());
            // Dùng hàm chuyển đổi từ String sang ResID
            int resId = getResId(imgName);
            iv.setImageResource(resId);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(250, 250);
            params.setMargins(10, 10, 10, 10);
            iv.setLayoutParams(params);
            iv.setPadding(10, 10, 10, 10);

            iv.setOnClickListener(v -> selectImage(imgName, v));
            layoutImages.addView(iv);
            imgViews.put(imgName, iv);
        }

        // 4. Hiển thị chữ
        for (String word : words) {
            Button btn = new Button(getContext());
            btn.setText(word);
            btn.setOnClickListener(v -> linkWordToSelectedImage(word, btn));
            layoutWords.addView(btn);
            wordButtons.put(word, btn);
        }
    }

    private void selectImage(String imgName, View v) {
        for (View view : imgViews.values()) view.setBackground(null);
        currentSelectedImageName = imgName;
        v.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.border_selected));
    }

    private void linkWordToSelectedImage(String word, Button btn) {
        if (currentSelectedImageName == null) {
            Toast.makeText(getContext(), "Hãy chọn 1 hình ảnh trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu từ này đã được nối với ảnh khác, xóa nối cũ của từ đó
        String oldImage = null;
        for (Map.Entry<String, String> entry : userPairs.entrySet()) {
            if (entry.getValue().equals(word)) {
                oldImage = entry.getKey();
                break;
            }
        }
        if (oldImage != null) userPairs.remove(oldImage);

        // Nối cặp mới
        userPairs.put(currentSelectedImageName, word);

        // Reset màu tất cả nút và highlight lại các nút đã có trong userPairs
        refreshWordButtonsUI();
        Toast.makeText(getContext(), "Đã kết nối dữ liệu!", Toast.LENGTH_SHORT).show();
    }

    private void refreshWordButtonsUI() {
        for (Button btn : wordButtons.values()) {
            btn.setBackgroundTintList(null); // Trả về màu gốc
        }
        for (String pairedWord : userPairs.values()) {
            wordButtons.get(pairedWord).setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.blue_selected));
        }
    }

    private void validateAll() {
        if (userPairs.size() < question.getImageNames().size()) {
            Toast.makeText(getContext(), "Thị trưởng ơi, còn công trình chưa quy hoạch xong!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean allCorrect = true;
        // Kiểm tra dựa trên cặp gốc trong Model
        for (int i = 0; i < question.getImageNames().size(); i++) {
            String originImg = question.getImageNames().get(i);
            String correctWord = question.getWords().get(i);

            // Nếu ảnh gốc không nối với từ đúng tương ứng -> SAI
            if (!correctWord.equals(userPairs.get(originImg))) {
                allCorrect = false;
                break;
            }
        }

        LessonActivity activity = (LessonActivity) getActivity();
        if (allCorrect) {
            btnCheckAll.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.green_correct));
            new android.os.Handler().postDelayed(activity::handleCorrectAnswer, 500);
        } else {
            btnCheckAll.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.red_wrong));
            activity.handleWrongAnswer();
            Toast.makeText(getContext(), "Sai quy hoạch rồi!", Toast.LENGTH_SHORT).show();
            new android.os.Handler().postDelayed(this::resetGame, 800);
        }
    }

    private int getResId(String imgName) {
        String name = imgName.contains(".") ? imgName.substring(0, imgName.lastIndexOf(".")) : imgName;
        return requireContext().getResources().getIdentifier(name, "drawable", requireContext().getPackageName());
    }
    public static MatchingIMGFragment newInstance(MatchingIMGQuestion question) {
        MatchingIMGFragment fragment = new MatchingIMGFragment();
        Bundle args = new Bundle();

        // Gửi đối tượng câu hỏi vào "hồ sơ" (Bundle) của Fragment
        args.putSerializable("data", question);

        fragment.setArguments(args);
        return fragment;
    }

    private void resetGame() {
        btnCheckAll.setBackgroundTintList(null);
        setupGame();
    }
}