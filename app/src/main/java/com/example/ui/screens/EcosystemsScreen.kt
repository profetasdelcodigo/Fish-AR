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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Waves
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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

data class MarineHabitat(
  val id: String,
  val name: String,
  val location: String,
  val discoveredCount: Int,
  val totalCount: Int,
  val imageRes: Int,
  val color: Color
)

@Composable
fun EcosystemsScreen(
  viewModel: MarineGameViewModel,
  modifier: Modifier = Modifier
) {
  val habitats = remember {
    listOf(
      MarineHabitat("coral", "Arrecife de Coral y Rocas", "Cabo Blanco y Peña Redonda", 12, 20, R.drawable.img_real_cabrilla_3d, MarineCyan),
      MarineHabitat("deep", "Fosa Abisal y Zona Profunda", "Fosa Oceánica de Paita", 8, 15, R.drawable.img_real_mero_3d, Color(0xFF7C4DFF)),
      MarineHabitat("mangrove", "Manglares y Estuarios", "San Pedro de Vice / Sechura", 6, 12, R.drawable.img_real_tortuga_3d, MarineGreen),
      MarineHabitat("shelf", "Plataforma Continental", "Bahía de Talara y El Ñuro", 10, 18, R.drawable.img_real_bonito_3d, MarineGold)
    )
  }

  var leagueEntered by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(OceanDeep)
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .testTag("ecosystems_screen"),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = "Ecosistemas y Liga", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 20.sp)
          Text(text = "Descubre hábitats marinos del norte peruano", color = TextSecondary, fontSize = 12.sp)
        }
        Icon(Icons.Default.Waves, contentDescription = null, tint = MarineCyan, modifier = Modifier.size(24.dp))
      }
    }

    // 2. Habitats Cards
    items(habitats) { habitat ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { MarineSoundEngine.playNavClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = OceanCard),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, habitat.color.copy(alpha = 0.4f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Habitat image thumbnail
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(OceanAbyss)
              .border(1.2.dp, habitat.color, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Image(
              painter = painterResource(id = habitat.imageRes),
              contentDescription = habitat.name,
              contentScale = ContentScale.Fit,
              modifier = Modifier.size(46.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(text = habitat.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = habitat.location, color = TextSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Especies: ${habitat.discoveredCount}/${habitat.totalCount}",
                color = habitat.color,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }
          }

          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }

    // 3. Liga de Peces Section (Exact replica from reference mockup)
    item {
      Spacer(modifier = Modifier.height(6.dp))
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = OceanCard),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MarineCyan)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "LIGA DE PECES",
            color = MarineCyan,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            letterSpacing = 1.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Shield Badge
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(OceanAbyss)
              .border(2.dp, MarineCyan, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = MarineCyan, modifier = Modifier.size(36.dp))
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Rango: Plata II",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )

          Spacer(modifier = Modifier.height(6.dp))

          // Progress Bar: 120 / 200
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Puntos de Rango", color = TextSecondary, fontSize = 10.sp)
              Text("120 / 200", color = MarineGold, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
              progress = { 120f / 200f },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = MarineCyan,
              trackColor = OceanAbyss
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              leagueEntered = true
              MarineSoundEngine.playSuccessChime()
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (leagueEntered) MarineGreen else MarineCyan,
              contentColor = OceanDeep
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
          ) {
            Text(
              text = if (leagueEntered) "¡INSCRITO EN LA LIGA!" else "Entrar a la liga",
              fontWeight = FontWeight.Black,
              fontSize = 13.sp
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(text = "Recompensas de temporada:", color = TextSecondary, fontSize = 11.sp)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MarineGold, modifier = Modifier.size(22.dp))
              Text("Poké-pez", color = TextPrimary, fontSize = 10.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = MarineCyan, modifier = Modifier.size(22.dp))
              Text("Skin exclusiva", color = TextPrimary, fontSize = 10.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.Landscape, contentDescription = null, tint = MarineGreen, modifier = Modifier.size(22.dp))
              Text("Objetos", color = TextPrimary, fontSize = 10.sp)
            }
          }
        }
      }
    }

    // 4. Footer Branding & Ocean Conservation message (from mockup bottom)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Conoce las especies  •  Cuida el mar  •  Come saludable",
          color = MarineCyan,
          fontWeight = FontWeight.SemiBold,
          fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "\"El océano nos da vida, tú puedes protegerlo.\"",
          color = Color.White.copy(alpha = 0.7f),
          fontSize = 12.sp,
          fontWeight = FontWeight.Normal
        )
      }
    }
  }
}
