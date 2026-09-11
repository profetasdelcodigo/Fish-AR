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
        if (marineDao.getLeaderboardCount() == 0) {
            val now = System.currentTimeMillis()
            val seedEntries = listOf(
                FairLeaderboardEntity(
                    username = "Don Lucho (Cevichería El Mero)",
                    score = 3450,
                    captures = 12,
                    bestSpecies = "Mero Murike Gigante",
                    maxCombo = 5,
                    durationSeconds = 360,
                    createdAtEpochMs = now - 3600000 * 20
                ),
                FairLeaderboardEntity(
                    username = "Capitán Peralta (Máncora)",
                    score = 2890,
                    captures = 9,
                    bestSpecies = "Pez Espada",
                    maxCombo = 4,
                    durationSeconds = 360,
                    createdAtEpochMs = now - 3600000 * 16
                ),
                FairLeaderboardEntity(
                    username = "Pescadora Rosa (El Ñuro)",
                    score = 2420,
                    captures = 8,
                    bestSpecies = "Bonito del Norte",
                    maxCombo = 3,
                    durationSeconds = 360,
                    createdAtEpochMs = now - 3600000 * 12
                ),
                FairLeaderboardEntity(
                    username = "Mateo_Piura",
                    score = 1980,
                    captures = 7,
                    bestSpecies = "Corvina Dorada",
                    maxCombo = 3,
                    durationSeconds = 360,
                    createdAtEpochMs = now - 3600000 * 8
                ),
                FairLeaderboardEntity(
                    username = "Marino_Talara",
                    score = 1750,
                    captures = 6,
                    bestSpecies = "Jurel Fino",
                    maxCombo = 2,
                    durationSeconds = 360,
                    createdAtEpochMs = now - 3600000 * 6
                ),
                FairLeaderboardEntity(
                    username = "Sra. Elena (Paita)",
                    score = 1520,
                    captures = 5,
                    bestSpecies = "Cachema Plateada",
                    maxCombo = 2,
                    durationSeconds = 360,
                    createdAtEpochMs = now - 3600000 * 4
                ),
                FairLeaderboardEntity(
                    username = "Buceador Carlos",
                    score = 1340,
                    captures = 5,
                    bestSpecies = "Caballa Norteña",
                    maxCombo = 2,
                    durationSeconds = 360,
                    createdAtEpochMs = now - 3600000 * 3
                ),
                FairLeaderboardEntity(
                    username = "Lucas_Surf",
                    score = 1180,
                    captures = 4,
                    bestSpecies = "Lenguado Costero",
                    maxCombo = 1,
                    durationSeconds = 360,
                    createdAtEpochMs = now - 3600000 * 2
                ),
                FairLeaderboardEntity(
                    username = "Pescador_Colán",
                    score = 950,
                    captures = 3,
                    bestSpecies = "Lisa de Bahía",
                    maxCombo = 1,
                    durationSeconds = 360,
                    createdAtEpochMs = now - 3600000
                ),
                FairLeaderboardEntity(
                    username = "Novato_Costero",
                    score = 720,
                    captures = 2,
                    bestSpecies = "Pintadilla",
                    maxCombo = 1,
                    durationSeconds = 360,
                    createdAtEpochMs = now - 1800000
                )
            )
            marineDao.insertLeaderboardEntries(seedEntries)
        }
    }

    suspend fun clearLeaderboard() {
        marineDao.clearFairLeaderboard()
    }
}
