package com.erdevelopments.powerpath.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "day_plan_workouts",
    primaryKeys = ["dayPlanId", "workoutId"],
    foreignKeys = [
        ForeignKey(
            entity = DayPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["dayPlanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["dayPlanId"]), Index(value = ["workoutId"])]
)
data class DayPlanWorkoutEntity(
    val dayPlanId: Long,
    val workoutId: Long,

    val isDone: Boolean = false,

    // Overrides (nullable means "use template values")
    val weightKg: Float? = null,
    val sets: Int? = null,
    val reps: Int? = null,
    val restSeconds: Int? = null
)