package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Waves
import androidx.compose.ui.graphics.vector.ImageVector

enum class GameModeType(
  val title: String,
  val subtitle: String,
  val icon: ImageVector,
  val badge: String
) {
  TORNEO_FERIA(
    title = "Torneo de Feria 6 Min",
    subtitle = "Contrarreloj Oficial • Tabla de Récords",
    icon = Icons.Default.Star,
    badge = "🏆 COMPETITIVO"
  ),
  DUELO_1VS1(
    title = "Duelo 1 vs 1 (Bluetooth/P2P)",
    subtitle = "Combate Coordinado • Rival en Vivo",
    icon = Icons.Default.Public,
    badge = "⚔️ MULTIJUGADOR"
  ),
  COOPERATIVO(
    title = "Modo Cooperativo Dúo",
    subtitle = "2 Celulares en la Misma Bahía • Casco Compartido",
    icon = Icons.Default.Group,
    badge = "🤝 TRABAJO EN EQUIPO"
  ),
  SOLITARIO(
    title = "Modo Solitario Libre",
    subtitle = "Exploración de Bahías y Combate AR",
    icon = Icons.Default.Explore,
    badge = "🧭 EXPEDICIÓN"
  )
}

data class TutorialStep(
  val stepNumber: Int,
  val title: String,
  val description: String,
  val icon: ImageVector,
  val tip: String
)

data class ModeTutorialData(
  val modeType: GameModeType,
  val introHeadline: String,
  val durationOrCondition: String,
  val steps: List<TutorialStep>,
  val scoringRules: List<String>
)

object ModeTutorialCatalog {
  val tutorials: Map<GameModeType, ModeTutorialData> = mapOf(
    GameModeType.TORNEO_FERIA to ModeTutorialData(
      modeType = GameModeType.TORNEO_FERIA,
      introHeadline = "¡Compite por el 1° Lugar en la Feria 'Sabores del Mar'!",
      durationOrCondition = "⏱️ Tiempo Límite: 6 Minutos Exactos (360s)",
      steps = listOf(
        TutorialStep(
          stepNumber = 1,
          title = "Registra tu Username / Nickname",
          description = "Escribe tu nombre o apodo para que tu puntaje quede grabado en la base de datos oficial de la feria.",
          icon = Icons.Default.Star,
          tip = "Tu récord aparecerá en la Tabla de Clasificación y el Podio de Medallas."
        ),
        TutorialStep(
          stepNumber = 2,
          title = "Captura Rápida de Peces Peruanos",
          description = "Los peces aparecerán sucesivamente. Localízalos con la orientación del celular y el sonar acústico.",
          icon = Icons.Default.Timer,
          tip = "Especies como el Mero Murike o el Súper Pez otorgan bonificaciones de hasta 500 puntos."
        ),
        TutorialStep(
          stepNumber = 3,
          title = "Multiplicadores de Combo (x1.5 / x2.0 / x3.0)",
          description = "Encadena capturas limpias con Choque Eléctrico en Zona Letal (<15m) sin recibir embestidas para multiplicar tu puntaje.",
          icon = Icons.Default.Bolt,
          tip = "Si esquivas el Frenesí (Haywire) desviando la vista, conservas tu multiplicador."
        )
      ),
      scoringRules = listOf(
        "🐟 Captura de Pez Menor (Caballa/Cabrilla): +150 pts",
        "🐟 Captura de Pez Mayor (Bonito/Jurel/Cachema): +250 pts",
        "👑 Captura Legendaria (Mero Murike/Súper Pez): +500 pts",
        "⚡ Choque Eléctrico Perfecto: +100 pts extra",
        "🔥 Combo x3: ¡Triplica todos los puntos de captura!"
      )
    ),

    GameModeType.DUELO_1VS1 to ModeTutorialData(
      modeType = GameModeType.DUELO_1VS1,
      introHeadline = "¡Enfrentamiento Táctico 1 vs 1 en Tiempo Real!",
      durationOrCondition = "⚔️ Sincronización Bluetooth / P2P entre 2 Celulares",
      steps = listOf(
        TutorialStep(
          stepNumber = 1,
          title = "Conexión Bluetooth P2P (Host o Unirse)",
          description = "Un jugador crea la sala táctica ('Anfitrión') y el segundo celular se une seleccionando el dispositivo emparejado.",
          icon = Icons.Default.Public,
          tip = "También puedes usar el modo 'Enlace Rápido Directo' para probar al instante."
        ),
        TutorialStep(
          stepNumber = 2,
          title = "Combate Simultáneo en Cuadrantes Separados",
          description = "Ambos cazan peces simultáneamente. Verás en tu pantalla el puntaje en vivo, capturas y combo del rival.",
          icon = Icons.Default.Waves,
          tip = "El marcador se actualiza en tiempo real con cada descarga."
        ),
        TutorialStep(
          stepNumber = 3,
          title = "Lanzamiento de Sabotajes",
          description = "Al lograr combos x2 puedes enviar 'Corriente Turbulenta' o 'Niebla Marina' a la pantalla del rival para entorpecer su visor.",
          icon = Icons.Default.Shield,
          tip = "¡Gana el jugador con mayor puntaje al finalizar el tiempo!"
        )
      ),
      scoringRules = listOf(
        "🏆 Victoria: Mayor puntaje acumulado al terminar la ronda",
        "⚡ Aturdimiento veloz: +150 pts y recarga de sabotaje",
        "🌪️ Sabotaje 'Corriente Marina': sacude el sensor del rival",
        "🌫️ Sabotaje 'Niebla Marina': reduce la visibilidad 3D del rival por 4s"
      )
    ),

    GameModeType.COOPERATIVO to ModeTutorialData(
      modeType = GameModeType.COOPERATIVO,
      introHeadline = "¡Dúo de Submarino en la Misma Partida Compartida!",
      durationOrCondition = "🤝 2 Celulares • 1 Mismo Ambiente Marino • 1 Mismo Casco",
      steps = listOf(
        TutorialStep(
          stepNumber = 1,
          title = "Asignación de Roles Tácticos",
          description = "Jugador 1 toma el rol de 'Operador de Choque & Sonar' y Jugador 2 toma el rol de 'Operador de Red & Señuelos'.",
          icon = Icons.Default.Group,
          tip = "Ambos ven al mismo pez en las mismas coordenadas espaciales."
        ),
        TutorialStep(
          stepNumber = 2,
          title = "Integridad del Casco Compartida (100%)",
          description = "Si el pez embiste al sumergible o no esquivan el Frenesí, el daño afectará la barra de vida de ambos dispositivos.",
          icon = Icons.Default.Shield,
          tip = "El Operador de Señuelo puede activar el Escudo Súper Pez para proteger a ambos."
        ),
        TutorialStep(
          stepNumber = 3,
          title = "Sinergia de Captura Doble",
          description = "El Jugador 1 aturde al pez con el Choque EMP, y el Jugador 2 activa la Red Magnética para sellar la captura.",
          icon = Icons.Default.Bolt,
          tip = "¡La puntuación y los ejemplares se guardan en el historial de ambos jugadores!"
        )
      ),
      scoringRules = listOf(
        "🤝 Captura Coordinada: +400 pts compartidos",
        "🛡️ Bloqueo de Embestida con Escudo: +150 pts",
        "⚡ Aturdimiento Sincronizado: +200 pts",
        "❤️ Supervivencia con Casco > 80%: Bonificación de Equipo +300 pts"
      )
    ),

    GameModeType.SOLITARIO to ModeTutorialData(
      modeType = GameModeType.SOLITARIO,
      introHeadline = "Modo Exploración y Pesca Submarina en Solitario",
      durationOrCondition = "🧭 Libre exploración por puertos del litoral peruano",
      steps = listOf(
        TutorialStep(
          stepNumber = 1,
          title = "Radar Sonar y Coordenadas Costeras",
          description = "Selecciona puertos emblemáticos (Máncora, El Ñuro, Cabo Blanco, Paita, Callao) o tu GPS real y toca las anomalías.",
          icon = Icons.Default.Explore,
          tip = "La intensidad acústica y el destello revelan el nivel de anomalía marina."
        ),
        TutorialStep(
          stepNumber = 2,
          title = "Cámara Submarina 3D & Giroscopio",
          description = "Mueve tu dispositivo o arrastra la pantalla para buscar al pez guiándote por la estática y el medidor de proximidad.",
          icon = Icons.Default.Waves,
          tip = "El pez nada a la altura del horizonte en 3D."
        ),
        TutorialStep(
          stepNumber = 3,
          title = "Mecánicas de Combate (Frenesí, Amago y Embestida)",
          description = "• Frenesí (Ojos rojos): Gira 35° para desviar la mirada.\n• Amago (Aura dorada): Espera a que se disuelva.\n• Embestida Real (Alarma roja): Espera <15m y dispara el Choque.",
          icon = Icons.Default.Bolt,
          tip = "Al aturdirlo, completa el minijuego de recogida manteniendo la tensión magnética."
        )
      ),
      scoringRules = listOf(
        "🪙 Ganancia de Pescacoins según la especie y energía requerida",
        "📖 Registro permanente en la Pescadex y datos nutricionales",
        "🔬 Recursos marinos para el Laboratorio de Equipamiento"
      )
    )
  )
}
