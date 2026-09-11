package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.example.game.MarineGameViewModel
import com.example.ui.components.CameraPreviewView
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale

private enum class FeaturePage(val title: String, val subtitle: String) {
  HUB("Centro de Operaciones", "Funciones avanzadas de Fish AR"),
  CAMERA("Cámara de exploración", "Escaneo visual del entorno"),
  WEATHER("Clima marino", "Condiciones en tiempo real"),
  SOCIAL("Radar social", "Señales compartidas sin perfiles inventados"),
  LEAGUE("Fish League", "Progreso competitivo de la temporada"),
  STORE("Tienda de expedición", "Consumibles y mejoras cosméticas")
}

private data class StoreItem(val id: String, val name: String, val description: String, val cost: Int)

@Composable
fun MoreFeaturesScreen(
  viewModel: MarineGameViewModel,
  modifier: Modifier = Modifier,
  onNavigate: (String) -> Unit = {}
) {
  var page by remember { mutableStateOf(FeaturePage.HUB) }
  val pescacoins by viewModel.pescacoins.collectAsState()
  val unlocked by viewModel.unlockedSpeciesIds.collectAsState()
  val location by viewModel.realLocation.collectAsState()
  val ownedItems by viewModel.ownedStoreItems.collectAsState()
  val socialSharing by viewModel.socialSharingEnabled.collectAsState()
  val isNightMode by viewModel.nightModeEnabled.collectAsState()

  val storeItems = remember {
    listOf(
      StoreItem("battery_pack", "Kit de batería", "+20% de carga inicial en expediciones.", 80),
      StoreItem("sonar_skin", "Skin Sonar Obsidiana", "Acabado visual para el HUD de exploración.", 120),
      StoreItem("decoy_pack", "Paquete de señuelos", "Preparación para encuentros y prácticas de amago.", 140),
      StoreItem("golden_badge", "Insignia Mar de Grau", "Distintivo cosmético de perfil local.", 200)
    )
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Brush.verticalGradient(listOf(OceanDeep, OceanAbyss, OceanDeep)))
      .testTag("more_features_screen")
  ) {
    when (page) {
      FeaturePage.HUB -> FeatureHub(
        pescacoins = pescacoins,
        speciesCount = unlocked.size,
        isNightMode = isNightMode,
        onToggleNightMode = { viewModel.toggleNightMode() },
        onResetWelcome = { viewModel.resetWelcome() },
        onOpen = { page = it },
        onNavigate = onNavigate
      )
      FeaturePage.CAMERA -> CameraExplorationScreen(onBack = { page = FeaturePage.HUB })
      FeaturePage.WEATHER -> WeatherScreen(
        latitude = location.latitude,
        longitude = location.longitude,
        isRealGps = location.isRealGps,
        onBack = { page = FeaturePage.HUB }
      )
      FeaturePage.SOCIAL -> SocialRadarScreen(
        viewModel = viewModel,
        sharing = socialSharing,
        onToggleSharing = { viewModel.toggleSocialSharing() },
        onBack = { page = FeaturePage.HUB }
      )
      FeaturePage.LEAGUE -> FishLeagueScreen(
        speciesCount = unlocked.size,
        pescacoins = pescacoins,
        onBack = { page = FeaturePage.HUB }
      )
      FeaturePage.STORE -> ExpeditionStore(
        items = storeItems,
        ownedItems = ownedItems,
        pescacoins = pescacoins,
        onBuy = { item -> viewModel.buyStoreItem(item.id, item.cost) },
        onBack = { page = FeaturePage.HUB }
      )
    }
  }
}

@Composable
private fun FeatureHub(
  pescacoins: Int,
  speciesCount: Int,
  isNightMode: Boolean,
  onToggleNightMode: () -> Unit,
  onResetWelcome: () -> Unit,
  onOpen: (FeaturePage) -> Unit,
  onNavigate: (String) -> Unit = {}
) {
  val cards = listOf(
    Triple(FeaturePage.CAMERA, "Cámara AR", Icons.Default.CameraAlt),
    Triple(FeaturePage.WEATHER, "Clima en tiempo real", Icons.Default.Cloud),
    Triple(FeaturePage.SOCIAL, "Mapa social", Icons.Default.Groups),
    Triple(FeaturePage.LEAGUE, "Fish League", Icons.Default.Leaderboard),
    Triple(FeaturePage.STORE, "Tienda", Icons.Default.Storefront)
  )

  val navigationCards = listOf(
    Triple("Ecosistemas", "Zonas marinas de Piura", Icons.Default.Waves),
    Triple("Laboratorio", "Equipamiento abisal", Icons.Default.Tune)
  )

  Column(Modifier.fillMaxSize().padding(18.dp)) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
      Column(Modifier.weight(1f)) {
        Text("EXPLORACIÓN AVANZADA", color = MarineCyan, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.8.sp)
        Text("Centro de Operaciones", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Black)
        Text("Todo lo que necesitas para convertir una salida al litoral en una expedición.", color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
      }
      Row(Modifier.clip(RoundedCornerShape(16.dp)).background(OceanCard).border(1.dp, MarineGold.copy(alpha = .65f), RoundedCornerShape(16.dp)).padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.MonetizationOn, null, tint = MarineGold, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(5.dp))
        Text("$pescacoins", color = MarineGold, fontWeight = FontWeight.Black, fontSize = 12.sp)
      }
    }

    Spacer(Modifier.height(16.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      StatPill("REGISTRO", "$speciesCount especies", MarineCyan, Icons.Default.Waves, Modifier.weight(1f))
      StatPill("MODO", if (isNightMode) "NOCTURNO" else "NORMAL", if (isNightMode) MarineGold else MarineGreen, Icons.Default.DarkMode, Modifier.weight(1f))
    }

    Spacer(Modifier.height(16.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
      items(cards) { (page, title, icon) ->
        FeatureCard(page, title, icon, onOpen)
      }
      items(navigationCards) { (title, subtitle, icon) ->
        FeatureNavCard(title, subtitle, icon, onNavigate)
      }
      item {
        Row(
          Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(OceanCard.copy(alpha = .45f)).border(1.dp, MarineGold.copy(alpha = .35f), RoundedCornerShape(20.dp)).padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(Modifier.size(42.dp).clip(CircleShape).background(MarineGold.copy(alpha = .10f)).border(1.dp, MarineGold.copy(alpha = .45f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.DarkMode, null, tint = MarineGold, modifier = Modifier.size(20.dp))
          }
          Spacer(Modifier.width(12.dp))
          Column(Modifier.weight(1f)) {
            Text("Modo nocturno", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Text("Reduce luminancia y prioriza la información crítica durante expediciones nocturnas.", color = TextSecondary, fontSize = 11.sp)
          }
          Switch(checked = isNightMode, onCheckedChange = { onToggleNightMode() }, colors = SwitchDefaults.colors(checkedThumbColor = OceanDeep, checkedTrackColor = MarineGold))
        }
      }
      item {
        Spacer(Modifier.height(10.dp))
        Column(
          Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFF421212).copy(alpha = .35f)).border(1.dp, Color(0xFFE53935).copy(alpha = .45f), RoundedCornerShape(20.dp)).padding(14.dp)
        ) {
          Text("ZONA DE RIESGO", color = Color(0xFFFF5252), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
          Spacer(Modifier.height(6.dp))
          Button(
            onClick = { onResetWelcome() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935).copy(alpha = .2f), contentColor = Color(0xFFFF8A80)),
            modifier = Modifier.fillMaxWidth().height(40.dp),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("REINICIAR TUTORIAL INICIAL", fontWeight = FontWeight.Black, fontSize = 11.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun StatPill(label: String, value: String, accent: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
  Row(modifier.clip(RoundedCornerShape(15.dp)).background(OceanAbyss).border(1.dp, accent.copy(alpha = .25f), RoundedCornerShape(15.dp)).padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
    Icon(icon, null, tint = accent, modifier = Modifier.size(16.dp))
    Spacer(Modifier.width(6.dp))
    Column {
      Text(label, color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
      Text(value, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
  }
}

@Composable
private fun FeatureCard(page: FeaturePage, title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onOpen: (FeaturePage) -> Unit) {
  Row(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(OceanCard.copy(alpha = .68f)).border(1.dp, MarineCyan.copy(alpha = .18f), RoundedCornerShape(20.dp)).clickable { onOpen(page) }.padding(14.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(MarineCyan.copy(alpha = .09f)).border(1.dp, MarineCyan.copy(alpha = .38f), RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
      Icon(icon, null, tint = MarineCyan, modifier = Modifier.size(24.dp))
    }
    Spacer(Modifier.width(12.dp))
    Column(Modifier.weight(1f)) {
      Text(title, color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 14.sp)
      Text(page.subtitle, color = TextSecondary, fontSize = 11.sp)
    }
    Text("ABRIR", color = MarineCyan, fontWeight = FontWeight.Black, fontSize = 9.sp)
  }
}

@Composable
private fun FeatureNavCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onNavigate: (String) -> Unit) {
  Row(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(OceanCard.copy(alpha = .68f)).border(1.dp, MarineCyan.copy(alpha = .18f), RoundedCornerShape(20.dp)).clickable { onNavigate(title.uppercase()) }.padding(14.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(Modifier.size(48.dp).clip(RoundedCornerShape(15.dp)).background(MarineCyan.copy(alpha = .09f)).border(1.dp, MarineCyan.copy(alpha = .38f), RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
      Icon(icon, null, tint = MarineCyan, modifier = Modifier.size(24.dp))
    }
    Spacer(Modifier.width(12.dp))
    Column(Modifier.weight(1f)) {
      Text(title, color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 14.sp)
      Text(subtitle, color = TextSecondary, fontSize = 11.sp)
    }
    Text("IR", color = MarineCyan, fontWeight = FontWeight.Black, fontSize = 9.sp)
  }
}

@Composable
private fun CameraExplorationScreen(onBack: () -> Unit) {
  Column(Modifier.fillMaxSize().background(Color.Black)) {
    Box(Modifier.fillMaxWidth().weight(1f)) {
      CameraPreviewView(Modifier.fillMaxSize())
      Box(Modifier.fillMaxSize().background(OceanDeep.copy(alpha = .32f)))
      Column(Modifier.align(Alignment.TopCenter).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("CÁMARA DE EXPLORACIÓN", color = MarineCyan, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        Text("Busca siluetas, superficies y puntos de interés.", color = TextPrimary, fontSize = 12.sp, textAlign = TextAlign.Center)
      }
      Box(Modifier.align(Alignment.Center).size(190.dp).border(1.dp, MarineCyan.copy(alpha = .8f), RoundedCornerShape(28.dp)))
      Row(Modifier.align(Alignment.BottomCenter).padding(24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp).clip(CircleShape).background(OceanAbyss.copy(alpha = .9f)).border(1.dp, MarineCyan, CircleShape)) {
          Icon(Icons.Default.Radar, "Volver", tint = MarineCyan)
        }
        Box(Modifier.size(70.dp).clip(CircleShape).background(Color.White.copy(alpha = .95f)).border(4.dp, MarineCyan, CircleShape), contentAlignment = Alignment.Center) {
          Icon(Icons.Default.PhotoCamera, "Escanear", tint = OceanDeep, modifier = Modifier.size(30.dp))
        }
      }
    }
  }
}

@Composable
private fun WeatherScreen(latitude: Double, longitude: Double, isRealGps: Boolean, onBack: () -> Unit) {
  var loading by remember { mutableStateOf(true) }
  var summary by remember { mutableStateOf("Consultando condiciones marinas…") }
  var temperature by remember { mutableStateOf("—") }
  var wind by remember { mutableStateOf("—") }
  var humidity by remember { mutableStateOf("—") }
  val scope = rememberCoroutineScope()

  fun refresh() {
    scope.launch {
      loading = true
      val result = withContext(Dispatchers.IO) {
        try {
          val url = String.format(Locale.US, "https://api.open-meteo.com/v1/forecast?latitude=%.5f&longitude=%.5f&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code", latitude, longitude)
          val response = OkHttpClient().newCall(Request.Builder().url(url).build()).execute()
          val json = response.body?.string().orEmpty()
          val current = JSONObject(json).optJSONObject("current") ?: return@withContext null
          val temp = current.optDouble("temperature_2m", Double.NaN)
          val hum = current.optDouble("relative_humidity_2m", Double.NaN)
          val windSpeed = current.optDouble("wind_speed_10m", Double.NaN)
          val code = current.optInt("weather_code", -1)
          Triple(temp, hum, Pair(windSpeed, code))
        } catch (_: Exception) { null }
      }
      if (result != null) {
        temperature = if (result.first.isNaN()) "—" else String.format(Locale.US, "%.1f °C", result.first)
        humidity = if (result.second.isNaN()) "—" else String.format(Locale.US, "%.0f %%", result.second)
        wind = if (result.third.first.isNaN()) "—" else String.format(Locale.US, "%.1f km/h", result.third.first)
        summary = weatherLabel(result.third.second)
      } else {
        summary = "Sin conexión; revisa tu red e inténtalo de nuevo."
      }
      loading = false
    }
  }

  LaunchedEffect(latitude, longitude) { refresh() }

  Column(Modifier.fillMaxSize().padding(16.dp)) {
    HeaderWithBack("Clima marino", "Datos actuales por coordenadas", onBack)
    Spacer(Modifier.height(14.dp))
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(OceanCard.copy(alpha = .6f)).border(1.dp, MarineCyan.copy(alpha = .25f), RoundedCornerShape(18.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.Default.LocationOn, null, tint = MarineGreen, modifier = Modifier.size(18.dp))
      Spacer(Modifier.width(8.dp))
      Column {
        Text(if (isRealGps) "GPS REAL" else "UBICACIÓN DE PRUEBA", color = if (isRealGps) MarineGreen else MarineGold, fontWeight = FontWeight.Black, fontSize = 10.sp)
        Text(String.format(Locale.US, "%.4f°, %.4f°", latitude, longitude), color = TextSecondary, fontSize = 11.sp)
      }
    }
    Spacer(Modifier.height(14.dp))
    Card(colors = CardDefaults.cardColors(containerColor = OceanCard), shape = RoundedCornerShape(24.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MarineCyan.copy(alpha = .35f))) {
      Column(Modifier.padding(18.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Cloud, null, tint = MarineCyan, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(8.dp)); Text(summary, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Black) }
          Text(temperature, color = MarineGold, fontSize = 25.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          WeatherStat("HUMEDAD", humidity)
          WeatherStat("VIENTO", wind)
          WeatherStat("FUENTE", "Open-Meteo")
        }
      }
    }
    Spacer(Modifier.height(12.dp))
    Button(onClick = { refresh() }, enabled = !loading, colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanDeep), modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp)) {
      Text(if (loading) "ACTUALIZANDO…" else "ACTUALIZAR CONDICIONES", fontWeight = FontWeight.Black, fontSize = 12.sp)
    }
  }
}

private fun weatherLabel(code: Int): String = when (code) {
  0 -> "Cielo despejado"
  1, 2, 3 -> "Parcialmente nublado"
  45, 48 -> "Niebla"
  51, 53, 55, 56, 57 -> "Llovizna"
  61, 63, 65, 66, 67 -> "Lluvia"
  71, 73, 75, 77 -> "Nieve"
  80, 81, 82 -> "Chubascos"
  95, 96, 99 -> "Tormenta"
  else -> "Condición no clasificada"
}

@Composable
private fun WeatherStat(label: String, value: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(label, color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(3.dp))
    Text(value, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Black)
  }
}

@Composable
private fun SocialRadarScreen(viewModel: MarineGameViewModel, sharing: Boolean, onToggleSharing: () -> Unit, onBack: () -> Unit) {
  val zone by viewModel.selectedZone.collectAsState()
  Column(Modifier.fillMaxSize().padding(16.dp)) {
    HeaderWithBack("Radar social", "Exploración comunitaria respetuosa", onBack)
    Spacer(Modifier.height(14.dp))
    Card(colors = CardDefaults.cardColors(containerColor = OceanCard), shape = RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MarineCyan.copy(alpha = .35f))) {
      Column(Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(Modifier.size(42.dp).clip(CircleShape).background(MarineCyan.copy(alpha = .10f)).border(1.dp, MarineCyan.copy(alpha = .45f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Groups, null, tint = MarineCyan) }
          Spacer(Modifier.width(10.dp))
          Column(Modifier.weight(1f)) { Text("Compartir mi señal", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 14.sp); Text("No crea perfiles ni nombres ficticios.", color = TextSecondary, fontSize = 10.sp) }
          Switch(checked = sharing, onCheckedChange = { onToggleSharing() }, colors = SwitchDefaults.colors(checkedThumbColor = OceanDeep, checkedTrackColor = MarineGreen))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(OceanAbyss).padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Radar, null, tint = MarineGold, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Zona activa: ${zone.name}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        Spacer(Modifier.height(10.dp))
        Text(if (sharing) "Tu señal está visible para la sesión local de exploración." else "Tu señal permanece privada.", color = if (sharing) MarineGreen else TextSecondary, fontSize = 11.sp)
      }
    }
  }
}

@Composable
private fun FishLeagueScreen(speciesCount: Int, pescacoins: Int, onBack: () -> Unit) {
  val score = speciesCount * 120 + pescacoins / 2
  val tier = when {
    score >= 1800 -> "ORO ABISAL"
    score >= 900 -> "PLATA COSTERA"
    else -> "BRONCE MARINO"
  }
  val progress = (score % 600) / 600f
  Column(Modifier.fillMaxSize().padding(16.dp)) {
    HeaderWithBack("Fish League", "Clasificación de expedición", onBack)
    Spacer(Modifier.height(14.dp))
    Card(colors = CardDefaults.cardColors(containerColor = OceanCard), shape = RoundedCornerShape(24.dp), border = androidx.compose.foundation.BorderStroke(1.5.dp, MarineGold.copy(alpha = .6f))) {
      Column(Modifier.padding(18.dp)) {
        Text("CLASIFICACIÓN ACTUAL", color = MarineGold, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
        Spacer(Modifier.height(4.dp))
        Text(tier, color = TextPrimary, fontSize = 25.sp, fontWeight = FontWeight.Black)
        Text("Puntos acumulados: $score", color = TextSecondary, fontSize = 11.sp)
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(5.dp)), color = MarineGold, trackColor = OceanAbyss)
        Spacer(Modifier.height(6.dp))
        Text("Progreso de división · ${(progress * 100).toInt()}%", color = TextSecondary, fontSize = 10.sp)
      }
    }
    Spacer(Modifier.height(12.dp))
    LeagueRow("Registro de especies", "$speciesCount / 9", Icons.Default.Waves)
    LeagueRow("Economía de expedición", "$pescacoins PC", Icons.Default.MonetizationOn)
    LeagueRow("Próximo objetivo", "Completa 1 encuentro real", Icons.Default.Radar)
  }
}

@Composable
private fun LeagueRow(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
  Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(OceanCard.copy(alpha = .55f)).border(1.dp, MarineCyan.copy(alpha = .15f), RoundedCornerShape(16.dp)).padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
    Icon(icon, null, tint = MarineCyan, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(10.dp)); Text(title, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f)); Text(value, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Black)
  }
  Spacer(Modifier.height(8.dp))
}

@Composable
private fun ExpeditionStore(items: List<StoreItem>, ownedItems: Set<String>, pescacoins: Int, onBuy: (StoreItem) -> Unit, onBack: () -> Unit) {
  Column(Modifier.fillMaxSize().padding(16.dp)) {
    HeaderWithBack("Tienda de expedición", "Usa Pescacoins · sin perfiles ni pagos externos", onBack)
    Spacer(Modifier.height(10.dp))
    Text("Saldo: $pescacoins PC", color = MarineGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
    Spacer(Modifier.height(12.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      items(items) { item ->
        val owned = item.id in ownedItems
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(OceanCard.copy(alpha = .65f)).border(1.dp, if (owned) MarineGreen.copy(alpha = .5f) else MarineCyan.copy(alpha = .15f), RoundedCornerShape(20.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
          Box(Modifier.size(44.dp).clip(CircleShape).background(MarineCyan.copy(alpha = .10f)).border(1.dp, MarineCyan.copy(alpha = .3f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Storefront, null, tint = MarineCyan, modifier = Modifier.size(21.dp)) }
          Spacer(Modifier.width(10.dp))
          Column(Modifier.weight(1f)) { Text(item.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Black); Text(item.description, color = TextSecondary, fontSize = 10.sp); Text("${item.cost} PC", color = MarineGold, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
          Button(onClick = { onBuy(item) }, enabled = !owned && pescacoins >= item.cost, colors = ButtonDefaults.buttonColors(containerColor = if (owned) MarineGreen else MarineCyan, contentColor = OceanDeep), shape = RoundedCornerShape(12.dp), modifier = Modifier.height(36.dp)) { Text(if (owned) "ADQUIRIDO" else "COMPRAR", fontSize = 9.sp, fontWeight = FontWeight.Black) }
        }
      }
    }
  }
}

@Composable
private fun HeaderWithBack(title: String, subtitle: String, onBack: () -> Unit) {
  Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    IconButton(onClick = onBack, modifier = Modifier.size(40.dp).clip(CircleShape).background(OceanCard).border(1.dp, MarineCyan.copy(alpha = .35f), CircleShape)) { Icon(Icons.Default.Radar, "Volver", tint = MarineCyan) }
    Spacer(Modifier.width(10.dp))
    Column { Text(title, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black); Text(subtitle, color = TextSecondary, fontSize = 11.sp) }
  }
}
