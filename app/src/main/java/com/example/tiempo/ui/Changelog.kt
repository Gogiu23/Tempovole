package com.example.tiempo.ui

import android.content.Context
import com.example.tiempo.BuildConfig
import com.example.tiempo.R

/** Una versión publicada de la app y lo que cambió en ella. */
data class ChangelogEntry(
    val version: String,
    @androidx.annotation.StringRes val dateRes: Int,
    val changeRes: List<Int>
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
            version = "1.5",
            dateRes = R.string.changelog_v15_date,
            changeRes = listOf(
                R.string.changelog_v15_item1,
                R.string.changelog_v15_item2
            )
        ),
        ChangelogEntry(
            version = "1.4",
            dateRes = R.string.changelog_v14_date,
            changeRes = listOf(
                R.string.changelog_v14_item1,
                R.string.changelog_v14_item2,
                R.string.changelog_v14_item3,
                R.string.changelog_v14_item4
            )
        ),
        ChangelogEntry(
            version = "1.3",
            dateRes = R.string.changelog_v13_date,
            changeRes = listOf(
                R.string.changelog_v13_item1
            )
        ),
        ChangelogEntry(
            version = "1.2",
            dateRes = R.string.changelog_v12_date,
            changeRes = listOf(
                R.string.changelog_v12_item1,
                R.string.changelog_v12_item2,
                R.string.changelog_v12_item3,
                R.string.changelog_v12_item4,
                R.string.changelog_v12_item5,
                R.string.changelog_v12_item6,
                R.string.changelog_v12_item7,
                R.string.changelog_v12_item8,
                R.string.changelog_v12_item9,
                R.string.changelog_v12_item10,
                R.string.changelog_v12_item11,
                R.string.changelog_v12_item12
            )
        ),
        ChangelogEntry(
            version = "1.1",
            dateRes = R.string.changelog_v11_date,
            changeRes = listOf(
                R.string.changelog_v11_item1,
                R.string.changelog_v11_item2,
                R.string.changelog_v11_item3,
                R.string.changelog_v11_item4,
                R.string.changelog_v11_item5,
                R.string.changelog_v11_item6,
                R.string.changelog_v11_item7
            )
        ),
        ChangelogEntry(
            version = "1.0",
            dateRes = R.string.changelog_v10_date,
            changeRes = listOf(
                R.string.changelog_v10_item1,
                R.string.changelog_v10_item2,
                R.string.changelog_v10_item3,
                R.string.changelog_v10_item4,
                R.string.changelog_v10_item5
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
