package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FairLeaderboardEntity
import com.example.data.FishArDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val FAIR_DURATION_SECONDS = 360

class FairTournamentViewModel(application: Application) : AndroidViewModel(application) {
  private val dao = FishArDatabase.getInstance(application).fairLeaderboardDao()

  val leaderboard = dao.observeAll()

  private val _username = MutableStateFlow("")
  val username: StateFlow<String> = _username.asStateFlow()
  private val _remainingSeconds = MutableStateFlow(FAIR_DURATION_SECONDS)
  val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()
  private val _score = MutableStateFlow(0)
  val score: StateFlow<Int> = _score.asStateFlow()
  private val _captures = MutableStateFlow(0)
  val captures: StateFlow<Int> = _captures.asStateFlow()
  private val _combo = MutableStateFlow(0)
  val combo: StateFlow<Int> = _combo.asStateFlow()
  private val _maxCombo = MutableStateFlow(0)
  val maxCombo: StateFlow<Int> = _maxCombo.asStateFlow()
  private val _bestSpecies = MutableStateFlow("—")
  val bestSpecies: StateFlow<String> = _bestSpecies.asStateFlow()
  private val _running = MutableStateFlow(false)
  val running: StateFlow<Boolean> = _running.asStateFlow()
  private val _finished = MutableStateFlow(false)
  val finished: StateFlow<Boolean> = _finished.asStateFlow()

  private var timerJob: Job? = null

  fun setUsername(value: String) { _username.value = value.take(18) }

  fun start(name: String) {
    val safeName = name.trim().ifBlank { "Pescador" }.take(18)
    timerJob?.cancel()
    _username.value = safeName
    _remainingSeconds.value = FAIR_DURATION_SECONDS
    _score.value = 0
    _captures.value = 0
    _combo.value = 0
    _maxCombo.value = 0
    _bestSpecies.value = "—"
    _finished.value = false
    _running.value = true
    timerJob = viewModelScope.launch {
      while (_remainingSeconds.value > 0 && _running.value) {
        delay(1000)
        _remainingSeconds.update { (it - 1).coerceAtLeast(0) }
      }
      if (_running.value && _remainingSeconds.value == 0) finish()
    }
  }

  fun registerCapture(species: String, basePoints: Int = 100, clean: Boolean = true) {
    if (!_running.value) return
    val nextCombo = if (clean) (_combo.value + 1).coerceAtLeast(1) else 0
    val multiplier = when {
      nextCombo >= 5 -> 3
      nextCombo >= 3 -> 2
      else -> 1
    }
    val gained = basePoints.coerceAtLeast(0) * multiplier
    _score.update { it + gained }
    _captures.update { it + 1 }
    _combo.value = nextCombo
    _maxCombo.update { maxOf(it, nextCombo) }
    if (_captures.value == 1 || gained > 0) _bestSpecies.value = species
  }

  fun registerMiss() {
    if (!_running.value) return
    _combo.value = 0
  }

  fun finish() {
    if (!_running.value && _finished.value) return
    timerJob?.cancel()
    _running.value = false
    _finished.value = true
    if (_captures.value > 0 || _score.value > 0) {
      val entry = FairLeaderboardEntity(
        username = _username.value.ifBlank { "Pescador" },
        score = _score.value,
        captures = _captures.value,
        bestSpecies = _bestSpecies.value,
        maxCombo = _maxCombo.value,
        durationSeconds = FAIR_DURATION_SECONDS
      )
      viewModelScope.launch { dao.insert(entry) }
    }
  }

  fun resetResult() { _finished.value = false }

  override fun onCleared() {
    timerJob?.cancel()
    super.onCleared()
  }
}
