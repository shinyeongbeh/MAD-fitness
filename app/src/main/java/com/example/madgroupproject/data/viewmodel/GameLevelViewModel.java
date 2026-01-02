package com.example.madgroupproject.data.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.entity.GameLevelEntity;
import com.example.madgroupproject.data.local.entity.GameLevelHistoryEntity;
import com.example.madgroupproject.data.local.entity.GameProgressEntity;
import com.example.madgroupproject.data.repository.GameLevelRepository;

import java.util.List;

public class GameLevelViewModel extends AndroidViewModel {

    private final GameLevelRepository repo;

    public GameLevelViewModel(@NonNull Application app) {
        super(app);
        AppDatabase db = AppDatabase.getDatabase(app);
        repo = new GameLevelRepository(
                db.gameLevelHistoryDao(),
                db.gameProgressDao(),
                db.gameLevelDao()
        );
    }

    public LiveData<List<GameLevelEntity>> getAllLevels() {
        return repo.getAllLevels();
    }

    public LiveData<GameLevelEntity> getLevel(int level) {
        return repo.getLevel(level);
    }

    public LiveData<GameProgressEntity> observeProgress() {
        return repo.observeProgress();
    }

    public void updateProgress(GameProgressEntity entity) {
        repo.updateProgress(entity);
    }

    public void completeLevel(int level, String type, String date) {
        repo.saveLevelCompleted(level, type, date);
    }

    public List<GameLevelHistoryEntity> getHistoryByGameType(String type) {
        return repo.getHistoryByGameType(type);
    }

    public GameLevelHistoryEntity getHistoryForLevel(int level) {
        return repo.getHistoryForLevel(level);
    }

    public LiveData<Integer> getCurrentLevelNumber() {
        return repo.getCurrentLevel();
    }

    public LiveData<GameLevelHistoryEntity> observeHistoryForLevel(int level) {
        return repo.observeHistoryForLevel(level);
    }



}