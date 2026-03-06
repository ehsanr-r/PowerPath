package com.erdevelopments.powerpath.data.local.model

data class DayPlanItem(
    val dayPlanId: Long,
    val dayId: Long,
    val planId: Long,
    val planName: String,
    val orderIndex: Int,
    val totalWorkouts: Int,
    val doneWorkouts: Int
)