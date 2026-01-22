package com.example.madgroupproject.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.madgroupproject.data.local.entity.GameLevelHistoryEntity;

import java.util.List;

@Dao
public interface GameLevelHistoryDao {
    @Query("""
        SELECT * FROM game_level_history
        WHERE gameType = :type
        ORDER BY levelNum DESC
    """)
    List<GameLevelHistoryEntity> getHistoryByGameTypeDesc(String type);

    @Query("""
        SELECT * FROM game_level_history
        WHERE levelNum = :level
    """)
    List<GameLevelHistoryEntity> getHistoryByLevel(int level);

    @Query("""
    SELECT * FROM game_level_history
    WHERE levelNum = :level
    LIMIT 1
""")
    GameLevelHistoryEntity getHistoryForLevel(int level);

    @Query("""
    SELECT * FROM game_level_history
    WHERE levelNum = :level
    LIMIT 1
""")
    LiveData<GameLevelHistoryEntity> observeHistoryForLevel(int level);

    @Insert
    void insert(GameLevelHistoryEntity entity);

    // this is a dummy / quick way to update the database
    // to manually update database for developer testing
    @Query("INSERT INTO game_level_history (levelNum, gameType, completedDate) VALUES (1, 'STEPS', '2025-12-1')")
    void dummyInsert();
}
