package com.example.tiempo.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
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
import com.example.tiempo.data.LanguagePreferences
import com.example.tiempo.data.LocationRepository
import com.example.tiempo.data.MarineRepository
import com.example.tiempo.data.SavedLocation
import com.example.tiempo.data.UnsplashRepository
import com.example.tiempo.data.WidgetBackground
import com.example.tiempo.data.WidgetColor
import com.example.tiempo.data.WidgetPreferences
import com.example.tiempo.data.model.BackgroundPhoto
import com.example.tiempo.data.model.CurrentWeather
import com.example.tiempo.R
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
import kotlin.math.pow
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sin
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
/**
 * Lo que tapa el fondo en el centro de una tarjeta. Es el unico mando del cristal: a 0 la
 * tarjeta desaparece, subiendolo se vuelve mas solida.
 */
private const val TILE_GLASS_ALPHA = 0.14f

/**
 * Fondo de una tarjeta de la rejilla: un unico color que se apaga del centro hacia afuera
 * hasta quedar transparente antes de llegar a las esquinas. Sin canto ni sombra y sin un
 * segundo color, asi no se ve el cuadrado alrededor del texto, solo una mancha difusa.
 * El radio pasa del lado mas largo (0.85) para que el apagado sea lento y no se note el
 * circulo del degradado.
 */
private fun DrawScope.drawTileGlass(alpha: Float = 1f) {
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = TILE_GLASS_ALPHA),
                Color.White.copy(alpha = 0f)
            ),
            center = center,
            radius = size.maxDimension * 0.85f
        ),
        alpha = alpha
    )
}


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

    // El gesto de borde izquierdo (o botón atrás) cierra el overlay activo en lugar de salir.
    BackHandler(enabled = overlay != OverlayScreen.NONE) {
        overlayIndex = OverlayScreen.NONE.ordinal
    }

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

            OverlayScreen.LOCATION -> LocationSearchScreen(
                onBack = { overlayIndex = OverlayScreen.NONE.ordinal },
                onLocationSelected = { location ->
                    LocationRepository.save(appContext, location)
                    currentLocation = location
                    WidgetUpdater.requestUpdate(appContext)
                    overlayIndex = OverlayScreen.NONE.ordinal
                }
            )

            OverlayScreen.BLOG -> BlogScreen(onBack = { overlayIndex = OverlayScreen.NONE.ordinal })

            OverlayScreen.INFO -> InfoScreen(onBack = { overlayIndex = OverlayScreen.NONE.ordinal })

            OverlayScreen.CHANGELOG -> ChangelogScreen(
                onBack = { overlayIndex = OverlayScreen.NONE.ordinal }
            )

            OverlayScreen.LANGUAGE -> LanguageScreen(
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

private enum class OverlayScreen { NONE, SETTINGS, BLOG, INFO, CHANGELOG, LANGUAGE, LOCATION }

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
            text = stringResource(R.string.menu_title),
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
        )
        Spacer(Modifier.height(4.dp))
        SideMenuItem(R.drawable.ic_location, stringResource(R.string.menu_ubicacion)) {
            onSelect(OverlayScreen.LOCATION)
        }
        SideMenuItem(R.drawable.ic_bell, stringResource(R.string.menu_novedades), badge = hasUnseenChanges) {
            onSelect(OverlayScreen.CHANGELOG)
        }
        SideMenuItem(R.drawable.ic_blog, stringResource(R.string.menu_blog)) { onSelect(OverlayScreen.BLOG) }
        SideMenuItem(R.drawable.ic_info, stringResource(R.string.menu_info)) { onSelect(OverlayScreen.INFO) }
        SideMenuItem(R.drawable.ic_settings, stringResource(R.string.menu_ajustes)) { onSelect(OverlayScreen.SETTINGS) }
        SideMenuItem(R.drawable.ic_language, stringResource(R.string.menu_idioma)) { onSelect(OverlayScreen.LANGUAGE) }
    }
}

@Composable
private fun SideMenuItem(
    iconRes: Int,
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
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )
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
            Image(
                painter = painterResource(R.drawable.ic_bell),
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = stringResource(R.string.changelog_screen_title),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Si la versión instalada no tiene entrada, se avisa en vez de marcar como
        // "Actual" una versión antigua: así el despiste se ve enseguida al abrir la pantalla.
        if (!Changelog.isInSync) {
            Text(
                text = stringResource(
                    R.string.changelog_screen_out_of_sync_warning,
                    BuildConfig.VERSION_NAME
                ),
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
                            text = stringResource(R.string.changelog_screen_version_label, entry.version),
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
                                Text(
                                    text = stringResource(R.string.changelog_screen_current_badge),
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    Text(
                        text = stringResource(entry.dateRes),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    entry.changeRes.forEach { changeRes ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "•",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                            Text(
                                text = stringResource(changeRes),
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
            Image(
                painter = painterResource(R.drawable.ic_blog),
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = stringResource(R.string.blog_title),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = stringResource(R.string.blog_placeholder),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 15.sp
        )
    }
}

@Composable
private fun LanguageScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val currentTag = LanguagePreferences.getLanguageTag(context) ?: ""
    val options: List<Triple<String, String?, Int>> = listOf(
        Triple(stringResource(R.string.language_picker_follow_system), null, R.drawable.ic_language),
        Triple(stringResource(R.string.language_picker_es), "es", R.drawable.ic_flag_es),
        Triple(stringResource(R.string.language_picker_en), "en", R.drawable.ic_flag_en),
        Triple(stringResource(R.string.language_picker_ru), "ru", R.drawable.ic_flag_ru),
        Triple(stringResource(R.string.language_picker_it), "it", R.drawable.ic_flag_it)
    )

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
            Image(
                painter = painterResource(R.drawable.ic_language),
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = stringResource(R.string.menu_idioma),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (label, tag, iconRes) ->
                val selected = if (tag == null) currentTag.isEmpty() else currentTag.startsWith(tag)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            LanguagePreferences.setLanguageTag(context, tag)
                            // El widget no es una Activity: aunque el idioma de la app cambie,
                            // su RemoteViews no se repinta solo. Sin esto se queda en el idioma
                            // con el que se dibujó la última vez. delayMillis evita pintarlo justo
                            // antes de que el sistema termine de aplicar el locale nuevo; va en el
                            // scope propio de WidgetUpdater porque en API <33 la Activity se
                            // recrea justo después y cancelaría un scope ligado a la composición.
                            WidgetUpdater.requestUpdate(context, delayMillis = 300)
                            if (Build.VERSION.SDK_INT < 33) {
                                (context as? Activity)?.recreate()
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = if (selected) 0.28f else 0.16f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(iconRes),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                            )
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                        if (selected) {
                            Text(text = "✓", color = Color.White, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
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
            Image(
                painter = painterResource(R.drawable.ic_info),
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = stringResource(R.string.info_title),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.info_app_name_label),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.info_version_label, BuildConfig.VERSION_NAME),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
            Text(
                text = stringResource(R.string.info_description),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }

        LegalSection(
            title = stringResource(R.string.info_terms_title),
            body = listOf(
                stringResource(R.string.info_terms_p1),
                stringResource(R.string.info_terms_p2),
                stringResource(R.string.info_terms_p3),
                stringResource(R.string.info_terms_p4)
            ).joinToString("\n\n")
        )

        PhotoCreditsSection()

        IconCreditsSection()

        LegalSection(
            title = stringResource(R.string.info_privacy_title),
            body = listOf(
                stringResource(R.string.info_privacy_p1),
                stringResource(R.string.info_privacy_p2),
                stringResource(R.string.info_privacy_p3),
                stringResource(R.string.info_privacy_p4),
                stringResource(R.string.info_privacy_p5),
                stringResource(R.string.info_privacy_p6),
                stringResource(R.string.info_privacy_p7),
                stringResource(R.string.info_privacy_p8)
            ).joinToString("\n\n")
        )

        val uriHandler = LocalUriHandler.current
        Text(
            text = stringResource(R.string.info_privacy_policy_link),
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
            text = stringResource(R.string.photo_credits_title),
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.photo_credits_description),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
        if (credits.isEmpty()) {
            Text(
                text = stringResource(R.string.photo_credits_empty),
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
                        text = stringResource(R.string.photo_credits_author_prefix, credit.authorName),
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
                            text = stringResource(R.string.photo_credits_view_on_unsplash),
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
private fun IconCreditsSection() {
    val uriHandler = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.icon_credits_title),
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.icon_credits_description),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
        Text(
            text = stringResource(R.string.icon_credits_flaticon_link),
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
            modifier = Modifier.clickable { uriHandler.openUri("https://www.flaticon.com") }
        )
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
    notificationHour: Int,
    notificationMinute: Int,
    onNotificationTimeChanged: (Int, Int) -> Unit,
    onBack: () -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

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
                text = stringResource(R.string.settings_title),
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        SettingsSection(title = stringResource(R.string.settings_app_section_title)) {
            SettingsRow(
                label = stringResource(R.string.settings_morning_report_label),
                iconRes = R.drawable.ic_bell,
                value = "%02d:%02d".format(notificationHour, notificationMinute),
                onClick = { showTimePicker = true }
            )
        }

        SettingsSection(title = stringResource(R.string.settings_extra_data_label)) {
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

        SettingsSection(title = stringResource(R.string.settings_widget_label)) {
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
                    text = stringResource(option.labelRes),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
                Text(
                    text = stringResource(option.descriptionRes),
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
                text = stringResource(selected.labelRes),
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
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(feature.iconRes()),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(feature.labelRes),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
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
                    text = stringResource(feature.descriptionRes),
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
        title = { Text(stringResource(R.string.time_picker_title)) },
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
            TextButton(onClick = { onConfirm(hour, minute) }) { Text(stringResource(R.string.time_picker_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.time_picker_cancel)) }
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
private fun SettingsRow(
    label: String,
    value: String,
    iconRes: Int? = null,
    onClick: () -> Unit
) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (iconRes != null) {
                    Image(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(text = label, color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp)
            }
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
                text = stringResource(R.string.location_search_title),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.location_search_placeholder), color = Color.White.copy(alpha = 0.6f)) },
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
                text = stringResource(R.string.location_search_network_error),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )

            query.trim().length >= 2 && results.isEmpty() -> Text(
                text = stringResource(R.string.location_search_no_results, query),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )

            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(results) { result ->
                    SettingsRow(
                        label = result.displayName,
                        iconRes = R.drawable.ic_location,
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
            icon = {
                Image(
                    painter = painterResource(R.drawable.ic_today),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text(stringResource(R.string.bottom_bar_today)) },
            colors = colors
        )
        NavigationBarItem(
            selected = selected == 1,
            onClick = { onSelect(1) },
            icon = {
                Image(
                    painter = painterResource(R.drawable.ic_week),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            },
            label = { Text(stringResource(R.string.bottom_bar_week)) },
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
                text = stringResource(R.string.weather_error_prefix, state.message),
                color = Color.White,
                modifier = Modifier.padding(24.dp)
            )
        }

        is WeatherUiState.Success -> {
            val today = state.days.firstOrNull()
            when {
                selectedTab == 0 && today != null ->
                    TodayScreen(
                        today,
                        state.current,
                        state.hours,
                        state.elevationM,
                        location,
                        padding
                    )
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
private fun ShrinkingHeader(
    title: String,
    subtitle: String,
    titleIconRes: Int? = null,
    subtitleIconRes: Int? = null,
    shrink: () -> Float,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
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
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f * progress),
                            Color.Transparent
                        ),
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.graphicsLayer {
                    // El titulo se hace pequeno escalandolo, no cambiando su tamano de
                    // letra: asi no hay que volver a medir el texto en cada fotograma.
                    val scale = lerp(1f, HEADER_TITLE_SHRINK, shrink())
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0f, 0.5f)
                }
            ) {
                leading?.invoke()
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (titleIconRes != null) {
                            Image(
                                painter = painterResource(titleIconRes),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.graphicsLayer { alpha = 1f - shrink() }
                    ) {
                        if (subtitleIconRes != null) {
                            Image(
                                painter = painterResource(subtitleIconRes),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = subtitle,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                    }
                }
            }
            trailing?.invoke()
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
 * Version adaptada de un `ScrollTrigger` de GSAP con `start`/`end` (como los de `main.js` en
 * scroll-driven-animations.style) al hecho de que, aqui, [key] es el UNICO item (y el ultimo)
 * de un `LazyColumn` normal, no una seccion gigante fijada aparte: su recorrido de scroll real
 * va desde que su borde superior toca el borde inferior de la pantalla (recien entrando, `t=0`)
 * hasta que el scroll llega al tope porque su borde inferior ya no puede subir mas (`t=1`) — ese
 * recorrido total equivale exactamente a su propia altura, sea cual sea el tamano de pantalla.
 * En el JS original la seccion truco es mucho mas alta que la pantalla, asi que sus porcentajes
 * se miden en alturas de pantalla; aqui los medimos como fraccion de ese recorrido de `t`, que
 * es lo unico que este item puede ofrecer.
 */
private fun LazyListState.scrollTriggerProgress(
    key: Any,
    startFraction: Float,
    endFraction: Float
): Float {
    val info = layoutInfo
    val item = info.visibleItemsInfo.firstOrNull { it.key == key } ?: return 0f
    val viewportHeight = (info.viewportEndOffset - info.viewportStartOffset).toFloat()
    val itemHeight = item.size.toFloat()
    if (viewportHeight <= 0f || itemHeight <= 0f) return 0f

    val t = ((viewportHeight - item.offset) / itemHeight).coerceIn(0f, 1f)
    // El principio y el final de la ventana pueden llegar a coincidir (por ejemplo antes
    // del primer layout, cuando todavia no se sabe el alto del viewport): una ventana de
    // ancho 0 dividiria por cero y devolveria NaN, que revienta mas tarde en un
    // roundToInt(). Sin ventana, no hay nada que animar.
    val span = endFraction - startFraction
    if (span <= 0f) return 0f
    return ((t - startFraction) / span).coerceIn(0f, 1f)
}

/**
 * `poweri.inOut` de GSAP: quad/cubic/quart/quint segun [power] (1 a 4), simetrica y con
 * aceleracion-freno en el punto medio. `power` mas alto = se queda mas tiempo cerca de los
 * extremos y acelera mas de golpe por el medio.
 */
private fun easeInOutPower(power: Int, t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    val n = (power + 1).toDouble()
    return if (x < 0.5f) {
        (0.5 * (2.0 * x).pow(n)).toFloat()
    } else {
        (1.0 - 0.5 * (2.0 * (1f - x)).pow(n)).toFloat()
    }
}

/** `sine.out` de GSAP: arranca rapido y llega suave al final. */
private fun easeSineOut(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    return sin(x * (Math.PI / 2.0)).toFloat()
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

/**
 * true mientras el centro del elemento [key] cae en la franja central de la pantalla.
 * Sirve para no gastar la animacion de un icono con la tarjeta asomando por el borde:
 * se reproduce al llegar al medio, que es donde se esta mirando.
 */
@Composable
private fun LazyListState.isCenteredInViewport(key: Any, bandFraction: Float = 0.2f): Boolean {
    val state = this
    return remember(state, key) {
        derivedStateOf {
            val info = state.layoutInfo
            val item = info.visibleItemsInfo.firstOrNull { it.key == key }
                ?: return@derivedStateOf false
            val viewportHeight = (info.viewportEndOffset - info.viewportStartOffset).toFloat()
            if (viewportHeight <= 0f) return@derivedStateOf false
            val viewportCenter = info.viewportStartOffset + viewportHeight / 2f
            val itemCenter = item.offset + item.size / 2f
            abs(itemCenter - viewportCenter) <= viewportHeight * bandFraction
        }
    }.value
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
    val isDay = remember {
        val now = LocalTime.now()
        now.isAfter(today.sunrise.toLocalTime()) && now.isBefore(today.sunset.toLocalTime())
    }

    // Se calcula aquí arriba (no dentro de ExtrasSection) para que sobreviva a la
    // navegación a una página de detalle y no vuelva a pedir todo por red al volver.
    val context = LocalContext.current.applicationContext
    val enabledExtras = remember { ExtraFeature.entries.filter { FeaturePreferences.isEnabled(context, it) } }
    val extraItems = remember(location) { mutableStateListOf<DetailItem>() }

    LaunchedEffect(location, enabledExtras) {
        extraItems.clear()
        extraItems.addAll(loadExtraItems(location, current, elevationM, enabledExtras, context))
    }

    // Foto de la ubicacion actual (no la ambiental de fondo): la tarjeta protagonista de
    // la rejilla la usa mientras es grande. Se vuelve a pedir si se cambia de ubicacion.
    val heroPhotoUrl by produceState<String?>(initialValue = null, location) {
        value = UnsplashRepository(context).locationPhoto(location.name)?.url
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

    val maxLabel = stringResource(R.string.today_max_label)
    val minLabel = stringResource(R.string.today_min_label)
    val windLabel = stringResource(DetailType.WIND.titleRes)
    val precipitationLabel = stringResource(DetailType.PRECIPITATION.titleRes)
    val rainProbabilityLabel = stringResource(R.string.today_rain_probability_label)
    val detailItems = buildList {
        add(DetailItem("🌡️", maxLabel, "${today.tempMax.roundToInt()}°", DetailType.MAX_TEMP))
        add(DetailItem("🌡️", minLabel, "${today.tempMin.roundToInt()}°", DetailType.MIN_TEMP))
        add(DetailItem("💨", windLabel, "${today.windKmh.roundToInt()} km/h", DetailType.WIND))
        add(
            DetailItem(
                "🌧️", precipitationLabel, "${today.precipitationMm} mm",
                DetailType.PRECIPITATION
            )
        )
        today.precipProbability?.let {
            add(DetailItem("☔", rainProbabilityLabel, "$it%", DetailType.RAIN_PROBABILITY))
        }
    }

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
                    WeatherIcon(today.condition, isDay = true, size = 80.dp)
                    Text(
                        text = stringResource(today.condition.labelRes),
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
                            .padding(horizontal = HOURLY_CHART_SIDE_PADDING)
                            .appearOnScroll(listState, "chart"),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.today_next_hours_title),
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

            if (ExtraFeature.SUN_MOON in enabledExtras) {
                item(key = "sun-moon") {
                    SunMoonCard(
                        today = today,
                        animateAstro = listState.isCenteredInViewport("sun-moon"),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .revealOnScroll(listState, "sun-moon")
                    )
                }
            }

            // Las 15 tarjetas juntas en una sola rejilla de 3 columnas.
            item(key = "detail-tiles") {
                AppGridReveal(
                    items = (detailItems + extraItems).withHeroAtCenter(DetailType.RAIN_PROBABILITY),
                    listState = listState,
                    itemKey = "detail-tiles",
                    heroPhotoUrl = heroPhotoUrl,
                    heroLocationName = location.name,
                    onClick = { selectedDetail = it }
                )
            }
        }

        ShrinkingHeader(
            title = location.name,
            titleIconRes = R.drawable.ic_location,
            subtitleIconRes = current.condition.staticIcon(isDay),
            subtitle = "${current.temp.roundToInt()}°  ·  ${today.date.fullDate(stringResource(R.string.date_full_format))}",
            shrink = shrink::value,
            trailing = { LiveClock() },
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}

/** Pide en paralelo (una petición de red simultánea por cada dato activado en Ajustes) los datos extra de Open-Meteo. */
private suspend fun loadExtraItems(
    location: SavedLocation,
    current: CurrentWeather,
    elevationM: Double,
    enabled: List<ExtraFeature>,
    context: android.content.Context
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
                add(DetailItem("☀️", context.getString(DetailType.UV_INDEX.titleRes), "%.1f".format(it), DetailType.UV_INDEX))
            }
            current.solarRadiation?.let {
                add(DetailItem("🔆", context.getString(DetailType.SOLAR_RADIATION.titleRes), "${it.roundToInt()} W/m²", DetailType.SOLAR_RADIATION))
            }
            current.dewPoint?.let {
                add(DetailItem("💧", context.getString(DetailType.DEW_POINT.titleRes), "${it.roundToInt()}°", DetailType.DEW_POINT))
            }
            current.pressureHpa?.let {
                add(DetailItem("", context.getString(R.string.today_pressure_label), "${it.roundToInt()} hPa", DetailType.PRESSURE))
            }
        }
        if (ExtraFeature.ELEVATION in enabled) {
            add(DetailItem("⛰️", context.getString(DetailType.ELEVATION.titleRes), "${elevationM.roundToInt()} m", DetailType.ELEVATION))
        }
        pollenDeferred?.await()?.dominantPollen?.let { (typeRes, levelRes) ->
            add(DetailItem("🌾", context.getString(typeRes), context.getString(levelRes), DetailType.POLLEN))
        }
        historicalDeferred?.await()?.let {
            val sign = if (it.diffFromToday >= 0) "+" else ""
            add(
                DetailItem(
                    "📊", context.getString(R.string.today_historical_label), "$sign${it.diffFromToday.roundToInt()}°",
                    DetailType.HISTORICAL
                )
            )
        }
        marineDeferred?.await()?.let {
            add(DetailItem("🌊", context.getString(DetailType.MARINE.titleRes), "${it.waveHeightM} m", DetailType.MARINE))
        }
        floodDeferred?.await()?.let {
            add(DetailItem("🏞️", context.getString(DetailType.FLOOD.titleRes), "${it.roundToInt()} m³/s", DetailType.FLOOD))
        }
        climateDeferred?.await()?.let {
            val sign = if (it.deltaC >= 0) "+" else ""
            add(
                DetailItem(
                    "🌍", context.getString(R.string.today_climate_label, it.projectedYear), "$sign${it.deltaC.roundToInt()}°",
                    DetailType.CLIMATE
                )
            )
        }
        ensembleDeferred?.await()?.let {
            add(
                DetailItem(
                    "🎯",
                    context.getString(R.string.today_ensemble_label),
                    "${it.minTemp.roundToInt()}°–${it.maxTemp.roundToInt()}°",
                    DetailType.ENSEMBLE
                )
            )
        }
    }
}

private enum class HourlyMetric(
    @androidx.annotation.StringRes val labelRes: Int,
    val unit: String,
    val iconRes: Int
) {
    RAIN(R.string.today_metric_rain_label, "%", R.drawable.ic_heavy_rain),
    TEMP(R.string.today_metric_temp_label, "°", R.drawable.ic_temp_medium),
    WIND(DetailType.WIND.titleRes, "", R.drawable.ic_wind)
}

/**
 * Momento del dia en el que cada color manda del todo, y su tono. Son los centros de las
 * franjas: madrugada (hasta las 9), dia (hasta las 18), tarde (hasta las 22) y noche.
 */
private val CHART_TINT_ANCHORS = listOf(
    4.5f to Color(0xFFFF7F50),
    13.5f to Color(0xFFFFD166),
    20f to Color(0xFF06D6A0),
    23f to Color(0xFF118AB2)
)

/**
 * Color con el que se tine el fondo del grafico a una hora dada. Entre franja y franja el
 * color se mezcla poco a poco en vez de saltar de golpe, y a medianoche enlaza con el
 * primer tono, asi que el recorrido de un dia entero no tiene ningun corte.
 */
private fun chartTintFor(hour: Int): Color {
    val anchors = CHART_TINT_ANCHORS
    anchors.forEachIndexed { index, (start, startColor) ->
        val (nextHour, endColor) = anchors[(index + 1) % anchors.size]
        val end = if (nextHour > start) nextHour else nextHour + 24f
        val position = if (hour >= start) hour.toFloat() else hour + 24f
        if (position in start..end) {
            return androidx.compose.ui.graphics.lerp(
                startColor,
                endColor,
                (position - start) / (end - start)
            )
        }
    }
    return anchors.first().second
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Image(
                        painter = painterResource(metric.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(metric.labelRes),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

/** Alto de la tarjeta de "Próximas horas": la protagonista de la rejilla arranca grande
 * con este mismo tamano (alto y margen lateral), no con uno propio inventado. */
private val HOURLY_CHART_HEIGHT = 190.dp

/** Margen lateral de la tarjeta de "Próximas horas" respecto a los bordes de la pantalla. */
private val HOURLY_CHART_SIDE_PADDING = 16.dp

@Composable
private fun HourlyLineChart(hours: List<HourWeather>, metric: HourlyMetric) {
    if (hours.isEmpty()) return

    val pointSpacing = 52.dp
    val chartSidePadding = 0.dp
    val chartHeight = HOURLY_CHART_HEIGHT
    val lineColor = Color.White

    val values = remember(hours, metric) { hours.map { it.valueFor(metric) } }
    val minValue = values.min()
    val maxValue = values.max()
    val range = (maxValue - minValue).let { if (it < 1f) 1f else it }

    // Un color por hora: el fondo recorre las franjas del dia de izquierda a derecha.
    val tints = remember(hours) {
        hours.mapIndexed { index, hour ->
            (index + 0.5f) / hours.size to chartTintFor(hour.time.hour).copy(alpha = 0.45f)
        }.toTypedArray()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.10f))
    ) {
        // Los iconos del grafico se resuelven aqui fuera: imageResource es @Composable y
        // dentro del Canvas ya no se puede llamar. Uno por condicion distinta, no por hora.
        // Clave (condicion, es de dia): a las 22:00 con claros toca luna, no sol.
        val iconosPorHora = hours.map { it.condition to it.isDay }.distinct().associateWith {
            (cond, esDeDia) -> ImageBitmap.imageResource(cond.staticIcon(esDeDia))
        }
        // Mismo motivo que los iconos de arriba: stringResource es @Composable.
        val nowLabel = stringResource(R.string.today_now_label)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            Canvas(
                modifier = Modifier
                    // El ancho extra es el aire de los lados: el fondo sigue llegando a
                    // los bordes de la tarjeta, pero las horas ya no quedan pegadas.
                    .width(pointSpacing * hours.size + chartSidePadding * 2)
                    .height(chartHeight)
            ) {
                // Fondo: el color de cada franja horaria a lo ancho, desvaneciendose
                // hacia arriba hasta quedar en un gris translucido que deja ver la foto.
                //
                // El desvanecido se hace en una capa aparte: se pinta el color y luego se
                // le recorta la opacidad con un degradado vertical (DstIn), porque un
                // Brush por si solo no puede variar de color a lo ancho y de opacidad a
                // lo alto a la vez.
                drawContext.canvas.saveLayer(
                    Rect(0f, 0f, size.width, size.height),
                    Paint()
                )
                if (tints.size > 1) {
                    drawRect(brush = Brush.horizontalGradient(colorStops = tints))
                } else {
                    drawRect(color = tints.first().second)
                }
                drawRect(
                    brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)),
                    blendMode = BlendMode.DstIn
                )
                drawContext.canvas.restore()

                // Y encima, el gris que domina la parte de arriba.
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF9E9E9E).copy(alpha = 0.22f), Color.Transparent)
                    )
                )

                val stepPx = pointSpacing.toPx()
                val sidePad = chartSidePadding.toPx()
                val topPad = 56.dp.toPx()
                val bottomPad = 44.dp.toPx()
                val usableHeight = size.height - topPad - bottomPad

                fun yFor(v: Float): Float {
                    val ratio = (v - minValue) / range
                    return topPad + usableHeight * (1f - ratio)
                }

                val points = values.mapIndexed { i, v ->
                    Offset(sidePad + stepPx * i + stepPx / 2f, yFor(v))
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
                val rainPaint = android.graphics.Paint(valuePaint).apply {
                    color = android.graphics.Color.parseColor("#7EC8E3")
                    isFakeBoldText = false
                    textSize = 9.sp.toPx()
                }
                val iconPaint = android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 16.sp.toPx()
                    isAntiAlias = true
                }

                // Altura máxima de las barras de lluvia (en la zona del bottomPad)
                val rainBarMaxH = 18.dp.toPx()
                val rainBarWidth = (stepPx * 0.35f).coerceAtMost(10.dp.toPx())
                val maxPrecip = hours.maxOfOrNull { it.precipitationMm.toFloat() }?.coerceAtLeast(1f) ?: 1f
                val rainBarBaseY = size.height - 18.dp.toPx() // justo encima de la hora

                points.forEachIndexed { i, p ->
                    drawCircle(lineColor, radius = 3.dp.toPx(), center = p)

                    // Barra de precipitación en la zona inferior
                    val precip = hours[i].precipitationMm.toFloat()
                    if (precip > 0f) {
                        val barH = (precip / maxPrecip) * rainBarMaxH
                        drawRect(
                            color = Color(0xFF7EC8E3).copy(alpha = 0.75f),
                            topLeft = Offset(p.x - rainBarWidth / 2f, rainBarBaseY - barH),
                            size = androidx.compose.ui.geometry.Size(rainBarWidth, barH)
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            "%.1f".format(precip),
                            p.x,
                            rainBarBaseY - barH - 2.dp.toPx(),
                            rainPaint
                        )
                    }

                    if (metric != HourlyMetric.TEMP) {
                        val icono =
                            iconosPorHora.getValue(hours[i].condition to hours[i].isDay)
                        val lado = 22.dp.toPx()
                        val centroY = p.y - 42.dp.toPx()
                        drawImage(
                            image = icono,
                            dstOffset = IntOffset(
                                (p.x - lado / 2f).toInt(),
                                (centroY - lado / 2f).toInt()
                            ),
                            dstSize = IntSize(lado.toInt(), lado.toInt())
                        )
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        "${values[i].roundToInt()}${metric.unit}",
                        p.x,
                        p.y - 14.dp.toPx(),
                        valuePaint
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        hours[i].time.hourLabel(nowLabel),
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
/** Esquinas de la tarjeta, en proporcion a su lado (50px sobre 200px en el original). */
private val TILE_CORNER_FRACTION = androidx.compose.foundation.shape.CornerSize(25)

/**
 * El fondo con el que arranca la tarjeta protagonista, con los mismos colores que el
 * fondo del grafico por horas (coral, dorado, verde, azul), aqui todos juntos en un solo
 * degradado en vez de repartidos por franjas horarias.
 */
private val HERO_INTRO_GRADIENT = Brush.linearGradient(
    CHART_TINT_ANCHORS.map { (_, color) -> color }
)


/** Ancho de celda para el que estan pensados los tamanos de letra de la tarjeta. */
private val DETAIL_TILE_REFERENCE = 170.dp

/** Proporcion de la tarjeta: 4:5, como las imagenes del demo. */
private const val DETAIL_TILE_ASPECT = 0.8f

@Composable
private fun PoppingIcon(
    emoji: String,
    iconRes: Int? = null,
    delayMillis: Int = 0,
    size: androidx.compose.ui.unit.TextUnit = 28.sp
) {
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
        if (iconRes != null) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(with(LocalDensity.current) { size.toDp() })
            )
        } else {
            Text(text = emoji, fontSize = size)
        }
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

/**
 * La rejilla aparece inspirada en el pen https://codepen.io/jh3y/pen/VYZwOwd, adaptada a un
 * `LazyColumn` normal (sin bloque artificial ni `position: sticky`):
 *
 *  - El grupo se coloca justo despues de Sol y luna, como cualquier otro elemento de la
 *    lista. La protagonista arranca con el mismo tamano que la tarjeta de "Próximas
 *    horas" (paisaje, mas ancha que alta): de fondo lleva [heroPhotoUrl] (una foto del
 *    lugar) y sin el icono ni el texto del dato todavia.
 *  - Mientras es grande va pegada a Sol y luna, como otra tarjeta mas de la pila (sin
 *    el hueco de las filas invisibles de la rejilla por encima de su celda).
 *  - Al llegar al centro de la pantalla, la rejilla se engancha ahi (como un
 *    `position: sticky`): la pantalla sigue bajando por detras, pero la rejilla no se
 *    mueve, y la protagonista encoge y baja a la vez hasta aterrizar en su celda del
 *    centro, mientras la foto se desvanece, aparecen el icono y el valor, y las demas
 *    brotan desde cero (`scale: 0` + `opacity: 0`) a su alrededor, por capas segun lo
 *    lejos que esten de ella.
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun AppGridReveal(
    items: List<DetailItem>,
    listState: LazyListState,
    itemKey: String,
    /** Foto de fondo para la protagonista mientras es grande (null -> sin foto). */
    heroPhotoUrl: String?,
    /** Nombre de la ubicacion, escrito sobre la foto mientras la protagonista es grande. */
    heroLocationName: String,
    onClick: (DetailItem) -> Unit
) {
    if (items.isEmpty()) return

    val spacing = 13.dp
    val density = LocalDensity.current

    // Tres columnas, como la rejilla del demo en movil.
    val columns = GRID_COLUMNS
    val rows = (items.size + columns - 1) / columns
    // La protagonista es la que cae en el centro de la rejilla.
    val heroIndex = heroIndexFor(items.size)
    val heroRow = heroIndex / columns
    val heroColumn = heroIndex % columns

    // Sin bloque artificial ni "position: sticky": el grupo mide lo que mide su propia
    // rejilla (nada mas) y se coloca justo despues de Sol y luna, como cualquier otro
    // elemento de la lista. La protagonista, mientras es grande, se sale visualmente de
    // esta caja (a proposito: no hay `clipToBounds()`) y monta sobre lo de alrededor.
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GRID_HORIZONTAL_PADDING)
    ) {
        val tile = (maxWidth - spacing * (columns - 1)) / columns
        // Las celdas son 4:5, asi que son mas altas que anchas.
        val tileHeight = tile / DETAIL_TILE_ASPECT
        val gridHeight = tileHeight * rows + spacing * (rows - 1)

        // Al arrancar, la protagonista tiene el mismo tamano que la tarjeta de "Próximas
        // horas" (mismo margen lateral, no el hueco de 1dp de la rejilla).
        val heroWidth = maxWidth + GRID_HORIZONTAL_PADDING * 2 - HOURLY_CHART_SIDE_PADDING * 2

        val tilePx = with(density) { tile.toPx() }
        val tileHeightPx = with(density) { tileHeight.toPx() }
        val spacingPx = with(density) { spacing.toPx() }
        val gridWidthPx = with(density) { maxWidth.toPx() }
        val gridHeightPx = with(density) { gridHeight.toPx() }
        val heroWidthPx = with(density) { heroWidth.toPx() }
        // Mismo alto que la tarjeta de "Próximas horas", no una proporcion inventada.
        val heroHeightPx = with(density) { HOURLY_CHART_HEIGHT.toPx() }
        val viewportHeightPx = listState.layoutInfo.let { it.viewportEndOffset - it.viewportStartOffset }
            .toFloat()

        // Recorrido de scroll que se consume con la rejilla clavada en pantalla: es lo
        // que dura el encogido. Equivale al `min-height: 240vh` de la seccion del demo
        // (alli, 2,4 pantallas para un contenido de 1 pantalla = 1,4 pantallas de scroll
        // con todo quieto). Se anade como hueco libre DEBAJO de la rejilla dentro de este
        // mismo elemento de la lista, y el enganche lo va reabsorbiendo empujando la
        // rejilla hacia abajo: al llegar al tope de scroll la rejilla queda pegada al
        // fondo y ese hueco ha desaparecido, sin espacio muerto sobrante.
        val pinRunwayPx = viewportHeightPx * GRID_PIN_RUNWAY_SCREENS
        val pinRunway = with(density) { pinRunwayPx.toDp() }
        val itemHeightPx = gridHeightPx + pinRunwayPx

        // Centro de la celda de la protagonista dentro de la rejilla. Es el punto sobre
        // el que la protagonista escala (crece y encoge sin moverse de ahi, porque
        // graphicsLayer escala por defecto sobre el centro del propio elemento), asi que
        // es tambien el punto que hay que clavar en la pantalla.
        val heroCellCenterPx = heroRow * (tileHeightPx + spacingPx) + tileHeightPx / 2f
        // Posicion (relativa al viewport) que debe tener el borde superior de la rejilla
        // para que esa celda quede justo en el centro de la pantalla — fija, no cambia
        // con el scroll: es el punto en el que la rejilla se "engancha" (el `position:
        // sticky; top: 0` de `.content`, que por medir 100vh deja su contenido centrado).
        val pinnedTopPx = viewportHeightPx / 2f - heroCellCenterPx
        // La protagonista empieza a encogerse justo cuando se engancha y termina justo
        // cuando el enganche se suelta: asi nunca se la ve moverse y encoger a la vez.
        // Primero sube con el scroll hasta el centro; a partir de ahi se queda quieta y
        // solo cambia de tamano mientras se consume pinRunwayPx de scroll.
        val heroStartT = if (itemHeightPx > 0f) {
            ((viewportHeightPx - pinnedTopPx) / itemHeightPx).coerceIn(0f, 1f)
        } else {
            0f
        }
        val heroEndT = if (itemHeightPx > 0f) {
            (heroStartT + pinRunwayPx / itemHeightPx).coerceIn(0f, 1f)
        } else {
            0f
        }

        Box(modifier = Modifier.height(gridHeight + pinRunway)) {
            Column(
                modifier = Modifier
                    .height(gridHeight)
                    .graphicsLayer {
                        // Como `position: sticky`: en cuanto la rejilla llega al punto de
                        // enganche, toda ella (protagonista y hermanas juntas) se queda
                        // fija en pantalla — la pantalla sigue desplazandose, pero la
                        // rejilla no — mientras la protagonista encoge y las hermanas
                        // aparecen. Se calcula con la posicion actual de la rejilla (no
                        // con una fraccion 0..1) para que sea reversible sea cual sea la
                        // velocidad o la direccion del scroll.
                        //
                        // El tope es pinRunwayPx: mas empuje que eso sacaria la rejilla
                        // de su propio hueco en la lista y sus ultimas filas quedarian
                        // fuera de la pantalla sin scroll que pedir para verlas. Justo en
                        // ese tope la rejilla queda pegada al fondo del elemento, que es
                        // donde tiene que acabar.
                        val liveTopPx = (listState.layoutInfo.visibleItemsInfo
                            .firstOrNull { it.key == itemKey }?.offset ?: 0).toFloat()
                        translationY = (pinnedTopPx - liveTopPx)
                            .coerceIn(0f, pinRunwayPx)
                    },
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
            items.chunked(columns).forEachIndexed { row, rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    rowItems.forEachIndexed { column, item ->
                        val index = row * columns + column
                        val isHero = index == heroIndex
                        // Anillo al que pertenece: 1 las de al lado, 2 las esquinas...
                        val ring = maxOf(
                            abs(row - heroRow),
                            abs(column - heroColumn)
                        )
                        DetailTile(
                            item = item,
                            tileSize = tile,
                            // El fondo de color usa el mismo ritmo que el ancho de la
                            // protagonista (`power2.inOut`, igual que `.scaler img`
                            // width en el JS).
                            heroStyleProgress = if (isHero) {
                                {
                                    easeInOutPower(
                                        2,
                                        listState.scrollTriggerProgress(
                                            itemKey,
                                            heroStartT,
                                            heroEndT
                                        )
                                    )
                                }
                            } else {
                                null
                            },
                            // Ritmo del alto (`power1.inOut`), para poder compensar el
                            // estirado del ancho y el alto por separado (ver mas abajo,
                            // donde se usan junto a heroStyleProgress).
                            heroHeightProgress = if (isHero) {
                                {
                                    easeInOutPower(
                                        1,
                                        listState.scrollTriggerProgress(
                                            itemKey,
                                            heroStartT,
                                            heroEndT
                                        )
                                    )
                                }
                            } else {
                                null
                            },
                            heroPhotoUrl = if (isHero) heroPhotoUrl else null,
                            heroPhotoWidth = if (isHero) heroWidth else null,
                            heroPhotoHeight = if (isHero) HOURLY_CHART_HEIGHT else null,
                            heroLocationName = heroLocationName,
                            modifier = Modifier
                                .weight(1f)
                                .zIndex(if (isHero) 1f else 0f)
                                .graphicsLayer {
                                    // Adaptacion de los dos ScrollTrigger del JS: `t`
                                    // recorre 0..1 a lo largo de todo el scroll que la
                                    // rejilla puede ofrecer (ver scrollTriggerProgress).
                                    // La protagonista no empieza a encogerse hasta que
                                    // se queda clavada en el centro de la pantalla
                                    // (heroStartT); las demas brotan en la misma
                                    // ventana, justo detras.
                                    val heroT = listState.scrollTriggerProgress(
                                        itemKey,
                                        heroStartT,
                                        heroEndT
                                    )
                                    val siblingsT = listState.scrollTriggerProgress(
                                        itemKey,
                                        heroStartT,
                                        heroEndT
                                    )

                                    if (isHero) {
                                        // `.from(..., {height}, "power1.inOut")` y
                                        // `.from(..., {width}, "power2.inOut")`: cada
                                        // eje encoge con su propia curva, no con una
                                        // unica escala que mantendria siempre la misma
                                        // proporcion 4:5.
                                        val heightEase = easeInOutPower(1, heroT)
                                        val widthEase = easeInOutPower(2, heroT)

                                        // La celda de la protagonista esta en el centro
                                        // de la rejilla, asi que si solo escalase sobre
                                        // su propio centro, la foto grande nacería a
                                        // media rejilla de Sol y luna con todo el hueco
                                        // de las filas invisibles en medio. Mientras es
                                        // grande se la sube hasta que su borde superior
                                        // coincide con el de la rejilla (toTopY), o sea
                                        // pegada a la tarjeta de arriba como una mas de
                                        // la pila; ese tiron se deshace con el mismo
                                        // ritmo que el alto, asi que al acabar de encoger
                                        // aterriza justo en el centro de su celda, que es
                                        // el punto que la rejilla clava en el centro de
                                        // la pantalla (vease pinnedTopPx).
                                        val toCenterX = gridWidthPx / 2f -
                                                (column * (tilePx + spacingPx) + tilePx / 2f)
                                        val toTopY = heroHeightPx / 2f - heroCellCenterPx
                                        scaleX = lerp(heroWidthPx / tilePx, 1f, widthEase)
                                        scaleY = lerp(heroHeightPx / tileHeightPx, 1f, heightEase)

                                        translationX = toCenterX * (1f - widthEase)
                                        translationY = toTopY * (1f - heightEase)
                                    } else {
                                        // Las tres `.layer` del demo comparten la MISMA
                                        // ventana de scroll (`layersTl`): lo unico que
                                        // cambia entre ellas es la curva de aceleracion
                                        // (`power1/3/4.inOut` para la escala), no cuando
                                        // empiezan o acaban. Aqui solo hay dos anillos
                                        // alrededor de la protagonista (la rejilla es de
                                        // 3 columnas), asi que usan los dos extremos de
                                        // esa gama: el anillo pegado a la protagonista
                                        // con la curva mas brusca (se queda pequeño mas
                                        // tiempo y luego salta), el de las esquinas con
                                        // la mas suave.
                                        val scalePower = if (ring <= 1) 4 else 1
                                        val toCenterX = gridWidthPx / 2f -
                                                (column * (tilePx + spacingPx) + tilePx / 2f)
                                        val toCenterY = gridHeightPx / 2f -
                                                (row * (tileHeightPx + spacingPx) + tileHeightPx / 2f)
                                        val fadeEase = easeSineOut(siblingsT)
                                        val scaleEase = easeInOutPower(scalePower, siblingsT)

                                        scaleX = scaleEase
                                        scaleY = scaleEase
                                        alpha = fadeEase
                                        translationX = toCenterX * (1f - scaleEase)
                                        translationY = toCenterY * (1f - scaleEase)
                                    }
                                },
                            entryIndex = index,
                            onClick = { onClick(item) }
                        )
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            }
        }
    }
}

/** Padding horizontal de la rejilla: se recupera (junto con [HOURLY_CHART_SIDE_PADDING])
 * para que el ancho de la protagonista, al arrancar, iguale al de la tarjeta de
 * "Próximas horas" en vez de quedarse en el hueco que deja este padding. */
private val GRID_HORIZONTAL_PADDING = 1.dp

/** Esquinas de la protagonista mientras es la foto grande: un redondeo minimo (frente
 * al 25% de las celdas normales), para que no se vean completamente a escuadra. */
private const val HERO_PHOTO_CORNER_PERCENT = 6f

/**
 * Coloca el dato de [type] en la celda que abre la animacion, si esta en esta tanda: la
 * del centro de la rejilla, que es la que el demo reserva al `.scaler`.
 */
private fun List<DetailItem>.withHeroAtCenter(type: DetailType): List<DetailItem> {
    val current = indexOfFirst { it.type == type }
    if (current < 0) return this
    val target = heroIndexFor(size)
    if (current == target) return this
    return toMutableList().apply { add(target, removeAt(current)) }
}

/** Celda que hace de protagonista en una rejilla de [count] tarjetas. */
private fun heroIndexFor(count: Int): Int {
    val rows = (count + GRID_COLUMNS - 1) / GRID_COLUMNS
    return (((rows - 1) / 2) * GRID_COLUMNS + (GRID_COLUMNS - 1) / 2).coerceIn(0, count - 1)
}

/** Columnas de la rejilla, como el `repeat(3, 1fr)` del demo en movil. */
private const val GRID_COLUMNS = 3

/** Tarjetas por tanda: dos filas de tres. */
private const val GRID_TILES_PER_BLOCK = 6

/**
 * Cuantas pantallas de scroll se consumen con la rejilla clavada en el sitio (que es lo
 * que dura el encogido de la protagonista). Es el equivalente al `min-height: 240vh` de
 * la seccion del demo: alli el contenido pegajoso mide 1 pantalla y la seccion 2,4, o
 * sea 1,4 pantallas de scroll sin que nada se mueva de sitio.
 */
private const val GRID_PIN_RUNWAY_SCREENS = 1.2f

/** La tarjeta crece desde cero y se funde segun entra en pantalla, como las de la rejilla. */
private fun Modifier.revealOnScroll(
    listState: LazyListState,
    key: Any
): Modifier = graphicsLayer {
    val eased = smoothstep(listState.entryProgress(key, rangeFraction = 1f))
    scaleX = eased
    scaleY = eased
    alpha = eased
}

@Composable
private fun DetailTile(
    item: DetailItem,
    modifier: Modifier = Modifier,
    entryIndex: Int = 0,
    /** Lado de la celda: el contenido se ajusta a el en vez de usar tamanos fijos. */
    tileSize: Dp = DETAIL_TILE_REFERENCE,
    /**
     * Solo la protagonista de la rejilla: 0 al principio (la foto de [heroPhotoUrl] se ve
     * entera y ni el icono ni el valor todavia) y 1 cuando ya ha terminado de encogerse
     * (para entonces la foto se ha desvanecido del todo y ha aparecido el mismo cristal
     * translucido, icono y texto que las demas tarjetas). Null en el resto, que siempre
     * llevan el estilo normal.
     */
    heroStyleProgress: (() -> Float)? = null,
    /** Igual que [heroStyleProgress] pero con el ritmo del alto (`power1.inOut`), no el
     * del ancho: hace falta el par para compensar el estirado de la foto y el texto por
     * separado en cada eje (ver mas abajo). Si es null se usa [heroStyleProgress]. */
    heroHeightProgress: (() -> Float)? = null,
    /** Foto que se ve de fondo mientras la protagonista es grande; ignorada si es null. */
    heroPhotoUrl: String? = null,
    /** Tamano real (no el de la celda) al que debe verse la foto y el texto de encima,
     * para que ninguno de los dos quede estirado por el escalado no uniforme con el que
     * la celda pequeña simula ser esta protagonista grande. */
    heroPhotoWidth: Dp? = null,
    heroPhotoHeight: Dp? = null,
    /** Nombre de la ubicacion, escrito abajo a la izquierda sobre [heroPhotoUrl]. */
    heroLocationName: String? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(TILE_CORNER_FRACTION)
    Box(
        modifier = modifier
            .aspectRatio(DETAIL_TILE_ASPECT)
            .then(
                if (heroStyleProgress != null) {
                    // Esquinas casi rectas mientras es la foto grande; se redondean
                    // del todo (como las demas) segun se asienta. Animado aqui (no
                    // con un `.clip()` fijo) para no recomponer en cada fotograma.
                    Modifier.graphicsLayer {
                        val settle = heroStyleProgress().coerceIn(0f, 1f)
                        this.clip = true
                        this.shape = RoundedCornerShape(
                            percent = lerp(HERO_PHOTO_CORNER_PERCENT, 25f, settle).roundToInt()
                        )
                        // La foto grande si lleva sombra, que es lo que la despega del
                        // fondo mientras es una tarjeta de verdad; se apaga segun se
                        // asienta para acabar como las demas: sin sombra y sin canto, que
                        // es lo que dibujaba el cuadrado. Va aqui, y no en un .shadow(),
                        // porque este bloque se reevalua por fotograma sin recomponer.
                        shadowElevation = lerp(14.dp.toPx(), 0f, settle)
                        ambientShadowColor = Color.Black.copy(alpha = 0.5f)
                        spotShadowColor = Color.Black.copy(alpha = 0.7f)
                    }
                } else {
                    Modifier.clip(shape)
                }
            )
            .then(
                if (heroStyleProgress != null) {
                    // Crossfade: mientras la foto (dibujada como hijo, mas abajo) se
                    // desvanece, el cristal de las demas aparece por detras segun avanza
                    // el progreso, en vez de cambiar de golpe de un fondo a otro.
                    Modifier.drawWithCache {
                        onDrawBehind {
                            drawTileGlass(alpha = heroStyleProgress().coerceIn(0f, 1f))
                        }
                    }
                } else {
                    Modifier.drawBehind { drawTileGlass() }
                }
            )
            .clickable(onClick = onClick)
    ) {
        if (heroStyleProgress != null && heroPhotoUrl != null &&
            heroPhotoWidth != null && heroPhotoHeight != null
        ) {
            // El resto de esta tarjeta (fondo, borde) puede estirarse sin problema para
            // simular el tamano grande porque son formas lisas, pero una foto y un texto
            // se notarian deformados. Por eso esta caja interior tiene su tamano real
            // ([heroPhotoWidth] x [heroPhotoHeight], no el de la celda) y una escala
            // exactamente inversa a la del contenedor en cada instante: al pintarse
            // dentro de una celda que luego se estira de forma distinta en ancho y alto,
            // el estirado y esta escala inversa se cancelan y queda sin deformar (el
            // recorte del contenedor, que si seguimos aplicandose, es lo que la va
            // "encogiendo" de verdad segun avanza el scroll).
            val heroWidthRatio = heroPhotoWidth / tileSize
            val heroHeightRatio = heroPhotoHeight / (tileSize / DETAIL_TILE_ASPECT)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    // requiredSize, no size: la celda ya viene con restricciones fijas
                    // (min=max=su propio tamano pequeño) desde el aspectRatio()+weight()
                    // de mas arriba, y un .size() normal se recorta dentro de esas
                    // restricciones en vez de imponer el suyo — por eso la foto salia
                    // pequeña pese a pedir el tamano grande.
                    .requiredSize(heroPhotoWidth, heroPhotoHeight)
                    .graphicsLayer {
                        val widthEase = heroStyleProgress().coerceIn(0f, 1f)
                        val heightEase = (heroHeightProgress?.invoke() ?: widthEase)
                            .coerceIn(0f, 1f)
                        scaleX = 1f / lerp(heroWidthRatio, 1f, widthEase)
                        scaleY = 1f / lerp(heroHeightRatio, 1f, heightEase)
                    }
            ) {
                // La foto se ve entera al principio y se desvanece con el mismo ritmo
                // con el que aparece el cristal normal (arriba) y el icono+texto (mas
                // abajo).
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(heroPhotoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = 1f - heroStyleProgress().coerceIn(0f, 1f) }
                )
                if (!heroLocationName.isNullOrBlank()) {
                    // Degradado gris oscuro por detras, para que el texto se lea encima
                    // de cualquier foto por clara que sea.
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(56.dp)
                            .graphicsLayer { alpha = 1f - heroStyleProgress().coerceIn(0f, 1f) }
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                                )
                            )
                    )
                    Text(
                        text = heroLocationName,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .graphicsLayer { alpha = 1f - heroStyleProgress().coerceIn(0f, 1f) }
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp * (tileSize / DETAIL_TILE_REFERENCE))
                .then(
                    if (heroStyleProgress != null) {
                        // El icono y el texto aparecen con el mismo ritmo con el que se
                        // desvanece la foto: nada de golpes ni recomposicion, solo alpha.
                        Modifier.graphicsLayer { alpha = heroStyleProgress().coerceIn(0f, 1f) }
                    } else {
                        Modifier
                    }
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val scale = tileSize / DETAIL_TILE_REFERENCE
            PoppingIcon(
                emoji = item.emoji,
                iconRes = item.type.iconRes(),
                delayMillis = entryIndex * 40,
                size = 28.sp * scale
            )
            Spacer(Modifier.height(6.dp * scale))
            Text(
                text = item.value,
                color = Color.White,
                fontSize = 20.sp * scale,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = item.label,
                color = Color.White.copy(alpha = 0.9f),
                // Con la celda pequena, el tamano proporcional se quedaba en 8sp: por
                // debajo de 12sp no hay quien lo lea.
                fontSize = maxOf(13f * scale, 12f).sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

/** Tarjeta "Sol y luna": arco solar con la posición actual del sol + fase lunar dibujada. */
@Composable
private fun SunMoonCard(
    today: DayWeather,
    /** Solo con la tarjeta en el medio de la pantalla se anima el astro del arco. */
    animateAstro: Boolean,
    modifier: Modifier = Modifier
) {
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
                text = stringResource(R.string.sun_moon_title),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            SunArc(
                sunrise = today.sunrise,
                sunset = today.sunset,
                moonPhase = today.moonPhase,
                animateAstro = animateAstro
            )
            val daylight = Duration.between(today.sunrise, today.sunset)
            val (_, moonLabelRes) = moonPhaseInfo(today.moonPhase)
            val moonLabel = stringResource(moonLabelRes)
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
                        text = stringResource(
                            R.string.sun_moon_daylight_hours,
                            daylight.toHours(),
                            daylight.toMinutes() % 60
                        ),
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
private val ARC_HEIGHT = 84.dp
private val ASTRO_SIZE = 34.dp

@Composable
private fun SunArc(
    sunrise: LocalDateTime,
    sunset: LocalDateTime,
    moonPhase: Double,
    animateAstro: Boolean
) {
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(ARC_HEIGHT)
        ) {
            // La misma geometría que usa el Canvas, pero en dp: hace falta fuera de él para
            // poder colocar encima el icono animado del astro, que es un composable.
            val baseline = ARC_HEIGHT - 2.dp
            val horizontalRadius = maxWidth / 2f
            val angleRad = Math.toRadians(180.0 + 180.0 * progress)
            val markerX = maxWidth / 2f + horizontalRadius * cos(angleRad).toFloat()
            val markerY = baseline + baseline * sin(angleRad).toFloat()

            Canvas(modifier = Modifier.fillMaxSize()) {
                val baselineY = size.height - 2.dp.toPx()
                // Elipse en vez de círculo: el radio horizontal ocupa todo el ancho, pero el
                // vertical se ajusta a la altura disponible para que el arco no se salga del recuadro.
                val horizontalRadiusPx = size.width / 2f
                val verticalRadiusPx = baselineY
                val centerX = size.width / 2f

                drawLine(
                    color = Color.White.copy(alpha = 0.3f),
                    start = Offset(0f, baselineY),
                    end = Offset(size.width, baselineY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )

                val arcTopLeft = Offset(centerX - horizontalRadiusPx, baselineY - verticalRadiusPx)
                drawArc(
                    color = Color.White.copy(alpha = 0.45f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = androidx.compose.ui.geometry.Size(horizontalRadiusPx * 2f, verticalRadiusPx * 2f),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // El astro que recorre el arco: sol de día, luna de noche. La version animada
            // solo mientras la tarjeta esta en el medio de la pantalla (ver
            // isCenteredInViewport): el WebP se reproduce una vez y se queda en el ultimo
            // fotograma, asi que si se compusiera al asomar por el borde ya habria acabado
            // cuando se mira. Fuera de esa franja, el icono fijo equivalente.
            val astroDescription = if (isDaytime) {
                stringResource(R.string.sun_moon_sun_label)
            } else {
                stringResource(R.string.sun_moon_moon_label)
            }
            val astroModifier = Modifier.offset(
                x = markerX - ASTRO_SIZE / 2,
                y = markerY - ASTRO_SIZE / 2
            )
            if (animateAstro) {
                AnimatedRawIcon(
                    res = if (isDaytime) R.raw.anim_clear else R.raw.anim_moon,
                    size = ASTRO_SIZE,
                    contentDescription = astroDescription,
                    modifier = astroModifier
                )
            } else {
                Image(
                    painter = painterResource(
                        if (isDaytime) R.drawable.ic_sun else R.drawable.ic_crescent_moon
                    ),
                    contentDescription = astroDescription,
                    modifier = astroModifier.size(ASTRO_SIZE)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_sunrise),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = stringResource(DetailType.SUNRISE.titleRes),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = sunrise.format(timeFormatter),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = stringResource(DetailType.SUNSET.titleRes),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Image(
                        painter = painterResource(R.drawable.ic_sunset),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                }
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
            padding = padding,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_location),
                    contentDescription = null,
                    modifier = Modifier.size(if (isLandscape) 20.dp else 26.dp)
                )
                Text(
                    text = locationName,
                    color = Color.White,
                    fontSize = if (isLandscape) 20.sp else 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!isLandscape) {
                Text(
                    text = stringResource(R.string.week_title),
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
private const val UNSPLASH_UTM_SOURCE = "Tempovole"

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
    /** El de la pantalla: sin el, el contenido se mete debajo de la barra de pestanas. */
    padding: PaddingValues,
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

    val shrinkPx = with(LocalDensity.current) { HEADER_SHRINK_DISTANCE.toPx() }
    val shrink = remember { derivedStateOf { (scrollState.value / shrinkPx).coerceIn(0f, 1f) } }

    Box(Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            // Hueco para el header fijo, y sitio abajo para la barra de pestanas.
            .padding(top = HEADER_MAX_HEIGHT, bottom = padding.calculateBottomPadding() + 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WeatherIcon(day.condition, isDay = true, size = 80.dp)
            Text(
                text = stringResource(day.condition.labelRes),
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
                    text = stringResource(R.string.day_detail_hourly_label),
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

        val (moonEmoji, moonLabelRes) = moonPhaseInfo(day.moonPhase)
        val moonLabel = stringResource(moonLabelRes)
        val dayMaxLabel = stringResource(R.string.day_detail_max)
        val dayMinLabel = stringResource(R.string.day_detail_min)
        val dayAvgLabel = stringResource(R.string.day_detail_avg)
        val dayWindLabel = stringResource(DetailType.WIND.titleRes)
        val dayPrecipitationLabel = stringResource(DetailType.PRECIPITATION.titleRes)
        val dayRainProbabilityLabel = stringResource(R.string.day_detail_rain_probability_label)
        val daySunriseLabel = stringResource(DetailType.SUNRISE.titleRes)
        val daySunsetLabel = stringResource(DetailType.SUNSET.titleRes)
        val dayMoonLabel = stringResource(R.string.day_detail_moon_label)
        val detailItems = buildList {
            add(DetailItem("🌡️", dayMaxLabel, "${day.tempMax.roundToInt()}°", DetailType.MAX_TEMP))
            add(DetailItem("🌡️", dayMinLabel, "${day.tempMin.roundToInt()}°", DetailType.MIN_TEMP))
            add(DetailItem("🌡️", dayAvgLabel, "${day.tempMean.roundToInt()}°", DetailType.AVG_TEMP))
            add(DetailItem("💨", dayWindLabel, "${day.windKmh.roundToInt()} km/h", DetailType.WIND))
            add(
                DetailItem(
                    "🌧️", dayPrecipitationLabel, "${day.precipitationMm} mm",
                    DetailType.PRECIPITATION
                )
            )
            day.precipProbability?.let {
                add(DetailItem("☔", dayRainProbabilityLabel, "$it%", DetailType.RAIN_PROBABILITY))
            }
            add(DetailItem("🌅", daySunriseLabel, day.sunrise.format(timeFormatter), DetailType.SUNRISE))
            add(DetailItem("🌇", daySunsetLabel, day.sunset.format(timeFormatter), DetailType.SUNSET))
            add(DetailItem(moonEmoji, dayMoonLabel, moonLabel, DetailType.MOON_PHASE))
        }
        DetailGrid(detailItems, onClick = { selectedDetail = it })
    }

        ShrinkingHeader(
            title = locationName,
            subtitle = day.date.fullDate(stringResource(R.string.date_full_format)),
            shrink = shrink::value,
            leading = { IconCircleButton(emoji = "←", onClick = onBack) },
            modifier = Modifier.align(Alignment.TopStart)
        )
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
            WeatherIconStatic(
                condition = day.condition,
                isDay = true,
                size = with(LocalDensity.current) { emojiFont.toDp() }
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${day.tempMax.roundToInt()}° / ${day.tempMin.roundToInt()}°",
                    color = Color.White,
                    fontSize = tempFont,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val iconoPie = with(LocalDensity.current) { detailFont.toDp() }
                    Image(
                        painter = painterResource(R.drawable.ic_wind),
                        contentDescription = null,
                        modifier = Modifier.size(iconoPie)
                    )
                    Text(
                        text = "${day.windKmh.roundToInt()}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = detailFont,
                        maxLines = 1
                    )
                    Image(
                        painter = painterResource(R.drawable.ic_heavy_rain),
                        contentDescription = null,
                        modifier = Modifier.size(iconoPie)
                    )
                    Text(
                        text = "${day.precipitationMm}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = detailFont,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun LocalDate.dayName(): String =
    dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        .replaceFirstChar { it.uppercase() }

private fun LocalDate.fullDate(format: String): String {
    val locale = Locale.getDefault()
    val day = dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        .replaceFirstChar { it.uppercase() }
    val monthName = month.getDisplayName(TextStyle.FULL, locale)
    return String.format(locale, format, day, dayOfMonth, monthName)
}

private fun LocalDateTime.hourLabel(nowLabel: String): String {
    val now = LocalDateTime.now()
    return if (hour == now.hour && toLocalDate() == now.toLocalDate()) {
        nowLabel
    } else {
        "%02d:00".format(hour)
    }
}
