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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.MarineSoundEngine
import com.example.game.MarineGameViewModel
import com.example.ui.theme.*

data class InventoryItem(val id: String, val name: String, val description: String, val count: Int, val icon: ImageVector, val iconColor: Color)
data class UpgradeItem(val id: String, val name: String, val level: Int, val effect: String, val cost: Int, val icon: ImageVector)

@Composable
fun InventoryScreen(
  viewModel: MarineGameViewModel,
  modifier: Modifier = Modifier,
  onOpenLab: () -> Unit = {}
) {
  val pescacoins by viewModel.pescacoins.collectAsState()
  var activeTab by remember { mutableStateOf("Objetos") }
  var upgradesList by remember {
    mutableStateOf(
      listOf(
        UpgradeItem("u_light", "Linterna mejorada", 2, "Mayor alcance y duración en aguas turbias.", 120, Icons.Default.FlashlightOn),
        UpgradeItem("u_shield", "Escudo de energía", 1, "Más resistencia contra ataques haywire.", 100, Icons.Default.Security),
        UpgradeItem("u_bat", "Batería extra", 1, "+50% de duración en expediciones AR.", 100, Icons.Default.BatteryChargingFull),
        UpgradeItem("u_bait", "Cebo especial avanzado", 1, "Atrae especies raras con más frecuencia.", 150, Icons.Default.VolunteerActivism)
      )
    )
  }

  val objectsList = remember {
    listOf(
      InventoryItem("flashlight", "Linterna Marina UV", "Revela peces en la oscuridad marina.", 5, Icons.Default.FlashlightOn, MarineCyan),
      InventoryItem("shield", "Escudo Súper Pez", "Te protege de embestidas de arrecife.", 3, Icons.Default.Security, MarineGold),
      InventoryItem("bait", "Cebo Artesanal Piurano", "Atrae especies raras de profundidad.", 8, Icons.Default.VolunteerActivism, MarineCoral),
      InventoryItem("battery", "Célula de Batería de Casco", "Recarga tus dispositivos y escáner.", 12, Icons.Default.BatteryChargingFull, MarineGreen),
      InventoryItem("lightning", "Rayo Eléctrico", "Aturde al pez durante la ventana de captura.", 4, Icons.Default.ElectricBolt, MarineCyan),
      InventoryItem("medal", "Medalla de Captura", "Úsala en eventos especiales de la feria.", 6, Icons.Default.MilitaryTech, MarineGold)
    )
  }

  Column(
    modifier = modifier.fillMaxSize().background(OceanDeep).testTag("inventory_screen")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().background(OceanAbyss).padding(horizontal = 16.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text("Inventario", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text("Equipo de campo · operador Fish AR", color = TextSecondary, fontSize = 11.sp)
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
          modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(OceanCard).border(1.dp, MarineGold.copy(alpha = .6f), RoundedCornerShape(16.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.MonetizationOn, null, tint = MarineGold, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(4.dp))
          Text("$pescacoins", color = MarineGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(Modifier.width(6.dp))
        IconButton(onClick = { MarineSoundEngine.playNavClick(); onOpenLab() }, modifier = Modifier.size(38.dp).testTag("open_equipment_lab")) {
          Icon(Icons.Default.Settings, "Laboratorio de equipamiento", tint = MarineCyan, modifier = Modifier.size(21.dp))
        }
      }
    }

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      listOf("Objetos", "Mejoras", "Recetas").forEach { tab ->
        val selected = activeTab == tab
        Box(
          modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (selected) MarineCyan else OceanCard).border(1.dp, if (selected) MarineCyan else MarineCyan.copy(alpha = .25f), RoundedCornerShape(12.dp)).clickable { MarineSoundEngine.playNavClick(); activeTab = tab }.padding(vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) { Text(tab, color = if (selected) OceanDeep else TextPrimary, fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold, fontSize = 12.sp) }
      }
    }

    when (activeTab) {
      "Objetos" -> LazyColumn(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(objectsList) { item ->
          Card(colors = CardDefaults.cardColors(containerColor = OceanCard), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, item.iconColor.copy(alpha = .3f)), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
              Box(Modifier.size(44.dp).clip(CircleShape).background(item.iconColor.copy(alpha = .15f)).border(1.2.dp, item.iconColor.copy(alpha = .5f), CircleShape), contentAlignment = Alignment.Center) { Icon(item.icon, item.name, tint = item.iconColor, modifier = Modifier.size(24.dp)) }
              Spacer(Modifier.width(14.dp))
              Column(Modifier.weight(1f)) { Text(item.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text(item.description, color = TextSecondary, fontSize = 11.sp, maxLines = 2) }
              Text("x${item.count}", color = TextSecondary, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
          }
        }
      }
      "Mejoras" -> LazyColumn(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Potencia linterna, escudo, batería y cebo desde aquí o abre el Laboratorio para la configuración avanzada.", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp)) }
        items(upgradesList) { upgrade ->
          Card(colors = CardDefaults.cardColors(containerColor = OceanCard), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MarineCyan.copy(alpha = .3f)), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
              Box(Modifier.size(44.dp).clip(CircleShape).background(OceanAbyss).border(1.2.dp, MarineCyan, CircleShape), contentAlignment = Alignment.Center) { Icon(upgrade.icon, null, tint = MarineCyan, modifier = Modifier.size(22.dp)) }
              Spacer(Modifier.width(12.dp))
              Column(Modifier.weight(1f)) {
                Text("${upgrade.name} · Nv. ${upgrade.level}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(upgrade.effect, color = TextSecondary, fontSize = 11.sp)
                Text("${upgrade.cost} Pescacoins", color = MarineGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
              }
              Button(onClick = { if (pescacoins >= upgrade.cost) { viewModel.addPescacoins(-upgrade.cost); MarineSoundEngine.playSuccessChime(); upgradesList = upgradesList.map { if (it.id == upgrade.id) it.copy(level = it.level + 1, cost = (it.cost * 1.5f).toInt()) else it } } }, enabled = pescacoins >= upgrade.cost, colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanDeep), shape = RoundedCornerShape(12.dp), modifier = Modifier.height(36.dp)) { Text("Mejorar", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            }
          }
        }
      }
      "Recetas" -> LazyColumn(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { RecipeCard("Ceviche de Mero Murike", "Mero fresco, limón, ají limo, cebolla, camote y choclo.", MarineGold) }
        item { RecipeCard("Sudado de Cabrilla", "Cabrilla, chicha de jora, tomate, cebolla, culantro y yuca.", MarineCyan) }
        item { RecipeCard("Caballa Norteña", "Caballa, limón, cebolla roja, yuca y camote.", MarineGreen) }
      }
    }
  }
}

@Composable
private fun RecipeCard(title: String, body: String, accent: Color) {
  Card(colors = CardDefaults.cardColors(containerColor = OceanCard), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = .4f)), modifier = Modifier.fillMaxWidth()) {
    Column(Modifier.padding(14.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Restaurant, null, tint = accent, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
      Spacer(Modifier.height(4.dp)); Text(body, color = TextSecondary, fontSize = 11.sp)
    }
  }
}
