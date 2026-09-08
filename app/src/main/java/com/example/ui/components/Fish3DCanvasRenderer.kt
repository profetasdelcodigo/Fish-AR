package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.game.EncounterPhase
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural 3D Fish Geometry Engine.
 * Renders fully articulated, volumetric 3D marine creature meshes with realistic
 * sine-wave swimming physics, volumetric depth lighting, 3D fins, and bioluminescent eye optics.
 */
@Composable
fun Fish3DCanvasRenderer(
  speciesId: String,
  swimCycle: Float, // 0..1 swimming animation phase
  distanceMeters: Float,
  isHaywireActive: Boolean,
  phase: EncounterPhase,
  isFlashlightOn: Boolean,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    val canvasW = size.width
    val canvasH = size.height
    val center = Offset(canvasW / 2f, canvasH / 2f)

    // Dynamic wave phase along the fish spine
    val wavePhase = swimCycle * 2f * PI.toFloat()
    val tailWag = sin(wavePhase) * 22f
    val finFlutter = cos(wavePhase * 1.5f) * 16f

    // Shading palette based on species
    when (speciesId) {
      "bonito" -> draw3DBonito(center, canvasW, canvasH, tailWag, finFlutter, isHaywireActive, phase, isFlashlightOn)
      "mero_murike" -> draw3DMero(center, canvasW, canvasH, tailWag, finFlutter, isHaywireActive, phase, isFlashlightOn)
      "cabrilla" -> draw3DCabrilla(center, canvasW, canvasH, tailWag, finFlutter, isHaywireActive, phase, isFlashlightOn)
      "tortuga_nuro" -> draw3DTortuga(center, canvasW, canvasH, swimCycle, isHaywireActive, phase, isFlashlightOn)
      "super_pez" -> draw3DSuperPez(center, canvasW, canvasH, tailWag, finFlutter, isHaywireActive, phase, isFlashlightOn)
      else -> draw3DBonito(center, canvasW, canvasH, tailWag, finFlutter, isHaywireActive, phase, isFlashlightOn)
    }
  }
}

// -------------------------------------------------------------
// 1. BONITO DEL PACÍFICO (High-Speed Metallic Spindle Body)
// -------------------------------------------------------------
private fun DrawScope.draw3DBonito(
  center: Offset,
  w: Float,
  h: Float,
  tailWag: Float,
  finFlutter: Float,
  isHaywire: Boolean,
  phase: EncounterPhase,
  flashlight: Boolean
) {
  val baseColor = if (isHaywire) Color(0xFF8B0000) else Color(0xFF0F3D59)
  val bellyColor = if (isHaywire) Color(0xFFD32F2F) else Color(0xFFB0D5E5)
  val dorsalColor = if (isHaywire) Color(0xFF3B0000) else Color(0xFF071C2C)

  // 1. Caudal (Tail) Fin with 3D crescent shape
  val tailX = center.x - w * 0.38f + tailWag * 0.8f
  val tailY = center.y + tailWag * 0.4f
  val tailPath = Path().apply {
    moveTo(center.x - w * 0.28f, center.y)
    cubicTo(tailX - w * 0.05f, tailY - h * 0.22f, tailX - w * 0.12f, tailY - h * 0.3f, tailX - w * 0.16f, tailY - h * 0.32f)
    cubicTo(tailX - w * 0.1f, tailY, tailX - w * 0.1f, tailY, tailX - w * 0.16f, tailY + h * 0.32f)
    cubicTo(tailX - w * 0.12f, tailY + h * 0.3f, tailX - w * 0.05f, tailY + h * 0.22f, center.x - w * 0.28f, center.y)
    close()
  }
  drawPath(tailPath, dorsalColor)

  // 2. Dorsal Fin (Top Hydrofoil)
  val dorsalPath = Path().apply {
    moveTo(center.x - w * 0.12f, center.y - h * 0.18f)
    lineTo(center.x - w * 0.02f, center.y - h * 0.38f)
    lineTo(center.x + w * 0.08f, center.y - h * 0.2f)
    close()
  }
  drawPath(dorsalPath, dorsalColor)

  // 3. Volumetric Fusiform Body (3D elliptical cross section)
  val bodyPath = Path().apply {
    val snoutX = center.x + w * 0.42f
    val snoutY = center.y
    val midTopX = center.x + w * 0.05f
    val midTopY = center.y - h * 0.22f
    val midBotX = center.x + w * 0.05f
    val midBotY = center.y + h * 0.22f
    val peduncleX = center.x - w * 0.32f + tailWag * 0.3f
    val peduncleY = center.y

    moveTo(snoutX, snoutY)
    cubicTo(snoutX - w * 0.15f, snoutY - h * 0.18f, midTopX + w * 0.15f, midTopY, midTopX, midTopY)
    cubicTo(midTopX - w * 0.2f, midTopY, peduncleX + w * 0.1f, peduncleY - h * 0.06f, peduncleX, peduncleY)
    cubicTo(peduncleX + w * 0.1f, peduncleY + h * 0.06f, midBotX - w * 0.2f, midBotY, midBotX, midBotY)
    cubicTo(midBotX + w * 0.15f, midBotY, snoutX - w * 0.15f, snoutY + h * 0.15f, snoutX, snoutY)
    close()
  }

  // 3D Directional Lighting Gradient (sunlight from top)
  drawPath(
    path = bodyPath,
    brush = Brush.verticalGradient(
      colors = listOf(dorsalColor, baseColor, bellyColor),
      startY = center.y - h * 0.25f,
      endY = center.y + h * 0.25f
    )
  )

  // Lateral Speed Stripes (bonito signature)
  for (i in -2..2) {
    val yOff = i * h * 0.04f
    drawLine(
      color = Color(0x33FFFFFF),
      start = Offset(center.x - w * 0.2f, center.y + yOff),
      end = Offset(center.x + w * 0.18f, center.y + yOff + h * 0.02f),
      strokeWidth = 2.5f
    )
  }

  // 4. Pectoral Fin (3D wing flap)
  val pecPath = Path().apply {
    val pecBaseX = center.x + w * 0.16f
    val pecBaseY = center.y + h * 0.04f
    moveTo(pecBaseX, pecBaseY)
    lineTo(pecBaseX - w * 0.18f, pecBaseY + h * 0.16f + finFlutter)
    lineTo(pecBaseX - w * 0.08f, pecBaseY + h * 0.08f)
    close()
  }
  drawPath(pecPath, dorsalColor.copy(alpha = 0.85f))

  // 5. 3D Bioluminescent Eye
  draw3DEye(
    center = Offset(center.x + w * 0.28f, center.y - h * 0.04f),
    radius = w * 0.038f,
    isHaywire = isHaywire,
    phase = phase
  )

  // Haywire glitch sparks
  if (isHaywire) {
    drawGlitchAberration(center, w, h)
  }
}

// -------------------------------------------------------------
// 2. MERO MURIKE (Broad Heavy Reef Predator with Cavernous Head)
// -------------------------------------------------------------
private fun DrawScope.draw3DMero(
  center: Offset,
  w: Float,
  h: Float,
  tailWag: Float,
  finFlutter: Float,
  isHaywire: Boolean,
  phase: EncounterPhase,
  flashlight: Boolean
) {
  val baseColor = if (isHaywire) Color(0xFF6B1212) else Color(0xFF384533) // mottled olive reef
  val darkSpots = if (isHaywire) Color(0xFF220000) else Color(0xFF1E281C)
  val bellyColor = if (isHaywire) Color(0xFFA52A2A) else Color(0xFF7A8B74)

  // Broad Rounded Tail
  val tailX = center.x - w * 0.36f + tailWag
  val tailPath = Path().apply {
    moveTo(center.x - w * 0.24f, center.y)
    cubicTo(tailX, center.y - h * 0.28f, tailX - w * 0.08f, center.y - h * 0.25f, tailX - w * 0.1f, center.y)
    cubicTo(tailX - w * 0.08f, center.y + h * 0.25f, tailX, center.y + h * 0.28f, center.x - w * 0.24f, center.y)
    close()
  }
  drawPath(tailPath, darkSpots)

  // Spiny Dorsal Fin Crest
  val dorsalPath = Path().apply {
    moveTo(center.x - w * 0.16f, center.y - h * 0.22f)
    lineTo(center.x - w * 0.12f, center.y - h * 0.42f)
    lineTo(center.x - w * 0.04f, center.y - h * 0.36f)
    lineTo(center.x + w * 0.04f, center.y - h * 0.40f)
    lineTo(center.x + w * 0.12f, center.y - h * 0.26f)
    close()
  }
  drawPath(dorsalPath, darkSpots)

  // Massive Heavy Deep Body
  val bodyPath = Path().apply {
    val headX = center.x + w * 0.40f
    val headY = center.y + h * 0.04f // heavy jutting lower jaw
    val topX = center.x + w * 0.02f
    val topY = center.y - h * 0.26f
    val botX = center.x + w * 0.02f
    val botY = center.y + h * 0.28f
    val tailBaseX = center.x - w * 0.28f + tailWag * 0.4f

    moveTo(headX, headY)
    cubicTo(headX - w * 0.1f, topY, topX + w * 0.15f, topY, topX, topY)
    cubicTo(topX - w * 0.18f, topY, tailBaseX + w * 0.08f, center.y - h * 0.1f, tailBaseX, center.y)
    cubicTo(tailBaseX + w * 0.08f, center.y + h * 0.12f, botX - w * 0.18f, botY, botX, botY)
    cubicTo(botX + w * 0.18f, botY, headX - w * 0.05f, headY + h * 0.12f, headX, headY)
    close()
  }

  drawPath(
    path = bodyPath,
    brush = Brush.verticalGradient(
      colors = listOf(darkSpots, baseColor, bellyColor),
      startY = center.y - h * 0.3f,
      endY = center.y + h * 0.3f
    )
  )

  // Reef Spots Pattern
  for (i in -3..3) {
    for (j in -2..2) {
      val px = center.x + i * w * 0.08f
      val py = center.y + j * h * 0.09f
      drawCircle(
        color = darkSpots.copy(alpha = 0.5f),
        radius = (w * 0.022f),
        center = Offset(px, py)
      )
    }
  }

  // Broad Cavernous Jaw
  drawLine(
    color = darkSpots,
    start = Offset(center.x + w * 0.40f, center.y + h * 0.04f),
    end = Offset(center.x + w * 0.22f, center.y + h * 0.10f),
    strokeWidth = 4f
  )

  // Pectoral Fin
  drawCircle(
    color = darkSpots.copy(alpha = 0.8f),
    radius = w * 0.08f,
    center = Offset(center.x + w * 0.12f, center.y + h * 0.06f + finFlutter * 0.4f)
  )

  // Eye
  draw3DEye(
    center = Offset(center.x + w * 0.26f, center.y - h * 0.08f),
    radius = w * 0.046f,
    isHaywire = isHaywire,
    phase = phase
  )

  if (isHaywire) drawGlitchAberration(center, w, h)
}

// -------------------------------------------------------------
// 3. CABRILLA DE PEÑA (Rocky Perch with Lateral Banding)
// -------------------------------------------------------------
private fun DrawScope.draw3DCabrilla(
  center: Offset,
  w: Float,
  h: Float,
  tailWag: Float,
  finFlutter: Float,
  isHaywire: Boolean,
  phase: EncounterPhase,
  flashlight: Boolean
) {
  val baseColor = if (isHaywire) Color(0xFF7A1515) else Color(0xFF5C4033) // bronze rock
  val bandColor = if (isHaywire) Color(0xFF330000) else Color(0xFF2E1A11)
  val bellyColor = if (isHaywire) Color(0xFFB71C1C) else Color(0xFFC4A482)

  // Double V-notched tail
  val tailX = center.x - w * 0.36f + tailWag
  val tailPath = Path().apply {
    moveTo(center.x - w * 0.24f, center.y)
    lineTo(tailX - w * 0.06f, center.y - h * 0.24f)
    lineTo(tailX, center.y)
    lineTo(tailX - w * 0.06f, center.y + h * 0.24f)
    close()
  }
  drawPath(tailPath, bandColor)

  // Continuous Spiny Dorsal Ridge
  val dorsalPath = Path().apply {
    moveTo(center.x - w * 0.18f, center.y - h * 0.18f)
    lineTo(center.x - w * 0.08f, center.y - h * 0.32f)
    lineTo(center.x + w * 0.08f, center.y - h * 0.30f)
    lineTo(center.x + w * 0.18f, center.y - h * 0.18f)
    close()
  }
  drawPath(dorsalPath, bandColor)

  // Streamlined Perch Body
  val bodyPath = Path().apply {
    val snoutX = center.x + w * 0.40f
    moveTo(snoutX, center.y)
    cubicTo(snoutX - w * 0.12f, center.y - h * 0.20f, center.x, center.y - h * 0.22f, center.x - w * 0.05f, center.y - h * 0.22f)
    cubicTo(center.x - w * 0.20f, center.y - h * 0.20f, center.x - w * 0.28f + tailWag * 0.3f, center.y - h * 0.05f, center.x - w * 0.28f + tailWag * 0.3f, center.y)
    cubicTo(center.x - w * 0.28f + tailWag * 0.3f, center.y + h * 0.05f, center.x - w * 0.15f, center.y + h * 0.22f, center.x - w * 0.02f, center.y + h * 0.22f)
    cubicTo(center.x + w * 0.15f, center.y + h * 0.20f, snoutX - w * 0.08f, center.y + h * 0.10f, snoutX, center.y)
    close()
  }

  drawPath(
    path = bodyPath,
    brush = Brush.verticalGradient(
      colors = listOf(bandColor, baseColor, bellyColor),
      startY = center.y - h * 0.25f,
      endY = center.y + h * 0.25f
    )
  )

  // Dark vertical camouflage bands
  for (i in -2..2) {
    val bx = center.x + i * w * 0.09f
    drawLine(
      color = bandColor.copy(alpha = 0.65f),
      start = Offset(bx, center.y - h * 0.18f),
      end = Offset(bx - w * 0.03f, center.y + h * 0.18f),
      strokeWidth = 9f
    )
  }

  // Pectoral fin
  val pecPath = Path().apply {
    val px = center.x + w * 0.15f
    moveTo(px, center.y + h * 0.02f)
    lineTo(px - w * 0.14f, center.y + h * 0.14f + finFlutter)
    lineTo(px - w * 0.06f, center.y + h * 0.06f)
    close()
  }
  drawPath(pecPath, Color(0xFFD4AF37).copy(alpha = 0.8f)) // golden perch fin

  // Eye
  draw3DEye(
    center = Offset(center.x + w * 0.27f, center.y - h * 0.05f),
    radius = w * 0.040f,
    isHaywire = isHaywire,
    phase = phase
  )

  if (isHaywire) drawGlitchAberration(center, w, h)
}

// -------------------------------------------------------------
// 4. TORTUGA VERDE MARINA (3D Domed Carapace & Wing Flippers)
// -------------------------------------------------------------
private fun DrawScope.draw3DTortuga(
  center: Offset,
  w: Float,
  h: Float,
  swimCycle: Float,
  isHaywire: Boolean,
  phase: EncounterPhase,
  flashlight: Boolean
) {
  val shellColor = if (isHaywire) Color(0xFF4A0E17) else Color(0xFF1B4332)
  val scuteEdge = if (isHaywire) Color(0xFF800020) else Color(0xFF2D6A4F)
  val skinColor = if (isHaywire) Color(0xFF8B0000) else Color(0xFF52B788)

  // Flipper rowing cycle
  val flipperAngle = sin(swimCycle * 2 * PI.toFloat()) * 28f

  // 1. Posterior Hind Flippers
  drawCircle(
    color = skinColor,
    radius = w * 0.06f,
    center = Offset(center.x - w * 0.22f, center.y - h * 0.18f)
  )
  drawCircle(
    color = skinColor,
    radius = w * 0.06f,
    center = Offset(center.x - w * 0.22f, center.y + h * 0.18f)
  )

  // 2. Large Front Rowing Flippers (Wing-like Hydrodynamic Paddles)
  val leftFlipperPath = Path().apply {
    val fx = center.x + w * 0.12f
    val fy = center.y - h * 0.18f
    moveTo(fx, fy)
    cubicTo(fx + w * 0.08f, fy - h * 0.24f + flipperAngle * 2f, fx - w * 0.08f, fy - h * 0.38f + flipperAngle * 3f, fx - w * 0.22f, fy - h * 0.32f + flipperAngle * 2.5f)
    close()
  }
  val rightFlipperPath = Path().apply {
    val fx = center.x + w * 0.12f
    val fy = center.y + h * 0.18f
    moveTo(fx, fy)
    cubicTo(fx + w * 0.08f, fy + h * 0.24f - flipperAngle * 2f, fx - w * 0.08f, fy + h * 0.38f - flipperAngle * 3f, fx - w * 0.22f, fy + h * 0.32f - flipperAngle * 2.5f)
    close()
  }
  drawPath(leftFlipperPath, skinColor)
  drawPath(rightFlipperPath, skinColor)

  // 3. Articulated Head & Neck
  val headPath = Path().apply {
    val hx = center.x + w * 0.32f
    val hy = center.y
    moveTo(center.x + w * 0.16f, center.y - h * 0.08f)
    cubicTo(hx - w * 0.04f, hy - h * 0.10f, hx + w * 0.06f, hy - h * 0.06f, hx + w * 0.08f, hy)
    cubicTo(hx + w * 0.06f, hy + h * 0.06f, hx - w * 0.04f, hy + h * 0.10f, center.x + w * 0.16f, center.y + h * 0.08f)
    close()
  }
  drawPath(headPath, skinColor)

  // 4. 3D Domed Carapace Shell (Oval with Depth Shading)
  val shellPath = Path().apply {
    val sx = center.x
    val sy = center.y
    moveTo(sx + w * 0.22f, sy)
    cubicTo(sx + w * 0.20f, sy - h * 0.24f, sx - w * 0.20f, sy - h * 0.24f, sx - w * 0.24f, sy)
    cubicTo(sx - w * 0.20f, sy + h * 0.24f, sx + w * 0.20f, sy + h * 0.24f, sx + w * 0.22f, sy)
    close()
  }

  drawPath(
    path = shellPath,
    brush = Brush.radialGradient(
      colors = listOf(scuteEdge, shellColor, Color(0xFF081C15)),
      center = Offset(center.x, center.y),
      radius = w * 0.25f
    )
  )

  // Carapace Scute Pattern (geometric plates)
  for (k in -1..1) {
    val cx = center.x + k * w * 0.12f
    drawCircle(
      color = scuteEdge.copy(alpha = 0.8f),
      radius = w * 0.055f,
      center = Offset(cx, center.y),
      style = Stroke(width = 3f)
    )
  }

  // Beak / Eye
  draw3DEye(
    center = Offset(center.x + w * 0.35f, center.y - h * 0.04f),
    radius = w * 0.032f,
    isHaywire = isHaywire,
    phase = phase
  )

  if (isHaywire) drawGlitchAberration(center, w, h)
}

// -------------------------------------------------------------
// 5. SÚPER PEZ (Legendary Hero of Nutrition & Health)
// -------------------------------------------------------------
private fun DrawScope.draw3DSuperPez(
  center: Offset,
  w: Float,
  h: Float,
  tailWag: Float,
  finFlutter: Float,
  isHaywire: Boolean,
  phase: EncounterPhase,
  flashlight: Boolean
) {
  val heroGold = if (isHaywire) Color(0xFFFF2A2A) else Color(0xFFFFD700)
  val heroBlue = if (isHaywire) Color(0xFF400000) else Color(0xFF0066CC)
  val heroChest = if (isHaywire) Color(0xFFFF5252) else Color(0xFF00E5FF)

  // 1. Flowing Golden Hero Cape / Dorsal Crest
  val capeTailX = center.x - w * 0.42f + tailWag * 1.2f
  val capePath = Path().apply {
    moveTo(center.x + w * 0.06f, center.y - h * 0.18f)
    cubicTo(center.x - w * 0.1f, center.y - h * 0.42f, capeTailX, center.y - h * 0.38f, capeTailX - w * 0.08f, center.y - h * 0.15f)
    cubicTo(capeTailX, center.y, center.x - w * 0.18f, center.y - h * 0.06f, center.x + w * 0.06f, center.y - h * 0.18f)
    close()
  }
  drawPath(capePath, heroGold.copy(alpha = 0.9f))

  // Golden Caudal Fin
  val tailPath = Path().apply {
    val tx = center.x - w * 0.34f + tailWag
    moveTo(center.x - w * 0.22f, center.y)
    lineTo(tx - w * 0.12f, center.y - h * 0.28f)
    lineTo(tx - w * 0.04f, center.y)
    lineTo(tx - w * 0.12f, center.y + h * 0.28f)
    close()
  }
  drawPath(tailPath, heroGold)

  // 2. Muscular Heroic Body
  val bodyPath = Path().apply {
    val snoutX = center.x + w * 0.42f
    moveTo(snoutX, center.y)
    cubicTo(snoutX - w * 0.10f, center.y - h * 0.24f, center.x + w * 0.08f, center.y - h * 0.24f, center.x, center.y - h * 0.20f)
    cubicTo(center.x - w * 0.16f, center.y - h * 0.18f, center.x - w * 0.26f + tailWag * 0.3f, center.y - h * 0.06f, center.x - w * 0.26f + tailWag * 0.3f, center.y)
    cubicTo(center.x - w * 0.26f + tailWag * 0.3f, center.y + h * 0.06f, center.x - w * 0.16f, center.y + h * 0.20f, center.x, center.y + h * 0.20f)
    cubicTo(center.x + w * 0.12f, center.y + h * 0.22f, snoutX - w * 0.08f, center.y + h * 0.12f, snoutX, center.y)
    close()
  }

  drawPath(
    path = bodyPath,
    brush = Brush.radialGradient(
      colors = listOf(heroChest, heroBlue, Color(0xFF001A33)),
      center = Offset(center.x + w * 0.15f, center.y),
      radius = w * 0.35f
    )
  )

  // 3. Heroic S-Shield Insignia on Chest
  val shieldCenter = Offset(center.x + w * 0.12f, center.y)
  drawCircle(
    color = heroGold,
    radius = w * 0.06f,
    center = shieldCenter,
    style = Stroke(width = 4f)
  )
  drawCircle(
    color = Color.White,
    radius = w * 0.024f,
    center = shieldCenter
  )

  // Pectoral fin
  val pecPath = Path().apply {
    val px = center.x + w * 0.18f
    moveTo(px, center.y + h * 0.04f)
    lineTo(px - w * 0.15f, center.y + h * 0.18f + finFlutter)
    lineTo(px - w * 0.06f, center.y + h * 0.08f)
    close()
  }
  drawPath(pecPath, heroGold.copy(alpha = 0.85f))

  // Radiant Glowing Eye
  draw3DEye(
    center = Offset(center.x + w * 0.30f, center.y - h * 0.06f),
    radius = w * 0.045f,
    isHaywire = isHaywire,
    phase = phase
  )

  if (isHaywire) drawGlitchAberration(center, w, h)
}

// -------------------------------------------------------------
// 3D BIOLUMINESCENT EYE SPHERE SYSTEM
// -------------------------------------------------------------
private fun DrawScope.draw3DEye(
  center: Offset,
  radius: Float,
  isHaywire: Boolean,
  phase: EncounterPhase
) {
  val irisColor = when {
    isHaywire -> Color(0xFFFF0033)
    phase is EncounterPhase.RealCharge -> Color(0xFFFF3333)
    phase is EncounterPhase.FakeCharge -> Color(0xFFFFC107)
    else -> Color(0xFF00E5FF)
  }

  // Outer ambient bioluminescent halo
  drawCircle(
    color = irisColor.copy(alpha = 0.45f),
    radius = radius * 2.8f,
    center = center
  )

  // Sclera / dark socket
  drawCircle(
    color = Color(0xFF000814),
    radius = radius * 1.3f,
    center = center
  )

  // Glowing 3D Iris Sphere
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(Color.White, irisColor, irisColor.copy(alpha = 0.8f)),
      center = Offset(center.x - radius * 0.2f, center.y - radius * 0.2f),
      radius = radius
    ),
    radius = radius,
    center = center
  )

  // Slit Pupil (predator depth)
  drawCircle(
    color = Color.Black,
    radius = radius * 0.42f,
    center = center
  )

  // 3D Specular reflection glint (gives life and wet glass look)
  drawCircle(
    color = Color.White.copy(alpha = 0.95f),
    radius = radius * 0.25f,
    center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f)
  )
}

// -------------------------------------------------------------
// DIGITAL GLITCH & CAVITATION ABERRATION (FNAF AR Haywire)
// -------------------------------------------------------------
private fun DrawScope.drawGlitchAberration(center: Offset, w: Float, h: Float) {
  for (i in 0..6) {
    val y = center.y + (Random.nextFloat() * h * 0.8f - h * 0.4f)
    val sliceW = (Random.nextFloat() * w * 0.6f + w * 0.2f)
    val startX = center.x - sliceW / 2f + (Random.nextFloat() * 40f - 20f)
    drawLine(
      color = Color.Red.copy(alpha = 0.7f),
      start = Offset(startX, y),
      end = Offset(startX + sliceW, y),
      strokeWidth = (Random.nextFloat() * 4f + 2f)
    )
  }
}
