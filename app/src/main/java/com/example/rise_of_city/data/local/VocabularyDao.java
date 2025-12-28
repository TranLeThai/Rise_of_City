package com.example.rise_of_city.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface VocabularyDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVocabulary(Vocabulary vocabulary);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Vocabulary> vocabularies);
    
    @Query("SELECT * FROM vocabularies WHERE topicId = :topicId ORDER BY english ASC")
    List<Vocabulary> getVocabulariesByTopic(String topicId);
    
    @Query("SELECT * FROM vocabularies WHERE topicId = :topicId AND imageName IS NOT NULL AND imageName != ''")
    List<Vocabulary> getVocabulariesWithImages(String topicId);
    
    @Query("SELECT DISTINCT topicId FROM vocabularies")
    List<String> getAllTopics();
    
    @Query("SELECT COUNT(*) FROM vocabularies WHERE topicId = :topicId")
    int countVocabulariesByTopic(String topicId);
    
    @Query("DELETE FROM vocabularies WHERE topicId = :topicId")
    void deleteVocabulariesByTopic(String topicId);
    
    @Query("DELETE FROM vocabularies")
    void deleteAll();
}

