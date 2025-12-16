package com.example.rise_of_city.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.rise_of_city.data.model.Lesson;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository để quản lý lessons (bài học do người dùng chia sẻ)
 */
public class LessonRepository {
    private static final String TAG = "LessonRepository";
    private static LessonRepository instance;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private LessonRepository() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static LessonRepository getInstance() {
        if (instance == null) {
            instance = new LessonRepository();
        }
        return instance;
    }

    /**
     * Lấy danh sách lessons đã được approve của một topic
     */
    public void getLessonsByTopic(String topicId, OnLessonsLoadedListener listener) {
        Log.d(TAG, "Loading lessons for topic: " + topicId);
        
        // Thử query với orderBy trước (cần index)
        firestore.collection("topics")
                .document(topicId)
                .collection("lessons")
                .whereEqualTo("status", "approved") // Chỉ lấy lessons đã được approve
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Lesson> lessons = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            // Parse manually from Map to avoid Timestamp deserialization issues
                            java.util.Map<String, Object> data = document.getData();
                            if (data != null) {
                                Lesson lesson = new Lesson();
                                lesson.setId(document.getId());
                                lesson.setTopicId((String) data.get("topicId"));
                                lesson.setTitle((String) data.get("title"));
                                lesson.setContent((String) data.get("content"));
                                lesson.setAuthorId((String) data.get("authorId"));
                                lesson.setAuthorName((String) data.get("authorName"));
                                lesson.setAuthorAvatarUrl((String) data.get("authorAvatarUrl"));
                                lesson.setStatus((String) data.get("status"));
                                lesson.setLevel((String) data.get("level"));
                                
                                // Convert Timestamp to long for createdAt
                                Object createdAtObj = data.get("createdAt");
                                if (createdAtObj instanceof Timestamp) {
                                    lesson.setCreatedAt(((Timestamp) createdAtObj).toDate().getTime());
                                } else if (createdAtObj instanceof Number) {
                                    lesson.setCreatedAt(((Number) createdAtObj).longValue());
                                } else {
                                    lesson.setCreatedAt(0);
                                }
                                
                                // Convert Timestamp to long for updatedAt
                                Object updatedAtObj = data.get("updatedAt");
                                if (updatedAtObj instanceof Timestamp) {
                                    lesson.setUpdatedAt(((Timestamp) updatedAtObj).toDate().getTime());
                                } else if (updatedAtObj instanceof Number) {
                                    lesson.setUpdatedAt(((Number) updatedAtObj).longValue());
                                } else {
                                    lesson.setUpdatedAt(0);
                                }
                                
                                Object viewCountObj = data.get("viewCount");
                                lesson.setViewCount(viewCountObj != null ? ((Number) viewCountObj).intValue() : 0);
                                
                                Object likeCountObj = data.get("likeCount");
                                lesson.setLikeCount(likeCountObj != null ? ((Number) likeCountObj).intValue() : 0);
                                
                                Object commentCountObj = data.get("commentCount");
                                lesson.setCommentCount(commentCountObj != null ? ((Number) commentCountObj).intValue() : 0);
                                
                                Object tagsObj = data.get("tags");
                                if (tagsObj instanceof List) {
                                    @SuppressWarnings("unchecked")
                                    List<String> tags = (List<String>) tagsObj;
                                    lesson.setTags(tags);
                                }
                                
                                lessons.add(lesson);
                            }
                        } catch (Exception parseException) {
                            Log.e(TAG, "Error parsing lesson document " + document.getId() + ": " + parseException.getMessage());
                            parseException.printStackTrace();
                        }
                    }
                    Log.d(TAG, "Loaded " + lessons.size() + " approved lessons for topic " + topicId);
                    if (listener != null) {
                        listener.onLessonsLoaded(lessons);
                    }
                })
                .addOnFailureListener(error -> {
                    Log.w(TAG, "Error loading lessons with orderBy for topic " + topicId + ": " + error.getMessage());
                    Log.w(TAG, "Falling back to query without orderBy...");
                    
                    // Fallback: Query không có orderBy (không cần index)
                    firestore.collection("topics")
                            .document(topicId)
                            .collection("lessons")
                            .whereEqualTo("status", "approved")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                List<Lesson> lessons = new ArrayList<>();
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    try {
                                        // Parse manually from Map to avoid Timestamp deserialization issues
                                        java.util.Map<String, Object> data = document.getData();
                                        if (data != null) {
                                            Lesson lesson = new Lesson();
                                            lesson.setId(document.getId());
                                            lesson.setTopicId((String) data.get("topicId"));
                                            lesson.setTitle((String) data.get("title"));
                                            lesson.setContent((String) data.get("content"));
                                            lesson.setAuthorId((String) data.get("authorId"));
                                            lesson.setAuthorName((String) data.get("authorName"));
                                            lesson.setAuthorAvatarUrl((String) data.get("authorAvatarUrl"));
                                            lesson.setStatus((String) data.get("status"));
                                            lesson.setLevel((String) data.get("level"));
                                            
                                            // Convert Timestamp to long for createdAt
                                            Object createdAtObj = data.get("createdAt");
                                            if (createdAtObj instanceof Timestamp) {
                                                lesson.setCreatedAt(((Timestamp) createdAtObj).toDate().getTime());
                                            } else if (createdAtObj instanceof Number) {
                                                lesson.setCreatedAt(((Number) createdAtObj).longValue());
                                            } else {
                                                lesson.setCreatedAt(0);
                                            }
                                            
                                            // Convert Timestamp to long for updatedAt
                                            Object updatedAtObj = data.get("updatedAt");
                                            if (updatedAtObj instanceof Timestamp) {
                                                lesson.setUpdatedAt(((Timestamp) updatedAtObj).toDate().getTime());
                                            } else if (updatedAtObj instanceof Number) {
                                                lesson.setUpdatedAt(((Number) updatedAtObj).longValue());
                                            } else {
                                                lesson.setUpdatedAt(0);
                                            }
                                            
                                            Object viewCountObj = data.get("viewCount");
                                            lesson.setViewCount(viewCountObj != null ? ((Number) viewCountObj).intValue() : 0);
                                            
                                            Object likeCountObj = data.get("likeCount");
                                            lesson.setLikeCount(likeCountObj != null ? ((Number) likeCountObj).intValue() : 0);
                                            
                                            Object commentCountObj = data.get("commentCount");
                                            lesson.setCommentCount(commentCountObj != null ? ((Number) commentCountObj).intValue() : 0);
                                            
                                            Object tagsObj = data.get("tags");
                                            if (tagsObj instanceof List) {
                                                @SuppressWarnings("unchecked")
                                                List<String> tags = (List<String>) tagsObj;
                                                lesson.setTags(tags);
                                            }
                                            
                                            lessons.add(lesson);
                                        }
                                    } catch (Exception parseException) {
                                        Log.e(TAG, "Error parsing lesson document " + document.getId() + ": " + parseException.getMessage());
                                        parseException.printStackTrace();
                                    }
                                }
                                
                                // Sort manually by createdAt (descending)
                                lessons.sort((l1, l2) -> {
                                    long time1 = l1.getCreatedAt();
                                    long time2 = l2.getCreatedAt();
                                    return Long.compare(time2, time1); // Descending
                                });
                                
                                Log.d(TAG, "Loaded " + lessons.size() + " approved lessons (fallback) for topic " + topicId);
                                if (listener != null) {
                                    listener.onLessonsLoaded(lessons);
                                }
                            })
                            .addOnFailureListener(fallbackError -> {
                                Log.e(TAG, "Error loading lessons (fallback) for topic " + topicId + ": " + fallbackError.getMessage());
                                if (listener != null) {
                                    listener.onError(fallbackError.getMessage());
                                }
                            });
                });
    }

    /**
     * Tạo lesson mới (do người dùng chia sẻ)
     */
    public void createLesson(String topicId, String title, String content, String level, java.util.List<String> tags, OnLessonCreatedListener listener) {
        if (auth.getCurrentUser() == null) {
            if (listener != null) {
                listener.onError("Người dùng chưa đăng nhập");
            }
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String userName = auth.getCurrentUser().getDisplayName();
        if (userName == null || userName.isEmpty()) {
            userName = auth.getCurrentUser().getEmail();
        }
        
        // Lưu giá trị final để dùng trong lambda
        final String finalUserName = userName;

        // Lấy thông tin user từ Firestore
        firestore.collection("user_profiles")
                .document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String authorName = userDoc.getString("name");
                    if (authorName == null || authorName.isEmpty()) {
                        authorName = finalUserName;
                    }
                    String authorAvatarUrl = userDoc.getString("avatarUrl");

                    // Tạo lesson
                    Map<String, Object> lessonData = new HashMap<>();
                    lessonData.put("topicId", topicId);
                    lessonData.put("title", title);
                    lessonData.put("content", content);
                    lessonData.put("authorId", userId);
                    lessonData.put("authorName", authorName);
                    lessonData.put("authorAvatarUrl", authorAvatarUrl);
                    lessonData.put("createdAt", System.currentTimeMillis());
                    lessonData.put("updatedAt", System.currentTimeMillis());
                    lessonData.put("status", "pending"); // Chờ admin approve
                    lessonData.put("viewCount", 0);
                    lessonData.put("likeCount", 0);
                    lessonData.put("commentCount", 0);
                    lessonData.put("level", level != null ? level : "Beginner");
                    lessonData.put("tags", tags != null ? tags : new ArrayList<>());

                    // Lưu vào Firestore
                    firestore.collection("topics")
                            .document(topicId)
                            .collection("lessons")
                            .add(lessonData)
                            .addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "Lesson created with ID: " + documentReference.getId());
                                if (listener != null) {
                                    listener.onLessonCreated(documentReference.getId());
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error creating lesson: " + e.getMessage());
                                if (listener != null) {
                                    listener.onError(e.getMessage());
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user profile: " + e.getMessage());
                    // Vẫn tạo lesson với thông tin từ Auth
                    String authorName = finalUserName;
                    Map<String, Object> lessonData = new HashMap<>();
                    lessonData.put("topicId", topicId);
                    lessonData.put("title", title);
                    lessonData.put("content", content);
                    lessonData.put("authorId", userId);
                    lessonData.put("authorName", authorName);
                    lessonData.put("createdAt", System.currentTimeMillis());
                    lessonData.put("updatedAt", System.currentTimeMillis());
                    lessonData.put("status", "pending");
                    lessonData.put("viewCount", 0);
                    lessonData.put("likeCount", 0);
                    lessonData.put("commentCount", 0);
                    lessonData.put("level", level != null ? level : "Beginner");
                    lessonData.put("tags", tags != null ? tags : new ArrayList<>());

                    firestore.collection("topics")
                            .document(topicId)
                            .collection("lessons")
                            .add(lessonData)
                            .addOnSuccessListener(documentReference -> {
                                if (listener != null) {
                                    listener.onLessonCreated(documentReference.getId());
                                }
                            })
                            .addOnFailureListener(error -> {
                                if (listener != null) {
                                    listener.onError(error.getMessage());
                                }
                            });
                });
    }

    /**
     * Tăng view count khi người dùng xem lesson
     */
    public void incrementViewCount(String topicId, String lessonId) {
        firestore.collection("topics")
                .document(topicId)
                .collection("lessons")
                .document(lessonId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long currentViews = documentSnapshot.getLong("viewCount");
                        int newViews = (currentViews != null ? currentViews.intValue() : 0) + 1;
                        documentSnapshot.getReference().update("viewCount", newViews);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error incrementing view count: " + e.getMessage());
                });
    }

    // Interfaces
    public interface OnLessonsLoadedListener {
        void onLessonsLoaded(List<Lesson> lessons);
        void onError(String error);
    }

    public interface OnLessonCreatedListener {
        void onLessonCreated(String lessonId);
        void onError(String error);
    }
}

