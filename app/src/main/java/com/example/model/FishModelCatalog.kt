package com.example.model

/**
 * Fish AR 3D asset manifest.
 *
 * The five primary encounter species are fish documented as commonly consumed
 * in Piura: caballa, cachema, jurel, cabrilla and camotillo.
 * Each entry points to an actual mesh asset under app/src/main/assets/models.
 */
data class FishModel3D(
  val speciesId: String,
  val displayName: String,
  val scientificName: String,
  val objAsset: String,
  val materialAsset: String,
  val scale: Float,
  val dorsalTone: Long,
  val bellyTone: Long,
  val stripeTone: Long
)

object FishModelCatalog {
  val primaryPiuraSpecies = listOf(
    FishModel3D("caballa", "Caballa", "Scomber japonicus", "models/caballa.obj", "models/caballa.mtl", 1.0f, 0xFF4E6570, 0xFFD7E1E4, 0xFF5E7780),
    FishModel3D("cachema", "Cachema", "Cynoscion analis", "models/cachema.obj", "models/cachema.mtl", 1.04f, 0xFF66727A, 0xFFE0E3DF, 0xFFB9A26A),
    FishModel3D("jurel", "Jurel", "Trachurus murphyi", "models/jurel.obj", "models/jurel.mtl", 1.06f, 0xFF435D6B, 0xFFE2E9EA, 0xFF9AAAB0),
    FishModel3D("cabrilla", "Cabrilla", "Paralabrax humeralis", "models/cabrilla.obj", "models/cabrilla.mtl", 0.98f, 0xFF7C6A50, 0xFFE2D6C1, 0xFF9E553E),
    FishModel3D("camotillo", "Camotillo", "Diplectrum eumelum", "models/camotillo.obj", "models/camotillo.mtl", 0.86f, 0xFF9A856A, 0xFFE4D8C6, 0xFFB75D42)
  )

  fun forSpecies(speciesId: String): FishModel3D? =
    primaryPiuraSpecies.firstOrNull { it.speciesId == speciesId }
}
