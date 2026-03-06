package com.erdevelopments.powerpath.data.local.model

data class DayPlanWorkoutItem(
    val dayPlanId: Long,
    val planId: Long,
    val workoutId: Long,
    val workoutName: String,
    val bodyPart: String,

    // template values (from plan_workouts)
    val templateWeightKg: Float,
    val templateSets: Int,
    val templateReps: Int,
    val templateRestSeconds: Int,

    // overrides (from day_plan_workouts; nullable)
    val overrideWeightKg: Float?,
    val overrideSets: Int?,
    val overrideReps: Int?,
    val overrideRestSeconds: Int?,

    val isDone: Boolean
) {
    val effectiveWeightKg: Float get() = overrideWeightKg ?: templateWeightKg
    val effectiveSets: Int get() = overrideSets ?: templateSets
    val effectiveReps: Int get() = overrideReps ?: templateReps
    val effectiveRestSeconds: Int get() = overrideRestSeconds ?: templateRestSeconds
}