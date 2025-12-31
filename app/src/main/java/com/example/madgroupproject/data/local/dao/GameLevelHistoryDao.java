package com.example.madgroupproject.data.local.dao;

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


    @Insert
    void insert(GameLevelHistoryEntity entity);

    //TODO: delete later
    // this is a dummy / stupid / quick way to update the database
    // 这个只是给自己用来manually update database, 这样子才能看到结果吗
    // 就是加新的row 不过是我们直接手动加进database， 可以点左边有一个table和放大镜的图标，然后在database inspector看到结果
    // 这个不应该被用在其他任何地方
    @Query("INSERT INTO game_level_history (levelNum, gameType, completedDate) VALUES (1, 'STEPS', '2025-12-1')")
    void dummyInsert();

    @Query("INSERT INTO game_level_history (levelNum, gameType, completedDate) VALUES (2, 'distance', '2025-12-2')")
    void dummyInsert1();
}
