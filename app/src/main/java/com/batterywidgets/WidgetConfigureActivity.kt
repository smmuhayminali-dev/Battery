package com.batterywidgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Switch

/**
 * Shown automatically by the launcher the moment the user drags the widget
 * onto their home screen (declared via android:configure in
 * battery_widget_info.xml), and again if they tap the widget afterwards.
 * Pure SharedPreferences reads/writes — no network, no background work.
 */
class WidgetConfigureActivity : Activity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // If the widget host cancels (back button) before Save is tapped,
        // the widget placement is cancelled too — standard widget-config UX.
        setResult(Activity.RESULT_CANCELED)

        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val prefs = WidgetPrefs(this, widgetId)
        val themeGroup = findViewById<RadioGroup>(R.id.theme_group)
        val showIconSwitch = findViewById<Switch>(R.id.show_icon_switch)
        val saveButton = findViewById<Button>(R.id.save_button)

        // Pre-fill with whatever's already saved (defaults on first run).
        when (prefs.themeMode) {
            WidgetPrefs.THEME_LIGHT -> themeGroup.check(R.id.theme_light)
            WidgetPrefs.THEME_DARK -> themeGroup.check(R.id.theme_dark)
            else -> themeGroup.check(R.id.theme_auto)
        }
        showIconSwitch.isChecked = prefs.showIcon

        saveButton.setOnClickListener {
            prefs.themeMode = when (themeGroup.checkedRadioButtonId) {
                R.id.theme_light -> WidgetPrefs.THEME_LIGHT
                R.id.theme_dark -> WidgetPrefs.THEME_DARK
                else -> WidgetPrefs.THEME_AUTO
            }
            prefs.showIcon = showIconSwitch.isChecked

            // Push an immediate refresh so the widget reflects the new
            // settings right away, without waiting for the next battery
            // broadcast.
            val updateIntent = Intent(this, BatteryWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
            }
            sendBroadcast(updateIntent)

            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }
}
