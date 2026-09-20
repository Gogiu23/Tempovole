package com.example.tiempo.data

import android.content.Context

/** Cómo se pinta el fondo del widget de pantalla de inicio. */
enum class WidgetBackground(val label: String, val description: String) {
    IMAGE(
        "Imagen",
        "Usa la misma foto de fondo que la app, según la franja del día."
    ),
    TRANSPARENT(
        "Transparente",
        "Sin fondo: se ve el fondo de pantalla de tu móvil a través del widget."
    ),
    COLOR(
        "Color fijo",
        "Un color sólido a tu elección."
    )
}

/** Colores disponibles cuando el fondo del widget es de tipo [WidgetBackground.COLOR]. */
enum class WidgetColor(val label: String, val argb: Int) {
    NAVY("Azul noche", 0xFF1E3A5F.toInt()),
    SLATE("Gris pizarra", 0xFF2F3640.toInt()),
    FOREST("Verde bosque", 0xFF20503C.toInt()),
    PLUM("Morado", 0xFF3F2B56.toInt()),
    CRIMSON("Rojo oscuro", 0xFF5C2230.toInt()),
    BLACK("Negro", 0xFF101114.toInt())
}

/** Ajustes del widget, guardados solo en el dispositivo. */
object WidgetPreferences {
    private const val PREFS = "widget_prefs"
    private const val KEY_BACKGROUND = "background"
    private const val KEY_COLOR = "color"

    fun getBackground(context: Context): WidgetBackground {
        val stored = prefs(context).getString(KEY_BACKGROUND, null) ?: return WidgetBackground.IMAGE
        return runCatching { WidgetBackground.valueOf(stored) }.getOrDefault(WidgetBackground.IMAGE)
    }

    fun setBackground(context: Context, background: WidgetBackground) {
        prefs(context).edit().putString(KEY_BACKGROUND, background.name).apply()
    }

    fun getColor(context: Context): WidgetColor {
        val stored = prefs(context).getString(KEY_COLOR, null) ?: return WidgetColor.NAVY
        return runCatching { WidgetColor.valueOf(stored) }.getOrDefault(WidgetColor.NAVY)
    }

    fun setColor(context: Context, color: WidgetColor) {
        prefs(context).edit().putString(KEY_COLOR, color.name).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
