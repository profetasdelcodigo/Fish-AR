package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fair_leaderboard")
data class FairLeaderboardEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val username: String,
  val score: Int,
  val captures: Int = 0,
  val bestSpecies: String = "—",
  val maxCombo: Int = 0,
  val durationSeconds: Int = 360,
  val gameMode: String = "FAIR", // FAIR, COOP, PVP
  val createdAtEpochMs: Long = System.currentTimeMillis()
) {
  val fishesCaught: Int get() = captures
  val bestFishName: String get() = bestSpecies
  val playedAt: Long get() = createdAtEpochMs
}

