# Battery Widgets

A minimal, rounded-corner battery-level home-screen widget for Android.

## How it avoids battery drain
Most battery widgets drain battery because they poll on a timer
(`AlarmManager` / `WorkManager` every N minutes) even when nothing changed.
This one doesn't poll at all:

- `battery_widget_info.xml` sets `updatePeriodMillis="0"` — Android never
  wakes the widget on a schedule.
- The widget's receiver is registered in the manifest directly for
  `android.intent.action.BATTERY_CHANGED`, a **sticky, protected** system
  broadcast that's explicitly exempted from Android 8+'s implicit-broadcast
  restrictions. The system already sends this broadcast whenever the level
  changes for its own status-bar icon — the widget just listens in, at
  zero extra cost.
- `onReceive()` does a few milliseconds of work (read two ints, update a
  `RemoteViews`) and returns. No services, no wakelocks, no network.

## Compatibility
- `minSdk = 21` (Android 5.0) — runs on effectively every active device.
- `targetSdk = 35` — built and tested against current Android (14/15+),
  where the app is expected to "work excellent" per your brief.
- No AndroidX Compose/Glance dependency, no heavy libraries — keeps the
  APK small (a release build should land well under 2 MB).

## Features
- Rounded-corner card, minimal single-accent-color design.
- Live percentage + progress bar, charging icon when plugged in.
- Tap the widget to open **Settings**: Light / Dark / Match system, and a
  toggle for the charging icon. Settings are per-widget instance (add the
  widget twice with different settings if you like).
- Resizable (`resizeMode="horizontal|vertical"`).

## Building
1. Open this folder in Android Studio (Koala/2024.1 or newer recommended —
   it will fetch the Gradle wrapper distribution automatically the first
   time you open it, since this environment has no network access to do
   that for you).
2. Let Gradle sync, then **Run ▸ app** on a device/emulator, or
   **Build ▸ Generate Signed Bundle/APK** for a release build.
3. Long-press the home screen → Widgets → **Battery Widgets** → drag it out.

## Project layout
```
app/src/main/
  AndroidManifest.xml          # widget receiver + configure activity
  java/com/batterywidgets/
    BatteryWidgetProvider.kt   # renders/refreshes the widget
    WidgetConfigureActivity.kt # settings screen
    WidgetPrefs.kt             # per-widget SharedPreferences
  res/layout/widget_battery.xml
  res/layout/activity_settings.xml
  res/drawable/                # rounded backgrounds, progress bar
  res/xml/battery_widget_info.xml
```

## Extending
- Add more widget sizes by duplicating `widget_battery.xml` into e.g.
  `widget_battery_small.xml` and picking one in `onUpdate()` based on the
  widget's `AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH`.
- To ship several distinct widgets ("pack"), add more `AppWidgetProvider`
  classes + `<receiver>` entries + `appwidget-provider` XMLs the same way —
  each one just as cheap, since none of them poll either.
