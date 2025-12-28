package com.example.madgroupproject.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// store the current progress
// only one row for the current level
@Entity(tableName = "game_progress")
public class GameProgressEntity {
    @PrimaryKey(autoGenerate = true)
    public int id; // auto-generated key

//    @NonNull
//    public String gameType; // STEPS / DISTANCE

    public int currentLevel;

    public float progressValue; // steps or meters

//    public boolean completed;
//
//    @Nullable
//    public String completionDate; // yyyy-MM-dd

    public float lastSyncedFitnessValue;
    // track the last synced raw fitness value for accumulative calculation

    public String lastSyncedDate; // yyyy-MM-dd

}
