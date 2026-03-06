package com.erdevelopments.powerpath.data.local.model

data class WorkoutProgressPoint(
    val dayId: Long,
    val dayName: String,
    val maxWeightKg: Float,
    val totalSets: Int,
    val totalReps: Int,
    val totalVolume: Float
)