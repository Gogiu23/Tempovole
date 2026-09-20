package com.example.tiempo.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

/** Recibe el botón "Descartar" del morning report y cierra la notificación. */
class DismissNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationManagerCompat.from(context).cancel(NotificationHelper.NOTIFICATION_ID)
    }
}
