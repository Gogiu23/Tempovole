package com.example.tiempo.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.example.tiempo.R

object NotificationHelper {
    const val CHANNEL_ID = "daily_weather"
    const val NOTIFICATION_ID = 1001

    /**
     * [context] tiene que resolver los textos en el idioma elegido en la app: Android
     * congela el nombre y la descripcion del canal en el momento de crearlo y no los
     * vuelve a traducir nunca, asi que si se crea con el idioma equivocado se queda asi
     * para siempre. Por eso se llama a createNotificationChannel SIEMPRE y no solo cuando
     * el canal falta: crear uno con un id que ya existe no lo duplica ni pisa la
     * importancia ni los ajustes que haya tocado el usuario, solo refresca esos dos textos.
     */
    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
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
