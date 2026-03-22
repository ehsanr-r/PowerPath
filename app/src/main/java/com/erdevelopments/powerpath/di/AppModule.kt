package com.erdevelopments.powerpath.di

import android.content.Context
import androidx.room.Room
import com.erdevelopments.powerpath.data.local.POWER_PATH_DB_NAME
import com.erdevelopments.powerpath.data.local.PowerPathDatabase
import com.erdevelopments.powerpath.data.local.dao.DayDao
import com.erdevelopments.powerpath.data.local.dao.DayPlanDao
import com.erdevelopments.powerpath.data.local.dao.DayPlanWorkoutDao
import com.erdevelopments.powerpath.data.local.dao.DayPlanWorkoutSetDao
import com.erdevelopments.powerpath.data.local.dao.PlanDao
import com.erdevelopments.powerpath.data.local.dao.PlanWorkoutDao
import com.erdevelopments.powerpath.data.local.dao.UserDao
import com.erdevelopments.powerpath.data.local.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PowerPathDatabase {
        return Room.databaseBuilder(
            context,
            PowerPathDatabase::class.java,
            POWER_PATH_DB_NAME
        )
            // During development only. Later replace with proper migrations.
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideDayPlanWorkoutSetDao(db: PowerPathDatabase): DayPlanWorkoutSetDao =
        db.dayPlanWorkoutSetDao()

    @Provides fun provideUserDao(db: PowerPathDatabase): UserDao = db.userDao()
    @Provides fun provideDayDao(db: PowerPathDatabase): DayDao = db.dayDao()
    @Provides fun providePlanDao(db: PowerPathDatabase): PlanDao = db.planDao()
    @Provides fun provideWorkoutDao(db: PowerPathDatabase): WorkoutDao = db.workoutDao()
    @Provides fun providePlanWorkoutDao(db: PowerPathDatabase): PlanWorkoutDao = db.planWorkoutDao()
    @Provides fun provideDayPlanDao(db: PowerPathDatabase): DayPlanDao = db.dayPlanDao()
    @Provides fun provideDayPlanWorkoutDao(db: PowerPathDatabase): DayPlanWorkoutDao = db.dayPlanWorkoutDao()
}