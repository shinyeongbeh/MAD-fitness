package com.example.madgroupproject.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.madgroupproject.data.local.entity.GameLevelEntity;

import java.util.List;

@Dao
public interface GameLevelDao {

    @Query("SELECT * FROM game_level ORDER BY levelNum ASC")
    LiveData<List<GameLevelEntity>> getAllLevels();

    @Query("""
        SELECT * FROM game_level
        WHERE levelNum = :level
    """)
    LiveData<GameLevelEntity> getLevel(int level);

    @Query("SELECT * FROM game_level WHERE levelNum = :level")
    GameLevelEntity getLevelSync(int level);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GameLevelEntity> levels);


    //TODO: delete later
    // this is a dummy / stupid / quick way to update the database
    // 这个只是给自己用来manually update database, 这样子才能看到结果吗
    // 就是加新的row 不过是我们直接手动加进database， 可以点左边有一个table和放大镜的图标，然后在database inspector看到结果
    // 这个不应该被用在其他任何地方
    @Query("INSERT INTO game_level (levelNum, gameType, targetValue) VALUES (3, 'STEPS', 100)")
    void dummyInsert();

    @Query("DELETE FROM game_level WHERE levelNum=2")
    void dummyDelete();
}
