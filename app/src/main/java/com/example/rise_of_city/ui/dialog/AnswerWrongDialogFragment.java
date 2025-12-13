package com.example.rise_of_city.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.rise_of_city.R;

public class AnswerWrongDialogFragment extends DialogFragment {

    private String correctAnswer;
    private String correctAnswerVietnamese;

    public interface OnUnderstoodClickListener {
        void onUnderstoodClick();
    }

    private OnUnderstoodClickListener onUnderstoodClickListener;

    public static AnswerWrongDialogFragment newInstance(String correctAnswer, String correctAnswerVietnamese) {
        AnswerWrongDialogFragment fragment = new AnswerWrongDialogFragment();
        Bundle args = new Bundle();
        args.putString("correctAnswer", correctAnswer);
        args.putString("correctAnswerVietnamese", correctAnswerVietnamese);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            correctAnswer = getArguments().getString("correctAnswer", "");
            correctAnswerVietnamese = getArguments().getString("correctAnswerVietnamese", "");
        }
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_answer_wrong, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvCorrectAnswer = view.findViewById(R.id.tv_correct_answer);
        Button btnUnderstood = view.findViewById(R.id.btn_understood);

        // Set đáp án đúng
        String answerText = correctAnswer;
        if (correctAnswerVietnamese != null && !correctAnswerVietnamese.isEmpty()) {
            answerText = correctAnswer + " (" + correctAnswerVietnamese + ")";
        }
        tvCorrectAnswer.setText(answerText);

        btnUnderstood.setOnClickListener(v -> {
            if (onUnderstoodClickListener != null) {
                onUnderstoodClickListener.onUnderstoodClick();
            }
            dismiss();
        });
    }

    public void setOnUnderstoodClickListener(OnUnderstoodClickListener listener) {
        this.onUnderstoodClickListener = listener;
    }
}
