package com.erdevelopments.powerpath.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.erdevelopments.powerpath.data.local.DayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DayDao {

    @Query("SELECT * FROM days WHERE userId = :userId ORDER BY orderIndex ASC, id ASC")
    fun observeDays(userId: Long): Flow<List<DayEntity>>

    @Query("SELECT * FROM days WHERE id = :dayId")
    suspend fun getById(dayId: Long): DayEntity?

    // Used to generate default name: "Day " + (count + 1)
    @Query("SELECT COUNT(*) FROM days WHERE userId = :userId")
    suspend fun countForUser(userId: Long): Int

    // Used to append to end (orderIndex)
    @Query("SELECT COALESCE(MAX(orderIndex), -1) + 1 FROM days WHERE userId = :userId")
    suspend fun nextOrderIndex(userId: Long): Int

    @Insert
    suspend fun insert(day: DayEntity): Long

    @Update
    suspend fun update(day: DayEntity)

    @Query("UPDATE days SET name = :name WHERE id = :dayId")
    suspend fun updateName(dayId: Long, name: String)

    @Delete
    suspend fun delete(day: DayEntity)

    @Query("DELETE FROM days WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)
}