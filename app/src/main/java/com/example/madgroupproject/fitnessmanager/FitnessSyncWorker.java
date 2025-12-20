package com.example.madgroupproject.fitnessmanager;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.madgroupproject.data.repository.FitnessRepository;

public class FitnessSyncWorker extends Worker {

    public FitnessSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    //will need to be defined when to sync in MainActivity
    @NonNull
    @Override
    public Result doWork() {
        try {
            FitnessRepository repository = new FitnessRepository(getApplicationContext());
            repository.syncTodayFitnessData();
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}

