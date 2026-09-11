package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.MarineSoundEngine
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
  val myUsername: String = "Jugador",
  val isReady: Boolean = false,
  val opponentReady: Boolean = false,
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
  val myUsername: String = "Jugador",
  val isReady: Boolean = false,
  val partnerReady: Boolean = false,
  val partnerUsername: String = "Compañero Marino",
  val assignedRole: String = "Operador de Choque", // Operador de Choque vs Operador de Red
  val partnerHullPercent: Int = 100,
  val partnerAlive: Boolean = true,
  val iAmAlive: Boolean = true,
  val teamScore: Int = 0,
  val teamFishesCaught: Int = 0,
  val remainingSeconds: Int = 360, // 6 min co-op
  val isFinished: Boolean = false,
  val recentTeamEvent: String = "Sumergible sincronizado y listo para cazar."
)

data class MissionState(
  val id: String,
  val title: String,
  val progress: Int,
  val target: Int,
  val reward: Int,
  val isCompleted: Boolean,
  val isClaimed: Boolean,
  val icon: androidx.compose.ui.graphics.vector.ImageVector = androidx.compose.material.icons.Icons.Default.Public
)

data class InventoryItemState(
  val id: String,
  val name: String,
  val count: Int
)

data class UpgradeState(
  val id: String,
  val name: String,
  val level: Int,
  val maxLevel: Int = 3
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
  
  private val _allMissions = MutableStateFlow<List<MissionState>>(emptyList())
  val allMissions: StateFlow<List<MissionState>> = _allMissions.asStateFlow()

  private val _allInventory = MutableStateFlow<List<InventoryItemState>>(emptyList())
  val allInventory: StateFlow<List<InventoryItemState>> = _allInventory.asStateFlow()

  private val _allUpgrades = MutableStateFlow<List<UpgradeState>>(emptyList())
  val allUpgrades: StateFlow<List<UpgradeState>> = _allUpgrades.asStateFlow()

  private val _ownedStoreItems = MutableStateFlow<Set<String>>(emptySet())
  val ownedStoreItems: StateFlow<Set<String>> = _ownedStoreItems.asStateFlow()

  private val _socialSharingEnabled = MutableStateFlow(false)
  val socialSharingEnabled: StateFlow<Boolean> = _socialSharingEnabled.asStateFlow()

  private val _nightModeEnabled = MutableStateFlow(false)
  val nightModeEnabled: StateFlow<Boolean> = _nightModeEnabled.asStateFlow()

  private val _welcomeCompleted = MutableStateFlow(false)
  val welcomeCompleted: StateFlow<Boolean> = _welcomeCompleted.asStateFlow()

  val caughtFishLog = repository.allCaughtFish

  fun addPescacoins(amount: Int) {
    _pescacoins.update { max(0, it + amount) }
    viewModelScope.launch {
      repository.saveSetting("pescacoins", _pescacoins.value.toString())
    }
  }

  fun upgradeEquipment(id: String, cost: Int) {
    if (_pescacoins.value >= cost) {
      addPescacoins(-cost)
      val currentLevel = _allUpgrades.value.find { it.id == id }?.level ?: 0
      viewModelScope.launch {
        repository.updateUpgradeLevel(id, currentLevel + 1)
      }
      MarineSoundEngine.playSuccessChime()
    }
  }

  fun buyStoreItem(id: String, cost: Int) {
    if (!_ownedStoreItems.value.contains(id) && _pescacoins.value >= cost) {
      addPescacoins(-cost)
      _ownedStoreItems.update { it + id }
      viewModelScope.launch {
        repository.saveSetting("owned_items", _ownedStoreItems.value.joinToString(","))
      }
      MarineSoundEngine.playSuccessChime()
    }
  }

  fun toggleSocialSharing() {
    val newValue = !_socialSharingEnabled.value
    _socialSharingEnabled.value = newValue
    viewModelScope.launch {
      repository.saveSetting("social_sharing", newValue.toString())
    }
  }

  fun toggleNightMode() {
    val newValue = !_nightModeEnabled.value
    _nightModeEnabled.value = newValue
    viewModelScope.launch {
      repository.saveSetting("night_mode", newValue.toString())
    }
  }

  fun completeWelcome() {
    _welcomeCompleted.value = true
    viewModelScope.launch {
      repository.saveSetting("welcome_completed", "true")
    }
  }

  fun resetWelcome() {
    _welcomeCompleted.value = false
    viewModelScope.launch {
      repository.saveSetting("welcome_completed", "false")
    }
  }

  // Radar Ping Animation
  private val _radarPingRadius = MutableStateFlow(0f)
  val radarPingRadius: StateFlow<Float> = _radarPingRadius.asStateFlow()

  // Multiplayer Manager (Bluetooth & Local P2P)
  val multiplayerManager = MarineMultiplayerManager(application.applicationContext)
  val multiplayerState: StateFlow<MultiplayerState> = multiplayerManager.connectionState
  private var matchRandom = Random(System.currentTimeMillis())

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

      val savedOwnedItems = repository.getSetting("owned_items")
      if (savedOwnedItems != null) _ownedStoreItems.value = savedOwnedItems.split(",").toSet()

      val savedSocialSharing = repository.getSetting("social_sharing")?.toBooleanStrictOrNull()
      if (savedSocialSharing != null) _socialSharingEnabled.value = savedSocialSharing

      val savedNightMode = repository.getSetting("night_mode")?.toBooleanStrictOrNull()
      if (savedNightMode != null) _nightModeEnabled.value = savedNightMode

      val savedWelcome = repository.getSetting("welcome_completed")?.toBooleanStrictOrNull()
      if (savedWelcome != null) _welcomeCompleted.value = savedWelcome

      repository.seedInitialLeaderboardIfEmpty()

      // Real Missions & Inventory seeding
      launch {
        repository.getAllMissions().collect { missions ->
          if (missions.isEmpty()) {
            repository.saveMission("m1", 0, 5, false, false)
            repository.saveMission("m2", 0, 3, false, false)
            repository.saveMission("m3", 0, 1, false, false)
            repository.saveMission("m4", 1, 1, true, false)
          } else {
            _allMissions.value = missions.map {
              val title = when(it.id) {
                "m1" -> "Captura 5 peces raros"
                "m2" -> "Explora 3 zonas marinas"
                "m3" -> "Encuentro sin ser visto"
                "m4" -> "Participa en la Feria"
                else -> "Misión Marina"
              }
              val reward = when(it.id) {
                "m1" -> 100
                "m2" -> 150
                "m3" -> 200
                "m4" -> 250
                else -> 50
              }
              val icon = when(it.id) {
                "m1" -> androidx.compose.material.icons.Icons.Default.Shield
                "m2" -> androidx.compose.material.icons.Icons.Default.Explore
                "m3" -> androidx.compose.material.icons.Icons.Default.VisibilityOff
                "m4" -> androidx.compose.material.icons.Icons.Default.Public
                else -> androidx.compose.material.icons.Icons.Default.Public
              }
              MissionState(it.id, title, it.progress, it.target, reward, it.isCompleted, it.isClaimed, icon)
            }
          }
        }
      }

      launch {
        repository.getAllInventoryItems().collect { items ->
          if (items.isEmpty()) {
            repository.saveInventoryItem("flashlight", 5)
            repository.saveInventoryItem("shield", 3)
            repository.saveInventoryItem("bait", 8)
            repository.saveInventoryItem("battery", 12)
            repository.saveInventoryItem("lightning", 4)
            repository.saveInventoryItem("medal", 6)
          } else {
            _allInventory.value = items.map {
              val name = when(it.id) {
                "flashlight" -> "Linterna Marina UV"
                "shield" -> "Escudo Súper Pez"
                "bait" -> "Cebo Artesanal"
                "battery" -> "Célula de Batería"
                "lightning" -> "Rayo Eléctrico"
                "medal" -> "Medalla de Captura"
                else -> it.id
              }
              InventoryItemState(it.id, name, it.count)
            }
          }
        }
      }
      launch {
        repository.getAllUpgrades().collect { upgrades ->
          if (upgrades.isEmpty()) {
            repository.saveUpgrade("reactor", 0)
            repository.saveUpgrade("uv", 0)
            repository.saveUpgrade("shield", 0)
            repository.saveUpgrade("sonar", 0)
            repository.saveUpgrade("stabilizer", 0)
          } else {
            _allUpgrades.value = upgrades.map {
              val name = when(it.id) {
                "reactor" -> "Núcleo de Descarga"
                "uv" -> "Lente Abisal UV"
                "shield" -> "Escudo Súper Pez"
                "sonar" -> "Sonar de Cardumen"
                "stabilizer" -> "Estabilizador AR"
                else -> it.id
              }
              UpgradeState(it.id, name, it.level)
            }
          }
        }
      }
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
            _coopState.update { it.copy(partnerHullPercent = msg.newHullPercent) }
          }
          is MultiplayerMessage.MatchStart -> {
            // Sincronizar inicio de partida y semilla compartida
            matchRandom = Random(msg.initialSeed)
            if (msg.mode == "PVP") {
              _pvpState.update { it.copy(opponentReady = true) }
              checkStartConditions("PVP")
            } else if (msg.mode == "COOP") {
              _coopState.update { it.copy(partnerReady = true) }
              checkStartConditions("COOP")
            }
          }
          is MultiplayerMessage.PlayerReady -> {
            if (pendingGameStartMode == "PVP") {
              _pvpState.update { it.copy(opponentUsername = msg.username, opponentReady = true) }
              checkStartConditions("PVP")
            } else if (pendingGameStartMode == "COOP") {
              _coopState.update { it.copy(partnerUsername = msg.username, partnerReady = true) }
              checkStartConditions("COOP")
            }
          }
          is MultiplayerMessage.PlayerDied -> {
            if (_pvpState.value.isMatchActive) {
              _pvpState.update { it.copy(opponentAlive = false) }
              _pvpState.update { it.copy(winnerMessage = "¡Tu rival ha sido eliminado! Sigue pescando para el récord.") }
            } else if (_coopState.value.isMissionActive) {
              _coopState.update { it.copy(partnerAlive = false, partnerHullPercent = 0, recentTeamEvent = "¡Tu compañero ha sido eliminado! El sumergible resiste por ti.") }
            }
          }
          is MultiplayerMessage.FinalMatchResults -> {
             // Registrar el resultado del oponente/compañero en nuestra BD local para sincronización de tablas
             viewModelScope.launch {
               val mode = if (_coopState.value.isMissionActive || _coopState.value.isFinished) "COOP" else "PVP"
               scoreRepository.saveScore(
                 username = msg.username,
                 score = msg.score,
                 captures = msg.captures,
                 bestSpecies = msg.bestFish,
                 maxCombo = 1,
                 durationSeconds = 360,
                 gameMode = mode
               )
             }
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
    updateMissionProgress("m2", 1) // Incrementar exploración de zonas
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

  private var wasDamagedInCurrentEncounter = false

  fun startEncounter(species: FishSpecies, forceHeading: Float? = null) {
    wasDamagedInCurrentEncounter = false
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
    wasDamagedInCurrentEncounter = true
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
      
      if (newHull <= 0) {
          multiplayerManager.sendMessage(MultiplayerMessage.CoopAction("PLAYER_ELIMINATED", 0, _coopState.value.myUsername))
      }
    } else if (isPvp) {
      _pvpState.update { it.copy(myScore = max(0, it.myScore - 50)) }
      multiplayerManager.sendMessage(MultiplayerMessage.ScoreUpdate(
        username = _tournamentState.value.username,
        score = _pvpState.value.myScore,
        fishesCaught = _pvpState.value.myFishesCaught,
        combo = _pvpState.value.myCombo
      ))
      
      if (newHull <= 0) {
          multiplayerManager.sendMessage(MultiplayerMessage.CoopAction("PLAYER_ELIMINATED", 0, _pvpState.value.myUsername))
      }
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
      _coopState.update { it.copy(iAmAlive = false, recentTeamEvent = "¡HAS SIDO ELIMINADO! Queda 1 sobreviviente.") }
      multiplayerManager.sendMessage(MultiplayerMessage.PlayerDied(_coopState.value.myUsername))
      "¡Has sido eliminado! Queda 1 sobreviviente.\n($message)"
    } else if (isPvp) {
      _pvpState.update { it.copy(iAmAlive = false, myScore = max(0, it.myScore - 150)) }
      multiplayerManager.sendMessage(MultiplayerMessage.PlayerDied(_pvpState.value.myUsername))
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
                val currentState = _gameState.value
                if (currentState == null || currentState.hullIntegrityPercent <= 0) {
                  break
                }
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

  // ================= TORNEO DE FERIA 3 MINUTOS =================
  fun startFairTournament(username: String) {
    updateMissionProgress("m4", 1) // Incrementar participación en la Feria
    val cleanName = username.trim().ifEmpty { "Pescador ${Random.nextInt(100, 999)}" }
    tournamentTimerJob?.cancel()
    _tournamentState.value = TournamentState(
      isActive = true,
      username = cleanName,
      remainingSeconds = 180, // 3:00 min
      score = 0,
      fishesCaught = 0,
      currentCombo = 0,
      maxCombo = 0,
      bestFishName = "-",
      isFinished = false
    )

    // Timer Loop: 3 minutes countdown
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

  fun initiatePvpHandshake(username: String) {
    pendingGameStartMode = "PVP"
    val myName = username.trim().ifBlank { "Jugador 1" }
    _pvpState.update { it.copy(
      myUsername = myName,
      isReady = true,
      opponentReady = false
    ) }
    multiplayerManager.sendMessage(MultiplayerMessage.PlayerReady(myName))
    checkStartConditions("PVP")

    // Fallback: if opponent doesn't respond in 1.5s, auto-start with AI rival so user never gets stuck
    viewModelScope.launch {
      delay(1500)
      val current = _pvpState.value
      if (!current.isMatchActive && current.isReady && !current.opponentReady) {
        _pvpState.update { it.copy(opponentUsername = "Capitán Rival (IA)", opponentReady = true) }
        realStartPvp()
      }
    }
  }

  private fun checkStartConditions(mode: String) {
    if (mode == "PVP") {
      val state = _pvpState.value
      if (state.isReady && state.opponentReady && !state.isMatchActive) {
        realStartPvp()
      }
    } else if (mode == "COOP") {
      val state = _coopState.value
      if (state.isReady && state.partnerReady && !state.isMissionActive) {
        realStartCoop()
      }
    }
  }

  private fun realStartPvp() {
    pvpTimerJob?.cancel()
    _pvpState.update { it.copy(isMatchActive = true, remainingSeconds = 180, iAmAlive = true, opponentAlive = true) }
    
    // El host sincroniza el inicio oficial
    val isHost = (multiplayerState.value as? MultiplayerState.Connected)?.isHost == true
    if (isHost) {
      multiplayerManager.sendMessage(MultiplayerMessage.MatchStart("PVP", Random.nextLong()))
    }

    pvpTimerJob = viewModelScope.launch {
      while (_pvpState.value.isMatchActive && _pvpState.value.remainingSeconds > 0) {
        delay(1000)
        _pvpState.update { state ->
          val next = state.remainingSeconds - 1
          if (next <= 0 || (!state.iAmAlive && !state.opponentAlive)) {
            state.copy(remainingSeconds = max(0, next), isMatchActive = false, isFinished = true)
          } else {
            state.copy(remainingSeconds = next)
          }
        }
      }
      finishPvpMatch()
    }
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

    // Enviar resultados finales al otro dispositivo para sincronización de tablas
    multiplayerManager.sendMessage(MultiplayerMessage.FinalMatchResults(state.myUsername, state.myScore, state.myFishesCaught, "Duelo 1v1"))

    viewModelScope.launch {
      if (state.myScore > 0 || state.myFishesCaught > 0) {
        repository.recordFairTournamentRun(
          username = state.myUsername,
          score = state.myScore,
          fishesCaught = state.myFishesCaught,
          bestFishName = "Duelo vs ${state.opponentUsername}",
          maxCombo = state.myCombo,
          durationSeconds = 360 - state.remainingSeconds,
          gameMode = "PVP"
        )
      }
      
      // Registrar también el resultado del oponente si lo tenemos sincronizado
      if (state.opponentScore > 0 || state.opponentFishes > 0) {
        repository.recordFairTournamentRun(
          username = state.opponentUsername,
          score = state.opponentScore,
          fishesCaught = state.opponentFishes,
          bestFishName = "Duelo vs ${state.myUsername}",
          maxCombo = state.opponentCombo,
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
  fun initiateCoopHandshake(username: String) {
    pendingGameStartMode = "COOP"
    val myName = username.trim().ifBlank { "Jugador Dúo" }
    _coopState.update { it.copy(
      myUsername = myName,
      isReady = true,
      partnerReady = false
    ) }
    multiplayerManager.sendMessage(MultiplayerMessage.PlayerReady(myName))
    checkStartConditions("COOP")

    // Fallback: if partner doesn't respond in 1.5s, auto-start with AI partner so user never gets stuck
    viewModelScope.launch {
      delay(1500)
      val current = _coopState.value
      if (!current.isMissionActive && current.isReady && !current.partnerReady) {
        _coopState.update { it.copy(partnerUsername = "Compañero Marino (IA)", partnerReady = true) }
        realStartCoop()
      }
    }
  }

  private fun realStartCoop() {
    coopTimerJob?.cancel()
    _coopState.update { it.copy(isMissionActive = true, remainingSeconds = 180, iAmAlive = true, partnerAlive = true, partnerHullPercent = 100) }
    
    val isHost = (multiplayerState.value as? MultiplayerState.Connected)?.isHost == true
    if (isHost) {
      multiplayerManager.sendMessage(MultiplayerMessage.MatchStart("COOP", Random.nextLong()))
    }

    coopTimerJob = viewModelScope.launch {
      while (_coopState.value.isMissionActive && _coopState.value.remainingSeconds > 0) {
        delay(1000)
        _coopState.update { state ->
          val next = state.remainingSeconds - 1
          if (next <= 0 || (!state.iAmAlive && !state.partnerAlive)) {
            state.copy(remainingSeconds = max(0, next), isMissionActive = false, isFinished = true)
          } else {
            state.copy(remainingSeconds = next)
          }
        }
      }
      finishCoopMatch()
    }
    spawnNextFishInMatch()
  }

  fun finishCoopMatch() {
    coopTimerJob?.cancel()
    val state = _coopState.value
    _coopState.update { it.copy(isMissionActive = false, isFinished = true) }

    multiplayerManager.sendMessage(MultiplayerMessage.FinalMatchResults("Equipo: ${state.myUsername}", state.teamScore, state.teamFishesCaught, "Misión Cooperativa"))

    viewModelScope.launch {
      if (state.teamScore > 0 || state.teamFishesCaught > 0) {
        repository.recordFairTournamentRun(
          username = "Dúo: ${state.myUsername} & ${state.partnerUsername}",
          score = state.teamScore,
          fishesCaught = state.teamFishesCaught,
          bestFishName = "Misión Cooperativa",
          maxCombo = 1,
          durationSeconds = 360 - state.remainingSeconds,
          gameMode = "COOP"
        )
      }
    }
  }

  fun spawnNextFishInMatch() {
    val availableFish = MarineDatabase.speciesList
    val randomFish = availableFish[matchRandom.nextInt(availableFish.size)]
    
    val isCoop = _coopState.value.isMissionActive
    val isPvp = _pvpState.value.isMatchActive
    
    if (isCoop) {
      val isHost = (multiplayerState.value as? MultiplayerState.Connected)?.isHost == true
      if (isHost) {
        val initialHeading = (Random.nextFloat() * 40f - 20f + (_gameState.value?.playerHeading ?: 0f) + 360f) % 360f
        multiplayerManager.sendMessage(MultiplayerMessage.SpawnFish(randomFish.id, initialHeading, 22f))
        startEncounter(randomFish, forceHeading = initialHeading)
      } else {
        // Enviar solicitud al host para que spawnee el siguiente pez
        multiplayerManager.sendMessage(MultiplayerMessage.CoopAction("REQUEST_NEXT_FISH", 0))
        _coopState.update { it.copy(recentTeamEvent = "Solicitando siguiente objetivo al host...") }
      }
    } else {
      // PvP o Solo: Spawns independientes o locales
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
        val maxHull = _gameState.value?.maxHullIntegrityPercent ?: 100
        val currentHull = _gameState.value?.hullIntegrityPercent ?: 100
        val newHull = min(maxHull, currentHull + 25)
        _coopState.update { it.copy(recentTeamEvent = "¡Casco reparado +25%!") }
        _gameState.update { it?.copy(hullIntegrityPercent = newHull) }
        multiplayerManager.sendMessage(MultiplayerMessage.CoopHullUpdate(newHull))
      }
    }
  }

  private fun handleIncomingCoopAction(actionType: String, value: Int, extra: String) {
    when (actionType) {
      "DUEL_START" -> {
        if (!_pvpState.value.isMatchActive) {
          initiatePvpHandshake(extra.ifEmpty { "Rival Celular 1" })
        }
      }
      "COOP_START" -> {
        if (!_coopState.value.isMissionActive) {
          initiateCoopHandshake("Compañero Marino")
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
      "REQUEST_NEXT_FISH" -> {
        val isHost = (multiplayerState.value as? MultiplayerState.Connected)?.isHost == true
        if (isHost) {
          spawnNextFishInMatch()
        }
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
            
            // Update Mission Progress
            updateMissionProgress("m1", 1) // Incrementar captura de peces
            if (!wasDamagedInCurrentEncounter) {
                updateMissionProgress("m3", 1) // Incrementar encuentros sin ser visto
            }
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

          multiplayerManager.sendMessage(
            MultiplayerMessage.ScoreUpdate(
              username = pvp.myUsername,
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

  fun claimMission(missionId: String) {
    val mission = _allMissions.value.find { it.id == missionId } ?: return
    if (mission.isCompleted && !mission.isClaimed) {
      addPescacoins(mission.reward)
      MarineSoundEngine.playSuccessChime()
      viewModelScope.launch {
        repository.claimMission(missionId)
      }
    }
  }

  fun updateMissionProgress(missionId: String, delta: Int) {
    val mission = _allMissions.value.find { it.id == missionId } ?: return
    if (mission.isCompleted) return
    
    val newProgress = min(mission.target, mission.progress + delta)
    val isCompleted = newProgress >= mission.target
    
    viewModelScope.launch {
      repository.updateMissionProgress(missionId, newProgress, isCompleted)
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
