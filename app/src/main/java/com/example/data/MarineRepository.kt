package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MarineRepository(private val marineDao: MarineDao) {
    val allCaughtFish: Flow<List<CaughtFishEntity>> = marineDao.getAllCaughtFish()

    suspend fun addCaughtFish(speciesId: String, weight: Float) {
        marineDao.insertCaughtFish(
            CaughtFishEntity(
                speciesId = speciesId,
                caughtAt = System.currentTimeMillis(),
                weight = weight
            )
        )
    }

    fun observeSetting(key: String): Flow<String?> {
        return marineDao.observeSetting(key).map { it?.value }
    }

    suspend fun saveSetting(key: String, value: String) {
        marineDao.saveSetting(UserSettingEntity(key, value))
    }
    
    suspend fun getSetting(key: String): String? {
        return marineDao.getSetting(key)?.value
    }

    val fairLeaderboard: Flow<List<FairLeaderboardEntity>> = marineDao.getFairLeaderboard()
    val top10FairLeaderboard: Flow<List<FairLeaderboardEntity>> = marineDao.getTop10FairLeaderboard("FAIR")
    val top10CoopLeaderboard: Flow<List<FairLeaderboardEntity>> = marineDao.getTop10FairLeaderboard("COOP")
    val top10PvpLeaderboard: Flow<List<FairLeaderboardEntity>> = marineDao.getTop10FairLeaderboard("PVP")

    suspend fun recordFairTournamentRun(
        username: String,
        score: Int,
        fishesCaught: Int,
        bestFishName: String,
        maxCombo: Int,
        durationSeconds: Int,
        gameMode: String = "FAIR"
    ) {
        marineDao.insertLeaderboardEntry(
            FairLeaderboardEntity(
                username = username.trim().ifEmpty { "Pescador Anónimo" },
                score = score,
                captures = fishesCaught,
                bestSpecies = bestFishName,
                maxCombo = maxCombo,
                durationSeconds = durationSeconds,
                gameMode = gameMode
            )
        )
    }

    suspend fun seedInitialLeaderboardIfEmpty() {
        // Tabla de records inicia completamente vacía y se llena solo con partidas terminadas
    }

    suspend fun clearLeaderboard() {
        marineDao.clearFairLeaderboard()
    }

    // Inventory
    fun getAllInventoryItems(): Flow<List<InventoryEntity>> = marineDao.getAllInventoryItems()
    suspend fun saveInventoryItem(id: String, count: Int) = marineDao.insertInventoryItem(InventoryEntity(id, count))
    suspend fun updateInventoryItemCount(id: String, delta: Int) = marineDao.updateInventoryItemCount(id, delta)

    // Missions
    fun getAllMissions(): Flow<List<MissionEntity>> = marineDao.getAllMissions()
    suspend fun saveMission(id: String, progress: Int, target: Int, isCompleted: Boolean, isClaimed: Boolean) = 
        marineDao.insertMission(MissionEntity(id, progress, target, isCompleted, isClaimed))
    suspend fun updateMissionProgress(id: String, progress: Int, isCompleted: Boolean) = 
        marineDao.updateMissionProgress(id, progress, isCompleted)
    suspend fun claimMission(id: String) = marineDao.claimMission(id)

    // Upgrades
    fun getAllUpgrades(): Flow<List<UpgradeEntity>> = marineDao.getAllUpgrades()
    suspend fun saveUpgrade(id: String, level: Int) = marineDao.insertUpgrade(UpgradeEntity(id, level))
    suspend fun updateUpgradeLevel(id: String, level: Int) = marineDao.updateUpgradeLevel(id, level)
}
