package com.example.tiempo

import java.time.LocalTime

/**
 * Configuración global de la app.
 * La ubicación ya no es fija: se elige en Ajustes y se guarda con
 * [com.example.tiempo.data.LocationRepository] (por defecto, Barcelona).
 */
object UnsplashConfig {
    /** Franja del día que decide qué tipo de foto de fondo se pide. */
    enum class DayPeriod(val query: String) {
        MORNING("sunrise,morning,landscape"),
        AFTERNOON("landscape,nature,sky"),
        NIGHT("night sky,stars,moon");

        companion object {
            /** Mañana: 6:00–13:59 · Tarde: 14:00–20:59 · Noche: 21:00–5:59. */
            fun forTime(time: LocalTime = LocalTime.now()): DayPeriod = when (time.hour) {
                in 6..13 -> MORNING
                in 14..20 -> AFTERNOON
                else -> NIGHT
            }
        }
    }
}
