package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Waves
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.MarineGameViewModel
import com.example.model.CoastalZone
import com.example.model.MarineBeacon
import com.example.ui.components.MarineRadarView
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MarineGold
import com.example.ui.theme.MarineGreen
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MapRadarScreen(
  viewModel: MarineGameViewModel,
  onOpenPescadex: () -> Unit,
  onOpenMiniGames: () -> Unit,
  onOpenFairDetails: () -> Unit,
  modifier: Modifier = Modifier
) {
  val zones by viewModel.zones.collectAsState()
  val selectedZone by viewModel.selectedZone.collectAsState()
  val pescacoins by viewModel.pescacoins.collectAsState()
  val pingRadius by viewModel.radarPingRadius.collectAsState()
  val isSatelliteMode by viewModel.isSatelliteMapMode.collectAsState()

  var selectedBeacon by remember { mutableStateOf<MarineBeacon?>(null) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(OceanDeep)
      .testTag("map_radar_screen")
  ) {
    // 1. FULL-SCREEN INTERACTIVE GOOGLE MAPS / NAUTICAL RADAR CANVAS (Edge-to-Edge)
    MarineRadarView(
      zone = selectedZone,
      beacons = selectedZone.initialBeacons,
      pingRadius = pingRadius,
      isSatelliteMapMode = isSatelliteMode,
      selectedBeaconId = selectedBeacon?.id,
      onBeaconSelected = { beacon ->
        selectedBeacon = beacon
      },
      modifier = Modifier.fillMaxSize()
    )

    // 2. TOP FLOATING GLASS HUD (Branding, Live GPS Coordinates, Pescacoins)
    Column(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
      // Top Navigation Capsule
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(OceanAbyss.copy(alpha = 0.92f))
          .border(1.5.dp, MarineCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
          .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Image(
            painter = painterResource(id = R.drawable.img_real_super_pez_3d),
            contentDescription = "PescActívate Logo",
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .border(1.5.dp, MarineGold, CircleShape)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "PescActívate AR",
              color = MarineCyan,
              fontWeight = FontWeight.Black,
              fontSize = 15.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.GpsFixed, contentDescription = null, tint = MarineGreen, modifier = Modifier.size(11.dp))
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "${String.format("%.4f", selectedZone.latitude)}° S, ${String.format("%.4f", selectedZone.longitude)}° W",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Pescacoins Chip
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(OceanCard)
              .border(1.dp, MarineGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = MarineGold, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "$pescacoins",
              color = MarineGold,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 12.sp
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Fair info
          IconButton(
            onClick = onOpenFairDetails,
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(OceanCard)
              .border(1.dp, MarineGold.copy(alpha = 0.5f), CircleShape)
              .testTag("fair_info_button")
          ) {
            Icon(Icons.Default.Celebration, contentDescription = "Feria", tint = MarineGold, modifier = Modifier.size(17.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Coastal Zone Floating Filter Pills
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(zones) { zone ->
          val isSelected = zone.id == selectedZone.id
          val zoneIcon = when (zone.id) {
            "san_josefina" -> Icons.Default.School
            "cabo_blanco" -> Icons.Default.Waves
            else -> Icons.Default.Anchor
          }

          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSelected) MarineCyan else OceanAbyss.copy(alpha = 0.88f))
              .border(
                1.2.dp,
                if (isSelected) MarineCyan else MarineCyan.copy(alpha = 0.35f),
                RoundedCornerShape(16.dp)
              )
              .clickable {
                viewModel.selectZone(zone)
                selectedBeacon = null
              }
              .padding(horizontal = 12.dp, vertical = 6.dp)
              .testTag("zone_${zone.id}"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = zoneIcon,
              contentDescription = null,
              tint = if (isSelected) OceanDeep else MarineCyan,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = zone.name,
              color = if (isSelected) OceanDeep else TextPrimary,
              fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
              fontSize = 11.sp
            )
          }
        }
      }
    }

    // 3. RIGHT FLOATING TACTICAL ACTION BUTTONS
    Column(
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .padding(end = 14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Toggle Google Maps Satelital / Nautical Grid
      IconButton(
        onClick = { viewModel.toggleMapMode() },
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(OceanAbyss.copy(alpha = 0.92f))
          .border(1.5.dp, if (isSatelliteMode) MarineCyan else MarineGreen, CircleShape)
          .testTag("toggle_map_mode_button")
      ) {
        Icon(
          imageVector = if (isSatelliteMode) Icons.Default.Layers else Icons.Default.Map,
          contentDescription = "Alternar Satélite",
          tint = if (isSatelliteMode) MarineCyan else MarineGreen,
          modifier = Modifier.size(22.dp)
        )
      }

      // Re-center GPS on Player
      IconButton(
        onClick = {
          // Centering pulse
          selectedBeacon = null
        },
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(OceanAbyss.copy(alpha = 0.92f))
          .border(1.5.dp, MarineCyan.copy(alpha = 0.5f), CircleShape)
          .testTag("center_gps_button")
      ) {
        Icon(
          imageVector = Icons.Default.MyLocation,
          contentDescription = "Centrar en mi ubicación GPS",
          tint = MarineCyan,
          modifier = Modifier.size(22.dp)
        )
      }

      // Ultrasonic Radar Sonar Pulse
      IconButton(
        onClick = {
          com.example.audio.MarineSoundEngine.playSonarPing()
        },
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(OceanAbyss.copy(alpha = 0.92f))
          .border(1.5.dp, MarineGold, CircleShape)
          .testTag("sonar_ping_button")
      ) {
        Icon(
          imageVector = Icons.Default.Sensors,
          contentDescription = "Pulso de Sonar",
          tint = MarineGold,
          modifier = Modifier.size(22.dp)
        )
      }
    }

    // 4. BOTTOM FLOATING TACTICAL DRAWER
    // Case A: When a creature beacon is selected -> Show Rich Tactical Encounter Briefing
    AnimatedVisibility(
      visible = selectedBeacon != null,
      enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
      exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      val beacon = selectedBeacon
      if (beacon != null) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("beacon_info_card"),
          colors = CardDefaults.cardColors(containerColor = OceanAbyss.copy(alpha = 0.95f)),
          shape = RoundedCornerShape(22.dp),
          border = androidx.compose.foundation.BorderStroke(1.8.dp, MarineCyan)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            // Header with Close Button
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(beacon.anomalyLevel.colorHex))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "ANOMALÍA MARINA DETECTADA",
                  color = Color(beacon.anomalyLevel.colorHex),
                  fontWeight = FontWeight.Black,
                  fontSize = 10.sp
                )
              }

              IconButton(
                onClick = { selectedBeacon = null },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary, modifier = Modifier.size(18.dp))
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Creature 3D Preview Row + Stats
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Photorealistic 3D Creature Thumbnail
              Box(
                modifier = Modifier
                  .size(76.dp)
                  .clip(RoundedCornerShape(16.dp))
                  .background(OceanCard)
                  .border(1.5.dp, Color(beacon.anomalyLevel.colorHex), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
              ) {
                Image(
                  painter = painterResource(id = beacon.species.imageRes),
                  contentDescription = beacon.species.commonName,
                  contentScale = ContentScale.Fit,
                  modifier = Modifier.size(70.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = beacon.species.commonName,
                  color = TextPrimary,
                  fontWeight = FontWeight.Black,
                  fontSize = 17.sp
                )
                Text(
                  text = beacon.species.scientificName,
                  color = MarineCyan,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Text(
                    text = "Distancia: ${beacon.distanceMeters}m",
                    color = MarineGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  )
                  Text(
                    text = "• ${beacon.species.rarity.label}",
                    color = Color(beacon.species.rarity.colorHex),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Habitat and Nutritional Value
            Text(
              text = beacon.description,
              color = TextSecondary,
              fontSize = 11.sp,
              maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Big Action Button: SUMERGIRSE AL ENCUENTRO AR
            Button(
              onClick = { viewModel.startEncounter(beacon.species) },
              colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanDeep),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("launch_encounter_button")
            ) {
              Icon(Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "¡SUMERGIRSE AL ENCUENTRO AR!",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
              )
            }
          }
        }
      }
    }

    // Case B: When no creature is selected -> Floating bottom navigation pills
    AnimatedVisibility(
      visible = selectedBeacon == null,
      enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
      exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(22.dp))
          .background(OceanAbyss.copy(alpha = 0.90f))
          .border(1.2.dp, MarineCyan.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
          .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Pescadex
        Button(
          onClick = onOpenPescadex,
          colors = ButtonDefaults.buttonColors(containerColor = OceanCard),
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, MarineCyan.copy(alpha = 0.5f)),
          modifier = Modifier.testTag("pescadex_nav_button")
        ) {
          Icon(Icons.Default.MenuBook, contentDescription = null, tint = MarineCyan, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Pescadex", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
        }

        // Juegos de Feria
        Button(
          onClick = onOpenMiniGames,
          colors = ButtonDefaults.buttonColors(containerColor = OceanCard),
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, MarineGold.copy(alpha = 0.5f)),
          modifier = Modifier.testTag("minigames_nav_button")
        ) {
          Icon(Icons.Default.SportsEsports, contentDescription = null, tint = MarineGold, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Juegos Feria", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
