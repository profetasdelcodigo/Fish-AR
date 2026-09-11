package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FairLeaderboardDao {
  @Query("SELECT * FROM fair_leaderboard WHERE gameMode = :mode ORDER BY score DESC, maxCombo DESC, createdAtEpochMs ASC LIMIT 100")
  fun observeAll(mode: String = "FAIR"): Flow<List<FairLeaderboardEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(entry: FairLeaderboardEntity): Long

  @Query("DELETE FROM fair_leaderboard WHERE gameMode = :mode")
  suspend fun clear(mode: String = "FAIR")
}
