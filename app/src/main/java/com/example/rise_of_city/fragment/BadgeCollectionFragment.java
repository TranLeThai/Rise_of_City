package com.example.rise_of_city.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.Badge;
import com.example.rise_of_city.ui.dialog.BadgeDetailDialogFragment;
import com.example.rise_of_city.utils.BadgeManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class BadgeCollectionFragment extends Fragment {
    private static final String TAG = "BadgeCollectionFragment";
    
    private ImageButton btnBack;
    private RecyclerView rvBadges;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    
    private BadgeManager badgeManager;
    private FirebaseAuth mAuth;
    private BadgeAdapter badgeAdapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_badge_collection, container, false);
        
        badgeManager = BadgeManager.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        initViews(view);
        setupClickListeners();
        loadBadges();
        
        return view;
    }
    
    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        rvBadges = view.findViewById(R.id.rvBadges);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        
        rvBadges.setLayoutManager(new GridLayoutManager(getContext(), 3));
        badgeAdapter = new BadgeAdapter(new ArrayList<>());
        rvBadges.setAdapter(badgeAdapter);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }
    
    private void loadBadges() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            showEmptyState("Vui lòng đăng nhập");
            return;
        }
        
        showLoading(true);
        hideEmptyState();
        
        badgeManager.getAllBadges(user.getUid(), badges -> {
            showLoading(false);
            if (badges.isEmpty()) {
                showEmptyState("Chưa có huy hiệu nào");
            } else {
                hideEmptyState();
            }
            badgeAdapter.updateBadges(badges);
        });
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    private void showEmptyState(String message) {
        if (tvEmptyState != null) {
            tvEmptyState.setText(message);
            tvEmptyState.setVisibility(View.VISIBLE);
        }
        if (rvBadges != null) {
            rvBadges.setVisibility(View.GONE);
        }
    }
    
    private void hideEmptyState() {
        if (tvEmptyState != null) {
            tvEmptyState.setVisibility(View.GONE);
        }
        if (rvBadges != null) {
            rvBadges.setVisibility(View.VISIBLE);
        }
    }
    
    // Badge Adapter
    private class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.ViewHolder> {
        private List<Badge> badges;
        
        public BadgeAdapter(List<Badge> badges) {
            this.badges = badges;
        }
        
        public void updateBadges(List<Badge> newBadges) {
            this.badges = newBadges;
            notifyDataSetChanged();
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_badge, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Badge badge = badges.get(position);
            holder.bind(badge, position);
        }
        
        @Override
        public int getItemCount() {
            return badges.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            private android.widget.ImageView ivBadge;
            private android.widget.ProgressBar progressBar;
            private View badgeContainer;
            
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivBadge = itemView.findViewById(R.id.ivBadge);
                progressBar = itemView.findViewById(R.id.progressBar);
                badgeContainer = itemView.findViewById(R.id.badgeContainer);
            }
            
            void bind(Badge badge, int position) {
                // Set icon based on unlock status
                if (badge.isUnlocked()) {
                    // Unlocked badge - use actual badge icon
                    ivBadge.setImageResource(badge.getIconResId());
                    ivBadge.setAlpha(1.0f);
                    badgeContainer.setAlpha(1.0f);
                    ivBadge.clearColorFilter();
                    
                    // Hide progress bar
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                } else {
                    // Locked badge - use locked icon
                    ivBadge.setImageResource(R.drawable.badge_locked);
                    ivBadge.setAlpha(0.5f);
                    badgeContainer.setAlpha(0.7f);
                    ivBadge.clearColorFilter();
                    
                    // Show progress if available
                    if (progressBar != null && badge.getProgress() > 0) {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setProgress(badge.getProgress());
                    } else if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
                
                // Set click listener to show detail dialog
                itemView.setOnClickListener(v -> {
                    BadgeDetailDialogFragment dialog = BadgeDetailDialogFragment.newInstance(
                        badge, 
                        badge.getCurrentValue()
                    );
                    Fragment fragment = BadgeCollectionFragment.this;
                    if (fragment.getParentFragment() != null) {
                        dialog.show(fragment.getParentFragment().getChildFragmentManager(), "BadgeDetailDialog");
                    } else if (fragment.getFragmentManager() != null) {
                        dialog.show(fragment.getFragmentManager(), "BadgeDetailDialog");
                    }
                });
            }
        }
    }
}
