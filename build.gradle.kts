@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("jecnaapi.module")
    alias(libs.plugins.plugin.serialization)
}

group = "io.github.tomhula"
version = providers.gradleProperty("version").getOrElse("SNAPSHOT")

allprojects {
    group = rootProject.group
    version = rootProject.version
}

kotlin {
    linuxX64()
    jvm()
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ksoup)
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
        }
        nativeMain.dependencies {
            implementation(libs.ktor.client.curl)
        }
    }

    // Debugging only
    //implementation("io.ktor:ktor-client-logging-jvm:2.2.4")

    // testImplementation(kotlin("test"))
}

tasks.named("publishToMavenCentral") {
    dependsOn(tasks.check)
}
