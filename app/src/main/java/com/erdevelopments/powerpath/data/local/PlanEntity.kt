package com.erdevelopments.powerpath.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plans",
    foreignKeys = [ForeignKey(
        entity = DayEntity::class,
        parentColumns = ["id"],
        childColumns = ["dayId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("dayId")]
)
data class PlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayId: Long,
    val name: String,
    val orderIndex: Int
)