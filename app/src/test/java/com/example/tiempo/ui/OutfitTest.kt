package com.example.tiempo.ui

import com.example.tiempo.data.model.CurrentWeather
import com.example.tiempo.data.model.DayWeather
import com.example.tiempo.data.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Comprobación de la tabla de [outfitFor]. No pretende cubrirla entera: fija los casos
 * que se rompen en silencio si alguien mueve un umbral — los bordes de cada tramo y las
 * reglas que QUITAN prendas en vez de añadirlas, que son las que se olvidan.
 *
 * Los avisos se comprueban por cantidad y no por StringRes concreto: el valor de R en un
 * test de JVM es un detalle del build, y lo que importa es que la regla dispare o no.
 */
class OutfitTest {

    private fun day(
        tempMax: Double,
        tempMin: Double = tempMax - 5,
        windKmh: Double = 0.0,
        precipitationMm: Double = 0.0,
        precipProbability: Int? = 0,
        condition: WeatherCondition = WeatherCondition.CLEAR
    ) = DayWeather(
        date = LocalDate.of(2026, 10, 6),
        tempMax = tempMax,
        tempMin = tempMin,
        tempMean = (tempMax + tempMin) / 2,
        precipitationMm = precipitationMm,
        precipProbability = precipProbability,
        windKmh = windKmh,
        condition = condition,
        sunrise = LocalDateTime.of(2026, 10, 6, 8, 0),
        sunset = LocalDateTime.of(2026, 10, 6, 20, 0),
        moonPhase = 0.5
    )

    private fun current(uv: Double? = null) =
        CurrentWeather(temp = 20.0, condition = WeatherCondition.CLEAR, uvIndex = uv)

    @Test
    fun `cada tramo cae donde toca`() {
        assertEquals(TempBand.FREEZING, outfitFor(day(-1.0), current()).band)
        assertEquals(TempBand.COLD, outfitFor(day(0.0), current()).band)
        assertEquals(TempBand.COLD, outfitFor(day(7.9), current()).band)
        assertEquals(TempBand.COOL, outfitFor(day(8.0), current()).band)
        assertEquals(TempBand.MILD, outfitFor(day(14.0), current()).band)
        assertEquals(TempBand.WARM, outfitFor(day(20.0), current()).band)
        assertEquals(TempBand.HOT, outfitFor(day(26.0), current()).band)
    }

    @Test
    fun `el viento baja de tramo y se nota en la sensacion`() {
        // 21° en calma es WARM; con 60 km/h el techo de 4° lo deja en 17 y pasa a MILD.
        val calm = outfitFor(day(21.0), current())
        val windy = outfitFor(day(21.0, windKmh = 60.0), current())
        assertEquals(TempBand.WARM, calm.band)
        assertEquals(TempBand.MILD, windy.band)
        assertEquals(21, calm.feelsLike)
        assertEquals(17, windy.feelsLike)
    }

    @Test
    fun `lluvia con calma lleva paraguas, con viento lleva chubasquero`() {
        val calm = outfitFor(day(16.0, precipProbability = 60), current())
        assertTrue(Garment.UMBRELLA in calm.garments)
        assertFalse(Garment.RAINCOAT in calm.garments)

        val windy = outfitFor(day(16.0, windKmh = 40.0, precipProbability = 60), current())
        assertTrue(Garment.RAINCOAT in windy.garments)
        assertFalse(
            "con viento fuerte el paraguas no debe recomendarse",
            Garment.UMBRELLA in windy.garments
        )
    }

    @Test
    fun `la lluvia fuerte cambia el calzado ligero por botas`() {
        val advice = outfitFor(day(18.0, precipitationMm = 12.0), current())
        assertTrue(Garment.BOOTS in advice.garments)
        assertFalse(Garment.SNEAKERS in advice.garments)
        // Y el chubasquero sustituye al paraguas en vez de sumarse.
        assertTrue(Garment.RAINCOAT in advice.garments)
        assertFalse(Garment.UMBRELLA in advice.garments)
    }

    @Test
    fun `la nieve manda sobre el tramo`() {
        // 3° cae en COLD, que ya trae botas; lo que aporta la nieve es gorro y guantes.
        val advice = outfitFor(day(3.0, condition = WeatherCondition.SNOW), current())
        assertTrue(Garment.BEANIE in advice.garments)
        assertTrue(Garment.GLOVES in advice.garments)
        assertFalse(Garment.SNEAKERS in advice.garments)
    }

    @Test
    fun `el UV va por su cuenta, no con la temperatura`() {
        // Día frío pero de UV alto: tiene que salir protección solar igualmente.
        val coldAndSunny = outfitFor(day(5.0), current(uv = 7.0))
        assertEquals(TempBand.COLD, coldAndSunny.band)
        assertTrue(Garment.SUNSCREEN in coldAndSunny.garments)
        assertTrue(Garment.CAP in coldAndSunny.garments)
        assertTrue(Garment.SUNGLASSES in coldAndSunny.garments)

        // UV moderado: solo gafas.
        val moderate = outfitFor(day(22.0), current(uv = 4.0))
        assertTrue(Garment.SUNGLASSES in moderate.garments)
        assertFalse(Garment.SUNSCREEN in moderate.garments)

        // Sin dato de UV no se inventa protección.
        val unknown = outfitFor(day(22.0), current(uv = null))
        assertFalse(Garment.SUNGLASSES in unknown.garments)
    }

    @Test
    fun `mucha diferencia entre minima y maxima avisa de ir por capas`() {
        // Sin viento, sin lluvia y sin UV, el de capas es el único aviso posible.
        val wide = outfitFor(day(24.0, tempMin = 8.0), current())
        val narrow = outfitFor(day(24.0, tempMin = 20.0), current())
        assertEquals(1, wide.tipsRes.size)
        assertEquals(0, narrow.tipsRes.size)
    }

    @Test
    fun `las prendas no se repiten y salen en el orden en el que uno se viste`() {
        // Nieve con lluvia fuerte pide botas por dos reglas distintas.
        val advice = outfitFor(
            day(1.0, precipitationMm = 10.0, condition = WeatherCondition.SNOW),
            current(uv = 7.0)
        )
        assertEquals(advice.garments.distinct(), advice.garments)
        assertEquals(advice.garments.sortedBy { it.ordinal }, advice.garments)
    }
}
