package com.example.rise_of_city.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FriendDao {
    
    @Insert
    long insertFriend(Friend friend);
    
    @Update
    void updateFriend(Friend friend);
    
    // Lấy danh sách bạn bè đã chấp nhận
    @Query("SELECT * FROM friends WHERE (userId = :userId OR friendId = :userId) AND status = 'accepted'")
    List<Friend> getAcceptedFriends(int userId);
    
    // Lấy danh sách lời mời đang chờ (người khác gửi cho mình)
    @Query("SELECT * FROM friends WHERE friendId = :userId AND status = 'pending'")
    List<Friend> getPendingRequests(int userId);
    
    // Lấy danh sách lời mời đã gửi (mình gửi cho người khác)
    @Query("SELECT * FROM friends WHERE userId = :userId AND status = 'pending'")
    List<Friend> getSentRequests(int userId);
    
    // Kiểm tra đã là bạn bè chưa
    @Query("SELECT * FROM friends WHERE ((userId = :userId AND friendId = :friendId) OR (userId = :friendId AND friendId = :userId)) AND status = 'accepted' LIMIT 1")
    Friend checkFriendship(int userId, int friendId);
    
    // Kiểm tra đã gửi lời mời chưa
    @Query("SELECT * FROM friends WHERE ((userId = :userId AND friendId = :friendId) OR (userId = :friendId AND friendId = :userId)) AND status = 'pending' LIMIT 1")
    Friend checkPendingRequest(int userId, int friendId);
    
    // Xóa lời mời kết bạn
    @Query("DELETE FROM friends WHERE id = :friendId")
    void deleteFriend(int friendId);
    
    // Đếm số lời mời đang chờ
    @Query("SELECT COUNT(*) FROM friends WHERE friendId = :userId AND status = 'pending'")
    int countPendingRequests(int userId);
}

