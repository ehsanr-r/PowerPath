package com.erdevelopments.powerpath.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val age: Int,
    val weightKg: Float,
    val heightCm: Int
)