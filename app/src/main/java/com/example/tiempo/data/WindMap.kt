package com.example.tiempo.data

import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface WindMapApi {
    /** Con varias coordenadas Open-Meteo devuelve una lista, un elemento por punto y en el mismo orden. */
    @GET("v1/forecast")
    suspend fun getGrid(
        @Query("latitude") lats: String,
        @Query("longitude") lons: String,
        @Query("hourly") hourly: String = "wind_speed_10m,wind_direction_10m,cloud_cover,precipitation",
        @Query("wind_speed_unit") unit: String = "kmh",
        @Query("timezone") timezone: String = "auto",
        @Query("past_days") past: Int = 1,
        @Query("forecast_days") days: Int = 2
    ): List<WindPointResponse>
}

@Serializable
data class WindPointResponse(val hourly: WindPointHourly)

@Serializable
data class WindPointHourly(
    val time: List<String>,
    @SerialName("wind_speed_10m") val windSpeed: List<Double?>,
    @SerialName("wind_direction_10m") val windDirection: List<Double?>,
    @SerialName("cloud_cover") val cloudCover: List<Double?>,
    val precipitation: List<Double?>
)

/**
 * Viento y nubes sobre una rejilla alrededor de un punto. Las coordenadas del mapa son
 * normalizadas: x de 0 (oeste) a 1 (este), y de 0 (norte) a 1 (sur).
 */
class WindField(
    val rows: Int,
    val cols: Int,
    /** Horas locales ISO ("2026-11-03T17:00"), una por cada paso de tiempo. */
    val times: List<String>,
    /** [hora][fila * cols + columna], km/h hacia el este. */
    val u: List<FloatArray>,
    /** Igual, km/h hacia el norte. */
    val v: List<FloatArray>,
    /** Igual, porcentaje 0..100. */
    val cloud: List<FloatArray>,
    /** Igual, precipitación en mm por hora. */
    val rain: List<FloatArray>,
    val latMin: Double,
    val latMax: Double,
    val lonMin: Double,
    val lonMax: Double
) {
    val centerLat get() = (latMin + latMax) / 2
    val centerLon get() = (lonMin + lonMax) / 2
    val widthKm get() = ((lonMax - lonMin) * KM_PER_DEGREE * cos(Math.toRadians(centerLat))).toFloat()
    val heightKm get() = ((latMax - latMin) * KM_PER_DEGREE).toFloat()

    /** Interpolación bilineal de un campo de una hora en el punto (x, y) normalizado. */
    fun sample(grid: FloatArray, x: Float, y: Float): Float {
        val gx = min(max(x, 0f), 1f) * (cols - 1)
        val gy = min(max(y, 0f), 1f) * (rows - 1)
        val c0 = gx.toInt().coerceAtMost(cols - 2)
        val r0 = gy.toInt().coerceAtMost(rows - 2)
        val fx = gx - c0
        val fy = gy - r0
        val top = grid[r0 * cols + c0] * (1 - fx) + grid[r0 * cols + c0 + 1] * fx
        val bottom = grid[(r0 + 1) * cols + c0] * (1 - fx) + grid[(r0 + 1) * cols + c0 + 1] * fx
        return top * (1 - fy) + bottom * fy
    }

    fun xFor(lon: Double) = ((lon - lonMin) / (lonMax - lonMin)).toFloat()
    fun yFor(lat: Double) = ((latMax - lat) / (latMax - latMin)).toFloat()

    companion object {
        private const val KM_PER_DEGREE = 111.0
    }
}

object WindMapRepository {
    private const val SIDE = 9
    private const val LAT_HALF = 4.0

    /** Ancho / alto del lienzo del mapa; la caja de la rejilla lo respeta para que no se deforme. */
    const val ASPECT = 0.9f

    private var cached: Pair<Pair<Double, Double>, WindField>? = null

    suspend fun load(lat: Double, lon: Double, api: WindMapApi = Network.windMapApi): WindField {
        val key = lat to lon
        cached?.takeIf { it.first == key }?.let { return it.second }

        val latC = lat.coerceIn(-80.0, 80.0)
        val lonHalf = LAT_HALF * ASPECT / cos(Math.toRadians(latC))
        val latMax = latC + LAT_HALF
        val latMin = latC - LAT_HALF
        val lonMin = lon - lonHalf
        val lonMax = lon + lonHalf

        // Fila 0 al norte, columna 0 al oeste: el mismo orden que el lienzo.
        val lats = ArrayList<Double>()
        val lons = ArrayList<Double>()
        for (r in 0 until SIDE) for (c in 0 until SIDE) {
            lats += latMax - (latMax - latMin) * r / (SIDE - 1)
            lons += lonMin + (lonMax - lonMin) * c / (SIDE - 1)
        }
        val points = api.getGrid(
            lats = lats.joinToString(",") { "%.3f".format(java.util.Locale.US, it) },
            lons = lons.joinToString(",") { "%.3f".format(java.util.Locale.US, it) }
        )

        val times = points.first().hourly.time
        val n = times.size
        val u = List(n) { FloatArray(SIDE * SIDE) }
        val v = List(n) { FloatArray(SIDE * SIDE) }
        val cloud = List(n) { FloatArray(SIDE * SIDE) }
        val rain = List(n) { FloatArray(SIDE * SIDE) }
        points.forEachIndexed { p, point ->
            for (t in 0 until n) {
                val speed = point.hourly.windSpeed.getOrNull(t) ?: 0.0
                // La dirección es de dónde viene el viento: hacia dónde sopla, +180°.
                val dir = Math.toRadians(point.hourly.windDirection.getOrNull(t) ?: 0.0)
                u[t][p] = (-speed * sin(dir)).toFloat()
                v[t][p] = (-speed * cos(dir)).toFloat()
                cloud[t][p] = (point.hourly.cloudCover.getOrNull(t) ?: 0.0).toFloat()
                rain[t][p] = (point.hourly.precipitation.getOrNull(t) ?: 0.0).toFloat()
            }
        }
        return WindField(SIDE, SIDE, times, u, v, cloud, rain, latMin, latMax, lonMin, lonMax)
            .also { cached = key to it }
    }
}
