package com.example.madgroupproject.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.madgroupproject.data.local.entity.StreakHistoryEntity;

import java.util.List;

@Dao
public interface StreakHistoryDao {
    // Fetch LiveData filtered by a given date
    @Query("SELECT * FROM streak_history WHERE date = :date")
    LiveData<StreakHistoryEntity> observeByDate(String date);

    // Fetch data filtered by a given date
    @Query("SELECT * FROM streak_history WHERE date = :date")
    StreakHistoryEntity getByDate(String date);


    // Fetch all records that has reached the target
    // 有达到目标的日子的记录们
    @Query("SELECT * FROM streak_history WHERE achieved = 1 ORDER BY date DESC")
    LiveData<List<StreakHistoryEntity>> getAchievedDaysDescLive();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(StreakHistoryEntity entity);

    @Query("SELECT * FROM streak_history WHERE date LIKE :month || '%' ORDER BY date ASC")
    List<StreakHistoryEntity> getMonthData(String month);

    @Query("SELECT * FROM streak_history ORDER BY date ASC")
    List<StreakHistoryEntity> getAll();

    @Query("UPDATE streak_history SET minStepsRequired = :steps WHERE date = :date")
    void updateMinStepsRequired(String date, int steps);

    // used for Calendar view
    @Query("SELECT * FROM streak_history WHERE date LIKE :month || '%' ORDER BY date ASC")
    LiveData<List<StreakHistoryEntity>> observeMonthData(String month);

    // this is a dummy / quick way to update the database
    // to manually update database for developer testing
    @Query("INSERT INTO streak_history (date, steps, achieved, minStepsRequired, lastUpdated) VALUES ('2025-12-18', 123, 1, 100, 0)")
    void dummyInsert();
}
