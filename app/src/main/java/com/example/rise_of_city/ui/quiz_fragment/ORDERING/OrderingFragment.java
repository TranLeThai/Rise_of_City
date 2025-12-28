package com.example.rise_of_city.ui.quiz_fragment.ORDERING;

import android.content.Context;
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
import com.example.rise_of_city.data.model.learning.quiz.BaseQuestion;
import com.example.rise_of_city.data.model.learning.quiz.ORDERING.WordOrderQuestion;
import com.example.rise_of_city.data.model.learning.quiz.ORDERING.SentenceOrderQuestion;
import com.example.rise_of_city.ui.lesson.LessonActivity;

public class OrderingFragment extends Fragment {

    private BaseQuestion question;
    private TextView tvScrambled;
    private EditText edtAnswer;
    private Button btnCheck;

    public static OrderingFragment newInstance(BaseQuestion q) {
        OrderingFragment fragment = new OrderingFragment();
        Bundle b = new Bundle();
        b.putSerializable("data", q);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ordering, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            question = (BaseQuestion) getArguments().getSerializable("data");
        }

        tvScrambled = view.findViewById(R.id.tvScrambled);
        edtAnswer = view.findViewById(R.id.edtAnswer);
        btnCheck = view.findViewById(R.id.btnCheck);

        if (question != null) {
            setupQuestion();
            showKeyboard();
        }

        btnCheck.setOnClickListener(v -> checkAnswer());
    }

    private void setupQuestion() {
        if (question instanceof WordOrderQuestion) {
            WordOrderQuestion q = (WordOrderQuestion) question;
            tvScrambled.setText(q.getScrambledLetters());

        } else if (question instanceof SentenceOrderQuestion) {
            SentenceOrderQuestion q = (SentenceOrderQuestion) question;
            // Sử dụng StringBuilder để tương thích với các phiên bản Android cũ (dưới API 26)
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < q.getWords().size(); i++) {
                sb.append(q.getWords().get(i));
                if (i < q.getWords().size() - 1) {
                    sb.append(" / ");
                }
            }
            tvScrambled.setText(sb.toString());
        }
    }

    /**
     * Normalize chuỗi để so sánh: lowercase, trim, xóa dấu cách thừa
     */
    private String normalizeString(String str) {
        if (str == null) return "";
        // Trim và lowercase
        str = str.trim().toLowerCase();
        // Xóa dấu cách thừa (thay nhiều dấu cách liên tiếp bằng 1 dấu cách)
        str = str.replaceAll("\\s+", " ");
        return str;
    }

    private void checkAnswer() {
        String user = edtAnswer.getText().toString();
        boolean isCorrect = false;

        if (question instanceof WordOrderQuestion) {
            WordOrderQuestion q = (WordOrderQuestion) question;
            String normalizedUser = normalizeString(user);
            String normalizedCorrect = normalizeString(q.getCorrectWord());
            isCorrect = normalizedUser.equals(normalizedCorrect);

        } else if (question instanceof SentenceOrderQuestion) {
            SentenceOrderQuestion q = (SentenceOrderQuestion) question;
            String normalizedUser = normalizeString(user);
            String normalizedCorrect = normalizeString(q.getCorrectSentence());
            isCorrect = normalizedUser.equals(normalizedCorrect);
        }

        LessonActivity activity = (LessonActivity) getActivity();
        if (activity == null) return;

        if (isCorrect) {
            btnCheck.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.green_correct));
            hideKeyboard();
            // Delay 500ms để người chơi kịp thấy phản hồi đúng
            new android.os.Handler().postDelayed(activity::handleCorrectAnswer, 500);
        } else {
            btnCheck.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.red_wrong));
            activity.handleWrongAnswer();
            Toast.makeText(getContext(), "Quy hoạch từ ngữ chưa chuẩn, hãy thử lại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showKeyboard() {
        edtAnswer.requestFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(edtAnswer, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(edtAnswer.getWindowToken(), 0);
        }
    }
}