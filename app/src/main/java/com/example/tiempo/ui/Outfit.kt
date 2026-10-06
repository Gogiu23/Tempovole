package com.example.tiempo.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiempo.R
import com.example.tiempo.data.model.CurrentWeather
import com.example.tiempo.data.model.DayWeather
import com.example.tiempo.data.model.WeatherCondition
import kotlin.math.roundToInt

/**
 * Prendas y complementos que la app puede recomendar.
 *
 * El orden de declaración es el orden en el que se pintan: primero lo que va sobre el
 * cuerpo, luego las piernas, el calzado y al final los complementos, así la rejilla se
 * lee de arriba abajo en el mismo orden en el que uno se viste.
 */
enum class Garment(@DrawableRes val iconRes: Int, @StringRes val labelRes: Int) {
    COAT(R.drawable.ic_coat, R.string.garment_coat),
    RAINCOAT(R.drawable.ic_raincoat, R.string.garment_raincoat),
    JACKET(R.drawable.ic_jacket, R.string.garment_jacket),
    HOODIE(R.drawable.ic_hoodie, R.string.garment_hoodie),
    LONG_SLEEVE(R.drawable.ic_long_sleeve, R.string.garment_long_sleeve),
    SHORT_SLEEVE(R.drawable.ic_short_sleeve, R.string.garment_short_sleeve),
    TROUSERS(R.drawable.ic_trousers, R.string.garment_trousers),
    SHORTS(R.drawable.ic_shorts, R.string.garment_shorts),
    BOOTS(R.drawable.ic_boots, R.string.garment_boots),
    SNEAKERS(R.drawable.ic_sneakers, R.string.garment_sneakers),
    SCARF(R.drawable.ic_scarf, R.string.garment_scarf),
    BEANIE(R.drawable.ic_beanie, R.string.garment_beanie),
    GLOVES(R.drawable.ic_gloves, R.string.garment_gloves),
    UMBRELLA(R.drawable.ic_umbrella, R.string.garment_umbrella),
    SUNGLASSES(R.drawable.ic_sunglasses, R.string.garment_sunglasses),
    CAP(R.drawable.ic_cap, R.string.garment_cap),
    SUNSCREEN(R.drawable.ic_sunscreen, R.string.garment_sunscreen)
}

/** Tramo de temperatura con el que se decide la base del conjunto. */
enum class TempBand(@StringRes val labelRes: Int) {
    FREEZING(R.string.outfit_band_freezing),
    COLD(R.string.outfit_band_cold),
    COOL(R.string.outfit_band_cool),
    MILD(R.string.outfit_band_mild),
    WARM(R.string.outfit_band_warm),
    HOT(R.string.outfit_band_hot)
}

/** Recomendación de hoy: el tramo, con qué temperatura se decidió, las prendas y los avisos. */
data class OutfitAdvice(
    val band: TempBand,
    /** Temperatura con la que se ha decidido, ya descontada la sensación por viento. */
    val feelsLike: Int,
    val garments: List<Garment>,
    /** Avisos sueltos, como StringRes, en el orden en el que se han ido añadiendo. */
    val tipsRes: List<Int>
)

/** Desde aquí el viento empieza a notarse en la piel y a restar sensación térmica. */
private const val WIND_NOTICEABLE_KMH = 20.0

/** Probabilidad desde la que se da la lluvia por hecha a efectos de llevar paraguas. */
private const val RAIN_PROBABILITY_THRESHOLD = 50

/** Con este viento el paraguas estorba más que ayuda: manda el chubasquero. */
private const val STRONG_WIND_KMH = 30.0

/** Precipitación acumulada en el día a partir de la cual conviene calzado cerrado. */
private const val HEAVY_RAIN_MM = 5.0

/** Índice UV alto: toca protegerse de verdad. */
private const val HIGH_UV = 6.0

/** Índice UV moderado: con las gafas va bien. */
private const val MODERATE_UV = 3.0

/** Diferencia máxima-mínima del día a partir de la cual conviene ir por capas. */
private const val LAYERS_RANGE_C = 12.0

/**
 * Lo que el viento le resta a la temperatura percibida.
 *
 * NO es la fórmula oficial de wind chill: esa solo vale por debajo de 10 °C y con viento
 * fuerte, y aquí hace falta algo que también sirva a 18 °C con ventolera. Es un ajuste
 * lineal desde [WIND_NOTICEABLE_KMH], que es más o menos donde el viento empieza a notarse.
 *
 * ponytail: aproximación lineal con techo de 4 °C. Si en zonas muy ventosas se queda
 * corta, subir el techo antes que cambiar de fórmula.
 */
private fun windChill(windKmh: Double): Double =
    ((windKmh - WIND_NOTICEABLE_KMH) / 10.0).coerceIn(0.0, 4.0)

/**
 * Decide qué ponerse hoy. Es una tabla de decisión a propósito, no una llamada a un
 * modelo: con el mismo tiempo tiene que recomendar siempre lo mismo, funcionar sin
 * cobertura y salir traducida con los strings que ya existen.
 *
 * La base la fija la máxima del día (es lo que se vive la mayor parte de las horas)
 * corregida por viento; el resto de reglas añaden o quitan por encima de esa base.
 */
fun outfitFor(today: DayWeather, current: CurrentWeather): OutfitAdvice {
    val feels = today.tempMax - windChill(today.windKmh)
    val band = when {
        feels < 0 -> TempBand.FREEZING
        feels < 8 -> TempBand.COLD
        feels < 14 -> TempBand.COOL
        feels < 20 -> TempBand.MILD
        feels < 26 -> TempBand.WARM
        else -> TempBand.HOT
    }

    // Set, no lista: varias reglas pueden pedir la misma prenda (las botas salen por
    // nieve y por lluvia fuerte, por ejemplo) y no tiene que aparecer repetida.
    val garments = mutableSetOf<Garment>()
    val tips = mutableListOf<Int>()

    when (band) {
        TempBand.FREEZING -> garments += listOf(
            Garment.COAT, Garment.LONG_SLEEVE, Garment.TROUSERS, Garment.BOOTS,
            Garment.SCARF, Garment.BEANIE, Garment.GLOVES
        )
        TempBand.COLD -> garments += listOf(
            Garment.COAT, Garment.LONG_SLEEVE, Garment.TROUSERS, Garment.BOOTS, Garment.SCARF
        )
        TempBand.COOL -> garments += listOf(
            Garment.JACKET, Garment.LONG_SLEEVE, Garment.TROUSERS, Garment.SNEAKERS
        )
        TempBand.MILD -> garments += listOf(
            Garment.HOODIE, Garment.LONG_SLEEVE, Garment.TROUSERS, Garment.SNEAKERS
        )
        TempBand.WARM -> garments += listOf(
            Garment.SHORT_SLEEVE, Garment.TROUSERS, Garment.SNEAKERS
        )
        TempBand.HOT -> garments += listOf(
            Garment.SHORT_SLEEVE, Garment.SHORTS, Garment.SNEAKERS
        )
    }

    val rainLikely = (today.precipProbability ?: 0) >= RAIN_PROBABILITY_THRESHOLD ||
        today.precipitationMm > 0.0
    if (rainLikely) {
        val heavy = today.precipitationMm > HEAVY_RAIN_MM
        val windy = today.windKmh > STRONG_WIND_KMH
        if (windy || heavy) {
            // El chubasquero SUSTITUYE al paraguas, no se suma: recomendar los dos a la
            // vez es ruido, y con 30 km/h el paraguas se da la vuelta de todas formas.
            garments += Garment.RAINCOAT
            if (windy) tips += R.string.outfit_tip_umbrella_useless
        } else {
            garments += Garment.UMBRELLA
        }
        if (heavy) {
            garments += Garment.BOOTS
            garments -= Garment.SNEAKERS
        }
    }

    if (today.windKmh > STRONG_WIND_KMH) tips += R.string.outfit_tip_wind

    // La nieve manda sobre el tramo: puede nevar con el termómetro en 2 °C, que cae en
    // COLD, y aun así hacen falta botas, gorro y guantes.
    if (today.condition == WeatherCondition.SNOW) {
        garments += listOf(Garment.BOOTS, Garment.BEANIE, Garment.GLOVES)
        garments -= Garment.SNEAKERS
    }

    // El UV no acompaña a la temperatura: en marzo, en alta montaña o en un día claro de
    // invierno puede ser alto haciendo frío. Por eso va por su cuenta, fuera del tramo.
    val uv = current.uvIndex
    if (uv != null) {
        when {
            uv >= HIGH_UV -> {
                garments += listOf(Garment.SUNGLASSES, Garment.CAP, Garment.SUNSCREEN)
                tips += R.string.outfit_tip_uv_high
            }
            uv >= MODERATE_UV -> garments += Garment.SUNGLASSES
        }
    }

    if (today.tempMax - today.tempMin > LAYERS_RANGE_C) tips += R.string.outfit_tip_layers

    return OutfitAdvice(
        band = band,
        feelsLike = feels.roundToInt(),
        // Por orden de declaración del enum, que es el orden en el que se viste uno.
        garments = garments.sortedBy { it.ordinal },
        tipsRes = tips.distinct()
    )
}

/** Columnas de la rejilla de prendas, como la de datos de "Hoy". */
private const val GARMENT_COLUMNS = 3

/** Pestaña "Qué me pongo": la recomendación de hoy, decidida por [outfitFor]. */
@Composable
fun OutfitScreen(
    today: DayWeather,
    current: CurrentWeather,
    locationName: String,
    padding: PaddingValues
) {
    // La tabla es pura: con los mismos datos da lo mismo, asi que no hace falta
    // recalcularla en cada recomposicion (scroll incluido).
    val advice = remember(today, current) { outfitFor(today, current) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Misma cabecera que la pestaña de Semana: la ubicacion grande y el titulo debajo.
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_location),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = locationName,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = stringResource(R.string.outfit_title),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }

        OutfitHeadline(advice)
        GarmentGrid(advice.garments)
        advice.tipsRes.forEach { OutfitTip(it) }
        Spacer(Modifier.height(12.dp))
    }
}

/** El tramo de temperatura y con que sensacion termica se ha decidido. */
@Composable
private fun OutfitHeadline(advice: OutfitAdvice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(advice.band.labelRes),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.outfit_decided_with, advice.feelsLike),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }
    }
}

/** Las prendas en tres columnas, en el orden de declaracion del enum. */
@Composable
private fun GarmentGrid(garments: List<Garment>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        garments.chunked(GARMENT_COLUMNS).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { GarmentTile(it, Modifier.weight(1f)) }
                // Rellena la ultima fila: sin esto las celdas sueltas se estiran.
                repeat(GARMENT_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun GarmentTile(garment: Garment, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(garment.iconRes),
                contentDescription = null,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = stringResource(garment.labelRes),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

/** Un aviso suelto: el motivo por el que la tabla ha anadido o quitado algo. */
@Composable
private fun OutfitTip(@StringRes textRes: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = "\u2022", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Text(
            text = stringResource(textRes),
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 14.sp
        )
    }
}
