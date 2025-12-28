package com.example.rise_of_city.ui.assessment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.local.SurveyAnswer;
import com.example.rise_of_city.ui.main.MainActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class SurveyResultFragment extends Fragment {

    private List<SurveyAnswer> surveyAnswers;
    private int correctCount;
    private int totalCount;
    private RecyclerView recyclerView;
    private TextView tvSummary;
    private Button btnFinish;
    private Gson gson = new Gson();

    public static SurveyResultFragment newInstance(List<SurveyAnswer> answers, int correctCount, int totalCount) {
        SurveyResultFragment fragment = new SurveyResultFragment();
        Bundle args = new Bundle();
        args.putSerializable("answers", new java.util.ArrayList<>(answers));
        args.putInt("correctCount", correctCount);
        args.putInt("totalCount", totalCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            surveyAnswers = (List<SurveyAnswer>) getArguments().getSerializable("answers");
            correctCount = getArguments().getInt("correctCount", 0);
            totalCount = getArguments().getInt("totalCount", 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_survey_result, container, false);
        
        tvSummary = view.findViewById(R.id.tvSummary);
        recyclerView = view.findViewById(R.id.recyclerViewResults);
        btnFinish = view.findViewById(R.id.btnFinish);

        setupRecyclerView();
        setupSummary();
        setupFinishButton();

        return view;
    }

    private void setupSummary() {
        String summary = String.format("You got %d out of %d questions correct (%.0f%%)", 
            correctCount, totalCount, (correctCount * 100.0 / totalCount));
        tvSummary.setText(summary);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        SurveyResultAdapter adapter = new SurveyResultAdapter(surveyAnswers, gson);
        recyclerView.setAdapter(adapter);
    }

    private void setupFinishButton() {
        btnFinish.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra("SHOW_SURVEY_DIALOG", true);
            intent.putExtra("SCORE", correctCount);
            intent.putExtra("TOTAL", totalCount);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private static class SurveyResultAdapter extends RecyclerView.Adapter<SurveyResultAdapter.ViewHolder> {
        private List<SurveyAnswer> answers;
        private Gson gson;
        private Type listType = new TypeToken<List<String>>() {}.getType();

        public SurveyResultAdapter(List<SurveyAnswer> answers, Gson gson) {
            this.answers = answers;
            this.gson = gson;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_survey_result, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SurveyAnswer answer = answers.get(position);
            holder.tvQuestionTitle.setText((position + 1) + ". " + answer.questionTitle);
            
            // Parse user answer
            String userAnswerStr = "";
            if (answer.questionType.equals("FIND_ERROR") || answer.questionType.equals("EDUCATION_LEVEL")) {
                List<String> userAnswers = gson.fromJson(answer.userAnswer, listType);
                userAnswerStr = String.join(", ", userAnswers);
            } else {
                userAnswerStr = answer.userAnswer.replace("\"", "");
            }
            
            holder.tvUserAnswer.setText("Your answer: " + userAnswerStr);
            
            // Show correct/incorrect status
            if (answer.isCorrect) {
                holder.tvStatus.setText("✓ Correct");
                holder.tvStatus.setTextColor(0xFF4CAF50); // Green
            } else {
                holder.tvStatus.setText("✗ Incorrect");
                holder.tvStatus.setTextColor(0xFFF44336); // Red
            }
        }

        @Override
        public int getItemCount() {
            return answers != null ? answers.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvQuestionTitle;
            TextView tvUserAnswer;
            TextView tvStatus;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvQuestionTitle = itemView.findViewById(R.id.tvQuestionTitle);
                tvUserAnswer = itemView.findViewById(R.id.tvUserAnswer);
                tvStatus = itemView.findViewById(R.id.tvStatus);
            }
        }
    }
}

