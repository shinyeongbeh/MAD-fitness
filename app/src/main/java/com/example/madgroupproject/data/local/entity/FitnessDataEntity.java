package com.example.madgroupproject.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "fitness_data")
public class FitnessDataEntity {
    @PrimaryKey
    @NonNull
    public String date; // Format: "yyyy-MM-dd"

    public int steps;
    public float distanceMeters;
    public float calories;
    public long lastUpdated;

    public FitnessDataEntity(@NonNull String date, int steps, float distanceMeters, float calories) {
        this.date = date;
        this.steps = steps;
        this.distanceMeters = distanceMeters;
        this.calories = calories;
        this.lastUpdated = System.currentTimeMillis();
    }
}
