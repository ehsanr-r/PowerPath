package com.erdevelopments.powerpath.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.erdevelopments.powerpath.data.local.dao.DayDao
import com.erdevelopments.powerpath.data.local.dao.PlanDao
import com.erdevelopments.powerpath.data.local.dao.PlanWorkoutDao
import com.erdevelopments.powerpath.data.local.dao.UserDao
import com.erdevelopments.powerpath.data.local.dao.WorkoutDao

@Database(
    entities = [
        UserEntity::class,
        DayEntity::class,
        PlanEntity::class,
        WorkoutEntity::class,
        PlanWorkoutEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class PowerPathDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun dayDao(): DayDao
    abstract fun planDao(): PlanDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun planWorkoutDao(): PlanWorkoutDao
}