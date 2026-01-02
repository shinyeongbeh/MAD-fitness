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
}
