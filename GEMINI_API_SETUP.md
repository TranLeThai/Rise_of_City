# Hướng dẫn lấy Google Gemini API Key

## Bước 1: Truy cập Google AI Studio
1. Vào trang: https://makersuite.google.com/app/apikey
2. Đăng nhập bằng tài khoản Google của bạn

## Bước 2: Tạo API Key
1. Click vào nút **"Create API Key"** hoặc **"Get API Key"**
2. Chọn project Google Cloud (hoặc tạo mới nếu chưa có)
3. Copy API key được tạo

## Bước 3: Cấu hình trong ứng dụng

### Cách 1: Thay đổi trong code (Không khuyến nghị cho production)
Mở file `app/src/main/java/com/example/rise_of_city/utils/ApiKeyManager.java`
Thay `YOUR_GEMINI_API_KEY_HERE` bằng API key của bạn:

```java
private static final String DEFAULT_API_KEY = "AIzaSy..."; // API key của bạn
```

### Cách 2: Lưu vào SharedPreferences (Tốt hơn)
Sử dụng method `ApiKeyManager.setApiKey(context, "your_api_key")` để lưu API key động.

### Cách 3: Sử dụng BuildConfig (Khuyến nghị cho production)
1. Tạo file `local.properties` trong thư mục root của project
2. Thêm dòng: `GEMINI_API_KEY=your_api_key_here`
3. Trong `build.gradle.kts`, thêm:
```kotlin
android {
    defaultConfig {
        buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\"")
    }
}
```

## Lưu ý bảo mật
⚠️ **QUAN TRỌNG**: 
- KHÔNG commit API key vào Git
- Thêm `local.properties` vào `.gitignore`
- Trong production, nên gọi API qua server-side (Firebase Functions, Cloud Functions, etc.)

## Giới hạn miễn phí
- **60 requests/phút** cho Gemini Pro
- **1,500 requests/ngày**
- Đủ cho mục đích học tập và phát triển

## Xử lý lỗi 404

Nếu bạn gặp lỗi 404 khi gọi API, có thể do:

1. **Model không còn được hỗ trợ**: Một số model cũ đã bị Google ngừng hỗ trợ
   - ✅ **Model hiện tại**: `gemini-1.5-flash-002` (đã được cập nhật trong code)
   - ✅ **Model thay thế**: `gemini-1.5-pro-002` (chất lượng cao hơn)
   - ✅ **Model mới**: `gemini-2.0-flash-exp` (nếu có sẵn)

2. **API version**: Code đã được cập nhật từ `v1beta` sang `v1`

3. **API Key không hợp lệ**: Kiểm tra lại API key trong Google AI Studio

4. **Hết hạn mức miễn phí**: Kiểm tra quota trong Google Cloud Console

### Thay đổi model (nếu cần)

Mở file `app/src/main/java/com/example/rise_of_city/service/GeminiService.java` và thay đổi:
```java
private static final String GEMINI_MODEL = "gemini-1.5-pro-002"; // hoặc model khác
```

## Tài liệu tham khảo
- Gemini API Docs: https://ai.google.dev/docs
- Pricing: https://ai.google.dev/pricing
- Available Models: https://ai.google.dev/models/gemini

