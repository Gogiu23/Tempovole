package com.example.tiempo.ui

import com.example.tiempo.R
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Cada tipo de dato mostrado en una tarjeta de "Hoy", con su explicación y colores propios. */
enum class DetailType(
    @androidx.annotation.StringRes val titleRes: Int,
    val heroEmoji: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    @androidx.annotation.StringRes val explanationRes: Int,
    val scale: DetailScale? = null
) {
    MAX_TEMP(
        R.string.detail_type_max_temp_title, "🌡️",
        Color(0xFFFFA751), Color(0xFFFF6B6B),
        R.string.detail_type_max_temp_explanation
    ),
    MIN_TEMP(
        R.string.detail_type_min_temp_title, "🌡️",
        Color(0xFF4FC3F7), Color(0xFF3F51B5),
        R.string.detail_type_min_temp_explanation
    ),
    WIND(
        R.string.detail_type_wind_title, "💨",
        Color(0xFF43C6AC), Color(0xFF2E8B7A),
        R.string.detail_type_wind_explanation
    ),
    PRECIPITATION(
        R.string.detail_type_precipitation_title, "🌧️",
        Color(0xFF4A90D9), Color(0xFF3F3D9E),
        R.string.detail_type_precipitation_explanation
    ),
    RAIN_PROBABILITY(
        R.string.detail_type_rain_probability_title, "☔",
        Color(0xFF5B86E5), Color(0xFF36D1DC),
        R.string.detail_type_rain_probability_explanation
    ),
    UV_INDEX(
        R.string.detail_type_uv_index_title, "☀️",
        Color(0xFFFFD54A), Color(0xFFFF7043),
        R.string.detail_type_uv_index_explanation,
        scale = UV_SCALE
    ),
    SOLAR_RADIATION(
        R.string.detail_type_solar_radiation_title, "🔆",
        Color(0xFFFFB347), Color(0xFFFFD54A),
        R.string.detail_type_solar_radiation_explanation
    ),
    DEW_POINT(
        R.string.detail_type_dew_point_title, "💧",
        Color(0xFF56CCF2), Color(0xFF2F80ED),
        R.string.detail_type_dew_point_explanation
    ),
    PRESSURE(
        R.string.detail_type_pressure_title, "🧭",
        Color(0xFF757F9A), Color(0xFFD7DDE8),
        R.string.detail_type_pressure_explanation
    ),
    ELEVATION(
        R.string.detail_type_elevation_title, "⛰️",
        Color(0xFF7F9C6B), Color(0xFF4E6B3A),
        R.string.detail_type_elevation_explanation
    ),
    POLLEN(
        R.string.detail_type_pollen_title, "🌾",
        Color(0xFFA8E063), Color(0xFF56AB2F),
        R.string.detail_type_pollen_explanation
    ),
    HISTORICAL(
        R.string.detail_type_historical_title, "📊",
        Color(0xFF667EEA), Color(0xFF764BA2),
        R.string.detail_type_historical_explanation
    ),
    MARINE(
        R.string.detail_type_marine_title, "🌊",
        Color(0xFF2193B0), Color(0xFF6DD5ED),
        R.string.detail_type_marine_explanation
    ),
    FLOOD(
        R.string.detail_type_flood_title, "🏞️",
        Color(0xFF3A7BD5), Color(0xFF6B4F3A),
        R.string.detail_type_flood_explanation
    ),
    CLIMATE(
        R.string.detail_type_climate_title, "🌍",
        Color(0xFFFF512F), Color(0xFFDD2476),
        R.string.detail_type_climate_explanation
    ),
    ENSEMBLE(
        R.string.detail_type_ensemble_title, "🎯",
        Color(0xFF636FA4), Color(0xFFE8CBC0),
        R.string.detail_type_ensemble_explanation
    ),
    AVG_TEMP(
        R.string.detail_type_avg_temp_title, "🌡️",
        Color(0xFF8E9EAB), Color(0xFF5B7DB1),
        R.string.detail_type_avg_temp_explanation
    ),
    SUNRISE(
        R.string.detail_type_sunrise_title, "🌅",
        Color(0xFFFFAF7B), Color(0xFFD76D77),
        R.string.detail_type_sunrise_explanation
    ),
    SUNSET(
        R.string.detail_type_sunset_title, "🌇",
        Color(0xFFDD2476), Color(0xFF3A1C71),
        R.string.detail_type_sunset_explanation
    ),
    MOON_PHASE(
        R.string.detail_type_moon_phase_title, "🌙",
        Color(0xFF2C3E50), Color(0xFF4CA1AF),
        R.string.detail_type_moon_phase_explanation
    ),
    DAY_LENGTH(
        R.string.detail_type_day_length_title, "⏳",
        Color(0xFFF7971E), Color(0xFF6DD5FA),
        R.string.detail_type_day_length_explanation
    )
}

/** Escala de referencia (ej. índice UV) con tramos de color y etiqueta. */
data class DetailScale(val max: Float, val bands: List<ScaleBand>)
data class ScaleBand(val upTo: Float, val color: Color, @androidx.annotation.StringRes val labelRes: Int)

private val UV_SCALE = DetailScale(
    max = 11f,
    bands = listOf(
        ScaleBand(2f, Color(0xFF4CAF50), R.string.scale_band_low),
        ScaleBand(5f, Color(0xFFFFD54A), R.string.scale_band_moderate),
        ScaleBand(7f, Color(0xFFFF9800), R.string.scale_band_high),
        ScaleBand(10f, Color(0xFFE53935), R.string.scale_band_very_high),
        ScaleBand(11f, Color(0xFF9C27B0), R.string.scale_band_extreme)
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
                val animRes = type.animResOrNull()
                val iconRes = type.iconRes()
                when {
                    animRes != null -> AnimatedRawIcon(animRes, size = 72.dp, contentDescription = null)
                    iconRes != null -> Image(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                    else -> Text(text = type.heroEmoji, fontSize = 56.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(type.titleRes),
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
            text = stringResource(type.explanationRes),
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
                    text = stringResource(band.labelRes),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }
        }
    }
}
