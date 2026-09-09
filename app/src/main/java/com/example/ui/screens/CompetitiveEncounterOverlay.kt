package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.ArGameState
import com.example.game.EncounterPhase
import com.example.game.FairTournamentViewModel
import com.example.game.MarineGameViewModel
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MarineGold
import com.example.ui.theme.MarineGreen
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.TextPrimary

@Composable
fun CompetitiveEncounterOverlay(
  marineViewModel: MarineGameViewModel,
  tournamentViewModel: FairTournamentViewModel,
  gameState: ArGameState
) {
  val remaining by tournamentViewModel.remainingSeconds.collectAsState()
  val score by tournamentViewModel.score.collectAsState()
  val captures by tournamentViewModel.captures.collectAsState()
  val combo by tournamentViewModel.combo.collectAsState()
  val running by tournamentViewModel.running.collectAsState()
  var registeredThisEncounter by remember { mutableStateOf(false) }

  LaunchedEffect(gameState.phase) {
    if (!registeredThisEncounter && gameState.phase is EncounterPhase.Success && running) {
      tournamentViewModel.registerCapture(gameState.currentSpecies.displayName, basePoints = 100, clean = true)
      registeredThisEncounter = true
    }
  }

  LaunchedEffect(remaining, running) {
    if (running && remaining <= 0) {
      tournamentViewModel.finish()
      marineViewModel.endEncounter()
    }
  }

  Box(
    Modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 78.dp)
  ) {
    Row(
      Modifier
        .align(Alignment.TopCenter)
        .background(OceanAbyss.copy(alpha = .94f), RoundedCornerShape(16.dp))
        .border(1.2.dp, if (remaining <= 20) Color.Red else MarineGold.copy(alpha = .65f), RoundedCornerShape(16.dp))
        .padding(horizontal = 12.dp, vertical = 7.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("FERIA", color = MarineGold, fontWeight = FontWeight.Black, fontSize = 10.sp)
      Text(formatTime(remaining), color = if (remaining <= 20) Color.Red else MarineCyan, fontWeight = FontWeight.Black, fontSize = 18.sp)
      Text("$score pts", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
      Text("$captures CAP", color = MarineGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
      Text("x${multiplier(combo)}", color = MarineGold, fontWeight = FontWeight.Black, fontSize = 13.sp)
    }
  }
}

private fun multiplier(combo: Int) = when { combo >= 5 -> 3; combo >= 3 -> 2; else -> 1 }
private fun formatTime(seconds: Int) = "%d:%02d".format(seconds / 60, seconds % 60)
