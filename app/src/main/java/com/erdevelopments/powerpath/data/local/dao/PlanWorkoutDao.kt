package com.erdevelopments.powerpath.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erdevelopments.powerpath.data.local.PlanWorkoutEntity
import com.erdevelopments.powerpath.data.local.model.PlanWorkoutItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanWorkoutDao {

    // List workouts in a plan (with workout definition info)
    @Query(
        """
        SELECT 
            pw.planId,
            pw.workoutId,
            pw.weightKg,
            pw.sets,
            pw.reps,
            pw.restSeconds,
            pw.orderIndex,
            w.name AS workoutName,
            w.bodyPart AS bodyPart,
            w.description AS description,
            w.imageUri AS imageUri
        FROM plan_workouts pw
        INNER JOIN workouts w ON w.id = pw.workoutId
        WHERE pw.planId = :planId
        ORDER BY pw.orderIndex ASC
        """
    )
    fun observePlanWorkoutItems(planId: Long): Flow<List<PlanWorkoutItem>>

    // For computing next orderIndex when adding a workout to a plan
    @Query("SELECT COALESCE(MAX(orderIndex), -1) + 1 FROM plan_workouts WHERE planId = :planId")
    suspend fun nextOrderIndex(planId: Long): Int

    // Insert or replace a specific workout in a plan (planId+workoutId is PK)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PlanWorkoutEntity)

    // Update just the adjustable fields
    @Query(
        """
        UPDATE plan_workouts
        SET weightKg = :weightKg,
            sets = :sets,
            reps = :reps,
            restSeconds = :restSeconds
        WHERE planId = :planId AND workoutId = :workoutId
        """
    )
    suspend fun updateValues(
        planId: Long,
        workoutId: Long,
        weightKg: Float,
        sets: Int,
        reps: Int,
        restSeconds: Int
    )

    // Remove one workout from a plan
    @Query("DELETE FROM plan_workouts WHERE planId = :planId AND workoutId = :workoutId")
    suspend fun deleteFromPlan(planId: Long, workoutId: Long)

    // Remove all workouts from a plan
    @Query("DELETE FROM plan_workouts WHERE planId = :planId")
    suspend fun clearPlan(planId: Long)

    // -------- Summary helpers (offline analytics) --------

    // Total volume for a plan = sum(weight * reps * sets)
    @Query(
        """
        SELECT COALESCE(SUM(weightKg * reps * sets), 0)
        FROM plan_workouts
        WHERE planId = :planId
        """
    )
    fun observePlanVolume(planId: Long): Flow<Float>

    // Total sets in a plan
    @Query(
        """
        SELECT COALESCE(SUM(sets), 0)
        FROM plan_workouts
        WHERE planId = :planId
        """
    )
    fun observePlanTotalSets(planId: Long): Flow<Int>


    @Query(
        """
    SELECT 
        d.id AS dayId,
        d.name AS dayName,
        COALESCE(SUM(
            CASE WHEN COALESCE(dpw.isDone, 0) = 1 THEN
                COALESCE(dpw.weightKg, pw.weightKg) *
                COALESCE(dpw.reps, pw.reps) *
                COALESCE(dpw.sets, pw.sets)
            ELSE 0 END
        ), 0) AS volume
    FROM days d
    LEFT JOIN day_plans dp ON dp.dayId = d.id
    LEFT JOIN plan_workouts pw ON pw.planId = dp.planId
    LEFT JOIN day_plan_workouts dpw
        ON dpw.dayPlanId = dp.id
        AND dpw.workoutId = pw.workoutId
    WHERE d.userId = :userId
    GROUP BY d.id
    ORDER BY d.orderIndex ASC, d.id ASC
    """
    )
    fun observeDayVolumes(userId: Long): Flow<List<DayVolume>>
}