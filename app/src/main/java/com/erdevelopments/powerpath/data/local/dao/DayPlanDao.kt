package com.erdevelopments.powerpath.data.local.dao

import androidx.room.*
import com.erdevelopments.powerpath.data.local.DayPlanEntity
import com.erdevelopments.powerpath.data.local.model.DayPlanItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DayPlanDao {

    @Query("SELECT COALESCE(MAX(orderIndex), -1) + 1 FROM day_plans WHERE dayId = :dayId")
    suspend fun nextOrderIndex(dayId: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: DayPlanEntity): Long

    @Query("DELETE FROM day_plans WHERE id = :dayPlanId")
    suspend fun deleteById(dayPlanId: Long)

    @Query(
        """
        SELECT
            dp.id AS dayPlanId,
            dp.dayId AS dayId,
            dp.planId AS planId,
            p.name AS planName,
            dp.orderIndex AS orderIndex,
            COUNT(pw.workoutId) AS totalWorkouts,
            SUM(CASE WHEN COALESCE(dpw.isDone, 0) = 1 THEN 1 ELSE 0 END) AS doneWorkouts
        FROM day_plans dp
        INNER JOIN plans p ON p.id = dp.planId
        LEFT JOIN plan_workouts pw ON pw.planId = dp.planId
        LEFT JOIN day_plan_workouts dpw
            ON dpw.dayPlanId = dp.id
            AND dpw.workoutId = pw.workoutId
        WHERE dp.dayId = :dayId
        GROUP BY dp.id
        ORDER BY dp.orderIndex ASC, dp.id ASC
        """
    )
    fun observeDayPlans(dayId: Long): Flow<List<DayPlanItem>>
}