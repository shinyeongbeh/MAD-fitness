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

    private final MutableLiveData<String> selectedDate = new MutableLiveData<>();
    private final MutableLiveData<String> selectedMonth = new MutableLiveData<>();
    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        this.repository = new StatisticsRepository(application);
        // When date changes → new daily query
        dailyStats = Transformations.switchMap(
                selectedDate,
                repository::getDailyStats
        );

        // When month changes → new monthly totals
        monthlyTotalStats = Transformations.switchMap(
                selectedMonth,
                repository::getMonthlyTotals
        );

        // When month changes → new monthly averages
        monthlyAverageStats = Transformations.switchMap(
                selectedMonth,
                repository::getMonthlyAverage
        );
    }

    // setters
    // set the current day or month that needs to be displayed
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
}
