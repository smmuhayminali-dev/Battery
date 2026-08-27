plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.batterywidgets"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.batterywidgets"
        // Runs on Android 5.0 (Lollipop) and up — covers essentially every active device.
        minSdk = 21
        // Built and optimized against the latest SDK (covers Android 14/15 fully).
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Deliberately minimal — no heavy libraries, keeps the APK small and avoids
    // background work / extra services that would drain battery.
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
