package com.example.rise_of_city.ui.quiz_fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {
    private List<Lesson> lessons;
    private OnLessonClickListener listener;

    public interface OnLessonClickListener {
        void onLessonClick(Lesson lesson);
    }

    public LessonAdapter(List<Lesson> lessons, OnLessonClickListener listener) {
        this.lessons = lessons != null ? lessons : new ArrayList<>();
        this.listener = listener;
    }

    public void updateLessons(List<Lesson> newLessons) {
        this.lessons = newLessons != null ? newLessons : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessons.get(position);
        holder.bind(lesson);
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    class LessonViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvAuthor;
        TextView tvDate;
        TextView tvViews;
        TextView tvLevel;

        LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_lesson_title);
            tvAuthor = itemView.findViewById(R.id.tv_lesson_author);
            tvDate = itemView.findViewById(R.id.tv_lesson_date);
            tvViews = itemView.findViewById(R.id.tv_lesson_views);
            tvLevel = itemView.findViewById(R.id.tv_lesson_level);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onLessonClick(lessons.get(position));
                }
            });
        }

        void bind(Lesson lesson) {
            tvTitle.setText(lesson.getTitle());
            tvAuthor.setText("Bởi: " + (lesson.getAuthorName() != null ? lesson.getAuthorName() : "Ẩn danh"));
            
            // Format date
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String dateStr = sdf.format(new Date(lesson.getCreatedAt()));
            tvDate.setText(dateStr);
            
            tvViews.setText(lesson.getViewCount() + " lượt xem");
            
            if (lesson.getLevel() != null) {
                tvLevel.setText(lesson.getLevel());
                tvLevel.setVisibility(View.VISIBLE);
            } else {
                tvLevel.setVisibility(View.GONE);
            }
        }
    }
}

