package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.MarineGameViewModel
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MarineGold
import com.example.ui.theme.MarineGreen
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.MarineCoral
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private data class EquipmentUpgrade(
  val id: String,
  val name: String,
  val subtitle: String,
  val icon: ImageVector,
  val accent: Color,
  val effect: String,
  val costs: List<Int>,
  val maxLabel: String
)

@Composable
fun EquipmentLabScreen(
  viewModel: MarineGameViewModel,
  modifier: Modifier = Modifier
) {
  val pescacoins by viewModel.pescacoins.collectAsStateCompat()
  val levels = remember {
    mutableStateMapOf(
      "reactor" to 0,
      "uv" to 0,
      "shield" to 0,
      "sonar" to 0,
      "stabilizer" to 0
    )
  }
  var selectedId by remember { mutableStateOfNullable("reactor") }

  val upgrades = remember {
    listOf(
      EquipmentUpgrade(
        id = "reactor",
        name = "Núcleo de Descarga",
        subtitle = "Capacitor electromagnético",
        icon = Icons.Default.Bolt,
        accent = MarineCyan,
        effect = "Reduce el coste operativo de la descarga y acelera su recuperación.",
        costs = listOf(60, 120, 210),
        maxLabel = "DESCARGA + EFICIENCIA"
      ),
      EquipmentUpgrade(
        id = "uv",
        name = "Lente Abisal UV",
        subtitle = "Óptica de penetración marina",
        icon = Icons.Default.FlashlightOn,
        accent = MarineGold,
        effect = "Amplía la lectura de siluetas y hace más estable la iluminación durante un encuentro.",
        costs = listOf(50, 100, 180),
        maxLabel = "VISIÓN + ALCANCE"
      ),
      EquipmentUpgrade(
        id = "shield",
        name = "Escudo Súper Pez",
        subtitle = "Membrana cinética de emergencia",
        icon = Icons.Default.Shield,
        accent = MarineGreen,
        effect = "Mejora la tolerancia ante impactos y recupera mejor el sistema de protección.",
        costs = listOf(80, 160, 260),
        maxLabel = "CASCO + RESILIENCIA"
      ),
      EquipmentUpgrade(
        id = "sonar",
        name = "Sonar de Cardumen",
        subtitle = "Matriz de rastreo direccional",
        icon = Icons.Default.Sensors,
        accent = Color(0xFF7C4DFF),
        effect = "Afina la detección de anomalías y reduce las zonas muertas del radar.",
        costs = listOf(70, 140, 230),
        maxLabel = "RADAR + PRECISIÓN"
      ),
      EquipmentUpgrade(
        id = "stabilizer",
        name = "Estabilizador AR",
        subtitle = "Compensación de orientación",
        icon = Icons.Default.Speed,
        accent = MarineCoral,
        effect = "Suaviza los giros y mejora el control al seguir una criatura en carga.",
        costs = listOf(45, 90, 150),
        maxLabel = "CONTROL + FLUIDEZ"
      )
    )
  }

  val selected = upgrades.firstOrNull { it.id == selectedId } ?: upgrades.first()
  val selectedLevel = levels[selected.id] ?: 0
  val nextCost = selected.costs.getOrNull(selectedLevel)
  val canUpgrade = nextCost != null && pescacoins >= nextCost

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(
        androidx.compose.ui.graphics.Brush.verticalGradient(
          listOf(OceanDeep, OceanAbyss, OceanDeep)
        )
      )
      .padding(horizontal = 16.dp, vertical = 14.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "LABORATORIO",
          color = MarineCyan,
          fontSize = 11.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 1.7.sp
        )
        Text(
          text = "Equipamiento abisal",
          color = TextPrimary,
          fontSize = 27.sp,
          fontWeight = FontWeight.Black
        )
        Text(
          text = "Personaliza el equipo antes de entrar en zona de encuentro.",
          color = TextSecondary,
          fontSize = 12.sp,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }

      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(18.dp))
          .background(OceanCard.copy(alpha = 0.9f))
          .border(1.dp, MarineGold.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
          .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Tune, contentDescription = null, tint = MarineGold, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.width(7.dp))
        Text(
          text = "$pescacoins",
          color = MarineGold,
          fontWeight = FontWeight.Black,
          fontSize = 14.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(OceanCard.copy(alpha = 0.46f))
        .border(1.dp, MarineCyan.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
        .padding(13.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(MarineCyan.copy(alpha = 0.14f))
          .border(1.dp, MarineCyan.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.Memory, contentDescription = null, tint = MarineCyan, modifier = Modifier.size(22.dp))
      }
      Spacer(modifier = Modifier.width(11.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text("CONFIGURACIÓN DE MISIÓN", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black)
        Text("Cada mejora altera tu preparación, no reemplaza la habilidad del operador.", color = TextSecondary, fontSize = 11.sp)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(upgrades) { upgrade ->
        val level = levels[upgrade.id] ?: 0
        val isSelected = selected.id == upgrade.id
        val cost = upgrade.costs.getOrNull(level)
        val isMaxed = cost == null

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) OceanCard.copy(alpha = 0.9f) else OceanCard.copy(alpha = 0.58f))
            .border(
              width = if (isSelected) 1.4.dp else 1.dp,
              color = if (isSelected) upgrade.accent.copy(alpha = 0.72f) else MarineCyan.copy(alpha = 0.14f),
              shape = RoundedCornerShape(20.dp)
            )
            .clickable { selectedId = upgrade.id }
            .padding(14.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(upgrade.accent.copy(alpha = 0.13f))
                .border(1.dp, upgrade.accent.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(upgrade.icon, contentDescription = null, tint = upgrade.accent, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(upgrade.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Black)
              Text(upgrade.subtitle, color = TextSecondary, fontSize = 11.sp)
            }
            Text(
              text = "${level}/3",
              color = upgrade.accent,
              fontWeight = FontWeight.Black,
              fontSize = 12.sp
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(3) { index ->
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(5.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (index < level) upgrade.accent else TextMuted.copy(alpha = 0.22f))
              )
            }
          }

          if (isSelected) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(upgrade.effect, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (isMaxed) upgrade.maxLabel else "SIGUIENTE NIVEL · $cost PC",
                color = if (isMaxed) MarineGreen else upgrade.accent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
              )
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(14.dp))
                  .background(
                    when {
                      isMaxed -> MarineGreen.copy(alpha = 0.15f)
                      canUpgrade -> upgrade.accent.copy(alpha = 0.2f)
                      else -> Color.White.copy(alpha = 0.06f)
                    }
                  )
                  .border(
                    1.dp,
                    when {
                      isMaxed -> MarineGreen.copy(alpha = 0.55f)
                      canUpgrade -> upgrade.accent.copy(alpha = 0.7f)
                      else -> Color.White.copy(alpha = 0.1f)
                    },
                    RoundedCornerShape(14.dp)
                  )
                  .clickable(enabled = canUpgrade) {
                    if (nextCost != null && pescacoins >= nextCost) {
                      viewModel.addPescacoins(-nextCost)
                      levels[upgrade.id] = level + 1
                    }
                  }
                  .padding(horizontal = 13.dp, vertical = 8.dp)
              ) {
                Text(
                  text = when {
                    isMaxed -> "MAX"
                    canUpgrade -> "INSTALAR"
                    else -> "FONDOS INSUFICIENTES"
                  },
                  color = when {
                    isMaxed -> MarineGreen
                    canUpgrade -> upgrade.accent
                    else -> TextMuted
                  },
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Black
                )
              }
            }
          }
        }
      }
      item { Spacer(modifier = Modifier.height(4.dp)) }
    }
  }
}

@Composable
private fun <T> StateFlowValueCompat(value: T): T = value

private class NullableState<T>(initial: T) {
  var value by mutableStateOf(initial)
}

private fun <T> rememberMutableStateOf(value: T): NullableState<T> = androidx.compose.runtime.remember { NullableState(value) }

private fun <T> androidx.compose.runtime.ComposableScope.dummy() = Unit

private fun <T> androidx.compose.runtime.ComposableScope.rememberState(value: T) = Unit

private fun rememberMutableIntStateOf(value: Int) = mutableIntStateOf(value)

private fun rememberMutableStringStateOf(value: String?) = androidx.compose.runtime.remember { mutableStateOfNullable(value) }

private fun <T> mutableStateOfNullable(value: T) = androidx.compose.runtime.mutableStateOf(value)

private fun <T> remember(value: T) = androidx.compose.runtime.remember { mutableStateOfNullable(value) }

private val MarineGameViewModel.pescacoins get() = this.pescacoins

@Composable
private fun MarineGameViewModel.pescacoins.collectAsStateCompat() =
  androidx.compose.runtime.collectAsStateCompat(this.pescacoins)

@Composable
private fun <T> androidx.compose.runtime.collectAsStateCompat(flow: kotlinx.coroutines.flow.StateFlow<T>): androidx.compose.runtime.State<T> =
  flow.collectAsState()
