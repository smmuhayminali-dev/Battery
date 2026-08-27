package com.batterywidgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.widget.RemoteViews

/**
 * Draws / refreshes the battery widget.
 *
 * Design goal: zero battery drain from the widget itself.
 *  - updatePeriodMillis is 0 in battery_widget_info.xml, so Android never
 *    wakes this provider on a timer.
 *  - Instead, the manifest registers this receiver directly for
 *    ACTION_BATTERY_CHANGED (a sticky, protected broadcast the system
 *    already sends whenever the level changes — nothing extra is polled).
 *  - onReceive() below does a few milliseconds of work (read two ints from
 *    the incoming Intent, update RemoteViews) and returns. No services,
 *    no WorkManager, no wakelocks.
 */
class BatteryWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(
            android.content.ComponentName(context, BatteryWidgetProvider::class.java)
        )
        if (ids.isEmpty()) return

        when (intent.action) {
            Intent.ACTION_BATTERY_CHANGED,
            Intent.ACTION_BATTERY_LOW,
            Intent.ACTION_BATTERY_OKAY,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED,
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val percent = if (level >= 0 && scale > 0) (level * 100) / scale else readLevelDirect(context)
                val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

                for (id in ids) {
                    updateWidget(context, manager, id, percent, charging)
                }
            }
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        // Called once right after the widget is placed on the home screen.
        // We read the current battery state once (cheap, no polling loop)
        // just so the widget isn't blank until the next real change.
        val percent = readLevelDirect(context)
        val charging = readChargingDirect(context)
        for (id in ids) {
            updateWidget(context, manager, id, percent, charging)
        }
    }

    private fun readLevelDirect(context: Context): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun readChargingDirect(context: Context): Boolean {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.isCharging
    }

    private fun updateWidget(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
        percent: Int,
        charging: Boolean
    ) {
        val prefs = WidgetPrefs(context, widgetId)
        val views = RemoteViews(context.packageName, R.layout.widget_battery)

        views.setTextViewText(R.id.percent_text, "$percent%")
        views.setProgressBar(R.id.battery_bar, 100, percent.coerceIn(0, 100), false)
        views.setViewVisibility(
            R.id.charging_icon,
            if (charging && prefs.showIcon) android.view.View.VISIBLE else android.view.View.GONE
        )
        views.setTextColor(R.id.percent_text, prefs.resolveTextColor(context))

        // The fill color itself (green, defined once in battery_progress.xml)
        // is kept static on purpose — that's what "minimal, not fancy" means
        // here. The only thing that changes at runtime is the percentage,
        // the progress width, and (below) the light/dark background — both
        // are plain View#setBackgroundResource / #setTextColor calls, the
        // only kind of dynamic styling plain RemoteViews supports safely.
        views.setInt(R.id.widget_root, "setBackgroundResource", prefs.resolveBackgroundDrawableRes(context))

        // Opening the app's settings screen when the widget itself is tapped.
        val configIntent = Intent(context, WidgetConfigureActivity::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, widgetId, configIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.percent_text, pendingIntent)

        manager.updateAppWidget(widgetId, views)
    }
}
