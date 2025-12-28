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
import com.example.rise_of_city.ui.lesson.LessonActivity;

public class ChoiceFragment extends Fragment {

    private ChoiceQuestion question;
    private ImageView ivBackground;
    private TextView tvQuestionContent;
    private Button[] optionButtons;
    private static final String ARG_CHOICE_QUESTION = "choice_question_data";
    private ChoiceQuestion mQuestion;

    public static ChoiceFragment newInstance(ChoiceQuestion question) {
        ChoiceFragment fragment = new ChoiceFragment();
        Bundle args = new Bundle();
        // Truyền đối tượng câu hỏi (phải implement Serializable)
        args.putSerializable(ARG_CHOICE_QUESTION, question);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Nhận lại dữ liệu câu hỏi khi Fragment được khởi tạo
            mQuestion = (ChoiceQuestion) getArguments().getSerializable(ARG_CHOICE_QUESTION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choice, container, false);

        // Kiểm tra nếu dữ liệu đã sẵn sàng thì hiển thị lên UI
        if (mQuestion != null) {
            setupUI();
        }

        return view;
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