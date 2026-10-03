package com.example.tiempo.data

import android.content.Context
import androidx.annotation.StringRes
import com.example.tiempo.R

/** Cada uno de los datos extra de Open-Meteo que el usuario puede activar/desactivar en Ajustes. */
enum class ExtraFeature(
    val emoji: String,
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int
) {
    SUN_MOON("🌅", R.string.feature_sun_moon_label, R.string.feature_sun_moon_description),
    ATMOSPHERIC("🌤️", R.string.feature_atmospheric_label, R.string.feature_atmospheric_description),
    POLLEN("🌾", R.string.feature_pollen_label, R.string.feature_pollen_description),
    ELEVATION("⛰️", R.string.feature_elevation_label, R.string.feature_elevation_description),
    HISTORICAL("📊", R.string.feature_historical_label, R.string.feature_historical_description),
    MARINE("🌊", R.string.feature_marine_label, R.string.feature_marine_description),
    FLOOD("🏞️", R.string.feature_flood_label, R.string.feature_flood_description),
    CLIMATE("🌍", R.string.feature_climate_label, R.string.feature_climate_description),
    ENSEMBLE("🎯", R.string.feature_ensemble_label, R.string.feature_ensemble_description)
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
