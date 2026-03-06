package com.erdevelopments.powerpath.data.local.model

data class DayPlanWorkoutSetItem(
    val dayPlanId: Long,
    val workoutId: Long,
    val setNumber: Int,
    val weightKg: Float,
    val reps: Int,
    val isDone: Boolean
)