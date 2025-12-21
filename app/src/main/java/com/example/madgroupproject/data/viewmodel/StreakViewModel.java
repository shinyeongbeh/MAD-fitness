package com.example.madgroupproject.data.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.madgroupproject.data.local.entity.StreakHistoryEntity;
import com.example.madgroupproject.data.repository.StreakRepository;

// use View Model to interacts with UI
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

    // Used to get Current Streak: ? Days
    public LiveData<StreakRepository.StreakResult> getStreakLiveData() {
        return streakResultLiveData;
    }

    // Used to get Steps: ?(steps)/?(minStepsRequired)
    public LiveData<StreakHistoryEntity> getLiveStepsFromStreakEntity() {
        return stepsNow;
    }
}
