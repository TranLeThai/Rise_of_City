package com.example.rise_of_city.ui.quest;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.Quest;
import com.example.rise_of_city.data.repository.QuestRepository;
import com.example.rise_of_city.ui.ingame.InGameActivity;

import java.util.List;

public class QuestsAdapter extends RecyclerView.Adapter<QuestsAdapter.QuestViewHolder> {

    private static final String TAG = "QuestsAdapter";
    private List<Quest> questList;
    private QuestsActivity activity;
    private QuestRepository questRepository;

    public QuestsAdapter(List<Quest> questList, QuestsActivity activity) {
        this.questList = questList;
        this.activity = activity;
        this.questRepository = QuestRepository.getInstance();
    }

    public void updateQuests(List<Quest> newQuests) {
        this.questList = newQuests;
        notifyDataSetChanged();
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
        
        // Set icon
        if (quest.getIconResId() != 0) {
            holder.questIcon.setImageResource(quest.getIconResId());
        }

        // Set progress
        String progressText = quest.getProgress() + "/" + quest.getMaxProgress();
        holder.progressText.setText(progressText);
        
        // Handle button click
        if (quest.isCompleted() && !quest.isClaimed()) {
            // Quest completed but not claimed - show claim button
            holder.goButton.setEnabled(true);
            holder.goButton.setAlpha(1.0f);
            holder.goButton.setOnClickListener(v -> claimQuestReward(quest, holder.getAdapterPosition()));
        } else if (quest.isCompleted() && quest.isClaimed()) {
            // Quest completed and claimed - disable button
            holder.goButton.setEnabled(false);
            holder.goButton.setAlpha(0.5f);
            holder.goButton.setOnClickListener(null);
        } else {
            // Quest not completed - navigate to quest action
            holder.goButton.setEnabled(true);
            holder.goButton.setAlpha(1.0f);
            holder.goButton.setOnClickListener(v -> navigateToQuestAction(quest));
        }
    }

    @Override
    public int getItemCount() {
        return questList != null ? questList.size() : 0;
    }
    
    /**
     * Claim quest reward
     */
    private void claimQuestReward(Quest quest, int position) {
        questRepository.claimQuestReward(quest.getId(), new QuestRepository.OnQuestRewardClaimedListener() {
            @Override
            public void onRewardClaimed(String questId, int goldReward, int xpReward) {
                Toast.makeText(activity, 
                    "Nhận được " + goldReward + " vàng và " + xpReward + " XP!", 
                    Toast.LENGTH_SHORT).show();
                
                // Update quest in list
                quest.setClaimed(true);
                notifyItemChanged(position);
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(activity, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error claiming quest reward: " + error);
            }
        });
    }
    
    /**
     * Navigate to quest action (game, quiz, friends, etc.)
     */
    private void navigateToQuestAction(Quest quest) {
        String actionType = quest.getActionType();
        String quizType = quest.getQuizType();
        String lessonName = quest.getLessonName();
        
        if (actionType == null || actionType.isEmpty()) {
            // Default action: navigate to game
            Intent intent = new Intent(activity, InGameActivity.class);
            activity.startActivity(intent);
            return;
        }
        
        switch (actionType) {
            case "navigate_to_game":
                Intent gameIntent = new Intent(activity, InGameActivity.class);
                activity.startActivity(gameIntent);
                break;
            case "navigate_to_friends":
                // TODO: Navigate to friends screen
                Toast.makeText(activity, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
                break;
            case "navigate_to_quiz":
                // Navigate to quiz dựa trên quizType
                navigateToQuizByType(quizType, lessonName, quest.getTargetBuildingId());
                break;
            case "navigate_to_lesson":
                // Navigate to grammar quiz (KHÔNG phải lesson screen, mà là quiz)
                // Convert lessonName to topicId và navigate đến grammar quiz
                if (lessonName != null && !lessonName.isEmpty()) {
                    String topicId = lessonName.toLowerCase()
                        .replace(" ", "_")
                        .replace("ì", "i")
                        .replace("à", "a")
                        .replace("á", "a")
                        .replace("ả", "a")
                        .replace("ã", "a")
                        .replace("ạ", "a")
                        .replace("đ", "d")
                        .replace("ê", "e")
                        .replace("ế", "e")
                        .replace("ề", "e")
                        .replace("ể", "e")
                        .replace("ễ", "e")
                        .replace("ệ", "e")
                        .replace("ô", "o")
                        .replace("ố", "o")
                        .replace("ồ", "o")
                        .replace("ổ", "o")
                        .replace("ỗ", "o")
                        .replace("ộ", "o")
                        .replace("ơ", "o")
                        .replace("ớ", "o")
                        .replace("ờ", "o")
                        .replace("ở", "o")
                        .replace("ỡ", "o")
                        .replace("ợ", "o")
                        .replace("ư", "u")
                        .replace("ứ", "u")
                        .replace("ừ", "u")
                        .replace("ử", "u")
                        .replace("ữ", "u")
                        .replace("ự", "u");
                    
                    // Navigate đến grammar quiz
                    Intent intent = new Intent(activity, com.example.rise_of_city.ui.quiz.VocabularyQuizActivity.class);
                    intent.putExtra("quizType", "grammar");
                    intent.putExtra("topicId", topicId);
                    intent.putExtra("isMission", true);
                    activity.startActivity(intent);
                } else {
                    Toast.makeText(activity, "Không tìm thấy bài học", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                // Default: navigate to game
                Intent defaultIntent = new Intent(activity, InGameActivity.class);
                activity.startActivity(defaultIntent);
                break;
        }
    }
    
    /**
     * Navigate to quiz dựa trên quiz type
     */
    private void navigateToQuizByType(String quizType, String lessonName, String targetBuildingId) {
        Intent intent;
        
        if (quizType == null || quizType.isEmpty()) {
            quizType = "vocabulary"; // Default
        }
        
        switch (quizType) {
            case "vocabulary":
                // Quiz từ vựng - dùng VocabularyQuizActivity
                intent = new Intent(activity, com.example.rise_of_city.ui.quiz.VocabularyQuizActivity.class);
                if (targetBuildingId != null && !targetBuildingId.isEmpty()) {
                    intent.putExtra("buildingId", targetBuildingId);
                }
                intent.putExtra("isMission", true); // Quest có bonus reward
                activity.startActivity(intent);
                break;
                
            case "grammar":
                // Quiz ngữ pháp - navigate đến GrammarQuizActivity (CHỈ dùng cho quest, KHÔNG phải vocabulary)
                // Convert lessonName to topicId: "Thì hiện tại đơn" -> "thi_hien_tai_don"
                if (lessonName != null && !lessonName.isEmpty()) {
                    String topicId = lessonName.toLowerCase()
                        .replace(" ", "_")
                        .replace("ì", "i")
                        .replace("à", "a")
                        .replace("á", "a")
                        .replace("ả", "a")
                        .replace("ã", "a")
                        .replace("ạ", "a")
                        .replace("đ", "d")
                        .replace("ê", "e")
                        .replace("ế", "e")
                        .replace("ề", "e")
                        .replace("ể", "e")
                        .replace("ễ", "e")
                        .replace("ệ", "e")
                        .replace("ô", "o")
                        .replace("ố", "o")
                        .replace("ồ", "o")
                        .replace("ổ", "o")
                        .replace("ỗ", "o")
                        .replace("ộ", "o")
                        .replace("ơ", "o")
                        .replace("ớ", "o")
                        .replace("ờ", "o")
                        .replace("ở", "o")
                        .replace("ỡ", "o")
                        .replace("ợ", "o")
                        .replace("ư", "u")
                        .replace("ứ", "u")
                        .replace("ừ", "u")
                        .replace("ử", "u")
                        .replace("ữ", "u")
                        .replace("ự", "u");
                    
                    // Tạm thời dùng VocabularyQuizActivity với flag grammar và topicId
                    // TODO: Tạo GrammarQuizActivity riêng nếu cần
                    intent = new Intent(activity, com.example.rise_of_city.ui.quiz.VocabularyQuizActivity.class);
                    intent.putExtra("quizType", "grammar");
                    intent.putExtra("topicId", topicId);
                    intent.putExtra("isMission", true);
                    activity.startActivity(intent);
                } else {
                    Toast.makeText(activity, "Không tìm thấy bài học ngữ pháp", Toast.LENGTH_SHORT).show();
                }
                break;
                
            case "reading":
                // Quiz đọc hiểu - tạm thời dùng VocabularyQuizActivity
                // TODO: Tạo ReadingQuizActivity riêng nếu cần
                intent = new Intent(activity, com.example.rise_of_city.ui.quiz.VocabularyQuizActivity.class);
                intent.putExtra("quizType", "reading");
                intent.putExtra("isMission", true);
                activity.startActivity(intent);
                break;
                
            case "writing":
                // Quiz điền từ (Writing quiz)
                intent = new Intent(activity, com.example.rise_of_city.ui.quiz.VocabularyQuizActivity.class);
                intent.putExtra("quizType", "writing");
                intent.putExtra("isMission", true);
                activity.startActivity(intent);
                break;
                
            case "sentence_completion":
                // Quiz hoàn thành câu
                intent = new Intent(activity, com.example.rise_of_city.ui.quiz.VocabularyQuizActivity.class);
                intent.putExtra("quizType", "sentence_completion");
                intent.putExtra("isMission", true);
                activity.startActivity(intent);
                break;
                
            case "word_order":
                // Quiz sắp xếp từ
                intent = new Intent(activity, com.example.rise_of_city.ui.quiz.VocabularyQuizActivity.class);
                intent.putExtra("quizType", "word_order");
                intent.putExtra("isMission", true);
                activity.startActivity(intent);
                break;
                
            case "synonym_antonym":
                // Quiz từ đồng nghĩa/trái nghĩa
                intent = new Intent(activity, com.example.rise_of_city.ui.quiz.VocabularyQuizActivity.class);
                intent.putExtra("quizType", "synonym_antonym");
                intent.putExtra("isMission", true);
                activity.startActivity(intent);
                break;
                
            default:
                // Default: vocabulary quiz
                intent = new Intent(activity, com.example.rise_of_city.ui.quiz.VocabularyQuizActivity.class);
                if (targetBuildingId != null && !targetBuildingId.isEmpty()) {
                    intent.putExtra("buildingId", targetBuildingId);
                }
                intent.putExtra("isMission", true);
                activity.startActivity(intent);
                break;
        }
    }

    class QuestViewHolder extends RecyclerView.ViewHolder {
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
