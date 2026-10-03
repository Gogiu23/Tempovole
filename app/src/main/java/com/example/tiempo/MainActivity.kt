package com.example.tiempo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.work.WorkManager
import com.example.tiempo.data.LanguagePreferences
import com.example.tiempo.notifications.NotificationHelper
import com.example.tiempo.notifications.NotificationPreferences
import com.example.tiempo.notifications.NotificationScheduler
import com.example.tiempo.ui.WeatherScreen
import com.example.tiempo.ui.theme.TempovoleTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* nada */ }

    // Solo hace falta en API <33: ahí no existe LocaleManager, así que el idioma elegido en
    // la pantalla de Idioma se guarda a mano y se aplica envolviendo el Context base.
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguagePreferences.wrapContext(newBase))
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Se recrea la Activity al cambiar de idioma (el sistema la relanza al cambiar el
        // idioma por-app). Por defecto esa transición hace un fundido a través de negro que
        // se nota como un parpadeo; con 0,0 se desactiva esa animación, tanto en este arranque
        // como en el siguiente (Android la aplica a la salida de ESTA instancia también).
        overridePendingTransition(0, 0)
        enableEdgeToEdge()

        NotificationHelper.ensureChannel(this)
        maybeRequestNotificationPermission()
        clearLegacyWorkManagerScheduleOnce()
        NotificationScheduler.scheduleDaily(
            this,
            hour = NotificationPreferences.getHour(this),
            minute = NotificationPreferences.getMinute(this)
        )

        setContent {
            TempovoleTheme {
                WeatherScreen()
            }
        }
    }

    /**
     * El morning report se programaba antes con un trabajo periódico de WorkManager; ese
     * trabajo puede haber quedado vivo en el dispositivo tras el cambio a AlarmManager y
     * seguir disparando notificaciones con el horario antiguo. Se cancela todo lo que haya
     * en WorkManager una única vez (no vuelve a tocar nada después) para limpiarlo.
     */
    private fun clearLegacyWorkManagerScheduleOnce() {
        if (NotificationPreferences.isLegacyWorkCleared(this)) return
        WorkManager.getInstance(this).cancelAllWork()
        NotificationPreferences.markLegacyWorkCleared(this)
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
