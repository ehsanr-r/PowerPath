package com.erdevelopments.powerpath.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erdevelopments.powerpath.data.local.DayPlanWorkoutEntity
import com.erdevelopments.powerpath.data.local.model.DayPlanWorkoutItem
import com.erdevelopments.powerpath.data.local.model.DayVolume
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
            w.imageUri AS imageUri,

            pw.weightKg AS templateWeightKg,
            pw.sets AS templateSets,
            pw.reps AS templateReps,
            pw.restSeconds AS templateRestSeconds,

            dpw.weightKg AS overrideWeightKg,
            dpw.sets AS overrideSets,
            dpw.reps AS overrideReps,
            dpw.restSeconds AS overrideRestSeconds,

            COALESCE((
                SELECT COUNT(*)
                FROM day_plan_workout_sets s
                WHERE s.dayPlanId = dp.id AND s.workoutId = w.id AND s.isDone = 1
            ), 0) AS doneSetCount,

            COALESCE(dpw.sets, pw.sets) AS totalSetCount
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

    @Query(
        """
        DELETE FROM day_plan_workouts
        WHERE dayPlanId = :dayPlanId AND workoutId = :workoutId
        """
    )
    suspend fun delete(dayPlanId: Long, workoutId: Long)

    @Query("DELETE FROM day_plan_workouts WHERE dayPlanId = :dayPlanId")
    suspend fun clear(dayPlanId: Long)

    @Query(
        """
        SELECT 
            d.id AS dayId,
            d.name AS dayName,
            COALESCE(SUM(
                CASE WHEN s.isDone = 1 THEN
                    s.weightKg * s.reps
                ELSE 0 END
            ), 0) AS volume
        FROM days d
        LEFT JOIN day_plans dp ON dp.dayId = d.id
        LEFT JOIN day_plan_workout_sets s ON s.dayPlanId = dp.id
        WHERE d.userId = :userId
        GROUP BY d.id
        ORDER BY d.orderIndex ASC, d.id ASC
        """
    )
    fun observeDayVolumes(userId: Long): Flow<List<DayVolume>>
}
