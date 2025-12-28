package com.example.madgroupproject.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "game_level_history")
public class GameLevelHistoryEntity {
    @PrimaryKey
    @NonNull
    public int levelNum;

    @NonNull
    public String gameType;

    @NonNull
    public String completedDate; // yyyy-MM-dd

}
