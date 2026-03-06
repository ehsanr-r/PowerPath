package com.erdevelopments.powerpath.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val bodyPart: String,        // enum name as String
    val description: String? = null,
    val imageUri: String? = null // photo picker uri as String
)