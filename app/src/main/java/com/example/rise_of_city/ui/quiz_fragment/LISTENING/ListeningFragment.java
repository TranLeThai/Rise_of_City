package com.example.rise_of_city.ui.quiz_fragment.LISTENING;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.learning.quiz.LISTENING.ListeningQuestion;
import com.example.rise_of_city.ui.lesson.LessonActivity;

public class ListeningFragment extends Fragment {

    private ListeningQuestion question;
    private ImageButton btnPlayAudio;
    private TextView tvTranscript;
    private Button[] optionButtons;
    private MediaPlayer mediaPlayer;

    public static ListeningFragment newInstance(ListeningQuestion question) {
        ListeningFragment fragment = new ListeningFragment();
        Bundle args = new Bundle();
        args.putSerializable("data", question);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_listening, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            question = (ListeningQuestion) getArguments().getSerializable("data");
        }

        btnPlayAudio = view.findViewById(R.id.btnPlayAudio);
        tvTranscript = view.findViewById(R.id.tvTranscript);
        optionButtons = new Button[]{
                view.findViewById(R.id.btnOpt1), view.findViewById(R.id.btnOpt2),
                view.findViewById(R.id.btnOpt3), view.findViewById(R.id.btnOpt4)
        };

        if (question != null) {
            setupUI();
        }
    }

    private void setupUI() {
        if (btnPlayAudio == null || question == null) {
            Log.e("ListeningFragment", "Views or question not initialized");
            return;
        }

        // 1. Cài đặt phát âm thanh
        btnPlayAudio.setOnClickListener(v -> playAudio());

        // 2. Hiển thị nội dung câu hỏi nếu có
        TextView tvContent = getView().findViewById(R.id.tvContent);
        if (tvContent != null && question.getContent() != null) {
            tvContent.setText(question.getContent());
        }

        // 3. Hiển thị 4 lựa chọn
        if (optionButtons != null && question.getOptions() != null) {
            for (int i = 0; i < optionButtons.length; i++) {
                if (i < question.getOptions().size()) {
                    optionButtons[i].setText(question.getOptions().get(i));
                    optionButtons[i].setVisibility(View.VISIBLE);
                    final int index = i;
                    optionButtons[i].setOnClickListener(v -> checkAnswer(index));
                } else {
                    optionButtons[i].setVisibility(View.GONE);
                }
            }
        }
    }

    private void playAudio() {
        if (question == null || question.getAudioPath() == null || question.getAudioPath().isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy file âm thanh!", Toast.LENGTH_SHORT).show();
            Log.e("ListeningFragment", "Audio path is null or empty");
            return;
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        try {
            // Lấy Resource ID từ tên file trong raw
            // Loại bỏ extension nếu có (ví dụ: "audio.mp3" -> "audio")
            String audioName = question.getAudioPath();
            if (audioName.contains(".")) {
                audioName = audioName.substring(0, audioName.lastIndexOf("."));
            }

            int resId = requireContext().getResources().getIdentifier(
                    audioName, "raw", requireContext().getPackageName());

            Log.d("ListeningFragment", "Audio path: " + question.getAudioPath() + ", Audio name: " + audioName + ", ResId: " + resId);

            if (resId != 0) {
                mediaPlayer = MediaPlayer.create(requireContext(), resId);
                if (mediaPlayer != null) {
                    mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                        Log.e("ListeningFragment", "MediaPlayer error: what=" + what + ", extra=" + extra);
                        Toast.makeText(getContext(), "Lỗi phát âm thanh!", Toast.LENGTH_SHORT).show();
                        return true;
                    });
                    mediaPlayer.setOnCompletionListener(mp -> {
                        Log.d("ListeningFragment", "Audio playback completed");
                    });
                    mediaPlayer.start();
                    Log.d("ListeningFragment", "Audio playback started");
                } else {
                    Toast.makeText(getContext(), "Không thể tạo MediaPlayer!", Toast.LENGTH_SHORT).show();
                    Log.e("ListeningFragment", "MediaPlayer.create returned null");
                }
            } else {
                Toast.makeText(getContext(), "Không tìm thấy file âm thanh: " + audioName, Toast.LENGTH_SHORT).show();
                Log.e("ListeningFragment", "Resource ID is 0 for audio: " + audioName);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi khi phát âm thanh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ListeningFragment", "Error playing audio", e);
        }
    }

    private void checkAnswer(int selectedIndex) {
        LessonActivity activity = (LessonActivity) getActivity();
        if (activity == null || question == null) return;

        if (selectedIndex == question.getCorrectAnswerIndex()) {
            optionButtons[selectedIndex].setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.green_correct));

            // Hiện Transcript sau khi trả lời đúng
            tvTranscript.setVisibility(View.VISIBLE);
            tvTranscript.setText("Nội dung: " + question.getFulltranscript());

            // Delay 1.5s để người chơi kịp nghe/đọc transcript
            new android.os.Handler().postDelayed(activity::handleCorrectAnswer, 1500);
        } else {
            optionButtons[selectedIndex].setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.red_wrong));
            activity.handleWrongAnswer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}