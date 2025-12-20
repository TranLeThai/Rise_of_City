package com.example.rise_of_city.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rise_of_city.R;
import com.example.rise_of_city.ui.dialog.PauseSettingsDialogFragment;
import com.example.rise_of_city.fragment.WritingFragment;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment tương tác với chef - nâng cao từ bài tập điền vào chỗ trống
 */
public class NewScreenFragment extends Fragment {

    private RecyclerView rvChat;
    private LinearLayout llOptionsContainer;
    private Button btnContinue;
    private Button btnNext;
    private ImageView ivChef;
    private android.widget.ImageButton btnPause;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    
    private int currentStep = 0;
    private String selectedBreadOption = "";
    private String selectedCookieOption = "";
    private Button btnCoffee, btnBread; // Keep references for error handling

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_screen, container, false);

        rvChat = view.findViewById(R.id.rv_chat);
        llOptionsContainer = view.findViewById(R.id.ll_options_container);
        btnContinue = view.findViewById(R.id.btn_continue);
        btnNext = view.findViewById(R.id.btn_next);
        ivChef = view.findViewById(R.id.iv_chef);
        btnPause = view.findViewById(R.id.btn_pause);

        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);
        rvChat.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChat.setAdapter(chatAdapter);

        // Set initial chef expression (happy)
        setChefExpression(true);
        
        // Add Next button to options container (always visible)
        addNextButtonToContainer();
        
        // Start conversation
        startConversation();

        // Pause button - Open Settings dialog
        btnPause.setOnClickListener(v -> {
            PauseSettingsDialogFragment dialog = PauseSettingsDialogFragment.newInstance();
            dialog.show(getParentFragmentManager(), "PauseSettingsDialog");
        });

        // Next button - Navigate to Writing screen
        btnNext.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.rise_of_city.ui.main.MainActivity) {
                WritingFragment writingFragment = new WritingFragment();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, writingFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        btnContinue.setOnClickListener(v -> {
            if (currentStep == 3) {
                // Move to bread selection
                showBreadOptions();
            } else if (currentStep == 5) {
                // Move to cookie selection
                showCookieOptions();
            }
        });

        return view;
    }

    private void preserveNextButton() {
        // Preserve Next button when clearing options
        View nextButton = null;
        for (int i = 0; i < llOptionsContainer.getChildCount(); i++) {
            View child = llOptionsContainer.getChildAt(i);
            if (child instanceof Button && ((Button) child).getText().toString().equals("Next")) {
                nextButton = child;
                break;
            }
        }
        
        llOptionsContainer.removeAllViews();
        
        // Re-add Next button if it existed
        if (nextButton != null) {
            llOptionsContainer.addView(nextButton);
        }
    }

    private void addNextButtonToContainer() {
        // Add Next button to options container so it's always visible
        Button nextBtn = new Button(getContext());
        nextBtn.setText("Next");
        nextBtn.setTextSize(16);
        nextBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        nextBtn.setTextColor(0xFFFFFFFF);
        nextBtn.setBackgroundResource(R.drawable.bg_button_purple);
        nextBtn.setPadding(24, 12, 24, 12);
        nextBtn.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.rise_of_city.ui.main.MainActivity) {
                WritingFragment writingFragment = new WritingFragment();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, writingFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 0);
        nextBtn.setLayoutParams(params);
        
        llOptionsContainer.addView(nextBtn);
    }

    private void startConversation() {
        // Step 1: Chef greeting
        addChefMessage("Hello, How can I have you");
        showInitialOptions();
    }

    private void showInitialOptions() {
        // Keep Next button, only remove option buttons
        int nextButtonIndex = -1;
        View nextButton = null;
        for (int i = 0; i < llOptionsContainer.getChildCount(); i++) {
            View child = llOptionsContainer.getChildAt(i);
            if (child instanceof Button && ((Button) child).getText().toString().equals("Next")) {
                nextButtonIndex = i;
                nextButton = child;
                break;
            }
        }
        
        llOptionsContainer.removeAllViews();
        currentStep = 1;
        
        // Re-add Next button if it existed
        if (nextButton != null) {
            llOptionsContainer.addView(nextButton);
        }
        
        // Ensure chef is happy at the start
        setChefExpression(true);

        // Create coffee option with error icon
        LinearLayout coffeeContainer = new LinearLayout(getContext());
        coffeeContainer.setOrientation(LinearLayout.HORIZONTAL);
        coffeeContainer.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        btnCoffee = createOptionButton("Coffee, pls", v -> {
            // Wrong choice
            addUserMessage("Coffee, pls");
            
            // Show error on coffee button and fade bread button
            showErrorOnButtonWithIcon(btnCoffee, coffeeContainer);
            if (btnBread != null && btnBread.getParent() != null) {
                fadeButton(btnBread);
            }
            
            // Change chef to sad expression
            setChefExpression(false);
            
            // Chef response
            addChefMessage("Sorry, we have no Coffee. Only bread, cake, cookie, pastry and Tiramisu");
            
            // After delay, show options again with bread enabled
            btnCoffee.postDelayed(() -> {
                showInitialOptions();
            }, 2500);
        });
        
        ImageView ivCoffeeError = new ImageView(getContext());
        ivCoffeeError.setImageResource(R.drawable.ic_close_red);
        ivCoffeeError.setLayoutParams(new LinearLayout.LayoutParams(32, 32));
        ivCoffeeError.setVisibility(View.GONE);
        ivCoffeeError.setPadding(8, 0, 0, 0);
        
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        );
        btnCoffee.setLayoutParams(buttonParams);
        
        coffeeContainer.addView(btnCoffee);
        coffeeContainer.addView(ivCoffeeError);
        
        // Create bread option
        LinearLayout breadContainer = new LinearLayout(getContext());
        breadContainer.setOrientation(LinearLayout.HORIZONTAL);
        breadContainer.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        btnBread = createOptionButton("Bread, pls", v -> {
            // Correct choice
            addUserMessage("Bread, pls");
            currentStep = 2;
            
            // Show fill-in-the-blank
            showFillInTheBlank();
        });
        
        ImageView ivBreadError = new ImageView(getContext());
        ivBreadError.setImageResource(R.drawable.ic_close_red);
        ivBreadError.setLayoutParams(new LinearLayout.LayoutParams(32, 32));
        ivBreadError.setVisibility(View.GONE);
        ivBreadError.setPadding(8, 0, 0, 0);
        
        btnBread.setLayoutParams(buttonParams);
        
        breadContainer.addView(btnBread);
        breadContainer.addView(ivBreadError);
        
        // Store error icons in container tags
        coffeeContainer.setTag(ivCoffeeError);
        breadContainer.setTag(ivBreadError);
        
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(0, 8, 0, 8);
        coffeeContainer.setLayoutParams(containerParams);
        breadContainer.setLayoutParams(containerParams);

        llOptionsContainer.addView(coffeeContainer);
        llOptionsContainer.addView(breadContainer);
    }

    private void showFillInTheBlank() {
        // Keep Next button
        preserveNextButton();
        currentStep = 3;

        // Show fill-in-the-blank text
        TextView tvBlank = new TextView(getContext());
        tvBlank.setText("[----------] white bread, please.");
        tvBlank.setTextSize(16);
        tvBlank.setTextColor(0xFF000000);
        tvBlank.setPadding(16, 12, 16, 12);
        tvBlank.setBackgroundResource(R.drawable.bg_light_red_box);
        llOptionsContainer.addView(tvBlank);

        // Chef asks question
        addChefMessage("Sure. What kind of bread would you like?");
        
        // Show continue button
        btnContinue.setVisibility(View.VISIBLE);
    }

    private void showBreadOptions() {
        // Keep Next button
        preserveNextButton();
        btnContinue.setVisibility(View.GONE);
        currentStep = 4;

        Button btnWrong = createOptionButton("A white bread, please.", v -> {
            addUserMessage("A white bread, please.");
            showErrorOnButton((Button) v);
            addChefMessage("What do you mean, I can't understand");
            showBreadOptions();
        });

        Button btnCorrect = createOptionButton("A loaf of white bread, please.", v -> {
            addUserMessage("A loaf of white bread, please.");
            selectedBreadOption = "A loaf of white bread, please.";
            addChefMessage("Yes");
            currentStep = 5;
            
            // Ask for anything else
            addChefMessage("Anything else?");
            showCookieOptions();
        });

        llOptionsContainer.addView(btnWrong);
        llOptionsContainer.addView(btnCorrect);
    }

    private void showCookieOptions() {
        // Keep Next button
        preserveNextButton();
        currentStep = 6;

        Button btnWrong = createOptionButton("Yes, I'd like two koocies.", v -> {
            addUserMessage("Yes, I'd like two koocies.");
            showErrorOnButton((Button) v);
            addChefMessage("It's not koocies, sir");
            showCookieOptions();
        });

        Button btnCorrect = createOptionButton("Yes, I'd like two cookies.", v -> {
            addUserMessage("Yes, I'd like two cookies.");
            selectedCookieOption = "Yes, I'd like two cookies.";
            addChefMessage("Here you are. That's five dollars.");
            llOptionsContainer.removeAllViews();
            
            // Show Next button to go to Writing screen
            btnNext.setVisibility(View.VISIBLE);
            
            // Show completion message
            Toast.makeText(getContext(), "Order completed!", Toast.LENGTH_SHORT).show();
        });

        llOptionsContainer.addView(btnWrong);
        llOptionsContainer.addView(btnCorrect);
    }

    private Button createOptionButton(String text, View.OnClickListener listener) {
        Button button = new Button(getContext());
        button.setText(text);
        button.setTextSize(14);
        button.setTextColor(0xFF000000);
        button.setBackgroundResource(R.drawable.bg_option_button);
        button.setPadding(16, 12, 16, 12);
        button.setOnClickListener(listener);
        button.setAlpha(1.0f);
        button.setEnabled(true);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        button.setLayoutParams(params);
        
        return button;
    }
    
    private void showErrorOnButtonWithIcon(Button button, LinearLayout container) {
        button.setBackgroundResource(R.drawable.bg_option_button_wrong);
        button.setEnabled(false);
        
        // Show error icon from container tag
        ImageView ivError = (ImageView) container.getTag();
        if (ivError != null) {
            ivError.setVisibility(View.VISIBLE);
        }
    }
    
    private void showErrorOnButton(Button button) {
        button.setBackgroundResource(R.drawable.bg_option_button_wrong);
        button.setEnabled(false);
        
        // Try to find error icon in parent container
        View parent = (View) button.getParent();
        if (parent instanceof LinearLayout) {
            ImageView ivError = (ImageView) parent.getTag();
            if (ivError != null) {
                ivError.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void fadeButton(Button button) {
        if (button != null) {
            button.setAlpha(0.5f);
            button.setEnabled(false);
        }
    }
    
    private void setChefExpression(boolean isHappy) {
        if (ivChef != null) {
            if (isHappy) {
                // Show happy chef
                ivChef.setImageResource(R.drawable.chef_happy);
                ivChef.setAlpha(1.0f);
            } else {
                // Show sad chef
                ivChef.setImageResource(R.drawable.chef_sad);
                ivChef.setAlpha(1.0f);
            }
        }
    }

    private void addChefMessage(String message) {
        messages.add(new ChatMessage(message, true));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
    }

    private void addUserMessage(String message) {
        messages.add(new ChatMessage(message, false));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
    }

    // Chat Message Model
    private static class ChatMessage {
        String message;
        boolean isChef;

        ChatMessage(String message, boolean isChef) {
            this.message = message;
            this.isChef = isChef;
        }
    }

    // Chat Adapter
    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
        private List<ChatMessage> messages;

        ChatAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_interact_message, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            ChatMessage message = messages.get(position);
            
            if (message.isChef) {
                holder.cardChef.setVisibility(View.VISIBLE);
                holder.cardUser.setVisibility(View.GONE);
                holder.tvChefMessage.setText(message.message);
            } else {
                holder.cardChef.setVisibility(View.GONE);
                holder.cardUser.setVisibility(View.VISIBLE);
                holder.tvUserMessage.setText(message.message);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class MessageViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardChef, cardUser;
            TextView tvChefMessage, tvUserMessage;

            MessageViewHolder(@NonNull View itemView) {
                super(itemView);
                cardChef = itemView.findViewById(R.id.card_chef_message);
                cardUser = itemView.findViewById(R.id.card_user_message);
                tvChefMessage = itemView.findViewById(R.id.tv_chef_message);
                tvUserMessage = itemView.findViewById(R.id.tv_user_message);
            }
        }
    }
}