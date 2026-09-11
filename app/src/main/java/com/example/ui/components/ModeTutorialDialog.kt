package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.MarineSoundEngine
import com.example.model.GameModeType
import com.example.model.ModeTutorialCatalog
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MarineGold
import com.example.ui.theme.MarineGreen
import com.example.ui.theme.OceanAbyss
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanDeep
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ModeTutorialDialog(
  modeType: GameModeType,
  onDismiss: () -> Unit,
  onStartMode: () -> Unit
) {
  val tutorial = ModeTutorialCatalog.tutorials[modeType] ?: return

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .fillMaxHeight(0.88f)
        .clip(RoundedCornerShape(24.dp))
        .border(1.5.dp, MarineCyan.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
        .testTag("mode_tutorial_dialog"),
      color = OceanDeep
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        // Top Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MarineCyan.copy(alpha = 0.15f))
                .border(1.dp, MarineCyan, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(modeType.icon, contentDescription = null, tint = MarineCyan, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = modeType.title,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
              )
              Text(
                text = modeType.badge,
                color = MarineGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          IconButton(
            onClick = {
              MarineSoundEngine.playNavClick()
              onDismiss()
            },
            modifier = Modifier.testTag("close_tutorial_btn")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Headline & Duration badge
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(OceanAbyss)
            .padding(12.dp)
        ) {
          Column {
            Text(
              text = tutorial.introHeadline,
              color = MarineCyan,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = tutorial.durationOrCondition,
              color = MarineGreen,
              fontWeight = FontWeight.Medium,
              fontSize = 12.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Steps and Scoring list
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
          item {
            Text(
              text = "📋 PASO A PASO PARA JUGAR",
              color = TextSecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
          }

          items(tutorial.steps) { step ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              colors = CardDefaults.cardColors(containerColor = OceanCard),
              shape = RoundedCornerShape(14.dp)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.Top
              ) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MarineCyan.copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "${step.stepNumber}",
                    color = MarineCyan,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = step.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                  Spacer(modifier = Modifier.height(3.dp))
                  Text(
                    text = step.description,
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MarineGold, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = step.tip,
                      color = MarineGold,
                      fontSize = 10.5.sp,
                      fontWeight = FontWeight.Medium
                    )
                  }
                }
              }
            }
          }

          item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "🎯 REGLAS Y PUNTUACIÓN DE FERIA",
              color = TextSecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
          }

          items(tutorial.scoringRules) { rule ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Check, contentDescription = null, tint = MarineGreen, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = rule,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = {
              MarineSoundEngine.playNavClick()
              onDismiss()
            },
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("skip_tutorial_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
          ) {
            Text("Entendido", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }

          Button(
            onClick = {
              MarineSoundEngine.playNavClick()
              onStartMode()
            },
            modifier = Modifier
              .weight(1.3f)
              .height(48.dp)
              .testTag("start_from_tutorial_btn"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MarineCyan, contentColor = OceanAbyss)
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("¡COMENZAR!", fontWeight = FontWeight.Black, fontSize = 13.sp)
          }
        }
      }
    }
  }
}
