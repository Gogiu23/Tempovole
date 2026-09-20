package com.example.tiempo.notifications

import android.content.Context

/** Hora y minuto a los que se envía el morning report diario. */
object NotificationPreferences {
    const val DEFAULT_HOUR = 8
    const val DEFAULT_MINUTE = 0

    fun getHour(context: Context): Int =
        prefs(context).getInt(KEY_HOUR, DEFAULT_HOUR)

    fun setHour(context: Context, hour: Int) {
        prefs(context).edit().putInt(KEY_HOUR, hour).apply()
    }

    fun getMinute(context: Context): Int =
        prefs(context).getInt(KEY_MINUTE, DEFAULT_MINUTE)

    fun setMinute(context: Context, minute: Int) {
        prefs(context).edit().putInt(KEY_MINUTE, minute).apply()
    }

    /**
     * Antes de usar AlarmManager, el morning report se programaba con un trabajo periódico
     * de WorkManager. Esos trabajos antiguos pueden seguir vivos en el dispositivo (WorkManager
     * no los borra al actualizar la app) y disparar notificaciones con el horario viejo. Esto
     * marca si ya se hizo la limpieza única de ese trabajo heredado.
     */
    fun isLegacyWorkCleared(context: Context): Boolean =
        prefs(context).getBoolean(KEY_LEGACY_WORK_CLEARED, false)

    fun markLegacyWorkCleared(context: Context) {
        prefs(context).edit().putBoolean(KEY_LEGACY_WORK_CLEARED, true).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)

    private const val KEY_HOUR = "hour"
    private const val KEY_MINUTE = "minute"
    private const val KEY_LEGACY_WORK_CLEARED = "legacy_work_cleared"
}
