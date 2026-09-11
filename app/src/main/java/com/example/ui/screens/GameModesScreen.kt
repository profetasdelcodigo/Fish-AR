package com.example.ui.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.FairTournamentViewModel
import com.example.game.MarineGameViewModel
import com.example.multiplayer.MarineMultiplayerManager
import com.example.multiplayer.MarinePeerMessage
import com.example.multiplayer.MarinePeerState
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MarineGold
import com.example.ui.theme.MarineGreen
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private enum class FairMode(val title: String, val subtitle: String, val icon: @Composable () -> Unit) {
  COMPETITIVE("Competitivo de Feria", "6:00 · récord oficial · combos", { Icon(Icons.Default.Timer, null, tint = MarineGold) }),
  DUEL("Duelo 1 vs 1", "Bluetooth P2P · cuadrantes separados", { Icon(Icons.Default.Groups, null, tint = MarineCyan) }),
  COOP("Cooperativo Dúo", "Bahía compartida · casco y roles", { Icon(Icons.Default.Map, null, tint = MarineGreen) }),
  SOLO("Solitario Libre", "Exploración · GPS · Pescadex", { Icon(Icons.Default.Person, null, tint = MarineCyan) })
}

@Composable
fun GameModesScreen(
  marineViewModel: MarineGameViewModel,
  tournamentViewModel: FairTournamentViewModel,
  onBackToRadar: () -> Unit,
  onStartEncounter: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedMode by remember { mutableStateOf(FairMode.COMPETITIVE) }
  var showTutorial by remember { mutableStateOf(false) }
  var showUsername by remember { mutableStateOf(false) }
  var showLeaderboard by remember { mutableStateOf(false) }
  var usernameDraft by remember { mutableStateOf("") }

  val context = LocalContext.current
  val multiplayer = remember { MarineMultiplayerManager(context) }
  val peerState by multiplayer.state.collectAsState()
  val peerMessage by multiplayer.messages.collectAsState()
  var selectedPeer by remember { mutableStateOf<BluetoothDevice?>(null) }
  var peerScore by remember { mutableStateOf(0) }
  var peerCaptures by remember { mutableStateOf(0) }
  var peerCombo by remember { mutableStateOf(0) }
  var sabotageText by remember { mutableStateOf("") }

  DisposableEffect(Unit) { onDispose { multiplayer.close() } }

  androidx.compose.runtime.LaunchedEffect(peerMessage) {
    when (val m = peerMessage) {
      null -> Unit
      else -> when (m.type) {
        "SCORE" -> peerScore = m.value
        "CAPTURE" -> peerCaptures = m.value
        "COMBO" -> peerCombo = m.value
        "SABOTAGE" -> sabotageText = m.text
        "HULL" -> peerScore = m.value
        "PING" -> multiplayer.send(MarinePeerMessage("PONG", 1, "Fish AR"))
      }
    }
  }

  Column(modifier.fillMaxSize().background(OceanDeep)) {
    Row(
      Modifier.fillMaxWidth().background(OceanAbyss).padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBackToRadar, modifier = Modifier.size(42.dp).clip(CircleShape).background(OceanCard)) {
        Text("‹", color = MarineCyan, fontSize = 30.sp)
      }
      Spacer(Modifier.width(12.dp))
      Column(Modifier.weight(1f)) {
        Text("MODOS DE JUEGO", color = MarineCyan, fontWeight = FontWeight.Black, fontSize = 19.sp)
        Text("Operaciones marinas · Piura", color = TextSecondary, fontSize = 12.sp)
      }
      IconButton(onClick = { showTutorial = true }) {
        Icon(Icons.Default.TipsAndUpdates, "Tutorial y reglas", tint = MarineGold)
      }
    }

    Row(
      Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FairMode.values().forEach { mode ->
        val selected = selectedMode == mode
        Column(
          Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
            .background(if (selected) OceanCard else OceanAbyss)
            .border(1.dp, if (selected) MarineCyan else MarineCyan.copy(alpha = .18f), RoundedCornerShape(14.dp))
            .clickable { selectedMode = mode }
            .padding(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          mode.icon()
          Text(mode.title, color = if (selected) TextPrimary else TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2)
        }
      }
    }

    when (selectedMode) {
      FairMode.COMPETITIVE -> CompetitivePanel(tournamentViewModel, onShowUsername = { showUsername = true }, onLeaderboard = { showLeaderboard = true })
      FairMode.DUEL -> DuelPanel(multiplayer, peerState, peerScore, peerCaptures, peerCombo, sabotageText, onStartEncounter, selectedPeer, onPeerSelected = { selectedPeer = it }, onConnect = { it?.let(multiplayer::connect) }, onListen = { multiplayer.startListening() }, onSabotage = { text -> multiplayer.send(MarinePeerMessage("SABOTAGE", 5, text)) })
      FairMode.COOP -> CooperativePanel(multiplayer, peerState, onStartEncounter, onListen = { multiplayer.startListening() }, onConnect = { selectedPeer?.let(multiplayer::connect) }, onRoleChange = { role -> multiplayer.send(MarinePeerMessage("ROLE", 0, role)) })
      FairMode.SOLO -> SoloPanel(onStartEncounter)
    }
  }

  if (showTutorial) ModeTutorialDialog(selectedMode) { showTutorial = false }

  if (showUsername) {
    AlertDialog(
      onDismissRequest = { showUsername = false },
      title = { Text("Identificación del torneo") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("El Username se guarda junto al puntaje oficial local.", color = TextSecondary, fontSize = 12.sp)
          TextField(value = usernameDraft, onValueChange = { usernameDraft = it.take(18) }, singleLine = true, label = { Text("Username / apodo") })
        }
      },
      confirmButton = {
        Button(onClick = { tournamentViewModel.start(usernameDraft); showUsername = false; onStartEncounter() }, enabled = usernameDraft.trim().isNotEmpty()) { Text("INICIAR 6:00") }
      },
      dismissButton = { TextButton(onClick = { showUsername = false }) { Text("Cancelar") } }
    )
  }

  if (showLeaderboard) LeaderboardDialog(tournamentViewModel) { showLeaderboard = false }
}

@Composable
private fun CompetitivePanel(vm: FairTournamentViewModel, onShowUsername: () -> Unit, onLeaderboard: () -> Unit) {
  val remaining by vm.remainingSeconds.collectAsState()
  val score by vm.score.collectAsState()
  val captures by vm.captures.collectAsState()
  val combo by vm.combo.collectAsState()
  val running by vm.running.collectAsState()
  val finished by vm.finished.collectAsState()
  Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Card(colors = CardDefaults.cardColors(containerColor = OceanCard), shape = RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MarineGold.copy(alpha = .55f))) {
      Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("TORNEO CONTRARRELOJ", color = MarineGold, fontWeight = FontWeight.Black, fontSize = 13.sp)
        Text(formatTime(remaining), color = if (remaining <= 20) Color.Red else MarineCyan, fontWeight = FontWeight.Black, fontSize = 42.sp)
        Text("Puntos $score   ·   Capturas $captures   ·   Combo x${comboMultiplier(combo)}", color = TextSecondary, fontSize = 12.sp)
      }
    }
    Text("Encadena capturas limpias: combo 3 = x2 · combo 5+ = x3", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
      Button(onClick = onShowUsername, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MarineCyan)) { Text(if (running) "NUEVA PARTIDA" else "JUGAR") }
      OutlinedButton(onClick = onLeaderboard, modifier = Modifier.weight(1f)) { Text("RÉCORDS") }
    }
    if (finished) Text("Partida guardada en Room Database.", color = MarineGreen, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
  }
}

@SuppressLint("MissingPermission")
@Composable
private fun DuelPanel(
  manager: MarineMultiplayerManager,
  state: MarinePeerState,
  peerScore: Int,
  peerCaptures: Int,
  peerCombo: Int,
  sabotageText: String,
  onStartEncounter: () -> Unit,
  selectedPeer: BluetoothDevice?,
  onPeerSelected: (BluetoothDevice?) -> Unit,
  onConnect: (BluetoothDevice?) -> Unit,
  onListen: () -> Unit,
  onSabotage: (String) -> Unit
) {
  val devices = remember { manager.pairedDevices() }
  Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Text("DUELO 1 VS 1", color = MarineCyan, fontWeight = FontWeight.Black, fontSize = 20.sp)
    Text("Conecta dos teléfonos mediante Bluetooth RFCOMM. Cada uno juega su cuadrante y comparte marcador.", color = TextSecondary, fontSize = 12.sp)
    Card(colors = CardDefaults.cardColors(containerColor = OceanCard)) {
      Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Estado: ${peerStateLabel(state)}", color = if (state is MarinePeerState.Connected) MarineGreen else MarineGold, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
          Button(onClick = onListen, modifier = Modifier.weight(1f)) { Text("ESPERAR RIVAL") }
          Button(onClick = { onConnect(selectedPeer) }, enabled = selectedPeer != null, modifier = Modifier.weight(1f)) { Text("CONECTAR") }
        }
        LazyColumn(Modifier.height(120.dp)) {
          items(devices) { device ->
            Text(
              text = "${device.name ?: "Dispositivo"}  ·  ${device.address}",
              color = if (device == selectedPeer) MarineCyan else TextPrimary,
              fontSize = 11.sp,
              modifier = Modifier.fillMaxWidth().clickable { onPeerSelected(device) }.padding(7.dp)
            )
          }
        }
      }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
      Stat("RIVAL", peerScore.toString())
      Stat("CAPTURAS", peerCaptures.toString())
      Stat("COMBO", "x${comboMultiplier(peerCombo)}")
    }
    if (sabotageText.isNotBlank()) Text("⚠ $sabotageText", color = MarineGold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
      OutlinedButton(onClick = { onSabotage("CORRIENTE MARINA") }, enabled = state is MarinePeerState.Connected, modifier = Modifier.weight(1f)) { Text("CORRIENTE") }
      OutlinedButton(onClick = { onSabotage("NIEBLA OSCURA") }, enabled = state is MarinePeerState.Connected, modifier = Modifier.weight(1f)) { Text("NIEBLA") }
    }
    Button(onClick = onStartEncounter, enabled = state is MarinePeerState.Connected, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MarineGreen)) { Text("ENTRAR A MI CUADRANTE") }
  }
}

@Composable
private fun CooperativePanel(manager: MarineMultiplayerManager, state: MarinePeerState, onStartEncounter: () -> Unit, onListen: () -> Unit, onConnect: () -> Unit, onRoleChange: (String) -> Unit) {
  var role by remember { mutableStateOf("Operador de Choque") }
  Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text("COOPERATIVO DÚO", color = MarineGreen, fontWeight = FontWeight.Black, fontSize = 20.sp)
    Text("Una sola bahía sincronizada. El casco es común a ambos teléfonos.", color = TextSecondary, fontSize = 12.sp)
    Card(colors = CardDefaults.cardColors(containerColor = OceanCard)) {
      Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Conexión: ${peerStateLabel(state)}", color = MarineCyan, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(onClick = onListen, modifier = Modifier.weight(1f)) { Text("ANFITRIÓN") }
          Button(onClick = onConnect, modifier = Modifier.weight(1f)) { Text("UNIRSE") }
        }
        Text("Casco compartido", color = TextSecondary, fontSize = 10.sp)
        Text("100%", color = MarineGreen, fontWeight = FontWeight.Black, fontSize = 30.sp)
      }
    }
    Text("Tu rol", color = TextSecondary, fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
      listOf("Operador de Choque", "Operador de Red").forEach { item ->
        OutlinedButton(onClick = { role = item; onRoleChange(item) }, modifier = Modifier.weight(1f)) { Text(item, fontSize = 11.sp) }
      }
    }
    Text("Rol seleccionado: $role", color = MarineCyan, fontSize = 12.sp)
    Button(onClick = onStartEncounter, enabled = state is MarinePeerState.Connected, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MarineGreen)) { Text("DESCENDER EN BAHÍA COMPARTIDA") }
  }
}

@Composable
private fun SoloPanel(onStartEncounter: () -> Unit) {
  Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text("SOLITARIO LIBRE", color = MarineCyan, fontWeight = FontWeight.Black, fontSize = 20.sp)
    Text("Explora puertos y zonas costeras: Máncora, El Ñuro, Cabo Blanco, Paita y tu GPS real.", color = TextSecondary, fontSize = 12.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      Stat("GPS", "AUTO")
      Stat("PESCACOIN", "+")
      Stat("PESCADEX", "ACTIVO")
    }
    Button(onClick = onStartEncounter, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MarineCyan)) { Text("INICIAR EXPEDICIÓN") }
  }
}

@Composable
private fun ModeTutorialDialog(mode: FairMode, onDismiss: () -> Unit) {
  val content = when (mode) {
    FairMode.COMPETITIVE -> listOf("1. Registra tu Username.", "2. Tienes 6:00 exactos.", "3. Apunta y descarga el choque a menos de 15 m durante una embestida real.", "4. Capturas limpias encadenadas dan x2 en combo 3 y x3 desde combo 5.")
    FairMode.DUEL -> listOf("1. Vincula dos teléfonos por Bluetooth.", "2. Cada jugador pesca en su propio cuadrante.", "3. El marcador, capturas y combo del rival llegan por P2P.", "4. Corriente Marina y Niebla Oscura bloquean el visor rival durante 5 s.")
    FairMode.COOP -> listOf("1. Un jugador crea la sala y el otro se une.", "2. Seleccionen Choque o Red.", "3. El casco y los eventos se sincronizan.", "4. Coordinen herramientas para capturar sin romper el casco.")
    FairMode.SOLO -> listOf("1. Permite GPS para usar tu ubicación real.", "2. Explora zonas costeras y activa encuentros.", "3. Captura para desbloquear especies, Pescacoins y mejoras.", "4. Consulta la Pescadex después de cada descubrimiento.")
  }
  AlertDialog(onDismissRequest = onDismiss, title = { Text("Tutorial y reglas · ${mode.title}") }, text = { Column(verticalArrangement = Arrangement.spacedBy(9.dp)) { content.forEach { Text(it, color = TextSecondary, fontSize = 13.sp) }; Text("Especies destacadas: Bonito · Caballa · Cabrilla · Jurel · Mero Murike · Súper Pez", color = MarineCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold) } }, confirmButton = { TextButton(onClick = onDismiss) { Text("ENTENDIDO") } })
}

@Composable
private fun LeaderboardDialog(vm: FairTournamentViewModel, onDismiss: () -> Unit) {
  val entries by vm.leaderboard.collectAsState(initial = emptyList())
  AlertDialog(onDismissRequest = onDismiss, title = { Text("Tabla de Récords") }, text = { LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) { items(entries.take(20)) { entry -> Text("${rankMedal(entries.indexOf(entry))} ${entry.username} · ${entry.score} pts · ${entry.captures} capturas · ${entry.bestSpecies} · combo ${entry.maxCombo}", color = TextPrimary, fontSize = 11.sp) } } }, confirmButton = { TextButton(onClick = onDismiss) { Text("CERRAR") } })
}

@Composable
private fun Stat(label: String, value: String) {
  Card(modifier = Modifier.width(104.dp), colors = CardDefaults.cardColors(containerColor = OceanCard), shape = RoundedCornerShape(14.dp)) { Column(Modifier.fillMaxWidth().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(label, color = TextSecondary, fontSize = 9.sp); Text(value, color = MarineCyan, fontWeight = FontWeight.Black, fontSize = 18.sp) } }
}

private fun comboMultiplier(combo: Int) = when { combo >= 5 -> 3; combo >= 3 -> 2; else -> 1 }
private fun formatTime(seconds: Int) = "%d:%02d".format(seconds / 60, seconds % 60)
private fun peerStateLabel(state: MarinePeerState) = when (state) { MarinePeerState.Idle -> "Desconectado"; MarinePeerState.Listening -> "Esperando rival"; MarinePeerState.Connecting -> "Conectando…"; is MarinePeerState.Connected -> "Conectado a ${state.deviceName}"; is MarinePeerState.Error -> "Error: ${state.message}" }
private fun rankMedal(index: Int) = when (index) { 0 -> "🥇"; 1 -> "🥈"; 2 -> "🥉"; else -> "#${index + 1}" }
