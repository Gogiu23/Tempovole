package com.example.tiempo.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToInt

/** Respuesta cruda de la API Open-Meteo. */
@Serializable
data class ForecastResponse(
    val timezone: String,
    val elevation: Double,
    val current: CurrentForecast,
    val daily: DailyForecast,
    val hourly: HourlyForecast
)

@Serializable
data class CurrentForecast(
    @SerialName("temperature_2m") val temperature: Double,
    @SerialName("weather_code") val weatherCode: Int,
    @SerialName("uv_index") val uvIndex: Double? = null,
    @SerialName("shortwave_radiation") val solarRadiation: Double? = null,
    @SerialName("dew_point_2m") val dewPoint: Double? = null,
    @SerialName("surface_pressure") val pressure: Double? = null
)

@Serializable
data class DailyForecast(
    val time: List<String>,
    @SerialName("temperature_2m_max") val tempMax: List<Double>,
    @SerialName("temperature_2m_min") val tempMin: List<Double>,
    @SerialName("temperature_2m_mean") val tempMean: List<Double>,
    @SerialName("precipitation_sum") val precipitation: List<Double>,
    @SerialName("precipitation_probability_max") val precipProbability: List<Int?>,
    @SerialName("wind_speed_10m_max") val windSpeed: List<Double>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    val sunrise: List<String>,
    val sunset: List<String>,
    @SerialName("moon_phase") val moonPhase: List<Double>
)

@Serializable
data class HourlyForecast(
    val time: List<String>,
    @SerialName("temperature_2m") val temperature: List<Double>,
    @SerialName("precipitation_probability") val precipProbability: List<Int?>,
    @SerialName("wind_speed_10m") val windSpeed: List<Double>,
    @SerialName("weather_code") val weatherCode: List<Int>,
    @SerialName("is_day") val isDay: List<Int>
)

/** Modelo limpio que usa la UI. */
data class DayWeather(
    val date: LocalDate,
    val tempMax: Double,
    val tempMin: Double,
    val tempMean: Double,
    val precipitationMm: Double,
    val precipProbability: Int?,
    val windKmh: Double,
    val condition: WeatherCondition,
    val sunrise: LocalDateTime,
    val sunset: LocalDateTime,
    /** Fracción 0..1 del ciclo lunar (0 y 1 = luna nueva, 0.5 = luna llena). */
    val moonPhase: Double
)

data class HourWeather(
    val time: LocalDateTime,
    val temp: Double,
    val precipProbability: Int?,
    val windKmh: Double,
    val condition: WeatherCondition,
    val isDay: Boolean
) {
    /** Emoji de condición, con variante de noche (luna) para cielo despejado o parcial. */
    val displayEmoji: String
        get() = when {
            isDay -> condition.emoji
            condition == WeatherCondition.CLEAR -> "🌙"
            condition == WeatherCondition.PARTLY -> "☁️"
            else -> condition.emoji
        }
}

data class CurrentWeather(
    val temp: Double,
    val condition: WeatherCondition,
    val uvIndex: Double? = null,
    val solarRadiation: Double? = null,
    val dewPoint: Double? = null,
    val pressureHpa: Double? = null
)

/** Previsión completa: tiempo actual, diaria (7 días) y por horas. */
data class WeatherForecast(
    val current: CurrentWeather,
    val days: List<DayWeather>,
    val hours: List<HourWeather>,
    val elevationM: Double
)

enum class WeatherCondition(
    val label: String,
    val emoji: String,
    /** Qué se le pide a Unsplash para ilustrar este tiempo en las tarjetas de la semana. */
    val photoQuery: String
) {
    CLEAR("Despejado", "\u2600\uFE0F", "sunny sky,sunshine landscape"),
    PARTLY("Parcial", "\u26C5", "partly cloudy sky,clouds sunshine"),
    CLOUDY("Nublado", "\u2601\uFE0F", "cloudy sky,overcast landscape"),
    FOG("Niebla", "\uD83C\uDF2B\uFE0F", "fog,misty landscape"),
    DRIZZLE("Llovizna", "\uD83C\uDF26\uFE0F", "drizzle,light rain"),
    RAIN("Lluvia", "\uD83C\uDF27\uFE0F", "rain,rainy day"),
    SNOW("Nieve", "\u2744\uFE0F", "snow,winter landscape"),
    STORM("Tormenta", "\u26C8\uFE0F", "thunderstorm,lightning"),
    UNKNOWN("\u2014", "\u2753", "landscape,nature");

    companion object {
        fun fromCode(code: Int): WeatherCondition = when (code) {
            0 -> CLEAR
            1, 2 -> PARTLY
            3 -> CLOUDY
            45, 48 -> FOG
            51, 53, 55, 56, 57 -> DRIZZLE
            61, 63, 65, 66, 67, 80, 81, 82 -> RAIN
            71, 73, 75, 77, 85, 86 -> SNOW
            95, 96, 99 -> STORM
            else -> UNKNOWN
        }
    }
}

/** Nombre y emoji de la fase lunar a partir de la fracción 0..1 (0/1 = nueva, 0.5 = llena). */
fun moonPhaseInfo(fraction: Double): Pair<String, String> {
    val phases = listOf(
        "🌑" to "Luna nueva",
        "🌒" to "Luna creciente",
        "🌓" to "Cuarto creciente",
        "🌔" to "Gibosa creciente",
        "🌕" to "Luna llena",
        "🌖" to "Gibosa menguante",
        "🌗" to "Cuarto menguante",
        "🌘" to "Luna menguante"
    )
    val index = (fraction * 8).roundToInt() % 8
    return phases[index]
}

fun ForecastResponse.toDayWeatherList(): List<DayWeather> =
    daily.time.indices.map { i ->
        DayWeather(
            date = LocalDate.parse(daily.time[i]),
            tempMax = daily.tempMax[i],
            tempMin = daily.tempMin[i],
            tempMean = daily.tempMean[i],
            precipitationMm = daily.precipitation[i],
            precipProbability = daily.precipProbability.getOrNull(i),
            windKmh = daily.windSpeed[i],
            condition = WeatherCondition.fromCode(daily.weatherCode[i]),
            sunrise = LocalDateTime.parse(daily.sunrise[i]),
            sunset = LocalDateTime.parse(daily.sunset[i]),
            moonPhase = daily.moonPhase[i]
        )
    }

fun ForecastResponse.toHourWeatherList(): List<HourWeather> =
    hourly.time.indices.map { i ->
        HourWeather(
            time = LocalDateTime.parse(hourly.time[i]),
            temp = hourly.temperature[i],
            precipProbability = hourly.precipProbability.getOrNull(i),
            windKmh = hourly.windSpeed[i],
            condition = WeatherCondition.fromCode(hourly.weatherCode[i]),
            isDay = hourly.isDay.getOrNull(i) == 1
        )
    }

fun ForecastResponse.toCurrentWeather(): CurrentWeather =
    CurrentWeather(
        temp = current.temperature,
        condition = WeatherCondition.fromCode(current.weatherCode),
        uvIndex = current.uvIndex,
        solarRadiation = current.solarRadiation,
        dewPoint = current.dewPoint,
        pressureHpa = current.pressure
    )

fun ForecastResponse.toWeatherForecast(): WeatherForecast =
    WeatherForecast(
        current = toCurrentWeather(),
        days = toDayWeatherList(),
        hours = toHourWeatherList(),
        elevationM = elevation
    )
