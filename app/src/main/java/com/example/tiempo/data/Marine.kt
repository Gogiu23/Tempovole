package com.example.tiempo.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface MarineApi {
    @GET("v1/marine")
    suspend fun getCurrent(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "wave_height,wave_period",
        @Query("timezone") timezone: String = "auto"
    ): MarineResponse
}

@Serializable
data class MarineResponse(val current: CurrentMarine)

@Serializable
data class CurrentMarine(
    @SerialName("wave_height") val waveHeight: Double? = null,
    @SerialName("wave_period") val wavePeriod: Double? = null
)

/** Solo hay datos si la ubicación está en el mar o muy cerca de la costa. */
data class MarineConditions(val waveHeightM: Double, val wavePeriodS: Double)

class MarineRepository(private val api: MarineApi = Network.marineApi) {
    suspend fun get(lat: Double, lon: Double): MarineConditions? {
        val current = api.getCurrent(lat, lon).current
        val height = current.waveHeight
        val period = current.wavePeriod
        return if (height != null && period != null) MarineConditions(height, period) else null
    }
}
