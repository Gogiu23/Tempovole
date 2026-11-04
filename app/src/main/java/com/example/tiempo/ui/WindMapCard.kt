package com.example.tiempo.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import kotlin.math.roundToInt
import kotlin.math.atan2
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiempo.R
import com.example.tiempo.data.SavedLocation
import com.example.tiempo.data.TerrainRepository
import com.example.tiempo.data.WindField
import com.example.tiempo.data.WindMapRepository
import java.io.DataInputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.hypot
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PARTICLES = 450
private const val TRAIL = 10
private const val MAX_AGE = 110

/** Tiempo simulado por segundo real: a 30 km/h una partícula recorre ~22% del mapa por segundo. */
private const val HOURS_PER_SECOND = 6f

private val OCEAN = Color(0xFF0A1E3C)
private val LAND = Color(0xFF5A7350)

/**
 * Mapa propio con el viento (partículas que fluyen) y las nubes de la zona, con una rueda
 * de tiempo para ver cómo evolucionan. Datos de Open-Meteo.
 */
@Composable
fun WindMapCard(location: SavedLocation, animate: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var field by remember(location) { mutableStateOf<WindField?>(null) }
    var failed by remember(location) { mutableStateOf(false) }
    var baseMap by remember { mutableStateOf(BaseMap.EMPTY) }
    var windOn by rememberSaveable { mutableStateOf(true) }
    var cloudsOn by rememberSaveable { mutableStateOf(true) }
    var rainOn by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(location) {
        field = null
        failed = false
        try {
            field = WindMapRepository.load(location.lat, location.lon)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            failed = true
        }
    }
    LaunchedEffect(Unit) { baseMap = BaseMap.load(context) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.16f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.map_title),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LayerChip(stringResource(R.string.map_wind), windOn) { windOn = !windOn }
                LayerChip(stringResource(R.string.map_clouds), cloudsOn) { cloudsOn = !cloudsOn }
                LayerChip(stringResource(R.string.map_rain), rainOn) { rainOn = !rainOn }
            }
            val f = field
            when {
                f != null -> WindMapBody(f, baseMap, location.name, animate, windOn, cloudsOn, rainOn)
                failed -> Box(Modifier.fillMaxWidth().aspectRatio(WindMapRepository.ASPECT), Alignment.Center) {
                    Text(stringResource(R.string.map_error), color = Color.White.copy(alpha = 0.8f))
                }
                else -> Box(Modifier.fillMaxWidth().aspectRatio(WindMapRepository.ASPECT), Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun LayerChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = Color.White,
        fontSize = 14.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = if (selected) 0.32f else 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
private fun WindMapBody(
    field: WindField,
    baseMap: BaseMap,
    placeName: String,
    /** Las partículas solo se mueven con el mapa a la vista: fuera, gastan fotogramas para nada. */
    animate: Boolean,
    windOn: Boolean,
    cloudsOn: Boolean,
    rainOn: Boolean
) {
    val hourCount = field.times.size
    // Arranca en la hora actual de la zona; si no está en la lista, en el primer paso de hoy.
    var hourValue by remember(field) {
        val now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        mutableFloatStateOf(field.times.indexOf(now).takeIf { it >= 0 }?.toFloat() ?: 24f)
    }
    val hour = hourValue.toInt().coerceIn(0, hourCount - 1)
    val hourState = rememberUpdatedState(hour)

    val particles = remember(field) { Particles(PARTICLES) }
    var frame by remember { mutableLongStateOf(0L) }
    LaunchedEffect(field, windOn, animate) {
        if (!windOn || !animate) return@LaunchedEffect
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) particles.step(field, hourState.value, ((now - last) / 1e9f).coerceAtMost(0.05f))
                last = now
                frame = now
            }
        }
    }

    // Por debajo del 25% de cobertura no se pinta nada: el cielo despejado deja ver el mapa.
    val cloudImage = remember(field, hour) {
        fieldImage(field, field.cloud[hour]) { c ->
            val a = ((c - 25f) / 75f).coerceIn(0f, 1f)
            ((Math.pow(a.toDouble(), 1.2) * 0.6 * 255).toInt() shl 24) or 0xE8EEF7
        }
    }
    val rainImage = remember(field, hour) { fieldImage(field, field.rain[hour]) { rampArgb(it, RAIN_STOPS) } }

    // Relieve de la zona: llega aparte (descarga de teselas) y, hasta entonces, la tierra va lisa.
    var terrain by remember(field) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(field) {
        terrain = try {
            TerrainRepository.load(field)?.asImageBitmap()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    val density = LocalDensity.current
    val labelPaint = remember(density) { labelPaint(density, bold = false) }
    val placePaint = remember(density) { labelPaint(density, bold = true) }
    var mapSize by remember { mutableStateOf(IntSize.Zero) }
    val layers = remember(field, baseMap, placeName, mapSize) {
        if (mapSize == IntSize.Zero) null
        else buildLayers(field, baseMap, placeName, mapSize.width.toFloat(), mapSize.height.toFloat(), labelPaint, placePaint, density.density)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Tres capas con su propio RenderNode: las partículas se redibujan a cada fotograma, y
        // así no arrastran con ellas la tierra, el relieve, las nubes y los nombres, que solo
        // cambian con la hora o el tamaño.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(WindMapRepository.ASPECT)
                .clip(RoundedCornerShape(14.dp))
                .background(OCEAN)
                .onSizeChanged { mapSize = it }
        ) {
            Canvas(Modifier.fillMaxSize().graphicsLayer { }) {
                val w = size.width
                val h = size.height

                layers?.let { drawPath(it.land, LAND) }
                terrain?.let { drawImage(it, dstSize = IntSize(w.toInt(), h.toInt()), filterQuality = FilterQuality.High) }

                if (cloudsOn) {
                    drawImage(
                        image = cloudImage,
                        dstSize = IntSize(w.toInt(), h.toInt()),
                        filterQuality = FilterQuality.High
                    )
                }

                if (rainOn) {
                    drawImage(
                        image = rainImage,
                        dstSize = IntSize(w.toInt(), h.toInt()),
                        filterQuality = FilterQuality.High
                    )
                }

                layers?.let { drawPath(it.coast, Color.White.copy(alpha = 0.55f), style = Stroke(width = 1.2.dp.toPx())) }
            }

            if (windOn) {
                Canvas(Modifier.fillMaxSize().graphicsLayer { }) {
                    val w = size.width
                    val h = size.height
                    @Suppress("UNUSED_VARIABLE") val tick = frame // suscribe el dibujo a cada fotograma
                    for (i in 0 until particles.n) {
                        val b = i * TRAIL
                        val fade = minOf(1f, particles.age[i] / 12f, (MAX_AGE - particles.age[i]) / 12f).coerceAtLeast(0f)
                        val color = speedColor(particles.speed[i])
                        for (j in 0 until TRAIL - 1) {
                            val a = (1f - j / (TRAIL - 1f)) * fade
                            drawLine(
                                color = color.copy(alpha = a * 0.9f),
                                start = Offset(particles.tx[b + j] * w, particles.ty[b + j] * h),
                                end = Offset(particles.tx[b + j + 1] * w, particles.ty[b + j + 1] * h),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                }
            }

            Canvas(Modifier.fillMaxSize().graphicsLayer { }) {
                layers?.labels?.forEach { l ->
                    drawCircle(Color.White.copy(alpha = 0.9f), radius = 2.5.dp.toPx(), center = Offset(l.dotX, l.dotY))
                    drawContext.canvas.nativeCanvas.drawText(l.text, l.textX, l.baseline, if (l.bold) placePaint else labelPaint)
                }

                // La ubicación del usuario, siempre en el centro de la caja.
                val c = Offset(field.xFor(field.centerLon) * size.width, field.yFor(field.centerLat) * size.height)
                drawCircle(Color.White, radius = 5.dp.toPx(), center = c)
                drawCircle(Color(0xFFFF6B6B), radius = 3.dp.toPx(), center = c)
            }
        }

        // Valores en tu ubicación (el centro del mapa) a la hora elegida.
        val uHere = field.sample(field.u[hour], 0.5f, 0.5f)
        val vHere = field.sample(field.v[hour], 0.5f, 0.5f)
        Text(
            text = "💨 ${hypot(uHere, vHere).roundToInt()} km/h ${windArrow(uHere, vHere)}   " +
                "☁ ${field.sample(field.cloud[hour], 0.5f, 0.5f).roundToInt()} %   " +
                "🌧 ${"%.1f".format(field.sample(field.rain[hour], 0.5f, 0.5f))} mm/h",
            color = Color.White,
            fontSize = 14.sp
        )
        if (cloudsOn) {
            LegendBar(stringResource(R.string.map_clouds), "25%", "100%", listOf(Color(0x33E8EEF7), Color(0xFFE8EEF7)))
        }
        if (rainOn) {
            LegendBar(stringResource(R.string.map_rain), "0", "10+ mm/h", RAIN_STOPS.drop(1).map { Color(it.second or 0xFF000000.toInt()) })
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatHour(field.times[hour]),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(end = 12.dp)
            )
            Slider(
                value = hourValue,
                onValueChange = { hourValue = it },
                valueRange = 0f..(hourCount - 1).toFloat(),
                steps = hourCount - 2,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White.copy(alpha = 0.8f),
                    inactiveTrackColor = Color.White.copy(alpha = 0.25f),
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = stringResource(R.string.map_credits),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp
        )
    }
}

private fun formatHour(iso: String): String =
    LocalDateTime.parse(iso).format(DateTimeFormatter.ofPattern("EEE HH:mm"))

/** Azul con calma, verde, amarillo y rojo según se acelera el viento (km/h). */
private fun speedColor(kmh: Float): Color {
    val stops = listOf(
        0f to Color(0xFF4FC3F7), 15f to Color(0xFF66BB6A), 30f to Color(0xFFFFEE58),
        50f to Color(0xFFFF9800), 80f to Color(0xFFE53935)
    )
    if (kmh >= stops.last().first) return stops.last().second
    val i = stops.indexOfLast { it.first <= kmh }
    val (s0, c0) = stops[i]
    val (s1, c1) = stops[i + 1]
    return lerp(c0, c1, (kmh - s0) / (s1 - s0))
}

/** Bitmap de baja resolución del campo, interpolado entre los puntos de la rejilla, para estirarlo al lienzo. */
private fun fieldImage(field: WindField, grid: FloatArray, argb: (Float) -> Int): androidx.compose.ui.graphics.ImageBitmap {
    val w = 64
    val h = (w / WindMapRepository.ASPECT).toInt()
    val pixels = IntArray(w * h)
    for (py in 0 until h) for (px in 0 until w) {
        pixels[py * w + px] = argb(field.sample(grid, px / (w - 1f), py / (h - 1f)))
    }
    return Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888).asImageBitmap()
}

/** Escala de la lluvia: mm/h → color (ARGB). Por debajo de 0,1 no se pinta. */
private val RAIN_STOPS = listOf(
    0.05f to 0x005AA9FF,
    0.3f to 0x885AA9FF.toInt(),
    1f to 0xBB2F7BFF.toInt(),
    4f to 0xDD8E44FF.toInt(),
    10f to 0xEEE040FB.toInt()
)

private fun rampArgb(value: Float, stops: List<Pair<Float, Int>>): Int {
    if (value <= stops.first().first) return stops.first().second and 0x00FFFFFF
    if (value >= stops.last().first) return stops.last().second
    val i = stops.indexOfLast { it.first <= value }
    val (v0, c0) = stops[i]
    val (v1, c1) = stops[i + 1]
    val t = (value - v0) / (v1 - v0)
    fun ch(shift: Int) = (((c0 ushr shift) and 0xFF) * (1 - t) + ((c1 ushr shift) and 0xFF) * t).toInt()
    return (ch(24) shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
}

/** Flecha hacia donde sopla el viento (u al este, v al norte). */
private fun windArrow(u: Float, v: Float): String {
    val deg = (Math.toDegrees(atan2(u.toDouble(), v.toDouble())) + 360) % 360
    return listOf("↑", "↗", "→", "↘", "↓", "↙", "←", "↖")[((deg + 22.5) / 45).toInt() % 8]
}

@Composable
private fun LegendBar(label: String, from: String, to: String, colors: List<Color>) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, modifier = Modifier.width(56.dp))
        Text(from, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        Box(
            Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(Brush.horizontalGradient(colors))
        )
        Text(to, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
    }
}

private class Particles(val n: Int) {
    val x = FloatArray(n)
    val y = FloatArray(n)
    val age = IntArray(n)
    val speed = FloatArray(n)
    val tx = FloatArray(n * TRAIL)
    val ty = FloatArray(n * TRAIL)

    init { for (i in 0 until n) respawn(i, spreadAge = true) }

    private fun respawn(i: Int, spreadAge: Boolean = false) {
        x[i] = Random.nextFloat()
        y[i] = Random.nextFloat()
        age[i] = if (spreadAge) Random.nextInt(MAX_AGE) else 0
        for (j in 0 until TRAIL) { tx[i * TRAIL + j] = x[i]; ty[i * TRAIL + j] = y[i] }
    }

    fun step(f: WindField, hour: Int, dt: Float) {
        for (i in 0 until n) {
            val u = f.sample(f.u[hour], x[i], y[i])
            val v = f.sample(f.v[hour], x[i], y[i])
            speed[i] = hypot(u, v)
            x[i] += u * dt * HOURS_PER_SECOND / f.widthKm
            y[i] -= v * dt * HOURS_PER_SECOND / f.heightKm
            age[i]++
            val b = i * TRAIL
            System.arraycopy(tx, b, tx, b + 1, TRAIL - 1)
            System.arraycopy(ty, b, ty, b + 1, TRAIL - 1)
            tx[b] = x[i]
            ty[b] = y[i]
            if (age[i] > MAX_AGE || x[i] < 0f || x[i] > 1f || y[i] < 0f || y[i] > 1f) respawn(i)
        }
    }
}

private class Place(val name: String, val lon: Float, val lat: Float, val pop: Int)

/**
 * Datos del mapa base empaquetados en res/raw (Natural Earth, dominio público):
 * `coastline` y `land` guardan líneas/anillos como un Int con el número y, por cada uno, un Int
 * con sus puntos y pares (lon, lat) en centésimas de grado (Short); `places`, ciudades ordenadas
 * de mayor a menor población (nombre UTF, lon y lat en centésimas, población Int).
 * Si algún fichero falta, el mapa se dibuja sin esa parte.
 */
private class BaseMap(val coast: List<FloatArray>, val land: List<FloatArray>, val places: List<Place>) {
    companion object {
        val EMPTY = BaseMap(emptyList(), emptyList(), emptyList())
        private var cached: BaseMap? = null

        suspend fun load(context: Context): BaseMap = cached ?: withContext(Dispatchers.IO) {
            BaseMap(
                coast = readLines(context, "coastline"),
                land = readLines(context, "land"),
                places = readPlaces(context)
            ).also { cached = it }
        }

        private fun open(context: Context, name: String): DataInputStream? {
            val id = context.resources.getIdentifier(name, "raw", context.packageName)
            return if (id == 0) null else DataInputStream(context.resources.openRawResource(id).buffered())
        }

        private fun readLines(context: Context, name: String): List<FloatArray> =
            open(context, name)?.use { input ->
                List(input.readInt()) { FloatArray(input.readInt() * 2) { input.readShort() / 100f } }
            } ?: emptyList()

        private fun readPlaces(context: Context): List<Place> =
            open(context, "places")?.use { input ->
                List(input.readInt()) {
                    Place(input.readUTF(), input.readShort() / 100f, input.readShort() / 100f, input.readInt())
                }
            } ?: emptyList()
    }
}

private class MapLabel(
    val text: String,
    val textX: Float,
    val baseline: Float,
    val dotX: Float,
    val dotY: Float,
    val bold: Boolean
)

/** Lo que no cambia con la hora ni a cada fotograma: tierra, costas y nombres ya colocados. */
private class MapLayers(val land: Path, val coast: Path, val labels: List<MapLabel>)

private fun labelPaint(density: androidx.compose.ui.unit.Density, bold: Boolean) =
    android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = density.run { (if (bold) 13.sp else 11.sp).toPx() }
        if (bold) typeface = android.graphics.Typeface.DEFAULT_BOLD
        setShadowLayer(density.density * 2.5f, 0f, 0f, android.graphics.Color.BLACK)
    }

private const val MAX_LABELS = 18

private fun buildLayers(
    field: WindField,
    base: BaseMap,
    placeName: String,
    w: Float,
    h: Float,
    paint: android.graphics.Paint,
    boldPaint: android.graphics.Paint,
    density: Float
): MapLayers {
    // Tierra: se mantienen todos los puntos de un anillo (si no, se deforma al rellenar) y
    // solo se descartan los anillos enteros que caen fuera de la caja.
    val land = Path().apply { fillType = PathFillType.EvenOdd }
    for (ring in base.land) {
        var minX = Float.MAX_VALUE; var maxX = -Float.MAX_VALUE
        var minY = Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
        for (i in 0 until ring.size / 2) {
            val x = field.xFor(ring[2 * i].toDouble()); val y = field.yFor(ring[2 * i + 1].toDouble())
            if (x < minX) minX = x; if (x > maxX) maxX = x
            if (y < minY) minY = y; if (y > maxY) maxY = y
        }
        if (maxX < -0.1f || minX > 1.1f || maxY < -0.1f || minY > 1.1f) continue
        for (i in 0 until ring.size / 2) {
            val x = field.xFor(ring[2 * i].toDouble()) * w; val y = field.yFor(ring[2 * i + 1].toDouble()) * h
            if (i == 0) land.moveTo(x, y) else land.lineTo(x, y)
        }
        land.close()
    }

    val coast = Path()
    for (line in base.coast) {
        var open = false
        for (i in 0 until line.size / 2) {
            val x = field.xFor(line[2 * i].toDouble()); val y = field.yFor(line[2 * i + 1].toDouble())
            if (x < -0.2f || x > 1.2f || y < -0.2f || y > 1.2f) { open = false; continue }
            if (open) coast.lineTo(x * w, y * h) else { coast.moveTo(x * w, y * h); open = true }
        }
    }

    // Nombres: primero el de la ubicación y luego las ciudades de mayor a menor población,
    // aceptando cada una solo si su texto no pisa a otro nombre ni a un punto ya puesto.
    val rects = ArrayList<android.graphics.RectF>()
    val labels = ArrayList<MapLabel>()
    val gap = 6f * density
    fun place(text: String, x: Float, y: Float, p: android.graphics.Paint, bold: Boolean): Boolean {
        val tw = p.measureText(text)
        val th = p.textSize
        var tx = x + gap
        if (tx + tw > w - 4f * density) tx = x - gap - tw
        if (tx < 4f * density) return false
        val rect = android.graphics.RectF(tx - 3f, y - th * 0.75f, tx + tw + 3f, y + th * 0.5f)
        if (rect.top < 4f * density || rect.bottom > h - 4f * density) return false
        if (rects.any { android.graphics.RectF.intersects(it, rect) }) return false
        rects += rect
        rects += android.graphics.RectF(x - gap / 2, y - gap / 2, x + gap / 2, y + gap / 2)
        labels += MapLabel(text, tx, y + th * 0.35f, x, y, bold)
        return true
    }
    val cx = w / 2f
    val cy = h / 2f
    place(placeName, cx, cy, boldPaint, bold = true)
    // El marcador de la ubicación, para que ningún otro nombre lo pise (después, o taparía su propio nombre).
    rects += android.graphics.RectF(cx - 8f * density, cy - 8f * density, cx + 8f * density, cy + 8f * density)
    for (pl in base.places) {
        if (labels.size >= MAX_LABELS) break
        val x = field.xFor(pl.lon.toDouble()) * w
        val y = field.yFor(pl.lat.toDouble()) * h
        if (x < 0f || x > w || y < 0f || y > h) continue
        if (pl.name.equals(placeName, ignoreCase = true) || hypot(x - cx, y - cy) < 14f * density) continue
        place(pl.name, x, y, paint, bold = false)
    }
    return MapLayers(land, coast, labels)
}
