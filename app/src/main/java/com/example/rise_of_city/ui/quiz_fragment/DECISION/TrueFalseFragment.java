package com.example.rise_of_city.ui.quiz_fragment.DECISION;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.learning.quiz.DECISION.TrueFalseQuestion;
import com.example.rise_of_city.ui.lesson.LessonActivity;

public class TrueFalseFragment extends Fragment {

    private TrueFalseQuestion question;
    private ImageView ivQuestionImage;
    private TextView tvDescription;
    private Button btnTrue, btnFalse;

    public static TrueFalseFragment newInstance(TrueFalseQuestion question) {
        TrueFalseFragment fragment = new TrueFalseFragment();
        Bundle args = new Bundle();
        args.putSerializable("data", question);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_true_false, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            question = (TrueFalseQuestion) getArguments().getSerializable("data");
        }

        ivQuestionImage = view.findViewById(R.id.ivQuestionImage);
        tvDescription = view.findViewById(R.id.tvDescription);
        btnTrue = view.findViewById(R.id.btnTrue);
        btnFalse = view.findViewById(R.id.btnFalse);

        if (question != null) {
            setupUI();
        }
    }

    private void setupUI() {
        tvDescription.setText(question.getDescriptionEnglish());

        // Lấy Resource ID từ tên file ảnh trong drawable
        int imageResId = requireContext().getResources().getIdentifier(
                question.getImagePath(), "drawable", requireContext().getPackageName());

        if (imageResId != 0) {
            ivQuestionImage.setImageResource(imageResId);
        }

        btnTrue.setOnClickListener(v -> checkAnswer(true));
        btnFalse.setOnClickListener(v -> checkAnswer(false));
    }

    private void checkAnswer(boolean userChoice) {
        LessonActivity activity = (LessonActivity) getActivity();
        if (activity == null || question == null) return;

        Button selectedButton = userChoice ? btnTrue : btnFalse;

        if (userChoice == question.isCorrect()) {
            // Đúng: Đổi màu nút được chọn sang xanh lá
            selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.green_correct));
            // Delay 500ms để người chơi kịp thấy phản hồi trước khi sang câu tiếp theo
            new android.os.Handler().postDelayed(activity::handleCorrectAnswer, 500);
        } else {
            // Sai: Đổi màu nút sang đỏ và trừ mạng qua LessonActivity
            selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.red_wrong));
            activity.handleWrongAnswer();
        }
    }
}