package com.example.madgroupproject.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.dao.FitnessDataDao;
import com.example.madgroupproject.data.local.entity.FitnessDataEntity;
import com.example.madgroupproject.fitnessmanager.RecordingAPIManager;

import java.time.LocalDate;

public class FitnessRepository {
    private final RecordingAPIManager recordingAPIManager;
    private final FitnessDataDao fitnessDataDao;

    public FitnessRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        this.fitnessDataDao = db.fitnessDataDao();
        this.recordingAPIManager = new RecordingAPIManager(context);
    }


//    // used to sync to the database entity
    public int syncTodayFitnessData() {

        RecordingAPIManager.DataRecordingAPI data =
                recordingAPIManager.readDailyTotals();

        String today = LocalDate.now().toString(); // yyyy-MM-dd

        FitnessDataEntity entity = new FitnessDataEntity(
                today,
                data.steps,
                data.distance,
                data.calories,
                System.currentTimeMillis()
        );
        Log.i("SYNC DATA", "syncTodayFitnessData");
        Log.i("SYNC DATA", "\tdate: "+entity.date);
        Log.i("SYNC DATA", "\tsteps: "+entity.steps);
        Log.i("SYNC DATA", "\tdistanceMeters: "+entity.distanceMeters);
        Log.i("SYNC DATA", "\tcalories: "+entity.calories);
        Log.i("SYNC DATA", "\tlastUpdated: "+entity.lastUpdated);

        fitnessDataDao.insertOrUpdate(entity);
        return data.steps;
    }

    // date in yyyy-MM-dd, String
    // fetch from DB, not recording API
    public FitnessDataEntity fetchDailyData(String date) {
        return fitnessDataDao.getByDate(date);
    }
}
