package com.erdevelopments.powerpath.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "plan_workouts",
    primaryKeys = ["planId", "workoutId"],
    foreignKeys = [
        ForeignKey(
            entity = PlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(entity = WorkoutEntity::class, parentColumns = ["id"], childColumns = ["workoutId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("planId"), Index("workoutId")]
)
data class PlanWorkoutEntity(
    val planId: Long,
    val workoutId: Long,
    val weightKg: Float,
    val sets: Int,
    val reps: Int,
    val restSeconds: Int = 60,
    val orderIndex: Int
)