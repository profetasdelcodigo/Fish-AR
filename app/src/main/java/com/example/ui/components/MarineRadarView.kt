package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoastalZone
import com.example.model.MarineBeacon
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MarineGold
import com.example.ui.theme.MarineGreen
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun MarineRadarView(
  zone: CoastalZone,
  beacons: List<MarineBeacon>,
  pingRadius: Float,
  isSatelliteMapMode: Boolean,
  selectedBeaconId: String?,
  onBeaconSelected: (MarineBeacon) -> Unit,
  modifier: Modifier = Modifier
) {
  val sweepAngle = remember { Animatable(0f) }
  val pulseAnim = remember { Animatable(0f) }
  val waveTideAnim = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    sweepAngle.animateTo(
      targetValue = 360f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 3400, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
      )
    )
  }

  LaunchedEffect(Unit) {
    pulseAnim.animateTo(
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 2200, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
      )
    )
  }

  LaunchedEffect(Unit) {
    waveTideAnim.animateTo(
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 4000, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
      )
    )
  }

  BoxWithConstraints(
    modifier = modifier
      .background(if (isSatelliteMapMode) Color(0xFF061320) else OceanDeep)
      .testTag("marine_radar_view"),
    contentAlignment = Alignment.Center
  ) {
    val widthPx = constraints.maxWidth.toFloat()
    val heightPx = constraints.maxHeight.toFloat()
    val center = Offset(widthPx / 2f, heightPx / 2f)
    val maxRadius = minOf(widthPx, heightPx) / 2f * 0.88f

    // 1. Full-Screen Google Maps / Nautical Cartography Layer
    Canvas(modifier = Modifier.fillMaxSize()) {
      // Landmass & Coastline of Piura (Cabo Blanco, Paita, San Josefina)
      val coastlinePath = Path().apply {
        moveTo(0f, 0f)
        lineTo(widthPx * 0.42f, 0f)
        cubicTo(
          widthPx * 0.46f, heightPx * 0.25f,
          widthPx * 0.38f, heightPx * 0.52f,
          widthPx * 0.54f, heightPx * 0.72f
        )
        cubicTo(
          widthPx * 0.60f, heightPx * 0.85f,
          widthPx * 0.48f, heightPx * 0.95f,
          widthPx * 0.52f, heightPx
        )
        lineTo(0f, heightPx)
        close()
      }

      if (isSatelliteMapMode) {
        // Satellite Deep Ocean Gradient
        drawRect(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFF0F2B48), Color(0xFF071728), Color(0xFF030D17)),
            center = center,
            radius = maxRadius * 1.5f
          )
        )

        // Landmass / Coastal Soil & Sand
        drawPath(
          path = coastlinePath,
          color = Color(0xFF1C1F26)
        )

        // Golden Sand Shoreline Fringe
        drawPath(
          path = coastlinePath,
          color = Color(0xFFC2A675).copy(alpha = 0.35f),
          style = Stroke(width = 12f)
        )

        // Sea Foam / Wave surf animation lapping the beach
        val waveOffset = waveTideAnim.value * 14f
        drawPath(
          path = coastlinePath,
          color = Color(0xFF8CE8FF).copy(alpha = 0.28f),
          style = Stroke(width = 4f + waveOffset * 0.5f)
        )

        // Bathymetric Ocean Depth Contours (-10m, -30m, -60m, -120m)
        val bathy1 = Path().apply {
          moveTo(widthPx * 0.48f, 0f)
          cubicTo(widthPx * 0.54f, heightPx * 0.3f, widthPx * 0.48f, heightPx * 0.6f, widthPx * 0.64f, heightPx)
        }
        val bathy2 = Path().apply {
          moveTo(widthPx * 0.62f, 0f)
          cubicTo(widthPx * 0.70f, heightPx * 0.35f, widthPx * 0.62f, heightPx * 0.65f, widthPx * 0.78f, heightPx)
        }
        val bathy3 = Path().apply {
          moveTo(widthPx * 0.78f, 0f)
          cubicTo(widthPx * 0.86f, heightPx * 0.4f, widthPx * 0.78f, heightPx * 0.7f, widthPx * 0.92f, heightPx)
        }

        drawPath(bathy1, color = Color(0xFF00E5FF).copy(alpha = 0.22f), style = Stroke(width = 1.8f))
        drawPath(bathy2, color = Color(0xFF0091EA).copy(alpha = 0.25f), style = Stroke(width = 1.8f))
        drawPath(bathy3, color = Color(0xFF0D47A1).copy(alpha = 0.28f), style = Stroke(width = 1.8f))

        // Google Maps Nautical Harbor Piers & Docks (Paita / Cabo Blanco Muelle)
        val pierPath = Path().apply {
          moveTo(widthPx * 0.42f, heightPx * 0.45f)
          lineTo(widthPx * 0.58f, heightPx * 0.45f)
          lineTo(widthPx * 0.58f, heightPx * 0.47f)
          lineTo(widthPx * 0.42f, heightPx * 0.47f)
        }
        drawPath(pierPath, color = Color(0xFFD4AF37).copy(alpha = 0.7f), style = Fill)

        // Coastal Highway (Panamericana Norte / Malecón)
        val road1 = Path().apply {
          moveTo(widthPx * 0.25f, 0f)
          cubicTo(widthPx * 0.28f, heightPx * 0.35f, widthPx * 0.22f, heightPx * 0.7f, widthPx * 0.32f, heightPx)
        }
        drawPath(road1, color = Color(0xFF37474F), style = Stroke(width = 4.5f))
        drawPath(road1, color = Color(0xFFFFD54F).copy(alpha = 0.4f), style = Stroke(width = 1.2f))

      } else {
        // Tactical Nautical Vector Grid
        drawRect(color = OceanDeep)

        // Landmass outline in nautical blueprint green-cyan
        drawPath(path = coastlinePath, color = Color(0xFF062235))
        drawPath(path = coastlinePath, color = MarineCyan.copy(alpha = 0.5f), style = Stroke(width = 2.5f))

        // Pier & Port Infrastructure
        val pierPath = Path().apply {
          moveTo(widthPx * 0.42f, heightPx * 0.45f)
          lineTo(widthPx * 0.58f, heightPx * 0.45f)
        }
        drawPath(pierPath, color = MarineGold, style = Stroke(width = 3.5f))
      }

      // Latitude and Longitude Grid Lines (Google Maps Táctico)
      for (i in 1..6) {
        val x = (widthPx * i) / 7f
        drawLine(
          color = MarineCyan.copy(alpha = 0.07f),
          start = Offset(x, 0f),
          end = Offset(x, heightPx),
          strokeWidth = 1f
        )
      }
      for (j in 1..8) {
        val y = (heightPx * j) / 9f
        drawLine(
          color = MarineCyan.copy(alpha = 0.07f),
          start = Offset(0f, y),
          end = Offset(widthPx, y),
          strokeWidth = 1f
        )
      }

      // Concentric Range Rings (Pokémon GO 30m, 60m, 90m, 120m Radar Circles)
      val ringFractions = listOf(0.25f, 0.50f, 0.75f, 1.0f)
      ringFractions.forEach { fraction ->
        val ringR = maxRadius * fraction
        drawCircle(
          color = MarineCyan.copy(alpha = if (isSatelliteMapMode) 0.16f else 0.26f),
          radius = ringR,
          center = center,
          style = Stroke(width = 1.2f)
        )
      }

      // Cardinal Axis Lines with Navigation Ticks
      drawLine(
        color = MarineCyan.copy(alpha = 0.18f),
        start = Offset(center.x - maxRadius, center.y),
        end = Offset(center.x + maxRadius, center.y),
        strokeWidth = 1f
      )
      drawLine(
        color = MarineCyan.copy(alpha = 0.18f),
        start = Offset(center.x, center.y - maxRadius),
        end = Offset(center.x, center.y + maxRadius),
        strokeWidth = 1f
      )

      // Pokémon GO style expanding sonar radar pulse wave
      val pulseR = maxRadius * pulseAnim.value
      drawCircle(
        color = MarineCyan.copy(alpha = (1f - pulseAnim.value) * 0.40f),
        radius = pulseR,
        center = center,
        style = Stroke(width = 2.5f)
      )

      // Rotating Sonar Beam Sweep (Submarine tactical radar)
      val sweepRad = Math.toRadians(sweepAngle.value.toDouble())
      val sweepX = center.x + (maxRadius * cos(sweepRad)).toFloat()
      val sweepY = center.y + (maxRadius * sin(sweepRad)).toFloat()

      drawLine(
        brush = Brush.linearGradient(
          colors = listOf(MarineCyan.copy(alpha = 0.75f), Color.Transparent),
          start = center,
          end = Offset(sweepX, sweepY)
        ),
        start = center,
        end = Offset(sweepX, sweepY),
        strokeWidth = 2.5f
      )
    }

    // 2. Center Player GPS Avatar with Compass Orientation Cone
    Box(
      modifier = Modifier
        .size(54.dp)
        .testTag("player_gps_marker"),
      contentAlignment = Alignment.Center
    ) {
      // Pulsing outer sonar ring around player
      Box(
        modifier = Modifier
          .size(52.dp)
          .clip(CircleShape)
          .background(MarineCyan.copy(alpha = 0.18f))
          .border(1.5.dp, MarineCyan.copy(alpha = 0.6f), CircleShape)
      )

      // Center navigation arrow
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(OceanAbyss)
          .border(2.dp, MarineGold, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Navigation,
          contentDescription = "Posición GPS del Jugador",
          tint = MarineGold,
          modifier = Modifier
            .size(20.dp)
            .rotate(-30f)
        )
      }
    }

    // 3. Interactive Marine Creature Anomaly Nodes (Pokémon GO style with Realistic 3D Previews)
    beacons.forEach { beacon ->
      val normalizedDistance = (beacon.distanceMeters.toFloat() / 130f).coerceIn(0.25f, 0.88f)
      val angleRad = Math.toRadians((beacon.angleDegrees - 90f).toDouble())
      val radiusPx = (maxRadius * normalizedDistance)
      val offsetX = (radiusPx * cos(angleRad)).toFloat()
      val offsetY = (radiusPx * sin(angleRad)).toFloat()

      val isSelected = beacon.id == selectedBeaconId
      val beaconColor = Color(beacon.anomalyLevel.colorHex)

      Box(
        modifier = Modifier
          .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
          .clickable { onBeaconSelected(beacon) }
          .testTag("beacon_${beacon.id}"),
        contentAlignment = Alignment.Center
      ) {
        // Target lock-on reticle if selected
        if (isSelected) {
          Box(
            modifier = Modifier
              .size(68.dp)
              .clip(CircleShape)
              .border(2.dp, MarineGold, CircleShape)
          )
        }

        // Pin Container (Pill with Realistic 3D Thumbnail + Distance)
        Column(
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Circular Badge with 3D Creature Thumbnail
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(OceanAbyss)
              .border(2.dp, if (isSelected) MarineGold else beaconColor, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Image(
              painter = painterResource(id = beacon.species.imageRes),
              contentDescription = beacon.species.commonName,
              contentScale = ContentScale.Fit,
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
            )
          }

          Spacer(modifier = Modifier.height(3.dp))

          // Tag Pill with distance in meters
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(OceanCard.copy(alpha = 0.95f))
              .border(1.dp, if (isSelected) MarineGold else beaconColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "${beacon.distanceMeters}m",
              color = if (isSelected) MarineGold else beaconColor,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 10.sp
            )
          }
        }
      }
    }
  }
}
