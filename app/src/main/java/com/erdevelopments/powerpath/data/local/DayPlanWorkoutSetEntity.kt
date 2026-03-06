package com.erdevelopments.powerpath.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "day_plan_workout_sets",
    primaryKeys = ["dayPlanId", "workoutId", "setNumber"],
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
    indices = [
        Index(value = ["dayPlanId"]),
        Index(value = ["workoutId"])
    ]
)
data class DayPlanWorkoutSetEntity(
    val dayPlanId: Long,
    val workoutId: Long,
    val setNumber: Int,
    val weightKg: Float,
    val reps: Int,
    val isDone: Boolean = false
)