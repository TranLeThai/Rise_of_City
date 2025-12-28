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
    
    // API key bạn đã nhập
    private static final String DEFAULT_API_KEY = "AIzaSyBeGuQTJUk9NITjXGz6R7x1J-19KKrQuZ0";
    
    /**
     * Lấy API key từ SharedPreferences hoặc trả về default
     */
    public static String getApiKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String apiKey = prefs.getString(KEY_GEMINI_API_KEY, DEFAULT_API_KEY);
        
        // Nếu API key trống hoặc là default, trả về default
        if (apiKey == null || apiKey.isEmpty()) {
            return DEFAULT_API_KEY;
        }
        
        return apiKey;
    }
    
    /**
     * Lưu API key vào SharedPreferences
     */
    public static void setApiKey(Context context, String apiKey) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_GEMINI_API_KEY, apiKey);
        editor.apply();
    }
    
    /**
     * Kiểm tra xem API key đã được cấu hình chưa
     */
    public static boolean isApiKeyConfigured(Context context) {
        String apiKey = getApiKey(context);
        // Kiểm tra xem API key có hợp lệ không
        // Logic cũ bị sai vì bạn đã thay thế placeholder trong câu lệnh so sánh bằng chính API Key thật
        // nên nó trả về false. Đã sửa lại logic kiểm tra.
        return apiKey != null && 
               !apiKey.isEmpty() && 
               !apiKey.equals("YOUR_GEMINI_API_KEY_HERE");
    }
}