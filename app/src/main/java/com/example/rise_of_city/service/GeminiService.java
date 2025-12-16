package com.example.rise_of_city.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiService {
    // Sử dụng Gemini 2.5 Flash - Model tốt nhất về hiệu suất/giá, phù hợp với Android
    // Các model khác: gemini-2.5-pro (tư duy nâng cao), gemini-2.5-flash-lite (nhanh nhất)
    private static final String GEMINI_MODEL = "gemini-2.5-flash";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/" + GEMINI_MODEL + ":generateContent";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private String apiKey;
    private OkHttpClient client;
    private Gson gson;
    
    public interface GeminiCallback {
        void onSuccess(String response);
        void onError(String error);
        // Callback để update từng phần khi nhận được (cho streaming-like experience)
        default void onPartialUpdate(String partialResponse) {
            // Default implementation - không làm gì
        }
        // Callback khi response bị cắt và cần tiếp tục
        default void onTruncated(String partialResponse, Runnable continueCallback) {
            // Default: tự động tiếp tục
            continueCallback.run();
        }
    }
    
    public GeminiService(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder().create();
    }
    
    /**
     * Gửi tin nhắn đến Gemini API với retry tự động cho lỗi 503
     * @param message Tin nhắn của người dùng
     * @param conversationHistory Lịch sử cuộc trò chuyện (để context)
     * @param callback Callback để nhận kết quả
     */
    public void sendMessage(String message, List<String> conversationHistory, GeminiCallback callback) {
        sendMessageWithRetry(message, conversationHistory, callback, 0);
    }
    
    /**
     * Gửi tin nhắn với retry logic (tối đa 3 lần)
     * @param message Tin nhắn của người dùng
     * @param conversationHistory Lịch sử cuộc trò chuyện
     * @param callback Callback để nhận kết quả
     * @param retryCount Số lần đã retry
     */
    private void sendMessageWithRetry(String message, List<String> conversationHistory, GeminiCallback callback, int retryCount) {
        final int MAX_RETRIES = 3;
        final long[] RETRY_DELAYS = {1000, 2000, 4000}; // 1s, 2s, 4s
        final int currentRetryCount = retryCount; // Capture để sử dụng trong callback
        try {
            // Tạo request body
            JsonObject requestBody = new JsonObject();
            
            // Tạo contents array với lịch sử và tin nhắn mới
            JsonArray contents = new JsonArray();
            
            // Thêm system instruction vào message đầu tiên nếu đây là cuộc trò chuyện mới
            boolean isNewConversation = (conversationHistory == null || conversationHistory.isEmpty());
            if (isNewConversation) {
                // Thêm system instruction như một message ẩn (không hiển thị cho user)
                JsonObject systemPart = new JsonObject();
                systemPart.addProperty("text", "You are a friendly and patient English tutor. " +
                        "CRITICAL INSTRUCTIONS: " +
                        "1. Answer DIRECTLY and CONCISELY - get to the point immediately. " +
                        "2. Be SPECIFIC, not generic. Avoid vague explanations. " +
                        "3. If asked for a plan, provide a clear, structured plan without long introductions. " +
                        "4. If asked a direct question, answer it FIRST, then add context if needed. " +
                        "5. Keep responses focused - maximum 3-4 sentences unless asked for details. " +
                        "6. Respond in English, but you can use Vietnamese to explain if needed. " +
                        "7. FORMAT YOUR RESPONSES WITH MARKDOWN: Use **bold** for important points, " +
                        "*italic* for emphasis, `code` for examples, ## for section headers, " +
                        "and - for lists. Make your responses visually appealing and easy to read.");
                JsonObject systemContent = new JsonObject();
                JsonArray systemParts = new JsonArray();
                systemParts.add(systemPart);
                systemContent.add("parts", systemParts);
                systemContent.addProperty("role", "user");
                contents.add(systemContent);
                
                // AI acknowledgment (ẩn, không hiển thị)
                JsonObject aiAckPart = new JsonObject();
                aiAckPart.addProperty("text", "Understood. I will answer directly and concisely.");
                JsonObject aiAckContent = new JsonObject();
                JsonArray aiAckParts = new JsonArray();
                aiAckParts.add(aiAckPart);
                aiAckContent.add("parts", aiAckParts);
                aiAckContent.addProperty("role", "model");
                contents.add(aiAckContent);
            }
            
            // Thêm lịch sử cuộc trò chuyện (nếu có)
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                for (int i = 0; i < conversationHistory.size(); i += 2) {
                    if (i + 1 < conversationHistory.size()) {
                        // User message
                        JsonObject userPart = new JsonObject();
                        userPart.addProperty("text", conversationHistory.get(i));
                        JsonObject userContent = new JsonObject();
                        JsonArray userParts = new JsonArray();
                        userParts.add(userPart);
                        userContent.add("parts", userParts);
                        userContent.addProperty("role", "user");
                        contents.add(userContent);
                        
                        // AI response
                        JsonObject aiPart = new JsonObject();
                        aiPart.addProperty("text", conversationHistory.get(i + 1));
                        JsonObject aiContent = new JsonObject();
                        JsonArray aiParts = new JsonArray();
                        aiParts.add(aiPart);
                        aiContent.add("parts", aiParts);
                        aiContent.addProperty("role", "model");
                        contents.add(aiContent);
                    }
                }
            }
            
            // Thêm tin nhắn mới của user
            // Nếu không phải cuộc trò chuyện mới, thêm prompt nhắc nhở ngắn gọn
            String messageWithInstruction = message;
            if (!isNewConversation) {
                // Thêm prompt nhắc nhở ngắn gọn (không hiển thị cho user)
                messageWithInstruction = "[Answer directly and concisely. Be specific.] " + message;
            }
            
            JsonObject userPart = new JsonObject();
            userPart.addProperty("text", messageWithInstruction);
            JsonObject userContent = new JsonObject();
            JsonArray userParts = new JsonArray();
            userParts.add(userPart);
            userContent.add("parts", userParts);
            userContent.addProperty("role", "user");
            contents.add(userContent);
            
            requestBody.add("contents", contents);
            
            // Thêm generation config để tối ưu cho học tiếng Anh
            // Cân bằng giữa trả lời trực tiếp và đầy đủ thông tin
            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("temperature", 0.5); // Giảm temperature để trả lời tập trung hơn
            generationConfig.addProperty("topK", 20); // Giảm topK để chọn từ phù hợp hơn
            generationConfig.addProperty("topP", 0.8); // Giảm topP để trả lời ngắn gọn hơn
            generationConfig.addProperty("maxOutputTokens", 2048); // Tăng max tokens để có thể trả lời đầy đủ khi cần
            requestBody.add("generationConfig", generationConfig);
            
            // Lưu ý: systemInstruction không được hỗ trợ trong API v1
            // Thay vào đó, có thể thêm instruction vào message đầu tiên nếu cần
            
            String jsonBody = gson.toJson(requestBody);
            RequestBody body = RequestBody.create(jsonBody, JSON);
            
            // Tạo request
            String url = GEMINI_API_URL + "?key=" + apiKey;
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            
            // Gửi request bất đồng bộ
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // Retry nếu là lỗi kết nối và chưa vượt quá số lần retry
                    if (currentRetryCount < MAX_RETRIES) {
                        long delay = RETRY_DELAYS[Math.min(currentRetryCount, RETRY_DELAYS.length - 1)];
                        android.util.Log.d("GeminiService", "Retrying request after " + delay + "ms (attempt " + (currentRetryCount + 1) + "/" + MAX_RETRIES + ")");
                        
                        // Retry sau delay
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            sendMessageWithRetry(message, conversationHistory, callback, currentRetryCount + 1);
                        }, delay);
                    } else {
                        if (callback != null) {
                            callback.onError("Lỗi kết nối sau " + MAX_RETRIES + " lần thử: " + e.getMessage());
                        }
                    }
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        int statusCode = response.code();
                        
                        // Xử lý lỗi 503 (Service Unavailable) - Model quá tải
                        if (statusCode == 503) {
                            if (currentRetryCount < MAX_RETRIES) {
                                long delay = RETRY_DELAYS[Math.min(currentRetryCount, RETRY_DELAYS.length - 1)];
                                android.util.Log.d("GeminiService", "Model overloaded (503). Retrying after " + delay + "ms (attempt " + (currentRetryCount + 1) + "/" + MAX_RETRIES + ")");
                                
                                // Retry sau delay với exponential backoff
                                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                    sendMessageWithRetry(message, conversationHistory, callback, currentRetryCount + 1);
                                }, delay);
                                return; // Không gọi callback.onError, sẽ retry
                            } else {
                                // Đã retry hết, trả về lỗi
                                String errorMessage = "Model đang quá tải. Vui lòng thử lại sau vài giây.\n\n" +
                                        "Đã thử " + MAX_RETRIES + " lần nhưng vẫn không thành công.";
                                if (callback != null) {
                                    callback.onError(errorMessage);
                                }
                                return;
                            }
                        }
                        
                        String errorMessage = "Lỗi API: " + statusCode + " - " + errorBody;
                        
                        // Thông báo chi tiết hơn cho lỗi 404
                        if (statusCode == 404) {
                            errorMessage += "\n\nGợi ý khắc phục:\n" +
                                    "1. Kiểm tra API key có hợp lệ không\n" +
                                    "2. Model '" + GEMINI_MODEL + "' có thể không còn được hỗ trợ\n" +
                                    "3. Thử đổi model sang: gemini-2.5-pro hoặc gemini-2.5-flash-lite";
                        }
                        
                        if (callback != null) {
                            callback.onError(errorMessage);
                        }
                        return;
                    }
                    
                    String responseBody = response.body() != null ? response.body().string() : "";
                    try {
                        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                        
                        if (jsonResponse.has("candidates") && jsonResponse.getAsJsonArray("candidates").size() > 0) {
                            JsonObject candidate = jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject();
                            
                            // Kiểm tra finishReason để biết tại sao response dừng
                            String finishReason = candidate.has("finishReason") ? 
                                candidate.get("finishReason").getAsString() : "UNKNOWN";
                            
                            // Log để debug
                            android.util.Log.d("GeminiService", "Finish reason: " + finishReason);
                            
                            if (candidate.has("content") && candidate.getAsJsonObject("content").has("parts")) {
                                JsonArray parts = candidate.getAsJsonObject("content").getAsJsonArray("parts");
                                
                                // Lấy tất cả text từ tất cả parts (có thể có nhiều parts)
                                StringBuilder fullText = new StringBuilder();
                                for (int i = 0; i < parts.size(); i++) {
                                    JsonObject part = parts.get(i).getAsJsonObject();
                                    if (part.has("text")) {
                                        if (fullText.length() > 0) {
                                            fullText.append("\n");
                                        }
                                        fullText.append(part.get("text").getAsString());
                                    }
                                }
                                
                                String text = fullText.toString();
                                
                                // Kiểm tra nếu response bị cắt do MAX_TOKENS
                                if ("MAX_TOKENS".equals(finishReason) && text.length() > 0) {
                                    // Response bị cắt, tự động tiếp tục để lấy phần còn lại
                                    android.util.Log.w("GeminiService", "Response was truncated due to MAX_TOKENS. Continuing...");
                                    
                                    // Gọi callback với partial response trước
                                    if (callback != null) {
                                        callback.onPartialUpdate(text);
                                        
                                        // Tự động tiếp tục
                                        callback.onTruncated(text, () -> {
                                            // Tạo conversation history mới với phần đã nhận
                                            List<String> newHistory = new ArrayList<>();
                                            if (conversationHistory != null) {
                                                newHistory.addAll(conversationHistory);
                                            }
                                            newHistory.add(message);
                                            newHistory.add(text);
                                            
                                            // Gửi request tiếp tục
                                            continueMessage("Continue from where you left off.", newHistory, callback, text);
                                        });
                                    }
                                    return;
                                }
                                
                                if (text.length() > 0) {
                                    if (callback != null) {
                                        callback.onSuccess(text);
                                        return;
                                    }
                                }
                            }
                        }
                        
                        if (callback != null) {
                            callback.onError("Không thể lấy phản hồi từ AI");
                        }
                    } catch (Exception e) {
                        android.util.Log.e("GeminiService", "Error parsing response", e);
                        if (callback != null) {
                            callback.onError("Lỗi parse response: " + e.getMessage());
                        }
                    }
                }
            });
            
        } catch (Exception e) {
            if (callback != null) {
                callback.onError("Lỗi: " + e.getMessage());
            }
        }
    }
    
    /**
     * Tiếp tục message bị cắt
     * @param continuePrompt Prompt để tiếp tục
     * @param conversationHistory Lịch sử cuộc trò chuyện (đã bao gồm phần đã nhận)
     * @param callback Callback để nhận kết quả
     * @param previousText Phần text đã nhận trước đó
     */
    private void continueMessage(String continuePrompt, List<String> conversationHistory, GeminiCallback callback, String previousText) {
        try {
            // Tạo request body
            JsonObject requestBody = new JsonObject();
            JsonArray contents = new JsonArray();
            
            // Thêm lịch sử cuộc trò chuyện
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                for (int i = 0; i < conversationHistory.size(); i += 2) {
                    if (i + 1 < conversationHistory.size()) {
                        // User message
                        JsonObject userPart = new JsonObject();
                        userPart.addProperty("text", conversationHistory.get(i));
                        JsonObject userContent = new JsonObject();
                        JsonArray userParts = new JsonArray();
                        userParts.add(userPart);
                        userContent.add("parts", userParts);
                        userContent.addProperty("role", "user");
                        contents.add(userContent);
                        
                        // AI response
                        JsonObject aiPart = new JsonObject();
                        aiPart.addProperty("text", conversationHistory.get(i + 1));
                        JsonObject aiContent = new JsonObject();
                        JsonArray aiParts = new JsonArray();
                        aiParts.add(aiPart);
                        aiContent.add("parts", aiParts);
                        aiContent.addProperty("role", "model");
                        contents.add(aiContent);
                    }
                }
            }
            
            // Thêm prompt tiếp tục
            JsonObject continuePart = new JsonObject();
            continuePart.addProperty("text", continuePrompt);
            JsonObject continueContent = new JsonObject();
            JsonArray continueParts = new JsonArray();
            continueParts.add(continuePart);
            continueContent.add("parts", continueParts);
            continueContent.addProperty("role", "user");
            contents.add(continueContent);
            
            requestBody.add("contents", contents);
            
            // Generation config
            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("temperature", 0.5);
            generationConfig.addProperty("topK", 20);
            generationConfig.addProperty("topP", 0.8);
            generationConfig.addProperty("maxOutputTokens", 2048);
            requestBody.add("generationConfig", generationConfig);
            
            String jsonBody = gson.toJson(requestBody);
            RequestBody body = RequestBody.create(jsonBody, JSON);
            
            String url = GEMINI_API_URL + "?key=" + apiKey;
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (callback != null) {
                        callback.onError("Lỗi khi tiếp tục: " + e.getMessage());
                    }
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        if (callback != null) {
                            callback.onError("Lỗi khi tiếp tục: " + response.code() + " - " + errorBody);
                        }
                        return;
                    }
                    
                    String responseBody = response.body() != null ? response.body().string() : "";
                    try {
                        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                        
                        if (jsonResponse.has("candidates") && jsonResponse.getAsJsonArray("candidates").size() > 0) {
                            JsonObject candidate = jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject();
                            String finishReason = candidate.has("finishReason") ? 
                                candidate.get("finishReason").getAsString() : "UNKNOWN";
                            
                            if (candidate.has("content") && candidate.getAsJsonObject("content").has("parts")) {
                                JsonArray parts = candidate.getAsJsonObject("content").getAsJsonArray("parts");
                                
                                StringBuilder continuationText = new StringBuilder();
                                for (int i = 0; i < parts.size(); i++) {
                                    JsonObject part = parts.get(i).getAsJsonObject();
                                    if (part.has("text")) {
                                        if (continuationText.length() > 0) {
                                            continuationText.append("\n");
                                        }
                                        continuationText.append(part.get("text").getAsString());
                                    }
                                }
                                
                                String continuation = continuationText.toString();
                                
                                // Nối với phần trước
                                String fullText = previousText + continuation;
                                
                                // Nếu vẫn bị cắt, tiếp tục lần nữa
                                if ("MAX_TOKENS".equals(finishReason) && continuation.length() > 0) {
                                    android.util.Log.w("GeminiService", "Continuation also truncated. Continuing again...");
                                    
                                    if (callback != null) {
                                        callback.onPartialUpdate(fullText);
                                        
                                        List<String> newHistory = new ArrayList<>();
                                        if (conversationHistory != null) {
                                            newHistory.addAll(conversationHistory);
                                        }
                                        newHistory.add(continuePrompt);
                                        newHistory.add(continuation);
                                        
                                        callback.onTruncated(fullText, () -> {
                                            continueMessage("Continue.", newHistory, callback, fullText);
                                        });
                                    }
                                    return;
                                }
                                
                                // Hoàn thành
                                if (callback != null) {
                                    callback.onSuccess(fullText);
                                }
                                return;
                            }
                        }
                        
                        if (callback != null) {
                            callback.onError("Không thể lấy phần tiếp tục từ AI");
                        }
                    } catch (Exception e) {
                        android.util.Log.e("GeminiService", "Error parsing continuation response", e);
                        if (callback != null) {
                            callback.onError("Lỗi parse continuation: " + e.getMessage());
                        }
                    }
                }
            });
            
        } catch (Exception e) {
            if (callback != null) {
                callback.onError("Lỗi: " + e.getMessage());
            }
        }
    }
}

