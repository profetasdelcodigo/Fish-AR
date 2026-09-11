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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.MarineSoundEngine
import com.example.data.FairLeaderboardEntity
import com.example.game.MarineGameViewModel
import com.example.model.GameModeType
import com.example.ui.components.ModeTutorialDialog
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MarineGold
import com.example.ui.theme.MarineGreen
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.MultiplayerState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FairMiniGamesScreen(
  viewModel: MarineGameViewModel,
  onBackToRadar: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf(0) }
  val tournamentState by viewModel.tournamentState.collectAsState()
  val fairLeaderboard by viewModel.fairLeaderboard.collectAsState(initial = emptyList())
  val coopLeaderboard by viewModel.coopLeaderboard.collectAsState(initial = emptyList())
  val pvpLeaderboard by viewModel.pvpLeaderboard.collectAsState(initial = emptyList())
  val multiplayerState by viewModel.multiplayerState.collectAsState()
  val pvpState by viewModel.pvpState.collectAsState()
  val coopState by viewModel.coopState.collectAsState()
  val activeTutorial by viewModel.activeTutorialMode.collectAsState()

  var showUsernameDialog by remember { mutableStateOf(false) }
  var inputUsername by remember { mutableStateOf("") }
  var showResetLeaderboardDialog by remember { mutableStateOf(false) }
  var showAddCustomScoreDialog by remember { mutableStateOf(false) }
  var customScoreName by remember { mutableStateOf("") }
  var customScorePoints by remember { mutableStateOf("2150") }
  var customScoreFish by remember { mutableStateOf("Bonito del Norte") }
  var customScoreCaptures by remember { mutableStateOf("8") }
  var customScoreCombo by remember { mutableStateOf("3") }
  

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(OceanDeep)
      .testTag("fair_hub_screen")
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(OceanAbyss)
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = {
          MarineSoundEngine.playNavClick()
          onBackToRadar()
        },
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(OceanCard)
          .testTag("fair_hub_back_btn")
      ) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MarineCyan)
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Centro de Modos de Juego",
          color = MarineCyan,
          fontWeight = FontWeight.Black,
          fontSize = 17.sp
        )
        Text(
          text = "Feria 'Sabores del Mar' • Piura, Perú",
          color = TextSecondary,
          fontSize = 11.5.sp
        )
      }

      // Quick Help / Tutorial button
      IconButton(
        onClick = {
          val targetMode = when (selectedTab) {
            0 -> GameModeType.TORNEO_FERIA
            1 -> GameModeType.DUELO_1VS1
            2 -> GameModeType.COOPERATIVO
            3 -> GameModeType.SOLITARIO
            else -> GameModeType.TORNEO_FERIA
          }
          viewModel.showTutorial(targetMode)
        },
        modifier = Modifier.testTag("open_tutorial_top_btn")
      ) {
        Icon(Icons.Default.HelpOutline, contentDescription = "Tutorial", tint = MarineGold)
      }
    }

    // Scrollable Tabs
    ScrollableTabRow(
      selectedTabIndex = selectedTab,
      containerColor = OceanCard,
      contentColor = MarineCyan,
      edgePadding = 12.dp,
      indicator = {}
    ) {
      val tabs = listOf(
        "🏆 Torneo 6 Min",
        "⚔️ Duelo 1 vs 1",
        "🤝 Cooperativo",
        "🧭 Solitario",
        "🏅 Tabla de Récords"
      )

      tabs.forEachIndexed { index, title ->
        val selected = selectedTab == index
        Tab(
          selected = selected,
          onClick = {
            MarineSoundEngine.playNavClick()
            selectedTab = index
          },
          text = {
            Text(
              text = title,
              color = if (selected) MarineCyan else TextSecondary,
              fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
              fontSize = 12.5.sp
            )
          }
        )
      }
    }

    // Tab Content
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(14.dp)
    ) {
      when (selectedTab) {
        0 -> TournamentTabContent(
          viewModel = viewModel,
          tournamentState = tournamentState,
          onOpenUsernameDialog = {
            viewModel.pendingGameStartMode = "FAIR"
            showUsernameDialog = true
          },
          onOpenTutorial = { viewModel.showTutorial(GameModeType.TORNEO_FERIA) }
        )
        1 -> PvpTabContent(
          viewModel = viewModel,
          multiplayerState = multiplayerState,
          pvpState = pvpState,
          onOpenUsernameDialog = {
            viewModel.pendingGameStartMode = "PVP"
            showUsernameDialog = true
          },
          onOpenTutorial = { viewModel.showTutorial(GameModeType.DUELO_1VS1) }
        )
        2 -> CoopTabContent(
          viewModel = viewModel,
          multiplayerState = multiplayerState,
          coopState = coopState,
          onOpenUsernameDialog = {
            viewModel.pendingGameStartMode = "COOP"
            showUsernameDialog = true
          },
          onOpenTutorial = { viewModel.showTutorial(GameModeType.COOPERATIVO) }
        )
        3 -> SolitaryTabContent(
          viewModel = viewModel,
          onBackToRadar = onBackToRadar,
          onOpenTutorial = { viewModel.showTutorial(GameModeType.SOLITARIO) }
        )
        4 -> LeaderboardTabContent(
          fairLeaderboard = fairLeaderboard.take(10),
          coopLeaderboard = coopLeaderboard.take(10),
          pvpLeaderboard = pvpLeaderboard.take(10),
          onClearClick = { showResetLeaderboardDialog = true },
          onAddCustomScoreClick = { showAddCustomScoreDialog = true },
          onPlayTournamentClick = {
            selectedTab = 0
            showUsernameDialog = true
          },
          onReseedClick = {
            viewModel.seedFairLeaderboard()
          }
        )
      }
    }
  }

  // Add Custom Score / Capture Dialog to Room
  if (showAddCustomScoreDialog) {
    AlertDialog(
      onDismissRequest = { showAddCustomScoreDialog = false },
      containerColor = OceanCard,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Star, contentDescription = null, tint = MarineGold)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Registrar Puntaje en Feria (Room)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            "Registra un puntaje de captura directamente en la Base de Datos Room para competir en el Top 10 oficial:",
            color = TextSecondary,
            fontSize = 12.sp
          )
          OutlinedTextField(
            value = customScoreName,
            onValueChange = { customScoreName = it },
            label = { Text("Nombre o Apodo") },
            placeholder = { Text("Ej: Pescador_Mancora") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("custom_score_name_input")
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = customScorePoints,
              onValueChange = { customScorePoints = it },
              label = { Text("Puntaje (pts)") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("custom_score_points_input")
            )
            OutlinedTextField(
              value = customScoreCaptures,
              onValueChange = { customScoreCaptures = it },
              label = { Text("Capturas") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
          }
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = customScoreFish,
              onValueChange = { customScoreFish = it },
              label = { Text("Mejor Pez") },
              singleLine = true,
              modifier = Modifier.weight(1.2f)
            )
            OutlinedTextField(
              value = customScoreCombo,
              onValueChange = { customScoreCombo = it },
              label = { Text("Max Combo") },
              singleLine = true,
              modifier = Modifier.weight(0.8f)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val scoreVal = customScorePoints.toIntOrNull() ?: 1000
            val capturesVal = customScoreCaptures.toIntOrNull() ?: 5
            val comboVal = customScoreCombo.toIntOrNull() ?: 2
            val nameVal = customScoreName.trim().ifEmpty { "Pescador_Invitado" }
            val fishVal = customScoreFish.trim().ifEmpty { "Bonito del Norte" }
            viewModel.recordFairScoreManual(
              username = nameVal,
              score = scoreVal,
              captures = capturesVal,
              bestSpecies = fishVal,
              maxCombo = comboVal
            )
            MarineSoundEngine.playSuccessChime()
            showAddCustomScoreDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanAbyss),
          modifier = Modifier.testTag("submit_custom_score_btn")
        ) {
          Text("GUARDAR EN ROOM", fontWeight = FontWeight.Black)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddCustomScoreDialog = false }) {
          Text("Cancelar", color = TextSecondary)
        }
      }
    )
  }

  // Active Tutorial Dialog
  if (activeTutorial != null) {
    ModeTutorialDialog(
      modeType = activeTutorial!!,
      onDismiss = { viewModel.dismissTutorial() },
      onStartMode = {
        viewModel.dismissTutorial()
        when (activeTutorial) {
          GameModeType.TORNEO_FERIA -> {
            selectedTab = 0
            showUsernameDialog = true
          }
          GameModeType.DUELO_1VS1 -> selectedTab = 1
          GameModeType.COOPERATIVO -> selectedTab = 2
          GameModeType.SOLITARIO -> onBackToRadar()
          null -> {}
        }
      }
    )
  }
  
  if (showUsernameDialog) {
    AlertDialog(
      onDismissRequest = { showUsernameDialog = false },
      containerColor = OceanCard,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Star, contentDescription = null, tint = MarineGold)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Registro de Jugador", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      },
      text = {
        Column {
          Text(
            "Ingresa tu Username o Apodo para registrar tu puntaje en la Tabla Oficial (6:00 minutos de juego):",
            color = TextSecondary,
            fontSize = 12.5.sp
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = inputUsername,
            onValueChange = { inputUsername = it },
            placeholder = { Text("Ej: Pescador_Piura", color = TextSecondary.copy(alpha = 0.6f)) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = MarineCyan,
              unfocusedBorderColor = MarineCyan.copy(alpha = 0.4f),
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("tournament_username_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            MarineSoundEngine.playNavClick()
            showUsernameDialog = false
            when (viewModel.pendingGameStartMode) {
              "FAIR" -> viewModel.startFairTournament(inputUsername)
              "COOP" -> {
                viewModel.startFairTournament(inputUsername) // to set username
                viewModel.startCoopMatch()
              }
              "PVP" -> {
                viewModel.startFairTournament(inputUsername) // to set username
                viewModel.startPvpMatch()
              }
            }
          },
          enabled = inputUsername.isNotBlank(),
          colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanAbyss),
          modifier = Modifier.testTag("confirm_tournament_start_btn")
        ) {
          Text("¡INICIAR!", fontWeight = FontWeight.Black)
        }
      },
      dismissButton = {
        TextButton(onClick = { showUsernameDialog = false }) {
          Text("Cancelar", color = TextSecondary)
        }
      }
    )
  }

  // Clear Leaderboard Confirmation Dialog
  if (showResetLeaderboardDialog) {
    AlertDialog(
      onDismissRequest = { showResetLeaderboardDialog = false },
      containerColor = OceanCard,
      title = { Text("¿Reiniciar Tabla de Récords?", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Text("Esta acción borrará los puntajes almacenados en la base de datos local de la feria.", color = TextSecondary, fontSize = 13.sp)
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.clearFairLeaderboard()
            showResetLeaderboardDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
        ) {
          Text("Borrar Récords")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetLeaderboardDialog = false }) {
          Text("Cancelar", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
private fun TournamentTabContent(
  viewModel: MarineGameViewModel,
  tournamentState: com.example.game.TournamentState,
  onOpenUsernameDialog: () -> Unit,
  onOpenTutorial: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OceanCard),
        shape = RoundedCornerShape(18.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("🏆 MODO COMPETITIVO DE FERIA", color = MarineGold, fontWeight = FontWeight.Black, fontSize = 14.sp)
              Text("Contrarreloj oficial de 6 Minutos", color = TextSecondary, fontSize = 11.5.sp)
            }
            IconButton(onClick = onOpenTutorial) {
              Icon(Icons.Default.Info, contentDescription = "Tutorial", tint = MarineCyan)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          if (tournamentState.isActive) {
            // Live Tournament HUD
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(OceanAbyss)
                .border(1.5.dp, MarineCyan, RoundedCornerShape(14.dp))
                .padding(14.dp)
            ) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("👤 ${tournamentState.username}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                  val min = tournamentState.remainingSeconds / 60
                  val sec = tournamentState.remainingSeconds % 60
                  Text(
                    String.format(Locale.getDefault(), "⏱️ %02d:%02d", min, sec),
                    color = if (tournamentState.remainingSeconds <= 30) Color(0xFFFF5252) else MarineCyan,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceAround
                ) {
                  MetricBadge(label = "PUNTAJE", value = "${tournamentState.score} pts", color = MarineGold)
                  MetricBadge(label = "PECES", value = "${tournamentState.fishesCaught}", color = MarineGreen)
                  MetricBadge(label = "COMBO", value = "x${tournamentState.currentCombo}", color = MarineCyan)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                  onClick = { viewModel.spawnNextFishInMatch() },
                  modifier = Modifier.fillMaxWidth(),
                  colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanAbyss),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Icon(Icons.Default.Waves, contentDescription = null)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("¡BUSCAR SIGUIENTE PEZ!", fontWeight = FontWeight.Black)
                }
              }
            }
          } else if (tournamentState.isFinished) {
            // Finished Summary Card
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(OceanAbyss)
                .border(1.5.dp, MarineGold, RoundedCornerShape(14.dp))
                .padding(14.dp)
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎉 ¡TORNEO COMPLETADO!", color = MarineGold, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Participante: ${tournamentState.username}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceAround
                ) {
                  MetricBadge(label = "PUNTAJE FINAL", value = "${tournamentState.score} pts", color = MarineGold)
                  MetricBadge(label = "CAPTURAS", value = "${tournamentState.fishesCaught}", color = MarineGreen)
                  MetricBadge(label = "MAX COMBO", value = "x${tournamentState.maxCombo}", color = MarineCyan)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("✅ Récord grabado en la Base de Datos Room", color = MarineGreen, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                  onClick = onOpenUsernameDialog,
                  colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanAbyss),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text("NUEVA PARTIDA DE 6 MIN", fontWeight = FontWeight.Black)
                }
              }
            }
          } else {
            // Idle State: Start Button
            Text(
              "Demuestra tu destreza pesquera en la feria durante 6 minutos consecutivos. Encadena capturas con Choque Eléctrico para batir el récord local.",
              color = TextSecondary,
              fontSize = 12.5.sp,
              lineHeight = 17.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
              onClick = onOpenUsernameDialog,
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("start_tournament_mode_btn"),
              colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanAbyss),
              shape = RoundedCornerShape(14.dp)
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("REGISTRARSE Y JUGAR (6 MIN)", fontWeight = FontWeight.Black, fontSize = 13.5.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PvpTabContent(onOpenUsernameDialog: () -> Unit,
  viewModel: MarineGameViewModel,
  multiplayerState: MultiplayerState,
  pvpState: com.example.game.PvpState,
  onOpenTutorial: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OceanCard),
        shape = RoundedCornerShape(18.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("⚔️ MODO DUELO 1 VS 1", color = MarineCyan, fontWeight = FontWeight.Black, fontSize = 14.sp)
              Text("Partida Competitiva a 3 Minutos • Puntos por Captura", color = TextSecondary, fontSize = 11.5.sp)
            }
            IconButton(onClick = onOpenTutorial) {
              Icon(Icons.Default.Info, contentDescription = "Tutorial", tint = MarineCyan)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Bluetooth Connection Status
          BluetoothStatusBar(
            viewModel = viewModel,
            state = multiplayerState,
            onHost = { viewModel.multiplayerManager.startHostRoom("PescActivate-P1") },
            onQuickDirect = { viewModel.multiplayerManager.simulateConnect("Rival Celular 2", isHost = true) },
            onDisconnect = { viewModel.multiplayerManager.disconnect() }
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Match Active vs Idle State
          if (pvpState.isMatchActive) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(OceanAbyss)
                .border(1.5.dp, MarineCyan, RoundedCornerShape(14.dp))
                .padding(14.dp)
            ) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("⚔️ DUELO EN CURSO", color = MarineGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  val min = pvpState.remainingSeconds / 60
                  val sec = pvpState.remainingSeconds % 60
                  Text(
                    String.format(Locale.getDefault(), "⏱️ %02d:%02d", min, sec),
                    color = if (pvpState.remainingSeconds <= 30) Color(0xFFFF5252) else MarineCyan,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column {
                    Text("TU PUNTAJE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${pvpState.myScore} pts", color = MarineCyan, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text("Peces: ${pvpState.myFishesCaught} • Combo: x${pvpState.myCombo}", color = TextSecondary, fontSize = 11.sp)
                  }
                  Column(horizontalAlignment = Alignment.End) {
                    Text("RIVAL (${pvpState.opponentUsername})", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${pvpState.opponentScore} pts", color = Color(0xFFFF7043), fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text("Peces: ${pvpState.opponentFishes} • Combo: x${pvpState.opponentCombo}", color = TextSecondary, fontSize = 11.sp)
                  }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                  onClick = { viewModel.spawnNextFishInMatch() },
                  modifier = Modifier.fillMaxWidth(),
                  colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanAbyss),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Icon(Icons.Default.Bolt, contentDescription = null)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("¡PESCAR SIGUIENTE PEZ DE DUELO!", fontWeight = FontWeight.Black)
                }
              }
            }
          } else if (pvpState.isFinished) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(OceanAbyss)
                .border(1.5.dp, MarineGold, RoundedCornerShape(14.dp))
                .padding(14.dp)
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(pvpState.winnerMessage ?: "¡DUELO FINALIZADO!", color = MarineGold, fontWeight = FontWeight.Black, fontSize = 15.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tu puntaje: ${pvpState.myScore} pts (${pvpState.myFishesCaught} peces)", color = TextPrimary, fontSize = 13.sp)
                Text("Rival: ${pvpState.opponentScore} pts (${pvpState.opponentFishes} peces)", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                  onClick = onOpenUsernameDialog,
                  modifier = Modifier.fillMaxWidth(),
                  colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanAbyss),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Text("NUEVA PARTIDA DE DUELO 1 VS 1", fontWeight = FontWeight.Black)
                }
              }
            }
          } else {
            Button(
              onClick = onOpenUsernameDialog,
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanAbyss),
              shape = RoundedCornerShape(14.dp)
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("¡INICIAR PARTIDA DE DUELO 1 VS 1! (6 MIN)", fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Sabotage Trigger Controls
          Text("🌪️ SABOTAJES MARINOS DISPONIBLES (${pvpState.mySabotagesAvailable})", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { viewModel.sendPvPSabotage("current") },
              modifier = Modifier.weight(1f),
              enabled = pvpState.isMatchActive && pvpState.mySabotagesAvailable > 0,
              colors = ButtonDefaults.buttonColors(containerColor = OceanAbyss),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("🌊 Corriente Marina", fontSize = 11.sp, color = MarineCyan)
            }
            Button(
              onClick = { viewModel.sendPvPSabotage("fog") },
              modifier = Modifier.weight(1f),
              enabled = pvpState.isMatchActive && pvpState.mySabotagesAvailable > 0,
              colors = ButtonDefaults.buttonColors(containerColor = OceanAbyss),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("🌫️ Niebla Oscura", fontSize = 11.sp, color = MarineCyan)
            }
          }

          if (pvpState.activeSabotageOnPlayer != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              "⚠️ ¡SABOTAJE ACTIVO EN TU PANTALLA: ${pvpState.activeSabotageOnPlayer?.uppercase()}! (${pvpState.sabotageSecondsRemaining}s)",
              color = Color(0xFFFF5252),
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CoopTabContent(onOpenUsernameDialog: () -> Unit,
  viewModel: MarineGameViewModel,
  multiplayerState: MultiplayerState,
  coopState: com.example.game.CoopState,
  onOpenTutorial: () -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OceanCard),
        shape = RoundedCornerShape(18.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("🤝 MODO COOPERATIVO DÚO", color = MarineGreen, fontWeight = FontWeight.Black, fontSize = 14.sp)
              Text("2 Celulares en la Misma Bahía • Casco Submarino Compartido", color = TextSecondary, fontSize = 11.5.sp)
            }
            IconButton(onClick = onOpenTutorial) {
              Icon(Icons.Default.Info, contentDescription = "Tutorial", tint = MarineCyan)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Bluetooth Connection Status
          BluetoothStatusBar(
            viewModel = viewModel,
            state = multiplayerState,
            onHost = { viewModel.multiplayerManager.startHostRoom("PescActivate-Coop") },
            onQuickDirect = { viewModel.multiplayerManager.simulateConnect("Compañero Celular 2", isHost = true) },
            onDisconnect = { viewModel.multiplayerManager.disconnect() }
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Role Selection Cards
          Text("🛠️ SELECCIONA TU ROL EN EL SUMERGIBLE", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val isShock = coopState.assignedRole == "Operador de Choque"
            Card(
              modifier = Modifier
                .weight(1f)
                .clickable { viewModel.setCoopRole("Operador de Choque") }
                .border(1.2.dp, if (isShock) MarineCyan else Color.Transparent, RoundedCornerShape(12.dp)),
              colors = CardDefaults.cardColors(containerColor = if (isShock) OceanAbyss else OceanCard)
            ) {
              Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = MarineCyan)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Operador de Choque", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                Text("Aturde al pez", color = TextSecondary, fontSize = 9.5.sp)
              }
            }

            val isNet = coopState.assignedRole == "Operador de Red"
            Card(
              modifier = Modifier
                .weight(1f)
                .clickable { viewModel.setCoopRole("Operador de Red") }
                .border(1.2.dp, if (isNet) MarineGreen else Color.Transparent, RoundedCornerShape(12.dp)),
              colors = CardDefaults.cardColors(containerColor = if (isNet) OceanAbyss else OceanCard)
            ) {
              Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = MarineGreen)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Operador de Red", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                Text("Despliega señuelos", color = TextSecondary, fontSize = 9.5.sp)
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Mission Active vs Idle State
          if (coopState.isMissionActive) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(OceanAbyss)
                .border(1.5.dp, MarineGreen, RoundedCornerShape(14.dp))
                .padding(14.dp)
            ) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("🤝 MISIÓN DÚO EN CURSO", color = MarineGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  val min = coopState.remainingSeconds / 60
                  val sec = coopState.remainingSeconds % 60
                  Text(
                    String.format(Locale.getDefault(), "⏱️ %02d:%02d", min, sec),
                    color = if (coopState.remainingSeconds <= 30) Color(0xFFFF5252) else MarineCyan,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text("🛡️ Casco Compartido", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  Text("${coopState.sharedHullPercent}%", color = if (coopState.sharedHullPercent > 50) MarineGreen else Color(0xFFFF5252), fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Puntaje Equipo: ${coopState.teamScore} pts", color = MarineGold, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                  Text("Capturas: ${coopState.teamFishesCaught}", color = MarineCyan, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Último evento: ${coopState.recentTeamEvent}", color = TextSecondary, fontSize = 11.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                  onClick = { viewModel.spawnNextFishInMatch() },
                  modifier = Modifier.fillMaxWidth(),
                  colors = ButtonDefaults.buttonColors(containerColor = MarineGreen, contentColor = OceanAbyss),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Icon(Icons.Default.Waves, contentDescription = null)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("¡BUSCAR CRIATURA COOPERATIVA!", fontWeight = FontWeight.Black)
                }
              }
            }
          } else if (coopState.isFinished) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(OceanAbyss)
                .border(1.5.dp, MarineGold, RoundedCornerShape(14.dp))
                .padding(14.dp)
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎉 ¡MISIÓN COOPERATIVA FINALIZADA!", color = MarineGold, fontWeight = FontWeight.Black, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Puntaje total del equipo: ${coopState.teamScore} pts", color = TextPrimary, fontSize = 13.sp)
                Text("Capturas Dúo logradas: ${coopState.teamFishesCaught}", color = MarineCyan, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                  onClick = onOpenUsernameDialog,
                  modifier = Modifier.fillMaxWidth(),
                  colors = ButtonDefaults.buttonColors(containerColor = MarineGreen, contentColor = OceanAbyss),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Text("NUEVA MISIÓN COOPERATIVA", fontWeight = FontWeight.Black)
                }
              }
            }
          } else {
            Button(
              onClick = onOpenUsernameDialog,
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
              colors = ButtonDefaults.buttonColors(containerColor = MarineGreen, contentColor = OceanAbyss),
              shape = RoundedCornerShape(14.dp)
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("¡INICIAR MISIÓN COOPERATIVA! (6 MIN)", fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Sinergy Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { viewModel.triggerCoopAction("EMP_ASSIST") },
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanAbyss),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("⚡ Choque EMP", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            Button(
              onClick = { viewModel.triggerCoopAction("DEPLOY_NET") },
              modifier = Modifier.weight(1f),
              colors = ButtonDefaults.buttonColors(containerColor = MarineGreen, contentColor = OceanAbyss),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("🕸️ Red Magnética", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SolitaryTabContent(
  viewModel: MarineGameViewModel,
  onBackToRadar: () -> Unit,
  onOpenTutorial: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = OceanCard),
    shape = RoundedCornerShape(18.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("🧭 MODO SOLITARIO LIBRE", color = MarineCyan, fontWeight = FontWeight.Black, fontSize = 14.sp)
          Text("Expedición Individual por Puertos Costeros", color = TextSecondary, fontSize = 11.5.sp)
        }
        IconButton(onClick = onOpenTutorial) {
          Icon(Icons.Default.Info, contentDescription = "Tutorial", tint = MarineCyan)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        "Navega libremente por Bahía de Máncora, El Ñuro, Cabo Blanco, Puerto de Paita y Callao. Usa el radar sonar y sumérgete al encuentro en 3D para registrar peces en tu Pescadex.",
        color = TextSecondary,
        fontSize = 12.5.sp,
        lineHeight = 17.sp
      )

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = {
          MarineSoundEngine.playNavClick()
          onBackToRadar()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(46.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanAbyss),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.Explore, contentDescription = null)
        Spacer(modifier = Modifier.width(6.dp))
        Text("IR AL RADAR COSTEÑO", fontWeight = FontWeight.Black)
      }
    }
  }
}

@Composable
private fun LeaderboardTabContent(
  fairLeaderboard: List<FairLeaderboardEntity>,
  coopLeaderboard: List<FairLeaderboardEntity>,
  pvpLeaderboard: List<FairLeaderboardEntity>,
  onClearClick: () -> Unit,
  onAddCustomScoreClick: () -> Unit,
  onPlayTournamentClick: () -> Unit,
  onReseedClick: () -> Unit
) {
  var selectedSubTab by remember { mutableStateOf(0) }
  val activeLeaderboard = when (selectedSubTab) {
    0 -> fairLeaderboard
    1 -> coopLeaderboard
    2 -> pvpLeaderboard
    else -> fairLeaderboard
  }
  
  val dateFormat = remember { SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()) }
  val topScore = activeLeaderboard.firstOrNull()?.score ?: 0
  val avgScore = if (activeLeaderboard.isNotEmpty()) activeLeaderboard.sumOf { it.score } / activeLeaderboard.size else 0
  val maxComboOverall = activeLeaderboard.maxOfOrNull { it.maxCombo } ?: 0

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("leaderboard_screen"),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      ScrollableTabRow(
        selectedTabIndex = selectedSubTab,
        containerColor = OceanCard,
        contentColor = MarineCyan,
        edgePadding = 8.dp,
        indicator = {}
      ) {
        listOf("🏆 Torneo Feria", "🤝 Cooperativo", "⚔️ Duelo 1v1").forEachIndexed { index, title ->
          val isSelected = selectedSubTab == index
          Tab(
            selected = isSelected,
            onClick = {
              MarineSoundEngine.playNavClick()
              selectedSubTab = index
            },
            text = { Text(title, color = if (isSelected) MarineCyan else TextSecondary, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
          )
        }
      }
    }

    // Header & Summary Stats
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OceanCard),
        shape = RoundedCornerShape(18.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = MarineGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  "TABLA DE CLASIFICACIÓN • TOP 10",
                  color = MarineGold,
                  fontWeight = FontWeight.Black,
                  fontSize = 14.sp
                )
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                "Persistencia Local Room Database • Feria 'Sabores del Mar'",
                color = TextSecondary,
                fontSize = 11.sp
              )
            }

            Row {
              if (activeLeaderboard.isEmpty()) {
                IconButton(onClick = onReseedClick, modifier = Modifier.testTag("reseed_leaderboard_btn")) {
                  Icon(Icons.Default.Refresh, contentDescription = "Restaurar", tint = MarineCyan)
                }
              } else {
                IconButton(onClick = onClearClick, modifier = Modifier.testTag("clear_leaderboard_btn")) {
                  Icon(Icons.Default.Delete, contentDescription = "Reiniciar", tint = Color(0xFFE53935))
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Summary Badges
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(OceanAbyss)
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            MetricBadge(label = "RÉCORD #1", value = "$topScore pts", color = MarineGold)
            MetricBadge(label = "TOP 10 PROMEDIO", value = "$avgScore pts", color = MarineCyan)
            MetricBadge(label = "MÁX COMBO", value = "x$maxComboOverall", color = MarineGreen)
            MetricBadge(label = "REGISTROS", value = "${activeLeaderboard.size}", color = TextPrimary)
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Quick Action Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = onPlayTournamentClick,
              modifier = Modifier.weight(1.3f).testTag("play_tournament_from_leaderboard_btn"),
              colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanAbyss),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("¡COMPETIR (6 MIN)!", fontWeight = FontWeight.Black, fontSize = 11.sp)
            }

            OutlinedButton(
              onClick = onAddCustomScoreClick,
              modifier = Modifier.weight(1f).testTag("add_custom_score_btn"),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = MarineGold),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Registrar", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
          }
        }
      }
    }

    if (activeLeaderboard.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(OceanCard),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Default.Star, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("No hay registros en la tabla de clasificación.", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            Text("Juega una partida de 6 min o restaura los récords oficiales.", color = TextSecondary, fontSize = 11.5.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(14.dp))
            Button(
              onClick = onReseedClick,
              colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanAbyss)
            ) {
              Text("Restaurar Récords de la Feria", fontWeight = FontWeight.Black)
            }
          }
        }
      }
    } else {
      // Top 3 Podium Cards Banner (if at least 3 entries)
      if (activeLeaderboard.size >= 3) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = OceanCard.copy(alpha = 0.85f)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MarineGold.copy(alpha = 0.35f))
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                "👑 PODIO DE CAMPEONES DE LA FERIA",
                color = MarineGold,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
              ) {
                // 2nd Place (Silver)
                PodiumColumn(
                  rank = 2,
                  entry = activeLeaderboard[1],
                  badgeColor = Color(0xFFD1D5DB),
                  modifier = Modifier.weight(1f)
                )

                // 1st Place (Gold - Taller)
                PodiumColumn(
                  rank = 1,
                  entry = activeLeaderboard[0],
                  badgeColor = MarineGold,
                  modifier = Modifier.weight(1.15f)
                )

                // 3rd Place (Bronze)
                PodiumColumn(
                  rank = 3,
                  entry = activeLeaderboard[2],
                  badgeColor = Color(0xFFCD7F32),
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }
        }
      }

      // Complete Top 10 Ranking List
      itemsIndexed(activeLeaderboard) { index, entry ->
        val rank = index + 1
        val isTop3 = rank <= 3
        val rankBadgeColor = when (rank) {
          1 -> MarineGold
          2 -> Color(0xFFD1D5DB) // Silver
          3 -> Color(0xFFCD7F32) // Bronze
          else -> MarineCyan
        }

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("activeLeaderboard_entry_$rank"),
          colors = CardDefaults.cardColors(
            containerColor = if (rank == 1) OceanCard else OceanCard.copy(alpha = 0.8f)
          ),
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(
            width = if (rank == 1) 1.5.dp else 1.dp,
            color = if (rank == 1) MarineGold else rankBadgeColor.copy(alpha = 0.25f)
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Rank Number / Medal
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(rankBadgeColor.copy(alpha = 0.15f))
                .border(1.2.dp, rankBadgeColor, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = when (rank) {
                  1 -> "🥇"
                  2 -> "🥈"
                  3 -> "🥉"
                  else -> "#$rank"
                },
                fontSize = if (isTop3) 17.sp else 12.sp,
                fontWeight = FontWeight.Black,
                color = rankBadgeColor
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Player & Details
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = entry.username,
                  color = TextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.5.sp,
                  modifier = Modifier.weight(1f, fill = false)
                )
                if (rank == 1) {
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("👑", fontSize = 11.sp)
                }
              }

              Spacer(modifier = Modifier.height(2.dp))

              Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "🎣 ${entry.fishesCaught} capturas",
                  color = MarineGreen,
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Medium
                )
                Text("•", color = TextSecondary, fontSize = 9.sp)
                Text(
                  text = "⚡ x${entry.maxCombo}",
                  color = MarineCyan,
                  fontSize = 10.5.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              Spacer(modifier = Modifier.height(2.dp))

              Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "🐟 ${entry.bestFishName}",
                  color = TextSecondary,
                  fontSize = 10.sp
                )
                Text("•", color = TextSecondary, fontSize = 9.sp)
                val dateStr = try {
                  dateFormat.format(Date(entry.createdAtEpochMs))
                } catch (_: Exception) {
                  "Hoy"
                }
                Text(
                  text = dateStr,
                  color = TextSecondary.copy(alpha = 0.7f),
                  fontSize = 9.5.sp
                )
              }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Score Badge
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "${entry.score}",
                color = rankBadgeColor,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp
              )
              Text("puntos", color = TextSecondary, fontSize = 9.5.sp, fontWeight = FontWeight.Medium)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PodiumColumn(
  rank: Int,
  entry: FairLeaderboardEntity,
  badgeColor: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(OceanAbyss)
      .border(1.dp, badgeColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
      .padding(horizontal = 6.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = when (rank) {
          1 -> "🥇 1° LUGAR"
          2 -> "🥈 2° LUGAR"
          else -> "🥉 3° LUGAR"
        },
        color = badgeColor,
        fontWeight = FontWeight.Black,
        fontSize = 9.5.sp
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = entry.username,
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 10.5.sp,
        maxLines = 1,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "${entry.score} pts",
        color = badgeColor,
        fontWeight = FontWeight.Black,
        fontSize = 13.sp
      )
      Text(
        text = "${entry.fishesCaught} peces",
        color = TextSecondary,
        fontSize = 9.sp
      )
    }
  }
}

@Composable
private fun BluetoothStatusBar(
  viewModel: MarineGameViewModel,
  state: MultiplayerState,
  onHost: () -> Unit,
  onQuickDirect: () -> Unit,
  onDisconnect: () -> Unit
) {
  var showPairedDevicesDialog by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(OceanAbyss)
      .padding(12.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (state is MultiplayerState.Connected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
            contentDescription = null,
            tint = if (state is MultiplayerState.Connected) MarineGreen else MarineCyan,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = when (state) {
              is MultiplayerState.Connected -> "¡Conectado con ${state.peerName}!"
              is MultiplayerState.Hosting -> "Sala creada: ${state.roomName}..."
              is MultiplayerState.Connecting -> "Conectando con ${state.deviceName}..."
              is MultiplayerState.Error -> state.message
              is MultiplayerState.Disconnected -> "Bluetooth / BLE Desconectado"
            },
            color = if (state is MultiplayerState.Connected) MarineGreen else TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }

        if (state is MultiplayerState.Connected) {
          TextButton(onClick = onDisconnect) {
            Text("Desconectar", color = Color(0xFFFF5252), fontSize = 11.sp)
          }
        }
      }

      if (state is MultiplayerState.Disconnected) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Button(
            onClick = onHost,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = OceanCard),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Crear Sala", fontSize = 10.sp, color = MarineCyan)
          }

          Button(
            onClick = { showPairedDevicesDialog = true },
            modifier = Modifier.weight(1.1f),
            colors = ButtonDefaults.buttonColors(containerColor = OceanCard),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Escanear BLE", fontSize = 10.sp, color = MarineGold)
          }

          Button(
            onClick = onQuickDirect,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = OceanCard),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Enlace 2-Cel", fontSize = 10.sp, color = MarineGreen)
          }
        }
      }
    }
  }

  if (showPairedDevicesDialog) {
    val pairedDevices = remember { viewModel.multiplayerManager.getPairedDevices() }
    AlertDialog(
      onDismissRequest = { showPairedDevicesDialog = false },
      containerColor = OceanCard,
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Bluetooth, contentDescription = null, tint = MarineCyan)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Dispositivos Bluetooth Vinculados", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
      },
      text = {
        Column {
          Text(
            "Selecciona el celular emparejado para conectar el servicio de datos en tiempo real:",
            color = TextSecondary,
            fontSize = 12.sp
          )
          Spacer(modifier = Modifier.height(10.dp))
          if (pairedDevices.isEmpty()) {
            Text(
              "No se encontraron dispositivos Bluetooth emparejados. Vincula los celulares desde los Ajustes de Bluetooth de Android o usa el 'Enlace 2-Cel' para conexión directa.",
              color = TextSecondary,
              fontSize = 11.5.sp,
              lineHeight = 16.sp
            )
          } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              itemsIndexed(pairedDevices) { _, (name, address) ->
                Card(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      viewModel.multiplayerManager.connectToDevice(address, name)
                      showPairedDevicesDialog = false
                    },
                  colors = CardDefaults.cardColors(containerColor = OceanAbyss),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                      Text(address, color = TextSecondary, fontSize = 10.sp)
                    }
                    Text("CONECTAR", color = MarineCyan, fontWeight = FontWeight.Black, fontSize = 11.sp)
                  }
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showPairedDevicesDialog = false }) {
          Text("Cerrar", color = TextSecondary)
        }
      }
    )
  }
}

@Composable
private fun MetricBadge(label: String, value: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(label, color = TextSecondary, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
    Text(value, color = color, fontSize = 15.sp, fontWeight = FontWeight.Black)
  }
}
