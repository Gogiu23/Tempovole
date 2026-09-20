package com.example.tiempo.data

import retrofit2.http.GET
import retrofit2.http.Query
import java.time.LocalDate

interface ClimateApi {
    @GET("v1/climate")
    suspend fun getYearly(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("models") models: String = "MRI_AGCM3_2_S",
        @Query("daily") daily: String = "temperature_2m_mean"
    ): ArchiveResponse // misma forma (time + temperature_2m_mean) que el archivo histórico.
}

data class ClimateProjection(
    val projectedYear: Int,
    val projectedAverage: Double,
    val baselineAverage: Double,
    val deltaC: Double
)

class ClimateChangeRepository(
    private val climateApi: ClimateApi = Network.climateApi,
    private val archiveApi: ArchiveApi = Network.archiveApi
) {
    /** Compara la temperatura media anual proyectada para [projectedYear] con un año reciente real. */
    suspend fun getProjection(lat: Double, lon: Double, projectedYear: Int = 2050): ClimateProjection? {
        val baselineYear = LocalDate.now().year - 2

        val projected = climateApi.getYearly(
            lat, lon,
            startDate = "$projectedYear-01-01",
            endDate = "$projectedYear-12-31"
        ).daily.tempMean.filterNotNull()

        val baseline = archiveApi.getDaily(
            lat, lon,
            startDate = "$baselineYear-01-01",
            endDate = "$baselineYear-12-31"
        ).daily.tempMean.filterNotNull()

        if (projected.isEmpty() || baseline.isEmpty()) return null

        val projectedAvg = projected.average()
        val baselineAvg = baseline.average()
        return ClimateProjection(
            projectedYear = projectedYear,
            projectedAverage = projectedAvg,
            baselineAverage = baselineAvg,
            deltaC = projectedAvg - baselineAvg
        )
    }
}
