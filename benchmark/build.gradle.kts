plugins {
    alias(libs.plugins.android.test)
}

val targetApplicationId = "io.github.winschneid.graphcompare"

android {
    namespace = "io.github.winschneid.graphcompare.benchmark"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true

    // :app と同じ flavor を持たせ、flavor ごとに計測する（例: connectedMp3BenchmarkAndroidTest）
    flavorDimensions += "chart"
    productFlavors {
        listOf("mp3", "mp4", "vico").forEach { flavor ->
            create(flavor) {
                dimension = "chart"
                testInstrumentationRunnerArguments["targetAppId"] = "$targetApplicationId.$flavor"
            }
        }
    }

    buildTypes {
        create("benchmark") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

androidComponents {
    beforeVariants(selector().all()) { variant ->
        variant.enable = variant.buildType == "benchmark"
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.test.ext.junit)
}
