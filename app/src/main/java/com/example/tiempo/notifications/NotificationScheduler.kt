package com.example.tiempo.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.tiempo.MainActivity
import java.util.Calendar

object NotificationScheduler {
    private const val REQUEST_CODE = 4001

    /**
     * Programa el morning report a la hora indicada (por defecto 8:00), usando
     * AlarmManager.setAlarmClock(): a diferencia de WorkManager, estas alarmas están
     * exentas de Doze y de los recortes de batería de fabricantes como Xiaomi/HyperOS,
     * así que suenan exactamente a su hora sin depender de que se abra la app.
     * Como no es periódica, [MorningReportAlarmReceiver] se reprograma sola cada día.
     */
    fun scheduleDaily(context: Context, hour: Int = 8, minute: Int = 0) {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) add(Calendar.DAY_OF_MONTH, 1)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val triggerIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, MorningReportAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Se usa si el usuario toca el icono de alarma que Android muestra en la barra de estado.
        val showAppIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(next.timeInMillis, showAppIntent),
            triggerIntent
        )
    }
}
