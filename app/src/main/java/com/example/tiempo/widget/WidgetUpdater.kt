package com.example.tiempo.widget

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.example.tiempo.data.LanguagePreferences
import com.example.tiempo.data.WidgetPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Claves del estado de Glance donde se guarda el aspecto elegido para el widget. */
object WidgetStateKeys {
    val background = stringPreferencesKey("background")
    val color = intPreferencesKey("color")
    val languageTag = stringPreferencesKey("language_tag")
}

/**
 * Refresca el widget desde la app.
 *
 * El ajuste se escribe en el **estado de Glance**, no solo en SharedPreferences: llamar a
 * `updateAll()` sin más no vuelve a ejecutar `provideGlance` cuando el widget ya tiene una
 * sesión viva, así que la composición no se entera del cambio y el widget se queda como
 * estaba. Al escribir en el estado, Glance sí recompone con los valores nuevos.
 *
 * Además usa un scope propio ligado al proceso y no a la composición, para que el cambio
 * no se cancele si el usuario sale de Ajustes antes de que termine.
 */
object WidgetUpdater {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Las actualizaciones se encadenan de una en una: si se lanzaran a la vez (por ejemplo
    // al tocar varias opciones seguidas), la más lenta podría terminar la última y dejar
    // el widget pintado con el ajuste anterior.
    private val mutex = Mutex()

    fun requestUpdate(context: Context, delayMillis: Long = 0) {
        val appContext = context.applicationContext
        scope.launch {
            if (delayMillis > 0) delay(delayMillis)
            mutex.withLock {
                runCatching {
                    val mode = WidgetPreferences.getBackground(appContext).name
                    val color = WidgetPreferences.getColor(appContext).argb
                    // "" = seguir el idioma del sistema (DataStore no admite null en un string key).
                    val languageTag = LanguagePreferences.getLanguageTag(appContext) ?: ""
                    val widget = TempovoleWidget()

                    GlanceAppWidgetManager(appContext)
                        .getGlanceIds(TempovoleWidget::class.java)
                        .forEach { glanceId ->
                            updateAppWidgetState(appContext, glanceId) { prefs ->
                                prefs[WidgetStateKeys.background] = mode
                                prefs[WidgetStateKeys.color] = color
                                prefs[WidgetStateKeys.languageTag] = languageTag
                            }
                            widget.update(appContext, glanceId)
                        }
                }
            }
        }
    }
}
