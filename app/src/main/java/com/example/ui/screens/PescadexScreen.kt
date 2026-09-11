package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.model.FishSpecies
import com.example.model.MarineDatabase
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MarineGold
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PescadexScreen(
  viewModel: MarineGameViewModel,
  onBackToRadar: () -> Unit,
  modifier: Modifier = Modifier
) {
  val unlockedIds by viewModel.unlockedSpeciesIds.collectAsState()
  var selectedSpecies by remember { mutableStateOf<FishSpecies?>(null) }
  var activeFilter by remember { mutableStateOf("Peces") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(OceanDeep)
      .testTag("pescadex_screen")
  ) {
    // 1. Header (Catálogo, Search Icon)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(OceanAbyss)
        .padding(horizontal = 16.dp, vertical = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = {
            MarineSoundEngine.playNavClick()
            onBackToRadar()
          },
          modifier = Modifier.size(36.dp)
        ) {
          Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MarineCyan)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Catálogo",
          color = TextPrimary,
          fontWeight = FontWeight.Black,
          fontSize = 20.sp
        )
      }

      IconButton(
        onClick = { MarineSoundEngine.playNavClick() },
        modifier = Modifier.size(36.dp)
      ) {
        Icon(Icons.Default.Search, contentDescription = "Buscar", tint = TextSecondary, modifier = Modifier.size(20.dp))
      }
    }

    // 2. Filter Pills: Peces, Álbumes, Rarezas
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      listOf("Peces", "Álbumes", "Rarezas").forEach { filter ->
        val isSelected = activeFilter == filter
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MarineCyan else OceanCard)
            .border(1.dp, if (isSelected) MarineCyan else MarineCyan.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .clickable {
              MarineSoundEngine.playNavClick()
              activeFilter = filter
            }
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = filter,
            color = if (isSelected) OceanDeep else TextPrimary,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
            fontSize = 12.sp
          )
        }
      }
    }

    // 3. Species Grid (2 Columns like Screen 4 of reference image)
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      contentPadding = PaddingValues(16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(MarineDatabase.speciesList) { species ->
        val isUnlocked = unlockedIds.contains(species.id)
        val rarityColor = Color(species.rarity.colorHex)

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked) {
              MarineSoundEngine.playNavClick()
              selectedSpecies = species
            }
            .testTag("species_card_${species.id}"),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = if (isUnlocked) OceanCard else OceanAbyss),
          border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            if (isUnlocked) rarityColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f)
          )
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Top species render thumbnail
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(OceanAbyss),
              contentAlignment = Alignment.Center
            ) {
              if (isUnlocked) {
                Image(
                  painter = painterResource(id = species.imageRes),
                  contentDescription = species.commonName,
                  contentScale = ContentScale.Fit,
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
                )
              } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(Icons.Default.Lock, contentDescription = "Bloqueado", tint = TextSecondary, modifier = Modifier.size(24.dp))
                  Spacer(modifier = Modifier.height(4.dp))
                  Text("Por descubrir", color = TextSecondary, fontSize = 10.sp)
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Species Name
            Text(
              text = if (isUnlocked) species.commonName else "???",
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Level & Rarity dot
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Nv. ${species.baseHealth / 10 + 5}",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
              )

              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(if (isUnlocked) rarityColor else Color.Gray)
              )
            }
          }
        }
      }
    }

    // 4. Species Progress Indicator (e.g. 32 / 120 especies)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(OceanAbyss)
        .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Waves, contentDescription = null, tint = MarineCyan, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${unlockedIds.size} / ${MarineDatabase.speciesList.size} especies registradas",
            color = MarineCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }

        LinearProgressIndicator(
          progress = { unlockedIds.size.toFloat() / MarineDatabase.speciesList.size.toFloat() },
          modifier = Modifier
            .width(100.dp)
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = MarineCyan,
          trackColor = OceanCard
        )
      }
    }

    // 5. Selected Species Detail Modal (Nutritional facts & Fair stand)
    val species = selectedSpecies
    if (species != null) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.85f))
          .clickable { selectedSpecies = null }
          .padding(20.dp),
        contentAlignment = Alignment.Center
      ) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = false) {},
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = OceanCard),
          border = androidx.compose.foundation.BorderStroke(1.5.dp, MarineCyan)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = species.commonName,
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
              )
              IconButton(onClick = { selectedSpecies = null }) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
              }
            }

            Text(
              text = species.scientificName,
              color = MarineCyan,
              fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Realistic 3D Image preview
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(OceanAbyss),
              contentAlignment = Alignment.Center
            ) {
              Image(
                painter = painterResource(id = species.imageRes),
                contentDescription = species.commonName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                  .fillMaxSize()
                  .padding(12.dp)
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "\"${species.quote}\"\nHábitat: ${species.habitat}",
              color = TextSecondary,
              fontSize = 12.sp,
              lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Nutrition & Fair Stand info
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(OceanAbyss)
                .padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Nutrición:", color = MarineGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(species.nutritionalBenefits.joinToString(" • "), color = TextPrimary, fontSize = 10.sp)
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("Stand Gastronómico:", color = MarineCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(species.fairStandInfo, color = TextPrimary, fontSize = 10.sp)
              }
            }
          }
        }
      }
    }
  }
}
