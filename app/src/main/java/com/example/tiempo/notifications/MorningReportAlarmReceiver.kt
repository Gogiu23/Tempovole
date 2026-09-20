package com.example.tiempo.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Recibe la alarma exacta diaria (ver [NotificationScheduler]) y hace dos cosas:
 * encola el trabajo que de verdad pide el tiempo y muestra el aviso, y vuelve a
 * programar la alarma de mañana — setAlarmClock() dispara una sola vez, no es periódica.
 */
class MorningReportAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val request = OneTimeWorkRequestBuilder<WeatherNotificationWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueue(request)

        NotificationScheduler.scheduleDaily(
            context,
            hour = NotificationPreferences.getHour(context),
            minute = NotificationPreferences.getMinute(context)
        )
    }
}
