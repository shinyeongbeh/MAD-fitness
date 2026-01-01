package com.example.madgroupproject.data.repository;


import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.dao.GameLevelDao;
import com.example.madgroupproject.data.local.dao.GameLevelHistoryDao;
import com.example.madgroupproject.data.local.dao.GameProgressDao;
import com.example.madgroupproject.data.local.entity.GameLevelEntity;
import com.example.madgroupproject.data.local.entity.GameLevelHistoryEntity;
import com.example.madgroupproject.data.local.entity.GameProgressEntity;

import java.util.List;
import java.util.concurrent.Executors;

public class GameLevelRepository {

    private final GameLevelHistoryDao historyDao;
    private final GameProgressDao progressDao;
    private  final GameLevelDao levelDao;

    public GameLevelRepository(GameLevelHistoryDao historyDao, GameProgressDao progressDao, GameLevelDao levelDao) {
        this.historyDao = historyDao;
        this.progressDao = progressDao;
        this.levelDao = levelDao;
    }

    public LiveData<List<GameLevelEntity>> getAllLevels() {
        return levelDao.getAllLevels();
    }

    public LiveData<GameLevelEntity> getLevel(int level) {
        return levelDao.getLevel(level);
    }

    public LiveData<Integer> getCurrentLevel() {
        return progressDao.getCurrentLevel();
    }

    public LiveData<GameProgressEntity> observeProgress() {
        return progressDao.observeProgress();
    }

    public void updateProgress(GameProgressEntity entity) {
        Executors.newSingleThreadExecutor().execute(() ->
                progressDao.updateOrInsertProgress(entity)
        );
    }

    public void saveLevelCompleted(int level, String type, String date) {
        GameLevelHistoryEntity e = new GameLevelHistoryEntity();
        e.levelNum = level;
        e.gameType = type;
        e.completedDate = date;
        Executors.newSingleThreadExecutor().execute(() ->
                historyDao.insert(e)
        );
    }

    public List<GameLevelHistoryEntity> getHistoryByGameType(String type) {
        return historyDao.getHistoryByGameTypeDesc(type);
    }

    public GameLevelHistoryEntity getHistoryForLevel(int level) {
        return historyDao.getHistoryForLevel(level);
    }

}
