package com.example.tiempo.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationHelper {
    const val CHANNEL_ID = "daily_weather"
    const val NOTIFICATION_ID = 1001

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Previsión diaria",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Resumen del tiempo cada mañana"
            }
            manager.createNotificationChannel(channel)
        }
    }
}
