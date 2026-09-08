package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FishSpecies
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MarineGold
import com.example.ui.theme.MarineGreen
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.RealLocationData
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Maritime Google Maps overlay designed to render over the live AR camera viewport.
 * Provides real-time user GPS tracking, heading cone, bathymetric depth contours,
 * nautical grid coordinates, and target creature distance indicators.
 */
@Composable
fun ArMaritimeGoogleMapsOverlay(
  userLocation: RealLocationData,
  playerHeading: Float,
  creatureHeading: Float,
  creatureDistanceMeters: Float,
  currentSpecies: FishSpecies,
  isExpanded: Boolean,
  onToggleExpand: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isSatelliteMode by remember { mutableStateOf(true) }
  var zoomFactor by remember { mutableFloatStateOf(1.0f) }

  val pulseAnim = remember { Animatable(0f) }
  val sweepAnim = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    pulseAnim.animateTo(
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 2400, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
      )
    )
  }

  LaunchedEffect(Unit) {
    sweepAnim.animateTo(
      targetValue = 360f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 3200, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
      )
    )
  }

  val containerModifier = if (isExpanded) {
    modifier
      .fillMaxWidth(0.92f)
      .height(380.dp)
  } else {
    modifier
      .size(160.dp)
  }

  Box(
    modifier = containerModifier
      .clip(RoundedCornerShape(20.dp))
      .background(OceanAbyss.copy(alpha = 0.93f))
      .border(
        width = 1.8.dp,
        brush = Brush.verticalGradient(
          listOf(MarineCyan, MarineCyan.copy(alpha = 0.35f))
        ),
        shape = RoundedCornerShape(20.dp)
      )
      .clickable(enabled = !isExpanded) { onToggleExpand() }
      .testTag("ar_maritime_maps_overlay")
  ) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val widthPx = constraints.maxWidth.toFloat()
      val heightPx = constraints.maxHeight.toFloat()
      val center = Offset(widthPx / 2f, heightPx / 2f)
      val baseRadius = minOf(widthPx, heightPx) / 2f * 0.85f * zoomFactor

      // 1. Google Maps Nautical & Bathymetric Vector Canvas
      Canvas(modifier = Modifier.fillMaxSize()) {
        if (isSatelliteMode) {
          // Deep Abyss Radial Ocean
          drawRect(
            brush = Brush.radialGradient(
              colors = listOf(
                Color(0xFF0F3057),
                Color(0xFF081C30),
                Color(0xFF030D17)
              ),
              center = center,
              radius = baseRadius * 1.8f
            )
          )

          // Simulated Piura Coastline and Harbor contours
          val coast = Path().apply {
            moveTo(0f, 0f)
            lineTo(widthPx * 0.35f, 0f)
            cubicTo(
              widthPx * 0.40f, heightPx * 0.30f,
              widthPx * 0.32f, heightPx * 0.55f,
              widthPx * 0.48f, heightPx * 0.80f
            )
            cubicTo(
              widthPx * 0.52f, heightPx * 0.90f,
              widthPx * 0.44f, heightPx * 0.98f,
              widthPx * 0.46f, heightPx
            )
            lineTo(0f, heightPx)
            close()
          }
          // Coastal landmass
          drawPath(coast, color = Color(0xFF161A22))
          // Golden sandy shore
          drawPath(coast, color = Color(0xFFC7A76D).copy(alpha = 0.45f), style = Stroke(width = 7f))
          // White sea surf fringe
          drawPath(coast, color = Color(0xFFB3F5FF).copy(alpha = 0.35f), style = Stroke(width = 2.5f))

          // Bathymetric Depth Contours (-10m, -25m, -50m)
          val bathy1 = Path().apply {
            moveTo(widthPx * 0.42f, 0f)
            cubicTo(widthPx * 0.48f, heightPx * 0.35f, widthPx * 0.40f, heightPx * 0.65f, widthPx * 0.55f, heightPx)
          }
          val bathy2 = Path().apply {
            moveTo(widthPx * 0.56f, 0f)
            cubicTo(widthPx * 0.62f, heightPx * 0.40f, widthPx * 0.54f, heightPx * 0.70f, widthPx * 0.70f, heightPx)
          }
          drawPath(bathy1, color = Color(0xFF00E5FF).copy(alpha = 0.25f), style = Stroke(width = 1.6f))
          drawPath(bathy2, color = Color(0xFF0091EA).copy(alpha = 0.30f), style = Stroke(width = 1.6f))

        } else {
          // Tactical Nautical Blueprint Grid
          drawRect(color = OceanDeep)

          val coast = Path().apply {
            moveTo(0f, 0f)
            lineTo(widthPx * 0.35f, 0f)
            cubicTo(
              widthPx * 0.40f, heightPx * 0.30f,
              widthPx * 0.32f, heightPx * 0.55f,
              widthPx * 0.48f, heightPx * 0.80f
            )
            lineTo(widthPx * 0.46f, heightPx)
            lineTo(0f, heightPx)
            close()
          }
          drawPath(coast, color = Color(0xFF07243A))
          drawPath(coast, color = MarineCyan.copy(alpha = 0.6f), style = Stroke(width = 2.2f))
        }

        // Google Maps Tactical Coordinate Latitude/Longitude Grid
        val gridStep = (widthPx / 5f).coerceAtLeast(30f)
        var gx = gridStep
        while (gx < widthPx) {
          drawLine(
            color = MarineCyan.copy(alpha = 0.08f),
            start = Offset(gx, 0f),
            end = Offset(gx, heightPx),
            strokeWidth = 1f
          )
          gx += gridStep
        }
        var gy = gridStep
        while (gy < heightPx) {
          drawLine(
            color = MarineCyan.copy(alpha = 0.08f),
            start = Offset(0f, gy),
            end = Offset(widthPx, gy),
            strokeWidth = 1f
          )
          gy += gridStep
        }

        // Concentric Distance Range Rings (10m, 25m, 40m)
        val rings = listOf(0.33f, 0.66f, 1.0f)
        rings.forEach { fraction ->
          drawCircle(
            color = MarineCyan.copy(alpha = if (isSatelliteMode) 0.22f else 0.32f),
            radius = baseRadius * fraction,
            center = center,
            style = Stroke(width = 1.2f)
          )
        }

        // Radial Sonar Pulse Ping
        val pulseR = baseRadius * pulseAnim.value
        drawCircle(
          color = MarineCyan.copy(alpha = (1f - pulseAnim.value) * 0.45f),
          radius = pulseR,
          center = center,
          style = Stroke(width = 2f)
        )

        // Rotating Sonar Beam Sweep
        val sweepRad = Math.toRadians(sweepAnim.value.toDouble())
        val sweepX = center.x + (baseRadius * cos(sweepRad)).toFloat()
        val sweepY = center.y + (baseRadius * sin(sweepRad)).toFloat()
        drawLine(
          brush = Brush.linearGradient(
            colors = listOf(MarineCyan.copy(alpha = 0.7f), Color.Transparent),
            start = center,
            end = Offset(sweepX, sweepY)
          ),
          start = center,
          end = Offset(sweepX, sweepY),
          strokeWidth = 2.2f
        )

        // Draw Target Creature Bearing & Blip
        val relHeading = ((creatureHeading - playerHeading + 540f) % 360f) - 180f
        val creatureAngleRad = Math.toRadians((relHeading - 90f).toDouble())
        val distFraction = (creatureDistanceMeters / 45f).coerceIn(0.2f, 0.95f)
        val blipR = baseRadius * distFraction
        val blipX = center.x + (blipR * cos(creatureAngleRad)).toFloat()
        val blipY = center.y + (blipR * sin(creatureAngleRad)).toFloat()

        // Creature blip halo
        drawCircle(
          color = Color(0xFFFF3B30).copy(alpha = 0.4f),
          radius = 11f,
          center = Offset(blipX, blipY)
        )
        // Creature blip core
        drawCircle(
          color = Color(0xFFFF5252),
          radius = 5.5f,
          center = Offset(blipX, blipY)
        )

        // Vector line from user to creature
        drawLine(
          color = Color(0xFFFF5252).copy(alpha = 0.55f),
          start = center,
          end = Offset(blipX, blipY),
          strokeWidth = 1.4f
        )
      }

      // 2. User Center Real-Time GPS Pin & Orientation Cone
      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .size(if (isExpanded) 36.dp else 26.dp),
        contentAlignment = Alignment.Center
      ) {
        // Subtle outer pulse
        Box(
          modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(MarineCyan.copy(alpha = 0.22f))
            .border(1.2.dp, MarineCyan.copy(alpha = 0.8f), CircleShape)
        )
        // User direction pointer (pointing north relative to heading)
        Box(
          modifier = Modifier
            .size(if (isExpanded) 22.dp else 16.dp)
            .clip(CircleShape)
            .background(OceanAbyss)
            .border(1.5.dp, MarineGold, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Navigation,
            contentDescription = "Posición GPS del Usuario",
            tint = MarineGold,
            modifier = Modifier
              .size(if (isExpanded) 14.dp else 10.dp)
              .rotate(-45f)
          )
        }
      }

      // 3. Top Header Bar with Live GPS Data
      Row(
        modifier = Modifier
          .align(Alignment.TopStart)
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              listOf(OceanAbyss.copy(alpha = 0.95f), Color.Transparent)
            )
          )
          .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.GpsFixed,
            contentDescription = null,
            tint = if (userLocation.isRealGps) MarineGreen else MarineCyan,
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isExpanded) {
              "MAPA GPS • ${String.format(java.util.Locale.US, "%.4f", userLocation.latitude)}° S, ${String.format(java.util.Locale.US, "%.4f", userLocation.longitude)}° W"
            } else {
              "MAPA GPS"
            },
            color = MarineCyan,
            fontWeight = FontWeight.Bold,
            fontSize = if (isExpanded) 11.sp else 9.5.sp
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (isExpanded) {
            // Layer Toggle
            IconButton(
              onClick = { isSatelliteMode = !isSatelliteMode },
              modifier = Modifier.size(24.dp)
            ) {
              Icon(
                imageVector = if (isSatelliteMode) Icons.Default.Layers else Icons.Default.Map,
                contentDescription = "Modo de Mapa",
                tint = MarineCyan,
                modifier = Modifier.size(15.dp)
              )
            }

            // Zoom In
            IconButton(
              onClick = { zoomFactor = (zoomFactor + 0.2f).coerceAtMost(1.8f) },
              modifier = Modifier.size(24.dp)
            ) {
              Icon(Icons.Default.ZoomIn, contentDescription = "Acercar", tint = MarineCyan, modifier = Modifier.size(15.dp))
            }

            // Zoom Out
            IconButton(
              onClick = { zoomFactor = (zoomFactor - 0.2f).coerceAtLeast(0.7f) },
              modifier = Modifier.size(24.dp)
            ) {
              Icon(Icons.Default.ZoomOut, contentDescription = "Alejar", tint = MarineCyan, modifier = Modifier.size(15.dp))
            }

            // Close / Minimize
            IconButton(
              onClick = onToggleExpand,
              modifier = Modifier.size(24.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Minimizar", tint = TextSecondary, modifier = Modifier.size(15.dp))
            }
          } else {
            Text(
              text = "TAP PARA AMPLIAR",
              color = TextSecondary,
              fontSize = 7.5.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      // 4. Bottom Info Strip (Distance to Target & Species)
      Row(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .background(
            Brush.verticalGradient(
              listOf(Color.Transparent, OceanAbyss.copy(alpha = 0.95f))
            )
          )
          .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = currentSpecies.commonName.uppercase(),
          color = MarineGold,
          fontWeight = FontWeight.ExtraBold,
          fontSize = if (isExpanded) 10.sp else 8.sp
        )

        Text(
          text = "${creatureDistanceMeters.roundToInt()}m (${if (creatureDistanceMeters <= 15f) "ZONA LETAL" else "EN RANGO"})",
          color = if (creatureDistanceMeters <= 15f) Color(0xFFFF5252) else MarineCyan,
          fontWeight = FontWeight.Bold,
          fontSize = if (isExpanded) 10.sp else 8.sp
        )
      }
    }
  }
}
