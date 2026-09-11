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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.ArGameState
import com.example.game.EncounterPhase
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MarineGold
import com.example.ui.theme.MarineGreen
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanDeep
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun UnderwaterViewport3D(
  gameState: ArGameState,
  useCameraFeed: Boolean,
  onRotatePlayer: (Float, Float) -> Unit,
  modifier: Modifier = Modifier
) {
  val bubbleFloat = remember { Animatable(0f) }
  val creatureSwimAnim = remember { Animatable(0f) }
  val haywireGlitchAnim = remember { Animatable(0f) }
  val shieldPulseAnim = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    bubbleFloat.animateTo(
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 2800, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
      )
    )
  }

  LaunchedEffect(Unit) {
    creatureSwimAnim.animateTo(
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 1100, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
      )
    )
  }

  LaunchedEffect(gameState.isHaywireActive) {
    if (gameState.isHaywireActive) {
      haywireGlitchAnim.animateTo(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
          animation = tween(durationMillis = 70, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse
        )
      )
    } else {
      haywireGlitchAnim.snapTo(0f)
    }
  }

  LaunchedEffect(gameState.superPezUsed) {
    if (gameState.superPezUsed) {
      shieldPulseAnim.animateTo(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
          animation = tween(durationMillis = 1400, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse
        )
      )
    }
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .background(OceanDeep)
      .pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
          change.consume()
          // 360 look-around: horizontal drag rotates azimuth, vertical drag tilts pitch
          onRotatePlayer(-dragAmount.x * 0.32f, -dragAmount.y * 0.22f)
        }
      }
      .testTag("underwater_viewport_3d")
  ) {
    val widthPx = constraints.maxWidth.toFloat()
    val heightPx = constraints.maxHeight.toFloat()

    // 1. Live AR Background Feed (Rear Camera or Abyssal Fallback)
    if (useCameraFeed) {
      CameraPreviewView(modifier = Modifier.fillMaxSize())
      val overlayAlpha = if (gameState.isFlashlightOn) 0.30f else 0.70f
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(OceanDeep.copy(alpha = overlayAlpha))
      )
    } else {
      Image(
        painter = painterResource(id = R.drawable.img_underwater_bg),
        contentDescription = "Fondo Marino",
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .fillMaxSize()
          .alpha(if (gameState.isFlashlightOn) 0.85f else 0.40f)
      )
    }

    // 2. Rising Bubbles & Depth Illumination
    Canvas(modifier = Modifier.fillMaxSize()) {
      val bubbleProg = bubbleFloat.value
      for (i in 0..14) {
        val seed = i * 53
        val xPos = (seed * 29 % size.width.toInt()).toFloat()
        val yOffset = ((seed * 37 % size.height.toInt()) - (bubbleProg * size.height))
        val yPos = if (yOffset < 0) size.height + yOffset else yOffset
        val bubbleRadius = (4f + (i % 4) * 3f)

        drawCircle(
          color = MarineCyan.copy(alpha = 0.28f),
          radius = bubbleRadius,
          center = Offset(xPos, yPos),
          style = Stroke(width = 1.2f)
        )
      }

      // Flashlight Realistic Cone Illumination
      if (gameState.isFlashlightOn) {
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xEEFFFFFF),
              Color(0x8800F0FF),
              Color(0x2200E5FF),
              Color.Transparent
            ),
            center = Offset(size.width / 2f, size.height / 2f),
            radius = size.width * 0.75f
          ),
          center = Offset(size.width / 2f, size.height / 2f),
          radius = size.width * 0.75f
        )
      }

      // Electric Shock Arc Lightning Discharge
      if (gameState.electricShockAnimation) {
        val shockPath = Path()
        val startY = size.height * 0.88f
        shockPath.moveTo(size.width * 0.5f, startY)
        var curX = size.width * 0.5f
        var curY = startY
        while (curY > size.height * 0.15f) {
          curX += (Random.nextFloat() * 140f - 70f)
          curY -= (Random.nextFloat() * 80f + 30f)
          shockPath.lineTo(curX, curY)
        }
        drawPath(path = shockPath, color = Color.White, style = Stroke(width = 7f))
        drawPath(path = shockPath, color = MarineCyan, style = Stroke(width = 16f))
      }

      // Súper Pez Hexagonal Forcefield Barrier Overlay
      if (gameState.superPezUsed) {
        val barrierRadius = size.width * 0.46f * (0.95f + shieldPulseAnim.value * 0.08f)
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              MarineGold.copy(alpha = 0.05f),
              MarineCyan.copy(alpha = 0.18f),
              MarineGold.copy(alpha = 0.45f)
            ),
            center = Offset(size.width / 2f, size.height / 2f),
            radius = barrierRadius
          ),
          radius = barrierRadius,
          center = Offset(size.width / 2f, size.height / 2f),
          style = Stroke(width = 4f)
        )
      }
    }

    // 3. 3D Mathematical Spatial Projection in World Coordinates
    // Fixed world bearing: when the player turns their device away, the creature moves off screen naturally!
    var relHeading = (gameState.creatureHeading - gameState.playerHeading) % 360f
    if (relHeading > 180f) relHeading -= 360f
    if (relHeading < -180f) relHeading += 360f

    val relPitch = (gameState.playerPitch - gameState.creaturePitch)

    // Camera FOV in degrees (~52 deg horizontal, ~70 deg vertical)
    val hFov = 52f
    val vFov = 70f

    val screenX = (relHeading / (hFov / 2f)) * (widthPx / 2f)
    val screenY = (relPitch / (vFov / 2f)) * (heightPx / 2f)

    val distance = gameState.creatureDistance.coerceIn(3f, 50f)
    // Scale from 50m (0.45x) up to 3m (2.4x)
    val depthScale = (2.2f - (distance / 50f) * 1.6f).coerceIn(0.45f, 2.4f)

    // Undulating 3D swim tilt
    val swimTilt = sin(creatureSwimAnim.value * 2 * PI.toFloat()) * 8f

    // Visibility alpha:
    val baseAlpha = when {
      gameState.isHaywireActive -> 1.0f
      gameState.phase is EncounterPhase.RealCharge -> 0.95f
      gameState.phase is EncounterPhase.FakeCharge -> 0.60f
      gameState.isFlashlightOn && abs(relHeading) < 25f -> 1.0f
      else -> (1f - (distance / 45f)).coerceIn(0.18f, 0.75f)
    }

    // Haywire glitch displacement
    val glitchOffset = if (gameState.isHaywireActive) {
      val gx = (Random.nextFloat() * 30f - 15f) * haywireGlitchAnim.value
      val gy = (Random.nextFloat() * 30f - 15f) * haywireGlitchAnim.value
      IntOffset(gx.roundToInt(), gy.roundToInt())
    } else {
      IntOffset.Zero
    }

    // Creature is in viewport if within visible angular range (+- 45 degrees)
    val isCreatureInViewport = abs(relHeading) < 42f && abs(relPitch) < 48f

    if (isCreatureInViewport) {
      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .offset {
            IntOffset(
              screenX.roundToInt() + glitchOffset.x,
              screenY.roundToInt() + glitchOffset.y
            )
          }
          .scale(depthScale)
          .rotate(swimTilt)
          .alpha(baseAlpha)
      ) {
        // Realistic 3D Fish with Photorealistic Renders & FNAF AR Optical Effects
        RealisticFish3DRenderer(
          species = gameState.currentSpecies,
          distanceMeters = distance,
          swimCycle = creatureSwimAnim.value,
          isHaywireActive = gameState.isHaywireActive,
          isFlashlightOn = gameState.isFlashlightOn,
          isAimedAt = abs(relHeading) < 20f && abs(relPitch) < 20f,
          isShocked = gameState.electricShockAnimation,
          isDecoyCharge = gameState.phase is EncounterPhase.FakeCharge,
          behavior = gameState.creatureBehavior,
          modifier = Modifier.size(310.dp)
        )
      }
    }

    // 4. Directional Sonar Chevron Indicators (When creature is off-screen)
    if (!isCreatureInViewport && !gameState.isHaywireActive) {
      if (relHeading > 25f) {
        // Creature is to the right!
        Row(
          modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(OceanAbyss.copy(alpha = 0.82f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "${distance.roundToInt()}m", color = MarineCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.width(4.dp))
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MarineCyan, modifier = Modifier.size(18.dp))
        }
      } else if (relHeading < -25f) {
        // Creature is to the left!
        Row(
          modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(OceanAbyss.copy(alpha = 0.82f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MarineCyan, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "${distance.roundToInt()}m", color = MarineCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // 5. Tactical State Overlays: Real Charge Timer / Haywire Desvío Gauge / Amago
    if (gameState.phase is EncounterPhase.RealCharge) {
      val realCharge = gameState.phase
      androidx.compose.foundation.layout.Column(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 80.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(OceanAbyss.copy(alpha = 0.90f))
          .border(2.dp, Color(0xFFFF2A2A), RoundedCornerShape(16.dp))
          .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "¡EMBESTIDA EN CURSO! TIEMPO DE IMPACTO",
          color = Color(0xFFFF4545),
          fontWeight = FontWeight.Black,
          fontSize = 13.sp
        )
        Spacer(modifier = Modifier.size(6.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = String.format(java.util.Locale.US, "%.1fs", realCharge.timeRemainingSeconds),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp
          )
          // Real-time Countdown Timer Bar
          Box(
            modifier = Modifier
              .width(140.dp)
              .height(10.dp)
              .clip(RoundedCornerShape(5.dp))
              .background(Color.DarkGray)
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .scale(scaleX = gameState.chargeTimerProgress, scaleY = 1f)
                .background(
                  Brush.horizontalGradient(
                    listOf(Color(0xFFFF1111), Color(0xFFFF9900))
                  )
                )
            )
          }
        }
        Text(
          text = if (gameState.creatureDistance <= 15f) "¡ZONA LETAL! ¡DESCARGA EL CHOQUE AHORA!" else "ESPERA A QUE SE ACERQUE A < 15 METROS",
          color = if (gameState.creatureDistance <= 15f) Color(0xFF00E5FF) else Color.LightGray,
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp
        )
      }
    }

    if (gameState.phase is EncounterPhase.FakeCharge) {
      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .padding(bottom = 120.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(MarineGold.copy(alpha = 0.35f))
          .border(1.2.dp, MarineGold, RoundedCornerShape(12.dp))
          .padding(horizontal = 14.dp, vertical = 8.dp)
      ) {
        Text(
          text = "¡AMAGO FANTASMA DETECTADO! ¡NO DISPARES!",
          color = MarineGold,
          fontWeight = FontWeight.Black,
          fontSize = 13.sp
        )
      }
    }

    if (gameState.isHaywireActive) {
      // Read the same flag the ViewModel used to apply hull damage / build
      // neutralization progress, instead of recomputing it here with our own
      // copy of the angle thresholds — that duplication is what used to make
      // the HUD say "you're safe" while the game still judged you as looking.
      val isLooking = gameState.haywireLookingWarning
      androidx.compose.foundation.layout.Column(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(top = 75.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(OceanAbyss.copy(alpha = 0.92f))
          .border(
            width = 1.8.dp,
            color = if (isLooking) Color(0xFFFF2222) else MarineGreen,
            shape = RoundedCornerShape(14.dp)
          )
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = if (isLooking) "¡DESVÍA LA MIRADA! GIRA EL DISPOSITIVO" else "¡DESVÍO ACTIVO! MANTÉN LA MIRADA FUERA",
          color = if (isLooking) Color(0xFFFF3333) else MarineGreen,
          fontWeight = FontWeight.Black,
          fontSize = 13.sp
        )

        Spacer(modifier = Modifier.size(6.dp))

        // Neutralization gauge when averting eyes
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "NEUTRALIZACIÓN:",
            color = Color.LightGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
          Box(
            modifier = Modifier
              .width(130.dp)
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0xFF222222))
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .scale(scaleX = gameState.haywireAvertedProgress, scaleY = 1f)
                .background(if (isLooking) Color.Red else MarineGreen)
            )
          }
          Text(
            text = "${(gameState.haywireAvertedProgress * 100).roundToInt()}%",
            color = if (isLooking) Color.Red else MarineGreen,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
          )
        }
      }
    }
  }
}
