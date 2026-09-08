package com.example.model

import androidx.annotation.DrawableRes
import com.example.R

data class FishSpecies(
  val id: String,
  val commonName: String,
  val scientificName: String,
  val habitat: String,
  val rarity: Rarity,
  val attackSpeed: Float, // speed of charge in encounter
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
      id = "mero_murike",
      commonName = "Mero Murike",
      scientificName = "Epinephelus quinquefasciatus",
      habitat = "Arrecifes rocosos de Los Órganos y Paita",
      rarity = FishSpecies.Rarity.RARO,
      attackSpeed = 1.3f,
      energyRequired = 25,
      nutritionalBenefits = listOf("Alto en proteínas magras", "Rico en fósforo y potasio", "Bajo en grasas saturadas"),
      traditionalRecipe = "Ceviche tradicional de Mero con limón de Chulucanas y ají limo piurano",
      fairStandInfo = "Feria Gastronómica: Stand 01 - 'El Rey del Muelle'",
      quote = "¡Caserito, el verdadero mero norteño de carne firme y blanca!",
      imageRes = R.drawable.img_real_mero_3d
    ),
    FishSpecies(
      id = "cabrilla",
      commonName = "Cabrilla de Peña",
      scientificName = "Paralabrax humeralis",
      habitat = "Fondos rocosos de Colán y Cabo Blanco",
      rarity = FishSpecies.Rarity.ESPECIAL,
      attackSpeed = 1.0f,
      energyRequired = 15,
      nutritionalBenefits = listOf("Excelente fuente de Omega-3", "Favorece la salud cardiovascular", "Vitamina B12 y Selenio"),
      traditionalRecipe = "Sudado de Cabrilla a la piurana con chicha de jora, yuca y culantro",
      fairStandInfo = "Feria Gastronómica: Stand 03 - 'Sabores Ancestrales San Josefina'",
      quote = "¡El secreto del sudado está en el punto justo de la chicha de jora!",
      imageRes = R.drawable.img_real_cabrilla_3d
    ),
    FishSpecies(
      id = "bonito",
      commonName = "Bonito del Pacífico",
      scientificName = "Sarda chiliensis",
      habitat = "Aguas abiertas de Sechura y Bahía de Paita",
      rarity = FishSpecies.Rarity.COMUN,
      attackSpeed = 1.6f,
      energyRequired = 10,
      nutritionalBenefits = listOf("Rico en Hierro y prevención de anemia", "Proteínas de alto valor biológico", "Fortalece las defensas e inmunidad"),
      traditionalRecipe = "Escabeche de Bonito o Tiradito norteño con camote glaseado",
      fairStandInfo = "Infografías Nutricionales: Área de Ciencias y Salud",
      quote = "¡Pescado azul de los campeones, energía pura del mar de Grau!",
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
      nutritionalBenefits = listOf("¡Especie protegida!", "Indicador de ecosistemas oceánicos saludables", "Guardianes de la biodiversidad"),
      traditionalRecipe = "NO PARA CONSUMO - Visita y nado ecológico en la caleta El Ñuro",
      fairStandInfo = "Exposición 'Cuidado de la Biodiversidad y Valores Cristianos'",
      quote = "¡Cuidemos a nuestras tortugas, el tesoro vivo de El Ñuro!",
      imageRes = R.drawable.img_real_tortuga_3d,
      isSanctuaryProtected = true
    ),
    FishSpecies(
      id = "super_pez",
      commonName = "Súper Pez",
      scientificName = "Piscis Heroicus Josefina",
      habitat = "Feria 'Sabores del Mar' - Comunidad San Josefina",
      rarity = FishSpecies.Rarity.LEGENDARIO,
      attackSpeed = 1.5f,
      energyRequired = 50,
      nutritionalBenefits = listOf("Héroe de la nutrición escolar", "Fuerza infinita de Omega-3", "Campeón contra la desnutrición y anemia"),
      traditionalRecipe = "Dieta balanceada con pescado 3 veces por semana para toda la familia",
      fairStandInfo = "Teatro 'SÚPER PEZ' - Héroe de la alimentación saludable",
      quote = "¡Soy Súper Pez! ¡Protegiendo la nutrición de los niños de Piura!",
      imageRes = R.drawable.img_real_super_pez_3d
    )
  )
}
