package com.example.madgroupproject.data.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.dao.FitnessDataDao;
import com.example.madgroupproject.data.local.entity.FitnessDataEntity;
import com.example.madgroupproject.data.repository.StatisticsRepository;

import java.util.ArrayList;
import java.util.List;

public class StatisticsViewModel extends AndroidViewModel {

    private final FitnessDataDao fitnessDataDao;

    private final MutableLiveData<String> selectedDate = new MutableLiveData<>();
    private final MutableLiveData<String> selectedMonth = new MutableLiveData<>();

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        fitnessDataDao = AppDatabase.getDatabase(application).fitnessDataDao();
    }

    public void setSelectedDate(String date) {
        selectedDate.setValue(date);
    }

    public void setSelectedMonth(String month) {
        selectedMonth.setValue(month);
    }

    public LiveData<List<FitnessDataEntity>> getWeeklyStats(String startDate, String endDate) {
        return fitnessDataDao.getStatsForWeek(startDate, endDate);
    }

    // Daily Stats
    public LiveData<List<FitnessDataEntity>> getDailyStats() {
        return Transformations.switchMap(selectedDate, date -> {
            if (date == null) {
                MutableLiveData<List<FitnessDataEntity>> empty = new MutableLiveData<>();
                empty.setValue(new ArrayList<>()); // empty list
                return empty;
            } else {
                return fitnessDataDao.getByDateLive(date); // Room LiveData auto-updates
            }
        });
    }

    // Monthly totals
    public LiveData<List<StatisticsRepository.MonthlyTotalStats>> getMonthlyTotals(String month) {
        return fitnessDataDao.getMonthlyTotalsLive(month);
    }

    // Monthly averages
    public LiveData<List<StatisticsRepository.MonthlyAverageStats>> getMonthlyAverages(String month) {
        return fitnessDataDao.getMonthlyAverageLive(month);
    }

    // Get all daily stats for a month (used for charts)
    public LiveData<List<FitnessDataEntity>> getDailyStatsByMonth(String month) {
        return fitnessDataDao.getDailyStatsByMonth(month);
    }

    // Yearly steps (used for monthly bar chart)
    public LiveData<List<FitnessDataEntity>> getYearlySteps(String year) {
        // We'll use strftime to filter year in DAO, if needed
        return fitnessDataDao.getStatsByYear(year); // treat "YYYY" as prefix
    }



}
