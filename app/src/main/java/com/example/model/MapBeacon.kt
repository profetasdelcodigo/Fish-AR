package com.example.model

data class MarineBeacon(
  val id: String,
  val name: String,
  val zoneName: String,
  val description: String,
  val distanceMeters: Int,
  val angleDegrees: Float, // relative to player 0..360
  val latitude: Double,
  val longitude: Double,
  val species: FishSpecies,
  val anomalyLevel: AnomalyLevel,
  val isDiscovered: Boolean = false
) {
  enum class AnomalyLevel(val label: String, val colorHex: Long) {
    LEVE("Señal Leve", 0xFF00E5FF),
    MEDIA("Cardumen Activo", 0xFFFFC107),
    ALTA("Turbulencia Profunda", 0xFFFF5252),
    HEROICA("Señal Legendaria", 0xFFE040FB)
  }
}

data class CoastalZone(
  val id: String,
  val name: String,
  val province: String,
  val latitude: Double,
  val longitude: Double,
  val signatureDish: String,
  val depthRange: String,
  val initialBeacons: List<MarineBeacon>
)

object PiuraCoastalZones {
  val defaultZones = listOf(
    CoastalZone(
      id = "san_josefina",
      name = "Feria Escolar San Josefina",
      province = "Piura Centro",
      latitude = -5.1970,
      longitude = -80.6350,
      signatureDish = "Degustación Escolar & Ceviche",
      depthRange = "Patios y Muelle de la Feria",
      initialBeacons = listOf(
        MarineBeacon(
          id = "b1",
          name = "Ecosistema Educativo",
          zoneName = "Patio de Ciencias",
          description = "¡Señal detectada junto a la maqueta de corrientes marinas de Humboldt!",
          distanceMeters = 24,
          angleDegrees = 45f,
          latitude = -5.1972,
          longitude = -80.6348,
          species = PiuraMarineDatabase.speciesList.first { it.id == "bonito" },
          anomalyLevel = MarineBeacon.AnomalyLevel.LEVE
        ),
        MarineBeacon(
          id = "b2",
          name = "Escenario Teatro 'Súper Pez'",
          zoneName = "Auditorio Central",
          description = "¡Energía heroica emanando del escenario principal de nutrición!",
          distanceMeters = 65,
          angleDegrees = 180f,
          latitude = -5.1975,
          longitude = -80.6353,
          species = PiuraMarineDatabase.speciesList.first { it.id == "super_pez" },
          anomalyLevel = MarineBeacon.AnomalyLevel.HEROICA
        )
      )
    ),
    CoastalZone(
      id = "cabo_blanco",
      name = "Cabo Blanco & El Ñuro",
      province = "Talara",
      latitude = -4.2547,
      longitude = -81.2292,
      signatureDish = "Ceviche de Mero Murike",
      depthRange = "15m - 50m (Arrecife Profundo)",
      initialBeacons = listOf(
        MarineBeacon(
          id = "b3",
          name = "Bajo de Peña Cabo Blanco",
          zoneName = "Arrecife Norte",
          description = "Vibración sónica masiva entre formaciones de roca submarina.",
          distanceMeters = 110,
          angleDegrees = 290f,
          latitude = -4.2560,
          longitude = -81.2310,
          species = PiuraMarineDatabase.speciesList.first { it.id == "mero_murike" },
          anomalyLevel = MarineBeacon.AnomalyLevel.ALTA
        ),
        MarineBeacon(
          id = "b4",
          name = "Caleta Turística El Ñuro",
          zoneName = "Muelle Artesanal",
          description = "Nado suave detectado cerca de los pilotes de madera.",
          distanceMeters = 80,
          angleDegrees = 135f,
          latitude = -4.2181,
          longitude = -81.1822,
          species = PiuraMarineDatabase.speciesList.first { it.id == "tortuga_nuro" },
          anomalyLevel = MarineBeacon.AnomalyLevel.MEDIA
        )
      )
    ),
    CoastalZone(
      id = "paita_colan",
      name = "Bahía de Paita & Colán",
      province = "Paita",
      latitude = -5.0892,
      longitude = -81.1075,
      signatureDish = "Sudado de Cabrilla Piurano",
      depthRange = "8m - 25m (Zona Costera)",
      initialBeacons = listOf(
        MarineBeacon(
          id = "b5",
          name = "Roca Esmeralda de Colán",
          zoneName = "Punta de Peña",
          description = "Cardumen acechando bancos de arena de aguas cálidas.",
          distanceMeters = 48,
          angleDegrees = 90f,
          latitude = -5.0920,
          longitude = -81.1120,
          species = PiuraMarineDatabase.speciesList.first { it.id == "cabrilla" },
          anomalyLevel = MarineBeacon.AnomalyLevel.MEDIA
        )
      )
    )
  )
}
