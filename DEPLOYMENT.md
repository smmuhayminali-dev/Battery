# Battery Widgets — Full Deployment Guide
From unzip on your laptop → Play Store listing live.

## PHASE 1 — Set up your laptop (one-time)
1. Download and install **Android Studio** (free): https://developer.android.com/studio
   - It bundles the JDK — you don't need to install Java separately.
2. Open Android Studio once, let it finish its own first-run setup
   (SDK Manager download). This needs internet and can take 10–20 min.

## PHASE 2 — Open the project
1. Unzip `BatteryWidgets.zip` anywhere on your laptop (e.g. Desktop).
2. Android Studio → **File → Open** → select the unzipped `BatteryWidgets` folder
   (the one containing `settings.gradle.kts`).
3. Wait for "Gradle sync" to finish (bottom status bar). First sync
   downloads the Gradle wrapper + dependencies — needs internet.
4. If sync fails on Gradle/AGP/Kotlin version mismatches, click the
   "Update" links Android Studio suggests — it self-heals these.

## PHASE 3 — Test it works
1. Connect an Android phone via USB with **USB debugging** enabled
   (Settings → About phone → tap "Build number" 7 times → Developer
   options → USB debugging), OR create a virtual device: **Device
   Manager → Create device** in Android Studio.
2. Click the green ▶ Run button, select your device.
3. On the phone: long-press home screen → Widgets → find "Battery
   Widgets" → drag it out. Confirm it shows battery % and updates when
   you plug/unplug the charger.

## PHASE 4 — Prepare for release
1. **Replace the placeholder icon** — the one I generated is a plain
   vector glyph, not a polished app icon. Right-click `res` folder →
   **New → Image Asset** → pick your own image → this regenerates all
   `mipmap` sizes properly. Do this before publishing.
2. **Pick your final package name** (`applicationId` in
   `app/build.gradle.kts`, currently `com.batterywidgets`). This is
   permanent once published — you cannot change it later. If you want
   something unique to you, edit it now, e.g. `com.yourname.batterywidgets`.
3. **Bump `versionCode`/`versionName`** in `app/build.gradle.kts` if you
   make changes later — every Play Store upload needs a higher versionCode
   than the last.

## PHASE 5 — Create a signing key (required for release builds)
Run this once, in a terminal, from anywhere. Keep the resulting file and
password **forever** — losing it means you can never update the app again
under the same listing.

```
keytool -genkeypair -v -storetype PKCS12 -keystore battery-widgets-release.jks -alias battery-widgets -keyalg RSA -keysize 2048 -validity 10000
```

- It asks for a keystore password, your name, org, country, etc. — answer
  honestly but none of it is publicly shown.
- Store `battery-widgets-release.jks` somewhere safe (not inside the
  project folder, so it's never accidentally committed/shared) and back
  it up (e.g. a password manager or encrypted drive).

## PHASE 6 — Wire the signing key into the project
1. In the project root, create `keystore.properties` (same folder as
   `settings.gradle.kts`) with:
   ```
   storeFile=/absolute/path/to/battery-widgets-release.jks
   storePassword=YOUR_STORE_PASSWORD
   keyAlias=battery-widgets
   keyPassword=YOUR_KEY_PASSWORD
   ```
2. Add `keystore.properties` to a `.gitignore` if you use git — never
   share or commit this file.
3. In `app/build.gradle.kts`, add above the `android {` block:
   ```kotlin
   import java.util.Properties
   val keystoreProps = Properties().apply {
       load(rootProject.file("keystore.properties").inputStream())
   }
   ```
   and inside `android { ... }` add:
   ```kotlin
   signingConfigs {
       create("release") {
           storeFile = file(keystoreProps["storeFile"] as String)
           storePassword = keystoreProps["storePassword"] as String
           keyAlias = keystoreProps["keyAlias"] as String
           keyPassword = keystoreProps["keyPassword"] as String
       }
   }
   buildTypes {
       release {
           signingConfig = signingConfigs.getByName("release")
           isMinifyEnabled = true
           isShrinkResources = true
           proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
       }
   }
   ```

## PHASE 7 — Build the release bundle
Play Store requires an **.aab** (Android App Bundle), not a plain .apk.

- Android Studio menu: **Build → Generate Signed App Bundle / APK →
  Android App Bundle** → select your keystore/aliases → **release** →
  Finish.
- Output lands at `app/release/app-release.aab`.

## PHASE 8 — Google Play Console account (one-time, $25)
1. Go to https://play.google.com/console/signup
2. Sign in with a Google account, pay the one-time $25 registration fee,
   complete identity verification (can take a few hours to a couple of
   days).

## PHASE 9 — Create the app listing
In Play Console → **Create app**:
1. App name, default language, "App" type, "Free".
2. **Store listing**: short description (80 chars), full description (up
   to 4000 chars), app icon (512×512 PNG), feature graphic (1024×500),
   at least 2 phone screenshots (take these from your emulator/phone —
   Android Studio's Logcat panel has a screenshot button, or just use the
   phone's own screenshot function).
3. **Privacy policy URL** — required even for a simple widget with no
   data collection. Easiest free option: write one paragraph stating the
   app collects no personal data, host it as a free page (e.g. GitHub
   Pages, Google Sites, or a Google Doc set to "anyone with the link").
4. **App content** section: complete the Content rating questionnaire,
   Target audience, Data safety form (for this app: no data collected,
   since it only reads on-device battery status locally).
5. **App access**: mark it as not requiring special access (no login).

## PHASE 10 — Upload and release
1. **Production → Create new release** (or start with **Internal
   testing** to try it privately first — recommended for your first
   ever app).
2. Upload `app-release.aab`.
3. Add release notes (e.g. "Initial release").
4. Save → Review release → **Start rollout to Production**.

## PHASE 11 — Review and go live
- Google reviews new apps/developer accounts — can take anywhere from a
  few hours to a few days for a first submission.
- Once approved, it's live at
  `https://play.google.com/store/apps/details?id=<your applicationId>`
  and anyone can install it.

## After publishing
- Any future change = bump `versionCode`, rebuild the signed `.aab`,
  upload a new Production release.
- Keep `battery-widgets-release.jks` and its passwords backed up
  permanently — Play Console cannot recover a lost signing key for you.
