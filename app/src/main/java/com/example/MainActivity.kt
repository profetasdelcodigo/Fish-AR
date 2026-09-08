package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.MarineSoundEngine
import com.example.game.MarineGameViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class MainScreenNavigation(val label: String, val icon: ImageVector) {
  MAPA("Mapa", Icons.Default.Radar),
  CATALOGO("Catálogo", Icons.Default.MenuBook),
  INVENTARIO("Inventario", Icons.Default.Backpack),
  MISIONES("Misiones", Icons.Default.Assignment),
  ECOSISTEMAS("Ecosistemas", Icons.Default.Waves),
  MINIGAMES("Feria", Icons.Default.Public),
  LABORATORIO("Laboratorio", Icons.Default.Tune),
  MORE("Más", Icons.Default.MoreHoriz)
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { MyApplicationTheme { PescActivateApp() } }
  }
}

@Composable
fun PescActivateApp(viewModel: MarineGameViewModel = viewModel()) {
  val activeEncounterState by viewModel.gameState.collectAsState()
  var isWelcomeCompleted by remember { mutableStateOf(false) }
  var currentNavScreen by remember { mutableStateOf(MainScreenNavigation.MAPA) }
  var showFairInfo by remember { mutableStateOf(false) }
  var isNightMode by remember { mutableStateOf(false) }
  val context = LocalContext.current

  val permissionsLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions(),
    onResult = { }
  )

  LaunchedEffect(Unit) {
    val neededPermissions = mutableListOf<String>()
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) neededPermissions.add(Manifest.permission.CAMERA)
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
    if (neededPermissions.isNotEmpty()) permissionsLauncher.launch(neededPermissions.toTypedArray())
  }

  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
    Box(Modifier.fillMaxSize().padding(innerPadding)) {
      when {
        activeEncounterState != null -> ArEncounterScreen(viewModel, activeEncounterState!!, Modifier.fillMaxSize())
        !isWelcomeCompleted -> WelcomeHeroScreen(onStartClicked = { isWelcomeCompleted = true }, modifier = Modifier.fillMaxSize())
        else -> {
          Box(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().padding(bottom = 70.dp)) {
              AnimatedContent(
                targetState = currentNavScreen,
                transitionSpec = {
                  (slideInHorizontally(animationSpec = tween(260)) { it / 4 } + fadeIn(tween(260))).togetherWith(slideOutHorizontally(tween(180)) { -it / 4 } + fadeOut(tween(180)))
                },
                label = "MainNavigationTransition"
              ) { targetScreen ->
                when (targetScreen) {
                  MainScreenNavigation.MAPA -> MapRadarScreen(viewModel, onOpenPescadex = { MarineSoundEngine.playNavClick(); currentNavScreen = MainScreenNavigation.CATALOGO }, onOpenMiniGames = { MarineSoundEngine.playNavClick(); currentNavScreen = MainScreenNavigation.MINIGAMES }, onOpenFairDetails = { MarineSoundEngine.playNavClick(); showFairInfo = true }, modifier = Modifier.fillMaxSize())
                  MainScreenNavigation.CATALOGO -> PescadexScreen(viewModel, onBackToRadar = { currentNavScreen = MainScreenNavigation.MAPA }, modifier = Modifier.fillMaxSize())
                  MainScreenNavigation.INVENTARIO -> InventoryScreen(viewModel, modifier = Modifier.fillMaxSize(), onOpenLab = { currentNavScreen = MainScreenNavigation.LABORATORIO })
                  MainScreenNavigation.MISIONES -> MissionsScreen(viewModel, modifier = Modifier.fillMaxSize())
                  MainScreenNavigation.ECOSISTEMAS -> EcosystemsScreen(viewModel, modifier = Modifier.fillMaxSize())
                  MainScreenNavigation.MINIGAMES -> FairMiniGamesScreen(viewModel, onBackToRadar = { currentNavScreen = MainScreenNavigation.MAPA }, modifier = Modifier.fillMaxSize())
                  MainScreenNavigation.LABORATORIO -> EquipmentLabScreen(viewModel, modifier = Modifier.fillMaxSize())
                  MainScreenNavigation.MORE -> MoreFeaturesScreen(viewModel, isNightMode, onToggleNightMode = { isNightMode = !isNightMode }, modifier = Modifier.fillMaxSize())
                }
              }
            }

            Row(
              modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).clip(RoundedCornerShape(26.dp)).background(OceanAbyss.copy(alpha = .96f)).border(1.2.dp, MarineCyan.copy(alpha = .35f), RoundedCornerShape(26.dp)).padding(horizontal = 4.dp, vertical = 6.dp).testTag("main_bottom_nav_bar"),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically
            ) {
              listOf(MainScreenNavigation.MAPA, MainScreenNavigation.CATALOGO, MainScreenNavigation.INVENTARIO, MainScreenNavigation.MISIONES, MainScreenNavigation.ECOSISTEMAS).forEach { tab ->
                val selected = currentNavScreen == tab
                Column(
                  modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { MarineSoundEngine.playNavClick(); currentNavScreen = tab }.padding(horizontal = 10.dp, vertical = 4.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Icon(tab.icon, tab.label, tint = if (selected) MarineCyan else TextSecondary, modifier = Modifier.size(20.dp))
                  Spacer(Modifier.height(2.dp))
                  Text(tab.label, color = if (selected) MarineCyan else TextSecondary, fontWeight = if (selected) FontWeight.Black else FontWeight.Normal, fontSize = 10.sp)
                }
              }
            }

            if (currentNavScreen != MainScreenNavigation.MORE) {
              Row(
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(top = 12.dp, end = 12.dp)
                  .clip(RoundedCornerShape(18.dp))
                  .background(OceanAbyss.copy(alpha = .94f))
                  .border(1.dp, MarineCyan.copy(alpha = .4f), RoundedCornerShape(18.dp))
                  .clickable { MarineSoundEngine.playNavClick(); currentNavScreen = MainScreenNavigation.MORE }
                  .padding(horizontal = 10.dp, vertical = 7.dp)
                  .testTag("open_more_features"),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.MoreHoriz, contentDescription = "Más funciones", tint = MarineCyan, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(5.dp))
                Text("Más", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 10.sp)
              }
            }

            if (isNightMode) {
              Box(
                Modifier
                  .fillMaxSize()
                  .background(Color.Black.copy(alpha = .14f))
                  .testTag("night_mode_overlay")
              )
            }
          }
        }
      }
      if (showFairInfo) FairInfoDialog(onDismiss = { showFairInfo = false })
    }
  }
}
