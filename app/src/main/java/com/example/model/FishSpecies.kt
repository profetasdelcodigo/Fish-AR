package com.example.model

import androidx.annotation.DrawableRes
import com.example.R

data class FishSpecies(
  val id: String,
  val commonName: String,
  val scientificName: String,
  val habitat: String,
  val rarity: Rarity,
  val attackSpeed: Float,
  val energyRequired: Int,
  val nutritionalBenefits: List<String>,
  val traditionalRecipe: String,
  val fairStandInfo: String,
  val quote: String,
  @DrawableRes val imageRes: Int,
  val baseHealth: Int = 100,
  val isSanctuaryProtected: Boolean = false
) {
  enum class Rarity(val label: String, val colorHex: Long) {
    COMUN("Común", 0xFF00E5FF),
    ESPECIAL("Especial", 0xFF00E676),
    RARO("Raro de Arrecife", 0xFFFFC107),
    LEGENDARIO("Legendario Marino", 0xFFFF5252),
    PROTEGIDO("Santuario Protegido", 0xFF7C4DFF)
  }
}

object PiuraMarineDatabase {
  val speciesList = listOf(
    FishSpecies(
      id = "caballa",
      commonName = "Caballa del Pacífico",
      scientificName = "Scomber japonicus",
      habitat = "Aguas costeras de Paita, Sechura y el litoral piurano",
      rarity = FishSpecies.Rarity.COMUN,
      attackSpeed = 1.45f,
      energyRequired = 10,
      nutritionalBenefits = listOf("Pescado azul rico en Omega-3", "Proteínas de alto valor", "Fuente de vitamina B12"),
      traditionalRecipe = "Caballa frita o ceviche piurano con yuca, camote y cebolla roja",
      fairStandInfo = "Ruta Gastronómica Piurana · Estación Caballa",
      quote = "Una de las especies marinas más representativas de la mesa piurana.",
      imageRes = R.drawable.fish_caballa
    ),
    FishSpecies(
      id = "cachema",
      commonName = "Cachema",
      scientificName = "Cynoscion analis",
      habitat = "Fondos arenosos y zonas costeras de Sechura y Paita",
      rarity = FishSpecies.Rarity.ESPECIAL,
      attackSpeed = 1.15f,
      energyRequired = 14,
      nutritionalBenefits = listOf("Proteína magra", "Aporta fósforo y selenio", "Textura firme para cocina local"),
      traditionalRecipe = "Cachema frita con arroz, zarandaja, yuca y salsa criolla",
      fairStandInfo = "Ruta Gastronómica Piurana · Estación Cachema",
      quote = "La detectamos por su firma acústica limpia en fondos costeros.",
      imageRes = R.drawable.fish_cachema
    ),
    FishSpecies(
      id = "jurel",
      commonName = "Jurel del Pacífico",
      scientificName = "Trachurus murphyi",
      habitat = "Aguas abiertas y costeras frente a Paita y Sechura",
      rarity = FishSpecies.Rarity.ESPECIAL,
      attackSpeed = 1.55f,
      energyRequired = 16,
      nutritionalBenefits = listOf("Pescado azul", "Proteínas y grasas saludables", "Aporta Omega-3"),
      traditionalRecipe = "Jurel frito, ceviche o guiso norteño con yuca y arroz",
      fairStandInfo = "Ruta Gastronómica Piurana · Estación Jurel",
      quote = "Su cardumen produce una señal intensa: mantén el sonar activo.",
      imageRes = R.drawable.fish_jurel
    ),
    FishSpecies(
      id = "cachema_cabrilla",
      commonName = "Cabrilla de Peña",
      scientificName = "Paralabrax humeralis",
      habitat = "Fondos rocosos de Colán, Cabo Blanco y Paita",
      rarity = FishSpecies.Rarity.ESPECIAL,
      attackSpeed = 1.0f,
      energyRequired = 15,
      nutritionalBenefits = listOf("Proteína magra", "Aporta fósforo y selenio", "Carne firme"),
      traditionalRecipe = "Sudado de Cabrilla a la piurana con chicha de jora, yuca y culantro",
      fairStandInfo = "Feria Gastronómica · Stand Cabrilla",
      quote = "No desperdicies energía: espera la ventana de embestida.",
      imageRes = R.drawable.img_real_cabrilla_3d
    ),
    FishSpecies(
      id = "camotillo",
      commonName = "Camotillo",
      scientificName = "Diplectrum eumelum",
      habitat = "Fondos rocosos y arenosos del litoral norte",
      rarity = FishSpecies.Rarity.RARO,
      attackSpeed = 0.92f,
      energyRequired = 18,
      nutritionalBenefits = listOf("Proteína magra", "Carne apreciada en ceviche", "Buen aporte proteico"),
      traditionalRecipe = "Ceviche de Camotillo con limón, ají limo, cebolla, camote y choclo",
      fairStandInfo = "Ruta Gastronómica Piurana · Estación Camotillo",
      quote = "Pequeño, rápido y difícil de distinguir entre la estática.",
      imageRes = R.drawable.fish_camotillo
    ),
    FishSpecies(
      id = "mero_murike",
      commonName = "Mero Murike",
      scientificName = "Epinephelus quinquefasciatus",
      habitat = "Arrecifes rocosos de Los Órganos y Paita",
      rarity = FishSpecies.Rarity.RARO,
      attackSpeed = 1.3f,
      energyRequired = 25,
      nutritionalBenefits = listOf("Alto en proteínas magras", "Rico en fósforo y potasio", "Bajo en grasas saturadas"),
      traditionalRecipe = "Ceviche tradicional de Mero con limón de Chulucanas y ají limo piurano",
      fairStandInfo = "Feria Gastronómica: Stand 01 - El Rey del Muelle",
      quote = "Caserito, el verdadero mero norteño de carne firme y blanca.",
      imageRes = R.drawable.img_real_mero_3d
    ),
    FishSpecies(
      id = "bonito",
      commonName = "Bonito del Pacífico",
      scientificName = "Sarda chiliensis chiliensis",
      habitat = "Aguas abiertas de Sechura y Bahía de Paita",
      rarity = FishSpecies.Rarity.COMUN,
      attackSpeed = 1.6f,
      energyRequired = 10,
      nutritionalBenefits = listOf("Rico en hierro", "Proteínas de alto valor biológico", "Fuente de Omega-3"),
      traditionalRecipe = "Escabeche de Bonito o tiradito norteño con camote",
      fairStandInfo = "Infografías Nutricionales · Área de Ciencias y Salud",
      quote = "Energía pura del mar de Grau.",
      imageRes = R.drawable.img_real_bonito_3d
    ),
    FishSpecies(
      id = "tortuga_nuro",
      commonName = "Tortuga Verde Marina",
      scientificName = "Chelonia mydas",
      habitat = "Santuario Marino El Ñuro - Los Órganos",
      rarity = FishSpecies.Rarity.PROTEGIDO,
      attackSpeed = 0.8f,
      energyRequired = 30,
      nutritionalBenefits = listOf("Especie protegida", "Indicador de ecosistemas saludables", "Guardianes de la biodiversidad"),
      traditionalRecipe = "NO PARA CONSUMO - Observación y nado ecológico en El Ñuro",
      fairStandInfo = "Exposición Cuidado de la Biodiversidad",
      quote = "Cuidemos el tesoro vivo de El Ñuro.",
      imageRes = R.drawable.img_real_tortuga_3d,
      isSanctuaryProtected = true
    ),
    FishSpecies(
      id = "super_pez",
      commonName = "Súper Pez",
      scientificName = "Piscis Heroicus Josefina",
      habitat = "Feria Sabores del Mar - Comunidad San Josefina",
      rarity = FishSpecies.Rarity.LEGENDARIO,
      attackSpeed = 1.5f,
      energyRequired = 50,
      nutritionalBenefits = listOf("Héroe de la nutrición escolar", "Fuerza infinita de Omega-3", "Campeón contra la anemia"),
      traditionalRecipe = "Dieta balanceada con pescado varias veces por semana",
      fairStandInfo = "Teatro Súper Pez - Héroe de la alimentación saludable",
      quote = "Protegiendo la nutrición de los niños de Piura.",
      imageRes = R.drawable.img_real_super_pez_3d
    )
  )
}
