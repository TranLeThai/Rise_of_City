package com.example.rise_of_city.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDao {
    @Insert
    long registerUser(User user);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(int userId);
    
    @Query("SELECT * FROM users")
    java.util.List<User> getAllUsers();

    @Update
    void updateUser(User user);
    @Query("UPDATE users SET surveyCompleted = :status WHERE id = :userId")
    void updateSurveyStatus(int userId, boolean status);
}