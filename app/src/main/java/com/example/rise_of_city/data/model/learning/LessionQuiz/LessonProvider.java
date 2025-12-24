package com.example.rise_of_city.data.model.learning.LessionQuiz;// LessonProvider.java
import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.learning.LessionQuiz.LessonQuestion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LessonProvider {

    public static List<LessonQuestion> getHouseLessons() {
        List<LessonQuestion> lessons = new ArrayList<>();

        // 1. LECTURE: Tìm lỗi sai trong đoạn văn miêu tả nhà
        lessons.add(LessonQuestion.createLecture(
                "Miêu tả ngôi nhà",
                "My house **are** big and beautiful. It has a large **livings** room and two **bedroom**. The kitchen is **moderns**.",
                Arrays.asList("are", "livings", "bedroom", "moderns")  // Lỗi: are->is, livings->living, bedroom->bedrooms, moderns->modern
        ));

        // 2. WORD_MATCH: Ghép từ vựng phòng ốc (English - Vietnamese)
        lessons.add(LessonQuestion.createWordMatch(
                "Phòng trong nhà",
                Arrays.asList("Living room", "Bedroom", "Kitchen", "Bathroom", "Dining room"),
                Arrays.asList("Phòng khách", "Phòng ngủ", "Nhà bếp", "Phòng tắm", "Phòng ăn")
        ));

        // 3. IMAGE_MATCH: Ghép hình ảnh với đồ nội thất (Giả định bạn có resource ảnh: R.drawable.img_sofa, etc.)
        lessons.add(LessonQuestion.createImageMatch(
                "Đồ nội thất",
                Arrays.asList(R.drawable.sofa1, R.drawable.bed, R.drawable.fridge, R.drawable.bathlub),
                Arrays.asList("Sofa", "Bed", "Fridge", "Bathtub")
        ));

        // 4. GUESS: Sắp xếp chữ cái thành từ (từ vựng nhà cửa)
        lessons.add(LessonQuestion.createGuess(
                "Phòng ăn",
                "d/i/n/i/n/g/r/o/o/m",
                "dining room"
        ));

        // 5. GUESS_IMAGE: Nhìn hình đoán đồ vật (Giả định ảnh R.drawable.img_wardrobe)
        lessons.add(LessonQuestion.createGuessImage(
                "Đây là gì?",
                R.drawable.wardrobe,  // Ảnh tủ quần áo
                Arrays.asList("Wardrobe", "Table", "Chair", "Lamp"),
                "Wardrobe"
        ));

        // 6. LECTURE: Tìm lỗi ngữ pháp trong câu mô tả
        lessons.add(LessonQuestion.createLecture(
                "Mô tả đồ đạc",
                "There **is** two chairs in the room. The table **are** round. I have a **bookshelves** in my bedroom.",
                Arrays.asList("is", "are", "bookshelves")  // Lỗi: is->are, are->is, bookshelves->bookshelf
        ));

        // 7. WORD_MATCH: Ghép từ vựng đồ dùng nhà bếp
        lessons.add(LessonQuestion.createWordMatch(
                "Đồ dùng nhà bếp",
                Arrays.asList("Oven", "Microwave", "Sink", "Stove", "Dishwasher"),
                Arrays.asList("Lò nướng", "Lò vi sóng", "Bồn rửa", "Bếp gas", "Máy rửa chén")
        ));

        // 8. GUESS: Sắp xếp chữ cái (từ vựng nội thất)
        lessons.add(LessonQuestion.createGuess(
                "Tủ lạnh",
                "f/r/i/d/g/e",
                "fridge"
        ));

        // 9. GUESS_IMAGE: Nhìn hình đoán phòng (Giả định ảnh R.drawable.img_kitchen)
        lessons.add(LessonQuestion.createGuessImage(
                "Đây là phòng nào?",
                R.drawable.kitchen_full,
                Arrays.asList("Kitchen", "Garage", "Balcony", "Attic"),
                "Kitchen"
        ));

        // 10. IMAGE_MATCH: Ghép hình với từ vựng ngoài trời liên quan nhà
        lessons.add(LessonQuestion.createImageMatch(
                "Ngoài trời",
                Arrays.asList(R.drawable.img_garden, R.drawable.bookshelf, R.drawable.sofa1),
                Arrays.asList("Garden", "Bookshelf", "Sofa")
        ));

        return lessons;
    }

    // Giữ lại các phương thức cũ nếu cần...
    public static List<LessonQuestion> getBakeryLessons() {
        // Implement if needed
        return new ArrayList<>();
    }
}