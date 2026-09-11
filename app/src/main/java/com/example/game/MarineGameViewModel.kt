package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.MarineSoundEngine
import com.example.model.CoastalZone
import com.example.model.FishSpecies
import com.example.model.GameModeType
import com.example.model.MarineBeacon
import com.example.model.PiuraCoastalZones
import com.example.model.MarineDatabase
import com.example.util.LocationAndOrientationHelper
import com.example.util.MarineMultiplayerManager
import com.example.util.MultiplayerMessage
import com.example.util.MultiplayerState
import com.example.util.RealLocationData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class TournamentState(
  val isActive: Boolean = false,
  val username: String = "",
  val remainingSeconds: Int = 360, // 6:00 min
  val score: Int = 0,
  val fishesCaught: Int = 0,
  val currentCombo: Int = 0,
  val maxCombo: Int = 0,
  val bestFishName: String = "-",
  val isFinished: Boolean = false,
  val isPaused: Boolean = false
)

data class PvpState(
  val isMatchActive: Boolean = false,
  val myScore: Int = 0,
  val myFishesCaught: Int = 0,
  val myCombo: Int = 0,
  val opponentUsername: String = "Rival Marino",
  val opponentScore: Int = 0,
  val opponentFishes: Int = 0,
  val opponentCombo: Int = 0,
  val iAmAlive: Boolean = true,
  val opponentAlive: Boolean = true,
  val remainingSeconds: Int = 360, // 6 min PvP match
  val isFinished: Boolean = false,
  val winnerMessage: String? = null,
  val activeSabotageOnPlayer: String? = null,
  val sabotageSecondsRemaining: Int = 0,
  val mySabotagesAvailable: Int = 2
)

data class CoopState(
  val isMissionActive: Boolean = false,
  val partnerUsername: String = "Compañero Marino",
  val assignedRole: String = "Operador de Choque", // Operador de Choque vs Operador de Red
  val sharedHullPercent: Int = 200,
  val partnerAlive: Boolean = true,
  val iAmAlive: Boolean = true,
  val teamScore: Int = 0,
  val teamFishesCaught: Int = 0,
  val remainingSeconds: Int = 360, // 6 min co-op
  val isFinished: Boolean = false,
  val recentTeamEvent: String = "Sumergible sincronizado y listo para cazar."
)

class MarineGameViewModel(application: Application) : AndroidViewModel(application) {

  companion object {
    // Single source of truth for the Haywire "are you looking at it" check,
    // shared by the per-sensor-frame HUD update and the per-tick game logic
    // so both always agree.
    private const val HAYWIRE_LOOK_ANGLE = 36f
    private const val HAYWIRE_LOOK_PITCH = 30f
    private const val HAYWIRE_AVERT_ANGLE = 46f
    private const val HAYWIRE_AVERT_PITCH = 36f

    // Capture (Reeling) tuning: starts at 20%, progressive reeling with rhythmic taps
    private const val REEL_START_PROGRESS = 0.20f
    private const val REEL_DECAY_PER_TICK = 0.010f
    private const val REEL_PRECISE_GAIN = 0.18f
    private const val REEL_IMPRECISE_GAIN = 0.06f
    private const val REEL_NEEDLE_SPEED = 0.022f
  }

  private var reelNeedleDirection = 1f

  private fun bounceNeedle(position: Float, direction: Float): Float = when {
    position >= 0.97f -> -1f
    position <= 0.03f -> 1f
    else -> direction
  }

  private val locationHelper = LocationAndOrientationHelper(application.applicationContext)

  private val database = com.example.data.MarineDatabase.getDatabase(application)
  private val repository = com.example.data.MarineRepository(database.marineDao())
  val scoreRepository = com.example.data.ScoreRepository(
    fairLeaderboardDao = com.example.data.FishArDatabase.getInstance(application).fairLeaderboardDao(),
    marineDao = database.marineDao()
  )

  // Real GPS & Sensor Flow
  val realLocation: StateFlow<RealLocationData> = locationHelper.currentLocation
  val deviceHeading: StateFlow<Float> = locationHelper.deviceHeading
  val devicePitch: StateFlow<Float> = locationHelper.devicePitch

  // Zones & Beacons
  private val _zones = MutableStateFlow<List<CoastalZone>>(PiuraCoastalZones.defaultZones)
  val zones: StateFlow<List<CoastalZone>> = _zones.asStateFlow()

  private val _selectedZone = MutableStateFlow(
    PiuraCoastalZones.defaultZones.firstOrNull() ?: CoastalZone(
      id = "mancora",
      name = "Bahía de Máncora",
      province = "Talara, Piura",
      latitude = -4.1067,
      longitude = -81.0478,
      signatureDish = "Ceviche de Bonito & Tiradito",
      depthRange = "10 - 45 m",
      initialBeacons = emptyList()
    )
  )
  val selectedZone: StateFlow<CoastalZone> = _selectedZone.asStateFlow()

  // Map Mode: Google Maps Satellite vs Sonar
  private val _isSatelliteMapMode = MutableStateFlow(true)
  val isSatelliteMapMode: StateFlow<Boolean> = _isSatelliteMapMode.asStateFlow()

  // Active AR Game State
  private val _gameState = MutableStateFlow<ArGameState?>(null)
  val gameState: StateFlow<ArGameState?> = _gameState.asStateFlow()

  // Economy & Discoveries
  private val _pescacoins = MutableStateFlow(120)
  val pescacoins: StateFlow<Int> = _pescacoins.asStateFlow()

  private val _unlockedSpeciesIds = MutableStateFlow(setOf("bonito"))
  val unlockedSpeciesIds: StateFlow<Set<String>> = _unlockedSpeciesIds.asStateFlow()
  
  val caughtFishLog = repository.allCaughtFish

  fun addPescacoins(amount: Int) {
    _pescacoins.update { max(0, it + amount) }
    viewModelScope.launch {
      repository.saveSetting("pescacoins", _pescacoins.value.toString())
    }
  }

  // Radar Ping Animation
  private val _radarPingRadius = MutableStateFlow(0f)
  val radarPingRadius: StateFlow<Float> = _radarPingRadius.asStateFlow()

  // Multiplayer Manager (Bluetooth & Local P2P)
  val multiplayerManager = MarineMultiplayerManager(application.applicationContext)
  val multiplayerState: StateFlow<MultiplayerState> = multiplayerManager.connectionState

  // Fair Tournament State (6 Minutes Mode)
  private val _tournamentState = MutableStateFlow(TournamentState())
  val tournamentState: StateFlow<TournamentState> = _tournamentState.asStateFlow()

  // Room Database Fair Leaderboard Flow
  val fairLeaderboard = repository.top10FairLeaderboard
  val coopLeaderboard = repository.top10CoopLeaderboard
  val pvpLeaderboard = repository.top10PvpLeaderboard
  val top10FairLeaderboard = repository.top10FairLeaderboard

  // 1v1 PvP Match State
  private val _pvpState = MutableStateFlow(PvpState())
  val pvpState: StateFlow<PvpState> = _pvpState.asStateFlow()

  // Co-op Duo Match State
  private val _coopState = MutableStateFlow(CoopState())
  val coopState: StateFlow<CoopState> = _coopState.asStateFlow()

  // Active Tutorial View State
  private val _activeTutorialMode = MutableStateFlow<GameModeType?>(null)
  val activeTutorialMode: StateFlow<GameModeType?> = _activeTutorialMode.asStateFlow()

  // Background loops
  var pendingGameStartMode = "FAIR"
  private var encounterLoopJob: Job? = null
  private var batteryDrainJob: Job? = null
  private var tournamentTimerJob: Job? = null
  private var pvpSabotageTimerJob: Job? = null
  private var aiDecisionTimer = 0
  private var haywireGraceTimer = 0f

  // Shock Cooldown
  private var isShockOnCooldown = false

  init {
    viewModelScope.launch {
      val savedCoins = repository.getSetting("pescacoins")?.toIntOrNull()
      if (savedCoins != null) _pescacoins.value = savedCoins

      val savedSpecies = repository.getSetting("unlocked_species")
      if (savedSpecies != null) _unlockedSpeciesIds.value = savedSpecies.split(",").toSet()

      val savedMapMode = repository.getSetting("satellite_mode")?.toBooleanStrictOrNull()
      if (savedMapMode != null) _isSatelliteMapMode.value = savedMapMode

      repository.seedInitialLeaderboardIfEmpty()
    }

    startRadarWaveLoop()
    locationHelper.requestRealLocationUpdate()
    locationHelper.startTrackingOrientation { h, p ->
      rotatePlayerAbsolute(h, p)
    }

    // Continuous Real GPS tracking for Personal Game Mode
    viewModelScope.launch {
      locationHelper.currentLocation.collect { loc ->
        if (_selectedZone.value.isRealGpsMode || _selectedZone.value.id == "gps_personal") {
          updatePersonalGpsZone(loc.latitude, loc.longitude)
        }
      }
    }

    // Continuous device compass orientation tracking
    viewModelScope.launch {
      locationHelper.deviceHeading.collect { heading ->
        _gameState.update { it?.copy(playerHeading = heading) }
      }
    }

    // Listen to incoming Multiplayer & Bluetooth Messages
    viewModelScope.launch {
      multiplayerManager.incomingMessages.collect { msg ->
        when (msg) {
          is MultiplayerMessage.ScoreUpdate -> {
            _pvpState.update {
              it.copy(
                opponentUsername = msg.username,
                opponentScore = msg.score,
                opponentFishes = msg.fishesCaught,
                opponentCombo = msg.combo
              )
            }
          }
          is MultiplayerMessage.SabotageTriggered -> {
            applyIncomingSabotage(msg.type, msg.fromUser)
          }
          is MultiplayerMessage.CoopAction -> {
            handleIncomingCoopAction(msg.actionType, msg.value, msg.extra)
          }
          is MultiplayerMessage.CoopSharedCatch -> {
            _coopState.update {
              it.copy(
                teamScore = it.teamScore + msg.points,
                teamFishesCaught = it.teamFishesCaught + 1,
                recentTeamEvent = "¡${msg.fishName} capturado en equipo! (+${msg.points} pts)"
              )
            }
          }
          is MultiplayerMessage.CoopHullUpdate -> {
            _coopState.update { it.copy(sharedHullPercent = msg.newHullPercent) }
            _gameState.update { it?.copy(hullIntegrityPercent = msg.newHullPercent) }
          }
          is MultiplayerMessage.MatchStart -> {
            // Sincronizar inicio de partida
          }
          is MultiplayerMessage.SpawnFish -> {
            val species = MarineDatabase.speciesList.find { it.id == msg.speciesId } ?: return@collect
            startEncounter(species, forceHeading = msg.initialHeading)
          }
          is MultiplayerMessage.ChatOrAlert -> {
            _coopState.update { it.copy(recentTeamEvent = "${msg.sender}: ${msg.text}") }
          }
        }
      }
    }
  }

  fun updatePersonalGpsZone(lat: Double, lng: Double) {
    val personalZone = PiuraCoastalZones.createPersonalGpsZone(lat, lng)
    _selectedZone.value = personalZone
    // Update default zones list so the first item has the real coordinates
    _zones.update { list ->
      list.map { z ->
        if (z.id == "gps_personal") personalZone else z
      }
    }
  }

  fun switchToPersonalGpsMode() {
    MarineSoundEngine.playSonarPing()
    locationHelper.requestRealLocationUpdate()
    val loc = realLocation.value
    updatePersonalGpsZone(loc.latitude, loc.longitude)
    _transportNotification.value = "📍 ¡Modo Personal GPS Activado! Siguiendo tu posición y orientación real en vivo."
    viewModelScope.launch {
      delay(3800)
      _transportNotification.value = null
    }
  }

  fun centerOnUserGps() {
    MarineSoundEngine.playNavClick()
    locationHelper.requestRealLocationUpdate()
    val loc = realLocation.value
    if (_selectedZone.value.isRealGpsMode || _selectedZone.value.id == "gps_personal") {
      updatePersonalGpsZone(loc.latitude, loc.longitude)
    } else {
      switchToPersonalGpsMode()
    }
  }

  fun toggleMapMode() {
    MarineSoundEngine.playNavClick()
    _isSatelliteMapMode.update { !it }
    viewModelScope.launch {
      repository.saveSetting("satellite_mode", _isSatelliteMapMode.value.toString())
    }
  }

  private val _transportNotification = MutableStateFlow<String?>(null)
  val transportNotification: StateFlow<String?> = _transportNotification.asStateFlow()

  fun selectZone(zone: CoastalZone) {
    transportToZone(zone)
  }

  fun transportToZone(zone: CoastalZone) {
    MarineSoundEngine.playSonarPing()
    if (zone.isRealGpsMode || zone.id == "gps_personal") {
      switchToPersonalGpsMode()
    } else {
      _selectedZone.value = zone
      _transportNotification.value = "🚢 ¡Teletransporte activado a ${zone.name}! Evento fijado en ${zone.province}."
      viewModelScope.launch {
        delay(3800)
        _transportNotification.value = null
      }
    }
  }

  fun dismissTransportNotification() {
    _transportNotification.value = null
  }

  fun startEncounter(species: FishSpecies, forceHeading: Float? = null) {
    encounterLoopJob?.cancel()
    batteryDrainJob?.cancel()

    val isMultiplayer = _coopState.value.isMissionActive || _pvpState.value.isMatchActive
    val initialResource = 100

    val initialHeading = forceHeading ?: ((Random.nextFloat() * 40f - 20f + (_gameState.value?.playerHeading ?: 0f) + 360f) % 360f)
    _gameState.value = ArGameState(
      currentSpecies = species,
      playerHeading = _gameState.value?.playerHeading ?: 0f,
      playerPitch = 0f,
      creatureHeading = initialHeading,
      creaturePitch = 0f,
      creatureDistance = 22f,
      isFlashlightOn = false,
      batteryPercent = initialResource,
      hullIntegrityPercent = initialResource,
      maxBatteryPercent = initialResource,
      maxHullIntegrityPercent = initialResource,
      staticInterference = 0.25f,
      phase = EncounterPhase.Stalking,
      isHaywireActive = false,
      haywireLookingWarning = false,
      isLookingAwaySafely = false,
      haywireAvertedProgress = 0f,
      creatureBehavior = CreatureBehavior.SWIMMING_IDLE,
      chargeTimerProgress = 1f,
      superPezUsed = false,
      electricShockAnimation = false,
      caughtSpeciesHistory = _unlockedSpeciesIds.value,
      playerPescacoins = _pescacoins.value
    )

    MarineSoundEngine.playSonarPing()
    
    // Si somos host en multijugador, sincronizamos el spawn con el otro dispositivo
    val isHost = when (val s = multiplayerState.value) {
      is MultiplayerState.Connected -> s.isHost
      else -> false
    }
    if (isHost && isMultiplayer) {
      multiplayerManager.sendMessage(MultiplayerMessage.SpawnFish(species.id, initialHeading, 22f))
    }

    runEncounterLoop()
    runBatteryRoutine()
  }

  private fun takeDamage(hitMessage: String) {
    val state = _gameState.value ?: return
    // Aseguramos que 3 golpes maten al jugador (100 / 3 = 33.3 -> 34 por golpe)
    val damageAmount = 34

    val newHull = max(0, state.hullIntegrityPercent - damageAmount)

    MarineSoundEngine.playJumpscareSplash()

    // Penalización de puntos por recibir un golpe
    val isCoop = _coopState.value.isMissionActive
    val isPvp = _pvpState.value.isMatchActive
    val isTournament = _tournamentState.value.isActive

    if (isCoop) {
      _coopState.update { it.copy(
        teamScore = max(0, it.teamScore - 50),
        recentTeamEvent = "¡Impacto recibido! -50 pts, Casco -${damageAmount}%",
      )}
      multiplayerManager.sendMessage(MultiplayerMessage.CoopAction("SCORE_PENALTY", 50))
      multiplayerManager.sendMessage(MultiplayerMessage.CoopHullUpdate(newHull))
    } else if (isPvp) {
      _pvpState.update { it.copy(myScore = max(0, it.myScore - 50)) }
      multiplayerManager.sendMessage(MultiplayerMessage.ScoreUpdate(
        username = _tournamentState.value.username,
        score = _pvpState.value.myScore,
        fishesCaught = _pvpState.value.myFishesCaught,
        combo = _pvpState.value.myCombo
      ))
    } else if (isTournament) {
      _tournamentState.update { it.copy(score = max(0, it.score - 50)) }
    }

    if (newHull <= 0) {
      handlePlayerDeath(hitMessage)
    } else {
      _gameState.update {
        it?.copy(
          hullIntegrityPercent = newHull,
          phase = EncounterPhase.Stalking, // Reset to stalking after hit
          creatureDistance = 35f,
          creatureHeading = (it.playerHeading + 180f) % 360f,
          creatureBehavior = CreatureBehavior.SWIMMING_IDLE,
          chargeTimerProgress = 1f
        )
      }
    }
  }

  private fun handlePlayerDeath(message: String) {
    MarineSoundEngine.playJumpscareSplash()

    val isCoop = _coopState.value.isMissionActive
    val isPvp = _pvpState.value.isMatchActive

    val finalMessage = if (isCoop) {
      _coopState.update { it.copy(iAmAlive = false, recentTeamEvent = "¡Has sido eliminado! Queda 1 sobreviviente.") }
      multiplayerManager.sendMessage(MultiplayerMessage.CoopAction("PLAYER_ELIMINATED", 0))
      "¡Has sido eliminado! Queda 1 sobreviviente.\n($message)"
    } else if (isPvp) {
      _pvpState.update { it.copy(iAmAlive = false, myScore = max(0, it.myScore - 150)) }
      multiplayerManager.sendMessage(MultiplayerMessage.CoopAction("PLAYER_ELIMINATED", 0))
      multiplayerManager.sendMessage(MultiplayerMessage.ScoreUpdate(
        username = _tournamentState.value.username,
        score = _pvpState.value.myScore,
        fishesCaught = _pvpState.value.myFishesCaught,
        combo = _pvpState.value.myCombo
      ))
      "¡Has sido eliminado del duelo PvP! Queda 1 sobreviviente.\nPenalización de duelo: -150 pts.\n($message)"
    } else {
      message
    }

    _gameState.update {
      it?.copy(
        phase = EncounterPhase.Splashed(finalMessage),
        hullIntegrityPercent = 0,
        chargeTimerProgress = 0f,
        isHaywireActive = false,
        creatureBehavior = CreatureBehavior.CHARGING_FAST
      )
    }
  }

  fun endEncounter() {
    encounterLoopJob?.cancel()
    batteryDrainJob?.cancel()
    MarineSoundEngine.stopStaticLoop()
    MarineSoundEngine.playNavClick()
    _gameState.value = null
  }

  // Device orientation / touch rotation
  fun rotatePlayer(deltaHeading: Float, deltaPitch: Float = 0f) {
    _gameState.update { state ->
      if (state == null) null
      else {
        val newH = (state.playerHeading + deltaHeading + 360f) % 360f
        val newP = (state.playerPitch + deltaPitch).coerceIn(-60f, 60f)
        applyOrientationUpdate(state, newH, newP)
      }
    }
  }

  private fun rotatePlayerAbsolute(absHeading: Float, absPitch: Float) {
    _gameState.update { state ->
      if (state == null) null
      else {
        applyOrientationUpdate(state, absHeading, absPitch.coerceIn(-60f, 60f))
      }
    }
  }

  private fun applyOrientationUpdate(state: ArGameState, newHeading: Float, newPitch: Float): ArGameState {
    val angleDiff = calculateAngleDifference(newHeading, state.creatureHeading)

    // Static builds up when facing the creature in 3D polar space
    val facingFactor = (1f - (angleDiff / 180f)).coerceIn(0f, 1f)
    val distanceFactor = (1f - (state.creatureDistance / 45f)).coerceIn(0f, 1f)
    val staticVal = (facingFactor * 0.75f + distanceFactor * 0.25f).coerceIn(0.05f, 0.95f)

    // Looking at creature during Haywire is dangerous! These thresholds MUST
    // match the ones used in the Haywire branch of runEncounterLoop() below —
    // otherwise the HUD can show "safe" while the tick that actually applies
    // hull damage / neutralization progress judges it differently.
    val isLookingAtHaywire = state.isHaywireActive && (angleDiff < HAYWIRE_LOOK_ANGLE && abs(newPitch - state.creaturePitch) < HAYWIRE_LOOK_PITCH)
    // Looking away safely: angle difference must clear the wider margin
    val isLookingAway = state.isHaywireActive && (angleDiff >= HAYWIRE_AVERT_ANGLE || abs(newPitch - state.creaturePitch) >= HAYWIRE_AVERT_PITCH)

    MarineSoundEngine.updateStaticLoop(staticVal)

    return state.copy(
      playerHeading = newHeading,
      playerPitch = newPitch,
      staticInterference = staticVal,
      haywireLookingWarning = isLookingAtHaywire,
      isLookingAwaySafely = isLookingAway
    )
  }

  fun toggleFlashlight() {
    val state = _gameState.value ?: return
    val nextState = !state.isFlashlightOn
    MarineSoundEngine.playFlashlightClick(nextState)
    _gameState.update { it?.copy(isFlashlightOn = nextState) }
  }

  // Precise Electric Shock & Sonar Pulse to stun fish and start reeling
  fun fireElectricShock() {
    val state = _gameState.value ?: return
    if (state.batteryPercent < 8 || isShockOnCooldown) return

    isShockOnCooldown = true
    MarineSoundEngine.playElectricShock()

    _gameState.update {
      it?.copy(
        electricShockAnimation = true,
        creatureBehavior = CreatureBehavior.ELECTROCUTED,
        batteryPercent = max(0, it.batteryPercent - 10)
      )
    }

    viewModelScope.launch {
      delay(350)
      _gameState.update { it?.copy(electricShockAnimation = false) }
      delay(350)
      isShockOnCooldown = false
    }

    val phase = state.phase
    val angleDiff = calculateAngleDifference(state.playerHeading, state.creatureHeading)
    val isAimed = angleDiff < 52f

    if (isAimed && (phase is EncounterPhase.RealCharge || phase is EncounterPhase.Stalking)) {
      // Stun the marine creature and begin active Reeling mini-game!
      MarineSoundEngine.playSuccessChime()
      
      if (_coopState.value.isMissionActive) {
        multiplayerManager.sendMessage(MultiplayerMessage.CoopAction("EMP_ASSIST", 50))
      }

      val zoneStart = Random.nextFloat() * 0.35f + 0.20f
      reelNeedleDirection = 1f
      _gameState.update {
        it?.copy(
          phase = EncounterPhase.Reeling(progress = REEL_START_PROGRESS, targetZone = zoneStart..(zoneStart + 0.32f)),
          creatureBehavior = CreatureBehavior.ELECTROCUTED,
          creatureDistance = 8f,
          isFlashlightOn = false,
          reelNeedlePosition = 0.5f
        )
      }
    } else if (phase is EncounterPhase.FakeCharge) {
      // Shot during fake charge / decoy! Small penalty
      _gameState.update {
        it?.copy(
          batteryPercent = max(0, it.batteryPercent - 8),
          creatureBehavior = CreatureBehavior.FEINT_DISSOLVE
        )
      }
    } else if (state.isHaywireActive) {
      // Shocking during Haywire drains battery & warning
      _gameState.update {
        it?.copy(
          batteryPercent = max(0, it.batteryPercent - 10),
          hullIntegrityPercent = max(0, it.hullIntegrityPercent - 12)
        )
      }
    }
  }

  fun activateSuperPezShield() {
    _gameState.update { state ->
      if (state == null) return@update null
      if (state.shieldCooldownSeconds > 0) return@update state
      MarineSoundEngine.playShieldHum()

      if (_coopState.value.isMissionActive) {
        multiplayerManager.sendMessage(MultiplayerMessage.CoopAction("REPAIR_HULL", 25))
      }

      state.copy(
        isShieldActive = true,
        shieldCooldownSeconds = 15,
        superPezUsed = false,
        hullIntegrityPercent = min(state.maxHullIntegrityPercent, state.hullIntegrityPercent + 35),
        batteryPercent = min(state.maxBatteryPercent, state.batteryPercent + 30),
        creatureDistance = 34f,
        phase = if (state.isHaywireActive) EncounterPhase.Stalking else state.phase,
        isHaywireActive = false,
        isLookingAwaySafely = false,
        haywireAvertedProgress = 0f,
        creatureBehavior = CreatureBehavior.SWIMMING_IDLE
      )
    }
  }

  // ================= FNAF AR ENCOUNTER AI LOOP =================
  private fun runEncounterLoop() {
    aiDecisionTimer = 0
    haywireGraceTimer = 0f
    encounterLoopJob = viewModelScope.launch {
      while (true) {
        delay(150)
        val state = _gameState.value ?: break

        if (state.phase is EncounterPhase.Success || state.phase is EncounterPhase.Splashed) {
          MarineSoundEngine.stopStaticLoop()
          break
        }

        val angleDiff = calculateAngleDifference(state.playerHeading, state.creatureHeading)
        val isAimedAt = angleDiff < 38f && abs(state.playerPitch - state.creaturePitch) < 32f
        val isIlluminated = isAimedAt && state.isFlashlightOn
        val speed = state.currentSpecies.attackSpeed

        val isCompetitiveMode = _tournamentState.value.isActive || _pvpState.value.isMatchActive || _coopState.value.isMissionActive

        when (val currentPhase = state.phase) {
          is EncounterPhase.Stalking -> {
            aiDecisionTimer++

            // Slowly orbit around player in world space
            val orbitStep = (Random.nextFloat() * 8f - 4f) * speed
            val nextHeading = (state.creatureHeading + orbitStep + 360f) % 360f

            // Distance creeps closer
            val distDelta = if (isIlluminated) 0.8f else 0.4f * speed
            val nextDist = (state.creatureDistance - distDelta).coerceIn(10f, 46f)

            // Dynamic behavior in stalking
            val stalkingBehavior = if (aiDecisionTimer > 8) CreatureBehavior.AMBUSH_PREPARE else CreatureBehavior.STALKING_CIRCLING

            // If illuminated directly, trigger stun chance into reeling
            if (isIlluminated && aiDecisionTimer > 6) {
              MarineSoundEngine.playSuccessChime()
              val zoneStart = Random.nextFloat() * 0.35f + 0.20f
              reelNeedleDirection = 1f
              _gameState.update {
                it?.copy(
                  phase = EncounterPhase.Reeling(progress = REEL_START_PROGRESS, targetZone = zoneStart..(zoneStart + 0.32f)),
                  creatureBehavior = CreatureBehavior.ELECTROCUTED,
                  creatureDistance = 8f,
                  isFlashlightOn = false,
                  reelNeedlePosition = 0.5f
                )
              }
              continue
            }

            // AI Event Trigger: In competition, focus on fast charges for point hunting!
            val triggerThreshold = if (isCompetitiveMode) 7 else 12
            if (aiDecisionTimer > triggerThreshold) {
              aiDecisionTimer = 0
              val roll = Random.nextFloat()
              if (!isCompetitiveMode && roll < 0.25f) {
                // Haywire Trigger: creature manifests right in front of the player!
                MarineSoundEngine.playHaywireAlarm()
                haywireGraceTimer = 1.2f
                val haywireH = (state.playerHeading + (Random.nextFloat() * 12f - 6f) + 360f) % 360f
                _gameState.update {
                  it?.copy(
                    phase = EncounterPhase.Haywire(remainingSeconds = 3.5f),
                    isHaywireActive = true,
                    haywireLookingWarning = true,
                    isLookingAwaySafely = false,
                    haywireAvertedProgress = 0f,
                    creatureBehavior = CreatureBehavior.FRENZY_HAYWIRE,
                    creatureHeading = haywireH,
                    creaturePitch = it.playerPitch,
                    creatureDistance = 7.5f
                  )
                }
                continue
              } else if (!isCompetitiveMode && roll < 0.50f) {
                // Fake Charge Trigger
                MarineSoundEngine.playDecoyWhoosh()
                val chargeH = (state.playerHeading + (Random.nextFloat() * 16f - 8f) + 360f) % 360f
                _gameState.update {
                  it?.copy(
                    phase = EncounterPhase.FakeCharge(distance = 24f),
                    creatureDistance = 24f,
                    creatureHeading = chargeH,
                    creaturePitch = it.playerPitch,
                    creatureBehavior = CreatureBehavior.CHARGING_FAST
                  )
                }
                continue
              } else {
                // Real Charge Trigger directly toward player! Fast and fun!
                val totalChargeDuration = (24f / (1.2f * speed * 6.5f)).coerceIn(2.2f, 3.8f)
                MarineSoundEngine.playRealChargeApproach()
                val chargeH = (state.playerHeading + (Random.nextFloat() * 12f - 6f) + 360f) % 360f
                _gameState.update {
                  it?.copy(
                    phase = EncounterPhase.RealCharge(
                      distance = 24f,
                      initialDistance = 24f,
                      timeRemainingSeconds = totalChargeDuration,
                      totalTimeSeconds = totalChargeDuration
                    ),
                    creatureDistance = 24f,
                    creatureHeading = chargeH,
                    creaturePitch = it.playerPitch,
                    chargeTimerProgress = 1.0f,
                    creatureBehavior = CreatureBehavior.CHARGING_FAST
                  )
                }
                continue
              }
            }

            _gameState.update {
              it?.copy(
                creatureHeading = nextHeading,
                creatureDistance = nextDist,
                creatureBehavior = stalkingBehavior
              )
            }
          }

          is EncounterPhase.Haywire -> {
            // In Haywire, the creature's heading stays LOCKED at creatureHeading in world space!
            val newRemaining = currentPhase.remainingSeconds - 0.15f
            val isLooking = angleDiff < HAYWIRE_LOOK_ANGLE && abs(state.playerPitch - state.creaturePitch) < HAYWIRE_LOOK_PITCH
            val isAverted = !isLooking && (angleDiff >= HAYWIRE_AVERT_ANGLE || abs(state.playerPitch - state.creaturePitch) >= HAYWIRE_AVERT_PITCH)

            var newHull = state.hullIntegrityPercent
            var newBat = state.batteryPercent
            var newAvertedProg = state.haywireAvertedProgress

            if (haywireGraceTimer > 0f) {
              haywireGraceTimer -= 0.15f
            } else if (isLooking) {
              // Direct eye contact damages hull & drains battery
              newHull = (newHull - 3).coerceAtLeast(0)
              newBat = (newBat - 1).coerceAtLeast(0)
              newAvertedProg = (newAvertedProg - 0.08f).coerceAtLeast(0f)
            } else if (isAverted) {
              // Successfully looking away builds progress to neutralize the frenzy early!
              newAvertedProg = (newAvertedProg + 0.065f).coerceAtMost(1f)
            }

            if (newHull <= 0) {
              takeDamage("¡Fallo de casco por mirar fijamente! El frenesí del ${state.currentSpecies.commonName} rompió el visor.")
              break
            }

            // Early neutralization if player held look-away for ~2.3 seconds, or full timer expired
            if (newAvertedProg >= 1f || newRemaining <= 0f) {
              // Survived Haywire!
              MarineSoundEngine.playAvertSuccess()
              _gameState.update {
                it?.copy(
                  phase = EncounterPhase.Stalking,
                  isHaywireActive = false,
                  haywireLookingWarning = false,
                  isLookingAwaySafely = false,
                  haywireAvertedProgress = 0f,
                  creatureBehavior = CreatureBehavior.SWIMMING_IDLE,
                  creatureDistance = 35f,
                  creatureHeading = (it.playerHeading + 180f) % 360f
                )
              }
            } else {
              _gameState.update {
                it?.copy(
                  phase = EncounterPhase.Haywire(newRemaining),
                  hullIntegrityPercent = newHull,
                  batteryPercent = newBat,
                  haywireLookingWarning = isLooking,
                  isLookingAwaySafely = isAverted,
                  haywireAvertedProgress = newAvertedProg,
                  creatureBehavior = CreatureBehavior.FRENZY_HAYWIRE
                )
              }
            }
          }

          is EncounterPhase.FakeCharge -> {
            // Slower Fake Charge: decreases by 1.1f per tick
            val nextDist = currentPhase.distance - (1.1f * speed)
            if (nextDist <= 11f) {
              // Noticeable Decoy Dispersal into bubbles!
              MarineSoundEngine.playDecoyWhoosh()
              _gameState.update {
                it?.copy(
                  phase = EncounterPhase.Stalking,
                  creatureDistance = 36f,
                  creatureHeading = (it.creatureHeading + 130f) % 360f,
                  creatureBehavior = CreatureBehavior.FEINT_DISSOLVE
                )
              }
            } else {
              _gameState.update {
                it?.copy(
                  phase = EncounterPhase.FakeCharge(nextDist),
                  creatureDistance = nextDist,
                  creatureBehavior = CreatureBehavior.CHARGING_FAST
                )
              }
            }
          }

          is EncounterPhase.RealCharge -> {
            // Real Charge: decreases distance and timer
            val nextDist = currentPhase.distance - (1.05f * speed)
            val updatedTimer = (currentPhase.timeRemainingSeconds - 0.15f).coerceAtLeast(0f)
            val timerProgress = (updatedTimer / currentPhase.totalTimeSeconds).coerceIn(0f, 1f)

            // Audio warning tick when distance decreases
            if (nextDist <= 16f) {
              val urgency = 1f - (nextDist / 16f)
              MarineSoundEngine.playChargeWarningTick(urgency)
            }

            if (nextDist <= 4f || updatedTimer <= 0f) {
              // Creature hits the player
              if (state.superPezUsed) {
                // Deflected by Super Pez shield!
                MarineSoundEngine.playShieldHum()
                _gameState.update {
                  it?.copy(
                    phase = EncounterPhase.Stalking,
                    creatureDistance = 34f,
                    creatureHeading = (it.playerHeading + 180f) % 360f,
                    creatureBehavior = CreatureBehavior.SWIMMING_IDLE,
                    chargeTimerProgress = 1f
                  )
                }
              } else {
                takeDamage("¡Embestida directa del ${state.currentSpecies.commonName}!")
                break
              }
            } else {
              _gameState.update {
                it?.copy(
                  phase = EncounterPhase.RealCharge(
                    distance = nextDist,
                    initialDistance = currentPhase.initialDistance,
                    timeRemainingSeconds = updatedTimer,
                    totalTimeSeconds = currentPhase.totalTimeSeconds
                  ),
                  creatureDistance = nextDist,
                  chargeTimerProgress = timerProgress,
                  creatureBehavior = CreatureBehavior.CHARGING_FAST
                )
              }
            }
          }

          is EncounterPhase.Reeling -> {
            // Pokémon-like capture tension: the magnetic field slowly decays,
            // so the player must actively stabilize it instead of tapping once.
            // The needle sweeps back and forth across the bar; tapping while it
            // sits inside targetZone is what actually rewards precision.
            reelNeedleDirection = bounceNeedle(state.reelNeedlePosition, reelNeedleDirection)
            val nextNeedle = (state.reelNeedlePosition + reelNeedleDirection * REEL_NEEDLE_SPEED).coerceIn(0f, 1f)

            val decayedProgress = (currentPhase.progress - REEL_DECAY_PER_TICK).coerceAtLeast(0f)
            if (decayedProgress <= 0f) {
              MarineSoundEngine.playJumpscareSplash()
              if (_tournamentState.value.isActive) {
                _tournamentState.update { it.copy(currentCombo = 0) }
              }
              _gameState.update {
                it?.copy(
                  phase = EncounterPhase.Splashed("¡El pez rompió el campo magnético y escapó a las profundidades!"),
                  creatureBehavior = CreatureBehavior.FEINT_DISSOLVE
                )
              }
              break
            } else {
              _gameState.update {
                it?.copy(phase = currentPhase.copy(progress = decayedProgress), reelNeedlePosition = nextNeedle)
              }
            }
          }

          else -> {}
        }
      }
    }
  }

  private fun runBatteryRoutine() {
    batteryDrainJob?.cancel()
    batteryDrainJob = viewModelScope.launch {
      while (true) {
        delay(1000)
        _gameState.update { state ->
          if (state == null) null
          else if (state.phase is EncounterPhase.Success || state.phase is EncounterPhase.Splashed) {
            state
          } else {
            val isFlashlight = state.isFlashlightOn
            val bat = state.batteryPercent
            val newBat = if (isFlashlight) {
              max(0, bat - 2)
            } else {
              min(state.maxBatteryPercent, bat + 3) // Regenerates energy automatically when flashlight is off!
            }
            val updatedFlashlight = if (newBat == 0) false else isFlashlight
            val nextShieldCd = max(0, state.shieldCooldownSeconds - 1)
            val shieldActive = state.isShieldActive && nextShieldCd > 9

            state.copy(
              batteryPercent = newBat,
              isFlashlightOn = updatedFlashlight,
              shieldCooldownSeconds = nextShieldCd,
              isShieldActive = shieldActive
            )
          }
        }
      }
    }
  }

  private fun startRadarWaveLoop() {
    viewModelScope.launch {
      while (true) {
        delay(50)
        _radarPingRadius.update { (it + 0.025f) % 1.0f }
      }
    }
  }

  private fun calculateAngleDifference(angle1: Float, angle2: Float): Float {
    val diff = abs(angle1 - angle2) % 360f
    return if (diff > 180f) 360f - diff else diff
  }

  // Magnetic Arcade Mini-game States
  private val _magneticGameScore = MutableStateFlow(0)
  val magneticGameScore: StateFlow<Int> = _magneticGameScore.asStateFlow()

  private val _magneticFishPosition = MutableStateFlow(0.1f)
  val magneticFishPosition: StateFlow<Float> = _magneticFishPosition.asStateFlow()

  private val _magneticTargetZone = MutableStateFlow(0.35f..0.65f)
  val magneticTargetZone: StateFlow<ClosedFloatingPointRange<Float>> = _magneticTargetZone.asStateFlow()

  private val _magneticGameActive = MutableStateFlow(false)
  val magneticGameActive: StateFlow<Boolean> = _magneticGameActive.asStateFlow()

  private var miniGameJob: Job? = null

  fun startMagneticMiniGame() {
    _magneticGameActive.value = true
    miniGameJob?.cancel()
    miniGameJob = viewModelScope.launch {
      var direction = 1f
      var pos = 0.1f
      while (_magneticGameActive.value) {
        delay(35)
        pos += direction * 0.025f
        if (pos >= 0.95f) {
          pos = 0.95f
          direction = -1f
        } else if (pos <= 0.05f) {
          pos = 0.05f
          direction = 1f
        }
        _magneticFishPosition.value = pos
      }
    }
  }

  fun stopMagneticMiniGame() {
    _magneticGameActive.value = false
    miniGameJob?.cancel()
  }

  fun tapMagneticHook(): Boolean {
    val pos = _magneticFishPosition.value
    val target = _magneticTargetZone.value
    val isHit = pos in target

    if (isHit) {
      MarineSoundEngine.playSuccessChime()
      _magneticGameScore.update { it + 1 }
      addPescacoins(20)
      // Randomize target zone for next catch
      val newStart = Random.nextFloat() * 0.5f + 0.1f
      _magneticTargetZone.value = newStart..(newStart + 0.28f)
    } else {
      MarineSoundEngine.playJumpscareSplash()
    }
    return isHit
  }

  fun showTutorial(mode: GameModeType?) {
    MarineSoundEngine.playNavClick()
    _activeTutorialMode.value = mode
  }

  fun dismissTutorial() {
    MarineSoundEngine.playNavClick()
    _activeTutorialMode.value = null
  }

  // ================= TORNEO DE FERIA 6 MINUTOS =================
  fun startFairTournament(username: String) {
    val cleanName = username.trim().ifEmpty { "Pescador ${Random.nextInt(100, 999)}" }
    tournamentTimerJob?.cancel()
    _tournamentState.value = TournamentState(
      isActive = true,
      username = cleanName,
      remainingSeconds = 360, // 6:00 min
      score = 0,
      fishesCaught = 0,
      currentCombo = 0,
      maxCombo = 0,
      bestFishName = "-",
      isFinished = false
    )

    // Timer Loop: 6 minutes countdown
    tournamentTimerJob = viewModelScope.launch {
      while (_tournamentState.value.isActive && _tournamentState.value.remainingSeconds > 0) {
        delay(1000)
        _tournamentState.update { state ->
          val nextSec = state.remainingSeconds - 1
          if (nextSec <= 0) {
            state.copy(remainingSeconds = 0, isFinished = true, isActive = false)
          } else {
            state.copy(remainingSeconds = nextSec)
          }
        }
      }

      // Finish Tournament run & save to Room Database
      finishFairTournament()
    }

    // Spawn first tournament encounter
    spawnNextFishInMatch()
  }


  fun finishFairTournament() {
    tournamentTimerJob?.cancel()
    val state = _tournamentState.value
    _tournamentState.update { it.copy(isActive = false, isFinished = true) }

    viewModelScope.launch {
      if (state.score > 0 || state.fishesCaught > 0) {
        scoreRepository.saveScore(
          username = state.username,
          score = state.score,
          captures = state.fishesCaught,
          bestSpecies = state.bestFishName,
          maxCombo = state.maxCombo,
          durationSeconds = 360 - state.remainingSeconds
        )
        repository.recordFairTournamentRun(
          username = state.username,
          score = state.score,
          fishesCaught = state.fishesCaught,
          bestFishName = state.bestFishName,
          maxCombo = state.maxCombo,
          durationSeconds = 360 - state.remainingSeconds
        )
      }
    }
  }

  fun resetTournamentState() {
    tournamentTimerJob?.cancel()
    _tournamentState.value = TournamentState()
  }

  fun clearFairLeaderboard() {
    viewModelScope.launch {
      scoreRepository.clearScores()
      repository.clearLeaderboard()
    }
  }

  fun seedFairLeaderboard() {
    viewModelScope.launch {
      repository.seedInitialLeaderboardIfEmpty()
    }
  }

  fun recordFairScoreManual(
    username: String,
    score: Int,
    captures: Int,
    bestSpecies: String,
    maxCombo: Int
  ) {
    viewModelScope.launch {
      scoreRepository.saveScore(
        username = username,
        score = score,
        captures = captures,
        bestSpecies = bestSpecies,
        maxCombo = maxCombo,
        durationSeconds = 360
      )
      repository.recordFairTournamentRun(
        username = username,
        score = score,
        fishesCaught = captures,
        bestFishName = bestSpecies,
        maxCombo = maxCombo,
        durationSeconds = 360
      )
    }
  }

  // ================= 1 VS 1 DUEL MULTIPLAYER =================
  private var pvpTimerJob: Job? = null
  private var coopTimerJob: Job? = null

  fun startPvpMatch(opponentName: String = "Rival Marino") {
    val cleanRival = opponentName.trim().ifEmpty { "Rival Celular 2" }
    pvpTimerJob?.cancel()
    _pvpState.value = PvpState(
      isMatchActive = true,
      myScore = 0,
      myFishesCaught = 0,
      myCombo = 0,
      opponentUsername = cleanRival,
      opponentScore = 0,
      opponentFishes = 0,
      opponentCombo = 0,
      iAmAlive = true,
      opponentAlive = true,
      remainingSeconds = 360, // 6:00 min duel
      isFinished = false,
      winnerMessage = null,
      mySabotagesAvailable = 2
    )

    // Notify rival of duel start
    multiplayerManager.sendMessage(
      MultiplayerMessage.CoopAction("DUEL_START", 360, _tournamentState.value.username.ifEmpty { "P1" })
    )

    // PvP Match Countdown
    pvpTimerJob = viewModelScope.launch {
      while (_pvpState.value.isMatchActive && _pvpState.value.remainingSeconds > 0) {
        delay(1000)
        _pvpState.update { state ->
          val next = state.remainingSeconds - 1
          if (next <= 0 || (!state.iAmAlive && !state.opponentAlive)) {
            state.copy(remainingSeconds = max(0, next), isMatchActive = false, isFinished = true, winnerMessage = if (!state.iAmAlive && !state.opponentAlive) "¡Doble eliminación! Fin prematuro del duelo." else state.winnerMessage)
          } else {
            state.copy(remainingSeconds = next)
          }
        }
      }
      finishPvpMatch()
    }

    // Spawn first duel fish
    spawnNextFishInMatch()
  }

  fun finishPvpMatch() {
    pvpTimerJob?.cancel()
    val state = _pvpState.value
    val won = state.myScore > state.opponentScore
    val tied = state.myScore == state.opponentScore
    val defaultWinMsg = if (won) "🏆 ¡VICTORIA! Superaste al rival con ${state.myScore} pts." else if (tied) "🤝 ¡EMPATE! Ambos sumaron ${state.myScore} pts." else "🥈 ¡BUEN DUELO! El rival sumó ${state.opponentScore} pts."
    val winMsg = state.winnerMessage ?: defaultWinMsg
    _pvpState.update { it.copy(isMatchActive = false, isFinished = true, winnerMessage = winMsg) }

    viewModelScope.launch {
      if (state.myScore > 0 || state.myFishesCaught > 0) {
        repository.recordFairTournamentRun(
          username = _tournamentState.value.username.ifEmpty { "Jugador 1v1" },
          score = state.myScore,
          fishesCaught = state.myFishesCaught,
          bestFishName = "Duelo vs ${state.opponentUsername}",
          maxCombo = state.myCombo,
          durationSeconds = 360 - state.remainingSeconds,
          gameMode = "PVP"
        )
      }
    }
  }

  fun sendPvPSabotage(sabotageType: String) {
    val state = _pvpState.value
    if (state.mySabotagesAvailable <= 0) return

    MarineSoundEngine.playNavClick()
    _pvpState.update { it.copy(mySabotagesAvailable = it.mySabotagesAvailable - 1) }

    multiplayerManager.sendMessage(
      MultiplayerMessage.SabotageTriggered(
        type = sabotageType,
        fromUser = _tournamentState.value.username.ifEmpty { "Jugador 1" }
      )
    )
  }

  private fun applyIncomingSabotage(type: String, fromUser: String) {
    MarineSoundEngine.playJumpscareSplash()
    pvpSabotageTimerJob?.cancel()
    _pvpState.update {
      it.copy(
        activeSabotageOnPlayer = type,
        sabotageSecondsRemaining = 5
      )
    }

    pvpSabotageTimerJob = viewModelScope.launch {
      for (i in 5 downTo 1) {
        _pvpState.update { it.copy(sabotageSecondsRemaining = i) }
        delay(1000)
      }
      _pvpState.update { it.copy(activeSabotageOnPlayer = null, sabotageSecondsRemaining = 0) }
    }
  }

  // ================= COOPERATIVE DUO MULTIPLAYER =================
  fun startCoopMatch() {
    coopTimerJob?.cancel()
    _coopState.value = _coopState.value.copy(
      isMissionActive = true,
      teamScore = 0,
      teamFishesCaught = 0,
      sharedHullPercent = 100,
      partnerAlive = true,
      iAmAlive = true,
      remainingSeconds = 360, // 6:00 min Co-op
      isFinished = false,
      recentTeamEvent = "¡Misión Cooperativa iniciada! Protejan el sumergible."
    )

    multiplayerManager.sendMessage(
      MultiplayerMessage.CoopAction("COOP_START", 360, _coopState.value.assignedRole)
    )

    coopTimerJob = viewModelScope.launch {
      while (_coopState.value.isMissionActive && _coopState.value.remainingSeconds > 0) {
        delay(1000)
        _coopState.update { state ->
          val next = state.remainingSeconds - 1
          if (next <= 0 || (!state.iAmAlive && !state.partnerAlive)) {
            state.copy(remainingSeconds = max(0, next), isMissionActive = false, isFinished = true, recentTeamEvent = if (!state.iAmAlive && !state.partnerAlive) "¡Ambos jugadores eliminados! Misión fallida." else state.recentTeamEvent)
          } else {
            state.copy(remainingSeconds = next)
          }
        }
      }
      finishCoopMatch()
    }

    // Spawn first co-op creature
    spawnNextFishInMatch()
  }

  fun finishCoopMatch() {
    coopTimerJob?.cancel()
    val state = _coopState.value
    _coopState.update { it.copy(isMissionActive = false, isFinished = true, recentTeamEvent = "¡Misión Cooperativa completada con éxito!") }

    viewModelScope.launch {
      if (state.teamScore > 0 || state.teamFishesCaught > 0) {
        repository.recordFairTournamentRun(
          username = _tournamentState.value.username.ifEmpty { "Equipo Dúo" },
          score = state.teamScore,
          fishesCaught = state.teamFishesCaught,
          bestFishName = "Misión Dúo",
          maxCombo = 4,
          durationSeconds = 360 - state.remainingSeconds,
          gameMode = "COOP"
        )
      }
    }
  }

  fun spawnNextFishInMatch() {
    val availableFish = MarineDatabase.speciesList
    val randomFish = availableFish.random()
    
    val isCoop = _coopState.value.isMissionActive
    val isPvp = _pvpState.value.isMatchActive
    
    if (isCoop || isPvp) {
      val isHost = (multiplayerState.value as? MultiplayerState.Connected)?.isHost == true
      if (isHost) {
        val initialHeading = (Random.nextFloat() * 40f - 20f + (_gameState.value?.playerHeading ?: 0f) + 360f) % 360f
        multiplayerManager.sendMessage(MultiplayerMessage.SpawnFish(randomFish.id, initialHeading, 22f))
        startEncounter(randomFish, forceHeading = initialHeading)
      }
    } else {
      startEncounter(randomFish)
    }
  }

  fun setCoopRole(role: String) {
    _coopState.update { it.copy(assignedRole = role) }
    multiplayerManager.sendMessage(
      MultiplayerMessage.CoopAction(
        actionType = "ROLE_SELECTED",
        value = if (role == "Operador de Choque") 1 else 2,
        extra = role
      )
    )
  }

  fun triggerCoopAction(action: String) {
    when (action) {
      "EMP_ASSIST" -> {
        MarineSoundEngine.playElectricShock()
        multiplayerManager.sendMessage(MultiplayerMessage.CoopAction("EMP_ASSIST", 50))
      }
      "DEPLOY_NET" -> {
        MarineSoundEngine.playSuccessChime()
        multiplayerManager.sendMessage(MultiplayerMessage.CoopAction("DEPLOY_NET", 100))
      }
      "REPAIR_HULL" -> {
        val maxHull = _gameState.value?.maxHullIntegrityPercent ?: 200
        val newHull = min(maxHull, _coopState.value.sharedHullPercent + 25)
        _coopState.update { it.copy(sharedHullPercent = newHull, recentTeamEvent = "¡Casco reparado +25%!") }
        _gameState.update { it?.copy(hullIntegrityPercent = newHull) }
        multiplayerManager.sendMessage(MultiplayerMessage.CoopHullUpdate(newHull))
      }
    }
  }

  private fun handleIncomingCoopAction(actionType: String, value: Int, extra: String) {
    when (actionType) {
      "DUEL_START" -> {
        if (!_pvpState.value.isMatchActive) {
          startPvpMatch(extra.ifEmpty { "Rival Celular 1" })
        }
      }
      "COOP_START" -> {
        if (!_coopState.value.isMissionActive) {
          startCoopMatch()
        }
      }
      "ROLE_SELECTED" -> {
        val partnerRole = if (value == 1) "Operador de Choque" else "Operador de Red"
        _coopState.update { it.copy(partnerUsername = "Compañero ($partnerRole)", recentTeamEvent = "Tu compañero eligió: $partnerRole") }
      }
      "EMP_ASSIST" -> {
        MarineSoundEngine.playElectricShock()
        _coopState.update { it.copy(recentTeamEvent = "¡Tu compañero descargó Choque EMP!") }
        _gameState.update {
          it?.copy(
            creatureBehavior = CreatureBehavior.ELECTROCUTED,
            creatureDistance = max(8f, it.creatureDistance - 4f)
          )
        }
      }
      "DEPLOY_NET" -> {
        MarineSoundEngine.playSuccessChime()
        _coopState.update { it.copy(recentTeamEvent = "¡Red Magnética desplegada por tu compañero!") }
        advanceReelProgress(0.35f)
      }
      "REPAIR_HULL" -> {
        _coopState.update { it.copy(recentTeamEvent = "¡Tu compañero activó escudo de reparación!") }
      }
      "SPAWN_FISH" -> {
        val initialHeading = value.toFloat()
        val speciesId = extra
        val species = MarineDatabase.speciesList.find { it.id == speciesId } ?: MarineDatabase.speciesList.first()
        startEncounter(species, forceHeading = initialHeading)
      }
      "PLAYER_ELIMINATED" -> {
        MarineSoundEngine.playJumpscareSplash()
        if (_coopState.value.isMissionActive) {
          _coopState.update {
            it.copy(
              partnerAlive = false,
              recentTeamEvent = "¡Compañero eliminado! Queda 1 sobreviviente."
            )
          }
        }
        if (_pvpState.value.isMatchActive) {
          _pvpState.update {
            it.copy(
              opponentAlive = false,
              winnerMessage = if (it.iAmAlive) "🏆 ¡VICTORIA! El rival fue eliminado." else it.winnerMessage
            )
          }
        }
      }
    }
  }

  /**
   * Called when the player taps "ESTABILIZAR CAMPO". The gain depends on
   * whether the needle was inside the precision zone at the moment of the
   * tap: a well-timed tap is worth 3x an off-timing one, so precision (not
   * just tap speed) decides how fast the fish is reeled in.
   */
  fun tapReelStabilizer(): Boolean {
    val state = _gameState.value ?: return false
    val phase = state.phase
    if (phase !is EncounterPhase.Reeling) return false

    val isPrecise = state.reelNeedlePosition in phase.targetZone
    val gain = if (isPrecise) REEL_PRECISE_GAIN else REEL_IMPRECISE_GAIN

    if (isPrecise) MarineSoundEngine.playSuccessChime() else MarineSoundEngine.playNavClick()

    advanceReelProgress(gain)
    return isPrecise
  }

  fun advanceReelProgress(delta: Float) {
    val state = _gameState.value ?: return
    val phase = state.phase
    if (phase is EncounterPhase.Reeling) {
      val newProg = (phase.progress + delta).coerceIn(0f, 1f)
      if (newProg >= 1.0f) {
        MarineSoundEngine.playSuccessChime()
        val caught = state.currentSpecies
        
        viewModelScope.launch {
            val weight = (Random.nextFloat() * 4f) + 1f // Random weight between 1kg and 5kg
            repository.addCaughtFish(caught.id, weight)
        }
        
        _unlockedSpeciesIds.update { it + caught.id }
        viewModelScope.launch {
            repository.saveSetting("unlocked_species", _unlockedSpeciesIds.value.joinToString(","))
        }
        val gainedCoins = caught.energyRequired * 3
        addPescacoins(gainedCoins)

        // Tournament calculations
        val tState = _tournamentState.value
        val pvp = _pvpState.value
        val coop = _coopState.value

        val basePoints = when (caught.id) {
          "super_pez", "mero_murike" -> 500
          "bonito", "jurel", "cachema" -> 250
          else -> 150
        }

        if (tState.isActive) {
          val combo = tState.currentCombo + 1
          val multiplier = when {
            combo >= 3 -> 3.0f
            combo >= 2 -> 2.0f
            combo >= 1 -> 1.5f
            else -> 1.0f
          }
          val addedScore = (basePoints * multiplier).toInt()
          val newScore = tState.score + addedScore
          val newFishes = tState.fishesCaught + 1
          val maxC = max(tState.maxCombo, combo)
          val bestFish = if (caught.energyRequired > 70) caught.commonName else if (tState.bestFishName != "-") tState.bestFishName else caught.commonName

          _tournamentState.update {
            it.copy(
              score = newScore,
              fishesCaught = newFishes,
              currentCombo = combo,
              maxCombo = maxC,
              bestFishName = bestFish
            )
          }

          // Persist automatically in ScoreRepository upon winning competition encounter
          viewModelScope.launch {
            scoreRepository.saveScore(
              username = tState.username.trim().ifBlank { "Pescador_Feria" },
              score = newScore,
              captures = newFishes,
              bestSpecies = bestFish,
              maxCombo = maxC,
              durationSeconds = 360 - tState.remainingSeconds
            )
          }

          multiplayerManager.sendMessage(
            MultiplayerMessage.ScoreUpdate(
              username = tState.username,
              score = newScore,
              fishesCaught = newFishes,
              combo = combo
            )
          )
        }

        if (pvp.isMatchActive) {
          val combo = pvp.myCombo + 1
          val multiplier = if (combo >= 2) 2.0f else 1.0f
          val addedScore = (basePoints * multiplier).toInt()
          val newScore = pvp.myScore + addedScore
          val newFishes = pvp.myFishesCaught + 1

          _pvpState.update {
            it.copy(
              myScore = newScore,
              myFishesCaught = newFishes,
              myCombo = combo
            )
          }

          // Persist automatically in ScoreRepository for 1v1 PvP duel competition
          viewModelScope.launch {
            scoreRepository.saveScore(
              username = "Duelo PvP (P1)",
              score = newScore,
              captures = newFishes,
              bestSpecies = caught.commonName,
              maxCombo = combo,
              durationSeconds = 180 - pvp.remainingSeconds
            )
          }

          multiplayerManager.sendMessage(
            MultiplayerMessage.ScoreUpdate(
              username = "Rival P1",
              score = newScore,
              fishesCaught = newFishes,
              combo = combo
            )
          )
        }

        if (coop.isMissionActive) {
          val addedScore = (basePoints * 1.5f).toInt()
          val newScore = coop.teamScore + addedScore
          val newFishes = coop.teamFishesCaught + 1

          _coopState.update {
            it.copy(
              teamScore = newScore,
              teamFishesCaught = newFishes,
              recentTeamEvent = "¡Captura Dúo de ${caught.commonName}! +$addedScore pts"
            )
          }

          viewModelScope.launch {
            scoreRepository.saveScore(
              username = "Equipo Dúo",
              score = newScore,
              captures = newFishes,
              bestSpecies = caught.commonName,
              maxCombo = 1,
              durationSeconds = 240 - coop.remainingSeconds
            )
          }

          multiplayerManager.sendMessage(
            MultiplayerMessage.CoopSharedCatch(
              speciesId = caught.id,
              fishName = caught.commonName,
              points = addedScore
            )
          )
        }

        _gameState.update { it?.copy(phase = EncounterPhase.Success(caught)) }
      } else if (newProg <= 0.05f) {
        MarineSoundEngine.playJumpscareSplash()
        // Reset combo on escape
        if (_tournamentState.value.isActive) {
          _tournamentState.update { it.copy(currentCombo = 0) }
        }
        _gameState.update {
          it?.copy(phase = EncounterPhase.Splashed("¡El pez rompió el campo magnético y huyó a las profundidades!"))
        }
      } else {
        _gameState.update {
          it?.copy(phase = phase.copy(progress = newProg))
        }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    locationHelper.stopTrackingOrientation()
    locationHelper.stopLocationUpdates()
    encounterLoopJob?.cancel()
    batteryDrainJob?.cancel()
    tournamentTimerJob?.cancel()
    pvpSabotageTimerJob?.cancel()
    miniGameJob?.cancel()
    multiplayerManager.disconnect()
    MarineSoundEngine.stopStaticLoop()
  }
}
