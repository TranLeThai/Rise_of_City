package com.example.rise_of_city.ui.quiz_fragment.CHOICE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.learning.quiz.CHOICE.ChoiceQuestion;
import com.example.rise_of_city.ui.game.ingame.LessonActivity;

public class ChoiceFragment extends Fragment {

    private ChoiceQuestion question;
    private ImageView ivBackground;
    private TextView tvQuestionContent;
    private Button[] optionButtons;

    public static ChoiceFragment newInstance(ChoiceQuestion question) {
        ChoiceFragment fragment = new ChoiceFragment();
        Bundle args = new Bundle();
        args.putSerializable("data", question);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_choice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        question = (ChoiceQuestion) getArguments().getSerializable("data");

        ivBackground = view.findViewById(R.id.ivBackground);
        tvQuestionContent = view.findViewById(R.id.tvQuestionContent);
        optionButtons = new Button[]{
                view.findViewById(R.id.btnOpt1), view.findViewById(R.id.btnOpt2),
                view.findViewById(R.id.btnOpt3), view.findViewById(R.id.btnOpt4)
        };

        setupUI();
    }

    private void setupUI() {
        // 1. Thay đổi Background dựa trên SubType
        switch (question.getSubType()) {
            case GRAMMAR:
                ivBackground.setImageResource(R.drawable.bg_grammar);
                break;
            case COMPLETION:
                ivBackground.setImageResource(R.drawable.bg_completion);
                break;
            case SYNONYM:
                ivBackground.setImageResource(R.drawable.bg_syn);
                break;
            case ANTONYM:
                ivBackground.setImageResource(R.drawable.bg_ant);
                break;
        }

        // 2. Hiển thị nội dung
        tvQuestionContent.setText(question.getQuestionContent());

        // 3. Hiển thị 4 lựa chọn
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i].setText(question.getOptions().get(i));
            final int index = i;
            optionButtons[i].setOnClickListener(v -> checkAnswer(index));
        }
    }

    private void checkAnswer(int selectedIndex) {
        LessonActivity activity = (LessonActivity) getActivity();
        if (activity == null) return;

        if (selectedIndex == question.getCorrectAnswerIndex()) {
            // Đúng: Hiệu ứng nút xanh + Báo cáo Activity
            optionButtons[selectedIndex].setBackgroundTintList(getContext().getColorStateList(R.color.green_correct));
            new android.os.Handler().postDelayed(activity::handleCorrectAnswer, 500);
        } else {
            // Sai: Hiệu ứng rung/nút đỏ + Trừ tim
            optionButtons[selectedIndex].setBackgroundTintList(getContext().getColorStateList(R.color.red_wrong));
            activity.handleWrongAnswer();
        }
    }
}