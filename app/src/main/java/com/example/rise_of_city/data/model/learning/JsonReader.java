package com.example.rise_of_city.data.model.learning;

import android.content.Context;
import com.example.rise_of_city.data.model.learning.quiz.*;
import com.example.rise_of_city.data.model.learning.quiz.CHOICE.ChoiceQuestion;
import com.example.rise_of_city.data.model.learning.quiz.INPUT.WritingQuestion;
import com.example.rise_of_city.data.model.learning.quiz.MATCHING.MatchingIMGQuestion;

import com.example.rise_of_city.data.model.learning.quiz.MATCHING.MatchingTextQuestion;
import com.example.rise_of_city.data.model.learning.quiz.TEXT_INTERACT.LectureQuestion;
import com.example.rise_of_city.data.model.learning.quiz.DECISION.TrueFalseQuestion;
import com.example.rise_of_city.data.model.learning.quiz.ORDERING.SentenceOrderQuestion;
import com.example.rise_of_city.data.model.learning.quiz.ORDERING.WordOrderQuestion;
import com.example.rise_of_city.data.model.learning.quiz.LISTENING.ListeningQuestion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JsonReader {

    private final Context context;
    private final Gson gson;

    public JsonReader(Context context) {
        this.context = context;
        // Khởi tạo Gson với bộ giải mã tùy chỉnh cho BaseQuestion
        this.gson = new GsonBuilder()
                .registerTypeAdapter(BaseQuestion.class, new QuestionAdapter())
                .create();
    }

    /**
     * Đọc file JSON từ thư mục assets và trả về danh sách câu hỏi.
     */
    public List<BaseQuestion> readLessonFromJson(String fileName) {
        String jsonString;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        Type listType = new TypeToken<List<BaseQuestion>>() {}.getType();
        return gson.fromJson(jsonString, listType);
    }

    /**
     * Bộ adapter xử lý đa hình (Polymorphism) cho GSON.
     * Nó đọc trường "type" trong JSON để quyết định tạo Object nào.
     */
    private static class QuestionAdapter implements JsonDeserializer<BaseQuestion> {
        @Override
        public BaseQuestion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            // Lấy giá trị trường "type" (ví dụ: "LECTURE", "CHOICE"...)
            String typeStr = jsonObject.get("type").getAsString();

            try {
                BaseQuestion.QuestionType type = BaseQuestion.QuestionType.valueOf(typeStr.toUpperCase());

                switch (type) {
                    case LECTURE:
                        return context.deserialize(json, LectureQuestion.class);
                    case CHOICE:
                        return context.deserialize(json, ChoiceQuestion.class);
                    case MATCHINGTEXT:
                        return context.deserialize(json, MatchingTextQuestion.class);
                    case MATCHINGIMG:
                        return context.deserialize(json, MatchingIMGQuestion.class);
                    case INPUT:
                        return context.deserialize(json, WritingQuestion.class);
                    case DECISION:
                        return context.deserialize(json, TrueFalseQuestion.class);
                    case SENTENCEORDERING:
                        return context.deserialize(json, SentenceOrderQuestion.class);
                    case WORDORDERING:
                        return context.deserialize(json, WordOrderQuestion.class);
                    case LISTENING:
                        return context.deserialize(json, ListeningQuestion.class);
                    default:
                        throw new JsonParseException("Unknown question type: " + typeStr);
                }
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("Invalid question type: " + typeStr);
            }
        }
    }
}