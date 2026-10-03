package com.example.tiempo.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tiempo.MainActivity
import com.example.tiempo.R
import com.example.tiempo.data.AirQuality
import com.example.tiempo.data.AirQualityRepository
import com.example.tiempo.data.LanguagePreferences
import com.example.tiempo.data.LocationRepository
import com.example.tiempo.data.WeatherRepository
import com.example.tiempo.data.model.CurrentWeather
import com.example.tiempo.data.model.DayWeather
import com.example.tiempo.data.model.moonPhaseInfo
import com.example.tiempo.ui.staticIcon
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class WeatherNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    /**
     * Los textos del aviso salen de aqui, no de applicationContext: en API <33 el idioma
     * elegido en la app solo se aplica envolviendo el Context de la Activity (ver
     * [LanguagePreferences]), asi que un worker que use applicationContext a secas
     * escribiria el morning report en el idioma del sistema.
     */
    private val localizedContext: Context by lazy {
        LanguagePreferences.localizedContextForTag(
            applicationContext,
            LanguagePreferences.getLanguageTag(applicationContext)
        )
    }

    override suspend fun doWork(): Result {
        return try {
            val location = LocationRepository.get(applicationContext)
            val forecast = WeatherRepository().getForecast(location.lat, location.lon)
            val today = forecast.days.firstOrNull() ?: return Result.retry()
            val airQuality = runCatching {
                AirQualityRepository().get(location.lat, location.lon)
            }.getOrNull()

            showMorningReport(location.name, forecast.current, today, airQuality)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showMorningReport(
        locationName: String,
        current: CurrentWeather,
        day: DayWeather,
        airQuality: AirQuality?
    ) {
        NotificationHelper.ensureChannel(localizedContext)

        // En Android 13+ sin permiso concedido no se puede notificar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val title = localizedContext.getString(R.string.notif_daily_title, locationName)
        val rain = rainSummary(day)
        val (moonEmoji, moonLabelRes) = moonPhaseInfo(day.moonPhase)
        val moonLabel = localizedContext.getString(moonLabelRes)

        val shortText = localizedContext.getString(
            R.string.notif_daily_short_text,
            current.temp.roundToInt(),
            day.tempMean.roundToInt(),
            rain
        )

        val bigText = buildString {
            append(
                localizedContext.getString(
                    R.string.notif_daily_body_now_avg,
                    current.temp.roundToInt(),
                    day.tempMean.roundToInt()
                )
            )
            append("${day.condition.emoji} $rain\n")
            if (airQuality != null) {
                append(
                    localizedContext.getString(
                        R.string.notif_air_quality,
                        localizedContext.getString(airQuality.labelRes),
                        airQuality.europeanAqi
                    )
                )
            }
            append("🌅 ${day.sunrise.formatHour()}   🌇 ${day.sunset.formatHour()}\n")
            append("$moonEmoji $moonLabel")
        }

        val openAppIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val dismissIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            Intent(applicationContext, DismissNotificationReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Icono grande con el set nuevo: el mismo que la app usa para esa condicion. El
        // pequeño no puede ser uno de estos (el sistema lo pinta como silueta de un color,
        // asi que un icono a color saldria como un borron), y los emojis del cuerpo son
        // texto: para cambiarlos por iconos haria falta un RemoteViews propio.
        val conditionIcon = ContextCompat
            .getDrawable(applicationContext, day.condition.staticIcon(isDay = true))
            ?.toBitmap()

        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(conditionIcon)
            .setContentTitle(title)
            .setContentText(shortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                localizedContext.getString(R.string.notif_action_view_all),
                openAppIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                localizedContext.getString(R.string.notif_action_dismiss),
                dismissIntent
            )
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(NotificationHelper.NOTIFICATION_ID, notification)
    }

    private fun rainSummary(day: DayWeather): String {
        val prob = day.precipProbability
        return when {
            prob != null && prob >= 50 ->
                localizedContext.getString(R.string.notif_rain_likely, prob)
            prob != null && prob > 0 ->
                localizedContext.getString(R.string.notif_rain_possible_percent, prob)
            day.precipitationMm > 0 ->
                localizedContext.getString(R.string.notif_rain_possible_mm, day.precipitationMm.toString())
            else -> localizedContext.getString(R.string.notif_rain_unlikely)
        }
    }

    private fun LocalDateTime.formatHour(): String = format(hourFormatter)

    private companion object {
        val hourFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
