package com.example.tiempo.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.LocalDateTime

interface EnsembleApi {
    @GET("v1/ensemble")
    suspend fun getHourly(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("hourly") hourly: String = "temperature_2m",
        @Query("models") models: String = "icon_seamless",
        @Query("forecast_days") days: Int = 1,
        @Query("timezone") timezone: String = "auto"
    ): EnsembleResponse
}

// El número de miembros varía por modelo (ej. temperature_2m_member01..member39),
// así que se lee como JSON crudo en vez de un data class con campos fijos.
@Serializable
data class EnsembleResponse(val hourly: JsonObject)

/** Rango de temperatura entre los distintos miembros del modelo para la hora actual. */
data class EnsembleUncertainty(val minTemp: Double, val maxTemp: Double, val memberCount: Int)

class EnsembleRepository(private val api: EnsembleApi = Network.ensembleApi) {
    suspend fun getCurrentUncertainty(lat: Double, lon: Double): EnsembleUncertainty? {
        val hourly = api.getHourly(lat, lon).hourly
        val times = hourly["time"]?.jsonArray?.map { it.jsonPrimitive.content } ?: return null

        val nowHour = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
        val index = times.indexOfFirst { runCatching { LocalDateTime.parse(it) == nowHour }.getOrDefault(false) }
            .let { if (it >= 0) it else 0 }

        val values = hourly.keys
            .filter { it.startsWith("temperature_2m_member") }
            .mapNotNull { key -> hourly[key]?.jsonArray?.getOrNull(index)?.jsonPrimitive?.doubleOrNull }

        if (values.isEmpty()) return null
        return EnsembleUncertainty(values.min(), values.max(), values.size)
    }
}
