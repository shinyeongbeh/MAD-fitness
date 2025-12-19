package com.example.madgroupproject.fitnessmanager;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.entity.FitnessDataEntity;
import com.google.android.gms.fitness.Fitness;

import java.time.LocalDate;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            FitnessManager fitnessManager = new FitnessManager(getApplicationContext());
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());

            // 1. Read API (Sync call is safe here because doWork runs on background thread)
            FitnessManager.DailyData data = fitnessManager.readDailyTotals();

            // 2. Save to DB
            String today = LocalDate.now().toString();
            FitnessDataEntity stat = new FitnessDataEntity(today, data.steps, data.distance, data.calories);

            db.fitnessDataDao().insertOrUpdate(stat);

            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}

