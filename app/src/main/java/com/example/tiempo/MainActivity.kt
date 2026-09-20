package com.example.tiempo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.work.WorkManager
import com.example.tiempo.notifications.NotificationHelper
import com.example.tiempo.notifications.NotificationPreferences
import com.example.tiempo.notifications.NotificationScheduler
import com.example.tiempo.ui.WeatherScreen
import com.example.tiempo.ui.theme.TiempoTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* nada */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            TiempoTheme {
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
