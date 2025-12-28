package com.example.madgroupproject.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.madgroupproject.data.local.entity.GameProgressEntity;

@Dao
public interface GameProgressDao {
    //    @Query("""
//        SELECT * FROM game_progress
//        WHERE gameType = :type
//        LIMIT 1
//    """)
    @Query("""
        SELECT * FROM game_progress
    """)
    GameProgressEntity getProgressSync();

    //    @Query("""
//        SELECT * FROM game_progress
//        WHERE gameType = :type
//        LIMIT 1
//    """)
    @Query("SELECT * FROM game_progress")
    LiveData<GameProgressEntity> observeProgress();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void updateOrInsertProgress(GameProgressEntity entity);

    //TODO: delete later
    // this is a dummy / stupid / quick way to update the database
    // 这个只是给自己用来manually update database, 这样子才能看到结果吗
    // 就是加新的row 不过是我们直接手动加进database， 可以点左边有一个table和放大镜的图标，然后在database inspector看到结果
    // 这个不应该被用在其他任何地方
    @Query("INSERT INTO game_progress (currentLevel, progressValue, lastSyncedFitnessValue, lastSyncedDate) VALUES (1, 100, 123.5, '2025-12-27')")
    void dummyInsert();
}