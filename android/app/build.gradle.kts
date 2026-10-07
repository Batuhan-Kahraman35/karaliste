import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.bivora.karaliste"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bivora.karaliste"
        minSdk = 29
        targetSdk = 35
        versionCode = 8
        versionName = "1.6.0"

        buildConfigField("String", "API_ADRES", "\"https://karaliste.bivora.com.tr/api/\"")
    }

    // İmza bilgileri git dışındaki android/keystore.properties dosyasından okunur
    val imzaDosyasi = rootProject.file("keystore.properties")
    if (imzaDosyasi.exists()) {
        val imza = Properties().apply { imzaDosyasi.inputStream().use { load(it) } }
        signingConfigs {
            create("release") {
                storeFile = rootProject.file(imza.getProperty("storeFile"))
                storePassword = imza.getProperty("storePassword")
                keyAlias = imza.getProperty("keyAlias")
                keyPassword = imza.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    testImplementation("junit:junit:4.13.2")
}
