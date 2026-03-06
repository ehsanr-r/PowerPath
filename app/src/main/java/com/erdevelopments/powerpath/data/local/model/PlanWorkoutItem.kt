package com.erdevelopments.powerpath.data.local.model


data class PlanWorkoutItem(
    // from PlanWorkoutEntity (must match column names)
    val planId: Long,
    val workoutId: Long,
    val weightKg: Float,
    val sets: Int,
    val reps: Int,
    val restSeconds: Int,
    val orderIndex: Int,

    // from WorkoutEntity via JOIN
    val workoutName: String,
    val bodyPart: String,
    val description: String?,
    val imageUri: String?
)