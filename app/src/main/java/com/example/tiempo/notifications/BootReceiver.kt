package com.example.tiempo.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Reprograma el morning report al reiniciar el móvil (las alarmas no sobreviven a un reboot). */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        NotificationScheduler.scheduleDaily(
            context,
            hour = NotificationPreferences.getHour(context),
            minute = NotificationPreferences.getMinute(context)
        )
    }
}
