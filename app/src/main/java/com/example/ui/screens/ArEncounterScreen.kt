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
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Healing
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
    }

    // 3. Tension Reeling Overlay
    val phase = gameState.phase
    if (phase is EncounterPhase.Reeling) {
      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .padding(16.dp)
      ) {
        MagneticReelMeter(
          progress = phase.progress,
          onReelTap = { viewModel.advanceReelProgress(0.14f) }
        )
      }
    }

    // 4. Tactical FNAF AR Control Panel (Flashlight, Shocker Taser, Super Pez Shield)
    if (phase !is EncounterPhase.Success && phase !is EncounterPhase.Splashed) {
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
          gameState.isHaywireActive && gameState.isLookingAwaySafely -> "¡DESVÍO EXITOSO! MANTÉN LA MIRADA LEJOS HASTA QUE SE CALME (${(gameState.haywireAvertedProgress * 100).roundToInt()}%)"
          gameState.isHaywireActive -> "¡FRENESÍ SUBMARINO! ¡DESVÍA LA MIRADA INMEDIATAMENTE PARA NO DAÑAR TU CASCO!"
          phase is EncounterPhase.RealCharge -> "¡EMBESTIDA REAL! IMPACTO EN ${(phase.timeRemainingSeconds * 10).roundToInt() / 10f}s • ¡DISPARA AL ESTAR A < 15M!"
          phase is EncounterPhase.FakeCharge -> "¡AMAGO FANTASMA! ¡NO DISPARES O PERDERÁS ENERGÍA!"
          gameState.isFlashlightOn -> "LINTERNA UV ACTIVA • ALUMBRA PARA LOCALIZAR SILUETAS Y DETENER EL ACECHO"
          else -> "RASTREA EL ORIGEN DE LA ESTÁTICA GIRANDO EN 360 GRADOS"
        }

        Text(
          text = tacticalNotice,
          color = when {
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
          IconButton(
            onClick = { viewModel.activateSuperPezShield() },
            enabled = !gameState.superPezUsed,
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(if (!gameState.superPezUsed) MarineGreen else OceanCard.copy(alpha = 0.4f))
              .border(2.dp, if (!gameState.superPezUsed) Color.White else Color.Gray, CircleShape)
              .testTag("super_pez_shield_button")
          ) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = "Escudo Protector Súper Pez",
              tint = if (!gameState.superPezUsed) OceanDeep else Color.Gray,
              modifier = Modifier.size(28.dp)
            )
          }
        }
      }
    }

    // 5. Success Dialog (Pure Cyber-Marine Aesthetic, Zero Emojis)
    if (phase is EncounterPhase.Success) {
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

    // 6. Splashed / Defeat Dialog (No Emojis, Clean Marine Design)
    if (phase is EncounterPhase.Splashed) {
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
              text = "Táctica FNAF AR: Cuando el pez entre en Frenesí (ojos rojos), desvía la mirada. Cuando embista físicamente hacia ti, activa la descarga eléctrica antes del impacto.",
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

              Button(
                onClick = { viewModel.startEncounter(gameState.currentSpecies) },
                colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanDeep),
                modifier = Modifier.weight(1f)
              ) {
                Text("REINTENTAR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }
        }
      }
    }
  }
}
