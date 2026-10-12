package com.example.tiempo.ui

import com.example.tiempo.data.model.CurrentWeather
import com.example.tiempo.data.model.DayWeather
import com.example.tiempo.data.model.HourWeather
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

    private fun current(uv: Double? = null, temp: Double = 20.0) =
        CurrentWeather(temp = temp, condition = WeatherCondition.CLEAR, uvIndex = uv)

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

    // ---- Tramos del dia ----

    private fun hora(
        h: Int,
        temp: Double,
        isDay: Boolean = h in 8..19,
        windKmh: Double = 0.0,
        precipitationMm: Double = 0.0,
        precipProbability: Int? = 0,
        condition: WeatherCondition = WeatherCondition.CLEAR
    ) = HourWeather(
        time = LocalDateTime.of(2026, 10, 12, h, 0),
        temp = temp,
        precipProbability = precipProbability,
        precipitationMm = precipitationMm,
        windKmh = windKmh,
        condition = condition,
        isDay = isDay
    )

    /** Un dia entero: frio de madrugada, caluroso al mediodia. */
    private fun diaCompleto() = (0..23).map { h ->
        hora(h, temp = if (h in 9..17) 27.0 else if (h in 18..21) 18.0 else 6.0)
    }

    private fun hoy() = day(27.0, tempMin = 6.0).copy(date = LocalDate.of(2026, 10, 12))

    private fun aLas(h: Int, m: Int = 0) = LocalDateTime.of(2026, 10, 12, h, m)

    @Test
    fun `el bloque Ahora usa la temperatura actual, no la maxima del tramo`() {
        // Mediodia llega a 27, pero ahora mismo hacen 17. Es el fallo que se reporto:
        // ensenaba 24 estando a 17 porque miraba la maxima del tramo.
        val bloques = outfitByDayPart(hoy(), current(temp = 17.0), diaCompleto(), aLas(12))
        val ahora = bloques.single { it.isNow }
        assertEquals(null, ahora.part)
        assertEquals(17, ahora.temp)
        // Y decide con esa misma cifra: a 17° toca sudadera, no manga corta.
        assertTrue(Garment.HOODIE in ahora.advice.garments)
        assertFalse(Garment.SHORT_SLEEVE in ahora.advice.garments)
    }

    @Test
    fun `la ventana es el tramo de antes, ahora y los dos siguientes`() {
        val bloques = outfitByDayPart(hoy(), current(temp = 17.0), diaCompleto(), aLas(12))
        assertEquals(4, bloques.size)
        assertEquals(listOf(DayPart.MADRUGADA, null, DayPart.TARDE, DayPart.NOCHE),
            bloques.map { it.part })
        assertTrue("el de antes va atenuado", bloques.first().isPast)
        assertFalse(bloques.last().isPast)
    }

    @Test
    fun `de madrugada no hay tramo anterior y de noche no hay siguientes`() {
        val temprano = outfitByDayPart(hoy(), current(temp = 6.0), diaCompleto(), aLas(3))
        assertEquals(listOf(null, DayPart.MEDIODIA, DayPart.TARDE), temprano.map { it.part })

        val tarde = outfitByDayPart(hoy(), current(temp = 6.0), diaCompleto(), aLas(23))
        assertEquals(listOf(DayPart.TARDE, null), tarde.map { it.part })
    }

    @Test
    fun `los tramos con nombre siguen usando su propia maxima`() {
        val bloques = outfitByDayPart(hoy(), current(temp = 17.0), diaCompleto(), aLas(3))
        val mediodia = bloques.single { it.part == DayPart.MEDIODIA }
        assertEquals(27, mediodia.temp)
        assertTrue(Garment.SHORT_SLEEVE in mediodia.advice.garments)
    }

    @Test
    fun `el UV solo se aplica a los tramos mayoritariamente de dia`() {
        val bloques = outfitByDayPart(hoy(), current(uv = 8.0, temp = 20.0), diaCompleto(), aLas(3))
        val mediodia = bloques.single { it.part == DayPart.MEDIODIA }
        assertTrue("al mediodia hay luz", Garment.SUNSCREEN in mediodia.advice.garments)

        // A las 3 de la madrugada el bloque Ahora no debe pedir crema solar.
        val ahora = bloques.single { it.isNow }
        assertFalse("de madrugada no se pone uno crema solar", Garment.SUNSCREEN in ahora.advice.garments)
    }

    @Test
    fun `un tramo sin horas se omite en vez de inventarse`() {
        // Solo hay datos de mediodia en adelante.
        val parciales = (9..23).map { h -> hora(h, temp = 20.0) }
        val bloques = outfitByDayPart(hoy(), current(), parciales, aLas(12))
        // No hay madrugada que ensenar como tramo anterior.
        assertEquals(listOf(null, DayPart.TARDE, DayPart.NOCHE), bloques.map { it.part })
    }

    @Test
    fun `el viento y la lluvia del tramo van a lo peor de sus horas`() {
        // Una sola hora con ventolera basta para que el tramo pida chubasquero.
        val horas = (9..17).map { h ->
            hora(h, temp = 18.0, windKmh = if (h == 15) 45.0 else 5.0, precipProbability = 70)
        }
        val mediodia = outfitByDayPart(hoy(), current(), horas, aLas(3))
            .single { it.part == DayPart.MEDIODIA }
        assertTrue(Garment.RAINCOAT in mediodia.advice.garments)
        assertFalse(Garment.UMBRELLA in mediodia.advice.garments)
    }

    @Test
    fun `si nieva alguna hora, el tramo entero se viste de nieve`() {
        val horas = (0..17).map { h ->
            hora(h, temp = 2.0, condition = if (h == 5) WeatherCondition.SNOW else WeatherCondition.CLOUDY)
        }
        val madrugada = outfitByDayPart(hoy(), current(temp = 2.0), horas, aLas(12))
            .single { it.part == DayPart.MADRUGADA }
        assertTrue(Garment.BEANIE in madrugada.advice.garments)
        assertTrue(Garment.GLOVES in madrugada.advice.garments)
        assertFalse(Garment.SNEAKERS in madrugada.advice.garments)
    }
}
