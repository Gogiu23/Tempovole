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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
private val NIGHT_TINT = Color(0xFF050B2A)
private const val NIGHT_TINT_ALPHA = 0.62f
private const val NIGHT_CLOUD_DIM = 0.45f

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
                LayerChip(
                    stringResource(R.string.map_wind), R.drawable.ic_wind, windOn,
                    Color(0xFFBDBDBD), Color(0xFF616161), Color.White
                ) { windOn = !windOn }
                LayerChip(
                    stringResource(R.string.map_clouds), R.drawable.ic_clouds, cloudsOn,
                    Color(0xFF90CAF9), Color(0xFF1565C0), Color.White
                ) { cloudsOn = !cloudsOn }
                LayerChip(
                    stringResource(R.string.map_rain), R.drawable.ic_heavy_rain, rainOn,
                    Color(0xFFFFD700), Color(0xFF81D4FA), Color(0xFF1B1B1B)
                ) { rainOn = !rainOn }
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
private fun LayerChip(
    label: String,
    iconRes: Int,
    selected: Boolean,
    start: Color,
    end: Color,
    /** Letra con la capa activa, segun lo claro que sea su degradado. */
    selectedTextColor: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                Brush.horizontalGradient(
                    listOf(start, end).map { it.copy(alpha = if (selected) 1f else 0.4f) }
                )
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(18.dp))
        Text(
            text = label,
            color = if (selected) selectedTextColor else Color.White,
            fontSize = 14.sp
        )
    }
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
    // La posición se mide en horas (los datos son horarios) y avanza de 0,5 en 0,5: 30 min.
    val nowValue = remember(field) {
        val now = LocalDateTime.now()
        val index = field.times.indexOf(
            now.withMinute(0).withSecond(0).withNano(0)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        )
        if (index >= 0) index + (now.minute / 30) * HALF_HOUR else 24f
    }
    var hourValue by remember(field) { mutableFloatStateOf(nowValue) }
    var playing by rememberSaveable { mutableStateOf(false) }
    // Solo cambia al cruzar una hora: durante la reproducción, el cuerpo del mapa no se
    // recompone a cada fotograma, solo se redibujan las capas que leen `hourValue`.
    val hour by remember(field) { derivedStateOf { hourValue.toInt().coerceIn(0, hourCount - 1) } }
    val nextHour = (hour + 1).coerceAtMost(hourCount - 1)

    // Reproducción: avanza con el reloj de fotogramas (no a saltos de media hora) para que el
    // fundido entre horas sea continuo; al llegar al final se detiene.
    LaunchedEffect(playing, field, animate) {
        if (!playing || !animate) return@LaunchedEffect
        var last = 0L
        while (hourValue < hourCount - 1) {
            withFrameNanos { now ->
                if (last != 0L) {
                    hourValue = (hourValue + (now - last) / 1e9f * PLAY_HOURS_PER_SECOND)
                        .coerceAtMost((hourCount - 1).toFloat())
                }
                last = now
            }
        }
        playing = false
    }

    val particles = remember(field) { Particles(PARTICLES) }
    // Buffers y colores de las estelas, reutilizados en cada fotograma (sin asignar memoria).
    val lines = remember { Array(SPEED_BUCKETS * (TRAIL - 1)) { FloatArray(PARTICLES * 4) } }
    val counts = remember { IntArray(lines.size) }
    val groupColors = remember {
        IntArray(lines.size) { g ->
            val a = 1f - (g % (TRAIL - 1)) / (TRAIL - 1f)
            speedColor((g / (TRAIL - 1) + 0.5f) * SPEED_BUCKET_KMH).copy(alpha = a * 0.9f).toArgb()
        }
    }
    val paint = remember { android.graphics.Paint().apply { isAntiAlias = true; style = android.graphics.Paint.Style.STROKE } }
    var frame by remember { mutableLongStateOf(0L) }
    LaunchedEffect(field, windOn, animate) {
        if (!windOn || !animate) return@LaunchedEffect
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) particles.step(field, hourValue, ((now - last) / 1e9f).coerceAtMost(0.05f))
                last = now
                frame = now
            }
        }
    }

    // Por debajo del 25% de cobertura no se pinta nada: el cielo despejado deja ver el mapa.
    // Cada hora se pinta una sola vez: al cruzar de una a otra, la siguiente ya estaba hecha
    // y solo hay que preparar una nueva, en vez de rehacer cuatro mapas de golpe.
    val cloudCache = remember(field) { HashMap<Int, androidx.compose.ui.graphics.ImageBitmap>() }
    val rainCache = remember(field) { HashMap<Int, androidx.compose.ui.graphics.ImageBitmap>() }
    val cloudImage = cloudCache.getOrPut(hour) { cloudFieldImage(field, hour) }
    val cloudNext = cloudCache.getOrPut(nextHour) { cloudFieldImage(field, nextHour) }
    val rainImage = rainCache.getOrPut(hour) { fieldImage(field, field.rain[hour]) { rampArgb(it, RAIN_STOPS) } }
    val rainNext = rainCache.getOrPut(nextHour) { fieldImage(field, field.rain[nextHour]) { rampArgb(it, RAIN_STOPS) } }

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

                val dst = IntSize(w.toInt(), h.toInt())
                // Entre dos horas se funden las capas de las dos. Se lee aquí, en el dibujo.
                val frac = if (nextHour == hour) 0f else (hourValue - hour).coerceIn(0f, 1f)
                // De noche, un velo azul oscuro sobre tierra y relieve; las nubes y la costa se apagan.
                val night = field.night[hour] * (1f - frac) + field.night[nextHour] * frac
                if (night > 0f) drawRect(NIGHT_TINT.copy(alpha = NIGHT_TINT_ALPHA * night))
                if (cloudsOn) {
                    drawImage(cloudImage, dstSize = dst, alpha = (1f - frac) * (1f - NIGHT_CLOUD_DIM * night), filterQuality = FilterQuality.High)
                    if (frac > 0f) drawImage(cloudNext, dstSize = dst, alpha = frac * (1f - NIGHT_CLOUD_DIM * night), filterQuality = FilterQuality.High)
                }

                if (rainOn) {
                    drawImage(rainImage, dstSize = dst, alpha = 1f - frac, filterQuality = FilterQuality.High)
                    if (frac > 0f) drawImage(rainNext, dstSize = dst, alpha = frac, filterQuality = FilterQuality.High)
                }

                layers?.let { drawPath(it.coast, Color.White.copy(alpha = 0.55f - 0.25f * night), style = Stroke(width = 1.2.dp.toPx())) }
            }

            if (windOn) {
                Canvas(Modifier.fillMaxSize().graphicsLayer { }) {
                    val w = size.width
                    val h = size.height
                    @Suppress("UNUSED_VARIABLE") val tick = frame // suscribe el dibujo a cada fotograma
                    // Miles de líneas sueltas por fotograma eran el cuello de botella: se agrupan
                    // por (velocidad, tramo de la estela) y cada grupo se pinta de una vez.
                    counts.fill(0)
                    for (i in 0 until particles.n) {
                        val fade = minOf(1f, particles.age[i] / 12f, (MAX_AGE - particles.age[i]) / 12f)
                        if (fade < 0.3f) continue // recién nacida o a punto de morir: casi invisible
                        val bucket = (particles.speed[i] / SPEED_BUCKET_KMH).toInt().coerceIn(0, SPEED_BUCKETS - 1)
                        val b = i * TRAIL
                        for (j in 0 until TRAIL - 1) {
                            val g = bucket * (TRAIL - 1) + j
                            val arr = lines[g]
                            val k = counts[g]
                            arr[k] = particles.tx[b + j] * w
                            arr[k + 1] = particles.ty[b + j] * h
                            arr[k + 2] = particles.tx[b + j + 1] * w
                            arr[k + 3] = particles.ty[b + j + 1] * h
                            counts[g] = k + 4
                        }
                    }
                    paint.strokeWidth = 2.dp.toPx()
                    drawIntoCanvas { canvas ->
                        for (g in lines.indices) {
                            if (counts[g] == 0) continue
                            paint.color = groupColors[g]
                            canvas.nativeCanvas.drawLines(lines[g], 0, counts[g], paint)
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

        TimeControls(
            times = field.times,
            hourValue = { hourValue },
            onHourValue = { hourValue = it },
            playing = playing,
            onPlaying = { playing = it },
            nowValue = nowValue,
            night = field.night
        )
        Text(
            text = stringResource(R.string.map_credits),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp
        )
    }
}

private fun formatHour(iso: String, extraMinutes: Int = 0): String =
    LocalDateTime.parse(iso).plusMinutes(extraMinutes.toLong()).format(DateTimeFormatter.ofPattern("EEE HH:mm"))

/** Colores del deslizador: los mismos del fondo del gráfico de "Próximas horas" (amanecer a noche). */
private val SLIDER_COLORS = listOf(Color(0xFFFF7F50), Color(0xFFFFD166), Color(0xFF06D6A0), Color(0xFF118AB2))

/**
 * Play, "Ahora", la hora que se ve y el deslizador. `hourValue` llega como lambda y se lee
 * aquí dentro: así, mientras suena la reproducción, solo se recompone esto y no el mapa.
 */
@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
private fun TimeControls(
    times: List<String>,
    hourValue: () -> Float,
    onHourValue: (Float) -> Unit,
    playing: Boolean,
    onPlaying: (Boolean) -> Unit,
    nowValue: Float,
    night: FloatArray
) {
    val last = (times.size - 1).toFloat()
    val value = hourValue()
    val hour = value.toInt().coerceIn(0, times.size - 1)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val playLabel = stringResource(if (playing) R.string.map_pause else R.string.map_play)
        // Al final de todo, el play vuelve a empezar desde el principio.
        TimeButton(if (playing) "⏸" else "▶", playLabel) {
            if (!playing && value >= last) onHourValue(0f)
            onPlaying(!playing)
        }
        TimeButton("⟲ " + stringResource(R.string.today_now_label), stringResource(R.string.today_now_label)) {
            onPlaying(false)
            onHourValue(nowValue)
        }
        Text(
            text = formatHour(times[hour], ((value - hour) * 2).toInt().coerceIn(0, 1) * 30),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
    Slider(
        value = value,
        // Tocar el deslizador toma el mando: se para la reproducción.
        onValueChange = { onPlaying(false); onHourValue(it) },
        valueRange = 0f..last,
        steps = (times.size - 1) * 2 - 1,
        // Pista con el degradado de la app y un pulgar blanco con sombra, en vez de la barra gris por defecto.
        track = { state ->
            val fraction = (state.value / last).coerceIn(0f, 1f)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .drawBehind {
                        val brush = Brush.horizontalGradient(SLIDER_COLORS)
                        drawRect(brush, alpha = 0.28f)
                        clipRect(right = size.width * fraction) { drawRect(brush) }
                    }
            )
        },
        // El indicador es el astro de la hora que marca: un sol que gira de día y una luna de
        // noche, según el amanecer y el atardecer reales del lugar (campo `is_day`).
        thumb = { state ->
            val v = state.value
            val h = v.toInt().coerceIn(0, times.size - 1)
            val day = night[h] == 0f
            Box(
                Modifier
                    .size(36.dp)
                    .shadow(8.dp, CircleShape)
                    .background(if (day) Color(0xFFFFE9A8) else Color(0xFF26335C), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (day) "☀️" else "🌙",
                    fontSize = 20.sp,
                    modifier = Modifier.graphicsLayer { rotationZ = if (day) v * 45f else 0f }
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

/** Botón redondeado de los controles de tiempo del mapa. */
@Composable
private fun TimeButton(text: String, description: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 14.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.22f))
            .clickable(onClick = onClick)
            .semantics { contentDescription = description }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

/** Media hora, el paso con que avanza la hora del mapa. */
private const val HALF_HOUR = 0.5f

/** Las estelas se agrupan en 8 tramos de velocidad de 12 km/h cada uno (azul a rojo). */
private const val SPEED_BUCKETS = 8
private const val SPEED_BUCKET_KMH = 12f

/** Velocidad de la reproducción: 0,5 h por segundo, media hora cada segundo. */
private const val PLAY_HOURS_PER_SECOND = 0.5f

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
private fun cloudFieldImage(field: WindField, hour: Int) = fieldImage(field, field.cloud[hour]) { c ->
    val a = ((c - 25f) / 75f).coerceIn(0f, 1f)
    ((Math.pow(a.toDouble(), 1.2) * 0.6 * 255).toInt() shl 24) or 0xE8EEF7
}

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

    /** [hourPos] son horas con decimales: el viento se mezcla entre la hora y la siguiente. */
    fun step(f: WindField, hourPos: Float, dt: Float) {
        val hour = hourPos.toInt().coerceIn(0, f.times.size - 1)
        val next = (hour + 1).coerceAtMost(f.times.size - 1)
        val t = if (next == hour) 0f else (hourPos - hour).coerceIn(0f, 1f)
        for (i in 0 until n) {
            val u = f.sample(f.u[hour], x[i], y[i]) * (1 - t) + f.sample(f.u[next], x[i], y[i]) * t
            val v = f.sample(f.v[hour], x[i], y[i]) * (1 - t) + f.sample(f.v[next], x[i], y[i]) * t
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
