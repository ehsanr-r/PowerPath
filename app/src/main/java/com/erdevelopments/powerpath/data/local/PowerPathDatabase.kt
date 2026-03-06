package com.erdevelopments.powerpath.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.erdevelopments.powerpath.data.local.dao.*

@Database(
    entities = [
        UserEntity::class,
        DayEntity::class,
        PlanEntity::class,
        WorkoutEntity::class,
        PlanWorkoutEntity::class,
        DayPlanEntity::class,
        DayPlanWorkoutEntity::class,
        DayPlanWorkoutSetEntity::class
    ],
    version = 7,
    exportSchema = true
)
abstract class PowerPathDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun dayDao(): DayDao
    abstract fun planDao(): PlanDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun planWorkoutDao(): PlanWorkoutDao
    abstract fun dayPlanDao(): DayPlanDao
    abstract fun dayPlanWorkoutDao(): DayPlanWorkoutDao
    abstract fun dayPlanWorkoutSetDao(): DayPlanWorkoutSetDao
}