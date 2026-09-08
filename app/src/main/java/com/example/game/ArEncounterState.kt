package com.example.game

import com.example.model.FishSpecies

/**
 * Behavior animation state for the 3D marine creature model
 */
enum class CreatureBehavior {
  SWIMMING_IDLE,    // Normal undulating swim
  STALKING_CIRCLING,// Circling around player, observing
  AMBUSH_PREPARE,   // Pausing, tense fin flutter before charging
  FRENZY_HAYWIRE,   // Erratic twitches, glitching, red optic glare
  CHARGING_FAST,    // High-speed charge directly at viewer
  FEINT_DISSOLVE,   // Decoy phantom dissolving into bubbles
  ELECTROCUTED      // Stunned by taser arc
}

sealed interface EncounterPhase {
  object Stalking : EncounterPhase
  // FNAF AR Haywire mechanic: creature glitches in front, player MUST look away!
  data class Haywire(val remainingSeconds: Float, val eyeGlowRed: Boolean = true) : EncounterPhase
  // FNAF AR Fake charge / amago: creature charges as phantom, dissolves; shocking it drains battery!
  data class FakeCharge(val distance: Float) : EncounterPhase
  // FNAF AR Real charge: creature charges for real with countdown timer to shock!
  data class RealCharge(
    val distance: Float,
    val initialDistance: Float = 28f,
    val timeRemainingSeconds: Float = 3.6f,
    val totalTimeSeconds: Float = 3.6f
  ) : EncounterPhase
  // Electric discharge arc animation state
  data class ShockFired(val success: Boolean, val message: String) : EncounterPhase
  // Tension reeling phase
  data class Reeling(val progress: Float, val targetZone: Float) : EncounterPhase
  // Win / Capture
  data class Success(val species: FishSpecies) : EncounterPhase
  // Defeat / Jumpscare splash
  data class Splashed(val message: String) : EncounterPhase
}

data class ArGameState(
  val currentSpecies: FishSpecies,
  val playerHeading: Float = 0f, // 0..360 horizontal degrees
  val playerPitch: Float = 0f,   // -90..+90 vertical tilt
  val creatureHeading: Float = 180f,
  val creaturePitch: Float = 0f,
  val creatureDistance: Float = 38f,
  val isFlashlightOn: Boolean = false,
  val batteryPercent: Int = 100,
  val hullIntegrityPercent: Int = 100,
  val staticInterference: Float = 0f, // 0..1 intensity
  val phase: EncounterPhase = EncounterPhase.Stalking,
  val isHaywireActive: Boolean = false,
  val haywireLookingWarning: Boolean = false,
  val isLookingAwaySafely: Boolean = false, // True when successfully averting eyes during haywire
  val haywireAvertedProgress: Float = 0f,   // 0..1 progress of holding look-away to neutralize frenzy
  val creatureBehavior: CreatureBehavior = CreatureBehavior.SWIMMING_IDLE,
  val chargeTimerProgress: Float = 1f,      // 1.0 down to 0.0 for impact countdown
  val superPezUsed: Boolean = false,
  val electricShockAnimation: Boolean = false,
  val caughtSpeciesHistory: Set<String> = emptySet(),
  val playerPescacoins: Int = 150
)
