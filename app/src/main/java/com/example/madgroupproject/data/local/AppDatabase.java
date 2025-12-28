package com.example.madgroupproject.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.madgroupproject.data.local.dao.FitnessDataDao;
import com.example.madgroupproject.data.local.dao.GameLevelDao;
import com.example.madgroupproject.data.local.dao.GameLevelHistoryDao;
import com.example.madgroupproject.data.local.dao.GameProgressDao;
import com.example.madgroupproject.data.local.dao.StreakHistoryDao;
import com.example.madgroupproject.data.local.dao.UserProfileDAO;
import com.example.madgroupproject.data.local.entity.FitnessDataEntity;
import com.example.madgroupproject.data.local.entity.GameLevelEntity;
import com.example.madgroupproject.data.local.entity.GameLevelHistoryEntity;
import com.example.madgroupproject.data.local.entity.GameProgressEntity;
import com.example.madgroupproject.data.local.entity.StreakHistoryEntity;
import com.example.madgroupproject.data.local.entity.UserProfile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                FitnessDataEntity.class,
                StreakHistoryEntity.class,
                UserProfile.class,
                GameLevelEntity.class,
                GameLevelHistoryEntity.class,
                GameProgressEntity.class
        },
        version = 2, // ✅ 一定要升版本
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract FitnessDataDao fitnessDataDao();
    public abstract StreakHistoryDao streakHistoryDao();
    public abstract UserProfileDAO userProfileDao();
    public abstract GameProgressDao gameProgressDao();
    public abstract GameLevelDao gameLevelDao();
    public abstract GameLevelHistoryDao gameLevelHistoryDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "fitness_app_db"
                            )
                            // ✅ 关键修复：允许删库重建
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

