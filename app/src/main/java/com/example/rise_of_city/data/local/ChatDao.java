package com.example.rise_of_city.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatDao {
    @Insert
    void insertMessage(ChatMessageEntity message);

    // Lấy tin nhắn giữa 2 users (conversation)
    @Query("SELECT * FROM chat_messages WHERE " +
           "((senderId = :userId1 AND receiverId = :userId2) OR " +
           "(senderId = :userId2 AND receiverId = :userId1)) " +
           "ORDER BY timestamp ASC")
    List<ChatMessageEntity> getConversation(int userId1, int userId2);
    
    // Lấy tin nhắn gần đây giữa 2 users
    @Query("SELECT * FROM chat_messages WHERE " +
           "((senderId = :userId1 AND receiverId = :userId2) OR " +
           "(senderId = :userId2 AND receiverId = :userId1)) " +
           "ORDER BY timestamp DESC LIMIT :limit")
    List<ChatMessageEntity> getRecentConversation(int userId1, int userId2, int limit);
    
    // Lấy tất cả conversations của một user (danh sách người đã chat)
    @Query("SELECT DISTINCT CASE " +
           "WHEN senderId = :userId THEN receiverId " +
           "ELSE senderId END as otherUserId " +
           "FROM chat_messages WHERE senderId = :userId OR receiverId = :userId")
    List<Integer> getConversationUserIds(int userId);

    // Xóa tin nhắn giữa 2 users
    @Query("DELETE FROM chat_messages WHERE " +
           "((senderId = :userId1 AND receiverId = :userId2) OR " +
           "(senderId = :userId2 AND receiverId = :userId1))")
    void deleteConversation(int userId1, int userId2);
    
    // Deprecated methods - giữ lại để backward compatibility
    @Deprecated
    @Query("SELECT * FROM chat_messages WHERE senderId = :userId ORDER BY timestamp ASC")
    List<ChatMessageEntity> getMessagesByUserId(int userId);
    
    @Deprecated
    @Query("SELECT * FROM chat_messages WHERE senderId = :userId ORDER BY timestamp DESC LIMIT :limit")
    List<ChatMessageEntity> getRecentMessagesByUserId(int userId, int limit);

    @Deprecated
    @Query("DELETE FROM chat_messages WHERE senderId = :userId")
    void deleteMessagesByUserId(int userId);
}