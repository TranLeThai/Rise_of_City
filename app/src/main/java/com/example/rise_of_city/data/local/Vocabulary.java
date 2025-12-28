package com.example.rise_of_city.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vocabularies")
public class Vocabulary {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String topicId;          // "house", "school", "library", "park", "coffee"
    public String english;          // Từ tiếng Anh
    public String vietnamese;       // Nghĩa tiếng Việt (có thể null nếu chỉ có hình ảnh)
    public String imageName;        // Tên file hình ảnh (có thể null)
    public String imagePath;        // Đường dẫn đầy đủ đến hình ảnh trong assets
    
    public Vocabulary() {
    }
    
    public Vocabulary(String topicId, String english, String vietnamese, String imageName) {
        this.topicId = topicId;
        this.english = english;
        this.vietnamese = vietnamese;
        this.imageName = imageName;
        if (imageName != null && !imageName.isEmpty()) {
            this.imagePath = getImagePathForTopic(topicId, imageName);
        }
    }
    
    private String getImagePathForTopic(String topicId, String imageName) {
        // Map topic ID to folder path
        switch (topicId.toLowerCase()) {
            case "house":
                return "Nhà Ở/IMG/" + imageName;
            case "park":
                return "Công Viên/IMG/" + imageName;
            case "school":
                return "Trường Học/IMG/" + imageName;
            case "library":
                return "Thư Viện/IMG/" + imageName;
            case "coffee":
            case "bakery":
                return "quan_ca_phe_pho_thong/image/" + imageName;
            default:
                return imageName;
        }
    }
}

