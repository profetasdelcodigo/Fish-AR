package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FairLeaderboardDao {
  @Query("SELECT * FROM fair_leaderboard ORDER BY score DESC, maxCombo DESC, createdAtEpochMs ASC LIMIT 100")
  fun observeAll(): Flow<List<FairLeaderboardEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(entry: FairLeaderboardEntity): Long

  @Query("DELETE FROM fair_leaderboard")
  suspend fun clear()
}
