package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.ArGameState
import com.example.game.EncounterPhase
import com.example.game.MarineGameViewModel
import com.example.ui.components.ArMaritimeGoogleMapsOverlay
import com.example.ui.components.MagneticReelMeter
import com.example.ui.components.UnderwaterViewport3D
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MarineGold
import com.example.ui.theme.MarineGreen
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun ArEncounterScreen(
  viewModel: MarineGameViewModel,
  gameState: ArGameState,
  modifier: Modifier = Modifier
) {
  // Rear camera feed enabled by default for true AR experience
  var useCameraFeed by remember { mutableStateOf(true) }
  var isMapOverlayVisible by remember { mutableStateOf(false) }
  var isMapOverlayExpanded by remember { mutableStateOf(false) }

  val realLocation by viewModel.realLocation.collectAsState()
  val tournamentState by viewModel.tournamentState.collectAsState()
  val pvpState by viewModel.pvpState.collectAsState()
  val coopState by viewModel.coopState.collectAsState()

  Box(
    modifier = modifier
      .fillMaxSize()
      .testTag("ar_encounter_screen")
  ) {
    // 1. Primary 3D AR Viewport with live camera feed & FNAF AR spatial mechanics
    UnderwaterViewport3D(
      gameState = gameState,
      useCameraFeed = useCameraFeed,
      onRotatePlayer = { deltaH, deltaP -> viewModel.rotatePlayer(deltaH, deltaP) },
      modifier = Modifier.fillMaxSize()
    )

    // 2. Tactical FNAF AR HUD Instrumentation
    Column(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Exit Radar Button
        IconButton(
          onClick = { viewModel.endEncounter() },
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(OceanAbyss.copy(alpha = 0.88f))
            .border(1.5.dp, MarineCyan, CircleShape)
            .testTag("exit_encounter_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Volver al Radar",
            tint = MarineCyan
          )
        }

        // Compass, Pitch & Distance Indicator
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(OceanAbyss.copy(alpha = 0.88f))
            .border(1.2.dp, MarineCyan.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Explore, contentDescription = null, tint = MarineCyan, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${gameState.playerHeading.roundToInt()}° • DISTANCIA: ${gameState.creatureDistance.roundToInt()}m",
            color = MarineCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Maritime Google Maps Overlay Toggle Button
          IconButton(
            onClick = { isMapOverlayVisible = !isMapOverlayVisible },
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(OceanAbyss.copy(alpha = 0.88f))
              .border(1.5.dp, if (isMapOverlayVisible) MarineGold else MarineCyan, CircleShape)
              .testTag("toggle_maritime_map_overlay_button")
          ) {
            Icon(
              imageVector = Icons.Default.Map,
              contentDescription = "Alternar Capa de Mapa Marítimo",
              tint = if (isMapOverlayVisible) MarineGold else MarineCyan,
              modifier = Modifier.size(20.dp)
            )
          }

          // Camera Feed Toggle (Live Rear Camera / Abyssal 3D)
          IconButton(
            onClick = { useCameraFeed = !useCameraFeed },
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(OceanAbyss.copy(alpha = 0.88f))
              .border(1.5.dp, if (useCameraFeed) MarineGreen else MarineCyan, CircleShape)
              .testTag("toggle_camera_mode_button")
          ) {
            Icon(
              imageVector = Icons.Default.CameraAlt,
              contentDescription = "Alternar Cámara Trasera",
              tint = if (useCameraFeed) MarineGreen else MarineCyan,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      // Battery, Hull Integrity & Sonar Interference Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(OceanAbyss.copy(alpha = 0.88f))
          .border(1.dp, MarineCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
          .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Battery status
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (gameState.batteryPercent > 20) Icons.Default.BatteryChargingFull else Icons.Default.BatteryAlert,
            contentDescription = null,
            tint = if (gameState.batteryPercent > 25) MarineGreen else Color.Red,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "BAT: ${gameState.batteryPercent}%",
            color = if (gameState.batteryPercent > 25) MarineGreen else Color.Red,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp
          )
        }

        // Hull integrity
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = if (gameState.hullIntegrityPercent > 40) MarineCyan else Color.Red,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "CASCO: ${gameState.hullIntegrityPercent}%",
            color = if (gameState.hullIntegrityPercent > 40) MarineCyan else Color.Red,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp
          )
        }

        // Sonar static indicator
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Sensors, contentDescription = null, tint = MarineGold, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          val staticPct = (gameState.staticInterference * 100).roundToInt()
          Text(
            text = "ESTÁTICA: $staticPct%",
            color = if (staticPct > 70) Color.Red else if (staticPct > 40) MarineGold else MarineCyan,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp
          )
        }
      }

      // Live Tournament / PvP / Co-op Active Banner
      if (tournamentState.isActive) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(OceanAbyss.copy(alpha = 0.92f))
            .border(1.dp, MarineGold, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          val min = tournamentState.remainingSeconds / 60
          val sec = tournamentState.remainingSeconds % 60
          Text(
            text = "🏆 ${tournamentState.username}",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp
          )
          Text(
            text = String.format(java.util.Locale.getDefault(), "⏱️ %02d:%02d", min, sec),
            color = if (tournamentState.remainingSeconds <= 30) Color(0xFFFF5252) else MarineGold,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp
          )
          Text(
            text = "${tournamentState.score} pts (x${tournamentState.currentCombo})",
            color = MarineCyan,
            fontWeight = FontWeight.Black,
            fontSize = 11.5.sp
          )
        }
      } else if (pvpState.opponentScore > 0 || pvpState.opponentFishes > 0) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(OceanAbyss.copy(alpha = 0.92f))
            .border(1.dp, MarineCyan, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "⚔️ VS ${pvpState.opponentUsername}",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp
          )
          Text(
            text = "Rival: ${pvpState.opponentScore} pts",
            color = MarineCyan,
            fontWeight = FontWeight.Black,
            fontSize = 11.5.sp
          )
        }
      } else if (coopState.teamScore > 0 || coopState.teamFishesCaught > 0) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(OceanAbyss.copy(alpha = 0.92f))
            .border(1.dp, MarineGreen, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "🤝 Equipo: ${coopState.teamScore} pts",
            color = MarineGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp
          )
          Text(
            text = "Capturas Dúo: ${coopState.teamFishesCaught}",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.5.sp
          )
        }
      }
    }

    // 2.1. Maritime Google Maps Real-Time GPS Overlay
    if (isMapOverlayVisible) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(
            top = if (isMapOverlayExpanded) 110.dp else 125.dp,
            end = 14.dp,
            start = if (isMapOverlayExpanded) 14.dp else 0.dp
          ),
        contentAlignment = if (isMapOverlayExpanded) Alignment.TopCenter else Alignment.TopEnd
      ) {
        ArMaritimeGoogleMapsOverlay(
          userLocation = realLocation,
          playerHeading = gameState.playerHeading,
          creatureHeading = gameState.creatureHeading,
          creatureDistanceMeters = gameState.creatureDistance,
          currentSpecies = gameState.currentSpecies,
          isExpanded = isMapOverlayExpanded,
          onToggleExpand = { isMapOverlayExpanded = !isMapOverlayExpanded },
          onClose = {
            isMapOverlayVisible = false
            isMapOverlayExpanded = false
          }
        )
      }
    }

    // 3. Tension Reeling Overlay (Single high-precision capture HUD)
    val phase = gameState.phase
    if (phase is EncounterPhase.Reeling) {
      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .fillMaxWidth()
          .padding(20.dp),
        contentAlignment = Alignment.Center
      ) {
        MagneticReelMeter(
          progress = phase.progress,
          needlePosition = gameState.reelNeedlePosition,
          targetZone = phase.targetZone,
          onReelTap = { viewModel.tapReelStabilizer() }
        )
      }
    } else if (phase !is EncounterPhase.Success && phase !is EncounterPhase.Splashed) {
      Column(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Tactical guidance message banner
        val tacticalNotice = when {
          gameState.isShieldActive -> "🛡️ ¡ESCUDO SÓNICO ACTIVO! (+35% CASCO Y PROTECCIÓN CONTRA ATAQUES)"
          gameState.isHaywireActive && gameState.isLookingAwaySafely -> "¡DESVÍO EXITOSO! MANTÉN LA MIRADA LEJOS HASTA QUE SE CALME (${(gameState.haywireAvertedProgress * 100).roundToInt()}%)"
          gameState.isHaywireActive -> "¡FRENESÍ SUBMARINO! ¡DESVÍA LA MIRADA INMEDIATAMENTE PARA NO DAÑAR TU CASCO!"
          phase is EncounterPhase.RealCharge -> "¡EMBESTIDA REAL! IMPACTO EN ${(phase.timeRemainingSeconds * 10).roundToInt() / 10f}s • ¡DISPARA CHOQUE!"
          phase is EncounterPhase.FakeCharge -> "¡AMAGO FANTASMA! ¡NO DISPARES O PERDERÁS ENERGÍA!"
          gameState.isFlashlightOn -> "LINTERNA UV ACTIVA • ALUMBRA DIRECTAMENTE AL PEZ PARA ATURDIRLO Y PESCARLO"
          else -> "ENFOCA EL PEZ Y DISPARA LA DESCARGA DE CHOQUE PARA INICIAR LA PESCA"
        }

        Text(
          text = tacticalNotice,
          color = when {
            gameState.isShieldActive -> MarineCyan
            gameState.isHaywireActive && gameState.isLookingAwaySafely -> MarineGreen
            gameState.isHaywireActive || phase is EncounterPhase.RealCharge -> Color(0xFFFF5252)
            else -> TextPrimary
          },
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .background(OceanAbyss.copy(alpha = 0.92f), RoundedCornerShape(8.dp))
            .border(
              1.dp,
              when {
                gameState.isShieldActive -> MarineCyan
                gameState.isHaywireActive && gameState.isLookingAwaySafely -> MarineGreen
                gameState.isHaywireActive || phase is EncounterPhase.RealCharge -> Color.Red
                else -> MarineCyan.copy(alpha = 0.4f)
              },
              RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // 1. UV Flashlight (FNAF AR Flashlight)
          IconButton(
            onClick = { viewModel.toggleFlashlight() },
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(if (gameState.isFlashlightOn) MarineGold else OceanCard)
              .border(2.dp, if (gameState.isFlashlightOn) Color.White else MarineCyan, CircleShape)
              .testTag("flashlight_toggle_button")
          ) {
            Icon(
              imageVector = if (gameState.isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
              contentDescription = "Linterna UV Submarina",
              tint = if (gameState.isFlashlightOn) OceanDeep else MarineCyan,
              modifier = Modifier.size(30.dp)
            )
          }

          // 2. FNAF AR Electromagnetic Shock Discharge Button (Taser)
          Button(
            onClick = { viewModel.fireElectricShock() },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (phase is EncounterPhase.RealCharge) Color(0xFFFF3B30) else MarineCyan,
              contentColor = OceanDeep
            ),
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
              .height(58.dp)
              .testTag("fire_electric_shock_button")
          ) {
            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (phase is EncounterPhase.RealCharge) "¡DESCARGA DE CHOQUE!" else "DESCARGA DE CHOQUE",
              fontWeight = FontWeight.Black,
              fontSize = 13.sp
            )
          }

          // 3. Súper Pez Emergency Shield
          val isShieldOnCooldown = gameState.shieldCooldownSeconds > 0
          IconButton(
            onClick = { viewModel.activateSuperPezShield() },
            enabled = !isShieldOnCooldown,
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(if (!isShieldOnCooldown) MarineGreen else OceanCard.copy(alpha = 0.4f))
              .border(2.dp, if (!isShieldOnCooldown) Color.White else Color.Gray, CircleShape)
              .testTag("super_pez_shield_button")
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Escudo Sónico Protector (Restaura +35% Casco y +30% Energía)",
                tint = if (!isShieldOnCooldown) OceanDeep else Color.Gray,
                modifier = Modifier.size(24.dp)
              )
              if (isShieldOnCooldown) {
                Text(
                  text = "${gameState.shieldCooldownSeconds}s",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.LightGray
                )
              }
            }
          }
        }
      }
    }

    // 5. Success Dialog (Pure Cyber-Marine Aesthetic, Zero Emojis)
    if (phase is EncounterPhase.Success) {
      val isMatchActive = tournamentState.isActive || pvpState.isMatchActive || coopState.isMissionActive

      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.85f))
          .padding(20.dp),
        contentAlignment = Alignment.Center
      ) {
        Card(
          colors = CardDefaults.cardColors(containerColor = OceanCard),
          shape = RoundedCornerShape(24.dp),
          border = androidx.compose.foundation.BorderStroke(2.dp, MarineGold),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("encounter_success_card")
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MarineGreen, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (phase.species.isSanctuaryProtected) "RESCATE MARINO COMPLETADO" else "CAPTURA DE ARRECIFE EXITOSA",
                color = MarineGold,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
              )
            }

            Image(
              painter = painterResource(id = phase.species.imageRes),
              contentDescription = phase.species.commonName,
              modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, MarineCyan, RoundedCornerShape(16.dp))
            )

            Text(
              text = phase.species.commonName,
              color = TextPrimary,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 18.sp
            )
            Text(
              text = phase.species.scientificName,
              color = MarineCyan,
              fontSize = 12.sp
            )

            Text(
              text = "\"${phase.species.quote}\"",
              color = MarineGold,
              fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
              fontSize = 12.sp,
              textAlign = TextAlign.Center
            )

            // Nutritional Value Card
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(OceanDeep)
                .padding(10.dp)
            ) {
              Text(
                text = "VALOR NUTRICIONAL (FERIA SAN JOSEFINA):",
                color = MarineCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
              phase.species.nutritionalBenefits.forEach { b ->
                Text(text = "• $b", color = TextSecondary, fontSize = 11.sp)
              }
              Spacer(modifier = Modifier.height(4.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Restaurant, contentDescription = null, tint = MarineGold, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Gastronomía: ${phase.species.traditionalRecipe}",
                  color = TextPrimary,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 11.sp
                )
              }
            }

            if (isMatchActive) {
              Button(
                onClick = { viewModel.spawnNextFishInMatch() },
                colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanDeep),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("collect_reward_button")
              ) {
                Text(
                  text = "⚡ ¡PESCAR SIGUIENTE PEZ! (PARTIDA ACTIVA)",
                  fontWeight = FontWeight.Black
                )
              }

              Button(
                onClick = { viewModel.endEncounter() },
                colors = ButtonDefaults.buttonColors(containerColor = OceanDeep),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = "SALIR DE LA PARTIDA",
                  color = TextSecondary,
                  fontSize = 12.sp
                )
              }
            } else {
              Button(
                onClick = { viewModel.endEncounter() },
                colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanDeep),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("collect_reward_button")
              ) {
                Text(
                  text = "REGISTRAR EN PESCADEX (+${phase.species.energyRequired * 4} PESCACOINS)",
                  fontWeight = FontWeight.Black
                )
              }
            }
          }
        }
      }
    }

    // 6. Splashed / Defeat Dialog (No Emojis, Clean Marine Design)
    if (phase is EncounterPhase.Splashed) {
      val isMatchActive = tournamentState.isActive || pvpState.isMatchActive || coopState.isMissionActive

      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.85f))
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Card(
          colors = CardDefaults.cardColors(containerColor = OceanCard),
          shape = RoundedCornerShape(24.dp),
          border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF5252)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(42.dp))

            Text(
              text = "CHAPUZÓN SUBMARINO",
              color = Color(0xFFFF5252),
              fontWeight = FontWeight.Black,
              fontSize = 18.sp
            )

            Text(
              text = phase.message,
              color = TextSecondary,
              textAlign = TextAlign.Center,
              fontSize = 13.sp
            )

            Text(
              text = "Táctica: Cuando el pez entre en Frenesí (ojos rojos), desvía la mirada. Para capturarlo, enfréntalo y activa la Descarga de Choque para jalar el sedal.",
              color = MarineGold,
              fontSize = 11.sp,
              textAlign = TextAlign.Center
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = { viewModel.endEncounter() },
                colors = ButtonDefaults.buttonColors(containerColor = OceanDeep),
                modifier = Modifier.weight(1f)
              ) {
                Text("RADAR", color = TextPrimary, fontSize = 12.sp)
              }

              val canContinue = !(coopState.isMissionActive && !coopState.iAmAlive) && !(pvpState.isMatchActive && !pvpState.iAmAlive)
              if (canContinue) {
                Button(
                  onClick = {
                    if (isMatchActive) {
                      viewModel.spawnNextFishInMatch()
                    } else {
                      viewModel.startEncounter(gameState.currentSpecies)
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanDeep),
                  modifier = Modifier.weight(1f)
                ) {
                  Text(if (isMatchActive) "SIGUIENTE PEZ" else "REINTENTAR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
              }
            }
          }
        }
      }
    }

    // 7. Match Finished Overlay (Summary)
    val isMatchFinished = (tournamentState.isFinished && tournamentState.isActive.let { false }) || 
                         (pvpState.isFinished && !pvpState.isMatchActive) || 
                         (coopState.isFinished && !coopState.isMissionActive)
    
    if (isMatchFinished) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.9f))
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Card(
          colors = CardDefaults.cardColors(containerColor = OceanCard),
          shape = RoundedCornerShape(28.dp),
          border = androidx.compose.foundation.BorderStroke(2.dp, MarineGold),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MarineGold, modifier = Modifier.size(54.dp))
            
            Text(
              text = "PARTIDA FINALIZADA",
              color = MarineGold,
              fontWeight = FontWeight.Black,
              fontSize = 22.sp
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
               val finalScore = when {
                 tournamentState.isFinished -> tournamentState.score
                 pvpState.isFinished -> pvpState.myScore
                 coopState.isFinished -> coopState.teamScore
                 else -> 0
               }
               val finalCaptures = when {
                 tournamentState.isFinished -> tournamentState.fishesCaught
                 pvpState.isFinished -> pvpState.myFishesCaught
                 coopState.isFinished -> coopState.teamFishesCaught
                 else -> 0
               }
               
               Text("PUNTUACIÓN FINAL", color = TextSecondary, fontSize = 12.sp)
               Text("$finalScore pts", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 32.sp)
               Text("Capturas logradas: $finalCaptures", color = MarineCyan, fontSize = 14.sp)
               
               if (pvpState.isFinished) {
                 Spacer(modifier = Modifier.height(8.dp))
                 Text(pvpState.winnerMessage ?: "Duelo concluido", color = MarineGold, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
               }
            }
            
            Button(
              onClick = { viewModel.endEncounter() },
              colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanAbyss),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
              Text("VOLVER AL CENTRO DE MODOS", fontWeight = FontWeight.Black)
            }
          }
        }
      }
    }
  }
}
