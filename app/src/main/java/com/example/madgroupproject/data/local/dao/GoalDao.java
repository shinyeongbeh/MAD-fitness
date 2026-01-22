package com.example.madgroupproject.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.madgroupproject.data.local.entity.GoalEntity;

import java.util.List;

@Dao
public interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(GoalEntity goal);

    // insert multiple goals
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GoalEntity> goals);

    @Update
    void update(GoalEntity goal);

    @Delete
    void delete(GoalEntity goal);

    @Query("DELETE FROM goals WHERE id = :goalId")
    void deleteById(int goalId);

    // retrieve all goals (ascending order)
    // 获取所有目标（按显示顺序排序）
    @Query("SELECT * FROM goals ORDER BY displayOrder ASC, createdAt ASC")
    List<GoalEntity> getAllGoals();

    // retrieve all goals (LiveData, to observe data changes)
    // 获取所有目标（LiveData，用于观察数据变化）
    @Query("SELECT * FROM goals ORDER BY displayOrder ASC, createdAt ASC")
    LiveData<List<GoalEntity>> getAllGoalsLive();


    @Query("SELECT * FROM goals WHERE id = :goalId")
    GoalEntity getGoalById(int goalId);

    // get goals that are in incomplete status
    // 获取未完成的目标
    @Query("SELECT * FROM goals WHERE completed = 0 ORDER BY displayOrder ASC, createdAt ASC")
    List<GoalEntity> getIncompleteGoals();

    // get goals that are in completed status
    // 获取已完成的目标
    @Query("SELECT * FROM goals WHERE completed = 1 ORDER BY displayOrder ASC, createdAt ASC")
    List<GoalEntity> getCompletedGoals();

    // update completed status (used when users manually change completed status)
    // 更新完成状态（用于用户手动切换目标状态）
    @Query("UPDATE goals SET completed = :completed WHERE id = :goalId")
    void updateCompletedStatus(int goalId, boolean completed);

    // reset all goals to incomplete (used in daily 0am reset)
    // 重置所有目标的完成状态为未完成（用于每日0点重置）
    @Query("UPDATE goals SET completed = 0")
    void resetAllCompletionStatus();

    @Query("DELETE FROM goals")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM goals")
    int getGoalCount();
}
