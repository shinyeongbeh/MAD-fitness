package com.example.madgroupproject.data.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.madgroupproject.data.local.entity.StreakHistoryEntity;
import com.example.madgroupproject.data.repository.StreakRepository;

import java.time.YearMonth;
import java.util.List;

public class StreakViewModel extends AndroidViewModel {
    private final StreakRepository repository;
    private final LiveData<StreakRepository.StreakResult> currentStreakLiveData;
    private final LiveData<StreakHistoryEntity> todayStepsLiveData;
    private final LiveData<StreakRepository.LongestStreakResult> longestStreakLiveData;

    // ✅ 新增：保存当前查看的月份
    private final MutableLiveData<YearMonth> currentViewingMonth = new MutableLiveData<>();

    public StreakViewModel(@NonNull Application application) {
        super(application);
        this.repository = new StreakRepository(application);
        this.currentStreakLiveData = this.repository.getCurrentStreakLive();
        this.todayStepsLiveData = this.repository.getLiveStepsFromStreakEntity();
        this.longestStreakLiveData = this.repository.getLongestStreakWithDetailsLive();

        // ✅ 初始化为当前月份
        this.currentViewingMonth.setValue(YearMonth.now());
    }

    public LiveData<StreakRepository.StreakResult> getCurrentStreakLiveData() {
        return currentStreakLiveData;
    }

    public LiveData<StreakHistoryEntity> getTodayStepsLiveData() {
        return todayStepsLiveData;
    }

    public LiveData<StreakHistoryEntity> getStreakByDate(String date) {
        return repository.getStreakByDateLive(date);
    }

    public void updateMinSteps(int newGoal, StreakRepository.OnUpdateCompleteListener listener) {
        repository.updateMinSteps(newGoal, listener);
    }

    public LiveData<List<StreakHistoryEntity>> getMonthStreakLive(String yearMonth) {
        return repository.getMonthStreakLive(yearMonth);
    }

    public LiveData<StreakRepository.LongestStreakResult> getLongestStreakWithDetailsLiveData() {
        return longestStreakLiveData;
    }

    public LiveData<Integer> getBestStreakLiveData() {
        return repository.getLongestStreakLiveData();
    }

    public void autoInitTodayRecord() {
        repository.autoInitTodayRecord();
    }

    // ✅ 新增：获取和设置当前查看的月份
    public LiveData<YearMonth> getCurrentViewingMonth() {
        return currentViewingMonth;
    }

    public void setCurrentViewingMonth(YearMonth yearMonth) {
        currentViewingMonth.setValue(yearMonth);
    }

    public YearMonth getCurrentViewingMonthValue() {
        YearMonth value = currentViewingMonth.getValue();
        return value != null ? value : YearMonth.now();
    }
}


