package com.example.tiempo.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.example.tiempo.R

object NotificationHelper {
    const val CHANNEL_ID = "daily_weather"
    const val NOTIFICATION_ID = 1001

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_description)
            }
            manager.createNotificationChannel(channel)
        }
    }
}
