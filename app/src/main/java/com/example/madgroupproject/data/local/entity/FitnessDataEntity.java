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
    public long lastUpdated; // System.currentTimeMillis() - returns a long that represents number of milliseconds that have elapsed since 1-1-1970dat

    public FitnessDataEntity(@NonNull String date, int steps, float distanceMeters, float calories, long lastUpdated) {
        this.date = date;
        this.steps = steps;
        this.distanceMeters = distanceMeters;
        this.calories = calories;
        this.lastUpdated = lastUpdated;
    }
}
