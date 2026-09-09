package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.game.CreatureBehavior
import com.example.model.FishModelCatalog
import com.example.model.FishSpecies
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MarineGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * Encounter creature renderer.
 *
 * The OpenGL mesh is now the primary creature. The species illustration is only
 * retained as a very subtle visual fallback/detail layer; it no longer fakes the
 * fish as the main 3D object.
 */
@Composable
fun RealisticFish3DRenderer(
  species: FishSpecies,
  distanceMeters: Float,
  swimCycle: Float,
  isHaywireActive: Boolean,
  isFlashlightOn: Boolean,
  isAimedAt: Boolean,
  isShocked: Boolean,
  isDecoyCharge: Boolean = false,
  behavior: CreatureBehavior = CreatureBehavior.SWIMMING_IDLE,
  modifier: Modifier = Modifier
) {
  val density = androidx.compose.ui.platform.LocalDensity.current.density
  val swimPhase = swimCycle * 2f * PI.toFloat()
  val undulation = sin(swimPhase)
  val lateralSway = cos(swimPhase)

  val pitch3D = when (behavior) {
    CreatureBehavior.CHARGING_FAST -> 12f + undulation * 3f
    CreatureBehavior.AMBUSH_PREPARE -> -5f + sin(swimPhase * 3f) * 2f
    CreatureBehavior.FRENZY_HAYWIRE -> sin(swimPhase * 7f) * 12f
    CreatureBehavior.ELECTROCUTED -> sin(swimPhase * 15f) * 18f
    else -> undulation * 7f
  }
  val yaw3D = when (behavior) {
    CreatureBehavior.CHARGING_FAST -> lateralSway * 4f
    CreatureBehavior.STALKING_CIRCLING -> 22f + lateralSway * 8f
    CreatureBehavior.AMBUSH_PREPARE -> lateralSway * 3f
    CreatureBehavior.FRENZY_HAYWIRE -> cos(swimPhase * 11f) * 24f
    CreatureBehavior.ELECTROCUTED -> sin(swimPhase * 13f) * 15f
    else -> lateralSway * 12f
  }
  val roll3D = when (behavior) {
    CreatureBehavior.CHARGING_FAST -> undulation * 4f
    CreatureBehavior.STALKING_CIRCLING -> 14f + undulation * 5f
    CreatureBehavior.FRENZY_HAYWIRE -> sin(swimPhase * 9f) * 18f
    CreatureBehavior.ELECTROCUTED -> cos(swimPhase * 14f) * 28f
    else -> undulation * 9f
  }

  val jitterAnim = remember { Animatable(0f) }
  val sparkAnim = remember { Animatable(0f) }
  val chargeSurgeAnim = remember { Animatable(1f) }
  LaunchedEffect(isHaywireActive) {
    if (isHaywireActive) jitterAnim.animateTo(1f, infiniteRepeatable(tween(60, easing = LinearEasing), RepeatMode.Reverse)) else jitterAnim.snapTo(0f)
  }
  LaunchedEffect(behavior == CreatureBehavior.CHARGING_FAST) {
    if (behavior == CreatureBehavior.CHARGING_FAST) chargeSurgeAnim.animateTo(1.12f, infiniteRepeatable(tween(180, easing = LinearEasing), RepeatMode.Reverse)) else chargeSurgeAnim.snapTo(1f)
  }
  LaunchedEffect(isShocked) {
    if (isShocked) sparkAnim.animateTo(1f, infiniteRepeatable(tween(80, easing = LinearEasing), RepeatMode.Reverse)) else sparkAnim.snapTo(0f)
  }

  val haywireJitterX = if (isHaywireActive) Random.nextFloat() * 26f - 13f else 0f
  val haywireJitterY = if (isHaywireActive) Random.nextFloat() * 20f - 10f else 0f
  val depthHazeAlpha = (distanceMeters / 45f).coerceIn(0f, 0.65f)
  val flashlightSpecular = if (isFlashlightOn && isAimedAt) 0.95f else 0.25f
  val baseBoxSize = 310.dp
  val nativeModel = FishModelCatalog.forSpecies(species.id)

  Box(
    modifier = modifier
      .size(baseBoxSize)
      .offset { IntOffset(haywireJitterX.roundToInt(), haywireJitterY.roundToInt()) }
      .graphicsLayer {
        rotationX = pitch3D
        rotationY = yaw3D
        rotationZ = roll3D
        scaleX = chargeSurgeAnim.value
        scaleY = chargeSurgeAnim.value
        cameraDistance = 16f * density
      }
  ) {
    // The illustration remains only as a faint texture/detail support so the
    // 3D mesh remains visually dominant.
    Image(
      painter = painterResource(species.imageRes),
      contentDescription = species.commonName,
      contentScale = ContentScale.Fit,
      modifier = Modifier.fillMaxSize().alpha(
        when {
          behavior == CreatureBehavior.FEINT_DISSOLVE -> .08f
          isHaywireActive -> .14f
          else -> .10f
        }
      )
    )

    if (isHaywireActive) {
      Image(painter = painterResource(species.imageRes), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().offset(x = (-10).dp, y = 3.dp).alpha(.18f), colorFilter = ColorFilter.tint(Color(0xFFFF1744), BlendMode.Screen))
      Image(painter = painterResource(species.imageRes), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().offset(x = 10.dp, y = (-3).dp).alpha(.18f), colorFilter = ColorFilter.tint(Color(0xFF00E5FF), BlendMode.Screen))
    }

    if (nativeModel != null) {
      val context = LocalContext.current
      AndroidView(
        factory = { FishMeshSurfaceView(context).apply { setSpecies(species.id) } },
        update = {
          it.setMotion(
            swim = swimCycle,
            yaw = yaw3D,
            pitch = pitch3D,
            scale = nativeModel.scale,
            behavior = behavior.name
          )
        },
        modifier = Modifier.fillMaxSize().alpha(if (isFlashlightOn) .98f else .94f)
      )
    }

    Box(
      modifier = Modifier.fillMaxSize().clip(CircleShape).background(
        Brush.radialGradient(
          listOf(Color.Transparent, Color(0x00020B14), Color(0xFF021224).copy(alpha = depthHazeAlpha))
        )
      )
    )

    if (isFlashlightOn && isAimedAt) {
      Box(
        modifier = Modifier
          .size(baseBoxSize * .7f)
          .clip(CircleShape)
          .background(
            Brush.radialGradient(
              listOf(
                Color.White.copy(alpha = .45f * flashlightSpecular),
                MarineCyan.copy(alpha = .30f * flashlightSpecular),
                Color.Transparent
              )
            )
          )
      )
    }

    val eyeColor = when {
      isHaywireActive -> Color(0xFFFF1133)
      behavior == CreatureBehavior.CHARGING_FAST -> Color(0xFFFF4500)
      isDecoyCharge -> MarineGold
      behavior == CreatureBehavior.AMBUSH_PREPARE -> Color(0xFF00FFAA)
      else -> MarineCyan
    }
    val eyeGlowRadius = if (isHaywireActive || behavior == CreatureBehavior.CHARGING_FAST) 32.dp else 18.dp

    Box(modifier = Modifier.offset(x = (-38).dp, y = (-16).dp).size(eyeGlowRadius).clip(CircleShape).background(Brush.radialGradient(listOf(eyeColor, eyeColor.copy(alpha = .6f), Color.Transparent))), contentAlignment = Alignment.Center) {
      Box(modifier = Modifier.size(if (isHaywireActive || behavior == CreatureBehavior.CHARGING_FAST) 10.dp else 6.dp).clip(CircleShape).background(if (isHaywireActive) Color.White else Color(0xFF003344)))
    }
    Box(modifier = Modifier.offset(x = 18.dp, y = (-22).dp).size(eyeGlowRadius * .75f).clip(CircleShape).background(Brush.radialGradient(listOf(eyeColor.copy(alpha = .85f), eyeColor.copy(alpha = .4f), Color.Transparent))), contentAlignment = Alignment.Center) {
      Box(modifier = Modifier.size(if (isHaywireActive || behavior == CreatureBehavior.CHARGING_FAST) 8.dp else 5.dp).clip(CircleShape).background(if (isHaywireActive) Color.White else Color(0xFF003344)))
    }

    if (isShocked || behavior == CreatureBehavior.ELECTROCUTED) {
      Box(modifier = Modifier.fillMaxSize().border(4.dp, Brush.radialGradient(listOf(Color.White, MarineCyan, Color.Transparent)), CircleShape))
    }
  }
}
