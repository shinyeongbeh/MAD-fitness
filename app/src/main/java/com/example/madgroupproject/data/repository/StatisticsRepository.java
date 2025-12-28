package com.example.madgroupproject.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.dao.FitnessDataDao;
import com.example.madgroupproject.data.local.entity.FitnessDataEntity;

import java.util.List;

public class StatisticsRepository {
    public static class MonthlyTotalStats {
        public int steps;
        public float distanceMeters;
        public float calories;
    }
    public static class MonthlyAverageStats {
        public float avgSteps;
        public float avgDistance;
        public float avgCalories;
    }


    private final FitnessDataDao fitnessDataDao;

    public StatisticsRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        this.fitnessDataDao = db.fitnessDataDao();
    }

    //methods to get stats
    // use LiveData so that DB changes, UI changes
    public LiveData<List<FitnessDataEntity>> getDailyStats(String date) {
        return this.fitnessDataDao.getByDateLive(date);
    }

    public LiveData<List<MonthlyTotalStats>> getMonthlyTotals(String month) {
        return this.fitnessDataDao.getMonthlyTotalsLive(month);
    }

    public LiveData<List<StatisticsRepository.MonthlyAverageStats>> getMonthlyAverage(String month) {
        return this.fitnessDataDao.getMonthlyAverageLive(month);
    }

    public LiveData<List<FitnessDataEntity>> getMonthlyDailyStats(String month) {
        return fitnessDataDao.getDailyStatsByMonth(month); // Implement DAO query below
    }



}

