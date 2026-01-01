package com.example.madgroupproject.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.madgroupproject.data.local.dao.FitnessDataDao;
import com.example.madgroupproject.data.local.dao.GameLevelDao;
import com.example.madgroupproject.data.local.dao.GameLevelHistoryDao;
import com.example.madgroupproject.data.local.dao.GameProgressDao;
import com.example.madgroupproject.data.local.dao.GoalDao;
import com.example.madgroupproject.data.local.dao.StreakHistoryDao;
import com.example.madgroupproject.data.local.dao.UserProfileDAO;
import com.example.madgroupproject.data.local.entity.FitnessDataEntity;
import com.example.madgroupproject.data.local.entity.GameLevelEntity;
import com.example.madgroupproject.data.local.entity.GameLevelHistoryEntity;
import com.example.madgroupproject.data.local.entity.GameProgressEntity;
import com.example.madgroupproject.data.local.entity.GoalEntity;
import com.example.madgroupproject.data.local.entity.StreakHistoryEntity;
import com.example.madgroupproject.data.local.entity.UserProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                FitnessDataEntity.class,
                StreakHistoryEntity.class,
                UserProfile.class,
                GameLevelEntity.class,
                GameLevelHistoryEntity.class,
                GameProgressEntity.class,
                GoalEntity.class  // ✅ 添加Goal实体
        },
        version = 6, // ✅ 升级数据库版本到4 //UserProfileIncreaseSchema
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
    public abstract GoalDao goalDao();  // ✅ 添加GoalDao

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "fitness_app_db"
                            )
                            // ✅ 允许删库重建
                            .fallbackToDestructiveMigration()
                            .addCallback(PREPOPULATE_LEVELS_CALLBACK)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    //populate level data
    private static final RoomDatabase.Callback PREPOPULATE_LEVELS_CALLBACK =
            new RoomDatabase.Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);

                    databaseWriteExecutor.execute(() -> {
                        if (INSTANCE != null) {
                            insertDefaultLevels(INSTANCE.gameLevelDao());
                        }
                    });
                }
            };

    private static void insertDefaultLevels(GameLevelDao dao) {
        List<GameLevelEntity> levels = new ArrayList<>();

        levels.add(createLevel(1, "STEPS", 100));
        levels.add(createLevel(2, "DISTANCE", 1000));
        levels.add(createLevel(3, "STEPS", 1500));
        levels.add(createLevel(4, "DISTANCE", 2000));
        levels.add(createLevel(5, "STEPS", 2500));
        levels.add(createLevel(6, "DISTANCE", 3000));
        levels.add(createLevel(7, "STEPS", 3500));
        levels.add(createLevel(8, "DISTANCE", 4000));
        levels.add(createLevel(9, "STEPS", 4500));
        levels.add(createLevel(10, "DISTANCE", 5000));

        levels.add(createLevel(11, "STEPS", 6000));
        levels.add(createLevel(12, "DISTANCE", 6500));
        levels.add(createLevel(13, "STEPS", 7000));
        levels.add(createLevel(14, "DISTANCE", 7500));
        levels.add(createLevel(15, "STEPS", 8000));
        levels.add(createLevel(16, "DISTANCE", 8500));
        levels.add(createLevel(17, "STEPS", 9000));
        levels.add(createLevel(18, "DISTANCE", 10000));
        levels.add(createLevel(19, "STEPS", 11000));
        levels.add(createLevel(20, "DISTANCE", 12000));


        dao.insertAll(levels);
    }

    private static GameLevelEntity createLevel(int levelNum, String type, int targetValue) {
        GameLevelEntity e = new GameLevelEntity();
        e.levelNum = levelNum;
        e.gameType = type;
        e.targetValue = targetValue;
        return e;
    }
}

