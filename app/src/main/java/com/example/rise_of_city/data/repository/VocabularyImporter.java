package com.example.rise_of_city.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.Vocabulary;
import com.example.rise_of_city.data.model.learning.JsonReader;
import com.example.rise_of_city.data.model.learning.quiz.BaseQuestion;
import com.example.rise_of_city.data.model.learning.quiz.MATCHING.MatchingIMGQuestion;
import com.example.rise_of_city.data.model.learning.quiz.MATCHING.MatchingTextQuestion;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VocabularyImporter {
    private static final String TAG = "VocabularyImporter";
    private final AppDatabase appDatabase;
    private final ExecutorService executorService;
    
    public VocabularyImporter(Context context) {
        this.appDatabase = AppDatabase.getInstance(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Import tất cả từ vựng từ các file JSON trong assets
     */
    public void importAllVocabularies(Context context) {
        executorService.execute(() -> {
            try {
                JsonReader jsonReader = new JsonReader(context);
                List<Vocabulary> allVocabularies = new ArrayList<>();
                
                // Import từ các file JSON trong assets
                importFromJsonFile(jsonReader, "House_lv1.json", "house", allVocabularies);
                importFromJsonFile(jsonReader, "School_lv1.json", "school", allVocabularies);
                importFromJsonFile(jsonReader, "Library_lv1.json", "library", allVocabularies);
                importFromJsonFile(jsonReader, "Park_lv1.json", "park", allVocabularies);
                importFromJsonFile(jsonReader, "Bakery_lv1.json", "bakery", allVocabularies);
                
                // Import từ các file JSON trong project folders
                importFromProjectJson(context, "Nhà Ở/NhaO.json", "house", allVocabularies);
                importFromProjectJson(context, "quan_ca_phe_pho_thong/Coffee.json", "coffee", allVocabularies);
                
                // Insert vào database
                if (!allVocabularies.isEmpty()) {
                    appDatabase.vocabularyDao().insertAll(allVocabularies);
                    Log.d(TAG, "Imported " + allVocabularies.size() + " vocabularies");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error importing vocabularies", e);
            }
        });
    }
    
    /**
     * Import từ file JSON trong assets folder
     */
    private void importFromJsonFile(JsonReader jsonReader, String fileName, String topicId, List<Vocabulary> vocabularies) {
        try {
            List<BaseQuestion> questions = jsonReader.readLessonFromJson(fileName);
            extractVocabularies(questions, topicId, vocabularies);
        } catch (Exception e) {
            Log.e(TAG, "Error reading " + fileName, e);
        }
    }
    
    /**
     * Import từ file JSON trong project folders
     * Note: Files trong project folders sẽ được copy vào assets hoặc đọc trực tiếp từ assets
     * Hiện tại chỉ import từ assets folder
     */
    private void importFromProjectJson(Context context, String filePath, String topicId, List<Vocabulary> vocabularies) {
        // Skip for now - files in project folders need to be copied to assets first
        // Or we can read directly from assets if they're already there
        Log.d(TAG, "Skipping project JSON import for: " + filePath + " - Use assets folder instead");
    }
    
    /**
     * Extract vocabularies từ BaseQuestion list
     */
    private void extractVocabularies(List<BaseQuestion> questions, String topicId, List<Vocabulary> vocabularies) {
        for (BaseQuestion question : questions) {
            if (question instanceof MatchingIMGQuestion) {
                MatchingIMGQuestion imgQuestion = (MatchingIMGQuestion) question;
                List<String> images = imgQuestion.getImageNames();
                List<String> words = imgQuestion.getWords();
                
                for (int i = 0; i < Math.min(images.size(), words.size()); i++) {
                    String imageName = images.get(i);
                    String word = words.get(i);
                    
                    // Clean image name (remove extension if needed)
                    String cleanImageName = imageName.contains(".") 
                        ? imageName.substring(0, imageName.lastIndexOf(".")) 
                        : imageName;
                    
                    Vocabulary vocab = new Vocabulary(topicId, word, null, imageName);
                    vocabularies.add(vocab);
                }
            } else if (question instanceof MatchingTextQuestion) {
                MatchingTextQuestion textQuestion = (MatchingTextQuestion) question;
                List<String> leftSide = textQuestion.getLeftSide();
                List<String> rightSide = textQuestion.getRightSide();
                
                for (int i = 0; i < Math.min(leftSide.size(), rightSide.size()); i++) {
                    String english = leftSide.get(i);
                    String vietnamese = rightSide.get(i);
                    
                    Vocabulary vocab = new Vocabulary(topicId, english, vietnamese, null);
                    vocabularies.add(vocab);
                }
            }
        }
    }
    
    /**
     * Extract vocabularies từ JSON objects (cho project folders)
     */
    private void extractVocabulariesFromJson(List<com.google.gson.JsonObject> jsonArray, String topicId, List<Vocabulary> vocabularies) {
        for (com.google.gson.JsonObject jsonObj : jsonArray) {
            String type = jsonObj.has("type") ? jsonObj.get("type").getAsString() : "";
            
            if ("MATCHINGIMG".equals(type)) {
                if (jsonObj.has("images") && jsonObj.has("answers")) {
                    com.google.gson.JsonArray images = jsonObj.getAsJsonArray("images");
                    com.google.gson.JsonArray answers = jsonObj.getAsJsonArray("answers");
                    
                    for (int i = 0; i < Math.min(images.size(), answers.size()); i++) {
                        String imageName = images.get(i).getAsString();
                        String word = answers.get(i).getAsString();
                        
                        Vocabulary vocab = new Vocabulary(topicId, word, null, imageName);
                        vocabularies.add(vocab);
                    }
                }
            } else if ("MATCHINGTEXT".equals(type)) {
                if (jsonObj.has("leftSide") && jsonObj.has("rightSide")) {
                    com.google.gson.JsonArray leftSide = jsonObj.getAsJsonArray("leftSide");
                    com.google.gson.JsonArray rightSide = jsonObj.getAsJsonArray("rightSide");
                    
                    for (int i = 0; i < Math.min(leftSide.size(), rightSide.size()); i++) {
                        String english = leftSide.get(i).getAsString();
                        String vietnamese = rightSide.get(i).getAsString();
                        
                        Vocabulary vocab = new Vocabulary(topicId, english, vietnamese, null);
                        vocabularies.add(vocab);
                    }
                }
            }
        }
    }
    
    /**
     * Import từ vựng từ file JSON đã được tạo từ script Python
     * File JSON này có format từ process_docx.py
     */
    public void importFromProcessedJson(Context context, String jsonFilePath, String topicId) {
        executorService.execute(() -> {
            try {
                java.io.InputStream inputStream = context.getAssets().open(jsonFilePath);
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(inputStream, "UTF-8"));
                
                StringBuilder jsonString = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonString.append(line);
                }
                reader.close();
                inputStream.close();
                
                com.google.gson.Gson gson = new com.google.gson.Gson();
                com.google.gson.JsonArray jsonArray = gson.fromJson(jsonString.toString(), com.google.gson.JsonArray.class);
                
                List<Vocabulary> vocabularies = new ArrayList<>();
                
                for (int i = 0; i < jsonArray.size(); i++) {
                    com.google.gson.JsonObject questionObj = jsonArray.get(i).getAsJsonObject();
                    String type = questionObj.has("type") ? questionObj.get("type").getAsString() : "";
                    
                    if ("MATCHINGIMG".equals(type)) {
                        if (questionObj.has("images") && questionObj.has("answers")) {
                            com.google.gson.JsonArray images = questionObj.getAsJsonArray("images");
                            com.google.gson.JsonArray answers = questionObj.getAsJsonArray("answers");
                            
                            for (int j = 0; j < Math.min(images.size(), answers.size()); j++) {
                                String imagePath = images.get(j).getAsString();
                                String word = answers.get(j).getAsString();
                                
                                // Extract image name from path (e.g., "images/image_001.png" -> "image_001.png")
                                String imageName = imagePath.contains("/") || imagePath.contains("\\")
                                    ? imagePath.substring(imagePath.lastIndexOf("/") + 1).replace("\\", "/")
                                    : imagePath;
                                
                                // Remove extension for imageName field
                                String cleanImageName = imageName.contains(".") 
                                    ? imageName.substring(0, imageName.lastIndexOf("."))
                                    : imageName;
                                
                                Vocabulary vocab = new Vocabulary(topicId, word, null, imageName);
                                vocabularies.add(vocab);
                            }
                        }
                    }
                }
                
                if (!vocabularies.isEmpty()) {
                    appDatabase.vocabularyDao().insertAll(vocabularies);
                    Log.d(TAG, "Imported " + vocabularies.size() + " vocabularies from " + jsonFilePath);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error importing from processed JSON: " + jsonFilePath, e);
            }
        });
    }
    
    /**
     * Import từ file JSON từ đường dẫn file system (cho development/testing)
     */
    public void importFromFileSystemJson(String jsonFilePath, String topicId) {
        executorService.execute(() -> {
            try {
                java.io.File file = new java.io.File(jsonFilePath);
                if (!file.exists()) {
                    Log.e(TAG, "File not found: " + jsonFilePath);
                    return;
                }
                
                java.io.FileReader fileReader = new java.io.FileReader(file);
                com.google.gson.Gson gson = new com.google.gson.Gson();
                com.google.gson.JsonArray jsonArray = gson.fromJson(fileReader, com.google.gson.JsonArray.class);
                fileReader.close();
                
                List<Vocabulary> vocabularies = new ArrayList<>();
                
                for (int i = 0; i < jsonArray.size(); i++) {
                    com.google.gson.JsonObject questionObj = jsonArray.get(i).getAsJsonObject();
                    String type = questionObj.has("type") ? questionObj.get("type").getAsString() : "";
                    
                    if ("MATCHINGIMG".equals(type)) {
                        if (questionObj.has("images") && questionObj.has("answers")) {
                            com.google.gson.JsonArray images = questionObj.getAsJsonArray("images");
                            com.google.gson.JsonArray answers = questionObj.getAsJsonArray("answers");
                            
                            for (int j = 0; j < Math.min(images.size(), answers.size()); j++) {
                                String imagePath = images.get(j).getAsString();
                                String word = answers.get(j).getAsString();
                                
                                // Extract image name from path
                                String imageName = imagePath.contains("/") || imagePath.contains("\\")
                                    ? imagePath.substring(imagePath.lastIndexOf("/") + 1).replace("\\", "/")
                                    : imagePath;
                                
                                Vocabulary vocab = new Vocabulary(topicId, word, null, imageName);
                                vocabularies.add(vocab);
                            }
                        }
                    }
                }
                
                if (!vocabularies.isEmpty()) {
                    appDatabase.vocabularyDao().insertAll(vocabularies);
                    Log.d(TAG, "Imported " + vocabularies.size() + " vocabularies from file system: " + jsonFilePath);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error importing from file system JSON: " + jsonFilePath, e);
            }
        });
    }
    
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}

