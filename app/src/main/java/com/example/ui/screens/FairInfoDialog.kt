package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MarineGold
import com.example.ui.theme.MarineGreen
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FairInfoDialog(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.85f))
      .clickable { onDismiss() }
      .padding(16.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      colors = CardDefaults.cardColors(containerColor = OceanCard),
      shape = RoundedCornerShape(22.dp),
      border = androidx.compose.foundation.BorderStroke(2.dp, MarineCyan),
      modifier = Modifier
        .fillMaxWidth()
        .clickable(enabled = false) {}
        .testTag("fair_info_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(18.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Banner Header
        Image(
          painter = painterResource(id = R.drawable.img_fair_banner),
          contentDescription = "Feria Sabores del Mar",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(14.dp))
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Feria \"Sabores del Mar\"",
              color = MarineGold,
              fontWeight = FontWeight.Black,
              fontSize = 18.sp
            )
            Text(
              text = "Para una vida saludable • PescActívate",
              color = MarineCyan,
              fontSize = 12.sp
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(OceanDeep)
          ) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextPrimary)
          }
        }

        // Date & Location
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(OceanDeep)
            .padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.DateRange, contentDescription = null, tint = MarineGold)
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text("Viernes 11 de Setiembre • 10:00 a.m.", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("Red de Cuidado Oceánico", color = TextSecondary, fontSize = 11.sp)
          }
        }

        // Nutritional Highlights from Poster (Vector Icons, Zero Emojis)
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(OceanAbyss)
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text("BENEFICIOS DEL CONSUMO DEL PESCADO:", color = MarineCyan, fontWeight = FontWeight.Black, fontSize = 12.sp)

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MarineGold, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Alto en proteína de alta calidad (High in protein)", color = TextPrimary, fontSize = 12.sp)
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Salud cardiovascular y arterias (Good for the heart)", color = TextPrimary, fontSize = 12.sp)
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, contentDescription = null, tint = MarineGreen, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Fortalece el sistema inmune y previene la anemia", color = TextPrimary, fontSize = 12.sp)
          }
        }

        // Program and stands
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text("ACTIVIDADES Y EXPOSICIONES DESTACADAS:", color = MarineGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          Text("• Feria Gastronómica a base de pescado típico costero", color = TextSecondary, fontSize = 11.sp)
          Text("• Teatro \"SUPER PEZ\" héroe de la nutrición escolar", color = TextSecondary, fontSize = 11.sp)
          Text("• Maquetas de ecosistemas marinos y esculturas marinas", color = TextSecondary, fontSize = 11.sp)
          Text("• Reflexión sobre el cuidado de la biodiversidad marina", color = TextSecondary, fontSize = 11.sp)
          Text("• Juegos interactivos: Pesca magnética y Memotest marino", color = TextSecondary, fontSize = 11.sp)
        }

        Text(
          text = "\"¡Cuidemos el mar, nuestro mayor tesoro! Juntos por una vida saludable y sostenible.\"",
          color = MarineGreen,
          fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
          textAlign = TextAlign.Center,
          fontSize = 11.sp
        )

        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanDeep),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("ENTENDIDO", fontWeight = FontWeight.Black)
        }
      }
    }
  }
}
