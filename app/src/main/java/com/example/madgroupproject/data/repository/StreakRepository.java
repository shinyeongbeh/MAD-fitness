package com.example.madgroupproject.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.example.madgroupproject.data.StreakPreferenceManager;
import com.example.madgroupproject.data.local.AppDatabase;
import com.example.madgroupproject.data.local.dao.StreakHistoryDao;
import com.example.madgroupproject.data.local.entity.StreakHistoryEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class StreakRepository {
    private static final String TAG = "StreakRepository";

    private final StreakHistoryDao streakHistoryDao;
    private final StreakPreferenceManager preferenceManager;

    public StreakRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        streakHistoryDao = db.streakHistoryDao();
        preferenceManager = new StreakPreferenceManager(context);
    }

    // ✅ 当前 Streak 结果
    public static class StreakResult {
        public List<StreakHistoryEntity> streakDays;
        public int streakCount;

        public StreakResult(List<StreakHistoryEntity> streakDays, int streakCount) {
            this.streakDays = streakDays;
            this.streakCount = streakCount;
        }
    }

    // ✅ 最长 Streak 的完整信息
    public static class LongestStreakResult {
        public int count;
        public String startDate;
        public String endDate;
        public List<StreakHistoryEntity> days;
        public int totalSteps;
        public int avgSteps;

        public LongestStreakResult(int count, String startDate, String endDate,
                                   List<StreakHistoryEntity> days) {
            this.count = count;
            this.startDate = startDate;
            this.endDate = endDate;
            this.days = days;

            // 计算总步数和平均步数
            if (days != null && !days.isEmpty()) {
                this.totalSteps = 0;
                for (StreakHistoryEntity e : days) {
                    this.totalSteps += e.steps;
                }
                this.avgSteps = count > 0 ? totalSteps / count : 0;
            }
        }

        public static LongestStreakResult empty() {
            return new LongestStreakResult(0, null, null, new ArrayList<>());
        }

        public boolean isEmpty() {
            return count == 0;
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
                try {
                    StreakResult result = calculateCurrentStreak(historyList);
                    resultLiveData.postValue(result);
                } catch (Exception e) {
                    Log.e(TAG, "Error calculating current streak", e);
                    resultLiveData.postValue(new StreakResult(null, 0));
                }
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

        // ✅ 修复：从今天开始往回找连续的达标天数
        // 先找到今天或昨天的记录位置
        int startIndex = -1;
        for (int i = 0; i < achievedDays.size(); i++) {
            if (achievedDays.get(i).date.equals(today) || achievedDays.get(i).date.equals(yesterday)) {
                startIndex = i;
                break;
            }
        }

        // 如果今天和昨天都没有达标，streak为0
        if (startIndex == -1) {
            return new StreakResult(null, 0);
        }

        // ✅ 从找到的位置开始，往后计算连续天数
        int streak = 1; // 至少有1天（今天或昨天）
        int endCounter = startIndex;

        for (int i = startIndex + 1; i < achievedDays.size(); i++) {
            try {
                LocalDate currentRecordDate = LocalDate.parse(achievedDays.get(i).date);
                LocalDate lastRecordDate = LocalDate.parse(achievedDays.get(i - 1).date);

                // 检查是否连续（相差1天）
                if (currentRecordDate.equals(lastRecordDate.minusDays(1))) {
                    streak++;
                    endCounter = i;
                } else {
                    break; // 不连续了，停止
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing date in current streak calculation", e);
                break;
            }
        }

        List<StreakHistoryEntity> result = achievedDays.subList(startIndex, endCounter + 1);
        return new StreakResult(result, streak);
    }

    // ✅ 新增：获取最长 Streak 的完整信息（包括日期范围和统计数据）
    public LiveData<LongestStreakResult> getLongestStreakWithDetailsLive() {
        return Transformations.map(
                streakHistoryDao.getAchievedDaysDescLive(),
                this::calculateLongestStreakWithDetails
        );
    }

    private LongestStreakResult calculateLongestStreakWithDetails(List<StreakHistoryEntity> achievedList) {
        if (achievedList == null || achievedList.isEmpty()) {
            return LongestStreakResult.empty();
        }

        try {
            // 按日期排序(从旧到新)
            List<StreakHistoryEntity> sortedList = new ArrayList<>(achievedList);
            sortedList.sort((a, b) -> a.date.compareTo(b.date));

            int maxCount = 0;
            int maxStartIndex = 0;
            int maxEndIndex = 0;
            int currentCount = 0;
            int currentStartIndex = 0;

            for (int i = 0; i < sortedList.size(); i++) {
                StreakHistoryEntity current = sortedList.get(i);

                if (current.achieved) {
                    if (currentCount == 0) {
                        currentStartIndex = i;
                    }
                    currentCount++;

                    boolean isLastDay = (i == sortedList.size() - 1);
                    boolean nextDayNotConsecutive = false;

                    if (!isLastDay) {
                        try {
                            LocalDate currentDate = LocalDate.parse(current.date);
                            LocalDate nextDate = LocalDate.parse(sortedList.get(i + 1).date);
                            nextDayNotConsecutive = !nextDate.equals(currentDate.plusDays(1))
                                    || !sortedList.get(i + 1).achieved;
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing dates in streak calculation", e);
                            nextDayNotConsecutive = true;
                        }
                    }

                    if (isLastDay || nextDayNotConsecutive) {
                        if (currentCount > maxCount) {
                            maxCount = currentCount;
                            maxStartIndex = currentStartIndex;
                            maxEndIndex = i;
                        }
                        currentCount = 0;
                    }
                } else {
                    currentCount = 0;
                }
            }

            if (maxCount > 0) {
                String startDate = sortedList.get(maxStartIndex).date;
                String endDate = sortedList.get(maxEndIndex).date;
                List<StreakHistoryEntity> days = new ArrayList<>(
                        sortedList.subList(maxStartIndex, maxEndIndex + 1)
                );
                return new LongestStreakResult(maxCount, startDate, endDate, days);
            }

            return LongestStreakResult.empty();
        } catch (Exception e) {
            Log.e(TAG, "Error calculating longest streak", e);
            return LongestStreakResult.empty();
        }
    }

    // 保留向后兼容
    public LiveData<Integer> getLongestStreakLiveData() {
        return Transformations.map(
                getLongestStreakWithDetailsLive(),
                result -> result.count
        );
    }

    // ✅ 插入或更新步数 - 会触发 LiveData 更新
    public void insertOrUpdateSteps(int steps) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String today = LocalDate.now().toString();
                StreakHistoryEntity todayData = streakHistoryDao.getByDate(today);

                int minSteps;
                boolean achieved;

                if (todayData == null) {
                    StreakHistoryEntity yesterday =
                            streakHistoryDao.getByDate(LocalDate.now().minusDays(1).toString());

                    minSteps = (yesterday != null) ? yesterday.minStepsRequired
                            : preferenceManager.getDailyGoal();
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
                Log.d(TAG, "Updated today's data: steps=" + steps + ", achieved=" + achieved);
            } catch (Exception e) {
                Log.e(TAG, "Error updating steps", e);
            }
        });
    }

    // ✅ 获取某日 streak - LiveData 会监听变化
    public LiveData<StreakHistoryEntity> getStreakByDateLive(String date) {
        return streakHistoryDao.observeByDate(date);
    }

    // ✅ 更新最小步数目标 - 添加回调机制
    public void updateMinSteps(int newGoal, OnUpdateCompleteListener listener) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 更新 SharedPreferences
                preferenceManager.setDailyGoal(newGoal);

                // 获取所有历史数据
                List<StreakHistoryEntity> allData = streakHistoryDao.getAll();

                // 只更新今天及以后的记录，保持历史记录不变
                String today = LocalDate.now().toString();

                for (StreakHistoryEntity entity : allData) {
                    if (entity.date.compareTo(today) >= 0) {
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
                }

                Log.d(TAG, "Updated min steps to: " + newGoal);

                if (listener != null) {
                    listener.onUpdateComplete(true, null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating min steps", e);
                if (listener != null) {
                    listener.onUpdateComplete(false, e.getMessage());
                }
            }
        });
    }

    // ✅ 获取当月数据 - LiveData 会监听变化
    public LiveData<List<StreakHistoryEntity>> getMonthStreakLive(String yearMonth) {
        return streakHistoryDao.observeMonthData(yearMonth);
    }

    // ✅ 安全初始化当天记录（自动版）
    public void autoInitTodayRecord() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String today = LocalDate.now().toString();
                StreakHistoryEntity todayData = streakHistoryDao.getByDate(today);

                if (todayData != null) {
                    return;
                }

                StreakHistoryEntity yesterdayData =
                        streakHistoryDao.getByDate(LocalDate.now().minusDays(1).toString());

                int minSteps = (yesterdayData != null) ? yesterdayData.minStepsRequired
                        : preferenceManager.getDailyGoal();

                StreakHistoryEntity newToday = new StreakHistoryEntity(
                        today,
                        0,
                        false,
                        minSteps,
                        System.currentTimeMillis()
                );

                streakHistoryDao.insertOrUpdate(newToday);
                Log.d(TAG, "Auto-initialized today's record: " + today);
            } catch (Exception e) {
                Log.e(TAG, "Error auto-initializing today's record", e);
            }
        });
    }

    // ✅ 回调接口
    public interface OnUpdateCompleteListener {
        void onUpdateComplete(boolean success, String errorMessage);
    }
}