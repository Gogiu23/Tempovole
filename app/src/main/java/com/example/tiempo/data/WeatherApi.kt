package com.example.tiempo.data

import com.example.tiempo.data.model.DayWeather
import com.example.tiempo.data.model.ForecastResponse
import com.example.tiempo.data.model.UnsplashPhoto
import com.example.tiempo.data.model.WeatherForecast
import com.example.tiempo.data.model.toDayWeatherList
import com.example.tiempo.data.model.toWeatherForecast
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,weather_code,uv_index," +
            "shortwave_radiation,dew_point_2m,surface_pressure",
        @Query("daily") daily: String =
            "temperature_2m_max,temperature_2m_min,temperature_2m_mean,precipitation_sum," +
                "precipitation_probability_max,wind_speed_10m_max,weather_code," +
                "sunrise,sunset,moon_phase",
        @Query("hourly") hourly: String =
            "temperature_2m,precipitation_probability,wind_speed_10m,weather_code,is_day",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") days: Int = 7
    ): ForecastResponse
}

interface UnsplashApi {
    @GET("photos/random")
    suspend fun randomPhoto(
        @Query("orientation") orientation: String = "portrait",
        @Query("query") query: String,
        @Query("client_id") clientId: String
    ): UnsplashPhoto

    /**
     * Varias fotos aleatorias de un mismo tema en **una sola petición** (`count`, máx. 30).
     * Pedirlas de una en una multiplicaría por [count] el gasto de cuota de la API.
     */
    /**
     * Avisa a Unsplash de que una foto se ha usado. La URL viene en `links.download_location`
     * de la propia foto, por eso se pasa entera con @Url en vez de componerla aqui.
     */
    @GET
    suspend fun trackDownload(
        @Url downloadLocation: String,
        @Query("client_id") clientId: String
    ): ResponseBody

    @GET("photos/random")
    suspend fun randomPhotos(
        @Query("query") query: String,
        @Query("count") count: Int,
        @Query("orientation") orientation: String = "squarish",
        @Query("client_id") clientId: String
    ): List<UnsplashPhoto>
}

object Network {
    private val json = Json { ignoreUnknownKeys = true }

    private fun build(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    val weatherApi: WeatherApi by lazy {
        build("https://api.open-meteo.com/").create(WeatherApi::class.java)
    }

    val unsplashApi: UnsplashApi by lazy {
        build("https://api.unsplash.com/").create(UnsplashApi::class.java)
    }

    val geocodingApi: GeocodingApi by lazy {
        build("https://geocoding-api.open-meteo.com/").create(GeocodingApi::class.java)
    }

    val airQualityApi: AirQualityApi by lazy {
        build("https://air-quality-api.open-meteo.com/").create(AirQualityApi::class.java)
    }

    val archiveApi: ArchiveApi by lazy {
        build("https://archive-api.open-meteo.com/").create(ArchiveApi::class.java)
    }

    val marineApi: MarineApi by lazy {
        build("https://marine-api.open-meteo.com/").create(MarineApi::class.java)
    }

    val floodApi: FloodApi by lazy {
        build("https://flood-api.open-meteo.com/").create(FloodApi::class.java)
    }

    val climateApi: ClimateApi by lazy {
        build("https://climate-api.open-meteo.com/").create(ClimateApi::class.java)
    }

    val ensembleApi: EnsembleApi by lazy {
        build("https://ensemble-api.open-meteo.com/").create(EnsembleApi::class.java)
    }
}

class WeatherRepository(private val api: WeatherApi = Network.weatherApi) {
    suspend fun getWeeklyForecast(lat: Double, lon: Double): List<DayWeather> =
        api.getForecast(lat, lon).toDayWeatherList()

    suspend fun getForecast(lat: Double, lon: Double): WeatherForecast =
        api.getForecast(lat, lon).toWeatherForecast()
}
