package com.example.rise_of_city.ui.quiz_fragment.TEXT_INTERACT;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.ui.game.ingame.LessonActivity;

public class LectureFragment extends Fragment {

    private TextView tvContentEnglish;
    private LinearLayout layoutOptions;
    private Button btnOpt1, btnOpt2, btnOpt3;
    private ImageButton btnTranslate;

    private String originalText = "Dear Mayor, The bakery project is ready for launch. We have imported enough [flower] to bake our signature bread.";
    private String translatedText = "Thưa Thị trưởng, Dự án tiệm bánh đã sẵn sàng khởi động. Chúng tôi đã nhập đủ [bột mì] để nướng loại bánh đặc trưng.";

    private boolean isTranslated = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lecture, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        tvContentEnglish = view.findViewById(R.id.tvContentEnglish);
        layoutOptions = view.findViewById(R.id.layoutOptions);
        btnOpt1 = view.findViewById(R.id.btnOpt1);
        btnOpt2 = view.findViewById(R.id.btnOpt2);
        btnOpt3 = view.findViewById(R.id.btnOpt3);
        btnTranslate = view.findViewById(R.id.btnTranslate);

        // Mặc định ẩn bảng lựa chọn
        layoutOptions.setVisibility(View.GONE);

        // 2. Thiết lập văn bản tương tác
        setupInteractiveText(originalText);

        // 3. Xử lý nút dịch
        btnTranslate.setOnClickListener(v -> {
            isTranslated = !isTranslated;
            if (isTranslated) {
                tvContentEnglish.setText(translatedText);
                layoutOptions.setVisibility(View.GONE); // Ẩn lựa chọn khi đang xem bản dịch
            } else {
                setupInteractiveText(originalText);
            }
        });
    }

    private void setupInteractiveText(String text) {
        SpannableString ss = new SpannableString(text);

        // Tìm vị trí của dấu ngoặc vuông [flower]
        int start = text.indexOf("[");
        int end = text.indexOf("]") + 1;

        if (start != -1 && end != -1) {
            // Tạo sự kiện Click cho từ trong ngoặc
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    showActionOptions("Flour", "Flower", "Floor");
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.RED); // Màu đỏ cho lỗi sai
                    ds.setUnderlineText(true); // Gạch chân
                }
            };

            ss.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvContentEnglish.setText(ss);
        tvContentEnglish.setMovementMethod(LinkMovementMethod.getInstance()); // Quan trọng để Click được
    }

    private void showActionOptions(String correct, String opt2, String opt3) {
        layoutOptions.setVisibility(View.VISIBLE);
        btnOpt1.setText(correct);
        btnOpt2.setText(opt2);
        btnOpt3.setText(opt3);

        btnOpt1.setOnClickListener(v -> handleAnswer(true, correct));
        btnOpt2.setOnClickListener(v -> handleAnswer(false, opt2));
        btnOpt3.setOnClickListener(v -> handleAnswer(false, opt3));
    }

    private void handleAnswer(boolean isCorrect, String selectedWord) {
        // Ép kiểu Activity để gọi hàm xử lý logic
        LessonActivity activity = (LessonActivity) getActivity();

        if (activity != null) {
            if (isCorrect) {
                // Thay đổi giao diện Fragment trước khi chuyển câu
                tvContentEnglish.setText(originalText.replace("[flower]", selectedWord));
                tvContentEnglish.setTextColor(Color.parseColor("#4CAF50")); // Đổi màu xanh lá

                // Đợi 1 chút để người chơi thấy đáp án đúng rồi mới chuyển
                new android.os.Handler().postDelayed(() -> {
                    activity.handleCorrectAnswer();
                }, 1000);

            } else {
                activity.handleWrongAnswer();
            }
        }
    }
}