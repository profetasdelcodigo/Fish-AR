package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MarineGold
import com.example.ui.theme.MarineGreen
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanDeep

@Composable
fun MagneticReelMeter(
  progress: Float, // 0..1
  needlePosition: Float = 0.5f, // 0..1, sweeps back and forth
  targetZone: ClosedFloatingPointRange<Float> = 0.35f..0.65f,
  onReelTap: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isPrecise = needlePosition in targetZone
  Column(
    modifier = modifier
      .fillMaxWidth(0.9f)
      .clip(RoundedCornerShape(16.dp))
      .background(OceanDeep.copy(alpha = 0.92f))
      .border(2.dp, MarineCyan, RoundedCornerShape(16.dp))
      .padding(16.dp)
      .testTag("magnetic_reel_meter"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Text(
      text = "TENSIÓN ELECTROMAGNÉTICA",
      color = MarineGold,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 15.sp
    )
    Text(
      text = if (isPrecise) "¡ZONA PRECISA! Toca AHORA para máximo efecto" else "Espera a que la aguja entre en la zona dorada",
      color = if (isPrecise) MarineGreen else Color.White,
      fontWeight = if (isPrecise) FontWeight.Bold else FontWeight.Normal,
      fontSize = 12.sp
    )

    // Precision Needle Bar: gold zone is the "sweet spot"; the cyan needle sweeps back and forth.
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxWidth()
        .height(20.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(OceanCard)
        .border(1.dp, MarineCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
        .testTag("reel_precision_bar")
    ) {
      val barWidth = maxWidth
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .width(barWidth * (targetZone.endInclusive - targetZone.start))
          .offset(x = barWidth * targetZone.start)
          .background(MarineGold.copy(alpha = 0.55f))
      )
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .width(4.dp)
          .offset(x = barWidth * needlePosition.coerceIn(0f, 1f) - 2.dp)
          .background(if (isPrecise) MarineGreen else MarineCyan)
      )
    }

    // Tension Bar
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(28.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(OceanCard)
        .border(1.dp, MarineCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
    ) {
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .fillMaxWidth(progress.coerceIn(0f, 1f))
          .clip(RoundedCornerShape(14.dp))
          .background(
            if (progress > 0.7f) MarineGreen else if (progress > 0.35f) MarineCyan else MarineGold
          )
      )

      Text(
        text = "${(progress * 100).toInt()}%",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier.align(Alignment.Center)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center
    ) {
      Button(
        onClick = onReelTap,
        colors = ButtonDefaults.buttonColors(containerColor = if (isPrecise) MarineGreen else MarineCyan, contentColor = OceanDeep),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth(0.85f)
          .height(52.dp)
          .testTag("reel_tap_button")
      ) {
        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (isPrecise) "¡ESTABILIZAR AHORA! (+24%)" else "ESTABILIZAR CAMPO (+8%)",
          fontWeight = FontWeight.Black,
          fontSize = 14.sp
        )
      }
    }
  }
}
