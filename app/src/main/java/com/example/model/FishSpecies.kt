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

object MarineDatabase {
  val speciesList = listOf(
    FishSpecies("caballa", "Caballa del Pacífico", "Scomber japonicus", "Aguas costeras de Piura (Paita, Talara)", FishSpecies.Rarity.COMUN, 1.45f, 10, listOf("Pescado azul rico en Omega-3", "Proteínas de alto valor", "Vitamina B12 y hierro"), "Caballa frita con chifles o ceviche clásico piurano", "Ruta Gastronómica · Estación Caballa", "Una de las especies marinas más emblemáticas de la pesca artesanal en Piura.", R.drawable.img_real_caballa_3d),
    FishSpecies("cachema", "Cachema", "Cynoscion analis", "Fondos arenosos y estuarios (Sechura, Paita)", FishSpecies.Rarity.ESPECIAL, 1.15f, 14, listOf("Proteína magra de fácil digestión", "Fósforo y selenio", "Carne suave y blanca"), "Cachema frita a la piurana con yuca y sarza criolla", "Ruta Gastronómica · Estación Cachema", "Detectada por su firma acústica suave en los estuarios y bahías de Piura.", R.drawable.img_real_cachema_3d),
    FishSpecies("jurel", "Jurel del Pacífico", "Trachurus murphyi", "Aguas abiertas oceánicas de Paita y Los Órganos", FishSpecies.Rarity.ESPECIAL, 1.55f, 16, listOf("Pescado azul muy nutritivo", "Grasas saludables DHA y EPA", "Excelente fuente de Omega-3"), "Jurel al vapor, en escabeche o sudado tradicional", "Ruta Gastronómica · Estación Jurel", "Su cardumen veloz produce una señal intensa: mantén el sonar sincronizado.", R.drawable.img_real_jurel_3d),
    FishSpecies("cabrilla", "Cabrilla de Peña", "Paralabrax humeralis", "Fondos rocosos de Máncora, Paita y Cabo Blanco", FishSpecies.Rarity.ESPECIAL, 1.0f, 15, listOf("Proteína magra de alta pureza", "Fósforo, potasio y selenio", "Carne firme y sabrosa"), "Sudado tradicional de Cabrilla con chicha de jora", "Feria Gastronómica · Stand Cabrilla de Peña", "Habita las peñas piuranas: espera la ventana de embestida para capturarla.", R.drawable.img_real_cabrilla_3d),
    FishSpecies("camotillo", "Camotillo", "Diplectrum eumelum", "Litoral y arrecifes rocosos de Colán y Paita", FishSpecies.Rarity.RARO, 0.92f, 18, listOf("Proteína magra premium", "Carne delicada y apreciada", "Bajo en calorías y alto en minerales"), "Ceviche tradicional piurano con ají limo y camote", "Ruta Gastronómica · Estación Camotillo", "Pez de arrecife ágil y difícil de distinguir en el visor de sonar subacuático.", R.drawable.img_real_camotillo_3d),
    FishSpecies("mero_murike", "Mero Murique", "Epinephelus quinquefasciatus", "Arrecifes profundos de Cabo Blanco y Los Órganos", FishSpecies.Rarity.RARO, 1.3f, 25, listOf("Proteínas magras de élite", "Fósforo y potasio", "Bajo en grasas y rico en nutrientes"), "Ceviche de Mero Murique o a la plancha con mantequilla de ajo", "Feria Gastronómica · El Rey del Muelle", "El legendario gigante de los arrecifes de Cabo Blanco, de fuerza titánica.", R.drawable.img_real_mero_3d),
    FishSpecies("bonito", "Bonito del Norte", "Sarda chiliensis chiliensis", "Aguas abiertas pelágicas de la costa norte", FishSpecies.Rarity.COMUN, 1.6f, 10, listOf("Súper alimento rico en hierro", "Proteínas de alto rendimiento", "Alto contenido de Omega-3 combate anemia"), "Escabeche de Bonito, tiradito norteño o a la brasa", "Infografías Nutricionales · Stand Salud Marina", "Poderoso nadador oceánico: fuente esencial de energía y combate a la anemia.", R.drawable.img_real_bonito_3d),
    FishSpecies("tortuga_nuro", "Tortuga Verde Marina", "Chelonia mydas", "Santuario de El Ñuro y arrecifes protegidos de Piura", FishSpecies.Rarity.PROTEGIDO, 0.8f, 30, listOf("Especie marina protegida por ley", "Indicador clave de salud de los arrecifes", "Patrimonio de biodiversidad del Mar de Grau"), "NO PARA CONSUMO - Observación responsable y conservación", "Exposición Cuidado de la Biodiversidad de El Ñuro", "Tesoro ecológico viviente de las caletas de Piura. ¡Protéjela siempre!", R.drawable.img_real_tortuga_3d, isSanctuaryProtected = true),
    FishSpecies("super_pez", "Súper Pez", "Piscis Heroicus", "Comunidad Virtual 'Sabores del Mar' Piura", FishSpecies.Rarity.LEGENDARIO, 1.5f, 50, listOf("Símbolo de nutrición infantil", "Campaña nacional contra la anemia", "Poder del Omega-3 en cada comida"), "Consumo de pescado al menos 3 veces por semana en el hogar", "Exhibición Principal Súper Pez de Piura", "El guardián de la buena nutrición en el norte peruano.", R.drawable.img_real_super_pez_3d)
  )
}
