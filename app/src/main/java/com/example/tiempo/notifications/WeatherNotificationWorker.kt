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

        val title = applicationContext.getString(R.string.notif_daily_title, locationName)
        val rain = rainSummary(day)
        val (moonEmoji, moonLabelRes) = moonPhaseInfo(day.moonPhase)
        val moonLabel = applicationContext.getString(moonLabelRes)

        val shortText = applicationContext.getString(
            R.string.notif_daily_short_text,
            current.temp.roundToInt(),
            day.tempMean.roundToInt(),
            rain
        )

        val bigText = buildString {
            append(
                applicationContext.getString(
                    R.string.notif_daily_body_now_avg,
                    current.temp.roundToInt(),
                    day.tempMean.roundToInt()
                )
            )
            append("${day.condition.emoji} $rain\n")
            if (airQuality != null) {
                append(
                    applicationContext.getString(
                        R.string.notif_air_quality,
                        applicationContext.getString(airQuality.labelRes),
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

        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(shortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                applicationContext.getString(R.string.notif_action_view_all),
                openAppIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                applicationContext.getString(R.string.notif_action_dismiss),
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
                applicationContext.getString(R.string.notif_rain_likely, prob)
            prob != null && prob > 0 ->
                applicationContext.getString(R.string.notif_rain_possible_percent, prob)
            day.precipitationMm > 0 ->
                applicationContext.getString(R.string.notif_rain_possible_mm, day.precipitationMm.toString())
            else -> applicationContext.getString(R.string.notif_rain_unlikely)
        }
    }

    private fun LocalDateTime.formatHour(): String = format(hourFormatter)

    private companion object {
        val hourFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
