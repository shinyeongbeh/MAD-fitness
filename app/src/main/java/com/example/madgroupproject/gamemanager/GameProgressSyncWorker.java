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

//        String today = LocalDate.now().toString();
//        String yesterday = LocalDate.now().minusDays(1).toString();
//

//        FitnessDataEntity yesterdayFitness = db.fitnessDataDao().getByDate(yesterday);
//
//        if (todayFitness == null)
//            return Result.success();
//
        syncCurrentLevel();

        return Result.success();
    }
//    private void syncCurrentLevel(
//            String today,
//            FitnessDataEntity todayFitness,
//            FitnessDataEntity yesterdayFitness
//    ) {
//
//        GameProgressDao progressDao = db.gameProgressDao();
//        GameLevelDao levelDao = db.gameLevelDao();
//        GameLevelHistoryDao historyDao = db.gameLevelHistoryDao();
//        //------------------------------------------------------------------
//        // Load current progress
//        GameProgressEntity currentProgress = progressDao.getProgressSync();
//
//        if (currentProgress == null) {
//            currentProgress = initializeProgress(today);
//        }
//
//        //---------------------------------------------------------------
//        // Get current level's details
//        GameLevelEntity currentLevelStructure = levelDao.getLevelSync(currentProgress.currentLevel);
//
//        if (currentLevelStructure == null) return;
//
//        //---------------------------------------------------------------
//        // initialize today and yesterday's fitness value based on game type
//        float todayValue;
//        float yesterdayValue;
//
//        if (currentLevelStructure.gameType.equals("STEPS")) { // Steps
//            todayValue = todayFitness.steps;
//            yesterdayValue = yesterdayFitness != null
//                    ? yesterdayFitness.steps
//                    : 0;
//        } else { //Distance
//            todayValue = todayFitness.distanceMeters;
//            yesterdayValue = yesterdayFitness != null
//                    ? yesterdayFitness.distanceMeters
//                    : 0;
//        }
//
//        //---------------------------------------------------------------
//        // calculate the steps / distance updated since last sync
//        float delta = calculateDelta(
//                currentProgress,
//                today,
//                todayValue,
//                yesterdayValue
//        );
//
//        currentProgress.progressValue += delta;
//
//        if (currentProgress.progressValue >= currentLevelStructure.targetValue) {
//            // completion of the level
//            completeLevel(currentProgress, currentLevelStructure, today, todayValue);
//        }
//
//        //update current progress in Game Progress Entity
//        progressDao.updateOrInsertProgress(currentProgress);
//    }

//    private void syncCurrentLevel(String today, FitnessDataEntity todayFitness, FitnessDataEntity yesterdayFitness) {
//        GameProgressDao progressDao = db.gameProgressDao();
//        GameLevelDao levelDao = db.gameLevelDao();
//
//        GameProgressEntity progress = progressDao.getProgressSync();
//        if (progress == null) progress = initializeProgress(today);
//
//        while (true) {
//            GameLevelEntity level = levelDao.getLevelSync(progress.currentLevel);
//            if (level == null) break;
//
//            float todayValue = level.gameType.equals("STEPS")
//                    ? todayFitness.steps
//                    : todayFitness.distanceMeters;
//            float yesterdayValue = level.gameType.equals("STEPS")
//                    ? (yesterdayFitness != null ? yesterdayFitness.steps : 0)
//                    : (yesterdayFitness != null ? yesterdayFitness.distanceMeters : 0);
//
//            float delta = calculateDelta(progress, today, todayValue, yesterdayValue);
//            progress.progressValue += delta;
//
//            if (progress.progressValue >= level.targetValue) {
//                // complete level and move to next
//                completeLevel(progress, level, today, todayValue);
//
//                // reset delta for next level
//                progress.progressValue = 0;
//
//                // if next level type is different, reset baseline
//                GameLevelEntity nextLevel = levelDao.getLevelSync(progress.currentLevel);
//                if (nextLevel == null) break;
//                if (!nextLevel.gameType.equals(level.gameType)) {
//                    progress.lastSyncedFitnessValue = 0;
//                }
//
//                continue; // loop again to process next level immediately
//            }
//
//            break; // current level not completed, exit
//        }
//
//        progressDao.updateOrInsertProgress(progress);
//    }

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

//    private void completeLevel(
//            GameProgressEntity progress,
//            GameLevelEntity level,
//            String today,
//            float todayValue
//    ) {
//        GameLevelHistoryDao historyDao = db.gameLevelHistoryDao();
//
//        // 1. Save level completion history
//        GameLevelHistoryEntity history = new GameLevelHistoryEntity();
//        history.levelNum = level.levelNum;
//        history.gameType = level.gameType;
//        history.completedDate = today;
//        historyDao.insert(history);
//
//        // 2. Calculate overflow
//        float overflow = progress.progressValue - level.targetValue;
//
//        // 3. Advance to next level
//        progress.currentLevel++;
//
//        // 4. Reset progress for next level
//        progress.progressValue = 0;
//
//        // 5. IMPORTANT: reset sync baseline
//        progress.lastSyncedDate = today;
//        progress.lastSyncedFitnessValue = todayValue-overflow;
//    }


//    /**
//     * Handles:
//     * - First sync
//     * - Same-day sync
//     * - Midnight crossover
//     */
//    private float calculateDelta(
//            GameProgressEntity progress,
//            String today,
//            float todayValue,
//            float yesterdayValue
//    ) {
//
//        // First-ever sync
//        if (progress.lastSyncedDate == null) {
//            progress.lastSyncedDate = today;
//            progress.lastSyncedFitnessValue = todayValue;
//            return todayValue;
//        }
//
//        // Same day
//        if (progress.lastSyncedDate.equals(today)) {
//            float delta = todayValue - progress.lastSyncedFitnessValue;
//            progress.lastSyncedFitnessValue = todayValue;
//            return Math.max(delta, 0);
//        }
//
//        // Crossed midnight
//        float yesterdayRemaining = Math.max(yesterdayValue - progress.lastSyncedFitnessValue, 0);
//
//        float delta = yesterdayRemaining + todayValue;
//
//        progress.lastSyncedDate = today;
//        progress.lastSyncedFitnessValue = todayValue;
//
//        return delta;
//    }

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
