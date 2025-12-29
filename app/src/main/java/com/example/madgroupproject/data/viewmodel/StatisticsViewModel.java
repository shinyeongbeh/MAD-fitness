package com.example.madgroupproject.data.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.madgroupproject.data.local.entity.FitnessDataEntity;
import com.example.madgroupproject.data.repository.StatisticsRepository;

import java.util.List;

public class StatisticsViewModel extends AndroidViewModel {
    private final StatisticsRepository repository;
    private final LiveData<List<FitnessDataEntity>> dailyStats;
    private final LiveData<List<StatisticsRepository.MonthlyTotalStats>> monthlyTotalStats;
    private final LiveData<List<StatisticsRepository.MonthlyAverageStats>> monthlyAverageStats;
    private final LiveData<List<FitnessDataEntity>> monthlyDailyStats; // NEW for BarChart

    private final MutableLiveData<String> selectedDate = new MutableLiveData<>();
    private final MutableLiveData<String> selectedMonth = new MutableLiveData<>();

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        this.repository = new StatisticsRepository(application);

        // Daily stats
        dailyStats = Transformations.switchMap(
                selectedDate,
                repository::getDailyStats
        );

        // Monthly totals
        monthlyTotalStats = Transformations.switchMap(
                selectedMonth,
                repository::getMonthlyTotals
        );

        // Monthly averages
        monthlyAverageStats = Transformations.switchMap(
                selectedMonth,
                repository::getMonthlyAverage
        );

        // Monthly daily stats (for bar chart)
        monthlyDailyStats = Transformations.switchMap(
                selectedMonth,
                repository::getMonthlyDailyStats
        );
    }

    // setters
    public void setSelectedDate(String date) { // yyyy-MM-dd
        selectedDate.setValue(date);
    }

    public void setSelectedMonth(String month) { // yyyy-MM
        selectedMonth.setValue(month);
    }

    // getters
    public LiveData<List<FitnessDataEntity>> getDailyStats() {
        return dailyStats;
    }

    public LiveData<List<StatisticsRepository.MonthlyTotalStats>> getMonthlyTotalStats() {
        return monthlyTotalStats;
    }

    public LiveData<List<StatisticsRepository.MonthlyAverageStats>> getMonthlyAverageStats() {
        return monthlyAverageStats;
    }

    public LiveData<List<FitnessDataEntity>> getMonthlyDailyStats() { // NEW
        return monthlyDailyStats;
    }
}
