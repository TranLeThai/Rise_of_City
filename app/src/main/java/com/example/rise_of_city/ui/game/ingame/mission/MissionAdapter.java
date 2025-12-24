package com.example.rise_of_city.ui.dialog;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.game.Mission;
import java.util.List;

public class MissionAdapter extends RecyclerView.Adapter<MissionAdapter.ViewHolder> {

    private List<Mission> missionList;
    private OnMissionClickListener listener;
    private Handler handler = new Handler(Looper.getMainLooper());

    public interface OnMissionClickListener {
        void onGoToClick(Mission mission);
    }

    public MissionAdapter(List<Mission> missionList, OnMissionClickListener listener) {
        this.missionList = missionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mission, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mission mission = missionList.get(position);
        holder.tvTitle.setText(mission.title);
        holder.tvBuilding.setText("Địa điểm: " + mission.buildingId);

        // Cập nhật đếm ngược mỗi giây
        Runnable updateTimer = new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                long timeLeft = mission.durationMs - (now - mission.startTime);

                if (timeLeft > 0) {
                    long h = timeLeft / 3600000;
                    long m = (timeLeft % 3600000) / 60000;
                    long s = (timeLeft % 60000) / 1000;
                    holder.tvTime.setText(String.format("Hạn chót: %02d:%02d:%02d", h, m, s));
                    handler.postDelayed(this, 1000);
                } else {
                    holder.tvTime.setText("ĐÃ QUÁ HẠN! (Sẽ bị phạt tiền)");
                    holder.tvTime.setTextColor(Color.BLACK);
                }
            }
        };
        handler.post(updateTimer);

        holder.btnGo.setOnClickListener(v -> listener.onGoToClick(mission));
    }

    @Override
    public int getItemCount() { return missionList != null ? missionList.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvBuilding, tvTime;
        Button btnGo;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvMissionTitle);
            tvBuilding = itemView.findViewById(R.id.tvTargetBuilding);
            tvTime = itemView.findViewById(R.id.tvTimeLeft);
            btnGo = itemView.findViewById(R.id.btnGoToMission);
        }
    }
}