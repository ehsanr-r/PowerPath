package com.erdevelopments.powerpath.data.local.dao

import androidx.room.*
import com.erdevelopments.powerpath.data.local.PlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {

    @Query("SELECT * FROM plans ORDER BY orderIndex ASC, id ASC")
    fun observeAllPlans(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plans WHERE dayId = :dayId ORDER BY orderIndex ASC, id ASC")
    fun observePlansForDay(dayId: Long): Flow<List<PlanEntity>>

    @Query("SELECT COUNT(*) FROM plans")
    suspend fun countAll(): Int

    @Query("SELECT COALESCE(MAX(orderIndex), -1) + 1 FROM plans")
    suspend fun nextOrderIndexGlobal(): Int

    @Insert
    suspend fun insert(plan: PlanEntity): Long

    @Update
    suspend fun update(plan: PlanEntity)

    @Query("UPDATE plans SET name = :name WHERE id = :planId")
    suspend fun updateName(planId: Long, name: String)

    @Query("UPDATE plans SET dayId = :dayId WHERE id = :planId")
    suspend fun setPlanDay(planId: Long, dayId: Long?)

    @Delete
    suspend fun delete(plan: PlanEntity)

    @Query("SELECT * FROM plans WHERE id = :planId")
    suspend fun getById(planId: Long): PlanEntity?
}