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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MarineGold
import com.example.ui.theme.MarineGreen
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class InventoryItem(
  val id: String,
  val name: String,
  val description: String,
  val count: Int,
  val icon: ImageVector,
  val iconColor: Color
)

data class UpgradeItem(
  val id: String,
  val name: String,
  val level: Int,
  val effect: String,
  val cost: Int,
  val icon: ImageVector
)

@Composable
fun InventoryScreen(
  viewModel: MarineGameViewModel,
  modifier: Modifier = Modifier
) {
  val pescacoins by viewModel.pescacoins.collectAsState()
  var activeTab by remember { mutableStateOf("Objetos") }

  val objectsList = remember {
    listOf(
      InventoryItem("flashlight", "Linterna Marina UV", "Revela peces en la oscuridad marina.", 5, Icons.Default.FlashlightOn, MarineCyan),
      InventoryItem("shield", "Escudo Súper Pez", "Te protege de embestidas de arrecife.", 3, Icons.Default.Security, MarineGold),
      InventoryItem("bait", "Cebo Artesanal Piurano", "Atrae especies raras de profundidad.", 8, Icons.Default.VolunteerActivism, Color(0xFFFF5252)),
      InventoryItem("battery", "Célula de Batería de Casco", "Recarga tus dispositivos y escáner.", 12, Icons.Default.BatteryChargingFull, MarineGreen),
      InventoryItem("lightning", "Rayo Eléctrico Taser", "Aturde al pez por unos segundos para la captura.", 4, Icons.Default.ElectricBolt, MarineCyan),
      InventoryItem("medal", "Medalla de Captura San Josefina", "Úsala en torneos y eventos especiales de la feria.", 6, Icons.Default.MilitaryTech, MarineGold)
    )
  }

  var upgradesList by remember {
    mutableStateOf(
      listOf(
        UpgradeItem("u_light", "Linterna mejorada", 2, "Mayor alcance y duración en aguas turbias.", 120, Icons.Default.FlashlightOn),
        UpgradeItem("u_shield", "Escudo de energía", 1, "Más resistencia contra ataques haywire.", 100, Icons.Default.Security),
        UpgradeItem("u_bat", "Batería extra", 1, "+50% de duración en expediciones AR.", 100, Icons.Default.BatteryChargingFull),
        UpgradeItem("u_bait", "Cebo especial avanzado", 1, "Atrae especies legendarias con más frecuencia.", 150, Icons.Default.VolunteerActivism)
      )
    )
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(OceanDeep)
      .testTag("inventory_screen")
  ) {
    // 1. Header with Title, Settings, and Coins
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(OceanAbyss)
        .padding(horizontal = 16.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Inventario",
        color = TextPrimary,
        fontWeight = FontWeight.Black,
        fontSize = 20.sp
      )

      Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(OceanCard)
            .border(1.dp, MarineGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = MarineGold, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "$pescacoins", color = MarineGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
          onClick = { MarineSoundEngine.playNavClick() },
          modifier = Modifier.size(34.dp)
        ) {
          Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
      }
    }

    // 2. Tabs Selector (Objetos, Mejoras, Recetas)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      listOf("Objetos", "Mejoras", "Recetas").forEach { tab ->
        val isSelected = activeTab == tab
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MarineCyan else OceanCard)
            .border(1.dp, if (isSelected) MarineCyan else MarineCyan.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .clickable {
              MarineSoundEngine.playNavClick()
              activeTab = tab
            }
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = tab,
            color = if (isSelected) OceanDeep else TextPrimary,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
            fontSize = 12.sp
          )
        }
      }
    }

    // 3. Tab Contents
    when (activeTab) {
      "Objetos" -> {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(objectsList) { item ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = OceanCard),
              shape = RoundedCornerShape(16.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, item.iconColor.copy(alpha = 0.3f))
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Item Icon Badge
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(item.iconColor.copy(alpha = 0.15f))
                    .border(1.2.dp, item.iconColor.copy(alpha = 0.5f), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(item.icon, contentDescription = item.name, tint = item.iconColor, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(text = item.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  Text(text = item.description, color = TextSecondary, fontSize = 11.sp, maxLines = 2)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                  text = "x${item.count}",
                  color = TextSecondary,
                  fontWeight = FontWeight.Black,
                  fontSize = 14.sp
                )
              }
            }
          }
        }
      }

      "Mejoras" -> {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          item {
            Text(
              text = "Potencia tus habilidades y dispositivos de rastreo:",
              color = TextSecondary,
              fontSize = 12.sp,
              modifier = Modifier.padding(bottom = 4.dp)
            )
          }

          items(upgradesList) { upgrade ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = OceanCard),
              shape = RoundedCornerShape(16.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, MarineCyan.copy(alpha = 0.3f))
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(OceanAbyss)
                    .border(1.2.dp, MarineCyan, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(upgrade.icon, contentDescription = null, tint = MarineCyan, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(text = "${upgrade.name} (Nv. ${upgrade.level})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  Text(text = upgrade.effect, color = TextSecondary, fontSize = 11.sp)
                  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = MarineGold, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "${upgrade.cost} Pescacoins", color = MarineGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                  }
                }

                Button(
                  onClick = {
                    if (pescacoins >= upgrade.cost) {
                      viewModel.addPescacoins(-upgrade.cost)
                      MarineSoundEngine.playSuccessChime()
                      upgradesList = upgradesList.map {
                        if (it.id == upgrade.id) it.copy(level = it.level + 1, cost = (it.cost * 1.5f).toInt()) else it
                      }
                    }
                  },
                  enabled = pescacoins >= upgrade.cost,
                  colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanDeep),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.height(36.dp)
                ) {
                  Text(text = "Mejorar", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
              }
            }
          }
        }
      }

      "Recetas" -> {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = OceanCard),
              shape = RoundedCornerShape(16.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, MarineGold.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Restaurant, contentDescription = null, tint = MarineGold, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Ceviche Tradicional de Mero Murike", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Ingredientes: Mero fresco de Paita, limón criollo de Chulucanas, ají limo piurano, choclo y camote dulce.", color = TextSecondary, fontSize = 11.sp)
              }
            }
          }

          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = OceanCard),
              shape = RoundedCornerShape(16.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, MarineCyan.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Restaurant, contentDescription = null, tint = MarineCyan, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Sudado de Cabrilla a la Piurana", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Ingredientes: Cabrilla fina de Cabo Blanco, reducción de chicha de jora, tomate, cebolla, culantro y yuca.", color = TextSecondary, fontSize = 11.sp)
              }
            }
          }
        }
      }
    }
  }
}
