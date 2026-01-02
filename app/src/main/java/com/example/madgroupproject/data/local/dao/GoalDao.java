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

    // 插入目标
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(GoalEntity goal);

    // 插入多个目标
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GoalEntity> goals);

    // 更新目标
    @Update
    void update(GoalEntity goal);

    // 删除目标
    @Delete
    void delete(GoalEntity goal);

    // 根据ID删除
    @Query("DELETE FROM goals WHERE id = :goalId")
    void deleteById(int goalId);

    // 获取所有目标（按显示顺序排序）
    @Query("SELECT * FROM goals ORDER BY displayOrder ASC, createdAt ASC")
    List<GoalEntity> getAllGoals();

    // 获取所有目标（LiveData，用于观察数据变化）
    @Query("SELECT * FROM goals ORDER BY displayOrder ASC, createdAt ASC")
    LiveData<List<GoalEntity>> getAllGoalsLive();

    // 根据ID获取目标
    @Query("SELECT * FROM goals WHERE id = :goalId")
    GoalEntity getGoalById(int goalId);

    // 获取未完成的目标
    @Query("SELECT * FROM goals WHERE completed = 0 ORDER BY displayOrder ASC, createdAt ASC")
    List<GoalEntity> getIncompleteGoals();

    // 获取已完成的目标
    @Query("SELECT * FROM goals WHERE completed = 1 ORDER BY displayOrder ASC, createdAt ASC")
    List<GoalEntity> getCompletedGoals();

    // 更新完成状态（用于用户手动切换目标状态）
    @Query("UPDATE goals SET completed = :completed WHERE id = :goalId")
    void updateCompletedStatus(int goalId, boolean completed);

    // 清空所有目标（用于每日0点清空前一天的目标）
    @Query("DELETE FROM goals")
    void deleteAll();

    // 获取目标总数
    @Query("SELECT COUNT(*) FROM goals")
    int getGoalCount();

    // 🗑️ 已删除：resetAllCompletionStatus()
    // 原因：新需求是清空目标而非重置状态
}
