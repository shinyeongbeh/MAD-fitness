package com.example.madgroupproject.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.madgroupproject.data.local.entity.GameProgressEntity;

@Dao
public interface GameProgressDao {
@Query("""
        SELECT * FROM game_progress LIMIT 1
    """)
    GameProgressEntity getProgressSync();

    @Query("SELECT * FROM game_progress LIMIT 1")
    LiveData<GameProgressEntity> observeProgress();

    @Query("SELECT currentLevel FROM game_progress LIMIT 1")
    LiveData<Integer> getCurrentLevel();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void updateOrInsertProgress(GameProgressEntity entity);

    // this is a dummy / quick way to update the database
    // to manually update database for developer testing
    @Query("INSERT INTO game_progress (currentLevel, progressValue, lastSyncedDate) VALUES (1, 100, '2025-12-27')")
    void dummyInsert();
}
