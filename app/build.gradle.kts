import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    kotlin("plugin.serialization")
}

configurations.all {
    resolutionStrategy.force(
        "androidx.emoji2:emoji2:1.3.0",
        "androidx.emoji2:emoji2-views-helper:1.3.0"
    )
}

val appVersionCode = 9
val appVersionName = "1.4.1"

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
val hasReleaseKeystore = keystorePropertiesFile.exists()
val isReleaseTaskRequested = gradle.startParameter.taskNames.any { taskName ->
    taskName.contains("release", ignoreCase = true) || taskName.contains("bundle", ignoreCase = true)
}

if (hasReleaseKeystore) {
    keystorePropertiesFile.inputStream().use(keystoreProperties::load)
} else if (isReleaseTaskRequested) {
    error(
        "Release build harus selalu memakai keystore lama dari keystore.properties. " +
            "Jangan buat keystore baru agar signature APK tetap sama."
    )
}

if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use(localProperties::load)
}

fun String.toBuildConfigString(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.sajda.app"
    compileSdk = 36

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    defaultConfig {
        applicationId = "com.sajda.app"
        minSdk = 21
        targetSdk = 33
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resourceConfigurations += listOf(
            "id",
            "in",
            "en",
            "ar",
            "es",
            "de",
            "pt",
            "zh",
            "ja",
            "ko",
            "it",
            "pl",
            "uk",
            "sw",
            "tl",
            "tr",
            "ur",
            "fr",
            "ms",
            "hi"
        )
        buildConfigField(
            "String",
            "HADITH_API_KEY",
            localProperties.getProperty("hadith.api.key", "").toBuildConfigString()
        )
        buildConfigField(
            "String",
            "HADITH_API_BASE_URL",
            localProperties.getProperty(
                "hadith.api.baseUrl",
                "https://hadithapi.com/public/api"
            ).toBuildConfigString()
        )
        buildConfigField(
            "String",
            "DUA_CONTENT_URL",
            localProperties.getProperty(
                "dua.content.url",
                "https://raw.githubusercontent.com/wafaaelmaandy/Hisn-Muslim-Json/master/hisnmuslim.json"
            ).toBuildConfigString()
        )
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

android.applicationVariants.configureEach {
    if (buildType.name == "release") {
        outputs.configureEach {
            @Suppress("UNCHECKED_CAST")
            (this as com.android.build.gradle.internal.api.ApkVariantOutputImpl).outputFileName =
                "NurApp-v$appVersionName.apk"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-service:2.6.2")

    implementation("androidx.compose.foundation:foundation:1.5.4")
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.ui:ui-graphics:1.5.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    implementation("androidx.room:room-runtime:2.5.2")
    ksp("androidx.room:room-compiler:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")

    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("androidx.media3:media3-common:1.1.1")
    implementation("androidx.media3:media3-exoplayer:1.1.1")
    implementation("androidx.media3:media3-session:1.1.1")
    implementation("androidx.media3:media3-ui:1.1.1")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.5.4")

    // Hilt DI
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Retrofit & Network
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Navigation Compose & Serialization
    implementation("androidx.navigation:navigation-compose:2.8.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Coil Image Loading
    implementation("io.coil-kt:coil-compose:2.6.0")
}
