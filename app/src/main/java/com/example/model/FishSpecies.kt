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
    FishSpecies("caballa", "Caballa del Pacífico", "Scomber japonicus", "Aguas costeras de Paita y Sechura", FishSpecies.Rarity.COMUN, 1.45f, 10, listOf("Pescado azul rico en Omega-3", "Proteínas de alto valor", "Vitamina B12"), "Caballa frita o ceviche piurano con yuca y camote", "Ruta Gastronómica Piurana · Estación Caballa", "Una de las especies marinas más representativas de la mesa piurana.", R.drawable.fish_bonito),
    FishSpecies("cachema", "Cachema", "Cynoscion analis", "Fondos arenosos de Sechura y Paita", FishSpecies.Rarity.ESPECIAL, 1.15f, 14, listOf("Proteína magra", "Fósforo y selenio", "Carne firme"), "Cachema frita con arroz, zarandaja y yuca", "Ruta Gastronómica Piurana · Estación Cachema", "La detectamos por su firma acústica limpia en fondos costeros.", R.drawable.fish_mero),
    FishSpecies("jurel", "Jurel del Pacífico", "Trachurus murphyi", "Aguas abiertas frente a Paita y Sechura", FishSpecies.Rarity.ESPECIAL, 1.55f, 16, listOf("Pescado azul", "Proteínas y grasas saludables", "Omega-3"), "Jurel frito, ceviche o guiso norteño", "Ruta Gastronómica Piurana · Estación Jurel", "Su cardumen produce una señal intensa: mantén el sonar activo.", R.drawable.fish_bonito),
    FishSpecies("cabrilla", "Cabrilla de Peña", "Paralabrax humeralis", "Fondos rocosos de Colán, Cabo Blanco y Paita", FishSpecies.Rarity.ESPECIAL, 1.0f, 15, listOf("Proteína magra", "Fósforo y selenio", "Carne firme"), "Sudado de Cabrilla a la piurana con chicha de jora", "Feria Gastronómica · Stand Cabrilla", "No desperdicies energía: espera la ventana de embestida.", R.drawable.img_real_cabrilla_3d),
    FishSpecies("camotillo", "Camotillo", "Diplectrum eumelum", "Fondos rocosos y arenosos del litoral norte", FishSpecies.Rarity.RARO, 0.92f, 18, listOf("Proteína magra", "Carne apreciada en ceviche", "Buen aporte proteico"), "Ceviche de Camotillo con limón y ají limo", "Ruta Gastronómica Piurana · Estación Camotillo", "Pequeño, rápido y difícil de distinguir entre la estática.", R.drawable.fish_cabrilla),
    FishSpecies("mero_murike", "Mero Murike", "Epinephelus quinquefasciatus", "Arrecifes rocosos de Los Órganos y Paita", FishSpecies.Rarity.RARO, 1.3f, 25, listOf("Proteínas magras", "Fósforo y potasio", "Bajo en grasas saturadas"), "Ceviche tradicional de Mero con limón y ají limo piurano", "Feria Gastronómica · El Rey del Muelle", "Caserito, el verdadero mero norteño de carne firme y blanca.", R.drawable.img_real_mero_3d),
    FishSpecies("bonito", "Bonito del Pacífico", "Sarda chiliensis chiliensis", "Aguas abiertas de Sechura y Bahía de Paita", FishSpecies.Rarity.COMUN, 1.6f, 10, listOf("Rico en hierro", "Proteínas de alto valor biológico", "Omega-3"), "Escabeche de Bonito o tiradito norteño con camote", "Infografías Nutricionales · Ciencias y Salud", "Energía pura del mar de Grau.", R.drawable.img_real_bonito_3d),
    FishSpecies("tortuga_nuro", "Tortuga Verde Marina", "Chelonia mydas", "Santuario Marino El Ñuro - Los Órganos", FishSpecies.Rarity.PROTEGIDO, 0.8f, 30, listOf("Especie protegida", "Indicador de ecosistemas saludables", "Biodiversidad"), "NO PARA CONSUMO - Observación y nado ecológico", "Exposición Cuidado de la Biodiversidad", "Cuidemos el tesoro vivo de El Ñuro.", R.drawable.img_real_tortuga_3d, isSanctuaryProtected = true),
    FishSpecies("super_pez", "Súper Pez", "Piscis Heroicus Josefina", "Feria Sabores del Mar - Comunidad San Josefina", FishSpecies.Rarity.LEGENDARIO, 1.5f, 50, listOf("Héroe de la nutrición escolar", "Omega-3", "Campeón contra la anemia"), "Dieta balanceada con pescado varias veces por semana", "Teatro Súper Pez", "Protegiendo la nutrición de los niños de Piura.", R.drawable.img_real_super_pez_3d)
  )
}
