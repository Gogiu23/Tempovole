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
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tiempo.MainActivity
import com.example.tiempo.R
import com.example.tiempo.data.AirQuality
import com.example.tiempo.data.AirQualityRepository
import com.example.tiempo.data.LocationRepository
import com.example.tiempo.data.WeatherRepository
import com.example.tiempo.data.model.CurrentWeather
import com.example.tiempo.data.model.DayWeather
import com.example.tiempo.data.model.moonPhaseInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class WeatherNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

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
        NotificationHelper.ensureChannel(applicationContext)

        // En Android 13+ sin permiso concedido no se puede notificar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val title = "🌅 Buenos días · $locationName"
        val rain = rainSummary(day)
        val (moonEmoji, moonLabel) = moonPhaseInfo(day.moonPhase)

        val shortText = "${current.temp.roundToInt()}° ahora  ·  " +
            "Media ${day.tempMean.roundToInt()}°  ·  $rain"

        val bigText = buildString {
            append("🌡️ Ahora ${current.temp.roundToInt()}°  ·  ")
            append("Media del día ${day.tempMean.roundToInt()}°\n")
            append("${day.condition.emoji} $rain\n")
            if (airQuality != null) {
                append("🌬️ Calidad del aire: ${airQuality.label} (AQI ${airQuality.europeanAqi})\n")
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

        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(shortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Ver todo", openAppIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Descartar", dismissIntent)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(NotificationHelper.NOTIFICATION_ID, notification)
    }

    private fun rainSummary(day: DayWeather): String {
        val prob = day.precipProbability
        return when {
            prob != null && prob >= 50 -> "Lluvia probable ($prob%)"
            prob != null && prob > 0 -> "Posible lluvia ($prob%)"
            day.precipitationMm > 0 -> "Posible lluvia (${day.precipitationMm} mm)"
            else -> "Sin lluvia prevista"
        }
    }

    private fun LocalDateTime.formatHour(): String = format(hourFormatter)

    private companion object {
        val hourFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
