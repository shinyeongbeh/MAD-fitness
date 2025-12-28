package com.example.madgroupproject.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity (tableName = "streak_history", indices = {@Index(value = "date")})

public class StreakHistoryEntity {
    @PrimaryKey
    @NonNull
    public String date; // Format: "yyyy-MM-dd"
    public int steps;
    public boolean achieved;
    public int minStepsRequired;
    public long lastUpdated;

    public StreakHistoryEntity(@NonNull String date, int steps, boolean achieved, int minStepsRequired, long lastUpdated) {
        this.date = date;
        this.steps = steps;
        this.achieved = achieved;
        this.minStepsRequired = minStepsRequired;
        this.lastUpdated = lastUpdated;
    }
}