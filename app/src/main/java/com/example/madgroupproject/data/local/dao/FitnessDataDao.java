package com.example.madgroupproject.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.madgroupproject.data.local.entity.FitnessDataEntity;

import java.time.LocalDate;

@Dao
public interface FitnessDataDao {
    // Return daily statistics
    @Query("SELECT * FROM fitness_data WHERE date = :date")
    FitnessDataEntity getByDate(String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(FitnessDataEntity data);

    //TODO: delete later
    @Query("INSERT INTO fitness_data (date, steps, distanceMeters, calories, lastUpdated) VALUES ('2025-12-18', 123, 0, 0, 0)")
    void dummyInsert();
}
