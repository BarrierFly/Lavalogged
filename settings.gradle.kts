pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases")
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
}

stonecutter {
    create(rootProject) {
        versions("1.19.4", "1.20.1", "1.21.11", "26.3")
        vcsVersion = "1.20.1"
    }
}

rootProject.name = "lavalogged"
