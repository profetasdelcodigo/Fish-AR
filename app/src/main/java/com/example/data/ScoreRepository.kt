package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * ScoreRepository handles local persistent persistence (Room) for competitive scores,
 * fair tournament capture tallies, and leaderboard records.
 */
class ScoreRepository(
    private val fairLeaderboardDao: FairLeaderboardDao? = null,
    private val marineDao: MarineDao? = null
) {

    val allScores: Flow<List<FairLeaderboardEntity>> =
        marineDao?.getFairLeaderboard() ?: fairLeaderboardDao?.observeAll()
        ?: emptyFlow()

    val top10Scores: Flow<List<FairLeaderboardEntity>> =
        marineDao?.getTop10FairLeaderboard() ?: fairLeaderboardDao?.observeAll()
        ?: emptyFlow()

    suspend fun saveScore(
        username: String,
        score: Int,
        captures: Int = 1,
        bestSpecies: String = "Especie Marina",
        maxCombo: Int = 1,
        durationSeconds: Int = 360
    ): Long {
        val entry = FairLeaderboardEntity(
            username = username.trim().ifBlank { "Pescador_Competencia" },
            score = score,
            captures = captures,
            bestSpecies = bestSpecies,
            maxCombo = maxCombo,
            durationSeconds = durationSeconds,
            createdAtEpochMs = System.currentTimeMillis()
        )
        return insertScore(entry)
    }

    suspend fun insertScore(entry: FairLeaderboardEntity): Long {
        var generatedId = 0L
        fairLeaderboardDao?.let {
            generatedId = it.insert(entry)
        }
        marineDao?.let {
            val daoId = it.insertLeaderboardEntry(entry)
            if (generatedId == 0L) generatedId = daoId
        }
        return generatedId
    }

    suspend fun clearScores() {
        fairLeaderboardDao?.clear()
        marineDao?.clearFairLeaderboard()
    }
}
