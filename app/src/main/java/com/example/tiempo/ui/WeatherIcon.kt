package com.example.tiempo.ui

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.request.repeatCount
import com.example.tiempo.R
import com.example.tiempo.data.ExtraFeature
import com.example.tiempo.data.model.WeatherCondition

/**
 * Iconos animados de Flaticon (WebP animado en res/raw). La licencia gratuita exige atribución:
 * hay que acreditar "Icons by Flaticon" en algún sitio visible de la app.
 */
private fun WeatherCondition.animRes(isDay: Boolean): Int? = when (this) {
    WeatherCondition.CLEAR -> if (isDay) R.raw.anim_clear else R.raw.anim_moon
    WeatherCondition.PARTLY -> if (isDay) R.raw.anim_partly else R.raw.anim_moon_cloudy
    WeatherCondition.CLOUDY -> R.raw.anim_cloudy
    WeatherCondition.FOG -> R.raw.anim_fog
    // No hay icono propio de llovizna: se usa el de lluvia.
    WeatherCondition.DRIZZLE -> R.raw.anim_rain
    WeatherCondition.RAIN -> R.raw.anim_rain
    WeatherCondition.SNOW -> R.raw.anim_snow
    WeatherCondition.STORM -> R.raw.anim_storm
    WeatherCondition.UNKNOWN -> R.raw.anim_weather
}

@Composable
fun WeatherIcon(
    condition: WeatherCondition,
    isDay: Boolean,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val res = condition.animRes(isDay)
    if (res == null) {
        Text(text = condition.emoji, fontSize = (size.value * 0.8f).sp, modifier = modifier)
        return
    }
    AnimatedRawIcon(res, size, condition.label, modifier)
}

/** Un WebP animado de res/raw, una sola pasada, congelado en el ultimo fotograma. */
@Composable
fun AnimatedRawIcon(
    res: Int,
    size: Dp,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // ImageLoader propio: el de Coil por defecto no sabe de GIF ni de WebP animado.
    // ponytail: en API 26-27 no hay ImageDecoder, asi que el WebP animado se ve como
    // fotograma fijo. Si alguna vez importa, convertir esos assets tambien a GIF.
    val loader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
                else add(GifDecoder.Factory())
            }
            .build()
    }
    AsyncImage(
        // repeatCount(0) = una sola pasada y se queda en el ultimo fotograma.
        model = ImageRequest.Builder(context).data(res).repeatCount(0).build(),
        imageLoader = loader,
        contentDescription = contentDescription,
        modifier = modifier.size(size)
    )
}

/** Icono fijo de una condición, el que se usa fuera de la primera página. */
private fun WeatherCondition.staticRes(isDay: Boolean): Int = when (this) {
    WeatherCondition.CLEAR -> if (isDay) R.drawable.ic_sun else R.drawable.ic_crescent_moon
    WeatherCondition.PARTLY -> if (isDay) R.drawable.ic_cloudy else R.drawable.ic_moon_cloudy
    WeatherCondition.CLOUDY -> R.drawable.ic_clouds
    WeatherCondition.FOG -> R.drawable.ic_fog
    WeatherCondition.DRIZZLE -> R.drawable.ic_heavy_rain
    WeatherCondition.RAIN -> R.drawable.ic_heavy_rain
    WeatherCondition.SNOW -> R.drawable.ic_snow
    WeatherCondition.STORM -> R.drawable.ic_storm
    WeatherCondition.UNKNOWN -> R.drawable.ic_weather
}

@Composable
fun WeatherIconStatic(
    condition: WeatherCondition,
    isDay: Boolean,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(condition.staticRes(isDay)),
        contentDescription = condition.label,
        modifier = modifier.size(size)
    )
}

/**
 * Icono del set nuevo para cada dato de detalle. null = no hay icono todavía y se sigue
 * usando el emoji: la rejilla mezcla ambos sin romperse.
 */
fun DetailType.iconRes(): Int? = when (this) {
    DetailType.MAX_TEMP -> R.drawable.ic_hot
    DetailType.MIN_TEMP -> R.drawable.ic_cold
    DetailType.AVG_TEMP -> R.drawable.ic_temp_medium
    DetailType.WIND -> R.drawable.ic_wind
    DetailType.PRECIPITATION -> R.drawable.ic_heavy_rain
    DetailType.RAIN_PROBABILITY -> R.drawable.ic_rain_prob
    DetailType.UV_INDEX -> R.drawable.ic_sun
    DetailType.SOLAR_RADIATION -> R.drawable.ic_solar_radiation
    DetailType.DEW_POINT -> R.drawable.ic_humidity
    DetailType.ELEVATION -> R.drawable.ic_altitude
    DetailType.POLLEN -> R.drawable.ic_pollen
    DetailType.HISTORICAL -> R.drawable.ic_statistics
    DetailType.MARINE -> R.drawable.ic_wave
    DetailType.FLOOD -> R.drawable.ic_river
    DetailType.CLIMATE -> R.drawable.ic_climate_change
    DetailType.ENSEMBLE -> R.drawable.ic_uncertainty
    DetailType.SUNRISE -> R.drawable.ic_sunrise
    DetailType.SUNSET -> R.drawable.ic_sunset
    DetailType.DAY_LENGTH -> R.drawable.ic_day_duration
    DetailType.MOON_PHASE -> R.drawable.ic_crescent_moon
    // Falta icono de presión atmosférica: sigue con el emoji.
    DetailType.PRESSURE -> R.drawable.ic_pressure
}

/** Recurso animado (raw) para el icono hero de un DetailType, o null si no hay versión animada. */
fun DetailType.animResOrNull(): Int? = when (this) {
    DetailType.MAX_TEMP -> R.raw.anim_temp_max
    DetailType.MIN_TEMP -> R.raw.anim_temp_min
    DetailType.AVG_TEMP -> R.raw.anim_avg_temp
    DetailType.WIND -> R.raw.anim_wind
    DetailType.PRECIPITATION -> R.raw.anim_precipitation
    DetailType.RAIN_PROBABILITY -> R.raw.anim_rain_probability
    DetailType.UV_INDEX -> R.raw.anim_uv
    DetailType.SOLAR_RADIATION -> R.raw.anim_solar_radiation
    DetailType.DEW_POINT -> R.raw.anim_dew_point
    DetailType.PRESSURE -> R.raw.anim_pressure
    DetailType.ELEVATION -> R.raw.anim_elevation
    DetailType.POLLEN -> R.raw.anim_pollen
    DetailType.HISTORICAL -> R.raw.anim_historical
    DetailType.MARINE -> R.raw.anim_marine
    DetailType.FLOOD -> R.raw.anim_flood
    DetailType.CLIMATE -> R.raw.anim_climate
    DetailType.ENSEMBLE -> R.raw.anim_ensemble
    DetailType.SUNRISE -> R.raw.anim_sunrise
    DetailType.SUNSET -> R.raw.anim_sunset
    DetailType.MOON_PHASE -> R.raw.anim_moon_phase
    DetailType.DAY_LENGTH -> R.raw.anim_day_length
}

/** El icono fijo de la condición. Para pintarlo dentro de un Canvas. */
fun WeatherCondition.staticIcon(isDay: Boolean = true): Int = staticRes(isDay)

/** Icono de cada dato opcional en la lista de Ajustes. */
fun ExtraFeature.iconRes(): Int = when (this) {
    ExtraFeature.SUN_MOON -> R.drawable.ic_day_duration
    ExtraFeature.ATMOSPHERIC -> R.drawable.ic_solar_radiation
    ExtraFeature.POLLEN -> R.drawable.ic_pollen
    ExtraFeature.ELEVATION -> R.drawable.ic_altitude
    ExtraFeature.HISTORICAL -> R.drawable.ic_statistics
    ExtraFeature.MARINE -> R.drawable.ic_wave
    ExtraFeature.FLOOD -> R.drawable.ic_river
    ExtraFeature.CLIMATE -> R.drawable.ic_climate_change
    ExtraFeature.ENSEMBLE -> R.drawable.ic_uncertainty
}
