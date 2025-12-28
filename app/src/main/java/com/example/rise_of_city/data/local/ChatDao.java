package com.example.rise_of_city.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatDao {
    @Insert
    void insertMessage(ChatMessageEntity message);

    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp ASC")
    List<ChatMessageEntity> getMessagesByUserId(int userId);

    @Query("DELETE FROM chat_messages WHERE userId = :userId")
    void deleteMessagesByUserId(int userId);
}