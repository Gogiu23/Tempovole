package com.example.tiempo.data

import android.content.Context

/** Cada uno de los datos extra de Open-Meteo que el usuario puede activar/desactivar en Ajustes. */
enum class ExtraFeature(val emoji: String, val label: String, val description: String) {
    SUN_MOON(
        "🌅", "Sol y luna",
        "Muestra en \"Hoy\": hora del amanecer y el atardecer, horas de luz del día y la " +
            "fase lunar actual."
    ),
    ATMOSPHERIC(
        "🌤️", "Datos atmosféricos (UV, radiación, punto de rocío, presión)",
        "Se muestran en \"Hoy\": índice UV, radiación solar, punto de rocío y presión " +
            "atmosférica."
    ),
    POLLEN(
        "🌾", "Polen (solo Europa)",
        "Muestra el tipo de polen predominante y su nivel. Solo hay datos en Europa."
    ),
    ELEVATION(
        "⛰️", "Altitud",
        "Añade la altitud sobre el nivel del mar de la ubicación elegida."
    ),
    HISTORICAL(
        "📊", "Comparación con la media histórica",
        "Compara la temperatura de hoy con la media de los últimos 15 años en esa fecha."
    ),
    MARINE(
        "🌊", "Oleaje / marina (solo costa)",
        "Altura del oleaje del mar más cercano. Solo hay datos si estás en la costa."
    ),
    FLOOD(
        "🏞️", "Ríos / crecidas",
        "Caudal estimado del río más cercano, si hay uno con cobertura del modelo."
    ),
    CLIMATE(
        "🌍", "Cambio climático a largo plazo",
        "Diferencia de temperatura media proyectada para 2050 frente a un año reciente."
    ),
    ENSEMBLE(
        "🎯", "Incertidumbre del modelo",
        "Rango de temperatura según las distintas simulaciones del modelo meteorológico."
    )
}

object FeaturePreferences {
    fun isEnabled(context: Context, feature: ExtraFeature): Boolean =
        prefs(context).getBoolean(feature.name, true)

    fun setEnabled(context: Context, feature: ExtraFeature, enabled: Boolean) {
        prefs(context).edit().putBoolean(feature.name, enabled).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences("feature_prefs", Context.MODE_PRIVATE)
}
