package com.example.madgroupproject.data.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.madgroupproject.data.local.entity.StreakHistoryEntity;
import com.example.madgroupproject.data.repository.StreakRepository;

import java.util.List;

public class StreakViewModel extends AndroidViewModel {
    private final StreakRepository repository;
    private final LiveData<StreakRepository.StreakResult> streakResultLiveData;
    private final LiveData<StreakHistoryEntity> stepsNow;

    public StreakViewModel(@NonNull Application application) {
        super(application);
        this.repository = new StreakRepository(application);
        this.streakResultLiveData = this.repository.getCurrentStreakLive();
        this.stepsNow = this.repository.getLiveStepsFromStreakEntity();
    }

    // Get current streak
    public LiveData<StreakRepository.StreakResult> getStreakLiveData() {
        return streakResultLiveData;
    }

    // Get today's steps
    public LiveData<StreakHistoryEntity> getLiveStepsFromStreakEntity() {
        return stepsNow;
    }

    // Get streak by date
    public LiveData<StreakHistoryEntity> getStreakByDate(String date) {
        return repository.getStreakByDateLive(date);
    }

    // Update minimum steps for a date
    public void updateMinSteps(String date, int steps) {
        repository.updateMinSteps(date, steps);
    }

    // Get month streak for calendar
    public LiveData<List<StreakHistoryEntity>> getMonthStreakLive(String yearMonth) {
        return repository.getMonthStreakLive(yearMonth);
    }

    // **FIXED: Get best streak directly from repository**
    // This calculates the longest streak from ALL achieved days in the database
    public LiveData<Integer> getBestStreakLiveData() {
        return repository.getLongestStreakLiveData();
    }

    // Get all streak data (for debugging or export)
    public List<StreakHistoryEntity> getAllStreakData() {
        return repository.getAll();
    }

    public void autoInitTodayRecord() {
        repository.autoInitTodayRecord();
    }

}


