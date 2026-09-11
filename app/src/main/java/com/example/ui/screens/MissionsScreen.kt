package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.MarineSoundEngine
import com.example.game.MarineGameViewModel
import com.example.game.MissionState
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MarineGold
import com.example.ui.theme.MarineGreen
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MissionsScreen(
  viewModel: MarineGameViewModel,
  modifier: Modifier = Modifier
) {
  val pescacoins by viewModel.pescacoins.collectAsState()
  val missionsList by viewModel.allMissions.collectAsState()
  var activeTab by remember { mutableStateOf("Activas") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(OceanDeep)
      .testTag("missions_screen")
  ) {
    // 1. Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(OceanAbyss)
        .padding(horizontal = 16.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Misiones y eventos",
        color = TextPrimary,
        fontWeight = FontWeight.Black,
        fontSize = 20.sp
      )

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
    }

    // 2. Tab Filter: Activas, Diarias, Especiales
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      listOf("Activas", "Diarias", "Especiales").forEach { tab ->
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

    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // 3. Missions List
      items(missionsList) { mission ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = OceanCard),
          shape = RoundedCornerShape(16.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, if (mission.isCompleted && !mission.isClaimed) MarineGold else MarineCyan.copy(alpha = 0.25f))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(if (mission.isCompleted) MarineGold.copy(alpha = 0.15f) else MarineCyan.copy(alpha = 0.15f))
                  .border(1.2.dp, if (mission.isCompleted) MarineGold else MarineCyan, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  mission.icon,
                  contentDescription = null,
                  tint = if (mission.isCompleted) MarineGold else MarineCyan,
                  modifier = Modifier.size(20.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(text = mission.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "${mission.progress} / ${mission.target}", color = TextSecondary, fontSize = 11.sp)
              }

              if (mission.isCompleted && !mission.isClaimed) {
                Button(
                  onClick = {
                    viewModel.claimMission(mission.id)
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = MarineGold, contentColor = OceanDeep),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.height(34.dp)
                ) {
                  Text("Reclamar", fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
              } else if (mission.isClaimed) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = MarineGreen, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Completado", color = MarineGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
              } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = MarineGold, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(text = "+${mission.reward}", color = MarineGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
              progress = { (mission.progress.toFloat() / mission.target.toFloat()).coerceIn(0f, 1f) },
              modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
              color = if (mission.isCompleted) MarineGold else MarineCyan,
              trackColor = OceanAbyss
            )
          }
        }
      }

      // 4. Special Event Banner Card (Exact style from reference mockup)
      item {
        Spacer(modifier = Modifier.height(6.dp))
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = OceanCard),
          border = androidx.compose.foundation.BorderStroke(1.5.dp, MarineCyan)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                Brush.horizontalGradient(
                  colors = listOf(OceanAbyss, Color(0xFF0F3254))
                )
              )
              .padding(14.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "EVENTO ESPECIAL",
                  color = Color(0xFFFF5252),
                  fontWeight = FontWeight.Black,
                  fontSize = 10.sp,
                  letterSpacing = 1.sp
                )
                Text(
                  text = "La Ruta del Atún",
                  color = TextPrimary,
                  fontWeight = FontWeight.Black,
                  fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Feria Sabores del Mar • Completa misiones y consigue recompensas exclusivas.",
                  color = TextSecondary,
                  fontSize = 11.sp,
                  lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Timer, contentDescription = null, tint = MarineCyan, modifier = Modifier.size(13.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(text = "Termina en: 2d 14h", color = MarineCyan, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
              }

              Spacer(modifier = Modifier.width(10.dp))

              // 3D Fish image in circular badge
              Box(
                modifier = Modifier
                  .size(72.dp)
                  .clip(CircleShape)
                  .background(OceanAbyss)
                  .border(1.5.dp, MarineCyan, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Image(
                  painter = painterResource(id = R.drawable.img_real_bonito_3d),
                  contentDescription = "Atún del Pacífico",
                  contentScale = ContentScale.Fit,
                  modifier = Modifier.size(64.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
