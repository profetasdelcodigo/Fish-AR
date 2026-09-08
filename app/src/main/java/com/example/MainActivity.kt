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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
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
import com.example.ui.screens.ArEncounterScreen
import com.example.ui.screens.EcosystemsScreen
import com.example.ui.screens.FairInfoDialog
import com.example.ui.screens.FairMiniGamesScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.MapRadarScreen
import com.example.ui.screens.MissionsScreen
import com.example.ui.screens.PescadexScreen
import com.example.ui.screens.WelcomeHeroScreen
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class MainScreenNavigation(val label: String, val icon: ImageVector) {
  MAPA("Mapa", Icons.Default.Radar),
  CATALOGO("Catálogo", Icons.Default.MenuBook),
  INVENTARIO("Inventario", Icons.Default.Backpack),
  MISIONES("Misiones", Icons.Default.Assignment),
  ECOSISTEMAS("Ecosistemas", Icons.Default.Waves),
  MINIGAMES("Feria", Icons.Default.Public)
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        PescActivateApp()
      }
    }
  }
}

@Composable
fun PescActivateApp(
  viewModel: MarineGameViewModel = viewModel()
) {
  val activeEncounterState by viewModel.gameState.collectAsState()
  var isWelcomeCompleted by remember { mutableStateOf(false) }
  var currentNavScreen by remember { mutableStateOf(MainScreenNavigation.MAPA) }
  var showFairInfo by remember { mutableStateOf(false) }

  val context = LocalContext.current

  // Request both camera (rear view AR) and location (real Google Maps GPS)
  val permissionsLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions(),
    onResult = { permissions ->
      val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
      val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
    }
  )

  LaunchedEffect(Unit) {
    val neededPermissions = mutableListOf<String>()
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      neededPermissions.add(Manifest.permission.CAMERA)
    }
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    if (neededPermissions.isNotEmpty()) {
      permissionsLauncher.launch(neededPermissions.toTypedArray())
    }
  }

  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      val encounter = activeEncounterState

      if (encounter != null) {
        // 1. ACTIVE 3D AR ENCOUNTER (Full camera backdrop + 3D meshes)
        ArEncounterScreen(
          viewModel = viewModel,
          gameState = encounter,
          modifier = Modifier.fillMaxSize()
        )
      } else if (!isWelcomeCompleted) {
        // 2. PORTADA DE BIENVENIDA CON FOTO REAL DE PLAYA/MAR DE PIURA
        WelcomeHeroScreen(
          onStartClicked = {
            isWelcomeCompleted = true
          },
          modifier = Modifier.fillMaxSize()
        )
      } else {
        // 3. MAIN APP INTERFACE WITH 5-TAB FLOATING BOTTOM NAVIGATION
        Box(modifier = Modifier.fillMaxSize()) {
          // Screen Content
          Box(modifier = Modifier.fillMaxSize().padding(bottom = 70.dp)) {
            AnimatedContent(
              targetState = currentNavScreen,
              transitionSpec = {
                (slideInHorizontally(animationSpec = tween(260)) { width -> width / 4 } + fadeIn(animationSpec = tween(260)))
                  .togetherWith(slideOutHorizontally(animationSpec = tween(260)) { width -> -width / 4 } + fadeOut(animationSpec = tween(180)))
              },
              label = "MainNavigationTransition"
            ) { targetScreen ->
              when (targetScreen) {
                MainScreenNavigation.MAPA -> {
                  MapRadarScreen(
                    viewModel = viewModel,
                    onOpenPescadex = {
                      MarineSoundEngine.playNavClick()
                      currentNavScreen = MainScreenNavigation.CATALOGO
                    },
                    onOpenMiniGames = {
                      MarineSoundEngine.playNavClick()
                      currentNavScreen = MainScreenNavigation.MINIGAMES
                    },
                    onOpenFairDetails = {
                      MarineSoundEngine.playNavClick()
                      showFairInfo = true
                    },
                    modifier = Modifier.fillMaxSize()
                  )
                }
                MainScreenNavigation.CATALOGO -> {
                  PescadexScreen(
                    viewModel = viewModel,
                    onBackToRadar = {
                      currentNavScreen = MainScreenNavigation.MAPA
                    },
                    modifier = Modifier.fillMaxSize()
                  )
                }
                MainScreenNavigation.INVENTARIO -> {
                  InventoryScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                  )
                }
                MainScreenNavigation.MISIONES -> {
                  MissionsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                  )
                }
                MainScreenNavigation.ECOSISTEMAS -> {
                  EcosystemsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                  )
                }
                MainScreenNavigation.MINIGAMES -> {
                  FairMiniGamesScreen(
                    viewModel = viewModel,
                    onBackToRadar = {
                      currentNavScreen = MainScreenNavigation.MAPA
                    },
                    modifier = Modifier.fillMaxSize()
                  )
                }
              }
            }
          }

          // 4. FLOATING 5-ITEM BOTTOM NAVIGATION BAR (Exact replica from reference mockup)
          Row(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp)
              .clip(RoundedCornerShape(26.dp))
              .background(OceanAbyss.copy(alpha = 0.95f))
              .border(1.2.dp, MarineCyan.copy(alpha = 0.35f), RoundedCornerShape(26.dp))
              .padding(horizontal = 4.dp, vertical = 6.dp)
              .testTag("main_bottom_nav_bar"),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            val mainTabs = listOf(
              MainScreenNavigation.MAPA,
              MainScreenNavigation.CATALOGO,
              MainScreenNavigation.INVENTARIO,
              MainScreenNavigation.MISIONES,
              MainScreenNavigation.ECOSISTEMAS
            )

            mainTabs.forEach { tab ->
              val isSelected = currentNavScreen == tab
              Column(
                modifier = Modifier
                  .clip(RoundedCornerShape(16.dp))
                  .clickable {
                    MarineSoundEngine.playNavClick()
                    currentNavScreen = tab
                  }
                  .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(
                  imageVector = tab.icon,
                  contentDescription = tab.label,
                  tint = if (isSelected) MarineCyan else TextSecondary,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = tab.label,
                  color = if (isSelected) MarineCyan else TextSecondary,
                  fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                  fontSize = 10.sp
                )
              }
            }
          }
        }
      }

      // Educational Fair Details Dialog
      if (showFairInfo) {
        FairInfoDialog(
          onDismiss = { showFairInfo = false }
        )
      }
    }
  }
}
