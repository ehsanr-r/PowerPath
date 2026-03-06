package com.erdevelopments.powerpath.data.local.dao

import androidx.room.*
import com.erdevelopments.powerpath.data.local.PlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {

    @Query("SELECT * FROM plans WHERE userId = :userId ORDER BY orderIndex ASC, id ASC")
    fun observePlans(userId: Long): Flow<List<PlanEntity>>

    @Query("SELECT COUNT(*) FROM plans WHERE userId = :userId")
    suspend fun countForUser(userId: Long): Int

    @Query("SELECT COALESCE(MAX(orderIndex), -1) + 1 FROM plans WHERE userId = :userId")
    suspend fun nextOrderIndex(userId: Long): Int

    @Insert
    suspend fun insert(plan: PlanEntity): Long

    @Query("UPDATE plans SET name = :name WHERE id = :planId")
    suspend fun updateName(planId: Long, name: String)

    @Delete
    suspend fun delete(plan: PlanEntity)

    @Query("SELECT * FROM plans WHERE id = :planId")
    suspend fun getById(planId: Long): PlanEntity?
}