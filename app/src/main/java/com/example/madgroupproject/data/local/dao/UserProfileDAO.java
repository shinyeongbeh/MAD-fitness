package com.example.madgroupproject.data.local.dao;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.madgroupproject.data.local.entity.UserProfile;

@Dao
public interface UserProfileDAO {
    @Insert
    void insert(UserProfile profile);

    @Update
    void update(UserProfile profile);

    @Query("SELECT * FROM user_profile LIMIT 1")
    UserProfile getProfile();
}



