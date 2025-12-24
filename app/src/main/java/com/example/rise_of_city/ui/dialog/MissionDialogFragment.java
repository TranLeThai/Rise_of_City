package com.example.rise_of_city.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.game.Mission;

public class MissionDialogFragment extends DialogFragment {

    private Mission mission;
    private OnAcceptClickListener onAcceptClickListener;
    private OnDenyClickListener onDenyClickListener;

    public interface OnAcceptClickListener {
        void onAcceptClick(Mission mission);
    }

    public interface OnDenyClickListener {
        void onDenyClick(Mission mission);
    }

    // Cập nhật: Nhận trực tiếp đối tượng Mission để lấy dữ liệu phạt/thưởng
    public static MissionDialogFragment newInstance(Mission mission) {
        MissionDialogFragment fragment = new MissionDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("mission_data", mission);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onContextItemSelected(null);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mission = (Mission) getArguments().getSerializable("mission_data");
        }
        // Thiết lập theme full screen hoặc không khung
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
        return inflater.inflate(R.layout.dialog_mission, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        TextView tvMissionTitle = view.findViewById(R.id.tv_mission_title);
        TextView tvMissionText = view.findViewById(R.id.tv_mission_text);
        Button btnAccept = view.findViewById(R.id.btn_accept);
        Button btnDeny = view.findViewById(R.id.btn_deny);

        if (mission != null) {
            // Hiển thị thông báo sự cố khẩn cấp
            tvMissionTitle.setText("⚠️ SỰ CỐ: " + mission.title);

            String detailText = "Một sự cố vừa xảy ra tại công trình này!\n\n" +
                    "• Yêu cầu: Ôn tập kiến thức để giải quyết.\n" +
                    "• Thời hạn: 12 tiếng.\n" +
                    "• Phần thưởng: " + mission.goldReward + " Vàng.\n" +
                    "• Hình phạt: Trừ " + mission.goldPenalty + " Vàng nếu quá hạn.";
            tvMissionText.setText(detailText);
        }

        // Đóng thông báo
        btnBack.setOnClickListener(v -> dismiss());

        // Chấp nhận làm nhiệm vụ ôn bài
        btnAccept.setOnClickListener(v -> {
            if (onAcceptClickListener != null) {
                onAcceptClickListener.onAcceptClick(mission);
            }
            dismiss();
        });

        // Từ chối (Bỏ qua sự cố nhưng vẫn tốn 12h để tự phục hồi hoặc chờ phạt)
        btnDeny.setOnClickListener(v -> {
            if (onDenyClickListener != null) {
                onDenyClickListener.onDenyClick(mission);
            }
            dismiss();
        });
    }

    public void setOnAcceptClickListener(OnAcceptClickListener listener) {
        this.onAcceptClickListener = listener;
    }

    public void setOnDenyClickListener(OnDenyClickListener listener) {
        this.onDenyClickListener = listener;
    }
}