package com.erdevelopments.powerpath.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.erdevelopments.powerpath.data.local.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workouts ORDER BY name COLLATE NOCASE")
    fun observeWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getById(workoutId: Long): WorkoutEntity?

    @Insert
    suspend fun insert(workout: WorkoutEntity): Long

    @Update
    suspend fun update(workout: WorkoutEntity)

    @Delete
    suspend fun delete(workout: WorkoutEntity)

    @Query("DELETE FROM workouts")
    suspend fun deleteAll()
}