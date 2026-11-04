package com.example.tiempo.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Relieve del mapa: baja las teselas de elevación abiertas "Terrarium" (AWS Terrain Tiles:
 * Mapzen, NASA SRTM...) que cubren la caja del mapa y las convierte en una imagen con color por
 * altitud y sombreado de laderas. El mar (elevación <= 0) queda transparente.
 */
object TerrainRepository {
    private val client = OkHttpClient()
    private const val WIDTH = 320
    private const val TILE = 256

    /** Luz desde el noroeste a 45° de altura y relieve exagerado: en llanuras apenas se nota si no. */
    private const val LIGHT_AZIMUTH = 315.0
    private const val LIGHT_ALTITUDE = 45.0
    private const val EXAGGERATION = 4.0

    private var cached: Pair<List<Double>, Bitmap>? = null

    suspend fun load(field: WindField): Bitmap? {
        val key = listOf(field.latMin, field.latMax, field.lonMin, field.lonMax)
        cached?.takeIf { it.first == key }?.let { return it.second }
        return build(field)?.also { cached = key to it }
    }

    private fun tileX(lon: Double, z: Int) = (lon + 180.0) / 360.0 * (1 shl z)

    private fun tileY(lat: Double, z: Int): Double {
        val r = Math.toRadians(lat.coerceIn(-85.0, 85.0))
        return (1.0 - ln(tan(r) + 1.0 / cos(r)) / PI) / 2.0 * (1 shl z)
    }

    private suspend fun build(field: WindField): Bitmap? {
        // z7 si no son demasiadas teselas; si lo son (latitudes altas), z6.
        var z = 7
        fun range(z: Int) = listOf(
            floor(tileX(field.lonMin, z)).toInt(), floor(tileX(field.lonMax, z)).toInt(),
            floor(tileY(field.latMax, z)).toInt(), floor(tileY(field.latMin, z)).toInt()
        )
        var (x0, x1, y0, y1) = range(z)
        if ((x1 - x0 + 1) * (y1 - y0 + 1) > 30) { z = 6; range(z).let { x0 = it[0]; x1 = it[1]; y0 = it[2]; y1 = it[3] } }
        val n = 1 shl z

        val tiles = HashMap<Long, IntArray>()
        coroutineScope {
            (y0..y1).flatMap { y -> (x0..x1).map { x -> x to y } }
                .filter { (x, y) -> x in 0 until n && y in 0 until n }
                .map { (x, y) -> async(Dispatchers.IO) { fetch(z, x, y)?.let { ((x.toLong() shl 32) or y.toLong()) to it } } }
                .awaitAll().filterNotNull().forEach { tiles[it.first] = it.second }
        }
        if (tiles.isEmpty()) return null

        return withContext(Dispatchers.Default) { render(field, z, tiles) }
    }

    private fun fetch(z: Int, x: Int, y: Int): IntArray? = try {
        val req = Request.Builder().url("https://elevation-tiles-prod.s3.amazonaws.com/terrarium/$z/$x/$y.png").build()
        client.newCall(req).execute().use { r ->
            if (!r.isSuccessful) return null
            val bytes = r.body?.bytes() ?: return null
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            IntArray(TILE * TILE).also { bmp.getPixels(it, 0, TILE, 0, 0, TILE, TILE); bmp.recycle() }
        }
    } catch (e: java.io.IOException) {
        null
    }

    private fun render(field: WindField, z: Int, tiles: Map<Long, IntArray>): Bitmap {
        val w = WIDTH
        val h = (w / WindMapRepository.ASPECT).toInt()

        // Elevación en metros de un píxel global de teselas; sin tesela, 0.
        fun elevAt(ix: Int, iy: Int): Float {
            val t = tiles[((ix shr 8).toLong() shl 32) or (iy shr 8).toLong()] ?: return 0f
            val p = t[(iy and 255) * TILE + (ix and 255)]
            return ((p shr 16) and 255) * 256f + ((p shr 8) and 255) + (p and 255) / 256f - 32768f
        }

        val elev = FloatArray(w * h)
        for (r in 0 until h) for (c in 0 until w) {
            val lon = field.lonMin + (field.lonMax - field.lonMin) * c / (w - 1)
            val lat = field.latMax - (field.latMax - field.latMin) * r / (h - 1)
            val gx = tileX(lon, z) * TILE - 0.5
            val gy = tileY(lat, z) * TILE - 0.5
            val ix = floor(gx).toInt(); val iy = floor(gy).toInt()
            val fx = (gx - ix).toFloat(); val fy = (gy - iy).toFloat()
            elev[r * w + c] =
                (elevAt(ix, iy) * (1 - fx) + elevAt(ix + 1, iy) * fx) * (1 - fy) +
                    (elevAt(ix, iy + 1) * (1 - fx) + elevAt(ix + 1, iy + 1) * fx) * fy
        }

        val cellX = field.widthKm * 1000.0 / w
        val cellY = field.heightKm * 1000.0 / h
        val zenith = Math.toRadians(90.0 - LIGHT_ALTITUDE)
        val azimuth = Math.toRadians((360.0 - LIGHT_AZIMUTH + 90.0) % 360.0)
        fun e(c: Int, r: Int) = elev[r.coerceIn(0, h - 1) * w + c.coerceIn(0, w - 1)]

        val pixels = IntArray(w * h)
        for (r in 0 until h) for (c in 0 until w) {
            val m = elev[r * w + c]
            if (m <= 0.5f) continue // mar: transparente
            val a = e(c - 1, r - 1); val b = e(c, r - 1); val cc = e(c + 1, r - 1)
            val d = e(c - 1, r); val f = e(c + 1, r)
            val g = e(c - 1, r + 1); val hh = e(c, r + 1); val i = e(c + 1, r + 1)
            val dzdx = ((cc + 2 * f + i) - (a + 2 * d + g)) / (8.0 * cellX)
            val dzdy = ((g + 2 * hh + i) - (a + 2 * b + cc)) / (8.0 * cellY)
            val slope = atan(EXAGGERATION * sqrt(dzdx * dzdx + dzdy * dzdy))
            val aspect = atan2(dzdy, -dzdx)
            val shade = (cos(zenith) * cos(slope) + sin(zenith) * sin(slope) * cos(azimuth - aspect)).coerceIn(0.0, 1.0)
            val k = (0.35 + 0.9 * shade).toFloat()
            val rgb = hypsometric(m)
            fun ch(shift: Int) = (((rgb shr shift) and 255) * k).toInt().coerceIn(0, 255)
            pixels[r * w + c] = (0xFF shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
        }
        return Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
    }

    /** Verde en la costa, tierra y pardo al subir, y casi blanco en las cumbres altas. */
    private val STOPS = listOf(
        0f to 0x5A7350, 300f to 0x7A8A5A, 700f to 0xA09A62,
        1200f to 0x9A7A55, 2000f to 0xB39B7E, 3000f to 0xD6CCBF, 4500f to 0xFFFFFF
    )

    private fun hypsometric(m: Float): Int {
        if (m >= STOPS.last().first) return STOPS.last().second
        val i = STOPS.indexOfLast { it.first <= m }.coerceAtLeast(0)
        val (m0, c0) = STOPS[i]
        val (m1, c1) = STOPS[i + 1]
        val t = ((m - m0) / (m1 - m0)).coerceIn(0f, 1f)
        fun mix(shift: Int) = (((c0 shr shift) and 255) * (1 - t) + ((c1 shr shift) and 255) * t).toInt()
        return (mix(16) shl 16) or (mix(8) shl 8) or mix(0)
    }
}
