rootProject.name = "jecnaapi"
include("jecnaapi-core", "jecnaapi-jecna", "jecnaapi-canteen", "jecnaapi-jecna-java")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}
