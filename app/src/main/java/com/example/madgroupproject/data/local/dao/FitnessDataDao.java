package com.example.madgroupproject.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.madgroupproject.data.local.entity.FitnessDataEntity;

@Dao
public interface FitnessDataDao {
    // Return daily statistics
    @Query("SELECT * FROM fitness_data WHERE date = :date")
    LiveData<FitnessDataEntity> getStatsLive(String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(FitnessDataEntity data);
}
