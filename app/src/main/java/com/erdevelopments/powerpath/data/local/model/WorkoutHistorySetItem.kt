package com.erdevelopments.powerpath.data.local.model

data class WorkoutHistorySetItem(
    val dayId: Long,
    val dayName: String,
    val dayPlanId: Long,
    val setNumber: Int,
    val weightKg: Float,
    val reps: Int
)
