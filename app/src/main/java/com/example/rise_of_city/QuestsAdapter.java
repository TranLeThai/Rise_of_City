package com.example.rise_of_city;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuestsAdapter extends RecyclerView.Adapter<QuestsAdapter.QuestViewHolder> {

    private List<Quest> questList;

    public QuestsAdapter(List<Quest> questList) {
        this.questList = questList;
    }

    @NonNull
    @Override
    public QuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_quest, parent, false);
        return new QuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestViewHolder holder, int position) {
        Quest quest = questList.get(position);
        holder.questName.setText(quest.getName());
        holder.rewards.setText("+" + quest.getGoldReward());
        holder.rewardsXp.setText("+" + quest.getXpReward());
        holder.questIcon.setImageResource(quest.getIconResId());

        String progressText = quest.getProgress() + "/" + quest.getMaxProgress();
        holder.progressText.setText(progressText);
    }

    @Override
    public int getItemCount() {
        return questList.size();
    }

    static class QuestViewHolder extends RecyclerView.ViewHolder {
        ImageView questIcon;
        TextView questName;
        TextView rewards;
        TextView rewardsXp;
        TextView progressText;
        ImageButton goButton;

        public QuestViewHolder(@NonNull View itemView) {
            super(itemView);
            questIcon = itemView.findViewById(R.id.imageView_quest_icon);
            questName = itemView.findViewById(R.id.textView_quest_name);
            rewards = itemView.findViewById(R.id.textView_rewards);
            rewardsXp = itemView.findViewById(R.id.textView_rewards_xp);
            progressText = itemView.findViewById(R.id.textView_progress_text);
            goButton = itemView.findViewById(R.id.button_go);
        }
    }
}
