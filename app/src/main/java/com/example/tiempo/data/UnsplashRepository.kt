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
        // Las fotos guardadas por versiones anteriores no traen el enlace de aviso que
        // exige Unsplash, asi que se tratan como caducadas y se pide una nueva.
        if (prefs.getString(period.dateKey(), null) == today &&
            cached != null &&
            cached.downloadLocation != null
        ) {
            return cached
        }

        val fresh = runCatching {
            val photo = api.randomPhoto(query = period.query, clientId = key)
            BackgroundPhoto(
                url = photo.urls.regular,
                authorName = photo.user?.name,
                authorUrl = photo.user?.links?.html,
                photoUrl = photo.links?.html,
                downloadLocation = photo.links?.downloadLocation
            )
        }.getOrNull()

        if (fresh != null) {
            prefs.edit()
                .putString(period.dateKey(), today)
                .putString(period.urlKey(), fresh.url)
                .putString(period.authorKey(), fresh.authorName)
                .putString(period.authorUrlKey(), fresh.authorUrl)
                .putString(period.photoUrlKey(), fresh.photoUrl)
                .putString(period.downloadKey(), fresh.downloadLocation)
                .apply()
            trackUsage(listOf(fresh))
        }
        return fresh ?: cached
    }

    /** Solo la URL, para quien no necesita los datos de autoría (por ejemplo, el widget). */
    suspend fun backgroundUrl(): String? = backgroundPhoto()?.url

    /**
     * Foto de la ubicacion actual (se busca por su nombre), para la tarjeta protagonista
     * de "Hoy" mientras es grande. Se guarda un dia en una sola plaza, no una por
     * ubicacion: solo hace falta la de la ciudad que se ve ahora mismo, asi que si el
     * usuario cambia de ciudad simplemente se pide y se guarda otra encima.
     */
    suspend fun locationPhoto(locationName: String): BackgroundPhoto? {
        val key = BuildConfig.UNSPLASH_ACCESS_KEY
        if (key.isBlank()) return null

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = LocalDate.now().toString()

        val cached = prefs.readLocationPhoto()
        if (prefs.getString(KEY_LOCATION_NAME, null) == locationName &&
            prefs.getString(KEY_LOCATION_DATE, null) == today &&
            cached != null &&
            cached.downloadLocation != null
        ) {
            return cached
        }

        val fresh = runCatching {
            val photo = api.randomPhoto(
                orientation = "landscape",
                query = locationName,
                clientId = key
            )
            BackgroundPhoto(
                url = photo.urls.regular,
                authorName = photo.user?.name,
                authorUrl = photo.user?.links?.html,
                photoUrl = photo.links?.html,
                downloadLocation = photo.links?.downloadLocation
            )
        }.getOrNull()

        if (fresh != null) {
            prefs.edit()
                .putString(KEY_LOCATION_NAME, locationName)
                .putString(KEY_LOCATION_DATE, today)
                .putString(KEY_LOCATION_URL, fresh.url)
                .putString(KEY_LOCATION_AUTHOR, fresh.authorName)
                .putString(KEY_LOCATION_AUTHOR_URL, fresh.authorUrl)
                .putString(KEY_LOCATION_PHOTO_URL, fresh.photoUrl)
                .putString(KEY_LOCATION_DOWNLOAD, fresh.downloadLocation)
                .apply()
            trackUsage(listOf(fresh))
        }
        return fresh ?: cached
    }

    /**
     * Avisa a Unsplash de que estas fotos se estan usando, como exigen sus normas de uso
     * de la API: es asi como contabilizan las descargas para sus fotografos.
     *
     * Cada foto se avisa una sola vez al dia, porque el aviso tambien gasta cuota: las
     * mismas siete tarjetas se miran muchas veces al dia y no son siete usos nuevos.
     */
    suspend fun trackUsage(photos: List<BackgroundPhoto>) {
        val key = BuildConfig.UNSPLASH_ACCESS_KEY
        if (key.isBlank()) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = LocalDate.now().toEpochDay()

        // Registro de las fotos que se estan mostrando hoy: son las unicas que hay que
        // acreditar. Caduca por fecha, asi que una foto que deja de usarse desaparece
        // sola de los creditos sin tener que limpiarla a mano.
        val usedToday = prefs.readUsedToday()
        val used = (usedToday + photos).distinctBy { it.url }
        if (used.size != usedToday.size) {
            prefs.edit()
                .putLong(KEY_USED_DAY, today)
                .putString(KEY_USED, used.serialize())
                .apply()
        }

        val alreadyTracked = if (prefs.getLong(KEY_TRACKED_DAY, 0L) == today) {
            prefs.getStringSet(KEY_TRACKED, emptySet()).orEmpty()
        } else {
            emptySet()
        }

        val pending = photos.mapNotNull { it.downloadLocation }
            .distinct()
            .filterNot { it in alreadyTracked }
        if (pending.isEmpty()) return

        pending.forEach { location -> runCatching { api.trackDownload(location, key) } }
        prefs.edit()
            .putLong(KEY_TRACKED_DAY, today)
            .putStringSet(KEY_TRACKED, alreadyTracked + pending)
            .apply()
    }

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
        private const val KEY_TRACKED = "tracked_downloads"
        private const val KEY_USED = "used_photos"
        private const val KEY_USED_DAY = "used_photos_day"
        private const val KEY_TRACKED_DAY = "tracked_downloads_day"
        private const val KEY_LOCATION_NAME = "location_photo_name"
        private const val KEY_LOCATION_DATE = "location_photo_date"
        private const val KEY_LOCATION_URL = "location_photo_url"
        private const val KEY_LOCATION_AUTHOR = "location_photo_author"
        private const val KEY_LOCATION_AUTHOR_URL = "location_photo_author_url"
        private const val KEY_LOCATION_PHOTO_URL = "location_photo_photo_url"
        private const val KEY_LOCATION_DOWNLOAD = "location_photo_download"
        private const val SEPARATOR = "\u001F"
        private const val RECORD_SEPARATOR = "\u001E"

        /** Fotos que se guardan de cada tipo de tiempo: suficientes para no repetir en semanas. */
        private const val PHOTOS_PER_CONDITION = 10

        /** Cada cuántos días se renueva la colección de fotos de una condición. */
        private const val REFRESH_DAYS = 7L

        /** Las fotos que se estan mostrando hoy, o vacio si el registro es de otro dia. */
        private fun SharedPreferences.readUsedToday(): List<BackgroundPhoto> =
            if (getLong(KEY_USED_DAY, 0L) == LocalDate.now().toEpochDay()) {
                getString(KEY_USED, null).parsePhotos()
            } else {
                emptyList()
            }

        private fun String?.parsePhotos(): List<BackgroundPhoto> =
            orEmpty()
                .split(RECORD_SEPARATOR)
                .mapNotNull { entry ->
                    val parts = entry.split(SEPARATOR)
                    val url = parts.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    BackgroundPhoto(
                        url = url,
                        authorName = parts.getOrNull(1)?.takeIf { it.isNotBlank() },
                        authorUrl = parts.getOrNull(2)?.takeIf { it.isNotBlank() },
                        photoUrl = parts.getOrNull(3)?.takeIf { it.isNotBlank() },
                        downloadLocation = parts.getOrNull(4)?.takeIf { it.isNotBlank() }
                    )
                }

        private fun SharedPreferences.readConditionPhotos(
            condition: WeatherCondition
        ): List<BackgroundPhoto> =
            getString(condition.photosKey(), null).parsePhotos()
                // Si vienen de una version anterior les falta el enlace de aviso: se
                // descartan enteras para que se pida la coleccion otra vez.
                .takeIf { photos -> photos.all { it.downloadLocation != null } }
                .orEmpty()

        private fun SharedPreferences.readPhoto(
            period: UnsplashConfig.DayPeriod
        ): BackgroundPhoto? {
            val url = getString(period.urlKey(), null) ?: return null
            return BackgroundPhoto(
                url = url,
                authorName = getString(period.authorKey(), null),
                authorUrl = getString(period.authorUrlKey(), null),
                photoUrl = getString(period.photoUrlKey(), null),
                downloadLocation = getString(period.downloadKey(), null)
            )
        }

        private fun SharedPreferences.readLocationPhoto(): BackgroundPhoto? {
            val url = getString(KEY_LOCATION_URL, null) ?: return null
            return BackgroundPhoto(
                url = url,
                authorName = getString(KEY_LOCATION_AUTHOR, null),
                authorUrl = getString(KEY_LOCATION_AUTHOR_URL, null),
                photoUrl = getString(KEY_LOCATION_PHOTO_URL, null),
                downloadLocation = getString(KEY_LOCATION_DOWNLOAD, null)
            )
        }

        private fun UnsplashConfig.DayPeriod.dateKey() = "date_$name"
        private fun UnsplashConfig.DayPeriod.urlKey() = "url_$name"
        private fun UnsplashConfig.DayPeriod.authorKey() = "author_$name"
        private fun UnsplashConfig.DayPeriod.authorUrlKey() = "author_url_$name"
        private fun UnsplashConfig.DayPeriod.photoUrlKey() = "photo_url_$name"
        private fun UnsplashConfig.DayPeriod.downloadKey() = "download_$name"

        private fun WeatherCondition.photosKey() = "photos_$name"
        private fun WeatherCondition.fetchedKey() = "photos_fetched_$name"
        private fun WeatherCondition.triedKey() = "photos_tried_$name"

        private fun UnsplashPhoto.toBackgroundPhoto() = BackgroundPhoto(
            // `raw` viene sin recorte ni tamaño: deja pedir a imgix justo el que hace falta.
            url = urls.raw ?: urls.regular,
            authorName = user?.name,
            authorUrl = user?.links?.html,
            photoUrl = links?.html,
            downloadLocation = links?.downloadLocation
        )

        private fun List<BackgroundPhoto>.serialize(): String = joinToString(RECORD_SEPARATOR) {
            listOf(
                it.url,
                it.authorName.orEmpty(),
                it.authorUrl.orEmpty(),
                it.photoUrl.orEmpty(),
                it.downloadLocation.orEmpty()
            ).joinToString(SEPARATOR)
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
         * Autores a los que hay que dar crédito: los de las fotos que la app **enseña**,
         * que son 11 al día — los tres fondos de las franjas, la de la ubicación y las
         * siete de las tarjetas de la semana.
         *
         * Se acredita lo mostrado, no lo guardado: de cada condición hay
         * [PHOTOS_PER_CONDITION] fotos en caché pero solo sale una por tarjeta, y una
         * foto que duerme en la caché sin verse no se ha usado. Las mostradas las
         * registra [trackUsage], que es el mismo sitio por el que se avisa a Unsplash.
         *
         * No es un histórico: el registro caduca por fecha, así que cuando una foto deja
         * de usarse su autor desaparece de la lista solo.
         */
        fun credits(context: Context): List<PhotoCredit> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            // Versiones anteriores llevaban un histórico acumulativo aquí. Ya no se usa.
            if (prefs.contains(LEGACY_KEY_CREDITS)) {
                prefs.edit().remove(LEGACY_KEY_CREDITS).apply()
            }
            val background = UnsplashConfig.DayPeriod.entries.mapNotNull { prefs.readPhoto(it) }
            val location = listOfNotNull(prefs.readLocationPhoto())
            val shown = prefs.readUsedToday()
            return (background + location + shown)
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
