package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MarineDao {
    @Query("SELECT * FROM caught_fish ORDER BY caughtAt DESC")
    fun getAllCaughtFish(): Flow<List<CaughtFishEntity>>

    @Insert
    suspend fun insertCaughtFish(fish: CaughtFishEntity)

    @Query("SELECT * FROM user_settings WHERE `key` = :key")
    suspend fun getSetting(key: String): UserSettingEntity?

    @Query("SELECT * FROM user_settings WHERE `key` = :key")
    fun observeSetting(key: String): Flow<UserSettingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: UserSettingEntity)

    @Query("SELECT * FROM fair_leaderboard WHERE gameMode = :mode ORDER BY score DESC, captures DESC, createdAtEpochMs ASC LIMIT 10")
    fun getTop10FairLeaderboard(mode: String = "FAIR"): Flow<List<FairLeaderboardEntity>>

    @Query("SELECT * FROM fair_leaderboard WHERE gameMode = :mode ORDER BY score DESC, captures DESC, createdAtEpochMs ASC LIMIT 100")
    fun getFairLeaderboard(mode: String = "FAIR"): Flow<List<FairLeaderboardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntry(entry: FairLeaderboardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntries(entries: List<FairLeaderboardEntity>)

    @Query("SELECT COUNT(*) FROM fair_leaderboard WHERE gameMode = :mode")
    suspend fun getLeaderboardCount(mode: String = "FAIR"): Int

    @Query("DELETE FROM fair_leaderboard WHERE gameMode = :mode")
    suspend fun clearFairLeaderboard(mode: String = "FAIR")
}
