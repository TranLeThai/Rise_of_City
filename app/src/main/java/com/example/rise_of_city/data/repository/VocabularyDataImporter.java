package com.example.rise_of_city.data.repository;

import android.content.Context;
import android.util.Log;
import com.example.rise_of_city.data.local.AppDatabase;
import com.example.rise_of_city.data.local.Vocabulary;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VocabularyDataImporter {
    private static final String TAG = "VocabularyDataImporter";
    private final AppDatabase appDatabase;
    private final ExecutorService executorService;

    public VocabularyDataImporter(Context context) {
        this.appDatabase = AppDatabase.getInstance(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void importData() {
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Starting vocabulary import by topics...");
                List<Vocabulary> vocabularies = new ArrayList<>();


                // ========== NÔNG TRẠI (Farm) - 10 từ ==========
                Vocabulary vocab_farm_1 = new Vocabulary("farm", "farm", "nông trại", "farm.jpg");
                vocabularies.add(vocab_farm_1);
                Vocabulary vocab_farm_2 = new Vocabulary("farm", "cow", "bò", "cow.jpg");
                vocabularies.add(vocab_farm_2);
                Vocabulary vocab_farm_3 = new Vocabulary("farm", "pig", "heo", "pig.jpg");
                vocabularies.add(vocab_farm_3);
                Vocabulary vocab_farm_4 = new Vocabulary("farm", "soil", "đất", "soil.jpg");
                vocabularies.add(vocab_farm_4);
                Vocabulary vocab_farm_5 = new Vocabulary("farm", "plant", "cây trồng", "plant.jpg");
                vocabularies.add(vocab_farm_5);
                Vocabulary vocab_farm_6 = new Vocabulary("farm", "tractor", "máy kéo", "tractor.jpg");
                vocabularies.add(vocab_farm_6);
                Vocabulary vocab_farm_7 = new Vocabulary("farm", "harvest", "thu hoạch", "harvest.jpg");
                vocabularies.add(vocab_farm_7);
                Vocabulary vocab_farm_8 = new Vocabulary("farm", "fertilizer", "phân bón", "fertilizer.jpg");
                vocabularies.add(vocab_farm_8);
                Vocabulary vocab_farm_9 = new Vocabulary("farm", "livestock", "gia súc", "livestock.jpg");
                vocabularies.add(vocab_farm_9);
                Vocabulary vocab_farm_10 = new Vocabulary("farm", "pasture", "đồng cỏ", "pasture.jpg");
                vocabularies.add(vocab_farm_10);

                // ========== TRƯỜNG HỌC (School) - 10 từ ==========
                Vocabulary vocab_school_1 = new Vocabulary("school", "school", "trường học", "school.jpg");
                vocabularies.add(vocab_school_1);
                Vocabulary vocab_school_2 = new Vocabulary("school", "teacher", "giáo viên", "teacher.jpg");
                vocabularies.add(vocab_school_2);
                Vocabulary vocab_school_3 = new Vocabulary("school", "student", "học sinh", "student.jpg");
                vocabularies.add(vocab_school_3);
                Vocabulary vocab_school_4 = new Vocabulary("school", "curriculum", "chương trình học", "curriculum.jpg");
                vocabularies.add(vocab_school_4);
                Vocabulary vocab_school_5 = new Vocabulary("school", "assignment", "bài tập", "assignment.jpg");
                vocabularies.add(vocab_school_5);
                Vocabulary vocab_school_6 = new Vocabulary("school", "exam", "kỳ thi", "exam.jpg");
                vocabularies.add(vocab_school_6);
                Vocabulary vocab_school_7 = new Vocabulary("school", "lecture", "bài giảng", "lecture.jpg");
                vocabularies.add(vocab_school_7);
                Vocabulary vocab_school_8 = new Vocabulary("school", "literature", "văn", "literature.jpg");
                vocabularies.add(vocab_school_8);
                Vocabulary vocab_school_9 = new Vocabulary("school", "math", "toán", "math.jpg");
                vocabularies.add(vocab_school_9);
                Vocabulary vocab_school_10 = new Vocabulary("school", "graduation", "tốt nghiệp", "graduation.jpg");
                vocabularies.add(vocab_school_10);

                // ========== THƯ VIỆN (Library) - 9 từ ==========
                Vocabulary vocab_library_1 = new Vocabulary("library", "library", "thư viện", "library.jpg");
                vocabularies.add(vocab_library_1);
                Vocabulary vocab_library_2 = new Vocabulary("library", "librarian", "thủ thư", "librarian.jpg");
                vocabularies.add(vocab_library_2);
                Vocabulary vocab_library_3 = new Vocabulary("library", "catalog", "mục lục", "catalog.jpg");
                vocabularies.add(vocab_library_3);
                Vocabulary vocab_library_4 = new Vocabulary("library", "dictionary", "từ điển", "dictionary.jpg");
                vocabularies.add(vocab_library_4);
                Vocabulary vocab_library_5 = new Vocabulary("library", "journal", "tạp chí học thuật", "journal.jpg");
                vocabularies.add(vocab_library_5);
                Vocabulary vocab_library_6 = new Vocabulary("library", "citation", "trích dẫn", "citation.jpg");
                vocabularies.add(vocab_library_6);
                Vocabulary vocab_library_7 = new Vocabulary("library", "archive", "kho lưu trữ", "archive.jpg");
                vocabularies.add(vocab_library_7);
                Vocabulary vocab_library_8 = new Vocabulary("library", "plagiarism", "đạo văn", "plagiarism.jpg");
                vocabularies.add(vocab_library_8);
                Vocabulary vocab_library_9 = new Vocabulary("library", "academic", "học thuật", "academic.jpg");
                vocabularies.add(vocab_library_9);

                // ========== CÔNG VIÊN (Park) - 10 từ ==========
                Vocabulary vocab_park_1 = new Vocabulary("park", "park", "công viên", "park.jpg");
                vocabularies.add(vocab_park_1);
                Vocabulary vocab_park_2 = new Vocabulary("park", "bench", "ghế dài", "bench.jpg");
                vocabularies.add(vocab_park_2);
                Vocabulary vocab_park_3 = new Vocabulary("park", "playground", "sân chơi", "playground.jpg");
                vocabularies.add(vocab_park_3);
                Vocabulary vocab_park_4 = new Vocabulary("park", "picnic", "dã ngoại", "picnic.jpg");
                vocabularies.add(vocab_park_4);
                Vocabulary vocab_park_5 = new Vocabulary("park", "fountain", "đài phun nước", "fountain.jpg");
                vocabularies.add(vocab_park_5);
                Vocabulary vocab_park_6 = new Vocabulary("park", "biodiversity", "đa dạng sinh học", "biodiversity.jpg");
                vocabularies.add(vocab_park_6);
                Vocabulary vocab_park_7 = new Vocabulary("park", "ecosystem", "hệ sinh thái", "ecosystem.jpg");
                vocabularies.add(vocab_park_7);
                Vocabulary vocab_park_8 = new Vocabulary("park", "conservation", "bảo tồn", "conservation.jpg");
                vocabularies.add(vocab_park_8);
                Vocabulary vocab_park_9 = new Vocabulary("park", "exercise", "luyện tập", "exercise.jpg");
                vocabularies.add(vocab_park_9);
                Vocabulary vocab_park_10 = new Vocabulary("park", "bicycle", "đạp xe", "bicycle.jpg");
                vocabularies.add(vocab_park_10);

                // ========== QUÁN COFFEE (Coffee shop) - 10 từ ==========
                Vocabulary vocab_coffee_1 = new Vocabulary("coffee", "coffee", "cà phê", "coffee.jpg");
                vocabularies.add(vocab_coffee_1);
                Vocabulary vocab_coffee_2 = new Vocabulary("coffee", "cup", "cái tách", "cup.jpg");
                vocabularies.add(vocab_coffee_2);
                Vocabulary vocab_coffee_3 = new Vocabulary("coffee", "table", "cái bàn", "table.jpg");
                vocabularies.add(vocab_coffee_3);
                Vocabulary vocab_coffee_4 = new Vocabulary("coffee", "chair", "cái ghế", "chair.jpg");
                vocabularies.add(vocab_coffee_4);
                Vocabulary vocab_coffee_5 = new Vocabulary("coffee", "menu", "thực đơn", "menu.jpg");
                vocabularies.add(vocab_coffee_5);
                Vocabulary vocab_coffee_6 = new Vocabulary("coffee", "milk", "sữa", "milk.jpg");
                vocabularies.add(vocab_coffee_6);
                Vocabulary vocab_coffee_7 = new Vocabulary("coffee", "sugar", "đường", "sugar.jpg");
                vocabularies.add(vocab_coffee_7);
                Vocabulary vocab_coffee_8 = new Vocabulary("coffee", "ice", "đá", "ice.jpg");
                vocabularies.add(vocab_coffee_8);
                Vocabulary vocab_coffee_9 = new Vocabulary("coffee", "order", "gọi món", "order.jpg");
                vocabularies.add(vocab_coffee_9);
                Vocabulary vocab_coffee_10 = new Vocabulary("coffee", "drink", "đồ uống", "drink.jpg");
                vocabularies.add(vocab_coffee_10);

                // ========== SHOP QUẦN ÁO (Clothing shop) - 10 từ ==========
                Vocabulary vocab_clothes_1 = new Vocabulary("clothes", "clothes", "quần áo", "clothes.jpg");
                vocabularies.add(vocab_clothes_1);
                Vocabulary vocab_clothes_2 = new Vocabulary("clothes", "fashion", "thời trang", "fashion.jpg");
                vocabularies.add(vocab_clothes_2);
                Vocabulary vocab_clothes_3 = new Vocabulary("clothes", "brand", "thương hiệu", "brand.jpg");
                vocabularies.add(vocab_clothes_3);
                Vocabulary vocab_clothes_4 = new Vocabulary("clothes", "outfit", "trang phục", "outfit.jpg");
                vocabularies.add(vocab_clothes_4);
                Vocabulary vocab_clothes_5 = new Vocabulary("clothes", "material", "chất liệu", "material.jpg");
                vocabularies.add(vocab_clothes_5);
                Vocabulary vocab_clothes_6 = new Vocabulary("clothes", "collection", "bộ sưu tập", "collection.jpg");
                vocabularies.add(vocab_clothes_6);
                Vocabulary vocab_clothes_7 = new Vocabulary("clothes", "trend", "xu hướng", "trend.jpg");
                vocabularies.add(vocab_clothes_7);
                Vocabulary vocab_clothes_8 = new Vocabulary("clothes", "dress", "đầm", "dress.jpg");
                vocabularies.add(vocab_clothes_8);
                Vocabulary vocab_clothes_9 = new Vocabulary("clothes", "inventory", "hàng tồn kho", "inventory.jpg");
                vocabularies.add(vocab_clothes_9);
                Vocabulary vocab_clothes_10 = new Vocabulary("clothes", "jacket", "áo khoác", "jacket.jpg");
                vocabularies.add(vocab_clothes_10);

                // ========== TIỆM BÁNH (Bakery) - 10 từ ==========
                Vocabulary vocab_bakery_1 = new Vocabulary("bakery", "bakery", "tiệm bánh", "bakery.jpg");
                vocabularies.add(vocab_bakery_1);
                Vocabulary vocab_bakery_2 = new Vocabulary("bakery", "bread", "bánh mì", "bread.jpg");
                vocabularies.add(vocab_bakery_2);
                Vocabulary vocab_bakery_3 = new Vocabulary("bakery", "cake", "bánh ngọt", "cake.jpg");
                vocabularies.add(vocab_bakery_3);
                Vocabulary vocab_bakery_4 = new Vocabulary("bakery", "pastry", "bánh ngọt nướng", "pastry.jpg");
                vocabularies.add(vocab_bakery_4);
                Vocabulary vocab_bakery_5 = new Vocabulary("bakery", "dough", "bột nhào", "dough.jpg");
                vocabularies.add(vocab_bakery_5);
                Vocabulary vocab_bakery_6 = new Vocabulary("bakery", "yeast", "men", "yeast.jpg");
                vocabularies.add(vocab_bakery_6);
                Vocabulary vocab_bakery_7 = new Vocabulary("bakery", "oven", "lò nướng", "oven.jpg");
                vocabularies.add(vocab_bakery_7);
                Vocabulary vocab_bakery_8 = new Vocabulary("bakery", "recipe", "công thức", "recipe.jpg");
                vocabularies.add(vocab_bakery_8);
                Vocabulary vocab_bakery_9 = new Vocabulary("bakery", "chocolate", "sô-cô-la", "chocolate.jpg");
                vocabularies.add(vocab_bakery_9);
                Vocabulary vocab_bakery_10 = new Vocabulary("bakery", "handmade", "thủ công", "handmade.jpg");
                vocabularies.add(vocab_bakery_10);

                // ========== NHÀ Ở (House / Home) - 8 từ ==========
                Vocabulary vocab_house_1 = new Vocabulary("house", "house", "ngôi nhà", "house.jpg");
                vocabularies.add(vocab_house_1);
                Vocabulary vocab_house_2 = new Vocabulary("house", "room", "phòng", "room.jpg");
                vocabularies.add(vocab_house_2);
                Vocabulary vocab_house_3 = new Vocabulary("house", "furniture", "nội thất", "furniture.jpg");
                vocabularies.add(vocab_house_3);
                Vocabulary vocab_house_4 = new Vocabulary("house", "kitchen", "nhà bếp", "kitchen.jpg");
                vocabularies.add(vocab_house_4);
                Vocabulary vocab_house_5 = new Vocabulary("house", "bathroom", "phòng tắm", "bathroom.jpg");
                vocabularies.add(vocab_house_5);
                Vocabulary vocab_house_6 = new Vocabulary("house", "balcony", "ban công", "balcony.jpg");
                vocabularies.add(vocab_house_6);
                Vocabulary vocab_house_7 = new Vocabulary("house", "smart home", "nhà thông minh", "smart_home.jpg");
                vocabularies.add(vocab_house_7);
                Vocabulary vocab_house_8 = new Vocabulary("house", "ventilation", "thông gió", "ventilation.jpg");
                vocabularies.add(vocab_house_8);

                if (!vocabularies.isEmpty()) {
                    // Xóa dữ liệu cũ trước khi import mới để tránh duplicate
                    for (String topicId : new String[]{"farm", "school", "library", "park", "coffee", "clothes", "bakery", "house", "general"}) {
                        appDatabase.vocabularyDao().deleteVocabulariesByTopic(topicId);
                    }
                    Log.d(TAG, "Deleted old vocabularies for all topics");
                    
                    // Import dữ liệu mới
                    appDatabase.vocabularyDao().insertAll(vocabularies);
                    Log.d(TAG, "Successfully imported " + vocabularies.size() + " vocabularies across all topics");
                    
                    // Đếm số từ vựng có hình ảnh
                    long withImages = vocabularies.stream()
                        .filter(v -> v.imageName != null && !v.imageName.isEmpty())
                        .count();
                    Log.d(TAG, "Vocabularies with images: " + withImages + "/" + vocabularies.size());
                } else {
                    Log.w(TAG, "No vocabularies to import!");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error importing vocabularies", e);
                e.printStackTrace();
            }
        });
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
