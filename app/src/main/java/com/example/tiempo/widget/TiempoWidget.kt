package com.example.tiempo.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.Preferences
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.tiempo.MainActivity
import com.example.tiempo.data.LocationRepository
import com.example.tiempo.data.UnsplashRepository
import com.example.tiempo.data.WeatherRepository
import com.example.tiempo.data.WidgetBackground
import com.example.tiempo.data.WidgetPreferences
import com.example.tiempo.data.model.CurrentWeather
import com.example.tiempo.data.model.HourWeather
import com.example.tiempo.data.model.WeatherCondition
import com.example.tiempo.data.model.WeatherForecast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val httpClient = OkHttpClient()
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

class TiempoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val location = LocationRepository.get(appContext)

        val forecast: WeatherForecast? = runCatching {
            WeatherRepository().getForecast(location.lat, location.lon)
        }.getOrNull()

        // Si la petición falla (red lenta, timeout…), reutilizamos el último dato
        // bueno en vez de dejar el icono/temperatura en blanco.
        val current: CurrentWeather?
        val rainNextHour: Int?
        if (forecast != null) {
            current = forecast.current
            rainNextHour = forecast.hours.nextHourRainProbability()
            WidgetWeatherCache.save(appContext, current, rainNextHour)
        } else {
            val cached = WidgetWeatherCache.load(appContext)
            current = cached?.current
            rainNextHour = cached?.rainNextHour
        }

        // La foto se carga siempre (está cacheada en disco por franja del día), para que
        // esté disponible si el usuario cambia a modo imagen sin recargar todo el widget.
        val photo = UnsplashRepository(appContext).backgroundUrl()
            ?.let { loadCachedBitmap(appContext, it) }

        provideContent {
            // El aspecto se lee del estado de Glance (ver [WidgetUpdater]): así, cuando la
            // app lo cambia, la composición se entera y el widget se repinta. Si todavía no
            // hay nada en el estado (widget recién añadido), se cae a las preferencias.
            val state = currentState<Preferences>()
            val backgroundMode = state[WidgetStateKeys.background]
                ?.let { runCatching { WidgetBackground.valueOf(it) }.getOrNull() }
                ?: WidgetPreferences.getBackground(appContext)
            val solidColor = state[WidgetStateKeys.color]
                ?: WidgetPreferences.getColor(appContext).argb

            WidgetContent(
                locationName = location.name,
                current = current,
                rainNextHour = rainNextHour,
                photo = photo.takeIf { backgroundMode == WidgetBackground.IMAGE },
                backgroundMode = backgroundMode,
                solidColor = solidColor
            )
        }
    }
}

/**
 * Color del velo que se pinta sobre la capa de imagen.
 * Con foto oscurece para que el texto blanco se lea; con color fijo es el propio color;
 * en transparente es un velo muy tenue que deja ver el fondo de pantalla del móvil.
 */
private fun overlayColorFor(
    mode: WidgetBackground,
    hasPhoto: Boolean,
    solidColor: Int
): Color = when {
    hasPhoto -> Color(0x66000000)
    mode == WidgetBackground.TRANSPARENT -> Color(0x33000000)
    mode == WidgetBackground.COLOR -> Color(solidColor)
    // Modo imagen pero sin foto disponible todavía: color de respaldo.
    else -> Color(0xFF1E3A5F)
}

/** Bitmap 1×1 transparente: hace de relleno para que el árbol del widget no cambie de forma. */
private val emptyBitmap: Bitmap by lazy {
    Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
}

@Composable
private fun WidgetContent(
    locationName: String,
    current: CurrentWeather?,
    rainNextHour: Int?,
    photo: Bitmap?,
    backgroundMode: WidgetBackground,
    solidColor: Int
) {
    val white = ColorProvider(Color.White)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(20.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        // El árbol del widget es siempre el mismo (imagen + velo + contenido) y solo
        // cambian el bitmap y el color. Si la forma del árbol cambiara entre modos, el
        // launcher puede quedarse con el layout anterior en caché y el widget no se
        // actualizaría al cambiar de opción.
        Image(
            provider = ImageProvider(photo ?: emptyBitmap),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = GlanceModifier.fillMaxSize()
        )
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(overlayColorFor(backgroundMode, photo != null, solidColor))
        ) {}

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = locationName,
                style = TextStyle(color = white, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            )
            Text(
                text = LocalDateTime.now().format(timeFormatter),
                style = TextStyle(color = white, fontSize = 12.sp)
            )
            Text(
                text = if (current != null) "${current.condition.emoji} ${current.temp.roundToInt()}°"
                else "—",
                style = TextStyle(color = white, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            )
            if (rainNextHour != null) {
                Text(
                    text = "☔ $rainNextHour% próx. hora",
                    style = TextStyle(color = white, fontSize = 12.sp)
                )
            }
        }
    }
}

/** Probabilidad de lluvia de la próxima hora (la primera franja horaria tras la hora actual). */
private fun List<HourWeather>.nextHourRainProbability(): Int? {
    val nextHourStart = LocalDateTime.now().plusHours(1)
        .withMinute(0).withSecond(0).withNano(0)
    return firstOrNull { !it.time.isBefore(nextHourStart) }?.precipProbability
}

private data class CachedWidgetWeather(val current: CurrentWeather, val rainNextHour: Int?)

/**
 * Última lectura de tiempo que se pudo obtener con éxito. Si una actualización del
 * widget falla (red lenta, timeout…), se usa esto para no dejar el icono en blanco.
 */
private object WidgetWeatherCache {
    private const val PREFS = "widget_weather_cache"
    private const val KEY_TEMP = "temp"
    private const val KEY_CONDITION = "condition"
    private const val KEY_RAIN = "rain"

    fun save(context: Context, current: CurrentWeather, rainNextHour: Int?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_TEMP, current.temp.toFloat())
            .putString(KEY_CONDITION, current.condition.name)
            .putInt(KEY_RAIN, rainNextHour ?: -1)
            .apply()
    }

    fun load(context: Context): CachedWidgetWeather? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val conditionName = prefs.getString(KEY_CONDITION, null) ?: return null
        val condition = runCatching { WeatherCondition.valueOf(conditionName) }.getOrNull() ?: return null
        val temp = prefs.getFloat(KEY_TEMP, Float.NaN)
        if (temp.isNaN()) return null
        val rain = prefs.getInt(KEY_RAIN, -1).takeIf { it >= 0 }
        return CachedWidgetWeather(CurrentWeather(temp.toDouble(), condition), rain)
    }
}

/**
 * Descarga la imagen de fondo y la guarda en disco. Si la URL no ha cambiado desde
 * la última vez (misma foto del día), reutiliza el fichero en caché sin red.
 */
private suspend fun loadCachedBitmap(context: Context, url: String): Bitmap? =
    withContext(Dispatchers.IO) {
        runCatching {
            val prefs = context.getSharedPreferences("widget_bg_cache", Context.MODE_PRIVATE)
            val file = File(context.cacheDir, "widget_bg.jpg")

            if (prefs.getString("url", null) != url || !file.exists()) {
                val bytes = httpClient.newCall(Request.Builder().url(url).build())
                    .execute()
                    .use { it.body?.bytes() }
                    ?: return@runCatching null
                file.writeBytes(bytes)
                prefs.edit().putString("url", url).apply()
            }

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.path, bounds)
            val sample = sampleSizeFor(bounds.outWidth, bounds.outHeight, maxDimension = 480)
            BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })
        }.getOrNull()
    }

private fun sampleSizeFor(width: Int, height: Int, maxDimension: Int): Int {
    var sample = 1
    while (width / sample > maxDimension || height / sample > maxDimension) {
        sample *= 2
    }
    return sample
}
