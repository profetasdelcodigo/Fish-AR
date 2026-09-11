package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.EncounterPhase
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Procedural 3D Marine Geometry Engine for Piura, Peru Species.
 * Renders fully articulated, volumetric 3D marine creature meshes with realistic
 * sine-wave swimming physics, multi-perspective shading, volumetric depth lighting,
 * 3D fin flutter, and bioluminescent eye optics.
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
    val tailWag = sin(wavePhase) * 24f
    val finFlutter = cos(wavePhase * 1.6f) * 18f

    // Check if this is a high-speed charge directly toward the camera
    val isHeadOnCharge = phase is EncounterPhase.RealCharge && distanceMeters <= 18f

    if (isHeadOnCharge && speciesId != "tortuga_nuro") {
      draw3DHeadOnPredator(center, canvasW, canvasH, speciesId, tailWag, finFlutter, isHaywireActive, phase, isFlashlightOn)
    } else {
      // Lateral swimming 3D rendering per species:
      when (speciesId) {
        "caballa" -> draw3DCaballa(center, canvasW, canvasH, tailWag, finFlutter, isHaywireActive, phase, isFlashlightOn)
        "cachema" -> draw3DCachema(center, canvasW, canvasH, tailWag, finFlutter, isHaywireActive, phase, isFlashlightOn)
        "jurel" -> draw3DJurel(center, canvasW, canvasH, tailWag, finFlutter, isHaywireActive, phase, isFlashlightOn)
        "cabrilla" -> draw3DCabrilla(center, canvasW, canvasH, tailWag, finFlutter, isHaywireActive, phase, isFlashlightOn)
        "camotillo" -> draw3DCamotillo(center, canvasW, canvasH, tailWag, finFlutter, isHaywireActive, phase, isFlashlightOn)
        "mero_murike" -> draw3DMero(center, canvasW, canvasH, tailWag, finFlutter, isHaywireActive, phase, isFlashlightOn)
        "bonito" -> draw3DBonito(center, canvasW, canvasH, tailWag, finFlutter, isHaywireActive, phase, isFlashlightOn)
        "tortuga_nuro" -> draw3DTortuga(center, canvasW, canvasH, swimCycle, isHaywireActive, phase, isFlashlightOn)
        "super_pez" -> draw3DSuperPez(center, canvasW, canvasH, tailWag, finFlutter, isHaywireActive, phase, isFlashlightOn)
        else -> draw3DBonito(center, canvasW, canvasH, tailWag, finFlutter, isHaywireActive, phase, isFlashlightOn)
      }
    }

    // Dynamic Phase FX Overlays:
    if (phase is EncounterPhase.RealCharge) {
      // Cavitation hydrodynamic shockwaves & bubbles around creature
      for (k in 1..4) {
        val radius = canvasW * (0.35f + k * 0.08f)
        drawCircle(
          color = Color(0xFFFF3333).copy(alpha = 0.18f / k),
          radius = radius,
          center = center,
          style = Stroke(width = 3.5f)
        )
      }
    } else if (phase is EncounterPhase.FakeCharge) {
      // Shimmering golden phantom echo ripples
      for (k in 1..3) {
        val radius = canvasW * (0.30f + k * 0.10f)
        drawCircle(
          color = Color(0xFFFFD700).copy(alpha = 0.25f / k),
          radius = radius,
          center = center,
          style = Stroke(width = 2.5f)
        )
      }
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

  // 3. Volumetric Fusiform Body
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

  // Pectoral Fin
  val pecPath = Path().apply {
    val pecBaseX = center.x + w * 0.16f
    val pecBaseY = center.y + h * 0.04f
    moveTo(pecBaseX, pecBaseY)
    lineTo(pecBaseX - w * 0.18f, pecBaseY + h * 0.16f + finFlutter)
    lineTo(pecBaseX - w * 0.08f, pecBaseY + h * 0.08f)
    close()
  }
  drawPath(pecPath, dorsalColor.copy(alpha = 0.85f))

  // Bioluminescent Eye
  draw3DEye(
    center = Offset(center.x + w * 0.28f, center.y - h * 0.04f),
    radius = w * 0.038f,
    isHaywire = isHaywire,
    phase = phase
  )

  if (isHaywire) drawGlitchAberration(center, w, h)
}

// -------------------------------------------------------------
// 2. CABALLA DEL PACÍFICO (Iridescent Green Tiger Mackerel)
// -------------------------------------------------------------
private fun DrawScope.draw3DCaballa(
  center: Offset,
  w: Float,
  h: Float,
  tailWag: Float,
  finFlutter: Float,
  isHaywire: Boolean,
  phase: EncounterPhase,
  flashlight: Boolean
) {
  val baseColor = if (isHaywire) Color(0xFF8B0000) else Color(0xFF0F5A47)
  val bellyColor = if (isHaywire) Color(0xFFD32F2F) else Color(0xFFC0E8DD)
  val dorsalColor = if (isHaywire) Color(0xFF3B0000) else Color(0xFF063328)

  val tailX = center.x - w * 0.38f + tailWag * 0.8f
  val tailY = center.y + tailWag * 0.4f
  val tailPath = Path().apply {
    moveTo(center.x - w * 0.28f, center.y)
    cubicTo(tailX - w * 0.05f, tailY - h * 0.20f, tailX - w * 0.12f, tailY - h * 0.28f, tailX - w * 0.16f, tailY - h * 0.30f)
    cubicTo(tailX - w * 0.1f, tailY, tailX - w * 0.1f, tailY, tailX - w * 0.16f, tailY + h * 0.30f)
    cubicTo(tailX - w * 0.12f, tailY + h * 0.28f, tailX - w * 0.05f, tailY + h * 0.20f, center.x - w * 0.28f, center.y)
    close()
  }
  drawPath(tailPath, dorsalColor)

  val dorsalPath = Path().apply {
    moveTo(center.x - w * 0.14f, center.y - h * 0.18f)
    lineTo(center.x - w * 0.04f, center.y - h * 0.36f)
    lineTo(center.x + w * 0.06f, center.y - h * 0.2f)
    close()
  }
  drawPath(dorsalPath, dorsalColor)

  val bodyPath = Path().apply {
    val snoutX = center.x + w * 0.40f
    val snoutY = center.y
    val midTopX = center.x + w * 0.04f
    val midTopY = center.y - h * 0.20f
    val midBotX = center.x + w * 0.04f
    val midBotY = center.y + h * 0.20f
    val peduncleX = center.x - w * 0.30f + tailWag * 0.3f
    val peduncleY = center.y

    moveTo(snoutX, snoutY)
    cubicTo(snoutX - w * 0.14f, snoutY - h * 0.16f, midTopX + w * 0.14f, midTopY, midTopX, midTopY)
    cubicTo(midTopX - w * 0.18f, midTopY, peduncleX + w * 0.1f, peduncleY - h * 0.05f, peduncleX, peduncleY)
    cubicTo(peduncleX + w * 0.1f, peduncleY + h * 0.05f, midBotX - w * 0.18f, midBotY, midBotX, midBotY)
    cubicTo(midBotX + w * 0.14f, midBotY, snoutX - w * 0.14f, snoutY + h * 0.14f, snoutX, snoutY)
    close()
  }

  drawPath(
    path = bodyPath,
    brush = Brush.verticalGradient(
      colors = listOf(dorsalColor, baseColor, bellyColor),
      startY = center.y - h * 0.22f,
      endY = center.y + h * 0.22f
    )
  )

  // Mackerel tiger wavy vermiculated lines on upper back
  for (i in -3..3) {
    val xOffset = i * w * 0.06f
    drawLine(
      color = Color(0x4400241B),
      start = Offset(center.x + xOffset, center.y - h * 0.18f),
      end = Offset(center.x + xOffset - w * 0.03f, center.y),
      strokeWidth = 2.8f
    )
  }

  val pecPath = Path().apply {
    val pecBaseX = center.x + w * 0.15f
    val pecBaseY = center.y + h * 0.04f
    moveTo(pecBaseX, pecBaseY)
    lineTo(pecBaseX - w * 0.16f, pecBaseY + h * 0.15f + finFlutter)
    lineTo(pecBaseX - w * 0.07f, pecBaseY + h * 0.07f)
    close()
  }
  drawPath(pecPath, dorsalColor.copy(alpha = 0.85f))

  draw3DEye(
    center = Offset(center.x + w * 0.26f, center.y - h * 0.04f),
    radius = w * 0.036f,
    isHaywire = isHaywire,
    phase = phase
  )

  if (isHaywire) drawGlitchAberration(center, w, h)
}

// -------------------------------------------------------------
// 3. CACHEMA (Silvery Coastal Drum with Golden-Yellow Fins)
// -------------------------------------------------------------
private fun DrawScope.draw3DCachema(
  center: Offset,
  w: Float,
  h: Float,
  tailWag: Float,
  finFlutter: Float,
  isHaywire: Boolean,
  phase: EncounterPhase,
  flashlight: Boolean
) {
  val baseColor = if (isHaywire) Color(0xFF7B1C1C) else Color(0xFF7A8B99)
  val bellyColor = if (isHaywire) Color(0xFFD32F2F) else Color(0xFFE8ECEF)
  val dorsalColor = if (isHaywire) Color(0xFF3E0A0A) else Color(0xFF435A6B)
  val finGold = if (isHaywire) Color(0xFFFF5252) else Color(0xFFE6B800)

  // Caudal tail (gently notched triangular drum tail)
  val tailX = center.x - w * 0.36f + tailWag * 0.7f
  val tailPath = Path().apply {
    moveTo(center.x - w * 0.25f, center.y)
    lineTo(tailX - w * 0.08f, center.y - h * 0.20f)
    lineTo(tailX, center.y)
    lineTo(tailX - w * 0.08f, center.y + h * 0.20f)
    close()
  }
  drawPath(tailPath, finGold.copy(alpha = 0.9f))

  // Long soft dorsal fin
  val dorsalPath = Path().apply {
    moveTo(center.x - w * 0.16f, center.y - h * 0.16f)
    lineTo(center.x - w * 0.06f, center.y - h * 0.30f)
    lineTo(center.x + w * 0.14f, center.y - h * 0.20f)
    close()
  }
  drawPath(dorsalPath, finGold.copy(alpha = 0.85f))

  // Slender elongated body
  val bodyPath = Path().apply {
    val snoutX = center.x + w * 0.42f
    moveTo(snoutX, center.y)
    cubicTo(snoutX - w * 0.14f, center.y - h * 0.18f, center.x + w * 0.06f, center.y - h * 0.20f, center.x, center.y - h * 0.18f)
    cubicTo(center.x - w * 0.18f, center.y - h * 0.16f, center.x - w * 0.28f + tailWag * 0.3f, center.y - h * 0.05f, center.x - w * 0.28f + tailWag * 0.3f, center.y)
    cubicTo(center.x - w * 0.28f + tailWag * 0.3f, center.y + h * 0.05f, center.x - w * 0.16f, center.y + h * 0.18f, center.x, center.y + h * 0.18f)
    cubicTo(center.x + w * 0.14f, center.y + h * 0.16f, snoutX - w * 0.10f, center.y + h * 0.08f, snoutX, center.y)
    close()
  }

  drawPath(
    path = bodyPath,
    brush = Brush.verticalGradient(
      colors = listOf(dorsalColor, baseColor, bellyColor),
      startY = center.y - h * 0.20f,
      endY = center.y + h * 0.20f
    )
  )

  // Golden opercular and pectoral fins
  val pecPath = Path().apply {
    val px = center.x + w * 0.16f
    moveTo(px, center.y + h * 0.02f)
    lineTo(px - w * 0.14f, center.y + h * 0.14f + finFlutter)
    lineTo(px - w * 0.06f, center.y + h * 0.06f)
    close()
  }
  drawPath(pecPath, finGold)

  // Eye
  draw3DEye(
    center = Offset(center.x + w * 0.28f, center.y - h * 0.04f),
    radius = w * 0.038f,
    isHaywire = isHaywire,
    phase = phase
  )

  if (isHaywire) drawGlitchAberration(center, w, h)
}

// -------------------------------------------------------------
// 4. JUREL DEL PACÍFICO (Metallic Blue with Serrated Scute Line)
// -------------------------------------------------------------
private fun DrawScope.draw3DJurel(
  center: Offset,
  w: Float,
  h: Float,
  tailWag: Float,
  finFlutter: Float,
  isHaywire: Boolean,
  phase: EncounterPhase,
  flashlight: Boolean
) {
  val baseColor = if (isHaywire) Color(0xFF8B0000) else Color(0xFF1B4965)
  val bellyColor = if (isHaywire) Color(0xFFD32F2F) else Color(0xFFCAE9EA)
  val dorsalColor = if (isHaywire) Color(0xFF3B0000) else Color(0xFF0C2333)

  // Deeply forked crescent hydrofoil tail
  val tailX = center.x - w * 0.38f + tailWag * 0.85f
  val tailY = center.y + tailWag * 0.4f
  val tailPath = Path().apply {
    moveTo(center.x - w * 0.26f, center.y)
    lineTo(tailX - w * 0.14f, tailY - h * 0.32f)
    lineTo(tailX - w * 0.04f, tailY)
    lineTo(tailX - w * 0.14f, tailY + h * 0.32f)
    close()
  }
  drawPath(tailPath, dorsalColor)

  // Dorsal fin with front spine
  val dorsalPath = Path().apply {
    moveTo(center.x - w * 0.12f, center.y - h * 0.18f)
    lineTo(center.x - w * 0.02f, center.y - h * 0.38f)
    lineTo(center.x + w * 0.06f, center.y - h * 0.20f)
    close()
  }
  drawPath(dorsalPath, dorsalColor)

  // Hydrodynamic jack body
  val bodyPath = Path().apply {
    val snoutX = center.x + w * 0.42f
    moveTo(snoutX, center.y)
    cubicTo(snoutX - w * 0.14f, center.y - h * 0.20f, center.x + w * 0.08f, center.y - h * 0.22f, center.x, center.y - h * 0.22f)
    cubicTo(center.x - w * 0.18f, center.y - h * 0.18f, center.x - w * 0.30f + tailWag * 0.3f, center.y - h * 0.05f, center.x - w * 0.30f + tailWag * 0.3f, center.y)
    cubicTo(center.x - w * 0.30f + tailWag * 0.3f, center.y + h * 0.05f, center.x - w * 0.18f, center.y + h * 0.22f, center.x, center.y + h * 0.22f)
    cubicTo(center.x + w * 0.12f, center.y + h * 0.20f, snoutX - w * 0.12f, center.y + h * 0.12f, snoutX, center.y)
    close()
  }

  drawPath(
    path = bodyPath,
    brush = Brush.verticalGradient(
      colors = listOf(dorsalColor, baseColor, bellyColor),
      startY = center.y - h * 0.24f,
      endY = center.y + h * 0.24f
    )
  )

  // Serrated lateral scute line (classic Jurel anatomy)
  val scutePath = Path().apply {
    moveTo(center.x + w * 0.12f, center.y)
    cubicTo(center.x, center.y + h * 0.04f, center.x - w * 0.15f, center.y, center.x - w * 0.28f, center.y)
  }
  drawPath(scutePath, Color(0x66FFFFFF), style = Stroke(width = 3.5f))

  // Opercular black dot
  drawCircle(
    color = Color.Black.copy(alpha = 0.85f),
    radius = w * 0.016f,
    center = Offset(center.x + w * 0.20f, center.y - h * 0.06f)
  )

  // Pectoral fin
  val pecPath = Path().apply {
    val px = center.x + w * 0.16f
    moveTo(px, center.y + h * 0.02f)
    lineTo(px - w * 0.18f, center.y + h * 0.16f + finFlutter)
    lineTo(px - w * 0.08f, center.y + h * 0.06f)
    close()
  }
  drawPath(pecPath, dorsalColor.copy(alpha = 0.85f))

  draw3DEye(
    center = Offset(center.x + w * 0.28f, center.y - h * 0.04f),
    radius = w * 0.038f,
    isHaywire = isHaywire,
    phase = phase
  )

  if (isHaywire) drawGlitchAberration(center, w, h)
}

// -------------------------------------------------------------
// 5. CABRILLA DE PEÑA (Rocky Perch with Lateral Tiger Bars)
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
  val baseColor = if (isHaywire) Color(0xFF7A1515) else Color(0xFF5C4033)
  val bandColor = if (isHaywire) Color(0xFF330000) else Color(0xFF2E1A11)
  val bellyColor = if (isHaywire) Color(0xFFB71C1C) else Color(0xFFC4A482)

  val tailX = center.x - w * 0.36f + tailWag
  val tailPath = Path().apply {
    moveTo(center.x - w * 0.24f, center.y)
    lineTo(tailX - w * 0.06f, center.y - h * 0.24f)
    lineTo(tailX, center.y)
    lineTo(tailX - w * 0.06f, center.y + h * 0.24f)
    close()
  }
  drawPath(tailPath, bandColor)

  val dorsalPath = Path().apply {
    moveTo(center.x - w * 0.18f, center.y - h * 0.18f)
    lineTo(center.x - w * 0.08f, center.y - h * 0.32f)
    lineTo(center.x + w * 0.08f, center.y - h * 0.30f)
    lineTo(center.x + w * 0.18f, center.y - h * 0.18f)
    close()
  }
  drawPath(dorsalPath, bandColor)

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

  for (i in -2..2) {
    val bx = center.x + i * w * 0.09f
    drawLine(
      color = bandColor.copy(alpha = 0.65f),
      start = Offset(bx, center.y - h * 0.18f),
      end = Offset(bx - w * 0.03f, center.y + h * 0.18f),
      strokeWidth = 9f
    )
  }

  val pecPath = Path().apply {
    val px = center.x + w * 0.15f
    moveTo(px, center.y + h * 0.02f)
    lineTo(px - w * 0.14f, center.y + h * 0.14f + finFlutter)
    lineTo(px - w * 0.06f, center.y + h * 0.06f)
    close()
  }
  drawPath(pecPath, Color(0xFFD4AF37).copy(alpha = 0.8f))

  draw3DEye(
    center = Offset(center.x + w * 0.27f, center.y - h * 0.05f),
    radius = w * 0.040f,
    isHaywire = isHaywire,
    phase = phase
  )

  if (isHaywire) drawGlitchAberration(center, w, h)
}

// -------------------------------------------------------------
// 6. CAMOTILLO (Sandy Reef Bass with Cyan Dots & Tail Filament)
// -------------------------------------------------------------
private fun DrawScope.draw3DCamotillo(
  center: Offset,
  w: Float,
  h: Float,
  tailWag: Float,
  finFlutter: Float,
  isHaywire: Boolean,
  phase: EncounterPhase,
  flashlight: Boolean
) {
  val baseColor = if (isHaywire) Color(0xFF7A1515) else Color(0xFF9A553E) // warm sand-orange
  val bellyColor = if (isHaywire) Color(0xFFB71C1C) else Color(0xFFE8D4BE)
  val dorsalColor = if (isHaywire) Color(0xFF330000) else Color(0xFF5A2A1A)

  // Tail with extended upper filament (camotillo trait)
  val tailX = center.x - w * 0.36f + tailWag
  val tailPath = Path().apply {
    moveTo(center.x - w * 0.24f, center.y)
    lineTo(tailX - w * 0.12f, center.y - h * 0.28f) // upper filament
    lineTo(tailX - w * 0.04f, center.y)
    lineTo(tailX - w * 0.08f, center.y + h * 0.20f)
    close()
  }
  drawPath(tailPath, dorsalColor)

  // Spiky dorsal ridge
  val dorsalPath = Path().apply {
    moveTo(center.x - w * 0.16f, center.y - h * 0.16f)
    lineTo(center.x - w * 0.08f, center.y - h * 0.34f)
    lineTo(center.x + w * 0.08f, center.y - h * 0.30f)
    lineTo(center.x + w * 0.16f, center.y - h * 0.16f)
    close()
  }
  drawPath(dorsalPath, dorsalColor)

  val bodyPath = Path().apply {
    val snoutX = center.x + w * 0.40f
    moveTo(snoutX, center.y)
    cubicTo(snoutX - w * 0.12f, center.y - h * 0.18f, center.x + w * 0.06f, center.y - h * 0.18f, center.x, center.y - h * 0.18f)
    cubicTo(center.x - w * 0.18f, center.y - h * 0.16f, center.x - w * 0.28f + tailWag * 0.3f, center.y - h * 0.05f, center.x - w * 0.28f + tailWag * 0.3f, center.y)
    cubicTo(center.x - w * 0.28f + tailWag * 0.3f, center.y + h * 0.05f, center.x - w * 0.16f, center.y + h * 0.18f, center.x, center.y + h * 0.18f)
    cubicTo(center.x + w * 0.14f, center.y + h * 0.16f, snoutX - w * 0.10f, center.y + h * 0.08f, snoutX, center.y)
    close()
  }

  drawPath(
    path = bodyPath,
    brush = Brush.verticalGradient(
      colors = listOf(dorsalColor, baseColor, bellyColor),
      startY = center.y - h * 0.22f,
      endY = center.y + h * 0.22f
    )
  )

  // Cyan bioluminescent spots along the flank
  for (i in -3..3) {
    val px = center.x + i * w * 0.07f
    val py = center.y + sin(i.toFloat()) * h * 0.06f
    drawCircle(
      color = Color(0xFF00E5FF).copy(alpha = 0.75f),
      radius = w * 0.014f,
      center = Offset(px, py)
    )
  }

  val pecPath = Path().apply {
    val px = center.x + w * 0.15f
    moveTo(px, center.y + h * 0.02f)
    lineTo(px - w * 0.14f, center.y + h * 0.14f + finFlutter)
    lineTo(px - w * 0.06f, center.y + h * 0.06f)
    close()
  }
  drawPath(pecPath, Color(0xFFFFB74D).copy(alpha = 0.85f))

  draw3DEye(
    center = Offset(center.x + w * 0.27f, center.y - h * 0.04f),
    radius = w * 0.038f,
    isHaywire = isHaywire,
    phase = phase
  )

  if (isHaywire) drawGlitchAberration(center, w, h)
}

// -------------------------------------------------------------
// 7. MERO MURIKE (Broad Heavy Reef Predator with Cavernous Head)
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
  val baseColor = if (isHaywire) Color(0xFF6B1212) else Color(0xFF384533)
  val darkSpots = if (isHaywire) Color(0xFF220000) else Color(0xFF1E281C)
  val bellyColor = if (isHaywire) Color(0xFFA52A2A) else Color(0xFF7A8B74)

  val tailX = center.x - w * 0.36f + tailWag
  val tailPath = Path().apply {
    moveTo(center.x - w * 0.24f, center.y)
    cubicTo(tailX, center.y - h * 0.28f, tailX - w * 0.08f, center.y - h * 0.25f, tailX - w * 0.1f, center.y)
    cubicTo(tailX - w * 0.08f, center.y + h * 0.25f, tailX, center.y + h * 0.28f, center.x - w * 0.24f, center.y)
    close()
  }
  drawPath(tailPath, darkSpots)

  val dorsalPath = Path().apply {
    moveTo(center.x - w * 0.16f, center.y - h * 0.22f)
    lineTo(center.x - w * 0.12f, center.y - h * 0.42f)
    lineTo(center.x - w * 0.04f, center.y - h * 0.36f)
    lineTo(center.x + w * 0.04f, center.y - h * 0.40f)
    lineTo(center.x + w * 0.12f, center.y - h * 0.26f)
    close()
  }
  drawPath(dorsalPath, darkSpots)

  val bodyPath = Path().apply {
    val headX = center.x + w * 0.40f
    val headY = center.y + h * 0.04f
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

  // Cavernous lower jaw
  drawLine(
    color = darkSpots,
    start = Offset(center.x + w * 0.40f, center.y + h * 0.04f),
    end = Offset(center.x + w * 0.22f, center.y + h * 0.10f),
    strokeWidth = 4f
  )

  drawCircle(
    color = darkSpots.copy(alpha = 0.8f),
    radius = w * 0.08f,
    center = Offset(center.x + w * 0.12f, center.y + h * 0.06f + finFlutter * 0.4f)
  )

  draw3DEye(
    center = Offset(center.x + w * 0.26f, center.y - h * 0.08f),
    radius = w * 0.046f,
    isHaywire = isHaywire,
    phase = phase
  )

  if (isHaywire) drawGlitchAberration(center, w, h)
}

// -------------------------------------------------------------
// 8. TORTUGA VERDE MARINA (3D Domed Carapace & Wing Flippers)
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

  val flipperAngle = sin(swimCycle * 2 * PI.toFloat()) * 28f

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

  val headPath = Path().apply {
    val hx = center.x + w * 0.32f
    val hy = center.y
    moveTo(center.x + w * 0.16f, center.y - h * 0.08f)
    cubicTo(hx - w * 0.04f, hy - h * 0.10f, hx + w * 0.06f, hy - h * 0.06f, hx + w * 0.08f, hy)
    cubicTo(hx + w * 0.06f, hy + h * 0.06f, hx - w * 0.04f, hy + h * 0.10f, center.x + w * 0.16f, center.y + h * 0.08f)
    close()
  }
  drawPath(headPath, skinColor)

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

  for (k in -1..1) {
    val cx = center.x + k * w * 0.12f
    drawCircle(
      color = scuteEdge.copy(alpha = 0.8f),
      radius = w * 0.055f,
      center = Offset(cx, center.y),
      style = Stroke(width = 3f)
    )
  }

  draw3DEye(
    center = Offset(center.x + w * 0.35f, center.y - h * 0.04f),
    radius = w * 0.032f,
    isHaywire = isHaywire,
    phase = phase
  )

  if (isHaywire) drawGlitchAberration(center, w, h)
}

// -------------------------------------------------------------
// 9. SÚPER PEZ (Legendary Hero of Nutrition & Health)
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

  val capeTailX = center.x - w * 0.42f + tailWag * 1.2f
  val capePath = Path().apply {
    moveTo(center.x + w * 0.06f, center.y - h * 0.18f)
    cubicTo(center.x - w * 0.1f, center.y - h * 0.42f, capeTailX, center.y - h * 0.38f, capeTailX - w * 0.08f, center.y - h * 0.15f)
    cubicTo(capeTailX, center.y, center.x - w * 0.18f, center.y - h * 0.06f, center.x + w * 0.06f, center.y - h * 0.18f)
    close()
  }
  drawPath(capePath, heroGold.copy(alpha = 0.9f))

  val tailPath = Path().apply {
    val tx = center.x - w * 0.34f + tailWag
    moveTo(center.x - w * 0.22f, center.y)
    lineTo(tx - w * 0.12f, center.y - h * 0.28f)
    lineTo(tx - w * 0.04f, center.y)
    lineTo(tx - w * 0.12f, center.y + h * 0.28f)
    close()
  }
  drawPath(tailPath, heroGold)

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

  // Shield Emblem
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

  val pecPath = Path().apply {
    val px = center.x + w * 0.18f
    moveTo(px, center.y + h * 0.04f)
    lineTo(px - w * 0.15f, center.y + h * 0.18f + finFlutter)
    lineTo(px - w * 0.06f, center.y + h * 0.08f)
    close()
  }
  drawPath(pecPath, heroGold.copy(alpha = 0.85f))

  draw3DEye(
    center = Offset(center.x + w * 0.30f, center.y - h * 0.06f),
    radius = w * 0.045f,
    isHaywire = isHaywire,
    phase = phase
  )

  if (isHaywire) drawGlitchAberration(center, w, h)
}

// -------------------------------------------------------------
// 10. DYNAMIC HEAD-ON ATTACK PERSPECTIVE (FNAF AR Charge)
// -------------------------------------------------------------
private fun DrawScope.draw3DHeadOnPredator(
  center: Offset,
  w: Float,
  h: Float,
  speciesId: String,
  tailWag: Float,
  finFlutter: Float,
  isHaywire: Boolean,
  phase: EncounterPhase,
  flashlight: Boolean
) {
  val primaryColor = when (speciesId) {
    "caballa" -> if (isHaywire) Color(0xFF8B0000) else Color(0xFF0F5A47)
    "cachema" -> if (isHaywire) Color(0xFF8B0000) else Color(0xFF5A6F7C)
    "jurel" -> if (isHaywire) Color(0xFF8B0000) else Color(0xFF1B4965)
    "cabrilla" -> if (isHaywire) Color(0xFF8B0000) else Color(0xFF5C4033)
    "camotillo" -> if (isHaywire) Color(0xFF8B0000) else Color(0xFF9A553E)
    "mero_murike" -> if (isHaywire) Color(0xFF8B0000) else Color(0xFF384533)
    "super_pez" -> if (isHaywire) Color(0xFFFF2A2A) else Color(0xFF0066CC)
    else -> if (isHaywire) Color(0xFF8B0000) else Color(0xFF0F3D59)
  }

  val darkTone = if (isHaywire) Color(0xFF330000) else Color(0xFF071C2C)

  // 1. Dual Outstretched Pectoral Fins (Charging forward)
  val leftWing = Path().apply {
    moveTo(center.x - w * 0.15f, center.y)
    lineTo(center.x - w * 0.44f, center.y + h * 0.12f + finFlutter)
    lineTo(center.x - w * 0.22f, center.y + h * 0.20f)
    close()
  }
  val rightWing = Path().apply {
    moveTo(center.x + w * 0.15f, center.y)
    lineTo(center.x + w * 0.44f, center.y + h * 0.12f - finFlutter)
    lineTo(center.x + w * 0.22f, center.y + h * 0.20f)
    close()
  }
  drawPath(leftWing, primaryColor.copy(alpha = 0.85f))
  drawPath(rightWing, primaryColor.copy(alpha = 0.85f))

  // 2. Head-on Massive Hydrodynamic Torso Silhouette
  val headCircleRadius = w * 0.28f
  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(primaryColor, darkTone, Color.Black),
      center = Offset(center.x, center.y),
      radius = headCircleRadius
    ),
    radius = headCircleRadius,
    center = center
  )

  // 3. Menacing Wide Open Cavernous Mouth
  val mouthRadiusX = w * 0.14f
  val mouthRadiusY = h * 0.10f
  drawCircle(
    color = Color(0xFF0A0002),
    radius = mouthRadiusX,
    center = Offset(center.x, center.y + h * 0.04f)
  )

  // Predator Teeth lining the mouth
  for (i in -3..3) {
    val tx = center.x + i * w * 0.035f
    val ty = center.y + h * 0.04f - mouthRadiusY * 0.6f
    drawLine(
      color = Color.White.copy(alpha = 0.9f),
      start = Offset(tx, ty),
      end = Offset(tx, ty + 10f),
      strokeWidth = 3f
    )
  }

  // 4. Stereo Glowing Bioluminescent Eyes (Left & Right staring into camera)
  val leftEyeCenter = Offset(center.x - w * 0.18f, center.y - h * 0.08f)
  val rightEyeCenter = Offset(center.x + w * 0.18f, center.y - h * 0.08f)
  val eyeR = w * 0.042f

  draw3DEye(center = leftEyeCenter, radius = eyeR, isHaywire = isHaywire, phase = phase)
  draw3DEye(center = rightEyeCenter, radius = eyeR, isHaywire = isHaywire, phase = phase)

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

  // Slit Pupil
  drawCircle(
    color = Color.Black,
    radius = radius * 0.42f,
    center = center
  )

  // 3D Specular reflection glint
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
