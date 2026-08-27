package com.batterywidgets

import android.content.Context
import android.content.res.Configuration

/**
 * Per-widget settings, stored in a tiny SharedPreferences file — no
 * database, no DataStore dependency. One widget on the home screen = one
 * small key set, so this stays fast and light no matter how many widgets
 * the user adds.
 */
class WidgetPrefs(context: Context, private val widgetId: Int) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var themeMode: String
        get() = prefs.getString(key("theme"), THEME_AUTO) ?: THEME_AUTO
        set(value) = prefs.edit().putString(key("theme"), value).apply()

    var showIcon: Boolean
        get() = prefs.getBoolean(key("show_icon"), true)
        set(value) = prefs.edit().putBoolean(key("show_icon"), value).apply()

    fun resolveBackgroundDrawableRes(context: Context): Int = when (themeMode) {
        THEME_LIGHT -> R.drawable.widget_background_light
        THEME_DARK -> R.drawable.widget_background_dark
        // "Auto" points at the plain widget_background resource, which has a
        // drawable-night/ variant — the system swaps it for us for free,
        // with no code, no listener, no work at all while idle.
        else -> R.drawable.widget_background
    }

    fun resolveTextColor(context: Context): Int {
        val dark = when (themeMode) {
            THEME_DARK -> true
            THEME_LIGHT -> false
            else -> isSystemInDarkMode(context)
        }
        return context.getColor(if (dark) R.color.text_dark else R.color.text_light)
    }

    fun clear() {
        prefs.edit()
            .remove(key("theme"))
            .remove(key("show_icon"))
            .apply()
    }

    private fun key(name: String) = "widget_${widgetId}_$name"

    companion object {
        private const val PREFS_NAME = "battery_widget_prefs"
        const val THEME_AUTO = "auto"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"

        // A single Configuration flag read — not a registered listener —
        // so this costs nothing while the widget is idle.
        fun isSystemInDarkMode(context: Context): Boolean {
            val flags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return flags == Configuration.UI_MODE_NIGHT_YES
        }
    }
}
