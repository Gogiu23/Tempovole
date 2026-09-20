import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// Lee la API key de Unsplash desde local.properties (no se sube a git).
// Añade en local.properties:  UNSPLASH_ACCESS_KEY=tu_access_key
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val unsplashKey: String = localProps.getProperty("UNSPLASH_ACCESS_KEY", "")

// Firma de release: credenciales en keystore.properties (no se sube a git).
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.example.tiempo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.giuliandominici.tiempo"
        minSdk = 26
        targetSdk = 35
        // La versión NO se toca en el desarrollo del día a día, solo al sacar versión nueva.
        // Entonces: subir versionCode en 1 (Play lo exige) y versionName, y añadir la entrada
        // correspondiente en ui/Changelog.kt con ese mismo versionName. Si se olvida, la
        // pantalla de Novedades avisa de que van desincronizados.
        versionCode = 3
        versionName = "1.2"

        buildConfigField("String", "UNSPLASH_ACCESS_KEY", "\"$unsplashKey\"")
    }

    signingConfigs {
        if (keystoreProps.containsKey("storeFile")) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystoreProps.containsKey("storeFile")) {
                signingConfig = signingConfigs.getByName("release")
            }
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
    // AndroidX + Compose (BOM fija versiones compatibles entre sí)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Estado de Compose ligado al ciclo de vida + ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Red: Retrofit + kotlinx.serialization + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    // Imágenes (fondo de Unsplash)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Notificaciones diarias en segundo plano
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Widget de pantalla de inicio (Jetpack Glance)
    implementation("androidx.glance:glance-appwidget:1.1.1")

    // Solo para previsualizar composables en Android Studio
    debugImplementation("androidx.compose.ui:ui-tooling")
}
