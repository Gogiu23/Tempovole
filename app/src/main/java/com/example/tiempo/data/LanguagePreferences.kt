package com.example.tiempo.data

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Idioma de la app, independiente del idioma del sistema.
 *
 * MainActivity es un ComponentActivity normal (no AppCompatActivity), así que
 * AppCompatDelegate.setApplicationLocales() no sirve aquí: sin una AppCompatDelegate
 * ligada a la Activity, esa llamada no toca el LocaleManager del framework y se queda
 * en un no-op silencioso. En API 33+ usamos el LocaleManager del framework directamente
 * (el propio sistema recrea la Activity al cambiarlo). En API <33 no existe esa API, así
 * que guardamos el idioma elegido nosotros y lo aplicamos envolviendo el Context base.
 */
object LanguagePreferences {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "language_tag"

    /** Tag de idioma activo (ej. "en"), o null si sigue el idioma del sistema. */
    fun getLanguageTag(context: Context): String? {
        if (Build.VERSION.SDK_INT >= 33) {
            // toLanguageTags() devuelve la lista entera separada por comas; el selector
            // de la app solo pone uno, pero Locale.forLanguageTag() no entiende una lista,
            // asi que se queda con el primero y no con un locale indefinido.
            val tags = context.getSystemService(LocaleManager::class.java).applicationLocales.toLanguageTags()
            return tags.substringBefore(',').ifEmpty { null }
        }
        return prefs(context).getString(KEY_LANGUAGE, null)
    }

    /** tag null = seguir el idioma del sistema. */
    fun setLanguageTag(context: Context, tag: String?) {
        if (Build.VERSION.SDK_INT >= 33) {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                if (tag == null) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(tag)
        } else {
            prefs(context).edit().putString(KEY_LANGUAGE, tag).apply()
        }
    }

    /** Envuelve el Context base de la Activity con el idioma guardado. En API 33+ no hace falta: lo aplica el sistema. */
    fun wrapContext(base: Context): Context {
        if (Build.VERSION.SDK_INT >= 33) return base
        return localizedContextForTag(base, getLanguageTag(base))
    }

    /**
     * Envuelve `base` con el idioma de `tag` (o lo devuelve tal cual si `tag` es null, es decir,
     * sigue el sistema). Para sitios sin Activity (el widget, en background): a diferencia de una
     * Activity, el framework no reaplica el idioma por-app de forma fiable aunque sea API 33+, así
     * que ahí conviene pasar el tag ya leído de un sitio reactivo (el estado de Glance) en vez de
     * volver a consultarlo aquí con [getLanguageTag] sobre un Context que puede venir de una sesión
     * de Glance creada antes del cambio de idioma.
     */
    fun localizedContextForTag(base: Context, tag: String?): Context {
        if (tag == null) return base
        val config = Configuration(base.resources.configuration)
        config.setLocale(Locale.forLanguageTag(tag))
        return base.createConfigurationContext(config)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
