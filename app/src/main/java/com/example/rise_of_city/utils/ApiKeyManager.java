package com.example.rise_of_city.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Quản lý API Key cho Gemini
 * Lưu ý: Trong production, nên lưu API key ở server-side hoặc sử dụng Firebase Functions
 */
public class ApiKeyManager {
    private static final String PREFS_NAME = "RiseOfCityPrefs";
    private static final String KEY_GEMINI_API_KEY = "gemini_api_key";
    
    // Default API key - THAY ĐỔI BẰNG API KEY CỦA BẠN
    // Lấy API key tại: https://makersuite.google.com/app/apikey
    private static final String DEFAULT_API_KEY = "AIzaSyBrPPyM52MsFYj0hEy-USWZb5wmaF6GRdI";
    
    /**
     * Lấy API key từ SharedPreferences hoặc trả về default
     */
    public static String getApiKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String apiKey = prefs.getString(KEY_GEMINI_API_KEY, DEFAULT_API_KEY);
        
        // Nếu vẫn là default, thử lấy từ BuildConfig (nếu có)
        if (apiKey.equals(DEFAULT_API_KEY) || apiKey.isEmpty()) {
            // Có thể thêm logic lấy từ BuildConfig hoặc environment variable
            return DEFAULT_API_KEY;
        }
        
        return apiKey;
    }
    
    /**
     * Kiểm tra xem API key đã được cấu hình chưa
     */
    public static boolean isApiKeyConfigured(Context context) {
        String apiKey = getApiKey(context);
        // Kiểm tra xem API key có hợp lệ không (không null, không rỗng, và không phải placeholder)
        return apiKey != null && 
               !apiKey.isEmpty() && 
               !apiKey.equals("YOUR_GEMINI_API_KEY_HERE") &&
               !apiKey.startsWith("AIzaSy...") &&
               apiKey.length() > 20; // API key thường dài hơn 20 ký tự
    }
}
