# Tiempo — Documentación Técnica

**Versión:** 1.2  
**Fecha:** Septiembre 2026  
**Plataforma:** Android (API 26+, Kotlin + Jetpack Compose)

---

## Índice

1. [Visión general](#1-visión-general)
2. [Arquitectura de la aplicación](#2-arquitectura-de-la-aplicación)
3. [Capa de datos](#3-capa-de-datos)
4. [Capa de presentación](#4-capa-de-presentación)
5. [Sistema de iconos](#5-sistema-de-iconos)
6. [Pantallas principales](#6-pantallas-principales)
7. [Notificaciones y widget](#7-notificaciones-y-widget)
8. [Recursos y assets](#8-recursos-y-assets)
9. [Permisos y seguridad](#9-permisos-y-seguridad)
10. [Dependencias principales](#10-dependencias-principales)

---

## 1. Visión general

**Tiempo** es una aplicación de previsión meteorológica para Android que muestra el tiempo actual, la previsión por horas y por días (7 días), junto con datos adicionales opcionales (calidad del aire, polen, oleaje, caudal de ríos, comparación histórica, cambio climático e incertidumbre del modelo).

### Características principales

- **Previsión completa**: tiempo actual, 7 días de previsión diaria y 48 horas detalladas
- **Gráfico horario interactivo**: selector de métrica (temperatura, viento, lluvia) con barras de precipitación
- **Fondos dinámicos de Unsplash**: fotos de fondo según la franja del día y la condición meteorológica
- **Iconos animados**: WebP animados de Flaticon para las condiciones meteorológicas
- **Widget de pantalla de inicio**: muestra el tiempo actual con fondo personalizable
- **Notificación diaria (morning report)**: resumen del día a la hora configurada
- **Datos extra opcionales**: el usuario activa sólo los que quiere (calidad del aire, polen, oleaje, etc.)

### Fuentes de datos

- **Datos meteorológicos**: [Open-Meteo API](https://open-meteo.com/) (gratuita, sin clave)
- **Fotos de fondo**: [Unsplash API](https://unsplash.com/developers) (requiere clave)
- **Geocodificación**: Open-Meteo Geocoding

---

## 2. Arquitectura de la aplicación

La app sigue el patrón **MVVM** (Model-View-ViewModel) con Jetpack Compose para la UI.

```
┌─────────────────────────────────────────────────────────────┐
│                      UI Layer (Compose)                     │
│  WeatherScreen, TodayScreen, WeekScreen, DetailInfoScreen  │
└───────────────────────────┬─────────────────────────────────┘
                            │ StateFlow<WeatherUiState>
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     ViewModel Layer                         │
│                     WeatherViewModel                         │
└───────────────────────────┬─────────────────────────────────┘
                            │ suspend fun load(lat, lon)
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                             │
│  WeatherRepository, UnsplashRepository, FeaturePreferences  │
└───────────────────────────┬─────────────────────────────────┘
                            │ Retrofit + kotlinx.serialization
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   External APIs                             │
│     Open-Meteo (forecast)    Unsplash (photos)             │
└─────────────────────────────────────────────────────────────┘
```

### Flujo de datos

1. El usuario abre la app → `MainActivity` renderiza `WeatherScreen`
2. `WeatherScreen` observa `viewModel.uiState` (StateFlow)
3. El ViewModel llama a `WeatherRepository.fetch(lat, lon)`
4. El repositorio hace peticiones paralelas a Open-Meteo y Unsplash
5. Los datos se transforman a modelos de dominio (`WeatherForecast`, `DayWeather`, `HourWeather`)
6. La UI se actualiza automáticamente con el nuevo estado

---

## 3. Capa de datos

### 3.1 Modelos de dominio (`data/model/Forecast.kt`)

#### `WeatherForecast`
Contiene toda la previsión:
```kotlin
data class WeatherForecast(
    val current: CurrentWeather,    // Tiempo actual
    val days: List<DayWeather>,     // 7 días de previsión
    val hours: List<HourWeather>,   // 48 horas de previsión
    val elevationM: Double          // Elevación del punto
)
```

#### `CurrentWeather`
Tiempo en el momento actual:
```kotlin
data class CurrentWeather(
    val temp: Double,
    val condition: WeatherCondition,
    val uvIndex: Double?,
    val solarRadiation: Double?,
    val dewPoint: Double?,
    val pressureHpa: Double?
)
```

#### `DayWeather`
Previsión para un día:
```kotlin
data class DayWeather(
    val date: LocalDate,
    val tempMax: Double,
    val tempMin: Double,
    val tempMean: Double,
    val precipitationMm: Double,
    val precipProbability: Int?,
    val windKmh: Double,
    val condition: WeatherCondition,
    val sunrise: LocalDateTime,
    val sunset: LocalDateTime,
    val moonPhase: Double          // 0..1 (0=luna nueva, 0.5=luna llena)
)
```

#### `HourWeather`
Previsión para una hora:
```kotlin
data class HourWeather(
    val time: LocalDateTime,
    val temp: Double,
    val precipProbability: Int?,
    val precipitationMm: Double,   // mm de lluvia para esa hora
    val windKmh: Double,
    val condition: WeatherCondition,
    val isDay: Boolean             // true si es de día (según WMO is_day)
)
```

#### `WeatherCondition` (enum)
Mapea los códigos WMO a condiciones comprensibles:
```kotlin
enum class WeatherCondition(val label: String, val emoji: String, ...) {
    CLEAR("Despejado", "☀️"),
    PARTLY("Parcialmente nublado", "⛅"),
    CLOUDY("Nublado", "☁️"),
    FOG("Niebla", "🌫️"),
    DRIZZLE("Llovizna", "🌦️"),
    RAIN("Lluvia", "🌧️"),
    SNOW("Nieve", "🌨️"),
    STORM("Tormenta", "⛈️"),
    UNKNOWN("Desconocido", "🌡️");
    
    companion object {
        fun fromCode(code: Int): WeatherCondition = when (code) {
            0, 1, 2, 3 -> CLEAR
            45, 48 -> FOG
            51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> RAIN
            71, 73, 75, 77, 85, 86 -> SNOW
            95, 96, 99 -> STORM
            // ... más mapeos
        }
    }
}
```

### 3.2 APIs Retrofit (`data/WeatherApi.kt`)

#### Petición principal a Open-Meteo
```kotlin
interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,weather_code,uv_index,...",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,...",
        @Query("hourly") hourly: String = "temperature_2m,precipitation_probability,precipitation,...",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") days: Int = 7
    ): ForecastResponse
}
```

### 3.3 Repositorios

#### `WeatherRepository` (`data/WeatherApi.kt`)
Orquesta todas las llamadas de red:
```kotlin
object WeatherRepository {
    suspend fun fetch(lat: Double, lon: Double): WeatherForecast {
        val response = weatherApi.getForecast(lat, lon)
        return response.toWeatherForecast()
    }
}
```

#### `UnsplashRepository` (`data/UnsplashRepository.kt`)
Gestiona las fotos de fondo:
- `backgroundPhoto()`: foto de fondo según la franja del día (mañana/tarde/noche)
- `conditionPhotos()`: fotos para las tarjetas de la semana según la condición
- Caché diario en SharedPreferences (máximo 11 fotos activas)
- Créditos de fotógrafo almacenados y mostrados en la pantalla Info

#### `LocationRepository` (`data/LocationRepository.kt`)
Persiste la ubicación elegida:
```kotlin
data class SavedLocation(val name: String, val lat: Double, val lon: Double)

object LocationRepository {
    fun get(context: Context): SavedLocation  // Por defecto: Barcelona
    fun save(context: Context, location: SavedLocation)
}
```

#### `FeaturePreferences` (`data/FeaturePreferences.kt`)
Features opcionales activables:
```kotlin
enum class ExtraFeature {
    SUN_MOON,       // Arco solar + fase lunar
    ATMOSPHERIC,    // Radiación solar + punto de rocío
    POLLEN,         // Calidad del aire + pólenes
    ELEVATION,      // Elevación del punto
    HISTORICAL,     // Comparación con años anteriores
    MARINE,         // Altura y período de ola
    FLOOD,          // Caudal de ríos
    CLIMATE,        // Proyección cambio climático
    ENSEMBLE        // Incertidumbre del modelo
}
```

---

## 4. Capa de presentación

### 4.1 ViewModel (`ui/WeatherViewModel.kt`)

```kotlin
sealed interface WeatherUiState {
    object Loading : WeatherUiState
    data class Success(val forecast: WeatherForecast) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()
    
    fun load(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            runCatching { WeatherRepository.fetch(lat, lon) }
                .onSuccess { _uiState.value = WeatherUiState.Success(it) }
                .onFailure { _uiState.value = WeatherUiState.Error(it.message ?: "Error") }
        }
    }
}
```

### 4.2 Navegación

La app no usa Navigation Compose. En su lugar, maneja el estado manualmente con variables `rememberSaveable`:

```kotlin
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }  // 0=Hoy, 1=Semana, 2=Detalle
    var overlayIndex by rememberSaveable { mutableIntStateOf(0) } // OverlayScreen.NONE por defecto
    var selectedDay by remember { mutableStateOf<DayWeather?>(null) }
    var selectedDetail by remember { mutableStateOf<DetailItem?>(null) }
    
    // ...
}
```

El `BackHandler` intercepta el gesto de vuelta cuando hay un overlay abierto:
```kotlin
BackHandler(enabled = overlay != OverlayScreen.NONE) {
    overlayIndex = OverlayScreen.NONE.ordinal
}
```

---

## 5. Sistema de iconos

### 5.1 Iconos animados (`res/raw/`)

WebP animados de Flaticon, 13 iconos para las condiciones meteorológicas:

| Archivo | Condición |
|---------|-----------|
| `anim_clear.webp` | Sol (cielo despejado, día) |
| `anim_moon.webp` | Luna (cielo despejado, noche) |
| `anim_partly.webp` | Parcialmente nublado (día) |
| `anim_moon_cloudy.webp` | Parcialmente nublado (noche) |
| `anim_cloudy.webp` | Nublado |
| `anim_fog.webp` | Niebla |
| `anim_rain.webp` | Lluvia / llovizna |
| `anim_snow.webp` | Nieve |
| `anim_storm.webp` | Tormenta |
| `anim_weather.webp` | Condición desconocida |

### 5.2 Iconos estáticos (`res/drawable/`)

37 iconos WebP estáticos de Flaticon para:
- Condiciones meteorológicas (variantes día/noche)
- Datos de detalle (temperatura, viento, humedad, presión, etc.)
- UI chrome (ubicación, ajustes, info, etc.)

### 5.3 Funciones de mapeo (`ui/WeatherIcon.kt`)

```kotlin
// Icono animado para una condición
fun WeatherIcon(condition: WeatherCondition, isDay: Boolean, size: Dp, ...) {
    val res = condition.animRes(isDay)  // privado
    AnimatedRawIcon(res, size, condition.label)
}

// Icono estático para una condición
fun WeatherIconStatic(condition: WeatherCondition, isDay: Boolean, size: Dp) {
    val res = condition.staticIcon(isDay)  // público
    Image(painterResource(res), ...)
}

// Icono para un tipo de dato
fun DetailType.iconRes(): Int? = when (this) {
    DetailType.MAX_TEMP -> R.drawable.ic_hot
    DetailType.MIN_TEMP -> R.drawable.ic_cold
    DetailType.RAIN_PROBABILITY -> R.drawable.ic_rain_prob
    DetailType.PRESSURE -> R.drawable.ic_pressure
    // ...
}
```

---

## 6. Pantallas principales

### 6.1 `WeatherScreen` — Pantalla raíz

Es el punto de entrada de la UI. Contiene:
- Fondo dinámico de Unsplash (o degradado si no hay API key)
- Cabecera con nombre de ubicación y reloj en tiempo real
- Menú lateral deslizante
- Navegación entre Hoy / Semana / Detalle
- Overlays para Ajustes, Info, Changelog, Blog

```kotlin
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    when (state) {
        is WeatherUiState.Loading -> /* Indicador de carga */
        is WeatherUiState.Error -> /* Mensaje de error */
        is WeatherUiState.Success -> {
            // Fondo Unsplash + contenido principal
        }
    }
}
```

### 6.2 `TodayScreen` — Pantalla "Hoy"

Muestra:
- **Header que se encoge** (`ShrinkingHeader`): nombre de ciudad + icono de condición + temperatura + fecha
- **Gráfico horario** (`HourlyLineChart`): línea con temperatura/viento/lluvia, iconos de condición, barras de precipitación
- **Tarjeta sol/luna** (`SunMoonCard`): arco con posición del sol, amanecer/atardecer, fase lunar
- **Rejilla de datos** (`AppGridReveal`): tarjetas con temperatura, viento, humedad, presión, UV, etc.

```kotlin
@Composable
private fun TodayScreen(
    today: DayWeather,
    current: CurrentWeather,
    hours: List<HourWeather>,
    elevationM: Double,
    location: SavedLocation,
    padding: PaddingValues
) {
    // ...
}
```

### 6.3 `HourlyLineChart` — Gráfico horario

Canvas custom que dibuja:
1. **Fondo**: gradiente horizontal por franja del día (mañana/atardecer/noche)
2. **Línea de datos**: temperatura, viento o prob. de lluvia según métrica seleccionada
3. **Relleno degradado**: bajo la línea, desvaneciéndose hacia abajo
4. **Iconos de condición**: encima de la línea (solo si métrica ≠ temperatura)
5. **Barras de precipitación**: en la zona inferior, altura proporcional a mm
6. **Etiquetas**: valor numérico + hora

```kotlin
@Composable
private fun HourlyLineChart(hours: List<HourWeather>, metric: HourlyMetric) {
    Card(...) {
        Canvas(...) {
            // Fondo con gradiente horizontal por hora
            // Línea con relleno degradado
            // Iconos de condición
            // Barras de lluvia
            // Etiquetas de valor y hora
        }
    }
}
```

### 6.4 `WeekScreen` — Pantalla "Semana"

Carrusel de tarjetas con efecto coverflow:
- Cada día tiene foto de fondo de Unsplash según la condición
- Al pulsar una tarjeta, abre `DayDetailScreen`

```kotlin
@Composable
private fun WeekScreen(days: List<DayWeather>, hours: List<HourWeather>, location: SavedLocation) {
    // LazyRow horizontal con efecto coverflow
}
```

### 6.5 `DayDetailScreen` — Detalle de un día

Pantalla completa para un día concreto:
- Icono animado de la condición
- Temperaturas máxima/mínima
- Gráfico horario (solo horas de ese día)
- Lista de datos de detalle

### 6.6 `DetailInfoScreen` — Pantalla de detalle expandida

Se abre al pulsar una tarjeta de datos. Muestra:
- Icono (animado si existe, estático si no)
- Valor grande
- Escala visual con bandas de color (si aplica)
- Explicación del dato

```kotlin
@Composable
fun DetailInfoScreen(type: DetailType, value: String, onBack: () -> Unit) {
    Column {
        // Icono hero en círculo con gradiente
        // Valor numérico
        // Escala (opcional)
        // Explicación
    }
}
```

### 6.7 `SettingsScreen` — Ajustes

- **Ubicación**: búsqueda por nombre
- **Notificación diaria**: selector de hora
- **Datos extra**: tarjetas expandibles para activar/desactivar cada feature
- **Widget**: selección de fondo (foto/transparente/color) y color

### 6.8 `InfoScreen` — Información

- Versión de la app
- Términos de uso
- Créditos de fotos (Unsplash)
- Créditos de iconos (Flaticon)
- Política de privacidad

---

## 7. Notificaciones y widget

### 7.1 Notificación diaria (morning report)

Programada con `AlarmManager` + `WorkManager`:

1. `NotificationScheduler.scheduleDaily()` programa una alarma exacta a la hora configurada
2. `MorningReportAlarmReceiver` recibe la alarma y encola un `WeatherNotificationWorker`
3. `WeatherNotificationWorker` obtiene la previsión y publica la notificación
4. `BootReceiver` reprograma la alarma tras un reinicio del dispositivo

```kotlin
object NotificationScheduler {
    fun scheduleDaily(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService<AlarmManager>()
        val intent = Intent(context, MorningReportAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, FLAG_IMMUTABLE)
        
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
}
```

### 7.2 Widget de pantalla de inicio

Implementado con Jetpack Glance:

```kotlin
class TiempoWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceAppWidgetId) {
        val forecast = WeatherRepository.fetch(location.lat, location.lon)
        
        provideContent {
            // Layout del widget: fondo + temperatura + condición
        }
    }
}
```

Opciones de fondo:
- `IMAGE`: foto de Unsplash
- `TRANSPARENT`: fondo transparente
- `COLOR`: color sólido (6 opciones)

---

## 8. Recursos y assets

### 8.1 Iconos animados (`res/raw/`)

13 archivos WebP animados, ~2.3 MB total.

### 8.2 Iconos estáticos (`res/drawable/`)

37 archivos WebP estáticos, ~350 KB total.

### 8.3 Icono de la app

- `ic_launcher_foreground.xml`: capa frontal (icono de sol)
- `ic_launcher_background.xml`: capa de fondo (gradiente)

---

## 9. Permisos y seguridad

| Permiso | Propósito |
|---------|-----------|
| `INTERNET` | Peticiones a Open-Meteo y Unsplash |
| `ACCESS_NETWORK_STATE` | Comprobar conectividad |
| `POST_NOTIFICATIONS` | Notificaciones en Android 13+ |
| `RECEIVE_BOOT_COMPLETED` | Reprogramar alarma tras reinicio |
| `SCHEDULE_EXACT_ALARM` | Alarma exacta para morning report (API < 33) |
| `USE_EXACT_ALARM` | Ídem (API 33+) |

La app **no usa GPS**. La ubicación se introduce manualmente por el usuario (búsqueda por nombre).

---

## 10. Dependencias principales

| Dependencia | Versión | Propósito |
|-------------|---------|-----------|
| Compose BOM | 2024.12.01 | UI declarativa |
| Lifecycle + ViewModel | 2.8.7 | Gestión del estado |
| Retrofit + OkHttp | 2.11.0 / 4.12.0 | Cliente HTTP |
| kotlinx.serialization | 1.7.3 | Deserialización JSON |
| Coil | 2.7.0 | Carga de imágenes y WebP animado |
| WorkManager | 2.9.1 | Notificaciones en segundo plano |
| Glance | 1.1.1 | Widget de pantalla de inicio |

---

## Apéndice: Estructura de archivos

```
app/src/main/java/com/example/tiempo/
├── Config.kt
├── MainActivity.kt
├── data/
│   ├── AirQuality.kt
│   ├── ClimateChange.kt
│   ├── Ensemble.kt
│   ├── FeaturePreferences.kt
│   ├── Flood.kt
│   ├── Geocoding.kt
│   ├── HistoricalWeather.kt
│   ├── LocationRepository.kt
│   ├── Marine.kt
│   ├── UnsplashRepository.kt
│   ├── WeatherApi.kt
│   ├── WidgetPreferences.kt
│   └── model/
│       ├── Forecast.kt
│       └── Unsplash.kt
├── notifications/
│   ├── BootReceiver.kt
│   ├── DismissNotificationReceiver.kt
│   ├── MorningReportAlarmReceiver.kt
│   ├── NotificationHelper.kt
│   ├── NotificationPreferences.kt
│   ├── NotificationScheduler.kt
│   └── WeatherNotificationWorker.kt
├── ui/
│   ├── Changelog.kt
│   ├── DetailInfo.kt
│   ├── WeatherIcon.kt
│   ├── WeatherScreen.kt
│   ├── WeatherViewModel.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── widget/
    ├── TiempoWidget.kt
    ├── TiempoWidgetReceiver.kt
    └── WidgetUpdater.kt
```

---

**Fin del documento**
