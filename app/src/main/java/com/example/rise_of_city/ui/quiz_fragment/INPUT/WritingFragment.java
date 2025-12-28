package com.example.rise_of_city.ui.quiz_fragment.INPUT;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.learning.quiz.INPUT.WritingQuestion;
import com.example.rise_of_city.ui.lesson.LessonActivity;

public class WritingFragment extends Fragment {

    private WritingQuestion question;
    private TextView tvSentence, tvHint;
    private EditText etAnswer;
    private Button btnSubmit;

    public static WritingFragment newInstance(WritingQuestion question) {
        WritingFragment fragment = new WritingFragment();
        Bundle args = new Bundle();
        args.putSerializable("data", question);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_writing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            question = (WritingQuestion) getArguments().getSerializable("data");
        }

        tvSentence = view.findViewById(R.id.tvSentence);
        tvHint = view.findViewById(R.id.tvHint);
        etAnswer = view.findViewById(R.id.etAnswer);
        btnSubmit = view.findViewById(R.id.btnSubmit);

        if (question != null) {
            setupUI();
            // Tự động mở bàn phím để Thị trưởng nhập liệu ngay lập tức
            showKeyboard();
        }
    }

    private void setupUI() {
        tvSentence.setText(question.getSentence());

        if (question.getHint() != null && !question.getHint().isEmpty()) {
            tvHint.setVisibility(View.VISIBLE);
            tvHint.setOnClickListener(v -> {
                tvHint.setText("Thư ký nhắc: " + question.getHint());
                tvHint.setTextColor(Color.BLUE);
            });
        } else {
            tvHint.setVisibility(View.GONE);
        }

        btnSubmit.setOnClickListener(v -> {
            String userAnswer = etAnswer.getText().toString().trim();
            checkAnswer(userAnswer);
        });
    }

    private void checkAnswer(String answer) {
        LessonActivity activity = (LessonActivity) getActivity();
        if (activity == null || question == null) return;

        if (answer.equalsIgnoreCase(question.getCorrectAnswer())) {
            btnSubmit.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.green_correct));
            hideKeyboard();
            new android.os.Handler().postDelayed(activity::handleCorrectAnswer, 500);
        } else {
            btnSubmit.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.red_wrong));
            activity.handleWrongAnswer();
            Toast.makeText(getContext(), "Sai rồi, hãy thử lại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showKeyboard() {
        etAnswer.requestFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etAnswer, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etAnswer.getWindowToken(), 0);
        }
    }
}