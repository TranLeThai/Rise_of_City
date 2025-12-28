package com.example.rise_of_city.data.model.learning;

import android.content.Context;
import com.example.rise_of_city.data.model.learning.quiz.*;
import com.example.rise_of_city.data.model.learning.quiz.CHOICE.ChoiceQuestion;
import com.example.rise_of_city.data.model.learning.quiz.INPUT.WritingQuestion;
import com.example.rise_of_city.data.model.learning.quiz.MATCHING.MatchingIMGQuestion;
import com.example.rise_of_city.data.model.learning.quiz.MatchingTextQuestion;
import com.example.rise_of_city.data.model.learning.quiz.TEXT_INTERACT.LectureQuestion;
import com.example.rise_of_city.data.model.learning.quiz.DECISION.TrueFalseQuestion;
import com.example.rise_of_city.data.model.learning.quiz.ORDERING.SentenceOrderQuestion;
import com.example.rise_of_city.data.model.learning.quiz.ORDERING.WordOrderQuestion;
import com.example.rise_of_city.data.model.learning.quiz.LISTENING.ListeningQuestion;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ExcelReader {

    private Context context;

    public ExcelReader(Context context) {
        this.context = context;
    }

    /**
     * Đọc file Excel từ assets và chuyển thành danh sách BaseQuestion.
     */
    public List<BaseQuestion> readLessonFromExcel(String fileName) {
        List<BaseQuestion> questionList = new ArrayList<>();

        try {
            InputStream is = context.getAssets().open(fileName);
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);

            // Duyệt từng dòng (Bỏ qua dòng tiêu đề index 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String typeStr = getCellString(row, 0); // Cột A: Question Type
                if (typeStr.isEmpty()) continue;

                BaseQuestion.QuestionType type = BaseQuestion.QuestionType.valueOf(typeStr.toUpperCase());
                String id = getCellString(row, 1);     // Cột B: ID
                String title = getCellString(row, 2);  // Cột C: Title
                int order = (int) row.getCell(3).getNumericCellValue(); // Cột D: Order

                switch (type) {
                    case LECTURE:
                        questionList.add(parseLecture(row, id, title, order));
                        break;
                    case CHOICE:
                        questionList.add(parseChoice(row, id, title, order));
                        break;
                    case MATCHINGTEXT:
                        questionList.add(parseMatchingText(row, id, title, order));
                        break;
                    case MATCHINGIMG:
                        questionList.add(parseMatchingImg(row, id, title, order));
                        break;
                    case INPUT:
                        questionList.add(parseWriting(row, id, title, order));
                        break;
                    case DECISION:
                        questionList.add(parseDecision(row, id, title, order));
                        break;
                    case SENTENCEORDERING:
                        questionList.add(parseSentenceOrdering(row, id, title, order));
                        break;
                    case WORDORDERING:
                        questionList.add(parseWordOrdering(row, id, title, order));
                        break;
                    case LISTENING:
                        questionList.add(parseListening(row, id, title, order));
                        break;
                    default:
                        break;
                }
            }
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return questionList;
    }

    // --- CÁC HÀM PARSE CHI TIẾT CHO TỪNG LOẠI ---

    private LectureQuestion parseLecture(Row row, String id, String title, int order) {
        String en = getCellString(row, 4); // Nội dung TA
        String vi = getCellString(row, 5); // Nội dung TV

        List<LectureQuestion.WrongWordInfo> errors = new ArrayList<>();

        // Parse W1
        String w1_original = getCellString(row, 6);
        String w1_options_str = getCellString(row, 7);
        String w1_correct = getCellString(row, 8);
        if (!w1_original.isEmpty()) {
            List<String> w1_options = Arrays.asList(w1_options_str.split("\\s*,\\s*"));
            errors.add(new LectureQuestion.WrongWordInfo(w1_original, w1_options, w1_correct));
        }

        // Parse W2
        String w2_original = getCellString(row, 9);
        String w2_options_str = getCellString(row, 10);
        String w2_correct = getCellString(row, 11);
        if (!w2_original.isEmpty()) {
            List<String> w2_options = Arrays.asList(w2_options_str.split("\\s*,\\s*"));
            errors.add(new LectureQuestion.WrongWordInfo(w2_original, w2_options, w2_correct));
        }

        return new LectureQuestion(id, title, order, en, vi, errors);
    }

    private ChoiceQuestion parseChoice(Row row, String id, String title, int order) {
        ChoiceQuestion.ChoiceType subType = ChoiceQuestion.ChoiceType.valueOf(getCellString(row, 4));
        String content = getCellString(row, 5);
        List<String> options = Arrays.asList(getCellString(row, 6).split("\\s*/\\s*"));
        int correctIdx = (int) row.getCell(7).getNumericCellValue();
        String exp = getCellString(row, 8);

        return new ChoiceQuestion(id, title, order, subType, content, options, correctIdx, exp);
    }

    private WritingQuestion parseWriting(Row row, String id, String title, int order) {
        return new WritingQuestion(id, title, order,
                getCellString(row, 4), getCellString(row, 5), getCellString(row, 6));
    }

    private MatchingTextQuestion parseMatchingText(Row row, String id, String title, int order) {
        List<String> en = Arrays.asList(getCellString(row, 4).split("\\s*/\\s*"));
        List<String> vi = Arrays.asList(getCellString(row, 5).split("\\s*/\\s*"));
        return new MatchingTextQuestion(id, title, order, en, vi);
    }

    private MatchingIMGQuestion parseMatchingImg(Row row, String id, String title, int order) {
        // 1. Đọc chuỗi thô từ cột 4 (Image_Names) và cột 5 (Words_List)
        String imageNamesRaw = getCellString(row, 4); // Cột E
        String wordsRaw = getCellString(row, 5);      // Cột F

        // 2. Xử lý danh sách Hình ảnh
        List<String> tempImageNames = Arrays.asList(imageNamesRaw.split("\\s*/\\s*"));
        List<Integer> finalImageResIds = new ArrayList<>();

        for (String name : tempImageNames) {
            // Chuyển tên ảnh thành Resource ID (R.drawable...)
            int resId = context.getResources().getIdentifier(name.trim(), "drawable", context.getPackageName());
            finalImageResIds.add(resId);
        }
        // Xáo trộn ngẫu nhiên danh sách ID ảnh
        java.util.Collections.shuffle(finalImageResIds);

        // 3. Xử lý danh sách Từ vựng
        List<String> finalWords = new ArrayList<>(Arrays.asList(wordsRaw.split("\\s*/\\s*")));
        // Xáo trộn ngẫu nhiên danh sách từ vựng
        java.util.Collections.shuffle(finalWords);

        // 4. Trả về đối tượng MatchingIMGQuestion với 2 danh sách đã xáo trộn độc lập
        return new MatchingIMGQuestion(id, title, order, finalImageResIds, finalWords);
    }

    private TrueFalseQuestion parseDecision(Row row, String id, String title, int order) {
        String image = getCellString(row, 4);
        String en = getCellString(row, 5);
        String correctStr = getCellString(row, 6);
        boolean isTrue = "TRUE".equalsIgnoreCase(correctStr);
        String exp = getCellString(row, 7);
        return new TrueFalseQuestion(id, title, order, image, en, isTrue, exp);
    }

    private SentenceOrderQuestion parseSentenceOrdering(Row row, String id, String title, int order) {

        // cột F: câu đúng hoàn chỉnh
        String correct = getCellString(row, 4).trim();

        // Tách thành các từ theo khoảng trắng
        List<String> scrambled = new ArrayList<>(Arrays.asList(correct.split("\\s+")));

        // Xáo trộn danh sách từ
        Collections.shuffle(scrambled);
        return new SentenceOrderQuestion(id, title, order, scrambled, correct);
    }

    private WordOrderQuestion parseWordOrdering(Row row, String id, String title, int order) {
        // 1. Lấy từ đúng từ cột 4 (Index 4)
        String correctWord = getCellString(row, 4);

        if (correctWord.isEmpty()) {
            return new WordOrderQuestion(id, title, order, "", "");
        }

        // 2. Chuyển từ thành danh sách các chữ cái để xáo trộn
        List<String> letters = new ArrayList<>(Arrays.asList(correctWord.split("")));

        // Loại bỏ khoảng trắng nếu có để tránh lỗi hiển thị
        letters.removeIf(String::isEmpty);

        // 3. Xáo trộn ngẫu nhiên danh sách chữ cái
        java.util.Collections.shuffle(letters);

        // 4. Nối các chữ cái lại bằng ký hiệu '/' (Ví dụ: a/p/p/l/e)
        // Sử dụng StringBuilder hoặc String.join (Java 8+)
        StringBuilder scrambledBuilder = new StringBuilder();
        for (int i = 0; i < letters.size(); i++) {
            scrambledBuilder.append(letters.get(i));
            if (i < letters.size() - 1) {
                scrambledBuilder.append("/");
            }
        }
        String scrambledLetters = scrambledBuilder.toString();

        return new WordOrderQuestion(id, title, order, scrambledLetters, correctWord);
    }

    private ListeningQuestion parseListening(Row row, String id, String title, int order) {
        // 1. Lấy dữ liệu theo các cột trong hình ảnh mới
        String audioPath = getCellString(row, 4);   // Cột E (4): Audio_Path
        String optionsRaw = getCellString(row, 5);  // Cột F (5): Options_List
        String content = getCellString(row, 7);     // Cột H (7): Question_Content

        // Đọc Index đáp án đúng ban đầu
        int originalCorrectIdx = 0;
        try {
            Cell correctCell = row.getCell(6);      // Cột G (6): Correct_Index
            if (correctCell.getCellType() == CellType.NUMERIC) {
                originalCorrectIdx = (int) correctCell.getNumericCellValue();
            } else {
                originalCorrectIdx = Integer.parseInt(getCellString(row, 6));
            }
        } catch (Exception e) { originalCorrectIdx = 0; }

        String transcript = getCellString(row, 8);  // Cột I (8): Transcript (nếu có)

        // 2. Xử lý xáo trộn (Shuffle) đáp án
        List<String> options = new ArrayList<>(Arrays.asList(optionsRaw.split("\\s*/\\s*")));

        // Lưu giá trị đúng trước khi xáo trộn để tìm lại Index sau này
        String correctValue = options.get(originalCorrectIdx);

        // Xáo trộn danh sách
        java.util.Collections.shuffle(options);

        // Tìm Index mới sau khi xáo trộn
        int newCorrectIdx = options.indexOf(correctValue);

        // 3. Trả về đối tượng (Đảm bảo Constructor trong Model đã nhận tham số 'content')
        return new ListeningQuestion(id, title, order, audioPath, content, options, newCorrectIdx, transcript);
    }

    // Helper: Đọc ô an toàn tránh lỗi Null
    private String getCellString(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return "";
        return cell.toString().trim();
    }
}