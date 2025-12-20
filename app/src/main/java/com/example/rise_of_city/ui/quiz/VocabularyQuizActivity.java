package com.example.rise_of_city.ui.quiz;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.rise_of_city.R;
import com.example.rise_of_city.data.model.Vocabulary;
import com.example.rise_of_city.data.repository.BuildingProgressRepository;
import com.example.rise_of_city.data.repository.BuildingHarvestRepository;
import com.example.rise_of_city.data.repository.GoldRepository;
import com.example.rise_of_city.data.repository.LearningLogRepository;
import com.example.rise_of_city.data.repository.QuestRepository;
import com.example.rise_of_city.data.repository.UserStatsRepository;
import com.example.rise_of_city.data.repository.VocabularyRepository;
import com.example.rise_of_city.data.repository.GrammarQuizRepository;
import com.example.rise_of_city.data.repository.WritingQuizRepository;
import com.example.rise_of_city.data.repository.SentenceCompletionQuizRepository;
import com.example.rise_of_city.data.repository.WordOrderQuizRepository;
import com.example.rise_of_city.data.repository.SynonymAntonymQuizRepository;
import com.example.rise_of_city.data.model.GrammarQuiz;
import com.example.rise_of_city.data.model.WritingQuiz;
import com.example.rise_of_city.data.model.SentenceCompletionQuiz;
import com.example.rise_of_city.data.model.WordOrderQuiz;
import com.example.rise_of_city.data.model.SynonymAntonymQuiz;
import com.example.rise_of_city.ui.dialog.AnswerCorrectDialogFragment;
import com.example.rise_of_city.ui.dialog.AnswerWrongDialogFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VocabularyQuizActivity extends AppCompatActivity {

    private TextView tvQuestion;
    // ivIllustration removed - not used in new layout design
    private Button btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4;
    private Button btnCheck;
    private Button btnNext;
    private TextView tvTimer;
    private ProgressBar progressTimer;
    private ImageView ivFeedback1, ivFeedback2, ivFeedback3, ivFeedback4;
    
    private Vocabulary correctVocabulary;
    private List<Vocabulary> wrongOptions;
    private String selectedAnswer;
    private boolean answerSelected = false;
    
    // Timer
    private Handler timerHandler;
    private Runnable timerRunnable;
    private int timeRemaining = 15; // 15 seconds
    private boolean timerRunning = false;
    
    private VocabularyRepository vocabRepository;
    private BuildingProgressRepository buildingProgressRepo;
    private LearningLogRepository learningLogRepo;
    private GoldRepository goldRepo;
    private BuildingHarvestRepository harvestRepo;
    private QuestRepository questRepo;
    private GrammarQuizRepository grammarQuizRepo;
    private WritingQuizRepository writingQuizRepo;
    private SentenceCompletionQuizRepository sentenceCompletionQuizRepo;
    private WordOrderQuizRepository wordOrderQuizRepo;
    private SynonymAntonymQuizRepository synonymAntonymQuizRepo;
    private GrammarQuiz currentGrammarQuiz; // Grammar quiz hiện tại (nếu quizType = "grammar")
    private WritingQuiz currentWritingQuiz; // Writing quiz hiện tại
    private SentenceCompletionQuiz currentSentenceCompletionQuiz; // Sentence completion quiz hiện tại
    private WordOrderQuiz currentWordOrderQuiz; // Word order quiz hiện tại
    private SynonymAntonymQuiz currentSynonymAntonymQuiz; // Synonym/Antonym quiz hiện tại
    private String buildingId; // ID của building đang quiz (từ intent)
    private boolean isMission = false; // Flag để biết đây là quiz từ Mission hay quiz thông thường
    private String quizType; // "vocabulary", "grammar", "reading", "writing", "sentence_completion", "word_order", "synonym_antonym"
    private String topicId; // Topic ID cho grammar quiz (từ intent)
    
    // Phần thưởng sẽ được tính dựa trên level building (trong updateBuildingProgress)
    private int buildingLevel = 1;
    
    // Quest info để track progress
    private String currentQuestId; // Quest ID đang làm
    private int currentQuestMaxProgress; // Max progress của quest đang làm
    private int currentQuestProgress; // Progress hiện tại của quest

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_quiz);

        // Lấy buildingId từ intent (nếu có)
        buildingId = getIntent().getStringExtra("buildingId");
        if (buildingId == null) {
            buildingId = "house"; // Default
        }
        
        // Kiểm tra xem đây có phải là quiz từ Mission không
        isMission = getIntent().getBooleanExtra("isMission", false);
        
        // Lấy quizType và topicId từ intent (cho grammar quiz từ quest)
        quizType = getIntent().getStringExtra("quizType");
        topicId = getIntent().getStringExtra("topicId");
        
        // Nếu là grammar quiz từ quest, KHÔNG dùng buildingId (không load vocabulary từ building)
        if ("grammar".equals(quizType) && topicId != null) {
            buildingId = null; // Không dùng building vocabulary cho grammar quiz
        }

        // Khởi tạo repositories
        vocabRepository = VocabularyRepository.getInstance();
        buildingProgressRepo = BuildingProgressRepository.getInstance();
        learningLogRepo = LearningLogRepository.getInstance();
        goldRepo = GoldRepository.getInstance();
        harvestRepo = BuildingHarvestRepository.getInstance();
        questRepo = QuestRepository.getInstance();
        grammarQuizRepo = GrammarQuizRepository.getInstance();
        writingQuizRepo = WritingQuizRepository.getInstance();
        sentenceCompletionQuizRepo = SentenceCompletionQuizRepository.getInstance();
        wordOrderQuizRepo = WordOrderQuizRepository.getInstance();
        synonymAntonymQuizRepo = SynonymAntonymQuizRepository.getInstance();
        
        // Load building level để tính phần thưởng
        loadBuildingLevel();

        initViews();
        
        // Load quiz dựa trên quizType
        if ("grammar".equals(quizType) && topicId != null) {
            loadGrammarQuizFromFirebase();
        } else if ("writing".equals(quizType)) {
            loadWritingQuizFromFirebase();
        } else if ("sentence_completion".equals(quizType)) {
            loadSentenceCompletionQuizFromFirebase();
        } else if ("word_order".equals(quizType)) {
            loadWordOrderQuizFromFirebase();
        } else if ("synonym_antonym".equals(quizType)) {
            loadSynonymAntonymQuizFromFirebase();
        } else {
            // Default: vocabulary quiz (hoặc reading quiz sẽ xử lý sau)
            loadQuizFromFirebase();
        }
        
        setupAnswerButtons();
        setupCheckButton();
    }

    private void initViews() {
        ImageButton btnClose = findViewById(R.id.btn_close);
        tvQuestion = findViewById(R.id.tv_question);
        // ivIllustration removed from new layout design
        btnAnswer1 = findViewById(R.id.btn_answer1);
        btnAnswer2 = findViewById(R.id.btn_answer2);
        btnAnswer3 = findViewById(R.id.btn_answer3);
        btnAnswer4 = findViewById(R.id.btn_answer4);
        btnCheck = findViewById(R.id.btn_check);
        btnNext = findViewById(R.id.btn_next);
        tvTimer = findViewById(R.id.tv_timer);
        progressTimer = findViewById(R.id.progress_timer);
        ivFeedback1 = findViewById(R.id.iv_feedback1);
        ivFeedback2 = findViewById(R.id.iv_feedback2);
        ivFeedback3 = findViewById(R.id.iv_feedback3);
        ivFeedback4 = findViewById(R.id.iv_feedback4);

        btnClose.setOnClickListener(v -> finish());
        
        // Setup Next button
        btnNext.setOnClickListener(v -> {
            // Navigate to next question or finish
            finish();
        });
        
        // Initialize timer
        timerHandler = new Handler(Looper.getMainLooper());
        initTimer();
    }
    
    private void initTimer() {
        timeRemaining = 15;
        progressTimer.setMax(15);
        progressTimer.setProgress(15);
        updateTimerDisplay();
        
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (timeRemaining > 0 && timerRunning) {
                    timeRemaining--;
                    updateTimerDisplay();
                    timerHandler.postDelayed(this, 1000);
                } else if (timeRemaining == 0 && timerRunning) {
                    // Time's up - auto check answer if selected
                    if (answerSelected) {
                        btnCheck.performClick();
                    } else {
                        Toast.makeText(VocabularyQuizActivity.this, "Hết thời gian!", Toast.LENGTH_SHORT).show();
                        stopTimer();
                    }
                }
            }
        };
    }
    
    private void updateTimerDisplay() {
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        String timeString = String.format("%02d:%02d", minutes, seconds);
        tvTimer.setText(timeString);
        progressTimer.setProgress(timeRemaining);
    }
    
    private void startTimer() {
        if (!timerRunning) {
            timerRunning = true;
            timerHandler.post(timerRunnable);
        }
    }
    
    private void stopTimer() {
        timerRunning = false;
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    /**
     * Load quiz từ Firebase
     */
    private void loadQuizFromFirebase() {
        // Hiển thị loading
        tvQuestion.setText("Đang tải câu hỏi...");
        btnCheck.setEnabled(false);
        
        // Lấy từ vựng theo buildingId (nếu có)
        vocabRepository.getQuizOptions(buildingId, new VocabularyRepository.OnQuizOptionsLoadedListener() {
            @Override
            public void onQuizOptionsLoaded(Vocabulary correctVocab, List<Vocabulary> wrongOpts) {
                correctVocabulary = correctVocab;
                wrongOptions = wrongOpts;
                
                // Setup câu hỏi
                setupQuestion();
                // Start timer after question is loaded
                startTimer();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(VocabularyQuizActivity.this, 
                    "Lỗi: " + error, Toast.LENGTH_LONG).show();
                // Fallback: dùng dữ liệu mẫu
                setupFallbackQuestion();
            }
        });
    }
    
    /**
     * Load grammar quiz từ Firebase (CHỈ dùng cho quest, KHÔNG phải vocabulary từ building)
     */
    private void loadGrammarQuizFromFirebase() {
        // Hiển thị loading
        tvQuestion.setText("Đang tải câu hỏi ngữ pháp...");
        btnCheck.setEnabled(false);
        // Illustration removed from new layout design
        
        // Load grammar quiz theo topicId
        grammarQuizRepo.getRandomQuizByTopic(topicId, new GrammarQuizRepository.OnGrammarQuizLoadedListener() {
            @Override
            public void onQuizLoaded(GrammarQuiz quiz) {
                currentGrammarQuiz = quiz;
                setupGrammarQuestion();
                startTimer();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(VocabularyQuizActivity.this, 
                    "Lỗi: " + error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
    
    /**
     * Setup câu hỏi grammar quiz
     */
    private void setupGrammarQuestion() {
        if (currentGrammarQuiz == null) {
            Toast.makeText(this, "Không tìm thấy câu hỏi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Reset UI state
        hideNextButton();
        resetAnswerButtons();
        enableAllButtons();
        answerSelected = false;
        
        // Set câu hỏi
        tvQuestion.setText(currentGrammarQuiz.getQuestion());
        
        // Set đáp án
        List<String> options = currentGrammarQuiz.getOptions();
        if (options != null && options.size() >= 4) {
            btnAnswer1.setText(options.get(0));
            btnAnswer2.setText(options.get(1));
            btnAnswer3.setText(options.get(2));
            btnAnswer4.setText(options.get(3));
        }
        
        // Enable nút check
        btnCheck.setEnabled(true);
    }
    
    /**
     * Load writing quiz từ Firebase
     */
    private void loadWritingQuizFromFirebase() {
        tvQuestion.setText("Đang tải câu hỏi điền từ...");
        btnCheck.setEnabled(false);
        // Illustration removed from new layout design
        
        writingQuizRepo.getRandomQuiz(new WritingQuizRepository.OnWritingQuizLoadedListener() {
            @Override
            public void onQuizLoaded(WritingQuiz quiz) {
                currentWritingQuiz = quiz;
                setupWritingQuestion();
                startTimer();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(VocabularyQuizActivity.this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
    
    /**
     * Setup writing question - chuyển thành multiple choice
     */
    private void setupWritingQuestion() {
        if (currentWritingQuiz == null) {
            Toast.makeText(this, "Không tìm thấy câu hỏi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Reset UI state
        hideNextButton();
        resetAnswerButtons();
        enableAllButtons();
        answerSelected = false;
        
        // Set câu hỏi
        tvQuestion.setText(currentWritingQuiz.getSentence());
        
        // Tạo options: answer + 3 distractors thông minh
        String answer = currentWritingQuiz.getAnswer().toLowerCase().trim();
        List<String> options = new ArrayList<>();
        options.add(answer);
        
        // Tạo distractors dựa trên answer để phù hợp với ngữ cảnh
        List<String> distractors = generateWritingDistractors(answer);
        options.addAll(distractors);
        
        // Đảm bảo có đủ 4 options
        while (options.size() < 4) {
            // Nếu thiếu, thêm các từ phổ biến
            String[] commonWords = {"is", "are", "was", "were", "has", "have", "had", "do", "does", "did", "can", "could", "will", "would"};
            for (String word : commonWords) {
                if (!options.contains(word) && !word.equals(answer)) {
                    options.add(word);
                    break;
                }
            }
        }
        
        // Chỉ lấy 4 options đầu tiên (1 answer + 3 distractors)
        while (options.size() > 4) {
            options.remove(options.size() - 1);
        }
        
        // Shuffle để tránh answer luôn ở vị trí đầu
        Collections.shuffle(options);
        
        btnAnswer1.setText(options.get(0));
        btnAnswer2.setText(options.get(1));
        btnAnswer3.setText(options.get(2));
        btnAnswer4.setText(options.get(3));
        
        btnCheck.setEnabled(true);
    }
    
    /**
     * Tạo distractors thông minh cho writing quiz dựa trên answer
     */
    private List<String> generateWritingDistractors(String answer) {
        List<String> distractors = new ArrayList<>();
        
        // Map các từ phổ biến với các từ dễ nhầm lẫn
        String[][] similarWords = {
            {"go", "goes", "went", "going"},
            {"is", "are", "was", "were", "be", "been"},
            {"have", "has", "had"},
            {"do", "does", "did", "done"},
            {"can", "could", "will", "would", "should"},
            {"get", "got", "gets", "getting"},
            {"make", "made", "makes", "making"},
            {"take", "took", "takes", "taking"},
            {"come", "came", "comes", "coming"},
            {"see", "saw", "sees", "seeing"},
            {"know", "knew", "knows", "known"},
            {"think", "thought", "thinks", "thinking"},
            {"say", "said", "says", "saying"},
            {"tell", "told", "tells", "telling"}
        };
        
        // Tìm group chứa answer
        String[] similarGroup = null;
        for (String[] group : similarWords) {
            for (String word : group) {
                if (word.equals(answer)) {
                    similarGroup = group;
                    break;
                }
            }
            if (similarGroup != null) break;
        }
        
        if (similarGroup != null) {
            // Lấy các từ khác trong cùng group
            for (String word : similarGroup) {
                if (!word.equals(answer) && distractors.size() < 3) {
                    distractors.add(word);
                }
            }
        }
        
        // Nếu chưa đủ, thêm các từ phổ biến khác
        String[] commonWords = {"is", "are", "was", "were", "has", "have", "had", "do", "does", "did", "can", "could", "will", "would", "go", "goes", "went"};
        for (String word : commonWords) {
            if (!word.equals(answer) && !distractors.contains(word) && distractors.size() < 3) {
                distractors.add(word);
            }
        }
        
        return distractors;
    }
    
    /**
     * Load sentence completion quiz từ Firebase
     */
    private void loadSentenceCompletionQuizFromFirebase() {
        tvQuestion.setText("Đang tải câu hỏi hoàn thành câu...");
        btnCheck.setEnabled(false);
        // Illustration removed from new layout design
        
        sentenceCompletionQuizRepo.getRandomQuiz(new SentenceCompletionQuizRepository.OnSentenceCompletionQuizLoadedListener() {
            @Override
            public void onQuizLoaded(SentenceCompletionQuiz quiz) {
                currentSentenceCompletionQuiz = quiz;
                setupSentenceCompletionQuestion();
                startTimer();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(VocabularyQuizActivity.this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
    
    /**
     * Setup sentence completion question
     */
    private void setupSentenceCompletionQuestion() {
        if (currentSentenceCompletionQuiz == null) {
            Toast.makeText(this, "Không tìm thấy câu hỏi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Reset UI state
        hideNextButton();
        resetAnswerButtons();
        enableAllButtons();
        answerSelected = false;
        
        tvQuestion.setText(currentSentenceCompletionQuiz.getSentence());
        
        List<String> options = currentSentenceCompletionQuiz.getOptions();
        if (options != null && options.size() >= 4) {
            btnAnswer1.setText(options.get(0));
            btnAnswer2.setText(options.get(1));
            btnAnswer3.setText(options.get(2));
            btnAnswer4.setText(options.get(3));
        }
        
        btnCheck.setEnabled(true);
    }
    
    /**
     * Load word order quiz từ Firebase
     */
    private void loadWordOrderQuizFromFirebase() {
        tvQuestion.setText("Đang tải câu hỏi sắp xếp từ...");
        btnCheck.setEnabled(false);
        // Illustration removed from new layout design
        
        wordOrderQuizRepo.getRandomQuiz(new WordOrderQuizRepository.OnWordOrderQuizLoadedListener() {
            @Override
            public void onQuizLoaded(WordOrderQuiz quiz) {
                currentWordOrderQuiz = quiz;
                setupWordOrderQuestion();
                startTimer();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(VocabularyQuizActivity.this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
    
    /**
     * Setup word order question - tạo các câu đã sắp xếp làm options
     */
    private void setupWordOrderQuestion() {
        if (currentWordOrderQuiz == null) {
            Toast.makeText(this, "Không tìm thấy câu hỏi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Reset UI state
        hideNextButton();
        resetAnswerButtons();
        enableAllButtons();
        answerSelected = false;
        
        // Hiển thị các từ cần sắp xếp
        List<String> words = currentWordOrderQuiz.getWords();
        tvQuestion.setText("Sắp xếp các từ sau thành câu đúng:\n" + String.join(" / ", words));
        
        // Tạo câu đúng
        List<Integer> correctOrder = currentWordOrderQuiz.getCorrectOrder();
        StringBuilder correctSentence = new StringBuilder();
        for (int i = 0; i < correctOrder.size(); i++) {
            if (i > 0) correctSentence.append(" ");
            correctSentence.append(words.get(correctOrder.get(i)));
        }
        String correctAnswer = correctSentence.toString();
        
        // Tạo options: câu đúng + 3 câu sai (sắp xếp lại một chút)
        List<String> options = new ArrayList<>();
        options.add(correctAnswer);
        
        // Tạo các câu sai bằng cách sắp xếp lại
        if (correctOrder.size() >= 2) {
            // Option 2: swap từ đầu và từ thứ 2 (nếu có ít nhất 2 từ)
            StringBuilder wrong1 = new StringBuilder();
            for (int i = 0; i < correctOrder.size(); i++) {
                if (i > 0) wrong1.append(" ");
                int idx;
                if (i == 0 && correctOrder.size() > 1) {
                    idx = correctOrder.get(1);
                } else if (i == 1) {
                    idx = correctOrder.get(0);
                } else {
                    idx = correctOrder.get(i);
                }
                wrong1.append(words.get(idx));
            }
            if (!wrong1.toString().equals(correctAnswer)) {
                options.add(wrong1.toString());
            }
            
            // Option 3: reverse order
            StringBuilder wrong2 = new StringBuilder();
            for (int i = correctOrder.size() - 1; i >= 0; i--) {
                if (i < correctOrder.size() - 1) wrong2.append(" ");
                wrong2.append(words.get(correctOrder.get(i)));
            }
            if (!wrong2.toString().equals(correctAnswer)) {
                options.add(wrong2.toString());
            }
        }
        
        // Option 4: random order (luôn tạo để có đủ 4 options)
        List<Integer> shuffledOrder = new ArrayList<>(correctOrder);
        Collections.shuffle(shuffledOrder);
        StringBuilder wrong3 = new StringBuilder();
        for (int i = 0; i < shuffledOrder.size(); i++) {
            if (i > 0) wrong3.append(" ");
            wrong3.append(words.get(shuffledOrder.get(i)));
        }
        // Đảm bảo không trùng với correctAnswer
        int attempts = 0;
        while (wrong3.toString().equals(correctAnswer) && attempts < 10) {
            Collections.shuffle(shuffledOrder);
            wrong3 = new StringBuilder();
            for (int i = 0; i < shuffledOrder.size(); i++) {
                if (i > 0) wrong3.append(" ");
                wrong3.append(words.get(shuffledOrder.get(i)));
            }
            attempts++;
        }
        options.add(wrong3.toString());
        
        // Đảm bảo có đủ 4 options (lặp lại nếu cần)
        while (options.size() < 4) {
            options.add(options.get(options.size() - 1)); // Lặp lại option cuối
        }
        
        Collections.shuffle(options);
        
        btnAnswer1.setText(options.get(0));
        btnAnswer2.setText(options.get(1));
        btnAnswer3.setText(options.get(2));
        btnAnswer4.setText(options.get(3));
        
        btnCheck.setEnabled(true);
    }
    
    /**
     * Load synonym/antonym quiz từ Firebase
     */
    private void loadSynonymAntonymQuizFromFirebase() {
        tvQuestion.setText("Đang tải câu hỏi từ đồng nghĩa/trái nghĩa...");
        btnCheck.setEnabled(false);
        // Illustration removed from new layout design
        
        synonymAntonymQuizRepo.getRandomQuiz(new SynonymAntonymQuizRepository.OnSynonymAntonymQuizLoadedListener() {
            @Override
            public void onQuizLoaded(SynonymAntonymQuiz quiz) {
                currentSynonymAntonymQuiz = quiz;
                setupSynonymAntonymQuestion();
                startTimer();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(VocabularyQuizActivity.this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
    
    /**
     * Setup synonym/antonym question
     */
    private void setupSynonymAntonymQuestion() {
        if (currentSynonymAntonymQuiz == null) {
            Toast.makeText(this, "Không tìm thấy câu hỏi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Reset UI state
        hideNextButton();
        resetAnswerButtons();
        enableAllButtons();
        answerSelected = false;
        
        tvQuestion.setText(currentSynonymAntonymQuiz.getQuestion());
        
        List<String> options = currentSynonymAntonymQuiz.getOptions();
        if (options != null && options.size() >= 4) {
            btnAnswer1.setText(options.get(0));
            btnAnswer2.setText(options.get(1));
            btnAnswer3.setText(options.get(2));
            btnAnswer4.setText(options.get(3));
        }
        
        btnCheck.setEnabled(true);
    }
    
    private void setupQuestion() {
        if (correctVocabulary == null || wrongOptions == null || wrongOptions.size() < 3) {
            setupFallbackQuestion();
            return;
        }
        
        // Reset UI state
        hideNextButton();
        resetAnswerButtons();
        enableAllButtons();
        answerSelected = false;
        
        // Tạo câu hỏi
        String question = "Từ nào có nghĩa là '" + correctVocabulary.getVietnamese() + "'?";
        tvQuestion.setText(question);
        
        // Illustration removed from new layout design - no longer needed
        // Image loading code removed as illustration is not part of new design
        
        // Tạo danh sách đáp án (1 đúng + 3 sai)
        List<String> answers = new ArrayList<>();
        answers.add(correctVocabulary.getEnglish().toUpperCase());
        answers.add(wrongOptions.get(0).getEnglish().toUpperCase());
        answers.add(wrongOptions.get(1).getEnglish().toUpperCase());
        answers.add(wrongOptions.get(2).getEnglish().toUpperCase());
        Collections.shuffle(answers);
        
        // Gán đáp án vào các buttons
        btnAnswer1.setText(answers.get(0));
        btnAnswer2.setText(answers.get(1));
        btnAnswer3.setText(answers.get(2));
        btnAnswer4.setText(answers.get(3));
        
        // Enable nút check
        btnCheck.setEnabled(true);
    }

    /**
     * Fallback: Dùng dữ liệu mẫu nếu không load được từ Firebase
     */
    private void setupFallbackQuestion() {
        // Reset UI state
        hideNextButton();
        resetAnswerButtons();
        enableAllButtons();
        answerSelected = false;
        
        String question = "Từ nào có nghĩa là 'Ngôi Nhà'?";
        tvQuestion.setText(question);
        // Illustration removed from new layout design
        
        List<String> answers = new ArrayList<>();
        answers.add("HOUSE");
        answers.add("MOUSE");
        answers.add("HORSE");
        answers.add("HOSE");
        Collections.shuffle(answers);
        
        btnAnswer1.setText(answers.get(0));
        btnAnswer2.setText(answers.get(1));
        btnAnswer3.setText(answers.get(2));
        btnAnswer4.setText(answers.get(3));
        
        // Tạo vocabulary mẫu
        correctVocabulary = new Vocabulary("HOUSE", "Ngôi nhà", null);
        
        btnCheck.setEnabled(true);
    }

    private void setupAnswerButtons() {
        View.OnClickListener answerClickListener = v -> {
            // Reset tất cả buttons về trạng thái mặc định
            resetAnswerButtons();
            
            // Highlight button được chọn
            Button selectedBtn = (Button) v;
            selectedBtn.setBackgroundResource(R.drawable.bg_answer_green_selected);
            selectedAnswer = selectedBtn.getText().toString();
            answerSelected = true;
            
            // Show feedback immediately when answer is selected
            showAnswerFeedback(selectedBtn);
        };

        btnAnswer1.setOnClickListener(answerClickListener);
        btnAnswer2.setOnClickListener(answerClickListener);
        btnAnswer3.setOnClickListener(answerClickListener);
        btnAnswer4.setOnClickListener(answerClickListener);
    }
    
    private void showAnswerFeedback(Button selectedBtn) {
        // Hide all feedback icons first
        hideAllFeedbackIcons();
        
        // Determine if answer is correct
        boolean isCorrect = false;
        String correctAnswerText = "";
        
        if ("grammar".equals(quizType) && currentGrammarQuiz != null) {
            List<String> options = currentGrammarQuiz.getOptions();
            if (options != null && currentGrammarQuiz.getCorrectAnswer() < options.size()) {
                correctAnswerText = options.get(currentGrammarQuiz.getCorrectAnswer());
                isCorrect = selectedAnswer.equals(correctAnswerText);
            }
        } else if ("writing".equals(quizType) && currentWritingQuiz != null) {
            correctAnswerText = currentWritingQuiz.getAnswer().toLowerCase();
            isCorrect = selectedAnswer.toLowerCase().equals(correctAnswerText);
        } else if ("sentence_completion".equals(quizType) && currentSentenceCompletionQuiz != null) {
            List<String> options = currentSentenceCompletionQuiz.getOptions();
            if (options != null && currentSentenceCompletionQuiz.getCorrectAnswer() < options.size()) {
                correctAnswerText = options.get(currentSentenceCompletionQuiz.getCorrectAnswer());
                isCorrect = selectedAnswer.equals(correctAnswerText);
            }
        } else if ("word_order".equals(quizType) && currentWordOrderQuiz != null) {
            List<String> words = currentWordOrderQuiz.getWords();
            List<Integer> correctOrder = currentWordOrderQuiz.getCorrectOrder();
            StringBuilder correctSentence = new StringBuilder();
            for (int i = 0; i < correctOrder.size(); i++) {
                if (i > 0) correctSentence.append(" ");
                correctSentence.append(words.get(correctOrder.get(i)));
            }
            correctAnswerText = correctSentence.toString();
            isCorrect = selectedAnswer.equals(correctAnswerText);
        } else if ("synonym_antonym".equals(quizType) && currentSynonymAntonymQuiz != null) {
            List<String> options = currentSynonymAntonymQuiz.getOptions();
            if (options != null && currentSynonymAntonymQuiz.getCorrectAnswer() < options.size()) {
                correctAnswerText = options.get(currentSynonymAntonymQuiz.getCorrectAnswer());
                isCorrect = selectedAnswer.equals(correctAnswerText);
            }
        } else {
            // Vocabulary quiz
            if (correctVocabulary != null) {
                correctAnswerText = correctVocabulary.getEnglish().toUpperCase();
                isCorrect = selectedAnswer.equals(correctAnswerText);
            }
        }
        
        // Show feedback icon for selected button
        ImageView feedbackIcon = null;
        if (selectedBtn == btnAnswer1) {
            feedbackIcon = ivFeedback1;
        } else if (selectedBtn == btnAnswer2) {
            feedbackIcon = ivFeedback2;
        } else if (selectedBtn == btnAnswer3) {
            feedbackIcon = ivFeedback3;
        } else if (selectedBtn == btnAnswer4) {
            feedbackIcon = ivFeedback4;
        }
        
        if (feedbackIcon != null) {
            feedbackIcon.setVisibility(View.VISIBLE);
            if (isCorrect) {
                feedbackIcon.setImageResource(R.drawable.ic_check_large);
            } else {
                feedbackIcon.setImageResource(R.drawable.ic_close_large);
            }
        }
    }
    
    private void hideAllFeedbackIcons() {
        ivFeedback1.setVisibility(View.GONE);
        ivFeedback2.setVisibility(View.GONE);
        ivFeedback3.setVisibility(View.GONE);
        ivFeedback4.setVisibility(View.GONE);
    }

    private void resetAnswerButtons() {
        // Reset background và màu chữ
        int defaultTextColor = 0xFF424242; // Màu đen cho chữ trên nền xanh lá nhạt
        btnAnswer1.setBackgroundResource(R.drawable.bg_answer_green);
        btnAnswer1.setTextColor(defaultTextColor);
        btnAnswer2.setBackgroundResource(R.drawable.bg_answer_green);
        btnAnswer2.setTextColor(defaultTextColor);
        btnAnswer3.setBackgroundResource(R.drawable.bg_answer_green);
        btnAnswer3.setTextColor(defaultTextColor);
        btnAnswer4.setBackgroundResource(R.drawable.bg_answer_green);
        btnAnswer4.setTextColor(defaultTextColor);
        
        // Hide all feedback icons
        hideAllFeedbackIcons();
    }

    private void setupCheckButton() {
        btnCheck.setOnClickListener(v -> {
            if (!answerSelected) {
                Toast.makeText(this, "Vui lòng chọn một đáp án!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Stop timer when checking answer
            stopTimer();

            // Kiểm tra đáp án - xử lý cả vocabulary và grammar quiz
            boolean isCorrect;
            String correctAnswerText;
            
            if ("grammar".equals(quizType) && currentGrammarQuiz != null) {
                // Grammar quiz - check với correctAnswer index
                List<String> options = currentGrammarQuiz.getOptions();
                if (options != null && currentGrammarQuiz.getCorrectAnswer() < options.size()) {
                    correctAnswerText = options.get(currentGrammarQuiz.getCorrectAnswer());
                    isCorrect = selectedAnswer.equals(correctAnswerText);
                } else {
                    isCorrect = false;
                    correctAnswerText = "";
                }
                // Grammar quiz không cần log learning (không có building vocabulary)
            } else if ("writing".equals(quizType) && currentWritingQuiz != null) {
                // Writing quiz - check với answer
                correctAnswerText = currentWritingQuiz.getAnswer().toLowerCase();
                isCorrect = selectedAnswer.toLowerCase().equals(correctAnswerText);
            } else if ("sentence_completion".equals(quizType) && currentSentenceCompletionQuiz != null) {
                // Sentence completion quiz - check với correctAnswer index
                List<String> options = currentSentenceCompletionQuiz.getOptions();
                if (options != null && currentSentenceCompletionQuiz.getCorrectAnswer() < options.size()) {
                    correctAnswerText = options.get(currentSentenceCompletionQuiz.getCorrectAnswer());
                    isCorrect = selectedAnswer.equals(correctAnswerText);
                } else {
                    isCorrect = false;
                    correctAnswerText = "";
                }
            } else if ("word_order".equals(quizType) && currentWordOrderQuiz != null) {
                // Word order quiz - tạo câu đúng và check
                List<String> words = currentWordOrderQuiz.getWords();
                List<Integer> correctOrder = currentWordOrderQuiz.getCorrectOrder();
                StringBuilder correctSentence = new StringBuilder();
                for (int i = 0; i < correctOrder.size(); i++) {
                    if (i > 0) correctSentence.append(" ");
                    correctSentence.append(words.get(correctOrder.get(i)));
                }
                correctAnswerText = correctSentence.toString();
                isCorrect = selectedAnswer.equals(correctAnswerText);
            } else if ("synonym_antonym".equals(quizType) && currentSynonymAntonymQuiz != null) {
                // Synonym/Antonym quiz - check với correctAnswer index
                List<String> options = currentSynonymAntonymQuiz.getOptions();
                if (options != null && currentSynonymAntonymQuiz.getCorrectAnswer() < options.size()) {
                    correctAnswerText = options.get(currentSynonymAntonymQuiz.getCorrectAnswer());
                    isCorrect = selectedAnswer.equals(correctAnswerText);
                } else {
                    isCorrect = false;
                    correctAnswerText = "";
                }
            } else {
                // Vocabulary quiz - check với vocabulary English
                if (correctVocabulary != null) {
                    correctAnswerText = correctVocabulary.getEnglish().toUpperCase();
                    isCorrect = selectedAnswer.equals(correctAnswerText);
                    
                    // Ghi log học tập (chỉ cho vocabulary quiz)
                    learningLogRepo.logQuizAttempt(buildingId, isCorrect, correctVocabulary.getEnglish());
                } else {
                    // Fallback chỉ cho vocabulary quiz, không dùng cho quiz types khác
                    correctAnswerText = "HOUSE";
                    isCorrect = false; // Không đúng nếu không có correctVocabulary
                    Toast.makeText(this, "Lỗi: Không tìm thấy đáp án", Toast.LENGTH_SHORT).show();
                }
            }
            
            if (isCorrect) {
                // Highlight đáp án đúng
                highlightCorrectAnswer();
                // Disable buttons
                disableAllButtons();
                // Show Next button
                showNextButton();
                // Cập nhật building progress CHỈ cho vocabulary quiz (có buildingId)
                // Grammar quiz từ quest không cập nhật building progress
                if (!"grammar".equals(quizType) && buildingId != null) {
                    updateBuildingProgress();
                    // Hiển thị dialog đáp án đúng
                    showCorrectAnswerDialog();
                } else if ("grammar".equals(quizType)) {
                    // Grammar quiz: chỉ thưởng vàng/XP (bonus cho mission)
                    if (isMission) {
                        // Thưởng bonus cho grammar quiz từ quest
                        int expReward = 30;
                        int goldReward = 20;
                        goldRepo.addGold(goldReward, null);
                        // Cập nhật quest progress cho grammar quiz và show dialog với Next button
                        updateRelatedQuestsAndShowDialog(expReward, goldReward);
                    } else {
                        // Cập nhật quest progress ngay cả khi không phải mission
                        updateRelatedQuestsAndShowDialog(0, 0);
                    }
                } else if ("reading".equals(quizType) || "writing".equals(quizType) || 
                          "sentence_completion".equals(quizType) || "word_order".equals(quizType) || 
                          "synonym_antonym".equals(quizType)) {
                    // Các quiz types mới: thưởng và cập nhật quest progress
                    if (isMission) {
                        int expReward = 25;
                        int goldReward = 15;
                        goldRepo.addGold(goldReward, null);
                        // Cập nhật quest progress và show dialog với Next button
                        updateRelatedQuestsAndShowDialog(expReward, goldReward);
                    } else {
                        updateRelatedQuestsAndShowDialog(0, 0);
                    }
                } else {
                    showCorrectAnswerDialog();
                }
            } else {
                // Highlight đáp án đúng và sai
                highlightCorrectAnswer();
                // Disable buttons
                disableAllButtons();
                // Show Next button
                showNextButton();
                // Hiển thị dialog đáp án sai
                showWrongAnswerDialog();
            }
        });
    }

    /**
     * Load building level từ Firebase
     */
    private void loadBuildingLevel() {
        if (buildingId == null) return;
        
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;
        
        String userId = auth.getCurrentUser().getUid();
        String buildingPath = "users/" + userId + "/buildings/" + buildingId;
        
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .document(buildingPath)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long level = documentSnapshot.getLong("level");
                        if (level != null) {
                            buildingLevel = level.intValue();
                        }
                    }
                });
    }
    
    /**
     * Cập nhật building progress khi quiz đúng
     */
    private void updateBuildingProgress() {
        if (buildingProgressRepo != null && buildingId != null && harvestRepo != null) {
            // Tính phần thưởng dựa trên level building
            BuildingHarvestRepository.HarvestReward reward = harvestRepo.calculateHarvestReward(buildingLevel);
            
            // Nếu đây là quiz từ Mission, thêm bonus reward (x1.5 gold và exp)
            if (isMission) {
                reward = new BuildingHarvestRepository.HarvestReward(
                    (int) (reward.expReward * 1.5), // Bonus 50% exp
                    (int) (reward.goldReward * 1.5)  // Bonus 50% gold
                );
            }
            
            // Thưởng EXP
            buildingProgressRepo.addExpToBuilding(buildingId, reward.expReward, 
                new BuildingProgressRepository.OnProgressUpdatedListener() {
                    @Override
                    public void onProgressUpdated(long level, int currentExp, int maxExp) {
                        // Progress đã được cập nhật trong Firebase
                        // Cập nhật số từ vựng đã học cho building này
                        updateVocabularyLearnedForBuilding();
                        
                        // Đánh dấu đã thu hoạch
                        harvestRepo.markAsHarvested(buildingId, new BuildingHarvestRepository.OnHarvestMarkedListener() {
                            @Override
                            public void onHarvestMarked() {
                            }
                            
                            @Override
                            public void onError(String error) {
                                Log.e("VocabularyQuiz", "Error marking harvest: " + error);
                            }
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e("VocabularyQuiz", "Error updating building progress: " + error);
                    }
                });
            
            // Thưởng vàng
            rewardGold(reward.goldReward);
            
            // Tự động update quest progress khi quiz đúng
            updateRelatedQuests();
        }
    }
    
    /**
     * Cập nhật số từ vựng đã học cho building
     */
    private void updateVocabularyLearnedForBuilding() {
        if (buildingId != null && correctVocabulary != null) {
            UserStatsRepository statsRepo = UserStatsRepository.getInstance();
            statsRepo.calculateVocabularyLearnedByBuilding(buildingId, vocabularyLearned -> {
                // Vocabulary learned đã được cập nhật trong BuildingProgressRepository
            });
        }
    }
    
    /**
     * Thưởng vàng khi quiz đúng (vàng dùng để mở khóa building)
     */
    private void rewardGold(int goldAmount) {
        if (goldRepo != null) {
            goldRepo.addGold(goldAmount, new GoldRepository.OnGoldUpdatedListener() {
                @Override
                public void onGoldUpdated(int newGold) {
                    // Vàng đã được cập nhật
                }
                
                @Override
                public void onError(String error) {
                    Log.e("VocabularyQuiz", "Error rewarding gold: " + error);
                }
            });
        }
    }
    
    /**
     * Update quest progress và show dialog với Next button nếu quest chưa hoàn thành
     */
    private void updateRelatedQuestsAndShowDialog(int expReward, int goldReward) {
        if (questRepo == null) {
            showCorrectAnswerDialogForGrammar(expReward, goldReward);
            return;
        }
        
        // Load quests để tìm quest cần update
        questRepo.getAllQuests(new QuestRepository.OnQuestsLoadedListener() {
            @Override
            public void onQuestsLoaded(java.util.List<com.example.rise_of_city.data.model.Quest> quests) {
                com.example.rise_of_city.data.model.Quest questToUpdate = null;
                
                for (com.example.rise_of_city.data.model.Quest quest : quests) {
                    if (quest.isCompleted() || quest.isClaimed()) {
                        continue;
                    }
                    
                    String questType = quest.getQuestType();
                    boolean shouldUpdate = checkQuestTypeMatch(questType, quest);
                    
                    if (shouldUpdate) {
                        questToUpdate = quest;
                        currentQuestId = quest.getId();
                        currentQuestMaxProgress = quest.getMaxProgress();
                        currentQuestProgress = quest.getProgress();
                        break; // Chỉ update quest đầu tiên match
                    }
                }
                
                if (questToUpdate != null) {
                    // Update quest progress
                    questRepo.incrementQuestProgress(questToUpdate.getId(), new QuestRepository.OnQuestUpdatedListener() {
                        @Override
                        public void onQuestUpdated(String updatedQuestId, int progress, boolean completed) {
                            runOnUiThread(() -> {
                                // Update progress sau khi increment
                                currentQuestProgress = progress;
                                
                                // Show dialog với Next button nếu quest chưa hoàn thành
                                if (completed || progress >= currentQuestMaxProgress) {
                                    // Quest đã hoàn thành - hiển thị "TIẾP TỤC"
                                    AnswerCorrectDialogFragment dialog = AnswerCorrectDialogFragment.newInstance(expReward, goldReward);
                                    dialog.setOnContinueClickListener(() -> finish());
                                    dialog.show(getSupportFragmentManager(), "AnswerCorrectDialog");
                                } else {
                                    // Quest chưa hoàn thành - hiển thị "NEXT" để làm quiz tiếp theo
                                    AnswerCorrectDialogFragment dialog = AnswerCorrectDialogFragment.newInstance(expReward, goldReward, "NEXT");
                                    dialog.setOnContinueClickListener(() -> loadNextQuizForQuest());
                                    dialog.show(getSupportFragmentManager(), "AnswerCorrectDialog");
                                }
                            });
                        }
                        
                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                // Lỗi, show dialog bình thường
                                showCorrectAnswerDialogForGrammar(expReward, goldReward);
                            });
                        }
                    });
                } else {
                    // Không tìm thấy quest, show dialog bình thường
                    showCorrectAnswerDialogForGrammar(expReward, goldReward);
                }
            }
            
            @Override
            public void onError(String error) {
                // Lỗi, show dialog bình thường
                showCorrectAnswerDialogForGrammar(expReward, goldReward);
            }
        });
    }
    
    /**
     * Check xem quest type có match với quiz type hiện tại không
     */
    private boolean checkQuestTypeMatch(String questType, com.example.rise_of_city.data.model.Quest quest) {
        if (questType == null) return false;
        
        switch (questType) {
            case "complete_grammar_quiz":
                if ("grammar".equals(quizType)) {
                    String questLessonName = quest.getLessonName();
                    if (questLessonName != null && topicId != null) {
                        String questTopicId = questLessonName.toLowerCase()
                            .replace(" ", "_")
                            .replace("ì", "i").replace("à", "a").replace("á", "a")
                            .replace("ả", "a").replace("ã", "a").replace("ạ", "a")
                            .replace("đ", "d").replace("ê", "e").replace("ế", "e")
                            .replace("ề", "e").replace("ể", "e").replace("ễ", "e").replace("ệ", "e")
                            .replace("ô", "o").replace("ố", "o").replace("ồ", "o")
                            .replace("ổ", "o").replace("ỗ", "o").replace("ộ", "o")
                            .replace("ơ", "o").replace("ớ", "o").replace("ờ", "o")
                            .replace("ở", "o").replace("ỡ", "o").replace("ợ", "o")
                            .replace("ư", "u").replace("ứ", "u").replace("ừ", "u")
                            .replace("ử", "u").replace("ữ", "u").replace("ự", "u");
                        return topicId.equals(questTopicId);
                    }
                }
                return false;
            case "complete_reading":
                return "reading".equals(quizType);
            case "complete_writing_quiz":
                return "writing".equals(quizType);
            case "complete_sentence_completion_quiz":
                return "sentence_completion".equals(quizType);
            case "complete_word_order_quiz":
                return "word_order".equals(quizType);
            case "complete_synonym_antonym_quiz":
                return "synonym_antonym".equals(quizType);
            default:
                return false;
        }
    }
    
    /**
     * Tự động update quest progress khi user làm quiz đúng
     * Đây làm cho quest system hợp lý và thú vị hơn vì user thấy progress tăng tự động
     * Logic: Update các quest có liên quan đến việc làm quiz
     */
    private void updateRelatedQuests() {
        if (questRepo == null) {
            return;
        }
        
        // Update quest progress trong background (không block UI)
        new Thread(() -> {
            // Load tất cả quests để kiểm tra quest nào cần update
            questRepo.getAllQuests(new QuestRepository.OnQuestsLoadedListener() {
                @Override
                public void onQuestsLoaded(java.util.List<com.example.rise_of_city.data.model.Quest> quests) {
                    for (com.example.rise_of_city.data.model.Quest quest : quests) {
                        // Skip nếu quest đã completed hoặc đã claimed
                        if (quest.isCompleted() || quest.isClaimed()) {
                            continue;
                        }
                        
                        String questType = quest.getQuestType();
                        String targetBuildingId = quest.getTargetBuildingId();
                        String questId = quest.getId();
                        
                        if (questId == null) {
                            continue;
                        }
                        
                        // Update quest dựa trên quest type và quiz type hiện tại
                        boolean shouldUpdate = false;
                        
                        if (questType != null) {
                            switch (questType) {
                                case "complete_quiz":
                                case "complete_vocabulary_quiz":
                                case "answer_correct":
                                    // Quest về làm quiz nói chung - chỉ update cho vocabulary quiz
                                    if (quizType == null || "vocabulary".equals(quizType)) {
                                        shouldUpdate = true;
                                    }
                                    break;
                                    
                                case "complete_building_quiz":
                                    // Quest về làm quiz của building cụ thể - chỉ cho vocabulary quiz
                                    if ((quizType == null || "vocabulary".equals(quizType)) 
                                            && targetBuildingId != null && targetBuildingId.equals(buildingId)) {
                                        shouldUpdate = true;
                                    }
                                    break;
                                    
                                case "complete_grammar_quiz":
                                    // Quest về làm grammar quiz - kiểm tra lessonName match với topicId
                                    if ("grammar".equals(quizType)) {
                                        String questLessonName = quest.getLessonName();
                                        if (questLessonName != null && topicId != null) {
                                            // Convert lessonName to topicId format để so sánh
                                            String questTopicId = questLessonName.toLowerCase()
                                                .replace(" ", "_")
                                                .replace("ì", "i").replace("à", "a").replace("á", "a")
                                                .replace("ả", "a").replace("ã", "a").replace("ạ", "a")
                                                .replace("đ", "d").replace("ê", "e").replace("ế", "e")
                                                .replace("ề", "e").replace("ể", "e").replace("ễ", "e").replace("ệ", "e")
                                                .replace("ô", "o").replace("ố", "o").replace("ồ", "o")
                                                .replace("ổ", "o").replace("ỗ", "o").replace("ộ", "o")
                                                .replace("ơ", "o").replace("ớ", "o").replace("ờ", "o")
                                                .replace("ở", "o").replace("ỡ", "o").replace("ợ", "o")
                                                .replace("ư", "u").replace("ứ", "u").replace("ừ", "u")
                                                .replace("ử", "u").replace("ữ", "u").replace("ự", "u");
                                            if (topicId.equals(questTopicId)) {
                                                shouldUpdate = true;
                                            }
                                        }
                                    }
                                    break;
                                    
                                case "complete_reading":
                                    // Quest về làm reading quiz
                                    if ("reading".equals(quizType)) {
                                        shouldUpdate = true;
                                    }
                                    break;
                                    
                                case "complete_writing_quiz":
                                    // Quest về làm writing quiz
                                    if ("writing".equals(quizType)) {
                                        shouldUpdate = true;
                                    }
                                    break;
                                    
                                case "complete_sentence_completion_quiz":
                                    // Quest về làm sentence completion quiz
                                    if ("sentence_completion".equals(quizType)) {
                                        shouldUpdate = true;
                                    }
                                    break;
                                    
                                case "complete_word_order_quiz":
                                    // Quest về làm word order quiz
                                    if ("word_order".equals(quizType)) {
                                        shouldUpdate = true;
                                    }
                                    break;
                                    
                                case "complete_synonym_antonym_quiz":
                                    // Quest về làm synonym/antonym quiz
                                    if ("synonym_antonym".equals(quizType)) {
                                        shouldUpdate = true;
                                    }
                                    break;
                                    
                                case "complete_all_quests":
                                    // Quest này sẽ được update khi claim quest khác, không update ở đây
                                    break;
                                    
                                case "catch_ink":
                                case "reach_score":
                                case "shoot_bullet":
                                case "give_gold":
                                    // Các quest này không liên quan đến quiz, skip
                                    break;
                                    
                                default:
                                    // Default: Nếu quest có targetBuildingId và match với building hiện tại, update (chỉ cho vocabulary)
                                    if ((quizType == null || "vocabulary".equals(quizType))
                                            && targetBuildingId != null && targetBuildingId.equals(buildingId)) {
                                        shouldUpdate = true;
                                    }
                                    break;
                            }
                        }
                        
                        // Update quest progress nếu cần
                        if (shouldUpdate) {
                            questRepo.incrementQuestProgress(questId, new QuestRepository.OnQuestUpdatedListener() {
                                @Override
                                public void onQuestUpdated(String updatedQuestId, int progress, boolean completed) {
                                    Log.d("VocabularyQuiz", "Quest progress updated: " + updatedQuestId + " -> " + progress);
                                    if (completed) {
                                        Log.d("VocabularyQuiz", "Quest completed: " + updatedQuestId);
                                        // Có thể thêm notification ở đây nếu muốn
                                    }
                                }
                                
                                @Override
                                public void onError(String error) {
                                    Log.e("VocabularyQuiz", "Error updating quest: " + error);
                                }
                            });
                        }
                    }
                }
                
                @Override
                public void onError(String error) {
                    Log.e("VocabularyQuiz", "Error loading quests for update: " + error);
                }
            });
        }).start();
    }

    /**
     * Show correct answer dialog cho grammar quiz và các quiz types khác (fallback khi không phải mission)
     */
    private void showCorrectAnswerDialogForGrammar(int expReward, int goldReward) {
        // Chỉ được gọi khi không phải mission hoặc không có quest
        AnswerCorrectDialogFragment dialog = AnswerCorrectDialogFragment.newInstance(expReward, goldReward);
        dialog.setOnContinueClickListener(() -> finish());
        dialog.show(getSupportFragmentManager(), "AnswerCorrectDialog");
    }
    
    /**
     * Load quiz tiếp theo cho quest (giữ nguyên quizType)
     */
    private void loadNextQuizForQuest() {
        // Reset quiz state
        answerSelected = false;
        selectedAnswer = null;
        currentGrammarQuiz = null;
        currentWritingQuiz = null;
        currentSentenceCompletionQuiz = null;
        currentWordOrderQuiz = null;
        currentSynonymAntonymQuiz = null;
        
        // Reset buttons
        resetAnswerButtons();
        enableAllButtons();
        
        // Load quiz tiếp theo dựa trên quizType
        if ("grammar".equals(quizType) && topicId != null) {
            loadGrammarQuizFromFirebase();
        } else if ("writing".equals(quizType)) {
            loadWritingQuizFromFirebase();
        } else if ("sentence_completion".equals(quizType)) {
            loadSentenceCompletionQuizFromFirebase();
        } else if ("word_order".equals(quizType)) {
            loadWordOrderQuizFromFirebase();
        } else if ("synonym_antonym".equals(quizType)) {
            loadSynonymAntonymQuizFromFirebase();
        } else {
            // Default: vocabulary quiz
            loadQuizFromFirebase();
        }
    }
    
    private void highlightCorrectAnswer() {
        // Tìm đáp án đúng - xử lý tất cả quiz types
        String correctAnswerText = "";
        
        if ("grammar".equals(quizType) && currentGrammarQuiz != null) {
            // Grammar quiz
            List<String> options = currentGrammarQuiz.getOptions();
            if (options != null && currentGrammarQuiz.getCorrectAnswer() < options.size()) {
                correctAnswerText = options.get(currentGrammarQuiz.getCorrectAnswer());
            }
        } else if ("writing".equals(quizType) && currentWritingQuiz != null) {
            // Writing quiz
            correctAnswerText = currentWritingQuiz.getAnswer().toLowerCase();
        } else if ("sentence_completion".equals(quizType) && currentSentenceCompletionQuiz != null) {
            // Sentence completion quiz
            List<String> options = currentSentenceCompletionQuiz.getOptions();
            if (options != null && currentSentenceCompletionQuiz.getCorrectAnswer() < options.size()) {
                correctAnswerText = options.get(currentSentenceCompletionQuiz.getCorrectAnswer());
            }
        } else if ("word_order".equals(quizType) && currentWordOrderQuiz != null) {
            // Word order quiz
            List<String> words = currentWordOrderQuiz.getWords();
            List<Integer> correctOrder = currentWordOrderQuiz.getCorrectOrder();
            StringBuilder correctSentence = new StringBuilder();
            for (int i = 0; i < correctOrder.size(); i++) {
                if (i > 0) correctSentence.append(" ");
                correctSentence.append(words.get(correctOrder.get(i)));
            }
            correctAnswerText = correctSentence.toString();
        } else if ("synonym_antonym".equals(quizType) && currentSynonymAntonymQuiz != null) {
            // Synonym/Antonym quiz
            List<String> options = currentSynonymAntonymQuiz.getOptions();
            if (options != null && currentSynonymAntonymQuiz.getCorrectAnswer() < options.size()) {
                correctAnswerText = options.get(currentSynonymAntonymQuiz.getCorrectAnswer());
            }
        } else {
            // Vocabulary quiz (default) - chỉ dùng fallback nếu thực sự là vocabulary quiz
            if (correctVocabulary != null) {
                correctAnswerText = correctVocabulary.getEnglish().toUpperCase();
            } else {
                // Không có correctVocabulary, không highlight (đã là lỗi)
                correctAnswerText = "";
            }
        }
        
        // Highlight đáp án đúng với màu xanh và chữ xanh đậm
        int greenColor = 0xFF2E7D32; // Màu xanh đậm
        // So sánh không phân biệt hoa thường cho writing quiz
        String answer1Text = btnAnswer1.getText().toString();
        String answer2Text = btnAnswer2.getText().toString();
        String answer3Text = btnAnswer3.getText().toString();
        String answer4Text = btnAnswer4.getText().toString();
        
        if (answer1Text.equals(correctAnswerText) || 
            ("writing".equals(quizType) && answer1Text.toLowerCase().equals(correctAnswerText.toLowerCase()))) {
            btnAnswer1.setBackgroundResource(R.drawable.bg_answer_button_correct);
            btnAnswer1.setTextColor(greenColor);
        } else if (answer2Text.equals(correctAnswerText) || 
                   ("writing".equals(quizType) && answer2Text.toLowerCase().equals(correctAnswerText.toLowerCase()))) {
            btnAnswer2.setBackgroundResource(R.drawable.bg_answer_button_correct);
            btnAnswer2.setTextColor(greenColor);
        } else if (answer3Text.equals(correctAnswerText) || 
                   ("writing".equals(quizType) && answer3Text.toLowerCase().equals(correctAnswerText.toLowerCase()))) {
            btnAnswer3.setBackgroundResource(R.drawable.bg_answer_button_correct);
            btnAnswer3.setTextColor(greenColor);
        } else if (answer4Text.equals(correctAnswerText) || 
                   ("writing".equals(quizType) && answer4Text.toLowerCase().equals(correctAnswerText.toLowerCase()))) {
            btnAnswer4.setBackgroundResource(R.drawable.bg_answer_button_correct);
            btnAnswer4.setTextColor(greenColor);
        }

        // Highlight đáp án sai với màu đỏ và chữ đỏ đậm
        if (!selectedAnswer.equals(correctAnswerText)) {
            int redColor = 0xFFC62828; // Màu đỏ đậm
            if (btnAnswer1.getText().toString().equals(selectedAnswer)) {
                btnAnswer1.setBackgroundResource(R.drawable.bg_answer_button_wrong);
                btnAnswer1.setTextColor(redColor);
            } else if (btnAnswer2.getText().toString().equals(selectedAnswer)) {
                btnAnswer2.setBackgroundResource(R.drawable.bg_answer_button_wrong);
                btnAnswer2.setTextColor(redColor);
            } else if (btnAnswer3.getText().toString().equals(selectedAnswer)) {
                btnAnswer3.setBackgroundResource(R.drawable.bg_answer_button_wrong);
                btnAnswer3.setTextColor(redColor);
            } else if (btnAnswer4.getText().toString().equals(selectedAnswer)) {
                btnAnswer4.setBackgroundResource(R.drawable.bg_answer_button_wrong);
                btnAnswer4.setTextColor(redColor);
            }
        } else {
            // Nếu chọn đúng, chỉ highlight đáp án đúng với màu xanh
            // (đã xử lý ở trên)
        }
    }

    private void disableAllButtons() {
        btnAnswer1.setEnabled(false);
        btnAnswer2.setEnabled(false);
        btnAnswer3.setEnabled(false);
        btnAnswer4.setEnabled(false);
        btnCheck.setEnabled(false);
    }
    
    private void enableAllButtons() {
        btnAnswer1.setEnabled(true);
        btnAnswer2.setEnabled(true);
        btnAnswer3.setEnabled(true);
        btnAnswer4.setEnabled(true);
        btnCheck.setEnabled(true);
    }
    
    private void showNextButton() {
        // Hide check button and show next button
        btnCheck.setVisibility(View.GONE);
        btnNext.setVisibility(View.VISIBLE);
    }
    
    private void hideNextButton() {
        // Show check button and hide next button
        btnCheck.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.GONE);
    }

    private void showCorrectAnswerDialog() {
        // Tính phần thưởng dựa trên level building
        BuildingHarvestRepository.HarvestReward reward = harvestRepo != null ? 
            harvestRepo.calculateHarvestReward(buildingLevel) : 
            new BuildingHarvestRepository.HarvestReward(20, 10);
        
        // Nếu đây là quiz từ Mission, thêm bonus reward
        if (isMission && reward != null) {
            reward = new BuildingHarvestRepository.HarvestReward(
                (int) (reward.expReward * 1.5), // Bonus 50% exp
                (int) (reward.goldReward * 1.5)  // Bonus 50% gold
            );
        }
        
        // Tạo message với thông báo bonus nếu là Mission
        String bonusMessage = isMission ? " (Mission Bonus +50%!)" : "";
        AnswerCorrectDialogFragment dialog = AnswerCorrectDialogFragment.newInstance(reward.expReward, reward.goldReward);
        dialog.setOnContinueClickListener(() -> {
            // Đóng activity và quay lại màn hình trước
            // Building progress đã được cập nhật trong updateBuildingProgress()
            // Vàng đã được thưởng trong rewardGold()
            finish();
        });
        dialog.show(getSupportFragmentManager(), "AnswerCorrectDialog");
    }

    private void showWrongAnswerDialog() {
        // Lấy nghĩa tiếng Việt từ vocabulary
        String englishWord = correctVocabulary != null ? 
            correctVocabulary.getEnglish() : "HOUSE";
        String vietnameseAnswer = correctVocabulary != null ? 
            correctVocabulary.getVietnamese() : getVietnameseTranslation(englishWord);
        
        AnswerWrongDialogFragment dialog = AnswerWrongDialogFragment.newInstance(
            englishWord, vietnameseAnswer);
        dialog.setOnUnderstoodClickListener(() -> {
            // Đóng activity và quay lại màn hình trước
            finish();
        });
        dialog.show(getSupportFragmentManager(), "AnswerWrongDialog");
    }

    private String getVietnameseTranslation(String englishWord) {
        // Fallback: Map English words to Vietnamese translations
        if (correctVocabulary != null) {
            return correctVocabulary.getVietnamese();
        }
        
        switch (englishWord.toUpperCase()) {
            case "HOUSE":
                return "Ngôi nhà";
            case "MOUSE":
                return "Chuột";
            case "HORSE":
                return "Ngựa";
            case "HOSE":
                return "Ống nước";
            default:
                return "";
        }
    }
}
