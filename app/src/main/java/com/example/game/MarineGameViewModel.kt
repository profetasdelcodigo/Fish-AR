package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.MarineSoundEngine
import com.example.model.CoastalZone
import com.example.model.FishSpecies
import com.example.model.MarineBeacon
import com.example.model.PiuraCoastalZones
import com.example.model.PiuraMarineDatabase
import com.example.util.LocationAndOrientationHelper
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

class MarineGameViewModel(application: Application) : AndroidViewModel(application) {

  private val locationHelper = LocationAndOrientationHelper(application.applicationContext)

  // Real GPS & Sensor Flow
  val realLocation: StateFlow<RealLocationData> = locationHelper.currentLocation
  val deviceHeading: StateFlow<Float> = locationHelper.deviceHeading
  val devicePitch: StateFlow<Float> = locationHelper.devicePitch

  // Zones & Beacons
  private val _zones = MutableStateFlow(PiuraCoastalZones.defaultZones)
  val zones: StateFlow<List<CoastalZone>> = _zones.asStateFlow()

  private val _selectedZone = MutableStateFlow(PiuraCoastalZones.defaultZones.first())
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

  fun addPescacoins(amount: Int) {
    _pescacoins.update { max(0, it + amount) }
  }

  // Radar Ping Animation
  private val _radarPingRadius = MutableStateFlow(0f)
  val radarPingRadius: StateFlow<Float> = _radarPingRadius.asStateFlow()

  // Background loops
  private var encounterLoopJob: Job? = null
  private var batteryDrainJob: Job? = null
  private var aiDecisionTimer = 0
  private var haywireGraceTimer = 0f

  // Shock Cooldown
  private var isShockOnCooldown = false

  init {
    startRadarWaveLoop()
    locationHelper.requestRealLocationUpdate()
    locationHelper.startTrackingOrientation { h, p ->
      rotatePlayerAbsolute(h, p)
    }

    // React to real GPS updates by spawning local beacons around the user
    viewModelScope.launch {
      locationHelper.currentLocation.collect { loc ->
        if (loc.isRealGps) {
          spawnLocalBeaconsForRealGps(loc.latitude, loc.longitude)
        }
      }
    }
  }

  private fun spawnLocalBeaconsForRealGps(lat: Double, lng: Double) {
    val localBeacons = listOf(
      MarineBeacon(
        id = "gps_1",
        name = "Cardumen Cercano (GPS Local)",
        zoneName = "Tu Ubicación Actual",
        description = "¡Vibración detectada a pocos metros de tus coordenadas reales!",
        distanceMeters = 32,
        angleDegrees = 65f,
        latitude = lat + 0.00028,
        longitude = lng + 0.00032,
        species = PiuraMarineDatabase.speciesList[0],
        anomalyLevel = MarineBeacon.AnomalyLevel.LEVE
      ),
      MarineBeacon(
        id = "gps_2",
        name = "Anomalía Profunda (GPS Local)",
        zoneName = "Tu Ubicación Actual",
        description = "Turbulencia marina detectada a 55 metros de tu posición.",
        distanceMeters = 55,
        angleDegrees = 190f,
        latitude = lat - 0.00045,
        longitude = lng - 0.00020,
        species = PiuraMarineDatabase.speciesList[1],
        anomalyLevel = MarineBeacon.AnomalyLevel.ALTA
      ),
      MarineBeacon(
        id = "gps_3",
        name = "Señal Heroica Súper Pez",
        zoneName = "Tu Ubicación Actual",
        description = "¡Energía de nutrición y salud escolar emanando cerca!",
        distanceMeters = 72,
        angleDegrees = 310f,
        latitude = lat + 0.00050,
        longitude = lng - 0.00040,
        species = PiuraMarineDatabase.speciesList.last(),
        anomalyLevel = MarineBeacon.AnomalyLevel.HEROICA
      )
    )

    val updatedZone = _selectedZone.value.copy(
      latitude = lat,
      longitude = lng,
      name = "Mi Ubicación GPS",
      province = "Coordenadas Reales",
      initialBeacons = localBeacons
    )
    _selectedZone.value = updatedZone
  }

  fun toggleMapMode() {
    MarineSoundEngine.playNavClick()
    _isSatelliteMapMode.update { !it }
  }

  fun selectZone(zone: CoastalZone) {
    MarineSoundEngine.playNavClick()
    _selectedZone.value = zone
  }

  fun startEncounter(species: FishSpecies) {
    encounterLoopJob?.cancel()
    batteryDrainJob?.cancel()

    val initialHeading = (Random.nextFloat() * 360f)
    _gameState.value = ArGameState(
      currentSpecies = species,
      playerHeading = _gameState.value?.playerHeading ?: 0f,
      playerPitch = 0f,
      creatureHeading = initialHeading,
      creaturePitch = 0f,
      creatureDistance = 36f,
      isFlashlightOn = false,
      batteryPercent = 100,
      hullIntegrityPercent = 100,
      staticInterference = 0.15f,
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
    runEncounterLoop()
    runBatteryRoutine()
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

    // Looking at creature during Haywire is dangerous!
    val isLookingAtHaywire = state.isHaywireActive && (angleDiff < 38f && abs(newPitch - state.creaturePitch) < 32f)
    // Looking away safely: angle difference must be greater than 48 degrees
    val isLookingAway = state.isHaywireActive && (angleDiff >= 48f || abs(newPitch - state.creaturePitch) >= 38f)

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

  // Precise Electric Shock with capacitor cooldown & lethal zone
  fun fireElectricShock() {
    val state = _gameState.value ?: return
    if (state.batteryPercent < 12 || isShockOnCooldown) return

    isShockOnCooldown = true
    MarineSoundEngine.playElectricShock()

    _gameState.update {
      it?.copy(
        electricShockAnimation = true,
        creatureBehavior = CreatureBehavior.ELECTROCUTED,
        batteryPercent = max(0, it.batteryPercent - 14)
      )
    }

    viewModelScope.launch {
      delay(400)
      _gameState.update { it?.copy(electricShockAnimation = false) }
      delay(500)
      isShockOnCooldown = false
    }

    val phase = state.phase
    val angleDiff = calculateAngleDifference(state.playerHeading, state.creatureHeading)
    val isAimed = angleDiff < 42f

    if (phase is EncounterPhase.RealCharge && state.creatureDistance <= 15f && isAimed) {
      // Successful FNAF AR shock now opens a short capture/reel phase.
      // This keeps the high-pressure shock as the first step and adds a
      // Pokémon-like skill layer before the fish is actually registered.
      MarineSoundEngine.playSuccessChime()
      _gameState.update {
        it?.copy(
          phase = EncounterPhase.Reeling(progress = 0.10f, targetZone = 0.35f..0.65f),
          creatureBehavior = CreatureBehavior.ELECTROCUTED,
          creatureDistance = 10f,
          isFlashlightOn = false
        )
      }
    } else if (phase is EncounterPhase.FakeCharge) {
      // Shot during fake charge / decoy! Penalize battery
      _gameState.update {
        it?.copy(
          batteryPercent = max(0, it.batteryPercent - 10),
          creatureBehavior = CreatureBehavior.FEINT_DISSOLVE
        )
      }
    } else if (state.isHaywireActive) {
      // Shocking during Haywire fails in FNAF AR!
      _gameState.update {
        it?.copy(
          batteryPercent = max(0, it.batteryPercent - 15),
          hullIntegrityPercent = max(0, it.hullIntegrityPercent - 20)
        )
      }
    }
  }

  fun activateSuperPezShield() {
    _gameState.update { state ->
      if (state == null || state.superPezUsed) state
      else {
        MarineSoundEngine.playShieldHum()
        state.copy(
          superPezUsed = true,
          hullIntegrityPercent = 100,
          batteryPercent = min(100, state.batteryPercent + 40),
          creatureDistance = 34f,
          phase = EncounterPhase.Stalking,
          isHaywireActive = false,
          isLookingAwaySafely = false,
          haywireAvertedProgress = 0f,
          creatureBehavior = CreatureBehavior.SWIMMING_IDLE
        )
      }
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

        when (val currentPhase = state.phase) {
          is EncounterPhase.Stalking -> {
            aiDecisionTimer++

            // Slowly orbit around player in world space
            val orbitStep = (Random.nextFloat() * 8f - 4f) * speed
            val nextHeading = (state.creatureHeading + orbitStep + 360f) % 360f

            // Distance creeps closer if unlit
            val distDelta = if (isIlluminated) -0.4f else 0.25f * speed
            val nextDist = (state.creatureDistance - distDelta).coerceIn(16f, 46f)

            // Dynamic behavior in stalking: circling vs ambush prep
            val stalkingBehavior = if (aiDecisionTimer > 15) CreatureBehavior.AMBUSH_PREPARE else CreatureBehavior.STALKING_CIRCLING

            // AI Event Trigger: Haywire (Frenesí), Fake Charge (Amago), or Real Charge
            if (aiDecisionTimer > 20) {
              aiDecisionTimer = 0
              val roll = Random.nextFloat()
              if (roll < 0.32f) {
                // Haywire Trigger
                MarineSoundEngine.playHaywireAlarm()
                haywireGraceTimer = 1.3f // 1.3 seconds grace period to look away!
                val haywireH = (state.playerHeading + (Random.nextFloat() * 20f - 10f) + 360f) % 360f
                _gameState.update {
                  it?.copy(
                    phase = EncounterPhase.Haywire(remainingSeconds = 4.2f),
                    isHaywireActive = true,
                    haywireLookingWarning = true,
                    isLookingAwaySafely = false,
                    haywireAvertedProgress = 0f,
                    creatureBehavior = CreatureBehavior.FRENZY_HAYWIRE,
                    creatureHeading = haywireH,
                    creatureDistance = 8.5f
                  )
                }
                continue
              } else if (roll < 0.65f) {
                // Fake Charge Trigger (Phantom Decoy)
                MarineSoundEngine.playDecoyWhoosh()
                _gameState.update {
                  it?.copy(
                    phase = EncounterPhase.FakeCharge(distance = 28f),
                    creatureDistance = 28f,
                    creatureBehavior = CreatureBehavior.CHARGING_FAST
                  )
                }
                continue
              } else if (nextDist <= 24f) {
                // Real Charge Trigger with countdown timer
                val totalChargeDuration = (28f / (1.05f * speed * 6.67f)).coerceIn(2.8f, 4.5f)
                MarineSoundEngine.playRealChargeApproach()
                _gameState.update {
                  it?.copy(
                    phase = EncounterPhase.RealCharge(
                      distance = 28f,
                      initialDistance = 28f,
                      timeRemainingSeconds = totalChargeDuration,
                      totalTimeSeconds = totalChargeDuration
                    ),
                    creatureDistance = 28f,
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
            val isLooking = angleDiff < 36f && abs(state.playerPitch - state.creaturePitch) < 30f
            val isAverted = !isLooking && (angleDiff >= 46f || abs(state.playerPitch - state.creaturePitch) >= 36f)

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
              MarineSoundEngine.playJumpscareSplash()
              _gameState.update {
                it?.copy(
                  phase = EncounterPhase.Splashed("¡Fallo de casco! El frenesí del ${state.currentSpecies.commonName} rompió el visor."),
                  isHaywireActive = false,
                  creatureBehavior = CreatureBehavior.CHARGING_FAST
                )
              }
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
                MarineSoundEngine.playJumpscareSplash()
                _gameState.update {
                  it?.copy(
                    phase = EncounterPhase.Splashed("¡Embestida directa del ${state.currentSpecies.commonName}!"),
                    hullIntegrityPercent = 0,
                    chargeTimerProgress = 0f
                  )
                }
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
            val decayedProgress = (currentPhase.progress - 0.025f).coerceAtLeast(0f)
            if (decayedProgress <= 0f) {
              MarineSoundEngine.playJumpscareSplash()
              _gameState.update {
                it?.copy(
                  phase = EncounterPhase.Splashed("¡El pez rompió el campo magnético y escapó a las profundidades!"),
                  creatureBehavior = CreatureBehavior.FEINT_DISSOLVE
                )
              }
              break
            } else {
              _gameState.update {
                it?.copy(phase = currentPhase.copy(progress = decayedProgress))
              }
            }
          }

          else -> {}
        }
      }
    }
  }

  private fun runBatteryRoutine() {
    batteryDrainJob = viewModelScope.launch {
      while (true) {
        delay(1000)
        _gameState.update { state ->
          if (state == null) null
          else if (state.phase is EncounterPhase.Reeling || state.phase is EncounterPhase.Success || state.phase is EncounterPhase.Splashed) {
            // Freeze expedition resources during the capture result screen so
            // the player is rewarded for the skill sequence rather than
            // losing battery while reading the outcome.
            state
          } else {
            val drain = if (state.isFlashlightOn) 2 else 1
            val newBat = max(0, state.batteryPercent - drain)
            val updatedFlashlight = if (newBat == 0) false else state.isFlashlightOn
            state.copy(batteryPercent = newBat, isFlashlightOn = updatedFlashlight)
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
      _pescacoins.update { it + 20 }
      // Randomize target zone for next catch
      val newStart = Random.nextFloat() * 0.5f + 0.1f
      _magneticTargetZone.value = newStart..(newStart + 0.28f)
    } else {
      MarineSoundEngine.playJumpscareSplash()
    }
    return isHit
  }

  fun advanceReelProgress(delta: Float) {
    val state = _gameState.value ?: return
    val phase = state.phase
    if (phase is EncounterPhase.Reeling) {
      val newProg = (phase.progress + delta).coerceIn(0f, 1f)
      if (newProg >= 1.0f) {
        MarineSoundEngine.playSuccessChime()
        val caught = state.currentSpecies
        _unlockedSpeciesIds.update { it + caught.id }
        _pescacoins.update { it + caught.energyRequired * 3 }
        _gameState.update { it?.copy(phase = EncounterPhase.Success(caught)) }
      } else if (newProg <= 0.05f) {
        MarineSoundEngine.playJumpscareSplash()
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
    miniGameJob?.cancel()
    MarineSoundEngine.stopStaticLoop()
  }
}
