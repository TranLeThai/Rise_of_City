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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.learning.quiz.TEXT_INTERACT.LectureQuestion;
import com.example.rise_of_city.ui.lesson.LessonActivity;

import java.util.HashSet;
import java.util.Set;

public class LectureFragment extends Fragment {

    private LectureQuestion question;
    private TextView tvContentEnglish;
    private LinearLayout layoutOptions;
    private Button btnOpt1, btnOpt2, btnOpt3;
    private ImageButton btnTranslate;

    private boolean isTranslated = false;
    private String currentDisplayingText;
    private Set<Integer> solvedErrorIndices = new HashSet<>(); // Lưu vị trí các lỗi đã sửa xong

    public static LectureFragment newInstance(LectureQuestion question) {
        LectureFragment fragment = new LectureFragment();
        Bundle args = new Bundle();
        args.putSerializable("data", question);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lecture, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            question = (LectureQuestion) getArguments().getSerializable("data");
        }

        tvContentEnglish = view.findViewById(R.id.tvContentEnglish);
        layoutOptions = view.findViewById(R.id.layoutOptions);
        btnOpt1 = view.findViewById(R.id.btnOpt1);
        btnOpt2 = view.findViewById(R.id.btnOpt2);
        btnOpt3 = view.findViewById(R.id.btnOpt3);
        btnTranslate = view.findViewById(R.id.btnTranslate);

        if (question != null) {
            currentDisplayingText = question.getContentEnglish();
            setupUI();
        }
    }

    private void setupUI() {
        layoutOptions.setVisibility(View.GONE);
        refreshInteractiveText();

        btnTranslate.setOnClickListener(v -> {
            isTranslated = !isTranslated;
            if (isTranslated) {
                tvContentEnglish.setText(question.getContentVietnamese());
                tvContentEnglish.setTextColor(Color.BLACK);
                layoutOptions.setVisibility(View.GONE);
            } else {
                refreshInteractiveText();
            }
        });
    }

    private void refreshInteractiveText() {
        SpannableString ss = new SpannableString(currentDisplayingText);

        // Duyệt qua danh sách lỗi từ Model
        for (int i = 0; i < question.getWrongWordList().size(); i++) {
            if (solvedErrorIndices.contains(i)) continue; // Bỏ qua nếu lỗi này đã sửa rồi

            LectureQuestion.WrongWordInfo errorInfo = question.getWrongWordList().get(i);
            String target = errorInfo.getOriginalWrongWord();

            int start = currentDisplayingText.indexOf(target);
            if (start == -1) continue;
            int end = start + target.length();

            final int errorIndex = i;
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    showActionOptions(errorIndex);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.RED); // Lỗi chưa sửa hiện màu đỏ
                    ds.setUnderlineText(true);
                }
            };
            ss.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvContentEnglish.setText(ss);
        tvContentEnglish.setTextColor(Color.BLACK);
        tvContentEnglish.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void showActionOptions(int errorIndex) {
        LectureQuestion.WrongWordInfo errorInfo = question.getWrongWordList().get(errorIndex);
        if (errorInfo.getOptions() == null || errorInfo.getOptions().size() < 3) return;

        layoutOptions.setVisibility(View.VISIBLE);
        btnOpt1.setText(errorInfo.getOptions().get(0));
        btnOpt2.setText(errorInfo.getOptions().get(1));
        btnOpt3.setText(errorInfo.getOptions().get(2));

        btnOpt1.setOnClickListener(v -> handleAnswer(errorIndex, btnOpt1.getText().toString()));
        btnOpt2.setOnClickListener(v -> handleAnswer(errorIndex, btnOpt2.getText().toString()));
        btnOpt3.setOnClickListener(v -> handleAnswer(errorIndex, btnOpt3.getText().toString()));
    }

    private void handleAnswer(int errorIndex, String selectedWord) {
        LessonActivity activity = (LessonActivity) getActivity();
        if (activity == null) return;

        LectureQuestion.WrongWordInfo errorInfo = question.getWrongWordList().get(errorIndex);

        if (selectedWord.equalsIgnoreCase(errorInfo.getCorrectAnswer())) {
            // Sửa từ sai thành từ đúng trong chuỗi hiển thị
            currentDisplayingText = currentDisplayingText.replaceFirst(errorInfo.getOriginalWrongWord(), selectedWord);
            solvedErrorIndices.add(errorIndex);

            layoutOptions.setVisibility(View.GONE);
            refreshInteractiveText();

            // Kiểm tra xem đã sửa hết tất cả các lỗi chưa
            if (solvedErrorIndices.size() == question.getWrongWordList().size()) {
                tvContentEnglish.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_correct));
                new android.os.Handler().postDelayed(activity::handleCorrectAnswer, 1000);
            }
        } else {
            activity.handleWrongAnswer();
        }
    }
}