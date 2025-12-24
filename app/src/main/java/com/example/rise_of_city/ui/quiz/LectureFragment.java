package com.example.rise_of_city.ui.quiz;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rise_of_city.R;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LectureFragment extends Fragment {

    private TextView tvContent;
    private LinearLayout layoutOptions;

    // Dữ liệu mẫu: từ sai nằm trong [], sau đó là các gợi ý ngăn cách bởi dấu |
    private String rawData = "Dear Mayor, \nThe bakery project is ready for launch. " +
            "We have imported enough [flower|Flour|Fluor] to bake our signature bread. " +
            "Currently, our professional chefs [is baking|are baking|bakes] the first batches.";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lecture, container, false);
        tvContent = view.findViewById(R.id.tvContent);
        layoutOptions = view.findViewById(R.id.layoutOptions);

        renderTextWithSpans(rawData);
        return view;
    }

    private void renderTextWithSpans(String data) {
        // Regex tìm nội dung trong ngoặc [ ... ]
        Pattern pattern = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher = pattern.matcher(data);

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        int lastIndex = 0;

        while (matcher.find()) {
            // Thêm đoạn văn bản bình thường trước dấu [
            ssb.append(data.substring(lastIndex, matcher.start()));

            // Tách từ sai và các lựa chọn (ví dụ: flower|Flour|Fluor)
            String group = matcher.group(1);
            String[] parts = group.split("\\|");
            String wrongWord = parts[0];
            String[] choices = (parts.length > 1) ? Arrays.copyOfRange(parts, 1, parts.length) : new String[]{};

            int spanStart = ssb.length();
            ssb.append(wrongWord);
            int spanEnd = ssb.length();

            // Tạo hiệu ứng click và màu sắc
            ssb.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    showOptionsPopup(choices, wrongWord);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    ds.setColor(Color.WHITE);
                    ds.bgColor = Color.parseColor("#E57373"); // Màu đỏ nhạt cho từ sai
                }
            }, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            lastIndex = matcher.end();
        }
        ssb.append(data.substring(lastIndex));

        tvContent.setText(ssb);
        tvContent.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void showOptionsPopup(String[] choices, String targetWord) {
        layoutOptions.removeAllViews();
        layoutOptions.setVisibility(View.VISIBLE);

        // Nút Back/Đóng (icon mũi tên quay lại như hình)
        ImageButton btnBack = new ImageButton(getContext());
        btnBack.setImageResource(R.drawable.ic_back_arrow); // Bạn cần thêm icon này
        btnBack.setBackgroundColor(Color.TRANSPARENT);
        btnBack.setOnClickListener(v -> layoutOptions.setVisibility(View.GONE));
        layoutOptions.addView(btnBack);

        for (String choice : choices) {
            Button btn = new Button(new ContextThemeWrapper(getContext(), R.style.OptionButton), null, 0);
            btn.setText(choice);
            btn.setAllCaps(false);
            btn.setOnClickListener(v -> {
                // Xử lý khi người dùng chọn từ đúng
                Toast.makeText(getContext(), "Bạn chọn: " + choice, Toast.LENGTH_SHORT).show();
                layoutOptions.setVisibility(View.GONE);
            });
            layoutOptions.addView(btn);
        }
    }
}