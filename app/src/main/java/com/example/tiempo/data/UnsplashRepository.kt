package com.example.tiempo.data

import android.content.Context
import android.content.SharedPreferences
import com.example.tiempo.BuildConfig
import com.example.tiempo.UnsplashConfig
import com.example.tiempo.data.model.BackgroundPhoto
import com.example.tiempo.data.model.UnsplashPhoto
import com.example.tiempo.data.model.WeatherCondition
import java.time.LocalDate

/**
 * Devuelve la foto vertical de fondo correspondiente a la franja del día actual
 * (mañana, tarde o noche). Si no hay API key configurada en local.properties,
 * devuelve null y la UI usa un degradado de respaldo (así la app funciona sin key).
 *
 * Cada franja guarda su propia foto junto con la fecha: la imagen de la mañana es
 * la misma durante toda la mañana, la de la tarde durante toda la tarde, etc., y
 * solo se pide una nueva a Unsplash una vez al día por franja.
 */
class UnsplashRepository(
    private val context: Context,
    private val api: UnsplashApi = Network.unsplashApi
) {
    suspend fun backgroundPhoto(): BackgroundPhoto? {
        val key = BuildConfig.UNSPLASH_ACCESS_KEY
        if (key.isBlank()) return null

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val period = UnsplashConfig.DayPeriod.forTime()
        val today = LocalDate.now().toString()

        val cached = prefs.readPhoto(period)
        if (prefs.getString(period.dateKey(), null) == today && cached != null) {
            return cached
        }

        val fresh = runCatching {
            val photo = api.randomPhoto(query = period.query, clientId = key)
            BackgroundPhoto(
                url = photo.urls.regular,
                authorName = photo.user?.name,
                authorUrl = photo.user?.links?.html,
                photoUrl = photo.links?.html
            )
        }.getOrNull()

        if (fresh != null) {
            prefs.edit()
                .putString(period.dateKey(), today)
                .putString(period.urlKey(), fresh.url)
                .putString(period.authorKey(), fresh.authorName)
                .putString(period.authorUrlKey(), fresh.authorUrl)
                .putString(period.photoUrlKey(), fresh.photoUrl)
                .apply()
        }
        return fresh ?: cached
    }

    /** Solo la URL, para quien no necesita los datos de autoría (por ejemplo, el widget). */
    suspend fun backgroundUrl(): String? = backgroundPhoto()?.url

    /**
     * Fotos con las que ilustrar las tarjetas de la semana, agrupadas por el tiempo que
     * hará: así la foto del jueves enseña lluvia si el jueves llueve.
     *
     * Pide [PHOTOS_PER_CONDITION] fotos de cada condición en **una sola petición** y las
     * guarda [REFRESH_DAYS] días, de modo que el gasto habitual es de unas pocas
     * peticiones por semana y no una por tarjeta y día. Solo se piden las condiciones que
     * de verdad salen en la previsión, y una condición que falle no se reintenta hasta el
     * día siguiente: sin cobertura, abrir la app muchas veces no debe fundir la cuota.
     */
    suspend fun conditionPhotos(
        conditions: Collection<WeatherCondition>
    ): Map<WeatherCondition, List<BackgroundPhoto>> {
        val key = BuildConfig.UNSPLASH_ACCESS_KEY
        if (key.isBlank()) return emptyMap()

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = LocalDate.now().toEpochDay()

        return conditions.distinct().mapNotNull { condition ->
            val cached = prefs.readConditionPhotos(condition)
            val stale = today - prefs.getLong(condition.fetchedKey(), 0L) >= REFRESH_DAYS
            val triedToday = prefs.getLong(condition.triedKey(), 0L) == today

            val fresh = if ((cached.isEmpty() || stale) && !triedToday) {
                prefs.edit().putLong(condition.triedKey(), today).apply()
                runCatching {
                    api.randomPhotos(
                        query = condition.photoQuery,
                        count = PHOTOS_PER_CONDITION,
                        clientId = key
                    )
                }.getOrNull()?.map { it.toBackgroundPhoto() }?.takeIf { it.isNotEmpty() }
            } else {
                null
            }

            if (fresh != null) {
                prefs.edit()
                    .putString(condition.photosKey(), fresh.serialize())
                    .putLong(condition.fetchedKey(), today)
                    .apply()
            }

            val photos = fresh ?: cached
            if (photos.isEmpty()) null else condition to photos
        }.toMap()
    }

    companion object {
        private const val PREFS_NAME = "unsplash_cache"
        private const val LEGACY_KEY_CREDITS = "credits"
        private const val SEPARATOR = "\u001F"
        private const val RECORD_SEPARATOR = "\u001E"

        /** Fotos que se guardan de cada tipo de tiempo: suficientes para no repetir en semanas. */
        private const val PHOTOS_PER_CONDITION = 10

        /** Cada cuántos días se renueva la colección de fotos de una condición. */
        private const val REFRESH_DAYS = 7L

        private fun SharedPreferences.readConditionPhotos(
            condition: WeatherCondition
        ): List<BackgroundPhoto> =
            getString(condition.photosKey(), null).orEmpty()
                .split(RECORD_SEPARATOR)
                .mapNotNull { entry ->
                    val parts = entry.split(SEPARATOR)
                    val url = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    BackgroundPhoto(
                        url = url,
                        authorName = parts.getOrNull(1)?.takeIf { it.isNotBlank() },
                        authorUrl = parts.getOrNull(2)?.takeIf { it.isNotBlank() },
                        photoUrl = parts.getOrNull(3)?.takeIf { it.isNotBlank() }
                    )
                }

        private fun SharedPreferences.readPhoto(
            period: UnsplashConfig.DayPeriod
        ): BackgroundPhoto? {
            val url = getString(period.urlKey(), null) ?: return null
            return BackgroundPhoto(
                url = url,
                authorName = getString(period.authorKey(), null),
                authorUrl = getString(period.authorUrlKey(), null),
                photoUrl = getString(period.photoUrlKey(), null)
            )
        }

        private fun UnsplashConfig.DayPeriod.dateKey() = "date_$name"
        private fun UnsplashConfig.DayPeriod.urlKey() = "url_$name"
        private fun UnsplashConfig.DayPeriod.authorKey() = "author_$name"
        private fun UnsplashConfig.DayPeriod.authorUrlKey() = "author_url_$name"
        private fun UnsplashConfig.DayPeriod.photoUrlKey() = "photo_url_$name"

        private fun WeatherCondition.photosKey() = "photos_$name"
        private fun WeatherCondition.fetchedKey() = "photos_fetched_$name"
        private fun WeatherCondition.triedKey() = "photos_tried_$name"

        private fun UnsplashPhoto.toBackgroundPhoto() = BackgroundPhoto(
            // `raw` viene sin recorte ni tamaño: deja pedir a imgix justo el que hace falta.
            url = urls.raw ?: urls.regular,
            authorName = user?.name,
            authorUrl = user?.links?.html,
            photoUrl = links?.html
        )

        private fun List<BackgroundPhoto>.serialize(): String = joinToString(RECORD_SEPARATOR) {
            listOf(it.url, it.authorName.orEmpty(), it.authorUrl.orEmpty(), it.photoUrl.orEmpty())
                .joinToString(SEPARATOR)
        }

        /**
         * Foto que le toca a un día concreto. Depende de la fecha, así que cambia cada día
         * pero es la misma mientras dure el día, aunque se gire la pantalla o se recomponga.
         */
        fun photoForDay(photos: List<BackgroundPhoto>, date: LocalDate): BackgroundPhoto? {
            if (photos.isEmpty()) return null
            return photos[date.toEpochDay().mod(photos.size)]
        }

        /**
         * La misma foto recortada al tamaño que se va a pintar. Las transformaciones las
         * hace imgix sobre la marcha y **no gastan cuota de la API**: solo cuentan las
         * llamadas a api.unsplash.com, no las descargas de images.unsplash.com.
         */
        fun thumbUrl(url: String, sizePx: Int): String {
            val separator = if ('?' in url) "&" else "?"
            return "$url${separator}w=$sizePx&h=$sizePx&fit=crop&fm=jpg&q=75"
        }

        /**
         * Autores a los que hay que dar crédito: los de las fotos que la app tiene **en
         * uso ahora mismo** — los fondos de las tres franjas del día y las fotos
         * guardadas de cada tipo de tiempo.
         *
         * No es un histórico: cuando una foto deja de usarse (el fondo cambia de día, o
         * la colección de una condición se renueva a los [REFRESH_DAYS] días) su autor
         * desaparece de la lista solo, y ninguna foto que se esté mostrando puede
         * quedarse fuera.
         */
        fun credits(context: Context): List<PhotoCredit> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            // Versiones anteriores llevaban un histórico acumulativo aquí. Ya no se usa.
            if (prefs.contains(LEGACY_KEY_CREDITS)) {
                prefs.edit().remove(LEGACY_KEY_CREDITS).apply()
            }
            val background = UnsplashConfig.DayPeriod.entries.mapNotNull { prefs.readPhoto(it) }
            val conditions = WeatherCondition.entries.flatMap { prefs.readConditionPhotos(it) }
            return (background + conditions)
                .mapNotNull { photo ->
                    photo.authorName?.takeIf { it.isNotBlank() }?.let { name ->
                        PhotoCredit(
                            authorName = name,
                            authorUrl = photo.authorUrl,
                            photoUrl = photo.photoUrl
                        )
                    }
                }
                .distinctBy { it.authorName }
                .sortedBy { it.authorName.lowercase() }
        }
    }
}

/** Autor al que hay que dar crédito por una de las fotos de fondo usadas. */
data class PhotoCredit(
    val authorName: String,
    val authorUrl: String?,
    val photoUrl: String?
)
