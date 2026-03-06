package com.erdevelopments.powerpath.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erdevelopments.powerpath.data.local.DayPlanWorkoutEntity
import com.erdevelopments.powerpath.data.local.model.DayPlanWorkoutItem
import com.erdevelopments.powerpath.data.local.model.WorkoutProgressPoint
import kotlinx.coroutines.flow.Flow

@Dao
interface DayPlanWorkoutDao {

    @Query(
        """
        SELECT
            dp.id AS dayPlanId,
            dp.planId AS planId,
            w.id AS workoutId,
            w.name AS workoutName,
            w.bodyPart AS bodyPart,

            pw.weightKg AS templateWeightKg,
            pw.sets AS templateSets,
            pw.reps AS templateReps,
            pw.restSeconds AS templateRestSeconds,

            dpw.weightKg AS overrideWeightKg,
            dpw.sets AS overrideSets,
            dpw.reps AS overrideReps,
            dpw.restSeconds AS overrideRestSeconds,

            COALESCE(dpw.isDone, 0) AS isDone
        FROM day_plans dp
        INNER JOIN plan_workouts pw ON pw.planId = dp.planId
        INNER JOIN workouts w ON w.id = pw.workoutId
        LEFT JOIN day_plan_workouts dpw
            ON dpw.dayPlanId = dp.id
            AND dpw.workoutId = w.id
        WHERE dp.id = :dayPlanId
        ORDER BY pw.orderIndex ASC
        """
    )
    fun observeWorkouts(dayPlanId: Long): Flow<List<DayPlanWorkoutItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DayPlanWorkoutEntity)

    @Query("DELETE FROM day_plan_workouts WHERE dayPlanId = :dayPlanId AND workoutId = :workoutId")
    suspend fun delete(dayPlanId: Long, workoutId: Long)

    @Query("DELETE FROM day_plan_workouts WHERE dayPlanId = :dayPlanId")
    suspend fun clear(dayPlanId: Long)


    @Query(
        """
        SELECT
            d.id AS dayId,
            d.name AS dayName,

            COALESCE(MAX(COALESCE(dpw.weightKg, pw.weightKg)), 0) AS maxWeightKg,
            COALESCE(SUM(COALESCE(dpw.sets, pw.sets)), 0) AS totalSets,
            COALESCE(SUM(COALESCE(dpw.sets, pw.sets) * COALESCE(dpw.reps, pw.reps)), 0) AS totalReps,
            COALESCE(SUM(
                COALESCE(dpw.weightKg, pw.weightKg) *
                COALESCE(dpw.sets, pw.sets) *
                COALESCE(dpw.reps, pw.reps)
            ), 0) AS totalVolume
        FROM days d
        INNER JOIN day_plans dp ON dp.dayId = d.id
        INNER JOIN plan_workouts pw ON pw.planId = dp.planId AND pw.workoutId = :workoutId
        INNER JOIN day_plan_workouts dpw ON dpw.dayPlanId = dp.id AND dpw.workoutId = :workoutId
        WHERE d.userId = :userId
          AND dpw.isDone = 1
        GROUP BY d.id
        ORDER BY d.orderIndex ASC, d.id ASC
        """
    )
    fun observeWorkoutProgress(userId: Long, workoutId: Long): Flow<List<WorkoutProgressPoint>>
}