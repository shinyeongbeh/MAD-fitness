package com.example.madgroupproject.fitnessmanager;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.madgroupproject.data.repository.FitnessRepository;
import com.example.madgroupproject.data.repository.StreakRepository;

public class FitnessSyncWorker extends Worker {

    public FitnessSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    //will need to be defined when to sync in MainActivity
    @NonNull
    @Override
    public Result doWork() {
        try {
            FitnessRepository fitnessRepository = new FitnessRepository(getApplicationContext());
            int steps = fitnessRepository.syncTodayFitnessData();

            StreakRepository streakRepository = new StreakRepository(getApplicationContext());
            streakRepository.insertOrUpdateSteps(steps);
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}

