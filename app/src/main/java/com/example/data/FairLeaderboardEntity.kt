package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fair_leaderboard")
data class FairLeaderboardEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val username: String,
  val score: Int,
  val captures: Int,
  val bestSpecies: String,
  val maxCombo: Int,
  val durationSeconds: Int = 360,
  val createdAtEpochMs: Long = System.currentTimeMillis()
)
