package com.example.madgroupproject.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.dao.StreakHistoryDao;
import com.example.madgroupproject.data.local.entity.StreakHistoryEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;

public class StreakRepository {
    private final StreakHistoryDao streakHistoryDao;

    public StreakRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        streakHistoryDao = db.streakHistoryDao();
    }
    // result format with list of the streak histories and streak count [etc. 8 days]
    public class StreakResult {
        public List<StreakHistoryEntity> streakDays;
        public int streakCount;

        public StreakResult(List<StreakHistoryEntity> streakDays, int streakCount) {
            this.streakDays = streakDays;
            this.streakCount = streakCount;
        }
    }

    // fetch the steps from streak entity [Steps: 120/200]
    // does not observe from Recording API, so UI takes 30 minutes to change, bcs it is sync with FitnessSyncWorker
    public LiveData<StreakHistoryEntity> getLiveStepsFromStreakEntity() {
        return streakHistoryDao.observeByDate(LocalDate.now().toString());
    }

    //Using LiveData will allow automatically notify the fragment when DB on change
    // fetch the current streak day [Current Streak: 4 Days]
    public LiveData<StreakResult> getCurrentStreakLive() {
        MediatorLiveData<StreakResult> resultLiveData = new MediatorLiveData<>();

        LiveData<List<StreakHistoryEntity>> source = streakHistoryDao.getAchievedDaysDescLive();

        resultLiveData.addSource(source, historyList -> {
            // This runs on MAIN THREAD, but streak logic may be heavy → offload
            Executors.newSingleThreadExecutor().execute(() -> {
                StreakResult result = calculateCurrentStreak(historyList); // ✅ Pass actual list
                // Post result back to main thread
                resultLiveData.postValue(result);
            });
        });

        return resultLiveData;
    }

    // helper method called in getCurrentStreakLive
    // used to calculate streak from today
    public StreakResult calculateCurrentStreak(List<StreakHistoryEntity> achievedDays) {
        String today = LocalDate.now().toString();
        String yesterday = LocalDate.now().minusDays(1).toString();

        if(achievedDays.isEmpty()) {
            return new StreakResult(null, 0);
        }
        // if achieved days has not contain today
        if(!achievedDays.get(0).date.equals(today)) {
            // check if achieved days contain yesterday or not
            if(!achievedDays.get(0).date.equals(yesterday)) {
                // today dont have, yesterday also no achieve -> streak = 0
                return new StreakResult(null, 0);
            }
        }

        // now only left cases:
        // today achieve, yesterday achieve / no achieve
        // today no achieve, yesterday achieve
        int streak = 0;
        int endCounter = 0;
        for(int i=0; i<achievedDays.size(); i++) {
            StreakHistoryEntity currentRecord = achievedDays.get(i);
            if(i>0) {
                // check if the day is 连续跟上个记录
                LocalDate currentRecordDate = LocalDate.parse(currentRecord.date);
                LocalDate lastRecordDate = LocalDate.parse(achievedDays.get(i-1).date);
//                Log.i("CALCSTREAK","Date: "+currentRecord.date);
                if(!currentRecordDate.equals(lastRecordDate.minusDays(1))) {
                    break;
                }
            }
            streak++;
            endCounter=i;
            Log.i("CALCSTREAK","Date: "+currentRecord.date);
            Log.i("CALCSTREAK","Current streak: "+streak);
        }
        List<StreakHistoryEntity> result = achievedDays.subList(0, endCounter+1);
        return new StreakResult(result, streak);
    }

    //TODO: Jiayi
    //create methods for features
    //example method below, can change the data type of the argument or the return variable, I simply create one only hahaha
    public List<StreakHistoryEntity> getLongestStreak(List<StreakHistoryEntity> achievedDays) {
        return null;
    }

    // used to update the steps when syncing from Recording API to database happens
    // used in FitnessSyncWorker
    public void insertOrUpdateSteps(int steps) {
        StreakHistoryEntity streakData = new StreakHistoryEntity(
                LocalDate.now().toString(),
                steps,
                checkAchieved(steps),
                getMinSteps(),
                System.currentTimeMillis()
        );
        Executors.newSingleThreadExecutor().execute(()->streakHistoryDao.insertOrUpdate(streakData));
    }

    private boolean checkAchieved(int steps) {
        StreakHistoryEntity dataToday = streakHistoryDao.getByDate(LocalDate.now().toString());
        // case where today's record hasnt been recorded
        if(dataToday==null) {
            return false;
        }
        if(dataToday.achieved) {
            return true;
        }
        // in the case where today has record
        // check whether the new steps can achieve streak
        if(steps>=dataToday.minStepsRequired) {
            return true;
        } else {
            return false;
        }
    }

    private int getMinSteps() {
        StreakHistoryEntity dataToday = streakHistoryDao.getByDate(LocalDate.now().toString());
        // case where today's record hasnt been recorded
        if(dataToday==null) {
            //check for yesterday's min steps
            StreakHistoryEntity dataYesterday = streakHistoryDao.getByDate(LocalDate.now().minusDays(1).toString());
            return dataYesterday.minStepsRequired;
        }

        //case where today has record
        return dataToday.minStepsRequired;
    }
}