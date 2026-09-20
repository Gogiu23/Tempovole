package com.example.tiempo.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Cada tipo de dato mostrado en una tarjeta de "Hoy", con su explicación y colores propios. */
enum class DetailType(
    val title: String,
    val heroEmoji: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val explanation: String,
    val scale: DetailScale? = null
) {
    MAX_TEMP(
        "Temperatura máxima", "🌡️",
        Color(0xFFFFA751), Color(0xFFFF6B6B),
        "La temperatura más alta prevista para hoy, normalmente alcanzada a primera hora " +
            "de la tarde. Te ayuda a decidir cómo vestirte y si conviene evitar las horas " +
            "de más calor."
    ),
    MIN_TEMP(
        "Temperatura mínima", "🌡️",
        Color(0xFF4FC3F7), Color(0xFF3F51B5),
        "La temperatura más baja prevista para hoy, habitual de madrugada o al amanecer. " +
            "Útil para saber si hará falta abrigo al salir temprano."
    ),
    WIND(
        "Viento", "💨",
        Color(0xFF43C6AC), Color(0xFF2E8B7A),
        "Velocidad máxima del viento prevista para hoy, medida a 10 metros de altura. A " +
            "partir de 40-50 km/h puede dificultar caminar o llevar paraguas; por encima " +
            "de 60 km/h se consideran rachas fuertes."
    ),
    PRECIPITATION(
        "Precipitación", "🌧️",
        Color(0xFF4A90D9), Color(0xFF3F3D9E),
        "Cantidad total de lluvia (u otra precipitación) prevista para todo el día, en " +
            "milímetros. 1 mm equivale a 1 litro de agua por metro cuadrado: menos de 1 mm " +
            "es casi imperceptible, más de 10 mm ya es lluvia notable."
    ),
    RAIN_PROBABILITY(
        "Probabilidad de lluvia", "☔",
        Color(0xFF5B86E5), Color(0xFF36D1DC),
        "Probabilidad de que llueva en algún momento del día, según el modelo " +
            "meteorológico. Por encima del 50% conviene llevar paraguas; por debajo del " +
            "20% es poco probable que llueva."
    ),
    UV_INDEX(
        "Índice UV", "☀️",
        Color(0xFFFFD54A), Color(0xFFFF7043),
        "Mide la intensidad de la radiación ultravioleta del sol, en una escala de 0 a " +
            "11+. Índices de 6 o más recomiendan protección solar; a partir de 8 conviene " +
            "evitar el sol directo en las horas centrales del día.",
        scale = UV_SCALE
    ),
    SOLAR_RADIATION(
        "Radiación solar", "🔆",
        Color(0xFFFFB347), Color(0xFFFFD54A),
        "Energía solar que llega a la superficie ahora mismo, en vatios por metro " +
            "cuadrado (W/m²). Valores por encima de 700-800 W/m² indican sol muy fuerte, " +
            "típico de mediodía en verano despejado."
    ),
    DEW_POINT(
        "Punto de rocío", "💧",
        Color(0xFF56CCF2), Color(0xFF2F80ED),
        "Temperatura a la que el aire se satura de humedad y empieza a condensarse. Por " +
            "debajo de 10° se percibe seco; por encima de 20° el ambiente resulta " +
            "bochornoso, aunque la temperatura del aire no sea muy alta."
    ),
    PRESSURE(
        "Presión atmosférica", "🧭",
        Color(0xFF757F9A), Color(0xFFD7DDE8),
        "Presión del aire a nivel de la superficie, en hectopascales (hPa). Alrededor de " +
            "1013 hPa es la presión media; valores más bajos suelen asociarse a mal " +
            "tiempo, y más altos a tiempo estable y despejado."
    ),
    ELEVATION(
        "Altitud", "⛰️",
        Color(0xFF7F9C6B), Color(0xFF4E6B3A),
        "Altura sobre el nivel del mar de la ubicación elegida. Influye en la " +
            "temperatura (a más altitud, más fresco) y en la radiación solar, más " +
            "intensa cuanto más arriba."
    ),
    POLLEN(
        "Polen", "🌾",
        Color(0xFFA8E063), Color(0xFF56AB2F),
        "Concentración del tipo de polen predominante en el aire ahora mismo, medida en " +
            "granos por metro cúbico. Solo disponible en Europa. Niveles altos pueden " +
            "afectar a personas con alergias estacionales."
    ),
    HISTORICAL(
        "Comparación histórica", "📊",
        Color(0xFF667EEA), Color(0xFF764BA2),
        "Diferencia entre la temperatura media de hoy y la media histórica de ese mismo " +
            "día en los últimos 15 años. Un valor positivo indica que hoy hace más calor " +
            "de lo habitual en esta fecha; uno negativo, que hace más frío."
    ),
    MARINE(
        "Oleaje", "🌊",
        Color(0xFF2193B0), Color(0xFF6DD5ED),
        "Altura de las olas en el mar más cercano a esta ubicación, en metros. Solo hay " +
            "datos si la ubicación está en la costa o muy cerca del mar. Por encima de 2 " +
            "metros ya se considera mar picado."
    ),
    FLOOD(
        "Caudal del río", "🏞️",
        Color(0xFF3A7BD5), Color(0xFF6B4F3A),
        "Caudal estimado del río más cercano según un modelo hidrológico global, en " +
            "metros cúbicos por segundo. Solo disponible cerca de ríos con cobertura del " +
            "modelo; es una referencia, no una alerta oficial de crecidas."
    ),
    CLIMATE(
        "Cambio climático", "🌍",
        Color(0xFFFF512F), Color(0xFFDD2476),
        "Diferencia entre la temperatura media anual proyectada para 2050 (según un " +
            "modelo climático) y la de un año reciente real. Da una idea de cuánto podría " +
            "calentarse esta zona a largo plazo."
    ),
    ENSEMBLE(
        "Incertidumbre del modelo", "🎯",
        Color(0xFF636FA4), Color(0xFFE8CBC0),
        "Rango de temperatura para la hora actual según las distintas simulaciones " +
            "(miembros) del modelo meteorológico por conjunto. Cuanto más ancho el " +
            "rango, menos seguro está el modelo sobre el resultado exacto."
    ),
    AVG_TEMP(
        "Temperatura media", "🌡️",
        Color(0xFF8E9EAB), Color(0xFF5B7DB1),
        "Promedio entre todas las temperaturas del día. Es una forma más representativa " +
            "de \"cómo hizo\" en general que mirar solo el máximo o el mínimo."
    ),
    SUNRISE(
        "Amanecer", "🌅",
        Color(0xFFFFAF7B), Color(0xFFD76D77),
        "Hora a la que sale el sol hoy en esta ubicación. Cambia a lo largo del año " +
            "según la estación y la latitud del lugar."
    ),
    SUNSET(
        "Atardecer", "🌇",
        Color(0xFFDD2476), Color(0xFF3A1C71),
        "Hora a la que se pone el sol hoy en esta ubicación, marcando el final de las " +
            "horas de luz natural."
    ),
    MOON_PHASE(
        "Fase lunar", "🌙",
        Color(0xFF2C3E50), Color(0xFF4CA1AF),
        "La luna pasa por 8 fases a lo largo de un ciclo de unos 29,5 días, alternando " +
            "entre luna nueva (casi invisible) y luna llena (visible al completo). Útil " +
            "para saber si habrá algo más de luz por la noche."
    ),
    DAY_LENGTH(
        "Horas de luz", "⏳",
        Color(0xFFF7971E), Color(0xFF6DD5FA),
        "Tiempo total entre el amanecer y el atardecer de hoy. Varía a lo largo del año " +
            "según la estación y la latitud: los días son más largos en verano y más " +
            "cortos en invierno."
    )
}

/** Escala de referencia (ej. índice UV) con tramos de color y etiqueta. */
data class DetailScale(val max: Float, val bands: List<ScaleBand>)
data class ScaleBand(val upTo: Float, val color: Color, val label: String)

private val UV_SCALE = DetailScale(
    max = 11f,
    bands = listOf(
        ScaleBand(2f, Color(0xFF4CAF50), "Bajo"),
        ScaleBand(5f, Color(0xFFFFD54A), "Moderado"),
        ScaleBand(7f, Color(0xFFFF9800), "Alto"),
        ScaleBand(10f, Color(0xFFE53935), "Muy alto"),
        ScaleBand(11f, Color(0xFF9C27B0), "Extremo")
    )
)

@Composable
fun DetailInfoScreen(type: DetailType, value: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        IconCircleButton(emoji = "←", onClick = onBack)

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(type.gradientStart, type.gradientEnd))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = type.heroEmoji, fontSize = 56.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = type.title,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        type.scale?.let { scale ->
            value.filter { it.isDigit() || it == '.' || it == ',' }
                .replace(',', '.')
                .toFloatOrNull()
                ?.let { numericValue -> ScaleGauge(scale, numericValue) }
        }

        Text(
            text = type.explanation,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 15.sp,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun ScaleGauge(scale: DetailScale, currentValue: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        ) {
            val barColors = scale.bands.map { it.color }
            drawLine(
                brush = Brush.horizontalGradient(barColors),
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = size.height,
                cap = StrokeCap.Round
            )
            val ratio = (currentValue / scale.max).coerceIn(0f, 1f)
            drawCircle(
                color = Color.White,
                radius = size.height * 0.9f,
                center = Offset(size.width * ratio, size.height / 2f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            scale.bands.forEach { band ->
                Text(
                    text = band.label,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
    }
}
