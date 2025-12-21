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

    //TODO: delete later
    // this is a dummy / stupid / quick way to update the database
    // 这个只是给自己用来manually update database, 这样子才能看到结果吗
    // 就是加新的row 不过是我们直接手动加进database， 可以点左边有一个table和放大镜的图标，然后在database inspector看到结果
    // 这个不应该被用在其他任何地方
    @Query("INSERT INTO streak_history (date, steps, achieved, minStepsRequired, lastUpdated) VALUES ('2025-12-18', 123, 1, 100, 0)")
    void dummyInsert();
}
