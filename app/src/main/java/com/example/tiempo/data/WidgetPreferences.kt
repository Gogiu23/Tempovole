package com.example.tiempo.data

import android.content.Context
import androidx.annotation.StringRes
import com.example.tiempo.R

/** Cómo se pinta el fondo del widget de pantalla de inicio. */
enum class WidgetBackground(@StringRes val labelRes: Int, @StringRes val descriptionRes: Int) {
    IMAGE(R.string.widget_bg_image_label, R.string.widget_bg_image_description),
    TRANSPARENT(R.string.widget_bg_transparent_label, R.string.widget_bg_transparent_description),
    COLOR(R.string.widget_bg_color_label, R.string.widget_bg_color_description)
}

/** Colores disponibles cuando el fondo del widget es de tipo [WidgetBackground.COLOR]. */
enum class WidgetColor(@StringRes val labelRes: Int, val argb: Int) {
    NAVY(R.string.widget_color_navy_label, 0xFF1E3A5F.toInt()),
    SLATE(R.string.widget_color_slate_label, 0xFF2F3640.toInt()),
    FOREST(R.string.widget_color_forest_label, 0xFF20503C.toInt()),
    PLUM(R.string.widget_color_plum_label, 0xFF3F2B56.toInt()),
    CRIMSON(R.string.widget_color_crimson_label, 0xFF5C2230.toInt()),
    BLACK(R.string.widget_color_black_label, 0xFF101114.toInt())
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
