package com.example.madgroupproject.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// store the current progress
// only one row for the current level
@Entity(tableName = "game_progress")
public class GameProgressEntity {
    @PrimaryKey
    public int id = 1; // auto-generated key
    // only allow one row

    public int currentLevel;

    public float progressValue; // steps or meters

    public String lastSyncedDate; // yyyy-MM-dd
}
