package com.example.tiempo.data

import androidx.annotation.StringRes
import com.example.tiempo.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface AirQualityApi {
    @GET("v1/air-quality")
    suspend fun getCurrent(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "european_aqi,alder_pollen,birch_pollen," +
            "grass_pollen,mugwort_pollen,olive_pollen,ragweed_pollen",
        @Query("timezone") timezone: String = "auto"
    ): AirQualityResponse
}

@Serializable
data class AirQualityResponse(val current: CurrentAirQuality)

@Serializable
data class CurrentAirQuality(
    @SerialName("european_aqi") val europeanAqi: Int?,
    @SerialName("alder_pollen") val alderPollen: Double? = null,
    @SerialName("birch_pollen") val birchPollen: Double? = null,
    @SerialName("grass_pollen") val grassPollen: Double? = null,
    @SerialName("mugwort_pollen") val mugwortPollen: Double? = null,
    @SerialName("olive_pollen") val olivePollen: Double? = null,
    @SerialName("ragweed_pollen") val ragweedPollen: Double? = null
)

/** Calidad del aire (índice europeo, EAQI): 0-100+, cuanto más bajo mejor. */
data class AirQuality(
    val europeanAqi: Int,
    val alderPollen: Double? = null,
    val birchPollen: Double? = null,
    val grassPollen: Double? = null,
    val mugwortPollen: Double? = null,
    val olivePollen: Double? = null,
    val ragweedPollen: Double? = null
) {
    @get:StringRes
    val labelRes: Int
        get() = when {
            europeanAqi <= 20 -> R.string.air_quality_good
            europeanAqi <= 40 -> R.string.air_quality_acceptable
            europeanAqi <= 60 -> R.string.air_quality_moderate
            europeanAqi <= 80 -> R.string.air_quality_bad
            europeanAqi <= 100 -> R.string.air_quality_very_bad
            else -> R.string.air_quality_extreme
        }

    /** Tipo de polen dominante (StringRes) y su nivel (StringRes: Bajo/Moderado/Alto), o null si no hay datos (fuera de Europa). */
    val dominantPollen: Pair<Int, Int>?
        get() {
            val pollens = listOfNotNull(
                alderPollen?.let { R.string.pollen_alder to it },
                birchPollen?.let { R.string.pollen_birch to it },
                grassPollen?.let { R.string.pollen_grass to it },
                mugwortPollen?.let { R.string.pollen_mugwort to it },
                olivePollen?.let { R.string.pollen_olive to it },
                ragweedPollen?.let { R.string.pollen_ragweed to it }
            )
            val top = pollens.maxByOrNull { it.second } ?: return null
            val level = when {
                top.second < 10 -> R.string.pollen_level_low
                top.second < 50 -> R.string.pollen_level_moderate
                else -> R.string.pollen_level_high
            }
            return top.first to level
        }
}

class AirQualityRepository(private val api: AirQualityApi = Network.airQualityApi) {
    suspend fun get(lat: Double, lon: Double): AirQuality? {
        val c = api.getCurrent(lat, lon).current
        return c.europeanAqi?.let {
            AirQuality(
                europeanAqi = it,
                alderPollen = c.alderPollen,
                birchPollen = c.birchPollen,
                grassPollen = c.grassPollen,
                mugwortPollen = c.mugwortPollen,
                olivePollen = c.olivePollen,
                ragweedPollen = c.ragweedPollen
            )
        }
    }
}
