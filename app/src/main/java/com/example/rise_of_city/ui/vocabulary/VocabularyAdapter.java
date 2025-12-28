package com.example.rise_of_city.ui.vocabulary;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;

import java.util.List;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.ViewHolder> {
    
    private List<VocabularyItem> vocabularyList;
    private Context context;
    
    public VocabularyAdapter(List<VocabularyItem> vocabularyList, Context context) {
        this.vocabularyList = vocabularyList;
        this.context = context;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vocabulary, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VocabularyItem item = vocabularyList.get(position);
        
        // Set English word
        holder.tvEnglish.setText(item.getEnglish());
        
        // Set Vietnamese translation
        if (item.getVietnamese() != null && !item.getVietnamese().isEmpty()) {
            holder.tvVietnamese.setText(item.getVietnamese());
            holder.tvVietnamese.setVisibility(View.VISIBLE);
        } else {
            holder.tvVietnamese.setVisibility(View.GONE);
        }
        
        // Set image if available
        if (item.hasImage() && item.getImageResourceName() != null) {
            String imageName = item.getImageResourceName();
            // Đảm bảo tên hình ảnh không có extension và không có ký tự đặc biệt
            imageName = imageName.replace(".png", "").replace(".jpg", "").replace(".jpeg", "");
            imageName = imageName.trim();
            
            int imageResId = getDrawableId(imageName);
            if (imageResId != 0) {
                holder.ivVocabulary.setImageResource(imageResId);
                holder.ivVocabulary.setVisibility(View.VISIBLE);
            } else {
                // Log để debug nếu không tìm thấy hình ảnh
                Log.w("VocabularyAdapter", "Image not found: " + imageName + " (original: " + item.getImageResourceName() + ")");
                holder.ivVocabulary.setVisibility(View.GONE);
            }
        } else {
            holder.ivVocabulary.setVisibility(View.GONE);
        }
    }
    
    @Override
    public int getItemCount() {
        return vocabularyList.size();
    }
    
    private int getDrawableId(String name) {
        try {
            if (name == null || name.isEmpty()) {
                return 0;
            }
            // Loại bỏ extension nếu có
            String cleanName = name;
            if (cleanName.contains(".")) {
                cleanName = cleanName.substring(0, cleanName.lastIndexOf("."));
            }
            // Loại bỏ khoảng trắng và ký tự đặc biệt
            cleanName = cleanName.trim().replace(" ", "_").replace("-", "_");
            
            int resId = context.getResources().getIdentifier(
                cleanName, "drawable", context.getPackageName()
            );
            
            if (resId == 0) {
                Log.w("VocabularyAdapter", "Drawable not found: " + cleanName + " (original: " + name + ")");
            }
            
            return resId;
        } catch (Exception e) {
            Log.e("VocabularyAdapter", "Error getting drawable for: " + name, e);
            return 0;
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivVocabulary;
        TextView tvEnglish;
        TextView tvVietnamese;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivVocabulary = itemView.findViewById(R.id.iv_vocabulary);
            tvEnglish = itemView.findViewById(R.id.tv_english);
            tvVietnamese = itemView.findViewById(R.id.tv_vietnamese);
        }
    }
}

