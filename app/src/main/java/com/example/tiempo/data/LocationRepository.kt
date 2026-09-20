package com.example.tiempo.data

import android.content.Context

/** Ubicación elegida por el usuario (o la de por defecto si no ha elegido ninguna). */
data class SavedLocation(val name: String, val lat: Double, val lon: Double)

object LocationRepository {
    val DEFAULT = SavedLocation(name = "Barcelona", lat = 41.3874, lon = 2.1686)

    fun get(context: Context): SavedLocation {
        val prefs = prefs(context)
        val name = prefs.getString(KEY_NAME, null) ?: return DEFAULT
        val lat = prefs.getString(KEY_LAT, null)?.toDoubleOrNull() ?: return DEFAULT
        val lon = prefs.getString(KEY_LON, null)?.toDoubleOrNull() ?: return DEFAULT
        return SavedLocation(name, lat, lon)
    }

    fun save(context: Context, location: SavedLocation) {
        prefs(context).edit()
            .putString(KEY_NAME, location.name)
            .putString(KEY_LAT, location.lat.toString())
            .putString(KEY_LON, location.lon.toString())
            .apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)

    private const val KEY_NAME = "name"
    private const val KEY_LAT = "lat"
    private const val KEY_LON = "lon"
}
