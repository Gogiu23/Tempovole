package com.example.tiempo.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.snapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tiempo.BuildConfig
import com.example.tiempo.UnsplashConfig
import com.example.tiempo.data.AirQualityRepository
import com.example.tiempo.data.ClimateChangeRepository
import com.example.tiempo.data.EnsembleRepository
import com.example.tiempo.data.ExtraFeature
import com.example.tiempo.data.FeaturePreferences
import com.example.tiempo.data.FloodRepository
import com.example.tiempo.data.GeocodingRepository
import com.example.tiempo.data.GeocodingResult
import com.example.tiempo.data.HistoricalWeatherRepository
import com.example.tiempo.data.LocationRepository
import com.example.tiempo.data.MarineRepository
import com.example.tiempo.data.SavedLocation
import com.example.tiempo.data.UnsplashRepository
import com.example.tiempo.data.WidgetBackground
import com.example.tiempo.data.WidgetColor
import com.example.tiempo.data.WidgetPreferences
import com.example.tiempo.data.model.BackgroundPhoto
import com.example.tiempo.data.model.CurrentWeather
import com.example.tiempo.data.model.DayWeather
import com.example.tiempo.data.model.HourWeather
import com.example.tiempo.data.model.WeatherCondition
import com.example.tiempo.data.model.moonPhaseInfo
import com.example.tiempo.notifications.NotificationPreferences
import com.example.tiempo.notifications.NotificationScheduler
import com.example.tiempo.widget.WidgetUpdater
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val esLocale = Locale("es", "ES")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Degradado para que el contenido se desvanezca en el borde inferior al hacer scroll. */
private val bottomFadeBrush = Brush.verticalGradient(
    0f to Color.Black,
    0.88f to Color.Black,
    1f to Color.Transparent
)

/** Velo sobre la foto de la tarjeta: sin el, el texto blanco se pierde en las fotos claras. */
private val coverScrim = Brush.verticalGradient(
    listOf(
        Color.Black.copy(alpha = 0.45f),
        Color.Black.copy(alpha = 0.15f),
        Color.Black.copy(alpha = 0.6f)
    )
)

/** Mascara del reflejo del cover flow: opaco pegado a la tarjeta, transparente al alejarse. */
private val reflectionBrush = Brush.verticalGradient(
    0f to Color.Black.copy(alpha = 0.85f),
    0.4f to Color.Black.copy(alpha = 0.3f),
    1f to Color.Transparent
)

/** Degradado diagonal claro-oscuro para el efecto 3D/relieve de las tarjetas de detalle. */
private val tileGradient = Brush.linearGradient(
    listOf(Color.White.copy(alpha = 0.32f), Color.White.copy(alpha = 0.06f))
)


/** Aplica [brush] como máscara de transparencia sobre el contenido ya dibujado. */
private fun Modifier.fadingEdge(brush: Brush): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }

@Composable
private fun LiveClock() {
    var time by remember { mutableStateOf(LocalTime.now().format(timeFormatter)) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L - (System.currentTimeMillis() % 60_000L))
            time = LocalTime.now().format(timeFormatter)
        }
    }
    Text(
        text = time,
        color = Color.White.copy(alpha = 0.85f),
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var overlayIndex by rememberSaveable { mutableIntStateOf(0) }
    val overlay = OverlayScreen.entries[overlayIndex]

    val appContext = LocalContext.current.applicationContext
    var currentLocation by remember { mutableStateOf(LocationRepository.get(appContext)) }
    var notificationHour by remember { mutableStateOf(NotificationPreferences.getHour(appContext)) }
    var notificationMinute by remember { mutableStateOf(NotificationPreferences.getMinute(appContext)) }
    var hasUnseenChanges by remember { mutableStateOf(ChangelogPreferences.hasUnseenChanges(appContext)) }

    LaunchedEffect(currentLocation) {
        viewModel.load(currentLocation.lat, currentLocation.lon)
    }

    // Fondo de Unsplash (null si no hay API key -> degradado). Hay una foto distinta
    // para la mañana, la tarde y la noche, cada una fijada para todo el día; si la app
    // sigue abierta cuando cambia la franja, el fondo se actualiza solo.
    val bgUrl by produceState<String?>(initialValue = null) {
        val repository = UnsplashRepository(appContext)
        var lastPeriod: UnsplashConfig.DayPeriod? = null
        while (true) {
            val period = UnsplashConfig.DayPeriod.forTime()
            if (period != lastPeriod) {
                lastPeriod = period
                value = repository.backgroundPhoto()?.url
            }
            delay(60_000L - (System.currentTimeMillis() % 60_000L))
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (bgUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(bgUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF2B5876), Color(0xFF4E4376)))
                    )
            )
        }

        // Capa oscura para que el texto se lea siempre.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0x55000000), Color(0xAA000000)))
                )
        )

        when (overlay) {
            OverlayScreen.SETTINGS -> SettingsScreen(
                currentLocation = currentLocation,
                onLocationSelected = { location ->
                    LocationRepository.save(appContext, location)
                    currentLocation = location
                    WidgetUpdater.requestUpdate(appContext)
                },
                notificationHour = notificationHour,
                notificationMinute = notificationMinute,
                onNotificationTimeChanged = { hour, minute ->
                    NotificationPreferences.setHour(appContext, hour)
                    NotificationPreferences.setMinute(appContext, minute)
                    notificationHour = hour
                    notificationMinute = minute
                    NotificationScheduler.scheduleDaily(appContext, hour = hour, minute = minute)
                },
                onBack = { overlayIndex = OverlayScreen.NONE.ordinal }
            )

            OverlayScreen.BLOG -> BlogScreen(onBack = { overlayIndex = OverlayScreen.NONE.ordinal })

            OverlayScreen.INFO -> InfoScreen(onBack = { overlayIndex = OverlayScreen.NONE.ordinal })

            OverlayScreen.CHANGELOG -> ChangelogScreen(
                onBack = { overlayIndex = OverlayScreen.NONE.ordinal }
            )

            OverlayScreen.NONE -> {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    bottomBar = {
                        if (state is WeatherUiState.Success) {
                            WeatherBottomBar(selected = selectedTab, onSelect = { selectedTab = it })
                        }
                    }
                ) { innerPadding ->
                    WeatherContent(state, selectedTab, currentLocation, innerPadding)
                }

                // Un unico boton arriba: el resto de accesos viven en el menu lateral.
                var menuOpen by remember { mutableStateOf(false) }

                // Capa oscura tras el menu: tocarla lo cierra.
                AnimatedVisibility(
                    visible = menuOpen,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.55f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { menuOpen = false }
                            )
                    )
                }

                AnimatedVisibility(
                    visible = menuOpen,
                    enter = slideInHorizontally(initialOffsetX = { it }),
                    exit = slideOutHorizontally(targetOffsetX = { it }),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    SideMenu(
                        hasUnseenChanges = hasUnseenChanges,
                        onSelect = { screen ->
                            menuOpen = false
                            if (screen == OverlayScreen.CHANGELOG) {
                                ChangelogPreferences.markSeen(appContext)
                                hasUnseenChanges = false
                            }
                            overlayIndex = screen.ordinal
                        }
                    )
                }

                // Va el ultimo para quedar por encima del panel: el mismo boton abre y cierra.
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    IconCircleButton(
                        emoji = if (menuOpen) "✕" else "☰",
                        showBadge = hasUnseenChanges && !menuOpen,
                        onClick = { menuOpen = !menuOpen }
                    )
                }
            }
        }
    }
}

private enum class OverlayScreen { NONE, SETTINGS, BLOG, INFO, CHANGELOG }

/**
 * Menu lateral con los accesos que antes estaban sueltos arriba: ahora llevan etiqueta,
 * que es lo que gana el sitio al no tener que caber cuatro iconos en una esquina.
 */
@Composable
private fun SideMenu(hasUnseenChanges: Boolean, onSelect: (OverlayScreen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(220.dp)
            .background(Color(0xF01A1C20))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(vertical = 20.dp)
    ) {
        Text(
            text = "Menú",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
        )
        Spacer(Modifier.height(4.dp))
        SideMenuItem("🔔", "Novedades", badge = hasUnseenChanges) {
            onSelect(OverlayScreen.CHANGELOG)
        }
        SideMenuItem("📰", "Blog") { onSelect(OverlayScreen.BLOG) }
        SideMenuItem("ℹ️", "Info") { onSelect(OverlayScreen.INFO) }
        SideMenuItem("⚙️", "Ajustes") { onSelect(OverlayScreen.SETTINGS) }
    }
}

@Composable
private fun SideMenuItem(
    emoji: String,
    label: String,
    badge: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Text(text = label, color = Color.White, fontSize = 17.sp)
        if (badge) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5252))
            )
        }
    }
}

@Composable
fun IconCircleButton(emoji: String, showBadge: Boolean = false, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.TopEnd) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 20.sp)
        }
        if (showBadge) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp, end = 2.dp)
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5252))
                    .border(1.5.dp, Color.White.copy(alpha = 0.9f), CircleShape)
            )
        }
    }
}

/** Pantalla de novedades: qué ha cambiado en cada versión de la app. */
@Composable
private fun ChangelogScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconCircleButton(emoji = "←", onClick = onBack)
            Text(
                text = "🔔 Novedades",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Si la versión instalada no tiene entrada, se avisa en vez de marcar como
        // "Actual" una versión antigua: así el despiste se ve enseguida al abrir la pantalla.
        if (!Changelog.isInSync) {
            Text(
                text = "⚠️ Estás usando la versión ${BuildConfig.VERSION_NAME}, que todavía no " +
                    "tiene entrada en este historial.",
                color = Color(0xFFFFD54A),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }

        Changelog.entries.forEach { entry ->
            val isCurrent = Changelog.isCurrent(entry.version)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = if (isCurrent) 0.22f else 0.14f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Versión ${entry.version}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF4CAF50).copy(alpha = 0.85f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(text = "Actual", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                    Text(
                        text = entry.date,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    entry.changes.forEach { change ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "•",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                            Text(
                                text = change,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BlogScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconCircleButton(emoji = "←", onClick = onBack)
            Text(
                text = "📰 Blog",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Todavía no hay blog. Próximamente encontrarás aquí artículos sobre " +
                "el tiempo y las novedades de la app.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 15.sp
        )
    }
}

@Composable
private fun InfoScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconCircleButton(emoji = "←", onClick = onBack)
            Text(
                text = "ℹ️ Info",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Tiempo",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Versión ${BuildConfig.VERSION_NAME}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
            Text(
                text = "App de previsión meteorológica. Datos de Open-Meteo, " +
                    "fondos de Unsplash.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }

        LegalSection(
            title = "Términos de uso",
            body = "Tiempo se ofrece \"tal cual\", con fines informativos, sin garantía de " +
                "disponibilidad ni de exactitud de los datos mostrados.\n\n" +
                "Los datos meteorológicos proceden de Open-Meteo y pueden contener errores " +
                "o retrasos; no uses esta app como única fuente para decisiones que " +
                "afecten a tu seguridad (por ejemplo, alertas por fenómenos graves) — " +
                "consulta siempre fuentes oficiales para eso.\n\n" +
                "No está permitido un uso que dañe, sobrecargue o comprometa la app o " +
                "los servicios de terceros de los que depende (Open-Meteo, Unsplash).\n\n" +
                "Estos términos pueden actualizarse; el uso continuado de la app tras un " +
                "cambio implica su aceptación."
        )

        PhotoCreditsSection()

        LegalSection(
            title = "Política de privacidad",
            body = "Tiempo no requiere registro ni cuenta, y no recoge datos personales " +
                "identificables.\n\n" +
                "Ubicación: la app no usa el GPS del dispositivo. Por defecto muestra " +
                "Barcelona; si buscas otra ciudad en Ajustes, el texto que escribes se " +
                "envía al geocodificador de Open-Meteo para obtener sus coordenadas, que " +
                "se guardan solo en tu dispositivo.\n\n" +
                "Datos meteorológicos: para mostrar la previsión y los datos adicionales " +
                "que actives en Ajustes (calidad del aire y polen, comparación histórica, " +
                "oleaje, ríos, cambio climático, incertidumbre del modelo), las " +
                "coordenadas de la ubicación elegida se envían a los distintos servicios " +
                "gratuitos de Open-Meteo. No se envía ningún otro dato.\n\n" +
                "Fotos: si hay una clave de Unsplash configurada, la app pide a Unsplash " +
                "(api.unsplash.com) la foto de fondo de cada franja del día y las fotos de " +
                "las tarjetas de la semana, y le avisa de qué fotos usa, como exigen sus " +
                "normas. Solo se envía el término de búsqueda (por ejemplo \"rain\"), " +
                "nunca datos personales.\n\n" +
                "Notificación diaria y widget: se generan en el propio dispositivo y " +
                "consultan los mismos servicios de Open-Meteo (y Unsplash, para el fondo " +
                "del widget) descritos arriba para mostrar el tiempo actualizado.\n\n" +
                "La app no usa cuentas, cookies, identificadores de publicidad ni " +
                "herramientas de analítica o rastreo, y no comparte datos con terceros " +
                "más allá de las llamadas a Open-Meteo y Unsplash descritas arriba.\n\n" +
                "Todos los ajustes (ubicación, hora de notificación, datos activados, " +
                "etc.) se guardan solo en el dispositivo y se borran al desinstalar la " +
                "app.\n\n" +
                "Contacto: giuliandominici@gmail.com"
        )

        val uriHandler = LocalUriHandler.current
        Text(
            text = "Ver esta política en el navegador",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 14.sp,
            modifier = Modifier.clickable { uriHandler.openUri(PRIVACY_POLICY_URL) }
        )
    }
}

/**
 * Créditos de las fotos de fondo. Las normas de la API de Unsplash exigen dar crédito
 * al autor de cada foto usada y enlazar a su perfil, así que se listan aquí los autores
 * de las fotos que la app ha mostrado.
 */
@Composable
private fun PhotoCreditsSection() {
    val context = LocalContext.current.applicationContext
    val credits = remember { UnsplashRepository.credits(context) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Créditos de las imágenes",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Las fotos proceden de Unsplash y se usan según su licencia. Cada foto " +
                "es obra de su autor: la app muestra una de fondo por la mañana, la tarde " +
                "y la noche, y otra en cada tarjeta de la semana según el tiempo que hará " +
                "ese día. Abajo están los autores de las fotos que la app usa ahora mismo; " +
                "toca cualquiera para ver su perfil.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
        if (credits.isEmpty()) {
            Text(
                text = "Todavía no se ha mostrado ninguna foto: aquí aparecerán sus autores " +
                    "en cuanto se cargue la primera.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        } else {
            val uriHandler = LocalUriHandler.current
            credits.forEach { credit ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    val authorLink = credit.authorUrl?.withUnsplashUtm()
                    Text(
                        text = "· Foto de ${credit.authorName}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        modifier = if (authorLink != null) {
                            Modifier.clickable { uriHandler.openUri(authorLink) }
                        } else {
                            Modifier
                        }
                    )
                    credit.photoUrl?.withUnsplashUtm()?.let { photoLink ->
                        Text(
                            text = "   ver la foto en Unsplash",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { uriHandler.openUri(photoLink) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegalSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = body,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun SettingsScreen(
    currentLocation: SavedLocation,
    onLocationSelected: (SavedLocation) -> Unit,
    notificationHour: Int,
    notificationMinute: Int,
    onNotificationTimeChanged: (Int, Int) -> Unit,
    onBack: () -> Unit
) {
    var showLocationSearch by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showLocationSearch) {
        LocationSearchScreen(
            onBack = { showLocationSearch = false },
            onLocationSelected = { location ->
                onLocationSelected(location)
                showLocationSearch = false
            }
        )
        return
    }

    if (showTimePicker) {
        NotificationTimePickerDialog(
            initialHour = notificationHour,
            initialMinute = notificationMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                onNotificationTimeChanged(hour, minute)
                showTimePicker = false
            }
        )
    }

    val context = LocalContext.current.applicationContext

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconCircleButton(emoji = "←", onClick = onBack)
            Text(
                text = "Ajustes",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        SettingsSection(title = "App") {
            SettingsRow(
                label = "📍 Ubicación",
                value = currentLocation.name,
                onClick = { showLocationSearch = true }
            )
            SettingsRow(
                label = "🔔 Morning report",
                value = "%02d:%02d".format(notificationHour, notificationMinute),
                onClick = { showTimePicker = true }
            )
        }

        SettingsSection(title = "Datos adicionales") {
            ExtraFeature.entries.forEach { feature ->
                var checked by remember { mutableStateOf(FeaturePreferences.isEnabled(context, feature)) }
                ExpandableFeatureCard(
                    feature = feature,
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                        FeaturePreferences.setEnabled(context, feature, it)
                    }
                )
            }
        }

        SettingsSection(title = "Widget") {
            var widgetBackground by remember { mutableStateOf(WidgetPreferences.getBackground(context)) }
            var widgetColor by remember { mutableStateOf(WidgetPreferences.getColor(context)) }

            WidgetBackground.entries.forEach { option ->
                WidgetBackgroundOption(
                    option = option,
                    selected = widgetBackground == option,
                    onSelect = {
                        widgetBackground = option
                        WidgetPreferences.setBackground(context, option)
                        WidgetUpdater.requestUpdate(context)
                    }
                )
            }

            if (widgetBackground == WidgetBackground.COLOR) {
                WidgetColorPicker(
                    selected = widgetColor,
                    onSelect = { color ->
                        widgetColor = color
                        WidgetPreferences.setColor(context, color)
                        WidgetUpdater.requestUpdate(context)
                    }
                )
            }
        }
    }
}

/** Una de las tres opciones de fondo del widget, con su descripción y marca de selección. */
@Composable
private fun WidgetBackgroundOption(
    option: WidgetBackground,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = if (selected) 0.28f else 0.16f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = option.label,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
                Text(
                    text = option.description,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            if (selected) {
                Text(text = "✓", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

/** Muestrario de colores sólidos para el fondo del widget. */
@Composable
private fun WidgetColorPicker(selected: WidgetColor, onSelect: (WidgetColor) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = selected.label,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WidgetColor.entries.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(color.argb))
                            .border(
                                width = if (color == selected) 3.dp else 1.dp,
                                color = if (color == selected) Color.White
                                else Color.White.copy(alpha = 0.35f),
                                shape = CircleShape
                            )
                            .clickable { onSelect(color) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandableFeatureCard(
    feature: ExtraFeature,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.16f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${feature.emoji} ${feature.label}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Switch(
                        checked = checked,
                        onCheckedChange = onCheckedChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF9CD5FF),
                            checkedBorderColor = Color(0xFF9CD5FF),
                            uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                            uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                    Text(
                        text = if (expanded) "▲" else "▼",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    text = feature.description,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
                )
            }
        }
    }
}

@Composable
private fun NotificationTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var hour by remember { mutableIntStateOf(initialHour) }
    var minute by remember { mutableIntStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hora del morning report") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeStepper(
                    value = "%02d".format(hour),
                    onIncrement = { hour = (hour + 1) % 24 },
                    onDecrement = { hour = (hour + 23) % 24 }
                )
                Text(
                    text = ":",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                TimeStepper(
                    value = "%02d".format(minute),
                    onIncrement = { minute = (minute + 1) % 60 },
                    onDecrement = { minute = (minute + 59) % 60 }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(hour, minute) }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun TimeStepper(value: String, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        StepperButton(symbol = "▲", onClick = onIncrement)
        Text(
            text = value,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(64.dp),
            textAlign = TextAlign.Center
        )
        StepperButton(symbol = "▼", onClick = onDecrement)
    }
}

@Composable
private fun StepperButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.08f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = symbol, fontSize = 16.sp)
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title.uppercase(),
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        content()
    }
}

@Composable
private fun SettingsRow(label: String, value: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.16f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = value,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 15.sp
                )
                Text(text = "›", color = Color.White.copy(alpha = 0.5f), fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun LocationSearchScreen(
    onBack: () -> Unit,
    onLocationSelected: (SavedLocation) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<GeocodingResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchFailed by remember { mutableStateOf(false) }

    LaunchedEffect(query) {
        val term = query.trim()
        if (term.length < 2) {
            results = emptyList()
            isSearching = false
            searchFailed = false
            return@LaunchedEffect
        }
        isSearching = true
        searchFailed = false
        delay(400) // debounce: espera a que el usuario deje de escribir
        val found = runCatching { GeocodingRepository().search(term) }.getOrNull()
        if (found == null) {
            searchFailed = true
            results = emptyList()
        } else {
            results = found
        }
        isSearching = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconCircleButton(emoji = "←", onClick = onBack)
            Text(
                text = "Ubicación",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Busca una ciudad…", color = Color.White.copy(alpha = 0.6f)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedContainerColor = Color.White.copy(alpha = 0.16f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.16f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        when {
            isSearching -> Box(Modifier.fillMaxWidth(), Alignment.Center) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.padding(24.dp))
            }

            searchFailed -> Text(
                text = "No se pudo buscar. Revisa tu conexión.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )

            query.trim().length >= 2 && results.isEmpty() -> Text(
                text = "Sin resultados para \"$query\".",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )

            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(results) { result ->
                    SettingsRow(
                        label = "📍 ${result.displayName}",
                        value = "",
                        onClick = {
                            onLocationSelected(
                                SavedLocation(
                                    name = result.name,
                                    lat = result.latitude,
                                    lon = result.longitude
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherBottomBar(selected: Int, onSelect: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color.Black.copy(alpha = 0.35f),
        contentColor = Color.White
    ) {
        val colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.White,
            selectedTextColor = Color.White,
            unselectedIconColor = Color.White.copy(alpha = 0.6f),
            unselectedTextColor = Color.White.copy(alpha = 0.6f),
            indicatorColor = Color.White.copy(alpha = 0.18f)
        )
        NavigationBarItem(
            selected = selected == 0,
            onClick = { onSelect(0) },
            icon = { Text("☀️", fontSize = 20.sp) },
            label = { Text("Hoy") },
            colors = colors
        )
        NavigationBarItem(
            selected = selected == 1,
            onClick = { onSelect(1) },
            icon = { Text("📅", fontSize = 20.sp) },
            label = { Text("Semana") },
            colors = colors
        )
    }
}

@Composable
private fun WeatherContent(
    state: WeatherUiState,
    selectedTab: Int,
    location: SavedLocation,
    padding: PaddingValues
) {
    when (state) {
        WeatherUiState.Loading -> Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }

        is WeatherUiState.Error -> Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            Alignment.Center
        ) {
            Text(
                text = "No se pudo cargar el tiempo:\n${state.message}",
                color = Color.White,
                modifier = Modifier.padding(24.dp)
            )
        }

        is WeatherUiState.Success -> {
            val today = state.days.firstOrNull()
            when {
                selectedTab == 0 && today != null ->
                    TodayScreen(today, state.current, state.hours, state.elevationM, location, padding)
                else -> WeekScreen(state.days, state.hours, location.name, padding)
            }
        }
    }
}

/** Alto del header de "Hoy" con la lista arriba del todo y una vez encogido. */
private val HEADER_MAX_HEIGHT = 104.dp
private val HEADER_MIN_HEIGHT = 58.dp

/** Cuanto hay que bajar para que el header termine de encoger. */
private val HEADER_SHRINK_DISTANCE = 150.dp

/** A que se queda el nombre de la ciudad al encoger (equivale a pasar de 30sp a 20sp). */
private const val HEADER_TITLE_SHRINK = 0.67f

/**
 * Header de "Hoy", fijo arriba: segun bajas, el nombre de la ciudad se hace mas pequeno,
 * la linea de resumen se desvanece y aparece una sombra debajo, como en el demo
 * https://scroll-driven-animations.style/demos/shrinking-header-shadow/css/
 */
@Composable
private fun TodayHeader(
    locationName: String,
    summary: String,
    shrink: () -> Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            // Fondo y sombra se pintan a mano: cambiar el tamano de un Box en cada
            // fotograma obligaria a recolocar la pantalla entera mientras haces scroll.
            .drawBehind {
                val progress = shrink()
                if (progress <= 0f) return@drawBehind

                val maxPx = HEADER_MAX_HEIGHT.toPx()
                val minPx = HEADER_MIN_HEIGHT.toPx()
                // Lo que sobra por encima del header es la barra de estado.
                val statusBar = size.height - maxPx
                val headerHeight = statusBar + (maxPx * (1f - progress) + minPx * progress)

                drawRect(
                    color = Color.Black.copy(alpha = 0.82f * progress),
                    size = Size(size.width, headerHeight)
                )
                // La `box-shadow` del demo.
                val shadow = 16.dp.toPx()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.4f * progress), Color.Transparent),
                        startY = headerHeight,
                        endY = headerHeight + shadow
                    ),
                    topLeft = Offset(0f, headerHeight),
                    size = Size(size.width, shadow)
                )
            }
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .height(HEADER_MAX_HEIGHT)
                // A la derecha, sitio para el boton del menu.
                .padding(start = 16.dp, end = 74.dp)
                .graphicsLayer {
                    // Al encoger el header, el contenido sube para seguir centrado en el.
                    val progress = shrink()
                    val lost = (HEADER_MAX_HEIGHT - HEADER_MIN_HEIGHT).toPx() * progress
                    translationY = -lost / 2f
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.graphicsLayer {
                    // El nombre se hace pequeno escalandolo, no cambiando su tamano de
                    // letra: asi no hay que volver a medir el texto en cada fotograma.
                    val scale = lerp(1f, HEADER_TITLE_SHRINK, shrink())
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0f, 0.5f)
                }
            ) {
                Text(
                    text = "📍 $locationName",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = summary,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp,
                    maxLines = 1,
                    modifier = Modifier.graphicsLayer { alpha = 1f - shrink() }
                )
            }
            LiveClock()
        }
    }
}

/**
 * Progreso de entrada (0..1) del elemento con esta `key`, equivalente al rango
 * `entry 0% -> entry 80%` de las animaciones CSS guiadas por scroll: vale 0 justo cuando
 * el borde superior del elemento asoma por la parte de abajo de la pantalla y llega a 1
 * cuando ya ha entrado el 80 % de su altura.
 */
private fun LazyListState.entryProgress(key: Any, rangeFraction: Float = 0.8f): Float {
    val info = layoutInfo
    val item = info.visibleItemsInfo.firstOrNull { it.key == key } ?: return 1f
    if (item.size <= 0) return 1f

    val bottomEdge = (info.viewportEndOffset - info.afterContentPadding).toFloat()
    val travel = item.size * rangeFraction
    val progress = ((bottomEdge - item.offset) / travel).coerceIn(0f, 1f)

    // Al final de la lista ya no queda scroll con el que terminar de revelar los ultimos
    // elementos, asi que el revelado se completa con el recorrido que todavia resta:
    // al tocar fondo estan siempre al 100 %, y sin saltos por el camino.
    val last = info.visibleItemsInfo.last()
    val floor = if (last.index == info.totalItemsCount - 1) {
        val remaining = ((last.offset + last.size) - bottomEdge).coerceAtLeast(0f)
        (1f - remaining / travel).coerceIn(0f, 1f)
    } else {
        0f
    }
    return maxOf(progress, floor)
}

/**
 * Revela la tarjeta segun entra en pantalla, igual que las tarjetas de
 * scroll-driven-animations.style: cae desde media altura por arriba mientras se funde, y
 * un barrido diagonal la va descubriendo desde la esquina inferior izquierda.
 *
 * Equivale a este CSS de la web (`animation-timeline: view()`), pero atado al estado de
 * la LazyColumn, asi que se reproduce hacia delante y hacia atras con el dedo:
 *
 *     from { opacity: 0; translate: 0 -50% 0; clip-path: inset(100% 100% 0% 0%) }
 *     to   { opacity: 1;                      clip-path: inset(0% 0% 0% 0%) }
 */
private fun Modifier.appearOnScroll(listState: LazyListState, key: Any): Modifier = this
    .graphicsLayer {
        val progress = listState.entryProgress(key)
        alpha = progress
        translationY = -size.height * 0.5f * (1f - progress)
    }
    .drawWithContent {
        val progress = listState.entryProgress(key)
        if (progress >= 1f) {
            drawContent()
        } else {
            clipRect(
                left = 0f,
                top = size.height * (1f - progress),
                right = size.width * progress,
                bottom = size.height
            ) {
                this@drawWithContent.drawContent()
            }
        }
    }

@Composable
private fun TodayScreen(
    today: DayWeather,
    current: CurrentWeather,
    hours: List<HourWeather>,
    elevationM: Double,
    location: SavedLocation,
    padding: PaddingValues
) {
    var selectedDetail by remember { mutableStateOf<DetailItem?>(null) }
    val listState = rememberLazyListState()

    // Se calcula aquí arriba (no dentro de ExtrasSection) para que sobreviva a la
    // navegación a una página de detalle y no vuelva a pedir todo por red al volver.
    val context = LocalContext.current.applicationContext
    val enabledExtras = remember { ExtraFeature.entries.filter { FeaturePreferences.isEnabled(context, it) } }
    val extraItems = remember(location) { mutableStateListOf<DetailItem>() }

    LaunchedEffect(location, enabledExtras) {
        extraItems.clear()
        extraItems.addAll(loadExtraItems(location, current, elevationM, enabledExtras))
    }

    selectedDetail?.let { detail ->
        DetailInfoScreen(
            type = detail.type,
            value = detail.value,
            onBack = { selectedDetail = null }
        )
        return
    }

    val upcomingHours = hours.upcoming()

    val detailItems = buildList {
        add(DetailItem("🌡️", "Máxima", "${today.tempMax.roundToInt()}°", DetailType.MAX_TEMP))
        add(DetailItem("🌡️", "Mínima", "${today.tempMin.roundToInt()}°", DetailType.MIN_TEMP))
        add(DetailItem("💨", "Viento", "${today.windKmh.roundToInt()} km/h", DetailType.WIND))
        add(
            DetailItem(
                "🌧️", "Precipitación", "${today.precipitationMm} mm",
                DetailType.PRECIPITATION
            )
        )
        today.precipProbability?.let {
            add(DetailItem("☔", "Prob. lluvia", "$it%", DetailType.RAIN_PROBABILITY))
        }
    }
    val detailRows = detailItems.chunked(2)
    val extraRows = extraItems.chunked(2)

    // El header deja de ser un elemento mas de la lista: se queda fijo arriba y encoge
    // segun bajas, como el demo shrinking-header-shadow de scroll-driven-animations.style.
    val shrinkPx = with(LocalDensity.current) { HEADER_SHRINK_DISTANCE.toPx() }
    // Sin `by`: el valor se lee dentro de las lambdas de dibujo del header, asi que el
    // scroll no dispara recomposiciones ni vuelve a medir el texto en cada fotograma.
    val shrink = remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (listState.firstVisibleItemScrollOffset / shrinkPx).coerceIn(0f, 1f)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .fadingEdge(bottomFadeBrush),
            // Hueco fijo para el header: el contenido no baila mientras el header encoge.
            contentPadding = PaddingValues(top = HEADER_MAX_HEIGHT, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(key = "condition") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .appearOnScroll(listState, "condition"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = today.condition.emoji, fontSize = 64.sp)
                    Text(
                        text = today.condition.label,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${today.tempMax.roundToInt()}° / ${today.tempMin.roundToInt()}°",
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (upcomingHours.isNotEmpty()) {
                item(key = "chart") {
                    var selectedMetricIndex by rememberSaveable { mutableIntStateOf(0) }
                    val selectedMetric = HourlyMetric.entries[selectedMetricIndex]

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .appearOnScroll(listState, "chart"),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Próximas horas",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        MetricSelector(
                            selected = selectedMetric,
                            onSelect = { selectedMetricIndex = it.ordinal }
                        )
                        HourlyLineChart(upcomingHours, selectedMetric)
                    }
                }
            }

            itemsIndexed(detailRows, key = { i, _ -> "detail-row-$i" }) { rowIndex, rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .appearOnScroll(listState, "detail-row-$rowIndex"),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEachIndexed { colIndex, item ->
                        DetailTile(
                            item,
                            modifier = Modifier.weight(1f),
                            entryIndex = rowIndex * 2 + colIndex,
                            onClick = { selectedDetail = item }
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            if (ExtraFeature.SUN_MOON in enabledExtras) {
                item(key = "sun-moon") {
                    SunMoonCard(
                        today = today,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .appearOnScroll(listState, "sun-moon")
                    )
                }
            }

            if (extraRows.isNotEmpty()) {
                item(key = "extras-title") {
                    Text(
                        text = "Más datos",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .appearOnScroll(listState, "extras-title")
                    )
                }
                itemsIndexed(extraRows, key = { i, _ -> "extra-row-$i" }) { rowIndex, rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .appearOnScroll(listState, "extra-row-$rowIndex"),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEachIndexed { colIndex, item ->
                            DetailTile(
                                item,
                                modifier = Modifier.weight(1f),
                                entryIndex = rowIndex * 2 + colIndex,
                                onClick = { selectedDetail = item }
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        TodayHeader(
            locationName = location.name,
            summary = "${current.condition.emoji} ${current.temp.roundToInt()}°  ·  " +
                today.date.fullDate(),
            shrink = shrink::value,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}

/** Pide en paralelo (una petición de red simultánea por cada dato activado en Ajustes) los datos extra de Open-Meteo. */
private suspend fun loadExtraItems(
    location: SavedLocation,
    current: CurrentWeather,
    elevationM: Double,
    enabled: List<ExtraFeature>
): List<DetailItem> = coroutineScope {
    val pollenDeferred = if (ExtraFeature.POLLEN in enabled) {
        async { runCatching { AirQualityRepository().get(location.lat, location.lon) }.getOrNull() }
    } else null
    val historicalDeferred = if (ExtraFeature.HISTORICAL in enabled) {
        async {
            runCatching {
                HistoricalWeatherRepository().compareToday(location.lat, location.lon, current.temp)
            }.getOrNull()
        }
    } else null
    val marineDeferred = if (ExtraFeature.MARINE in enabled) {
        async { runCatching { MarineRepository().get(location.lat, location.lon) }.getOrNull() }
    } else null
    val floodDeferred = if (ExtraFeature.FLOOD in enabled) {
        async { runCatching { FloodRepository().getRiverDischarge(location.lat, location.lon) }.getOrNull() }
    } else null
    val climateDeferred = if (ExtraFeature.CLIMATE in enabled) {
        async { runCatching { ClimateChangeRepository().getProjection(location.lat, location.lon) }.getOrNull() }
    } else null
    val ensembleDeferred = if (ExtraFeature.ENSEMBLE in enabled) {
        async { runCatching { EnsembleRepository().getCurrentUncertainty(location.lat, location.lon) }.getOrNull() }
    } else null

    buildList {
        if (ExtraFeature.ATMOSPHERIC in enabled) {
            current.uvIndex?.let {
                add(DetailItem("☀️", "Índice UV", "%.1f".format(it), DetailType.UV_INDEX))
            }
            current.solarRadiation?.let {
                add(DetailItem("🔆", "Radiación solar", "${it.roundToInt()} W/m²", DetailType.SOLAR_RADIATION))
            }
            current.dewPoint?.let {
                add(DetailItem("💧", "Punto de rocío", "${it.roundToInt()}°", DetailType.DEW_POINT))
            }
            current.pressureHpa?.let {
                add(DetailItem("🧭", "Presión", "${it.roundToInt()} hPa", DetailType.PRESSURE))
            }
        }
        if (ExtraFeature.ELEVATION in enabled) {
            add(DetailItem("⛰️", "Altitud", "${elevationM.roundToInt()} m", DetailType.ELEVATION))
        }
        pollenDeferred?.await()?.dominantPollen?.let { (type, level) ->
            add(DetailItem("🌾", type, level, DetailType.POLLEN))
        }
        historicalDeferred?.await()?.let {
            val sign = if (it.diffFromToday >= 0) "+" else ""
            add(
                DetailItem(
                    "📊", "Vs. media histórica", "$sign${it.diffFromToday.roundToInt()}°",
                    DetailType.HISTORICAL
                )
            )
        }
        marineDeferred?.await()?.let {
            add(DetailItem("🌊", "Oleaje", "${it.waveHeightM} m", DetailType.MARINE))
        }
        floodDeferred?.await()?.let {
            add(DetailItem("🏞️", "Caudal del río", "${it.roundToInt()} m³/s", DetailType.FLOOD))
        }
        climateDeferred?.await()?.let {
            val sign = if (it.deltaC >= 0) "+" else ""
            add(
                DetailItem(
                    "🌍", "Clima en ${it.projectedYear}", "$sign${it.deltaC.roundToInt()}°",
                    DetailType.CLIMATE
                )
            )
        }
        ensembleDeferred?.await()?.let {
            add(
                DetailItem(
                    "🎯",
                    "Rango de modelos",
                    "${it.minTemp.roundToInt()}°–${it.maxTemp.roundToInt()}°",
                    DetailType.ENSEMBLE
                )
            )
        }
    }
}

private enum class HourlyMetric(val label: String, val emoji: String, val unit: String) {
    RAIN("Lluvia", "🌧️", "%"),
    TEMP("Temperatura", "🌡️", "°"),
    WIND("Viento", "💨", "")
}

/**
 * Color con el que se tine el fondo del grafico, segun la hora en la que empieza: sigue el
 * tono de la luz a lo largo del dia — coral por la mañana, dorado al mediodia, verde por
 * la tarde y azul de noche.
 */
private fun chartTintFor(hour: Int): Color = when (hour) {
    in 6..11 -> Color(0xFFFF7F50)
    in 12..15 -> Color(0xFFFFD166)
    in 16..20 -> Color(0xFF06D6A0)
    else -> Color(0xFF118AB2)
}

private fun HourWeather.valueFor(metric: HourlyMetric): Float = when (metric) {
    HourlyMetric.TEMP -> temp.toFloat()
    HourlyMetric.RAIN -> (precipProbability ?: 0).toFloat()
    HourlyMetric.WIND -> windKmh.toFloat()
}

@Composable
private fun MetricSelector(selected: HourlyMetric, onSelect: (HourlyMetric) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HourlyMetric.entries.forEach { metric ->
            val isSelected = metric == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = if (isSelected) 0.28f else 0.1f))
                    .clickable { onSelect(metric) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${metric.emoji} ${metric.label}",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun HourlyLineChart(hours: List<HourWeather>, metric: HourlyMetric) {
    if (hours.isEmpty()) return

    val pointSpacing = 52.dp
    val chartHeight = 190.dp
    val lineColor = Color.White

    val values = remember(hours, metric) { hours.map { it.valueFor(metric) } }
    val minValue = values.min()
    val maxValue = values.max()
    val range = (maxValue - minValue).let { if (it < 1f) 1f else it }

    val tint = remember(hours.first().time) { chartTintFor(hours.first().time.hour) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                // De abajo arriba: el color de la franja del dia se va apagando en gris.
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF9E9E9E).copy(alpha = 0.18f),
                        tint.copy(alpha = 0.45f)
                    )
                )
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .width(pointSpacing * hours.size)
                    .height(chartHeight)
            ) {
                val stepPx = pointSpacing.toPx()
                val topPad = 56.dp.toPx()
                val bottomPad = 26.dp.toPx()
                val usableHeight = size.height - topPad - bottomPad

                fun yFor(v: Float): Float {
                    val ratio = (v - minValue) / range
                    return topPad + usableHeight * (1f - ratio)
                }

                val points = values.mapIndexed { i, v ->
                    Offset(stepPx * i + stepPx / 2f, yFor(v))
                }

                val fillPath = Path().apply {
                    moveTo(points.first().x, size.height - bottomPad)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, size.height - bottomPad)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0f)),
                        startY = topPad,
                        endY = size.height - bottomPad
                    )
                )

                val linePath = Path().apply {
                    points.forEachIndexed { i, p ->
                        if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                    }
                }
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                val valuePaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 12.sp.toPx()
                    isAntiAlias = true
                    isFakeBoldText = true
                }
                val hourPaint = android.graphics.Paint(valuePaint).apply {
                    isFakeBoldText = false
                    alpha = 190
                    textSize = 11.sp.toPx()
                }
                val iconPaint = android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 16.sp.toPx()
                    isAntiAlias = true
                }

                points.forEachIndexed { i, p ->
                    drawCircle(lineColor, radius = 3.dp.toPx(), center = p)

                    if (metric != HourlyMetric.TEMP) {
                        drawContext.canvas.nativeCanvas.drawText(
                            hours[i].displayEmoji,
                            p.x,
                            p.y - 36.dp.toPx(),
                            iconPaint
                        )
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        "${values[i].roundToInt()}${metric.unit}",
                        p.x,
                        p.y - 14.dp.toPx(),
                        valuePaint
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        hours[i].time.hourLabel(),
                        p.x,
                        size.height - 6.dp.toPx(),
                        hourPaint
                    )
                }
            }
        }
    }
}

/** Las próximas 24 horas a partir de la hora actual (incluida). */
private fun List<HourWeather>.upcoming(): List<HourWeather> {
    val now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
    return filter { !it.time.isBefore(now) }.take(24)
}

private data class DetailItem(
    val emoji: String,
    val label: String,
    val value: String,
    val type: DetailType
)

/**
 * El icono aparece con un pequeño "pop" (escala + fundido) la primera vez que se compone.
 */
@Composable
private fun PoppingIcon(emoji: String, delayMillis: Int = 0) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.3f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(animationSpec = tween(200))
    ) {
        Text(text = emoji, fontSize = 28.sp)
    }
}

@Composable
private fun DetailGrid(items: List<DetailItem>, onClick: (DetailItem) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEachIndexed { rowIndex, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEachIndexed { colIndex, item ->
                    DetailTile(
                        item,
                        modifier = Modifier.weight(1f),
                        entryIndex = rowIndex * 2 + colIndex,
                        onClick = { onClick(item) }
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DetailTile(
    item: DetailItem,
    modifier: Modifier = Modifier,
    entryIndex: Int = 0,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(
                elevation = 14.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.7f)
            )
            .clip(shape)
            .background(Color.White.copy(alpha = 0.12f))
            .background(tileGradient)
            .border(1.dp, Color.White.copy(alpha = 0.38f), shape)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PoppingIcon(emoji = item.emoji, delayMillis = entryIndex * 40)
            Spacer(Modifier.height(6.dp))
            Text(
                text = item.value,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = item.label,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Tarjeta "Sol y luna": arco solar con la posición actual del sol + fase lunar dibujada. */
@Composable
private fun SunMoonCard(today: DayWeather, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Sol y luna",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            SunArc(sunrise = today.sunrise, sunset = today.sunset, moonPhase = today.moonPhase)
            val daylight = Duration.between(today.sunrise, today.sunset)
            val (_, moonLabel) = moonPhaseInfo(today.moonPhase)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MoonPhaseVisual(phaseFraction = today.moonPhase, sizeDp = 52.dp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = moonLabel,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${daylight.toHours()}h ${daylight.toMinutesPart()}m de luz solar",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

/**
 * Arco que representa el recorrido del sol entre el amanecer y el atardecer de hoy (o, si es
 * de noche, el recorrido de la luna entre el atardecer y el próximo amanecer).
 */
@Composable
private fun SunArc(sunrise: LocalDateTime, sunset: LocalDateTime, moonPhase: Double) {
    val now = remember { LocalDateTime.now() }
    val isDaytime = now.isAfter(sunrise) && now.isBefore(sunset)

    val arcStart: LocalDateTime
    val arcEnd: LocalDateTime
    if (isDaytime) {
        arcStart = sunrise
        arcEnd = sunset
    } else if (now.isBefore(sunrise)) {
        arcStart = sunset.minusDays(1)
        arcEnd = sunrise
    } else {
        arcStart = sunset
        arcEnd = sunrise.plusDays(1)
    }
    val totalMinutes = Duration.between(arcStart, arcEnd).toMinutes().coerceAtLeast(1)
    val elapsedMinutes = Duration.between(arcStart, now).toMinutes()
    val progress = (elapsedMinutes.toFloat() / totalMinutes.toFloat()).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
        ) {
            val baselineY = size.height - 2.dp.toPx()
            // Elipse en vez de círculo: el radio horizontal ocupa todo el ancho, pero el
            // vertical se ajusta a la altura disponible para que el arco no se salga del recuadro.
            val horizontalRadius = size.width / 2f
            val verticalRadius = baselineY
            val centerX = size.width / 2f

            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(0f, baselineY),
                end = Offset(size.width, baselineY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
            )

            val arcTopLeft = Offset(centerX - horizontalRadius, baselineY - verticalRadius)
            drawArc(
                color = Color.White.copy(alpha = 0.45f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = androidx.compose.ui.geometry.Size(horizontalRadius * 2f, verticalRadius * 2f),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            val angleRad = Math.toRadians(180.0 + 180.0 * progress)
            val markerX = centerX + horizontalRadius * cos(angleRad).toFloat()
            val markerY = baselineY + verticalRadius * sin(angleRad).toFloat()

            if (isDaytime) {
                drawCircle(
                    color = Color(0xFFFFD54A).copy(alpha = 0.35f),
                    radius = 15.dp.toPx(),
                    center = Offset(markerX, markerY)
                )
                drawCircle(
                    color = Color(0xFFFFD54A),
                    radius = 8.dp.toPx(),
                    center = Offset(markerX, markerY)
                )
            } else {
                drawCircle(
                    color = Color(0xFFCBD3E8).copy(alpha = 0.3f),
                    radius = 14.dp.toPx(),
                    center = Offset(markerX, markerY)
                )
                drawMoonDisc(
                    center = Offset(markerX, markerY),
                    radius = 8.dp.toPx(),
                    phase = moonPhase.toFloat().mod(1f)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "🌅 Amanecer", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text(
                    text = sunrise.format(timeFormatter),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Atardecer 🌇", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text(
                    text = sunset.format(timeFormatter),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/** Dibuja un disco lunar con la porción iluminada real según la fase (0=luna nueva, 0.5=llena). */
private fun DrawScope.drawMoonDisc(center: Offset, radius: Float, phase: Float) {
    val fullCircleRect = Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius)
    drawArc(
        color = Color(0xFF1B2333),
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = true,
        topLeft = fullCircleRect.topLeft,
        size = fullCircleRect.size
    )

    val k = cos(phase * 2f * Math.PI.toFloat())
    val ellipseHalfWidth = radius * abs(k)
    val isRightHalf = phase < 0.5f
    val isCrescent = phase < 0.25f || phase >= 0.75f

    val fullCirclePath = Path().apply { addOval(fullCircleRect) }
    val halfPlaneRect = if (isRightHalf) {
        Rect(center.x, center.y - radius, center.x + radius, center.y + radius)
    } else {
        Rect(center.x - radius, center.y - radius, center.x, center.y + radius)
    }
    val halfPlanePath = Path().apply { addRect(halfPlaneRect) }
    val halfDiscPath = Path().apply { op(fullCirclePath, halfPlanePath, PathOperation.Intersect) }

    val ellipseRect = Rect(
        center.x - ellipseHalfWidth, center.y - radius,
        center.x + ellipseHalfWidth, center.y + radius
    )
    val ellipsePath = Path().apply { addOval(ellipseRect) }

    val litPath = Path().apply {
        if (isCrescent) {
            op(halfDiscPath, ellipsePath, PathOperation.Difference)
        } else {
            op(halfDiscPath, ellipsePath, PathOperation.Union)
        }
    }
    drawPath(litPath, color = Color(0xFFFFF6D9))
}

/** Tarjeta con el disco lunar dibujado a un tamaño mayor, para la fila de resumen. */
@Composable
private fun MoonPhaseVisual(phaseFraction: Double, sizeDp: androidx.compose.ui.unit.Dp) {
    Canvas(modifier = Modifier.size(sizeDp)) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        drawMoonDisc(center = center, radius = r, phase = phaseFraction.toFloat().mod(1f))
    }
}

@Composable
private fun WeekScreen(
    days: List<DayWeather>,
    hours: List<HourWeather>,
    locationName: String,
    padding: PaddingValues
) {
    var selectedDay by remember { mutableStateOf<DayWeather?>(null) }
    val listState = rememberLazyListState()

    // Una foto por tarjeta, elegida segun el tiempo que hara ese dia. Se piden a Unsplash
    // agrupadas por condicion (una sola peticion por condicion, guardadas una semana), asi
    // que ilustrar los 7 dias cuesta un par de peticiones, no una por tarjeta.
    val appContext = LocalContext.current.applicationContext
    val photosByCondition by produceState(
        initialValue = emptyMap<WeatherCondition, List<BackgroundPhoto>>(),
        key1 = days
    ) {
        value = UnsplashRepository(appContext).conditionPhotos(days.map { it.condition })
    }

    // Unsplash pide avisar de cada foto que se usa; el repositorio lo hace una vez al dia.
    LaunchedEffect(photosByCondition, days) {
        if (photosByCondition.isNotEmpty()) {
            val shown = days.mapNotNull { day ->
                UnsplashRepository.photoForDay(
                    photos = photosByCondition[day.condition].orEmpty(),
                    date = day.date
                )
            }
            UnsplashRepository(appContext).trackUsage(shown)
        }
    }

    selectedDay?.let { day ->
        DayDetailScreen(
            day = day,
            hours = hours.forDate(day.date),
            locationName = locationName,
            onBack = { selectedDay = null }
        )
        return
    }

    // La tarjeta que queda mas cerca del centro se dibuja por encima de las demas: es lo
    // que hace que las laterales parezcan apiladas hacia fuera, como en un cover flow.
    val centeredIndex by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val center = (info.viewportStartOffset + info.viewportEndOffset) / 2f
            info.visibleItemsInfo
                .minByOrNull { abs(it.offset + it.size / 2f - center) }
                ?.index ?: 0
        }
    }

    // Al soltar, el carrusel tiene que seguir rodando. El proveedor de snap que
    // trae Compose recorta el recorrido natural del fling en el ancho de una
    // tarjeta, que es lo que hacia que frenase en seco nada mas levantar el dedo:
    // aqui se le deja toda la inercia y el ajuste al centro se hace al final.
    // Menos friccion que el scroll normal de Android: un carrusel de tarjetas
    // grandes con la friccion de serie se para en la tarjeta de al lado.
    val decay = remember { exponentialDecay<Float>(frictionMultiplier = COVER_FLING_FRICTION) }
    val flingBehavior = remember(listState, decay) {
        val byDefault = SnapLayoutInfoProvider(listState, SnapPosition.Center)
        snapFlingBehavior(
            snapLayoutInfoProvider = object : SnapLayoutInfoProvider by byDefault {
                override fun calculateApproachOffset(
                    velocity: Float,
                    decayOffset: Float
                ): Float = decayOffset
            },
            decayAnimationSpec = decay,
            // Asentamiento sin rebote y resuelto: la inercia ya la pone el decay de
            // arriba, asi que un muelle final lento solo alarga la sensacion de no
            // tener el control.
            snapAnimationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    // Una sola zona de scroll: la lista ocupa la pantalla entera y el titulo va dibujado
    // encima. Asi se arrastra desde cualquier punto (los iconos de arriba estan por encima
    // y se quedan con sus gestos; la barra de abajo queda fuera de este `padding`) sin que
    // haya dos scrolls compitiendo por el mismo estado, que es lo que volvia tosco el
    // control cuando habia una animacion en curso.
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            // En horizontal la muesca se come un lateral de la pantalla.
            .displayCutoutPadding()
            .padding(padding)
    ) {
        // En horizontal la altura util se queda en la tercera parte (barra de estado,
        // barra de la app, gestos y muesca se comen ~170dp), asi que alli el carrusel va
        // con el reflejo mas corto y menos ampliacion, o las tarjetas saldrian minusculas.
        val isLandscape = maxWidth > maxHeight
        val reflectionRatio = if (isLandscape) 0.3f else 0.5f
        val zoom = if (isLandscape) COVER_LANDSCAPE_ZOOM else MAX_COVER_ZOOM

        // En vertical manda el ancho de la pantalla; en horizontal, la altura.
        val heightLimit = maxHeight / ((1f + reflectionRatio) * zoom)
        val coverSize = min(min(maxWidth * 0.42f, heightLimit), 168.dp)
        val itemHeight = coverSize * (1f + reflectionRatio) + 2.dp
        // El giro y la ampliacion pivotan sobre el centro de la tarjeta, no sobre el
        // conjunto tarjeta + reflejo.
        val pivotY = (coverSize / 2) / itemHeight

        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = (maxWidth - coverSize) / 2),
            verticalAlignment = Alignment.CenterVertically,
            flingBehavior = flingBehavior
        ) {
            itemsIndexed(days, key = { index, _ -> "day-$index" }) { index, day ->
                val photo = UnsplashRepository.photoForDay(
                    photos = photosByCondition[day.condition].orEmpty(),
                    date = day.date
                )
                // La tarjeta se dibuja una sola vez y queda grabada aqui; el reflejo
                // reaprovecha ese dibujo en lugar de componer y medir otra tarjeta
                // entera con su propia foto.
                val coverLayer = rememberGraphicsLayer()

                Column(
                    modifier = Modifier
                        .zIndex(-abs(index - centeredIndex).toFloat())
                        .width(coverSize)
                        .coverFlowItem(listState, "day-$index", pivotY, zoom, stacked = isLandscape),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DayCover(
                        day = day,
                        size = coverSize,
                        zoom = zoom,
                        photo = photo,
                        onClick = { selectedDay = day },
                        modifier = Modifier.drawWithContent {
                            coverLayer.record { this@drawWithContent.drawContent() }
                            drawLayer(coverLayer)
                        }
                    )
                    Spacer(Modifier.height(2.dp))
                    // El reflejo del cover flow: el mismo dibujo volteado y
                    // desvaneciendose. Hereda las esquinas redondeadas de la tarjeta
                    // porque es literalmente su dibujo, no un recuadro aparte.
                    Canvas(
                        modifier = Modifier
                            .width(coverSize)
                            .height(coverSize * reflectionRatio)
                            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    ) {
                        withTransform({
                            scale(
                                scaleX = 1f,
                                scaleY = -1f,
                                pivot = Offset(size.width / 2f, size.width / 2f)
                            )
                        }) {
                            drawLayer(coverLayer)
                        }
                        drawRect(brush = reflectionBrush, blendMode = BlendMode.DstIn)
                    }
                }
            }
        }

        // Encima del carrusel, pero sin robarle el gesto: es texto, no consume punteros.
        // En horizontal va en una sola linea, que de altura no sobra.
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 16.dp, vertical = if (isLandscape) 2.dp else 10.dp)
        ) {
            Text(
                text = "📍 $locationName",
                color = Color.White,
                fontSize = if (isLandscape) 20.sp else 26.sp,
                fontWeight = FontWeight.Bold
            )
            if (!isLandscape) {
                Text(
                    text = "Previsión de 7 días",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * Coloca cada tarjeta segun lo lejos que esta del centro del carrusel, como el cover flow
 * de https://scroll-driven-animations.style/demos/cover-flow/css/
 *
 * La distancia se mide en anchos de tarjeta ([COVER_FLOW_SPAN]), no en anchos de pantalla,
 * para que el efecto no dependa del tamano de la pantalla.
 *
 * Con [stacked] (la pantalla en horizontal, donde caben muchas mas tarjetas a lo ancho) la
 * tarjeta ademas crece segun se acerca al centro, encoge al salir y se mete un poco por
 * detras de la siguiente. En vertical se queda el efecto del demo: las tarjetas mantienen
 * su tamano y solo la que pasa por el centro se amplia.
 */
private fun Modifier.coverFlowItem(
    listState: LazyListState,
    key: Any,
    pivotY: Float,
    zoom: Float,
    stacked: Boolean
): Modifier = graphicsLayer {
    val info = listState.layoutInfo
    val item = info.visibleItemsInfo.firstOrNull { it.key == key } ?: return@graphicsLayer
    if (item.size <= 0) return@graphicsLayer

    val angle: Float
    val scale: Float
    val shift: Float

    if (stacked) {
        // HORIZONTAL. Caben muchas mas tarjetas a lo ancho, asi que la distancia se mide
        // en anchos de tarjeta y la tarjeta crece segun se acerca al centro, encoge al
        // salir y se mete un poco por detras de la siguiente.
        val itemCenter = item.offset + item.size / 2f
        val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
        val offset = ((itemCenter - viewportCenter) / (item.size * COVER_FLOW_SPAN))
            .coerceIn(-1f, 1f)
        val side = if (offset < 0f) -1f else 1f
        val away = abs(offset)

        // El giro se resuelve enseguida; el tamano y el apilado, por un tramo mas largo.
        angle = 45f * side * smoothstep(away / COVER_TURN_RANGE)
        val depth = smoothstep(away / COVER_DEPTH_RANGE)
        scale = lerp(zoom, COVER_MIN_SCALE, depth)
        // Hacia el centro: es lo que las solapa un poco unas con otras.
        shift = -side * COVER_STACK_SHIFT * depth
    } else {
        // VERTICAL. El recorrido del demo, medido sobre el ancho de la pantalla: la
        // tarjeta entra girada, se pone de frente y se amplia al cruzar el centro, y
        // vuelve a girarse al salir, siempre del mismo tamano.
        val viewportWidth = (info.viewportEndOffset - info.viewportStartOffset).toFloat()
        val travel = viewportWidth + item.size
        if (travel <= 0f) return@graphicsLayer

        // 0 = la tarjeta asoma por el borde derecho, 0.5 = centrada, 1 = sale por la izquierda.
        val progress = ((info.viewportEndOffset - item.offset) / travel).coerceIn(0f, 1f)
        when {
            progress < 0.35f -> {
                shift = lerp(-COVER_WIDE_SHIFT, 0f, smoothstep(progress / 0.35f))
                angle = -45f
                scale = 1f
            }
            progress > 0.65f -> {
                shift = lerp(0f, COVER_WIDE_SHIFT, smoothstep((progress - 0.65f) / 0.35f))
                angle = 45f
                scale = 1f
            }
            else -> {
                // Tramo central: el giro de -45° a +45° y la ampliación. La velocidad se
                // apaga al acercarse a los extremos del tramo, así que enlaza con los
                // otros dos sin el tirón que daba interpolar linealmente por tramos.
                val centered = ((progress - 0.5f) / 0.15f).coerceIn(-1f, 1f)
                val eased = smoothstep(abs(centered))
                shift = 0f
                angle = 45f * centered.sign * eased
                scale = lerp(zoom, 1f, eased)
            }
        }
    }

    transformOrigin = TransformOrigin(0.5f, pivotY)
    translationX = shift * size.width
    rotationY = angle
    scaleX = scale
    scaleY = scale
    // Equivalente al `perspective: 40em` del demo: cuanto mas corta, mas acusado el 3D.
    cameraDistance = 6f
}

/** Version publica de la politica de privacidad, la que pide Google Play. */
private const val PRIVACY_POLICY_URL = "https://gogiu23.github.io/Tempovole/privacidad.html"

/**
 * Nombre con el que la app esta registrada en Unsplash. Sus normas exigen que los enlaces
 * de credito lleven este origen para que el fotografo vea de donde le llegan las visitas.
 */
private const val UNSPLASH_UTM_SOURCE = "Tiempo"

/** Anade a un enlace de Unsplash los parametros de origen que exigen sus normas. */
private fun String.withUnsplashUtm(): String {
    val separator = if ('?' in this) "&" else "?"
    return "$this${separator}utm_source=$UNSPLASH_UTM_SOURCE&utm_medium=referral"
}

/** Acelera y frena en vez de ir a velocidad constante: quita los tirones en los enlaces. */
private fun smoothstep(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    return x * x * (3f - 2f * x)
}

/** Cuanto se amplia la tarjeta al quedar centrada (el `scale(1.5)` del demo, algo contenido). */
private const val MAX_COVER_ZOOM = 1.4f

/**
 * Solo en horizontal: cuanto se amplia la tarjeta centrada. Subirlo NO hace la central mas
 * grande en pantalla (la altura disponible manda y el tamano base baja en proporcion): lo
 * que hace es dejar mas pequenas a las demas, que es de donde sale el contraste.
 */
private const val COVER_LANDSCAPE_ZOOM = 1.45f

/** Solo en horizontal: tamano de las tarjetas que ya se han alejado del centro. */
private const val COVER_MIN_SCALE = 0.45f

/** Solo en horizontal: distancia en la que la tarjeta completa su giro. */
private const val COVER_TURN_RANGE = 0.3f

/**
 * Solo en horizontal: distancia en la que termina de encoger y de apilarse. Al ocupar todo
 * el recorrido, la tarjeta no para de crecer o menguar mientras dura el scroll.
 */
private const val COVER_DEPTH_RANGE = 1f

/** Cuantas tarjetas a cada lado del centro abarca la animacion de giro y apilado. */
private const val COVER_FLOW_SPAN = 2.5f

/**
 * Cuanto se mete cada tarjeta hacia el centro al alejarse (horizontal), en fraccion de su
 * ancho: es lo que las hace solaparse. Va en sentido contrario al scroll, asi que pasarse
 * aqui hace que las laterales parezcan clavadas mientras arrastras.
 */
private const val COVER_STACK_SHIFT = 0.35f

/** Lo mismo en vertical, donde las tarjetas no encogen y hace falta menos recorrido. */
private const val COVER_WIDE_SHIFT = 0.55f

/**
 * Friccion del deslizamiento al soltar el dedo, como fraccion de la del scroll normal de
 * Android (1f). Cuanto mas baja, mas lejos rueda el carrusel: a 1f se para en la tarjeta
 * de al lado y no parece que tenga inercia.
 */
private const val COVER_FLING_FRICTION = 0.6f

@Composable
private fun DayDetailScreen(
    day: DayWeather,
    hours: List<HourWeather>,
    locationName: String,
    onBack: () -> Unit
) {
    var selectedDetail by remember { mutableStateOf<DetailItem?>(null) }
    val scrollState = rememberScrollState()

    selectedDetail?.let { detail ->
        DetailInfoScreen(
            type = detail.type,
            value = detail.value,
            onBack = { selectedDetail = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconCircleButton(emoji = "←", onClick = onBack)
            Column {
                Text(
                    text = "📍 $locationName",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = day.date.fullDate(),
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = day.condition.emoji, fontSize = 64.sp)
            Text(
                text = day.condition.label,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${day.tempMax.roundToInt()}° / ${day.tempMin.roundToInt()}°",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (hours.isNotEmpty()) {
            var selectedMetricIndex by rememberSaveable { mutableIntStateOf(0) }
            val selectedMetric = HourlyMetric.entries[selectedMetricIndex]

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Por horas",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                MetricSelector(
                    selected = selectedMetric,
                    onSelect = { selectedMetricIndex = it.ordinal }
                )
                HourlyLineChart(hours, selectedMetric)
            }
        }

        val (moonEmoji, moonLabel) = moonPhaseInfo(day.moonPhase)
        val detailItems = buildList {
            add(DetailItem("🌡️", "Máxima", "${day.tempMax.roundToInt()}°", DetailType.MAX_TEMP))
            add(DetailItem("🌡️", "Mínima", "${day.tempMin.roundToInt()}°", DetailType.MIN_TEMP))
            add(DetailItem("🌡️", "Media", "${day.tempMean.roundToInt()}°", DetailType.AVG_TEMP))
            add(DetailItem("💨", "Viento", "${day.windKmh.roundToInt()} km/h", DetailType.WIND))
            add(
                DetailItem(
                    "🌧️", "Precipitación", "${day.precipitationMm} mm",
                    DetailType.PRECIPITATION
                )
            )
            day.precipProbability?.let {
                add(DetailItem("☔", "Prob. lluvia", "$it%", DetailType.RAIN_PROBABILITY))
            }
            add(DetailItem("🌅", "Amanecer", day.sunrise.format(timeFormatter), DetailType.SUNRISE))
            add(DetailItem("🌇", "Atardecer", day.sunset.format(timeFormatter), DetailType.SUNSET))
            add(DetailItem(moonEmoji, "Luna", moonLabel, DetailType.MOON_PHASE))
        }
        DetailGrid(detailItems, onClick = { selectedDetail = it })
    }
}

/** Las franjas horarias de un día concreto (00:00 a 23:00). */
private fun List<HourWeather>.forDate(date: LocalDate): List<HourWeather> =
    filter { it.time.toLocalDate() == date }

@Composable
private fun DayCover(
    day: DayWeather,
    size: Dp,
    zoom: Float,
    photo: BackgroundPhoto?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Se pide la foto un poco mas grande que la tarjeta porque la centrada se amplia.
    val density = LocalDensity.current
    val requestPx = with(density) { (size * zoom).roundToPx() }
    val context = LocalContext.current
    // Recordada: si no, cada recomposicion construye una peticion nueva para la misma foto.
    val request = remember(photo?.url, requestPx) {
        photo?.let {
            ImageRequest.Builder(context)
                .data(UnsplashRepository.thumbUrl(it.url, requestPx))
                .crossfade(true)
                .build()
        }
    }

    // Todo va en proporcion al lado de la tarjeta: en horizontal son bastante mas
    // pequenas, y con tamanos fijos la ultima linea se salia por abajo.
    val dayFont = with(density) { (size * 0.091f).toSp() }
    val emojiFont = with(density) { (size * 0.275f).toSp() }
    val tempFont = with(density) { (size * 0.108f).toSp() }
    val detailFont = with(density) { (size * 0.067f).toSp() }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.13f))
            .background(Color.White.copy(alpha = 0.16f))
            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(size * 0.13f))
            .clickable(onClick = onClick)
    ) {
        if (request != null) {
            AsyncImage(
                model = request,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(coverScrim)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(size * 0.085f),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.date.dayName(),
                color = Color.White,
                fontSize = dayFont,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(text = day.condition.emoji, fontSize = emojiFont)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${day.tempMax.roundToInt()}° / ${day.tempMin.roundToInt()}°",
                    color = Color.White,
                    fontSize = tempFont,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "💨 ${day.windKmh.roundToInt()}  ·  🌧️ ${day.precipitationMm}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = detailFont,
                    maxLines = 1
                )
            }
        }
    }
}

private fun LocalDate.dayName(): String =
    dayOfWeek.getDisplayName(TextStyle.FULL, esLocale)
        .replaceFirstChar { it.uppercase() }

private fun LocalDate.fullDate(): String {
    val day = dayOfWeek.getDisplayName(TextStyle.FULL, esLocale)
        .replaceFirstChar { it.uppercase() }
    val monthName = month.getDisplayName(TextStyle.FULL, esLocale)
    return "$day, $dayOfMonth de $monthName"
}

private fun LocalDateTime.hourLabel(): String {
    val now = LocalDateTime.now()
    return if (hour == now.hour && toLocalDate() == now.toLocalDate()) {
        "Ahora"
    } else {
        "%02d:00".format(hour)
    }
}
