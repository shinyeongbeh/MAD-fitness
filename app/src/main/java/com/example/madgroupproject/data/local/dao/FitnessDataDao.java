package com.example.madgroupproject.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.madgroupproject.data.local.entity.FitnessDataEntity;
import com.example.madgroupproject.data.repository.StatisticsRepository;

import java.util.List;

@Dao
public interface FitnessDataDao {
    // Return daily statistics
    @Query("SELECT * FROM fitness_data WHERE date = :date")
    FitnessDataEntity getByDate(String date);

    // Live data, UI changes when DB change
    @Query("SELECT * FROM fitness_data WHERE date = :date")
    LiveData<List<FitnessDataEntity>> getByDateLive(String date);

    @Query("SELECT SUM(steps) FROM fitness_data")
    int getTotalSteps();

    @Query("SELECT SUM(distanceMeters) FROM fitness_data")
    float getTotalDistance();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(FitnessDataEntity data);


    @Query("""
    SELECT
        SUM(steps) AS steps,
        SUM(distanceMeters) AS distanceMeters,
        SUM(calories) AS calories
    FROM fitness_data
    WHERE date LIKE :month || '%'
    """)
    LiveData<List<StatisticsRepository.MonthlyTotalStats>> getMonthlyTotalsLive(String month); // month = "2025-01"

    @Query("""
    SELECT
        AVG(steps) AS avgSteps,
        AVG(distanceMeters) AS avgDistance,
        AVG(calories) AS avgCalories
    FROM fitness_data
    WHERE date LIKE :month || '%'
    """)
    LiveData<List<StatisticsRepository.MonthlyAverageStats>> getMonthlyAverageLive(String month);

    //added for yearly update
    @Query("""
    SELECT * FROM fitness_data
    WHERE date LIKE :year || '%'
""")
    LiveData<List<FitnessDataEntity>> getStatsByYear(String year);






    @Query("SELECT * FROM fitness_data WHERE strftime('%Y-%m', date) = :month ORDER BY date ASC")
    LiveData<List<FitnessDataEntity>> getDailyStatsByMonth(String month);

    @Query("SELECT * FROM fitness_data WHERE date = :date")
    List<FitnessDataEntity> getStatsForDate(String date);

    // Return all rows for a given month: monthStr = "2026-01"
    @Query("SELECT * FROM fitness_data WHERE date LIKE :monthStr ORDER BY date ASC")
    LiveData<List<FitnessDataEntity>> getByMonth(String monthStr);

    // Return all rows for a given year: yearStr = "2026"
    @Query("SELECT * FROM fitness_data WHERE date LIKE :yearStr ORDER BY date ASC")
    LiveData<List<FitnessDataEntity>> getByYear(String yearStr);

    // DAO
    @Query("SELECT * FROM fitness_data WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    LiveData<List<FitnessDataEntity>> getStatsForWeek(String startDate, String endDate);

    // this is a dummy / quick way to update the database
    // to manually update database for developer testing
    @Query("INSERT INTO fitness_data (date, steps, distanceMeters, calories, lastUpdated) VALUES ('2025-12-18', 123, 0, 0, 0)")
    void dummyInsert();

}

