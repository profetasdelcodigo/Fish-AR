package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MarineGold
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanDeep

@Composable
fun WelcomeHeroScreen(
  onStartClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(OceanDeep)
      .testTag("welcome_hero_screen")
  ) {
    // 1. FOTOGRAFÍA REAL DEL MAR Y PLAYA DE PIURA (Máncora / Cabo Blanco al Atardecer)
    Image(
      painter = painterResource(id = R.drawable.real_piura_sunset),
      contentDescription = "Playa y Mar Real de Piura al atardecer",
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize()
    )

    // 2. Cinematic Gradient Scrim (High contrast readability like reference mockup)
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              OceanAbyss.copy(alpha = 0.55f),
              Color.Transparent,
              OceanAbyss.copy(alpha = 0.85f),
              OceanAbyss.copy(alpha = 0.98f)
            ),
            startY = 0f,
            endY = Float.POSITIVE_INFINITY
          )
        )
    )

    // 3. Top Tagline: Explora • Encuentra • Conoce • Cuida
    Row(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 48.dp, start = 20.dp, end = 20.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Explora  •  Encuentra  •  Conoce  •  Cuida",
        color = Color.White.copy(alpha = 0.85f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.2.sp
      )
    }

    // 4. Center Brand: Minimalist Marine Logo + FISH AR / PESCACTÍVATE AR
    Column(
      modifier = Modifier
        .align(Alignment.Center)
        .padding(horizontal = 24.dp)
        .offset(y = (-30).dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Minimalist stylized fish logo container
      Box(
        modifier = Modifier
          .size(90.dp)
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.12f))
          .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_fish_ar_logo),
          contentDescription = "Logo Oficial Fish AR",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(86.dp)
            .clip(CircleShape)
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      Text(
        text = "FISH AR",
        color = Color.White,
        fontWeight = FontWeight.Black,
        fontSize = 44.sp,
        letterSpacing = 4.sp
      )

      Text(
        text = "PescActívate • Costa de Piura",
        color = MarineCyan,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 1.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "El mar también está en tu mundo",
        color = Color.White.copy(alpha = 0.90f),
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.5.sp
      )
    }

    // 5. Bottom Action Section: Big White "Comenzar" Button + Tagline
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(horizontal = 28.dp, vertical = 42.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Real Location Badge of Piura
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(14.dp))
          .background(Color.Black.copy(alpha = 0.5f))
          .border(1.dp, MarineGold.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
          .padding(horizontal = 12.dp, vertical = 5.dp)
      ) {
        Text(
          text = "Playa y Mar Real de Piura • Mar de Grau",
          color = MarineGold,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Big Elegant Pill "Comenzar" Button (Exact style from reference image)
      Button(
        onClick = {
          MarineSoundEngine.playNavClick()
          onStartClicked()
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.White,
          contentColor = OceanDeep
        ),
        shape = RoundedCornerShape(32.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("welcome_start_button")
      ) {
        Text(
          text = "Comenzar",
          color = OceanDeep,
          fontSize = 18.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 0.5.sp
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Waves icon + subtitle
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.Waves,
          contentDescription = null,
          tint = Color.White.copy(alpha = 0.75f),
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Una aventura real, en la palma de tu mano.",
          color = Color.White.copy(alpha = 0.75f),
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}
