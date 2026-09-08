package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.MarineGameViewModel
import com.example.ui.theme.*

private data class EquipmentUpgrade(
  val id: String,
  val name: String,
  val subtitle: String,
  val icon: ImageVector,
  val accent: Color,
  val effect: String,
  val costs: List<Int>
)

@Composable
fun EquipmentLabScreen(viewModel: MarineGameViewModel, modifier: Modifier = Modifier) {
  val pescacoins by viewModel.pescacoins.collectAsState()
  val levels = remember { mutableStateMapOf<String, Int>() }
  var selectedId by remember { mutableStateOf("reactor") }

  val upgrades = remember {
    listOf(
      EquipmentUpgrade("reactor", "Núcleo de Descarga", "Capacitor electromagnético", Icons.Default.Bolt, MarineCyan, "Menor consumo y recuperación más rápida del rayo eléctrico.", listOf(60, 120, 210)),
      EquipmentUpgrade("uv", "Lente Abisal UV", "Óptica de penetración marina", Icons.Default.FlashlightOn, MarineGold, "Mayor alcance y lectura de siluetas durante el acecho.", listOf(50, 100, 180)),
      EquipmentUpgrade("shield", "Escudo Súper Pez", "Membrana cinética de emergencia", Icons.Default.Shield, MarineGreen, "Mejora la tolerancia al impacto y la recuperación del casco.", listOf(80, 160, 260)),
      EquipmentUpgrade("sonar", "Sonar de Cardumen", "Matriz de rastreo direccional", Icons.Default.Sensors, Color(0xFF7C4DFF), "Reduce zonas muertas y mejora la lectura de anomalías.", listOf(70, 140, 230)),
      EquipmentUpgrade("stabilizer", "Estabilizador AR", "Compensación de orientación", Icons.Default.Speed, MarineCoral, "Suaviza giros y facilita seguir una embestida.", listOf(45, 90, 150))
    )
  }

  Column(
    modifier = modifier.fillMaxSize().background(Brush.verticalGradient(listOf(OceanDeep, OceanAbyss, OceanDeep))).padding(16.dp)
  ) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
      Column(Modifier.weight(1f)) {
        Text("LABORATORIO", color = MarineCyan, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.7.sp)
        Text("Equipamiento abisal", color = TextPrimary, fontSize = 27.sp, fontWeight = FontWeight.Black)
        Text("Configura tu equipo antes de entrar en zona de encuentro.", color = TextSecondary, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
      }
      Row(Modifier.clip(RoundedCornerShape(18.dp)).background(OceanCard).border(1.dp, MarineGold.copy(alpha = .65f), RoundedCornerShape(18.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Tune, null, tint = MarineGold, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(6.dp))
        Text("$pescacoins PC", color = MarineGold, fontWeight = FontWeight.Black, fontSize = 12.sp)
      }
    }

    Spacer(Modifier.height(14.dp))
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(OceanCard.copy(alpha = .45f)).border(1.dp, MarineCyan.copy(alpha = .22f), RoundedCornerShape(18.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
      Box(Modifier.size(42.dp).clip(CircleShape).background(MarineCyan.copy(alpha = .12f)).border(1.dp, MarineCyan.copy(alpha = .45f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Memory, null, tint = MarineCyan, modifier = Modifier.size(21.dp)) }
      Spacer(Modifier.width(10.dp))
      Column(Modifier.weight(1f)) { Text("CONFIGURACIÓN DE MISIÓN", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Black); Text("La habilidad del operador sigue siendo decisiva.", color = TextSecondary, fontSize = 11.sp) }
    }

    Spacer(Modifier.height(12.dp))
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      items(upgrades) { upgrade ->
        val level = levels[upgrade.id] ?: 0
        val selected = selectedId == upgrade.id
        val cost = upgrade.costs.getOrNull(level)
        Column(
          Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(if (selected) OceanCard.copy(alpha = .92f) else OceanCard.copy(alpha = .58f)).border(1.dp, if (selected) upgrade.accent.copy(alpha = .72f) else MarineCyan.copy(alpha = .13f), RoundedCornerShape(20.dp)).clickable { selectedId = upgrade.id }.padding(14.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(upgrade.accent.copy(alpha = .12f)).border(1.dp, upgrade.accent.copy(alpha = .45f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Icon(upgrade.icon, null, tint = upgrade.accent, modifier = Modifier.size(23.dp)) }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) { Text(upgrade.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Black); Text(upgrade.subtitle, color = TextSecondary, fontSize = 11.sp) }
            Text("$level/3", color = upgrade.accent, fontWeight = FontWeight.Black, fontSize = 12.sp)
          }
          Spacer(Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { repeat(3) { index -> Box(Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(5.dp)).background(if (index < level) upgrade.accent else TextMuted.copy(alpha = .22f))) } }
          if (selected) {
            Spacer(Modifier.height(9.dp))
            Text(upgrade.effect, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
              Text(if (cost == null) "SISTEMA MAXIMIZADO" else "SIGUIENTE NIVEL · $cost PC", color = if (cost == null) MarineGreen else upgrade.accent, fontSize = 10.sp, fontWeight = FontWeight.Black)
              Box(Modifier.clip(RoundedCornerShape(12.dp)).background((if (cost == null) MarineGreen else upgrade.accent).copy(alpha = .14f)).border(1.dp, (if (cost == null) MarineGreen else upgrade.accent).copy(alpha = .55f), RoundedCornerShape(12.dp)).clickable(enabled = cost != null && pescacoins >= cost) { if (cost != null) { viewModel.addPescacoins(-cost); levels[upgrade.id] = level + 1 } }.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(if (cost == null) "MAX" else if (pescacoins >= cost) "INSTALAR" else "FONDOS INSUFICIENTES", color = if (cost == null) MarineGreen else if (pescacoins >= cost) upgrade.accent else TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Black)
              }
            }
          }
        }
      }
      item { Spacer(Modifier.height(4.dp)) }
    }
  }
}
