package com.example.rise_of_city.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog Fragment cho chat với chef
 * Hiển thị đối thoại giữa user và chef character
 */
public class ChefChatDialogFragment extends DialogFragment {

    private RecyclerView rvChatMessages;
    private LinearLayout optionsContainer;
    private ChatAdapter chatAdapter;
    private List<ChatItem> chatItems;
    
    private int currentStep = 0;
    private Handler handler;
    
    // Conversation flow
    private String[] conversationSteps = {
        "greeting",           // Step 0: Chef greets
        "order_coffee",      // Step 1: User tries to order coffee
        "coffee_unavailable", // Step 2: Chef says no coffee
        "order_bread",       // Step 3: User orders bread
        "bread_clarification", // Step 4: Chef asks what kind
        "bread_wrong",       // Step 5: User says wrong thing
        "bread_correct",     // Step 6: User says correct thing
        "anything_else",    // Step 7: Chef asks anything else
        "order_cookies_wrong", // Step 8: User orders with wrong spelling
        "spelling_correction", // Step 9: Chef corrects spelling
        "order_cookies_correct", // Step 10: User orders correctly
        "final"              // Step 11: Final transaction
    };

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_chef_chat, container, false);
        
        rvChatMessages = view.findViewById(R.id.rv_chat_messages);
        optionsContainer = view.findViewById(R.id.options_container);
        
        chatItems = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatItems);
        rvChatMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChatMessages.setAdapter(chatAdapter);
        
        handler = new Handler(Looper.getMainLooper());
        
        // Start conversation
        startConversation();
        
        return view;
    }
    
    private void startConversation() {
        // Step 0: Show chef greeting and initial options
        // Show RecyclerView from the start
        rvChatMessages.setVisibility(View.VISIBLE);
        addChefMessage("Hello, How can I have you", "happy");
        showOptions(new String[]{"Coffee, pls", "Bread, pls"});
    }
    
    private void addChefMessage(String message, String expression) {
        ChatItem item = new ChatItem(message, true, expression);
        chatItems.add(item);
        chatAdapter.notifyItemInserted(chatItems.size() - 1);
        scrollToBottom();
    }
    
    private void addUserMessage(String message) {
        ChatItem item = new ChatItem(message, false, null);
        chatItems.add(item);
        chatAdapter.notifyItemInserted(chatItems.size() - 1);
        scrollToBottom();
    }
    
    private void showOptions(String[] options) {
        optionsContainer.removeAllViews();
        
        for (String option : options) {
            Button button = new Button(getContext());
            button.setText(option);
            // Red background for options buttons
            button.setBackgroundResource(R.drawable.bg_vocab_button);
            button.setTextColor(0xFFFFFFFF);
            button.setPadding(24, 16, 24, 16);
            button.setTextSize(14);
            button.setAllCaps(false);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 8, 8, 8);
            button.setLayoutParams(params);
            
            button.setOnClickListener(v -> {
                handleOptionClick(option);
            });
            
            optionsContainer.addView(button);
        }
    }
    
    private void handleOptionClick(String option) {
        // Hide options
        optionsContainer.removeAllViews();
        
        // Add user message
        addUserMessage(option);
        
        // Process based on current step and option
        handler.postDelayed(() -> {
            if (currentStep == 0) {
                // Initial greeting - user selects Coffee or Bread
                if (option.contains("Coffee") || option.contains("COFFEE")) {
                    // User wants coffee - show X mark and chef says no
                    addChefMessage("Sorry, we have no Coffee. Only bread, cake, cookie, pastry and Tiramisu", "sad");
                    currentStep = 2;
                    handler.postDelayed(() -> {
                        showOptions(new String[]{"[.........] white bread, please."});
                    }, 1500);
                } else if (option.contains("Bread") || option.contains("BREAD")) {
                    // User wants bread
                    addChefMessage("Sure. What kind of bread would you like?", "happy");
                    currentStep = 4;
                    handler.postDelayed(() -> {
                        showOptions(new String[]{"A white bread, please.", "A loaf of white bread, please."});
                    }, 1000);
                }
            } else if (currentStep == 2) {
                // After coffee unavailable, user orders bread
                addChefMessage("Sure. What kind of bread would you like?", "happy");
                currentStep = 4;
                handler.postDelayed(() -> {
                    showOptions(new String[]{"A white bread, please.", "A loaf of white bread, please."});
                }, 1000);
            } else if (currentStep == 4) {
                // User selects bread type
                if (option.contains("A white bread") && !option.contains("loaf")) {
                    // Wrong - just "A white bread, please."
                    addChefMessage("What do you mean, I can't understand", "confused");
                    handler.postDelayed(() -> {
                        showOptions(new String[]{"A loaf of white bread, please."});
                    }, 1000);
                } else if (option.contains("loaf")) {
                    // Correct - "A loaf of white bread, please."
                    addChefMessage("Yes", "happy");
                    currentStep = 7;
                    handler.postDelayed(() -> {
                        addChefMessage("Anything else?", "happy");
                        showOptions(new String[]{"Yes, I'd like two koocies.", "Yes, I'd like two cookies."});
                    }, 1000);
                }
            } else if (currentStep == 7) {
                // Chef asks "Anything else?"
                if (option.contains("koocies")) {
                    // Wrong spelling - show X mark
                    addChefMessage("It's not koocies, sir", "sad");
                    handler.postDelayed(() -> {
                        showOptions(new String[]{"Yes, I'd like two cookies."});
                    }, 1000);
                } else if (option.contains("cookies")) {
                    // Correct spelling
                    addChefMessage("Here you are. That's five dollars.", "happy");
                    currentStep = 11;
                }
            }
        }, 500);
    }
    
    private void scrollToBottom() {
        if (rvChatMessages != null && chatAdapter != null) {
            rvChatMessages.post(() -> {
                if (chatItems.size() > 0) {
                    rvChatMessages.smoothScrollToPosition(chatItems.size() - 1);
                }
            });
        }
    }
    
    // Chat Item Model
    private static class ChatItem {
        String message;
        boolean isChef;
        String chefExpression; // "happy", "sad", "confused", "normal"
        
        ChatItem(String message, boolean isChef, String chefExpression) {
            this.message = message;
            this.isChef = isChef;
            this.chefExpression = chefExpression;
        }
    }
    
    // Chat Adapter
    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
        private List<ChatItem> items;
        
        ChatAdapter(List<ChatItem> items) {
            this.items = items;
        }
        
        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chef_chat_message, parent, false);
            return new ChatViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            ChatItem item = items.get(position);
            
            if (item.isChef) {
                // Show chef message
                holder.cardChefMessage.setVisibility(View.VISIBLE);
                holder.cardUserMessage.setVisibility(View.GONE);
                holder.ivChefCharacter.setVisibility(View.VISIBLE);
                holder.tvChefMessage.setText(item.message);
                
                // Set chef expression
                if (item.chefExpression != null) {
                    switch (item.chefExpression) {
                        case "happy":
                            holder.ivChefCharacter.setImageResource(R.drawable.chef_happy);
                            break;
                        case "sad":
                            holder.ivChefCharacter.setImageResource(R.drawable.chef_sad);
                            break;
                        case "confused":
                            holder.ivChefCharacter.setImageResource(R.drawable.chef_normal);
                            break;
                        default:
                            holder.ivChefCharacter.setImageResource(R.drawable.chef_normal);
                    }
                } else {
                    holder.ivChefCharacter.setImageResource(R.drawable.chef_normal);
                }
            } else {
                // Show user message
                holder.cardChefMessage.setVisibility(View.GONE);
                holder.cardUserMessage.setVisibility(View.VISIBLE);
                holder.ivChefCharacter.setVisibility(View.GONE);
                holder.tvUserMessage.setText(item.message);
            }
        }
        
        @Override
        public int getItemCount() {
            return items.size();
        }
        
        class ChatViewHolder extends RecyclerView.ViewHolder {
            ImageView ivChefCharacter;
            MaterialCardView cardChefMessage;
            MaterialCardView cardUserMessage;
            TextView tvChefMessage;
            TextView tvUserMessage;
            
            ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                ivChefCharacter = itemView.findViewById(R.id.iv_chef_character);
                cardChefMessage = itemView.findViewById(R.id.card_chef_message);
                cardUserMessage = itemView.findViewById(R.id.card_user_message);
                tvChefMessage = itemView.findViewById(R.id.tv_chef_message);
                tvUserMessage = itemView.findViewById(R.id.tv_user_message);
            }
        }
    }
}

