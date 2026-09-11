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
  val eventTitle: String = "Expedición Marina",
  val eventDescription: String = "Transporte a zona de pesca piurana",
  val isRealGpsMode: Boolean = false,
  val initialBeacons: List<MarineBeacon>
)

object PiuraCoastalZones {

  fun generatePersonalBeacons(lat: Double, lng: Double): List<MarineBeacon> {
    val speciesList = MarineDatabase.speciesList
    val sCaballa = speciesList.firstOrNull { it.id == "caballa" } ?: speciesList[0]
    val sBonito = speciesList.firstOrNull { it.id == "bonito" } ?: speciesList[0]
    val sJurel = speciesList.firstOrNull { it.id == "jurel" } ?: speciesList[0]
    val sCachema = speciesList.firstOrNull { it.id == "cachema" } ?: speciesList[0]
    val sCabrilla = speciesList.firstOrNull { it.id == "cabrilla" } ?: speciesList[0]
    val sCamotillo = speciesList.firstOrNull { it.id == "camotillo" } ?: speciesList[0]
    val sMero = speciesList.firstOrNull { it.id == "mero_murike" } ?: speciesList[0]
    val sSuperPez = speciesList.firstOrNull { it.id == "super_pez" } ?: speciesList.last()

    return listOf(
      MarineBeacon(
        id = "gps_b_1",
        name = "Cardumen de Caballa (Entorno Real)",
        zoneName = "Tu Ubicación Real",
        description = "Cardumen activo nadando cerca de tus coordenadas físicas en tiempo real.",
        distanceMeters = 24,
        angleDegrees = 45f,
        latitude = lat + 0.00022,
        longitude = lng + 0.00020,
        species = sCaballa,
        anomalyLevel = MarineBeacon.AnomalyLevel.LEVE
      ),
      MarineBeacon(
        id = "gps_b_2",
        name = "Bonito del Norte (Señal Local)",
        zoneName = "Tu Ubicación Real",
        description = "Pescado azul rico en hierro detectado a pocos pasos de donde estás.",
        distanceMeters = 38,
        angleDegrees = 135f,
        latitude = lat - 0.00030,
        longitude = lng + 0.00028,
        species = sBonito,
        anomalyLevel = MarineBeacon.AnomalyLevel.MEDIA
      ),
      MarineBeacon(
        id = "gps_b_3",
        name = "Jurel Veloz del Pacífico",
        zoneName = "Tu Ubicación Real",
        description = "Movimiento ágil en la superficie marina alrededor de tu posición.",
        distanceMeters = 54,
        angleDegrees = 220f,
        latitude = lat - 0.00042,
        longitude = lng - 0.00035,
        species = sJurel,
        anomalyLevel = MarineBeacon.AnomalyLevel.MEDIA
      ),
      MarineBeacon(
        id = "gps_b_4",
        name = "Cachema Plateada Costera",
        zoneName = "Tu Ubicación Real",
        description = "Eco acústico claro sobre fondo local a tu alrededor.",
        distanceMeters = 42,
        angleDegrees = 310f,
        latitude = lat + 0.00035,
        longitude = lng - 0.00025,
        species = sCachema,
        anomalyLevel = MarineBeacon.AnomalyLevel.LEVE
      ),
      MarineBeacon(
        id = "gps_b_5",
        name = "Mero Murique de Profundidad",
        zoneName = "Tu Ubicación Real",
        description = "Firma de sonar masiva detectada en tu área física.",
        distanceMeters = 68,
        angleDegrees = 15f,
        latitude = lat + 0.00055,
        longitude = lng + 0.00010,
        species = sMero,
        anomalyLevel = MarineBeacon.AnomalyLevel.ALTA
      ),
      MarineBeacon(
        id = "gps_b_6",
        name = "Súper Pez (Nutrición Escolar)",
        zoneName = "Tu Ubicación Real",
        description = "¡Aura dorada y protectora del Omega-3 emanando directamente en tu ubicación!",
        distanceMeters = 78,
        angleDegrees = 280f,
        latitude = lat + 0.00015,
        longitude = lng - 0.00060,
        species = sSuperPez,
        anomalyLevel = MarineBeacon.AnomalyLevel.HEROICA
      )
    )
  }

  fun createPersonalGpsZone(lat: Double = -5.1970, lng: Double = -80.6350): CoastalZone {
    return CoastalZone(
      id = "gps_personal",
      name = "Mi GPS en Vivo",
      province = "Juego Personal Real",
      latitude = lat,
      longitude = lng,
      signatureDish = "Peces de tu Entorno Real",
      depthRange = "Superficie Costera",
      eventTitle = "📍 Mi GPS en Vivo (Juego Personal)",
      eventDescription = "Sea donde sea que estés físicamente, sigue tu ubicación y dirección real por satélite en tiempo real.",
      isRealGpsMode = true,
      initialBeacons = generatePersonalBeacons(lat, lng)
    )
  }

  val defaultZones: List<CoastalZone> = listOf(
    createPersonalGpsZone(),
    CoastalZone(
      id = "feria_piura",
      name = "Feria 'Sabores del Mar' Piura",
      province = "I.E. Santa Josefina, Piura",
      latitude = -5.1945,
      longitude = -80.6328,
      signatureDish = "Ruta Gastronómica & Stands de Nutrición",
      depthRange = "Base Central",
      eventTitle = "🎪 Evento: Feria Gastronómica Central",
      eventDescription = "Teletransporte a la base central de la feria escolar en Piura con degustación y torneos.",
      isRealGpsMode = false,
      initialBeacons = listOf(
        MarineBeacon(
          id = "b_feria_1",
          name = "Stand Súper Pez (Nutrición)",
          zoneName = "Feria 'Sabores del Mar' Piura",
          description = "¡Energía dorada del guardián del Omega-3 y la lucha contra la anemia!",
          distanceMeters = 18,
          angleDegrees = 40f,
          latitude = -5.1943,
          longitude = -80.6325,
          species = MarineDatabase.speciesList.first { it.id == "super_pez" },
          anomalyLevel = MarineBeacon.AnomalyLevel.HEROICA
        ),
        MarineBeacon(
          id = "b_feria_2",
          name = "Stand Caballa Piurana",
          zoneName = "Feria 'Sabores del Mar' Piura",
          description = "Degustación de caballa frita con chifles y zarza criolla.",
          distanceMeters = 28,
          angleDegrees = 145f,
          latitude = -5.1947,
          longitude = -80.6323,
          species = MarineDatabase.speciesList.first { it.id == "caballa" },
          anomalyLevel = MarineBeacon.AnomalyLevel.LEVE
        ),
        MarineBeacon(
          id = "b_feria_3",
          name = "Stand Bonito del Norte",
          zoneName = "Feria 'Sabores del Mar' Piura",
          description = "Infografías nutricionales: el pescado azul más rico en hierro de la costa norte.",
          distanceMeters = 35,
          angleDegrees = 275f,
          latitude = -5.1942,
          longitude = -80.6332,
          species = MarineDatabase.speciesList.first { it.id == "bonito" },
          anomalyLevel = MarineBeacon.AnomalyLevel.MEDIA
        )
      )
    ),
    CoastalZone(
      id = "organos_nuro",
      name = "Los Órganos & El Ñuro",
      province = "Talara, Piura",
      latitude = -4.1755,
      longitude = -81.1280,
      signatureDish = "Sudado de Mero & Pescado a la Plancha",
      depthRange = "5 - 35 m",
      eventTitle = "🐢 Evento: Santuario Los Órganos & El Ñuro",
      eventDescription = "Teletransporte directo al muelle de El Ñuro. Protege la Tortuga Verde y rastrea al Mero Gigante.",
      isRealGpsMode = false,
      initialBeacons = listOf(
        MarineBeacon(
          id = "b_nuro_1",
          name = "Santuario de Tortuga Verde Marina",
          zoneName = "Los Órganos & El Ñuro",
          description = "Tortuga Verde nadando apaciblemente entre las aguas turquesas del muelle de El Ñuro.",
          distanceMeters = 24,
          angleDegrees = 75f,
          latitude = -4.1760,
          longitude = -81.1270,
          species = MarineDatabase.speciesList.first { it.id == "tortuga_nuro" },
          anomalyLevel = MarineBeacon.AnomalyLevel.MEDIA
        ),
        MarineBeacon(
          id = "b_nuro_2",
          name = "Mero Murique de Arrecife Profundo",
          zoneName = "Los Órganos & El Ñuro",
          description = "Eco acústico masivo en las grietas rocosas bajo el muelle de Los Órganos.",
          distanceMeters = 58,
          angleDegrees = 220f,
          latitude = -4.1740,
          longitude = -81.1295,
          species = MarineDatabase.speciesList.first { it.id == "mero_murike" },
          anomalyLevel = MarineBeacon.AnomalyLevel.ALTA
        ),
        MarineBeacon(
          id = "b_nuro_3",
          name = "Cabrilla de Peña Costera",
          zoneName = "Los Órganos & El Ñuro",
          description = "Habitante de las restingas coralinas de la costa norte.",
          distanceMeters = 42,
          angleDegrees = 330f,
          latitude = -4.1748,
          longitude = -81.1265,
          species = MarineDatabase.speciesList.first { it.id == "cabrilla" },
          anomalyLevel = MarineBeacon.AnomalyLevel.LEVE
        )
      )
    ),
    CoastalZone(
      id = "mancora",
      name = "Bahía de Máncora",
      province = "Talara, Piura",
      latitude = -4.1067,
      longitude = -81.0478,
      signatureDish = "Ceviche de Bonito & Tiradito Norteño",
      depthRange = "10 - 45 m",
      eventTitle = "🌊 Evento: Bahía de Máncora y Rompientes",
      eventDescription = "Teletransporte a las olas de Máncora para cardúmenes veloces de Bonito y Caballa.",
      isRealGpsMode = false,
      initialBeacons = listOf(
        MarineBeacon(
          id = "b_mancora_1",
          name = "Cardumen de Bonito Veloz",
          zoneName = "Bahía de Máncora",
          description = "Cardumen nadando a gran velocidad cerca de la rompiente costera.",
          distanceMeters = 30,
          angleDegrees = 45f,
          latitude = -4.1055,
          longitude = -81.0490,
          species = MarineDatabase.speciesList.first { it.id == "bonito" },
          anomalyLevel = MarineBeacon.AnomalyLevel.MEDIA
        ),
        MarineBeacon(
          id = "b_mancora_2",
          name = "Banco de Caballa del Pacífico",
          zoneName = "Bahía de Máncora",
          description = "Destellos esmeralda bajo la superficie. Pescado azul rico en Omega-3.",
          distanceMeters = 52,
          angleDegrees = 160f,
          latitude = -4.1080,
          longitude = -81.0465,
          species = MarineDatabase.speciesList.first { it.id == "caballa" },
          anomalyLevel = MarineBeacon.AnomalyLevel.LEVE
        ),
        MarineBeacon(
          id = "b_mancora_3",
          name = "Firma Heroica Súper Pez",
          zoneName = "Bahía de Máncora",
          description = "¡Ondas doradas bioluminiscentes emergiendo en la bahía de Máncora!",
          distanceMeters = 74,
          angleDegrees = 290f,
          latitude = -4.1040,
          longitude = -81.0505,
          species = MarineDatabase.speciesList.first { it.id == "super_pez" },
          anomalyLevel = MarineBeacon.AnomalyLevel.HEROICA
        )
      )
    ),
    CoastalZone(
      id = "cabo_blanco",
      name = "Caleta Cabo Blanco",
      province = "Talara, Piura",
      latitude = -4.2514,
      longitude = -81.2336,
      signatureDish = "Sudado de Cabrilla & Mero Murique",
      depthRange = "15 - 90 m",
      eventTitle = "🎣 Evento Legendario: Cabo Blanco & El Rey del Mar",
      eventDescription = "Teletransporte al histórico muelle de Cabo Blanco. Captura al Mero Murique y a la Cabrilla de Peña.",
      isRealGpsMode = false,
      initialBeacons = listOf(
        MarineBeacon(
          id = "b_cabo_1",
          name = "Mero Murique Titánico",
          zoneName = "Caleta Cabo Blanco",
          description = "El legendario gigante de Cabo Blanco acechando en los arrecifes profundos.",
          distanceMeters = 44,
          angleDegrees = 120f,
          latitude = -4.2520,
          longitude = -81.2325,
          species = MarineDatabase.speciesList.first { it.id == "mero_murike" },
          anomalyLevel = MarineBeacon.AnomalyLevel.ALTA
        ),
        MarineBeacon(
          id = "b_cabo_2",
          name = "Cabrilla de Peña",
          zoneName = "Caleta Cabo Blanco",
          description = "Pez de cantiles rocosos y peñas sumergidas de carne blanca y firme.",
          distanceMeters = 32,
          angleDegrees = 250f,
          latitude = -4.2505,
          longitude = -81.2350,
          species = MarineDatabase.speciesList.first { it.id == "cabrilla" },
          anomalyLevel = MarineBeacon.AnomalyLevel.MEDIA
        ),
        MarineBeacon(
          id = "b_cabo_3",
          name = "Camotillo del Cantil",
          zoneName = "Caleta Cabo Blanco",
          description = "Ágil pez de roca con destellos celestes y aleta dorsal erizada.",
          distanceMeters = 60,
          angleDegrees = 15f,
          latitude = -4.2530,
          longitude = -81.2310,
          species = MarineDatabase.speciesList.first { it.id == "camotillo" },
          anomalyLevel = MarineBeacon.AnomalyLevel.LEVE
        )
      )
    ),
    CoastalZone(
      id = "paita",
      name = "Puerto de Paita & La Islilla",
      province = "Paita, Piura",
      latitude = -5.0892,
      longitude = -81.1144,
      signatureDish = "Ceviche de Cachema & Jurel Frito",
      depthRange = "12 - 60 m",
      eventTitle = "⚓ Evento: Puerto de Paita y Grandes Cardúmenes",
      eventDescription = "Teletransporte a la bahía de Paita para rastrear cardúmenes masivos de Jurel del Pacífico y Cachema.",
      isRealGpsMode = false,
      initialBeacons = listOf(
        MarineBeacon(
          id = "b_paita_1",
          name = "Gran Cardumen de Jurel del Pacífico",
          zoneName = "Puerto de Paita & La Islilla",
          description = "Rápido cardumen azul metálico patrullando la bahía pesquera de Paita.",
          distanceMeters = 36,
          angleDegrees = 80f,
          latitude = -5.0880,
          longitude = -81.1130,
          species = MarineDatabase.speciesList.first { it.id == "jurel" },
          anomalyLevel = MarineBeacon.AnomalyLevel.MEDIA
        ),
        MarineBeacon(
          id = "b_paita_2",
          name = "Cachema Plateada",
          zoneName = "Puerto de Paita & La Islilla",
          description = "Señal acústica clara sobre lechos de arena fina en la bahía de Paita.",
          distanceMeters = 46,
          angleDegrees = 210f,
          latitude = -5.0905,
          longitude = -81.1160,
          species = MarineDatabase.speciesList.first { it.id == "cachema" },
          anomalyLevel = MarineBeacon.AnomalyLevel.LEVE
        ),
        MarineBeacon(
          id = "b_paita_3",
          name = "Caballa de Alta Mar",
          zoneName = "Puerto de Paita & La Islilla",
          description = "Pescado azul nadando en las corrientes frías frente a Paita.",
          distanceMeters = 68,
          angleDegrees = 315f,
          latitude = -5.0875,
          longitude = -81.1170,
          species = MarineDatabase.speciesList.first { it.id == "caballa" },
          anomalyLevel = MarineBeacon.AnomalyLevel.MEDIA
        )
      )
    ),
    CoastalZone(
      id = "colan",
      name = "Playa Colán & Bocana",
      province = "Paita, Piura",
      latitude = -5.0167,
      longitude = -81.0667,
      signatureDish = "Ceviche de Camotillo & Sudado Mixto",
      depthRange = "4 - 25 m",
      eventTitle = "🏖️ Evento: Arrecifes de Playa Colán",
      eventDescription = "Teletransporte a las aguas cálidas de Colán para encontrar al codiciado Camotillo piurano.",
      isRealGpsMode = false,
      initialBeacons = listOf(
        MarineBeacon(
          id = "b_colan_1",
          name = "Camotillo de Fondo Arenoso",
          zoneName = "Playa Colán & Bocana",
          description = "Pez de arena y roca de carne delicada muy apreciada en la gastronomía piurana.",
          distanceMeters = 28,
          angleDegrees = 95f,
          latitude = -5.0175,
          longitude = -81.0655,
          species = MarineDatabase.speciesList.first { it.id == "camotillo" },
          anomalyLevel = MarineBeacon.AnomalyLevel.MEDIA
        ),
        MarineBeacon(
          id = "b_colan_2",
          name = "Cabrilla Costera",
          zoneName = "Playa Colán & Bocana",
          description = "Frecuenta las zonas de peña y orilla durante la marea alta.",
          distanceMeters = 50,
          angleDegrees = 240f,
          latitude = -5.0155,
          longitude = -81.0680,
          species = MarineDatabase.speciesList.first { it.id == "cabrilla" },
          anomalyLevel = MarineBeacon.AnomalyLevel.LEVE
        )
      )
    ),
    CoastalZone(
      id = "sechura",
      name = "Bahía de Sechura & Parachique",
      province = "Sechura, Piura",
      latitude = -5.5583,
      longitude = -80.8222,
      signatureDish = "Cachema Frita & Conchas Negras",
      depthRange = "6 - 40 m",
      eventTitle = "🦀 Evento: Bahía de Sechura & Estuarios",
      eventDescription = "Teletransporte al sur de Piura en Sechura. Gran riqueza estuarina y cardúmenes de Cachema.",
      isRealGpsMode = false,
      initialBeacons = listOf(
        MarineBeacon(
          id = "b_sechura_1",
          name = "Cachema de la Bahía de Sechura",
          zoneName = "Bahía de Sechura & Parachique",
          description = "Firma limpia en fondos arenosos de Sechura. Clásico de la cocina piurana.",
          distanceMeters = 30,
          angleDegrees = 115f,
          latitude = -5.5590,
          longitude = -80.8210,
          species = MarineDatabase.speciesList.first { it.id == "cachema" },
          anomalyLevel = MarineBeacon.AnomalyLevel.MEDIA
        ),
        MarineBeacon(
          id = "b_sechura_2",
          name = "Jurel de Parachique",
          zoneName = "Bahía de Sechura & Parachique",
          description = "Cardumen activo cerca a la bocana de Parachique.",
          distanceMeters = 56,
          angleDegrees = 285f,
          latitude = -5.5570,
          longitude = -80.8240,
          species = MarineDatabase.speciesList.first { it.id == "jurel" },
          anomalyLevel = MarineBeacon.AnomalyLevel.LEVE
        )
      )
    )
  )
}
