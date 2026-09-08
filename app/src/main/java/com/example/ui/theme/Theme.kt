package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OceanColorScheme = darkColorScheme(
  primary = MarineCyan,
  onPrimary = OceanDeep,
  primaryContainer = OceanSurface,
  onPrimaryContainer = MarineCyan,
  secondary = MarineGold,
  onSecondary = OceanDeep,
  secondaryContainer = OceanCard,
  onSecondaryContainer = MarineGold,
  tertiary = MarineCoral,
  onTertiary = Color.White,
  background = OceanDeep,
  onBackground = TextPrimary,
  surface = OceanAbyss,
  onSurface = TextPrimary,
  surfaceVariant = OceanCard,
  onSurfaceVariant = TextSecondary
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = OceanColorScheme,
    typography = Typography,
    content = content
  )
}

