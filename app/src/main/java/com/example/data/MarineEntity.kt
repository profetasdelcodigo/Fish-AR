package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "caught_fish")
data class CaughtFishEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val speciesId: String,
    val caughtAt: Long,
    val weight: Float
)

@Entity(tableName = "user_settings")
data class UserSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
