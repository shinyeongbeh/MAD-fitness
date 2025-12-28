package com.example.madgroupproject.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "game_level")
public class GameLevelEntity {
    @NonNull
    @PrimaryKey
    public int levelNum;

    @NonNull
    public String gameType; //"STEPS" or "DISTANCE"

    public int targetValue;
}
