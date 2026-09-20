package com.example.tiempo.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface FloodApi {
    @GET("v1/flood")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("daily") daily: String = "river_discharge",
        @Query("forecast_days") days: Int = 1
    ): FloodResponse
}

@Serializable
data class FloodResponse(val daily: FloodDaily)

@Serializable
data class FloodDaily(
    @SerialName("river_discharge") val riverDischarge: List<Double?>
)

/** Caudal del río más cercano al modelo global (m³/s). Solo hay datos cerca de ríos monitorizados. */
class FloodRepository(private val api: FloodApi = Network.floodApi) {
    suspend fun getRiverDischarge(lat: Double, lon: Double): Double? =
        api.getForecast(lat, lon).daily.riverDischarge.firstOrNull()
}
