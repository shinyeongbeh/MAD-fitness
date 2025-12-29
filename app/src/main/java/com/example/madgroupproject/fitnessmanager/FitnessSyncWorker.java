package com.example.madgroupproject.fitnessmanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.madgroupproject.data.repository.FitnessRepository;
import com.example.madgroupproject.data.repository.StreakRepository;
import com.example.madgroupproject.gamemanager.GameProgressSyncWorker;

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

            // call another sync worker here (game progress)
            // this is due to in Main Activity, it is a periodic work, so it cannot have two different works, will have error
            WorkManager.getInstance(getApplicationContext())
                    .enqueue(new OneTimeWorkRequest.Builder(GameProgressSyncWorker.class)
                            .setConstraints(
                                    new Constraints.Builder()
                                            .setRequiresBatteryNotLow(true)
                                            .build()
                            )
                            .build());

            Log.d("FITNESS SYNC", "fitness data, streak, and game data successfully synced");
            return Result.success();
        } catch (Exception e) {
            Log.e("FITNESS SYNC", e.getMessage());
            return Result.retry();
        }
    }
}

