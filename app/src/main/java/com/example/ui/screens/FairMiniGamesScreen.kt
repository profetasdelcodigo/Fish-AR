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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.MarineGameViewModel
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MarineGold
import com.example.ui.theme.MarineGreen
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FairMiniGamesScreen(
  viewModel: MarineGameViewModel,
  onBackToRadar: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf(0) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(OceanDeep)
      .testTag("fair_minigames_screen")
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(OceanAbyss)
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBackToRadar,
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(OceanCard)
          .testTag("minigames_back_button")
      ) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MarineCyan)
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column {
        Text(
          text = "Juegos de la Feria",
          color = MarineCyan,
          fontWeight = FontWeight.Black,
          fontSize = 18.sp
        )
        Text(
          text = "Feria 'Sabores del Mar' • San Josefina",
          color = TextSecondary,
          fontSize = 12.sp
        )
      }
    }

    // Tabs
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = OceanCard,
      contentColor = MarineCyan
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Bolt, contentDescription = null, tint = MarineCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Pesca Magnética", fontWeight = FontWeight.Bold)
          }
        }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = MarineGold, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Memotest Marino", fontWeight = FontWeight.Bold)
          }
        }
      )
    }

    if (selectedTab == 0) {
      PescaMagneticaArcade(viewModel = viewModel)
    } else {
      MemotestMarinoView()
    }
  }
}

@Composable
fun PescaMagneticaArcade(
  viewModel: MarineGameViewModel,
  modifier: Modifier = Modifier
) {
  val score by viewModel.magneticGameScore.collectAsState()
  val fishPos by viewModel.magneticFishPosition.collectAsState()
  val targetZone by viewModel.magneticTargetZone.collectAsState()
  val isActive by viewModel.magneticGameActive.collectAsState()
  var lastResultFeedback by remember { mutableStateOf<String?>(null) }

  DisposableEffect(Unit) {
    viewModel.startMagneticMiniGame()
    onDispose {
      viewModel.stopMagneticMiniGame()
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "Pesca Magnética Arcade",
      color = MarineGold,
      fontWeight = FontWeight.ExtraBold,
      fontSize = 20.sp
    )

    Text(
      text = "El clásico juego de la feria escolar: Presiona el anzuelo magnético cuando el pez ingrese al campo electromagnético verde.",
      color = TextSecondary,
      fontSize = 12.sp,
      textAlign = TextAlign.Center
    )

    // Score Card
    Card(
      colors = CardDefaults.cardColors(containerColor = OceanCard),
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(1.5.dp, MarineCyan)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("CAPTURAS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          Text("$score", color = MarineCyan, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }

        Icon(Icons.Default.Sensors, contentDescription = null, tint = MarineCyan, modifier = Modifier.size(24.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("PESCACOINS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = MarineGold, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("+${score * 20}", color = MarineGold, fontSize = 22.sp, fontWeight = FontWeight.Black)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Magnetic Basin Track
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(84.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(OceanAbyss)
        .border(2.dp, MarineCyan.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
    ) {
      // Target Green Zone
      val startFrac = targetZone.start
      val endFrac = targetZone.endInclusive
      val spanFrac = (endFrac - startFrac).coerceAtLeast(0.12f)

      Box(
        modifier = Modifier
          .fillMaxHeight()
          .fillMaxWidth(spanFrac)
          .align(Alignment.CenterStart)
          .padding(start = (startFrac * 250).dp)
          .clip(RoundedCornerShape(10.dp))
          .background(MarineGreen.copy(alpha = 0.35f))
          .border(1.5.dp, MarineGreen, RoundedCornerShape(10.dp))
      )

      // Swimming fish icon thumbnail
      Box(
        modifier = Modifier
          .align(Alignment.CenterStart)
          .padding(start = (fishPos * 270).dp)
          .size(46.dp)
          .clip(CircleShape)
          .background(OceanCard)
          .border(2.dp, MarineGold, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.fish_bonito),
          contentDescription = "Pez Nadando",
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
        )
      }
    }

    lastResultFeedback?.let { msg ->
      Text(
        text = msg,
        color = if (msg.contains("¡Excelente")) MarineGreen else Color(0xFFFF5252),
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
      )
    }

    Spacer(modifier = Modifier.weight(1f))

    // Hook Button
    Button(
      onClick = {
        val hit = viewModel.tapMagneticHook()
        lastResultFeedback = if (hit) "¡Excelente captura magnética! +20 Pescacoins" else "¡El pez esquivó el campo! Calcula mejor el tiempo."
      },
      colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanDeep),
      shape = RoundedCornerShape(18.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(58.dp)
        .testTag("magnetic_hook_tap_button")
    ) {
      Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(22.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "ACTIVAR CAMPO MAGNÉTICO",
        fontWeight = FontWeight.Black,
        fontSize = 15.sp
      )
    }
  }
}

data class MemoCardItem(
  val id: Int,
  val name: String,
  val imageRes: Int
)

@Composable
fun MemotestMarinoView(modifier: Modifier = Modifier) {
  // 6 pairs of marine species / characters from Piura fair
  val cardItems = remember {
    listOf(
      MemoCardItem(1, "Bonito", R.drawable.fish_bonito),
      MemoCardItem(1, "Bonito", R.drawable.fish_bonito),
      MemoCardItem(2, "Mero", R.drawable.fish_mero),
      MemoCardItem(2, "Mero", R.drawable.fish_mero),
      MemoCardItem(3, "Cabrilla", R.drawable.fish_cabrilla),
      MemoCardItem(3, "Cabrilla", R.drawable.fish_cabrilla),
      MemoCardItem(4, "Tortuga", R.drawable.tortuga_nuro),
      MemoCardItem(4, "Tortuga", R.drawable.tortuga_nuro),
      MemoCardItem(5, "Súper Pez", R.drawable.img_super_pez),
      MemoCardItem(5, "Súper Pez", R.drawable.img_super_pez),
      MemoCardItem(6, "Ecosistema", R.drawable.img_fair_banner),
      MemoCardItem(6, "Ecosistema", R.drawable.img_fair_banner)
    ).shuffled()
  }

  val flipped = remember { mutableStateListOf(*Array(cardItems.size) { false }) }
  val matched = remember { mutableStateListOf(*Array(cardItems.size) { false }) }
  var firstChoiceIndex by remember { mutableStateOf<Int?>(null) }
  var moves by remember { mutableStateOf(0) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Text(
      text = "Memotest de Biodiversidad Marina",
      color = MarineGold,
      fontWeight = FontWeight.Black,
      fontSize = 18.sp
    )

    Text(
      text = "Encuentra las parejas de especies de la feria escolar de San Josefina. Movimientos: $moves",
      color = TextSecondary,
      fontSize = 12.sp,
      textAlign = TextAlign.Center
    )

    LazyVerticalGrid(
      columns = GridCells.Fixed(3),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.weight(1f)
    ) {
      itemsIndexed(cardItems) { index, card ->
        val isCardFlipped = flipped[index] || matched[index]

        Card(
          modifier = Modifier
            .height(95.dp)
            .clickable(enabled = !isCardFlipped) {
              if (firstChoiceIndex == null) {
                firstChoiceIndex = index
                flipped[index] = true
              } else {
                val first = firstChoiceIndex!!
                flipped[index] = true
                moves++
                if (cardItems[first].id == cardItems[index].id) {
                  matched[first] = true
                  matched[index] = true
                  firstChoiceIndex = null
                } else {
                  firstChoiceIndex = null
                  flipped[first] = false
                  flipped[index] = false
                }
              }
            }
            .testTag("memo_card_$index"),
          colors = CardDefaults.cardColors(containerColor = if (isCardFlipped) OceanCard else OceanAbyss),
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (matched[index]) MarineGreen else if (isCardFlipped) MarineGold else MarineCyan.copy(alpha = 0.4f)
          )
        ) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isCardFlipped) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
              ) {
                Image(
                  painter = painterResource(id = card.imageRes),
                  contentDescription = card.name,
                  modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                )
                Text(
                  text = card.name,
                  color = if (matched[index]) MarineGreen else TextPrimary,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            } else {
              Icon(Icons.Default.QuestionMark, contentDescription = "Carta oculta", tint = MarineCyan)
            }
          }
        }
      }
    }

    if (matched.all { it }) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(OceanCard)
          .padding(10.dp)
      ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MarineGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "¡Felicitaciones! ¡Memotest completado en $moves movimientos!",
          color = MarineGreen,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp
        )
      }
    }
  }
}
