package com.example.madgroupproject.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

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

    public static class StreakResult {
        public List<StreakHistoryEntity> streakDays;
        public int streakCount;

        public StreakResult(List<StreakHistoryEntity> streakDays, int streakCount) {
            this.streakDays = streakDays;
            this.streakCount = streakCount;
        }
    }

    // ✅ 获取今日步数 - LiveData 会自动监听数据库变化
    public LiveData<StreakHistoryEntity> getLiveStepsFromStreakEntity() {
        return streakHistoryDao.observeByDate(LocalDate.now().toString());
    }

    // ✅ 获取当前 streak - LiveData 会自动更新
    public LiveData<StreakResult> getCurrentStreakLive() {
        MediatorLiveData<StreakResult> resultLiveData = new MediatorLiveData<>();

        LiveData<List<StreakHistoryEntity>> source = streakHistoryDao.getAchievedDaysDescLive();

        resultLiveData.addSource(source, historyList -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                StreakResult result = calculateCurrentStreak(historyList);
                resultLiveData.postValue(result);
            });
        });

        return resultLiveData;
    }

    private StreakResult calculateCurrentStreak(List<StreakHistoryEntity> achievedDays) {
        String today = LocalDate.now().toString();
        String yesterday = LocalDate.now().minusDays(1).toString();

        if (achievedDays == null || achievedDays.isEmpty()) {
            return new StreakResult(null, 0);
        }

        // 检查今天或昨天是否达标
        if (!achievedDays.get(0).date.equals(today)) {
            if (!achievedDays.get(0).date.equals(yesterday)) {
                return new StreakResult(null, 0);
            }
        }

        // 计算连续 streak
        int streak = 0;
        int endCounter = 0;
        for (int i = 0; i < achievedDays.size(); i++) {
            StreakHistoryEntity currentRecord = achievedDays.get(i);
            if (i > 0) {
                LocalDate currentRecordDate = LocalDate.parse(currentRecord.date);
                LocalDate lastRecordDate = LocalDate.parse(achievedDays.get(i - 1).date);
                if (!currentRecordDate.equals(lastRecordDate.minusDays(1))) {
                    break;
                }
            }
            streak++;
            endCounter = i;
        }

        List<StreakHistoryEntity> result = achievedDays.subList(0, endCounter + 1);
        return new StreakResult(result, streak);
    }

    // ✅ 获取最长 streak - LiveData 会自动重新计算
    public LiveData<Integer> getLongestStreakLiveData() {
        return Transformations.map(
                streakHistoryDao.getAchievedDaysDescLive(),
                achievedList -> {
                    if (achievedList == null || achievedList.isEmpty()) {
                        return 0;
                    }

                    int longest = 1;
                    int current = 1;

                    for (int i = 0; i < achievedList.size() - 1; i++) {
                        LocalDate d1 = LocalDate.parse(achievedList.get(i).date);
                        LocalDate d2 = LocalDate.parse(achievedList.get(i + 1).date);

                        // 检查是否连续
                        if (d1.minusDays(1).equals(d2)) {
                            current++;
                            longest = Math.max(longest, current);
                        } else {
                            current = 1;
                        }
                    }
                    return longest;
                }
        );
    }

    // ✅ 插入或更新步数 - 会触发 LiveData 更新
    public void insertOrUpdateSteps(int steps) {
        Executors.newSingleThreadExecutor().execute(() -> {
            String today = LocalDate.now().toString();
            StreakHistoryEntity todayData = streakHistoryDao.getByDate(today);

            int minSteps;
            boolean achieved;

            if (todayData == null) {
                StreakHistoryEntity yesterday =
                        streakHistoryDao.getByDate(LocalDate.now().minusDays(1).toString());

                minSteps = (yesterday != null) ? yesterday.minStepsRequired : 0;
                achieved = steps >= minSteps;
            } else {
                minSteps = todayData.minStepsRequired;
                achieved = todayData.achieved || steps >= minSteps;
            }

            StreakHistoryEntity entity = new StreakHistoryEntity(
                    today,
                    steps,
                    achieved,
                    minSteps,
                    System.currentTimeMillis()
            );

            streakHistoryDao.insertOrUpdate(entity);
            Log.d("StreakRepository", "Updated today's data: steps=" + steps + ", achieved=" + achieved);
        });
    }

    // ✅ 获取某日 streak - LiveData 会监听变化
    public LiveData<StreakHistoryEntity> getStreakByDateLive(String date) {
        return streakHistoryDao.observeByDate(date);
    }

    // ✅ 更新最小步数目标 - 会触发 LiveData 更新并重新计算所有天数的 achieved 状态
    public void updateMinSteps(String date, int newGoal) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // 获取所有历史数据
            List<StreakHistoryEntity> allData = streakHistoryDao.getAll();

            // 更新所有天数的 minStepsRequired 和 achieved 状态
            for (StreakHistoryEntity entity : allData) {
                // 重新计算 achieved 状态
                boolean newAchieved = entity.steps >= newGoal;

                StreakHistoryEntity updated = new StreakHistoryEntity(
                        entity.date,
                        entity.steps,
                        newAchieved,
                        newGoal,
                        System.currentTimeMillis()
                );

                streakHistoryDao.insertOrUpdate(updated);
            }

            Log.d("StreakRepository", "Updated min steps for all dates to: " + newGoal);
        });
    }

    // ✅ 获取当月数据 - LiveData 会监听变化
    public LiveData<List<StreakHistoryEntity>> getMonthStreakLive(String yearMonth) {
        return streakHistoryDao.observeMonthData(yearMonth);
    }

    // 获取所有数据（用于调试或导出）
    public List<StreakHistoryEntity> getAll() {
        return streakHistoryDao.getAll();
    }
}