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

import kotlinx.coroutines.launch
import androidx.lifecycle.findViewTreeLifecycleOwner

import androidx.lifecycle.lifecycleScope

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
  // We use the procedural canvas renderer which handles the physics, shading, and styling natively
  Fish3DCanvasRenderer(
    speciesId = species.id,
    swimCycle = swimCycle,
    distanceMeters = distanceMeters,
    isHaywireActive = isHaywireActive || behavior == CreatureBehavior.FRENZY_HAYWIRE,
    phase = if (behavior == CreatureBehavior.CHARGING_FAST) com.example.game.EncounterPhase.RealCharge(distanceMeters) else if (isDecoyCharge) com.example.game.EncounterPhase.FakeCharge(distanceMeters) else com.example.game.EncounterPhase.Stalking,
    isFlashlightOn = isFlashlightOn && isAimedAt,
    modifier = modifier.fillMaxSize()
  )
}
