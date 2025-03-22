pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        // Adding JitPack to pluginManagement to ensure any plugins from JitPack can also be used.
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)  // Prefer using settings for dependency resolution.
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // Ensuring JitPack is included for all project dependencies.
    }
}

rootProject.name = "PowerSaver"
include(":app")
