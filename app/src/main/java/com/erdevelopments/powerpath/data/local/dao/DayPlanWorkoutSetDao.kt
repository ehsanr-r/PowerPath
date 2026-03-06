package com.erdevelopments.powerpath.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erdevelopments.powerpath.data.local.DayPlanWorkoutSetEntity
import com.erdevelopments.powerpath.data.local.model.DayPlanWorkoutSetItem
import com.erdevelopments.powerpath.data.local.model.WorkoutProgressPoint
import kotlinx.coroutines.flow.Flow

@Dao
interface DayPlanWorkoutSetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DayPlanWorkoutSetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<DayPlanWorkoutSetEntity>)

    @Query(
        """
        SELECT
            dayPlanId,
            workoutId,
            setNumber,
            weightKg,
            reps,
            isDone
        FROM day_plan_workout_sets
        WHERE dayPlanId = :dayPlanId AND workoutId = :workoutId
        ORDER BY setNumber ASC
        """
    )
    fun observeSets(dayPlanId: Long, workoutId: Long): Flow<List<DayPlanWorkoutSetItem>>

    @Query(
        """
        DELETE FROM day_plan_workout_sets
        WHERE dayPlanId = :dayPlanId AND workoutId = :workoutId
        """
    )
    suspend fun deleteForWorkout(dayPlanId: Long, workoutId: Long)

    @Query(
        """
        SELECT
            d.id AS dayId,
            d.name AS dayName,
            COALESCE(MAX(s.weightKg), 0) AS maxWeightKg,
            COALESCE(COUNT(*), 0) AS totalSets,
            COALESCE(SUM(s.reps), 0) AS totalReps,
            COALESCE(SUM(s.weightKg * s.reps), 0) AS totalVolume
        FROM day_plan_workout_sets s
        INNER JOIN day_plans dp ON dp.id = s.dayPlanId
        INNER JOIN days d ON d.id = dp.dayId
        WHERE d.userId = :userId
          AND s.workoutId = :workoutId
          AND s.isDone = 1
        GROUP BY d.id
        ORDER BY d.orderIndex ASC, d.id ASC
        """
    )
    fun observeWorkoutProgress(userId: Long, workoutId: Long): Flow<List<WorkoutProgressPoint>>
}