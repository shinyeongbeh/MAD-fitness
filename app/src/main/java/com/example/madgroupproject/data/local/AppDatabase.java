package com.example.madgroupproject.data.local;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.madgroupproject.data.local.dao.FitnessDataDao;
import com.example.madgroupproject.data.local.dao.UserProfileDAO;
import com.example.madgroupproject.data.local.dao.StreakHistoryDao;
import com.example.madgroupproject.data.local.entity.FitnessDataEntity;
import com.example.madgroupproject.data.local.entity.UserProfile;
import com.example.madgroupproject.data.local.entity.StreakHistoryEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {
                FitnessDataEntity.class,
                StreakHistoryEntity.class,
//                GameLevelEntity.class,
//                GoalEntity.class,
                UserProfile.class
        },
        version = 2, //change from 1 to 2
        exportSchema = false
)


public abstract class AppDatabase extends RoomDatabase{
    // Singleton instance (only one instance allowed in the app)
    private static volatile AppDatabase INSTANCE;

    public abstract FitnessDataDao fitnessDataDao();
    public abstract StreakHistoryDao streakHistoryDao();
//    public abstract GameLevelDao gameLevelDao();
//    public abstract GoalDao goalDao();


    public abstract UserProfileDAO userProfileDao();

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);
    // Singleton getInstance method
    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "fitness_app_db"
                            )
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
