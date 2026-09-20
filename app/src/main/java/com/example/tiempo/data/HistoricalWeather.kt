package com.example.tiempo.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.LocalDate

interface ArchiveApi {
    @GET("v1/archive")
    suspend fun getDaily(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("daily") daily: String = "temperature_2m_mean",
        @Query("timezone") timezone: String = "auto"
    ): ArchiveResponse
}

@Serializable
data class ArchiveResponse(val daily: ArchiveDaily)

@Serializable
data class ArchiveDaily(
    val time: List<String>,
    @SerialName("temperature_2m_mean") val tempMean: List<Double?>
)

data class HistoricalComparison(
    val historicalAverage: Double,
    val yearsUsed: Int,
    val diffFromToday: Double
)

class HistoricalWeatherRepository(private val api: ArchiveApi = Network.archiveApi) {
    /** Compara [todayTemp] con la media histórica del mismo día-mes en los últimos [yearsBack] años. */
    suspend fun compareToday(
        lat: Double,
        lon: Double,
        todayTemp: Double,
        yearsBack: Int = 15
    ): HistoricalComparison? {
        val today = LocalDate.now()
        // El archivo histórico tarda unos días en consolidarse; nos quedamos algo atrás de hoy.
        val end = today.minusDays(5)
        val start = end.minusYears(yearsBack.toLong())

        val response = api.getDaily(lat, lon, start.toString(), end.toString())
        val matches = response.daily.time.indices.mapNotNull { i ->
            val date = LocalDate.parse(response.daily.time[i])
            val temp = response.daily.tempMean.getOrNull(i)
            if (date.monthValue == today.monthValue && date.dayOfMonth == today.dayOfMonth && temp != null) {
                temp
            } else {
                null
            }
        }
        if (matches.isEmpty()) return null

        val average = matches.average()
        return HistoricalComparison(
            historicalAverage = average,
            yearsUsed = matches.size,
            diffFromToday = todayTemp - average
        )
    }
}
