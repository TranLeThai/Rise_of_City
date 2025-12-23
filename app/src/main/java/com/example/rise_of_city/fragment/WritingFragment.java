package com.example.rise_of_city.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;

/**
 * Fragment cho màn hình Writing Practice
 * Người dùng có thể nhập câu trả lời vào 3 input fields
 */
public class WritingFragment extends Fragment {

    private EditText etAnswer1, etAnswer2, etAnswer3;
    private Button btnNext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_writing, container, false);

        etAnswer1 = view.findViewById(R.id.et_answer1);
        etAnswer2 = view.findViewById(R.id.et_answer2);
        etAnswer3 = view.findViewById(R.id.et_answer3);
        btnNext = view.findViewById(R.id.btn_next);

        // Focus vào input đầu tiên khi mở
        etAnswer1.requestFocus();

        // Next button - Navigate to Matching screen
        btnNext.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.rise_of_city.ui.main.MainActivity) {
                MatchingFragment matchingFragment = new MatchingFragment();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, matchingFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    /**
     * Lấy các câu trả lời từ input fields
     */
    public String[] getAnswers() {
        return new String[]{
            etAnswer1.getText().toString().trim(),
            etAnswer2.getText().toString().trim(),
            etAnswer3.getText().toString().trim()
        };
    }

    /**
     * Clear tất cả input fields
     */
    public void clearAnswers() {
        if (etAnswer1 != null) etAnswer1.setText("");
        if (etAnswer2 != null) etAnswer2.setText("");
        if (etAnswer3 != null) etAnswer3.setText("");
    }
}
