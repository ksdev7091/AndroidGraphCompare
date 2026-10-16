pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // MPAndroidChart は JitPack でのみ配布されている
        maven("https://jitpack.io") {
            content { includeGroupByRegex("com\\.github\\.PhilJay.*") }
        }
    }
}

rootProject.name = "AndroidGraphCompare"
include(":core")
include(":app")
include(":benchmark")
