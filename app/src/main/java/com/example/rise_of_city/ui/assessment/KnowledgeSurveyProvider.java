package com.example.rise_of_city.ui.assessment;

import com.example.rise_of_city.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KnowledgeSurveyProvider {

    public static List<KnowledgeSurveyQuestion> getHouseQuestions() {
        List<KnowledgeSurveyQuestion> questions = new ArrayList<>();

        // 1. Trình độ học vấn (GIỮ LẠI)
        questions.add(new KnowledgeSurveyQuestion("Personal Info", "What is your education level?",
                null, Arrays.asList("High School", "Bachelor", "Master", "Other"), null,
                R.drawable.bg_light_yellow_box, KnowledgeSurveyQuestion.QuestionType.EDUCATION_LEVEL));

        // 2. Living room - Tìm lỗi (GIỮ LẠI)
        questions.add(new KnowledgeSurveyQuestion("Living Room",
                "In my living room, there is a big fridge and a bed to watch TV.",
                Arrays.asList("fridge", "bed"), Arrays.asList("sofa", "chair"), null,
                R.drawable.livingroom_bg, KnowledgeSurveyQuestion.QuestionType.FIND_ERROR));

        // 3. Kitchen - Tìm lỗi (GIỮ LẠI)
        questions.add(new KnowledgeSurveyQuestion("Kitchen",
                "The kitchen is where I shower and brush my teeth.",
                Arrays.asList("shower", "teeth"), Arrays.asList("cook", "eat"), null,
                R.drawable.kitchen, KnowledgeSurveyQuestion.QuestionType.FIND_ERROR));

        // 4. Bedroom - Tìm lỗi (GIỮ LẠI)
        questions.add(new KnowledgeSurveyQuestion("Bedroom",
                "My bedroom has a toilet and a large dining table.",
                Arrays.asList("toilet", "dining"), Arrays.asList("bed", "desk"), null,
                R.drawable.bedroom_bg, KnowledgeSurveyQuestion.QuestionType.FIND_ERROR));

        // --- BẮT ĐẦU CÁC CÂU MULTICHOICE (6 CÂU MỚI) ---

        // 5. Bathroom
        questions.add(new KnowledgeSurveyQuestion("Bathroom", "Where do you usually find a shower and a sink?",
                null, Arrays.asList("Living Room", "Bathroom", "Kitchen", "Garage"), "Bathroom",
                R.drawable.badge_xp, KnowledgeSurveyQuestion.QuestionType.MULTIPLE_CHOICE));

        // 6. Garage
        questions.add(new KnowledgeSurveyQuestion("Garage", "What is the primary purpose of a garage?",
                null, Arrays.asList("Cooking food", "Sleeping", "Parking cars", "Growing flowers"), "Parking cars",
                R.drawable.badge_xp, KnowledgeSurveyQuestion.QuestionType.MULTIPLE_CHOICE));

        // 7. Garden
        questions.add(new KnowledgeSurveyQuestion("Garden", "Which item is most likely to be found in a garden?",
                null, Arrays.asList("Wardrobe", "Trees", "Bathtub", "Microwave"), "Trees",
                R.drawable.badge_xp, KnowledgeSurveyQuestion.QuestionType.MULTIPLE_CHOICE));

        // 8. Attic
        questions.add(new KnowledgeSurveyQuestion("Attic", "Where is the attic typically located in a house?",
                null, Arrays.asList("Under the basement", "Outside the house", "At the very top", "In the kitchen"), "At the very top",
                R.drawable.badge_xp, KnowledgeSurveyQuestion.QuestionType.MULTIPLE_CHOICE));

        // 9. Dining Room
        questions.add(new KnowledgeSurveyQuestion("Dining Room", "What do people usually do in the dining room?",
                null, Arrays.asList("Take a nap", "Eat meals", "Wash clothes", "Park bikes"), "Eat meals",
                R.drawable.badge_xp, KnowledgeSurveyQuestion.QuestionType.MULTIPLE_CHOICE));

        // 10. Basement
        questions.add(new KnowledgeSurveyQuestion("Basement", "Which room is located below the ground floor?",
                null, Arrays.asList("Basement", "Balcony", "Roof", "Living Room"), "Basement",
                R.drawable.badge_xp, KnowledgeSurveyQuestion.QuestionType.MULTIPLE_CHOICE));

        return questions;
    }
}