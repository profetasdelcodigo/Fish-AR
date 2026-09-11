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

    // Inventory
    @Query("SELECT * FROM inventory_items")
    fun getAllInventoryItems(): Flow<List<InventoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryEntity)

    @Query("UPDATE inventory_items SET count = count + :delta WHERE id = :id")
    suspend fun updateInventoryItemCount(id: String, delta: Int)

    // Missions
    @Query("SELECT * FROM missions")
    fun getAllMissions(): Flow<List<MissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity)

    @Query("UPDATE missions SET progress = :progress, isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateMissionProgress(id: String, progress: Int, isCompleted: Boolean)

    @Query("UPDATE missions SET isClaimed = 1 WHERE id = :id")
    suspend fun claimMission(id: String)
}
