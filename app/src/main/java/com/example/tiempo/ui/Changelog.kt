package com.example.tiempo.ui

import android.content.Context
import com.example.tiempo.BuildConfig

/** Una versión publicada de la app y lo que cambió en ella. */
data class ChangelogEntry(
    val version: String,
    val date: String,
    val changes: List<String>
)

/**
 * Historial de cambios de la app, de más reciente a más antiguo.
 *
 * Cómo se gestiona (ver también app/build.gradle.kts):
 * 1. Durante el desarrollo normal NO se toca la versión: se trabaja sobre la que haya.
 * 2. Al sacar versión nueva: subir `versionCode` (+1, lo exige Google Play) y `versionName`
 *    en app/build.gradle.kts, y añadir aquí arriba una entrada con ese mismo `versionName`.
 * 3. Mientras una versión no esté publicada, se edita su entrada en vez de crear otra.
 * 4. Solo se listan cambios que se noten al usar la app, en lenguaje de usuario. El mismo
 *    texto sirve para el campo "Novedades" de Google Play.
 */
object Changelog {
    val entries: List<ChangelogEntry> = listOf(
        ChangelogEntry(
            version = "1.1",
            date = "20 de septiembre de 2026",
            changes = listOf(
                "Nueva tarjeta \"Sol y luna\" con el recorrido del sol, las horas de luz " +
                    "y la fase lunar dibujada.",
                "El fondo cambia según sea de mañana, tarde o noche.",
                "El widget ya se puede ver con imagen, transparente o con un color fijo " +
                    "a elegir.",
                "Los datos adicionales tardan mucho menos en aparecer.",
                "Animación de scroll más suave en la pantalla de Hoy.",
                "Créditos de los autores de las fotos en la pantalla de Info.",
                "Nueva pantalla de novedades (esta misma)."
            )
        ),
        ChangelogEntry(
            version = "1.0",
            date = "17 de septiembre de 2026",
            changes = listOf(
                "Primera versión: previsión de hoy y de los próximos 7 días.",
                "Gráfico por horas de temperatura, lluvia y viento.",
                "Datos adicionales configurables: UV, polen, oleaje, ríos, comparación " +
                    "histórica y más.",
                "Morning report: notificación diaria a la hora que elijas.",
                "Widget de pantalla de inicio."
            )
        )
    )

    val latestVersion: String get() = entries.first().version

    /** true si la versión instalada es la que encabeza el historial (lo normal). */
    val isInSync: Boolean get() = latestVersion == BuildConfig.VERSION_NAME

    /** Comprueba si [version] es la que está instalada ahora mismo. */
    fun isCurrent(version: String): Boolean = version == BuildConfig.VERSION_NAME
}

/** Recuerda qué versión de novedades ha visto ya el usuario, para el aviso del icono. */
object ChangelogPreferences {
    private const val PREFS = "changelog_prefs"
    private const val KEY_LAST_SEEN = "last_seen_version"

    /** true si hay novedades que el usuario todavía no ha abierto. */
    fun hasUnseenChanges(context: Context): Boolean =
        prefs(context).getString(KEY_LAST_SEEN, null) != BuildConfig.VERSION_NAME

    fun markSeen(context: Context) {
        prefs(context).edit().putString(KEY_LAST_SEEN, BuildConfig.VERSION_NAME).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
