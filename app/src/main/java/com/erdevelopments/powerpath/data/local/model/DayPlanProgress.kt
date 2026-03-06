package com.erdevelopments.powerpath.data.local.model

data class DayPlanProgress(
    val dayPlanId: Long,
    val totalWorkouts: Int,
    val doneWorkouts: Int
)