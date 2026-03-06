package com.erdevelopments.powerpath.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.erdevelopments.powerpath.data.local.PlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {

    @Query("SELECT * FROM plans WHERE dayId = :dayId ORDER BY orderIndex ASC, id ASC")
    fun observePlans(dayId: Long): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plans WHERE id = :planId")
    suspend fun getById(planId: Long): PlanEntity?

    // Default name: "Plan " + (count + 1)
    @Query("SELECT COUNT(*) FROM plans WHERE dayId = :dayId")
    suspend fun countForDay(dayId: Long): Int

    @Query("SELECT COALESCE(MAX(orderIndex), -1) + 1 FROM plans WHERE dayId = :dayId")
    suspend fun nextOrderIndex(dayId: Long): Int

    @Insert
    suspend fun insert(plan: PlanEntity): Long

    @Update
    suspend fun update(plan: PlanEntity)

    @Query("UPDATE plans SET name = :name WHERE id = :planId")
    suspend fun updateName(planId: Long, name: String)

    @Delete
    suspend fun delete(plan: PlanEntity)

    @Query("DELETE FROM plans WHERE dayId = :dayId")
    suspend fun deleteAllForDay(dayId: Long)
}