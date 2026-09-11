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

import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

@Composable
fun MarineRadarView(
  zone: CoastalZone,
  beacons: List<MarineBeacon>,
  pingRadius: Float,
  isSatelliteMapMode: Boolean,
  selectedBeaconId: String?,
  playerHeading: Float = 0f,
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

    // 1. Real OSM Map Background Layer (Firmly Anchored to Zone Station)
    AndroidView(
      modifier = Modifier.fillMaxSize(),
      factory = { context ->
        Configuration.getInstance().userAgentValue = context.packageName
        Configuration.getInstance().load(context, android.preference.PreferenceManager.getDefaultSharedPreferences(context))
        MapView(context).apply {
          setTileSource(TileSourceFactory.MAPNIK)
          setMultiTouchControls(false) // Keep player position anchored to zone station
          isClickable = false
          controller.setZoom(15.0)
          controller.setCenter(GeoPoint(zone.latitude, zone.longitude))
          
          if (isSatelliteMapMode) {
            // High-contrast nautical radar styling
            val colorMatrix = android.graphics.ColorMatrix().apply {
              set(floatArrayOf(
                0.90f, 0f, 0f, 0f, -10f,
                0f, 0.95f, 0f, 0f, -5f,
                0f, 0f, 1.10f, 0f, 15f,
                0f, 0f, 0f, 1f, 0f
              ))
            }
            overlayManager.tilesOverlay.setColorFilter(android.graphics.ColorMatrixColorFilter(colorMatrix))
          } else {
            overlayManager.tilesOverlay.setColorFilter(null)
          }
        }
      },
      update = { mapView ->
        val targetCenter = GeoPoint(zone.latitude, zone.longitude)
        mapView.controller.setCenter(targetCenter)
        if (isSatelliteMapMode) {
          val colorMatrix = android.graphics.ColorMatrix().apply {
            set(floatArrayOf(
              0.90f, 0f, 0f, 0f, -10f,
              0f, 0.95f, 0f, 0f, -5f,
              0f, 0f, 1.10f, 0f, 15f,
              0f, 0f, 0f, 1f, 0f
            ))
          }
          mapView.overlayManager.tilesOverlay.setColorFilter(android.graphics.ColorMatrixColorFilter(colorMatrix))
        } else {
          mapView.overlayManager.tilesOverlay.setColorFilter(null)
        }
      }
    )

    // 2. Radar UI Overlay (Grid, Sweep, Rings)
    Canvas(modifier = Modifier.fillMaxSize()) {
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

      // Distance graduation ticks along north/south/east/west axes
      ringFractions.forEach { fraction ->
        val ringR = maxRadius * fraction
        drawLine(
          color = MarineCyan.copy(alpha = 0.45f),
          start = Offset(center.x - 5f, center.y - ringR),
          end = Offset(center.x + 5f, center.y - ringR),
          strokeWidth = 1.5f
        )
        drawLine(
          color = MarineCyan.copy(alpha = 0.45f),
          start = Offset(center.x + ringR, center.y - 5f),
          end = Offset(center.x + ringR, center.y + 5f),
          strokeWidth = 1.5f
        )
      }

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
            .rotate(playerHeading)
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
