package com.example.madgroupproject.gamemanager;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.dao.FitnessDataDao;
import com.example.madgroupproject.data.local.dao.GameLevelDao;
import com.example.madgroupproject.data.local.dao.GameLevelHistoryDao;
import com.example.madgroupproject.data.local.dao.GameProgressDao;
import com.example.madgroupproject.data.local.entity.FitnessDataEntity;
import com.example.madgroupproject.data.local.entity.GameLevelEntity;
import com.example.madgroupproject.data.local.entity.GameLevelHistoryEntity;
import com.example.madgroupproject.data.local.entity.GameProgressEntity;

import java.time.LocalDate;

public class GameProgressSyncWorker extends Worker {
    private final AppDatabase db;
    FitnessDataDao fitnessDao;
    GameProgressDao progressDao;
    GameLevelDao levelDao;
    GameLevelHistoryDao gameLevelHistoryDao;
    public GameProgressSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        db = AppDatabase.getDatabase(context);
        fitnessDao = db.fitnessDataDao();
        progressDao = db.gameProgressDao();
        levelDao = db.gameLevelDao();
        gameLevelHistoryDao = db.gameLevelHistoryDao();
    }

    public Result doWork() {
        syncCurrentLevel();

        return Result.success();
    }

    private void syncCurrentLevel() {

        GameProgressEntity currentProgress = progressDao.getProgressSync();
        GameLevelEntity currentLevel = levelDao.getLevelSync(currentProgress.currentLevel);

        float currentTotal;
        // update current progress based on game type (accumulate total from start of the app)
        if(currentLevel.gameType.equals("STEPS")) {
            currentTotal = fitnessDao.getTotalSteps();
        } else {
            currentTotal = fitnessDao.getTotalDistance();
        }

        // update progress value
        currentProgress.progressValue = currentTotal;
        progressDao.updateOrInsertProgress(currentProgress);

        // check if the level can be completed or not
        completeLevel(currentProgress);
    }

    private void completeLevel(GameProgressEntity progress) {
        String today = LocalDate.now().toString();
        int levelNow = progress.currentLevel;
        GameLevelEntity currentLvl = levelDao.getLevelSync(levelNow);
        int targetNow = currentLvl.targetValue;

        if(targetNow<=progress.progressValue) {
            // add completion date to Game Level History
            GameLevelHistoryEntity history = new GameLevelHistoryEntity();
            history.levelNum = currentLvl.levelNum;
            history.gameType = currentLvl.gameType;
            history.completedDate = today;
            gameLevelHistoryDao.insert(history);

            // set progress to next level
            progress.currentLevel++;
            levelNow++;
            // update new progress value
            String newGameType = levelDao.getLevelSync(levelNow).gameType;
            progress.progressValue = (newGameType.equals("STEPS")) ? fitnessDao.getTotalSteps() : fitnessDao.getTotalDistance();
            progress.lastSyncedDate = today;
            progressDao.updateOrInsertProgress(progress);
        }
    }

    private GameProgressEntity initializeProgress(String today) {
        GameProgressEntity progress = new GameProgressEntity();
        progress.currentLevel = 1;
        progress.progressValue = 0;
        progress.id = 1;
        progress.lastSyncedDate = today;
        db.gameProgressDao().updateOrInsertProgress(progress);
        return progress;
    }

}
