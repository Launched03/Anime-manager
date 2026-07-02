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
    }
}

rootProject.name = "AnimeManager"
include(
    ":app",
    ":core:model",
    ":core:data",
    ":core:ui",
    ":feature:home",
    ":feature:library",
    ":feature:calendar",
    ":feature:profile",
    ":feature:detail",
    ":feature:edit",
)
