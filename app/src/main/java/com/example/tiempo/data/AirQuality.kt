package com.example.tiempo.data

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
    val label: String
        get() = when {
            europeanAqi <= 20 -> "Buena"
            europeanAqi <= 40 -> "Aceptable"
            europeanAqi <= 60 -> "Moderada"
            europeanAqi <= 80 -> "Mala"
            europeanAqi <= 100 -> "Muy mala"
            else -> "Extrema"
        }

    /** Tipo de polen dominante y su nivel (Bajo/Moderado/Alto), o null si no hay datos (fuera de Europa). */
    val dominantPollen: Pair<String, String>?
        get() {
            val pollens = listOfNotNull(
                alderPollen?.let { "Aliso" to it },
                birchPollen?.let { "Abedul" to it },
                grassPollen?.let { "Gramíneas" to it },
                mugwortPollen?.let { "Artemisa" to it },
                olivePollen?.let { "Olivo" to it },
                ragweedPollen?.let { "Ambrosía" to it }
            )
            val top = pollens.maxByOrNull { it.second } ?: return null
            val level = when {
                top.second < 10 -> "Bajo"
                top.second < 50 -> "Moderado"
                else -> "Alto"
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
