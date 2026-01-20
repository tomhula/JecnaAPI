@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("jecnaapi.module")
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.plugin.serialization)
}

group = "io.github.tomhula"
version = providers.gradleProperty("version").getOrElse("SNAPSHOT")

allprojects {
    group = rootProject.group
    version = rootProject.version
}

kotlin {
    jvm()
    linuxX64()
    js {
        browser {
            testTask { enabled = false }
        }
    }
    wasmJs {
        browser {
            testTask { enabled = false }
        }
    }
    androidLibrary {
        namespace = group.toString() + project.name
        compileSdk = 36
        minSdk = 26
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.ksoup)
            api(libs.kotlinx.datetime)
            api(libs.ktor.client.core)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
        }
        jvmMain.dependencies {
            runtimeOnly(libs.ktor.client.engine.java)
        }
        nativeMain.dependencies {
            runtimeOnly(libs.ktor.client.engine.curl)
        }
        webMain.dependencies {
            runtimeOnly(libs.ktor.client.engine.js)
        }
        androidMain.dependencies {
            runtimeOnly(libs.ktor.client.engine.android)
        }
    }

    // Debugging only
    //implementation("io.ktor:ktor-client-logging-jvm:2.2.4")
}

tasks.named("publishToMavenCentral") {
    dependsOn(tasks.check)
}
