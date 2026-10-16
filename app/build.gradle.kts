plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val baseApplicationId = "io.github.ksdev7091.graphcompare"

android {
    namespace = baseApplicationId
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = baseApplicationId
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    // MPAndroidChart 3.x と 4.x はパッケージ名が同じでクラスが重複するため、1 つの APK に同居できない。
    // flavor ごとに依存を分け、3 つの APK を並べてインストールできるよう applicationId も分ける。
    flavorDimensions += "chart"
    productFlavors {
        create("mp3") {
            dimension = "chart"
            applicationIdSuffix = ".mp3"
            manifestPlaceholders["appLabel"] = "Graph MP 3.2"
            buildConfigField("String", "CHART_LIBRARY", "\"MPAndroidChart ${libs.versions.mpandroidchart3.get()} (View)\"")
        }
        create("mp4") {
            dimension = "chart"
            applicationIdSuffix = ".mp4"
            manifestPlaceholders["appLabel"] = "Graph MP 4.0"
            buildConfigField("String", "CHART_LIBRARY", "\"MPAndroidChart ${libs.versions.mpandroidchart4.get()} (Compose)\"")
        }
        create("vico") {
            dimension = "chart"
            applicationIdSuffix = ".vico"
            manifestPlaceholders["appLabel"] = "Graph Vico"
            buildConfigField("String", "CHART_LIBRARY", "\"Vico ${libs.versions.vico.get()} (Compose)\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        // Macrobenchmark 用。release と同じ最適化をかけ、debug 署名でインストールできるようにする
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.profileinstaller)
    debugImplementation(libs.androidx.compose.ui.tooling)

    "mp3Implementation"(libs.mpandroidchart3)

    "mp4Implementation"(libs.mpandroidchart4.lib)
    "mp4Implementation"(libs.mpandroidchart4.compose)

    "vicoImplementation"(libs.vico.compose)
}
