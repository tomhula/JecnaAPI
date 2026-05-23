@file:OptIn(ExperimentalWasmDsl::class)

import org.gradle.internal.execution.caching.CachingState.enabled
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.plugin.serialization)
    id("jecnaapi.publish")
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
        namespace = "io.github.tomhula.jecnaapi.jecna"
        compileSdk = 36
        minSdk = 26
    }
    sourceSets {
        commonMain.dependencies {
            api(project(":jecnaapi-core"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.encoding)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        jvmMain.dependencies {
            runtimeOnly(libs.ktor.client.engine.java)
        }
        nativeMain.dependencies {
            runtimeOnly(libs.ktor.client.engine.curl)
        }
        jsMain.dependencies {
            runtimeOnly(libs.ktor.client.engine.js)
        }
        wasmJsMain.dependencies {
            runtimeOnly(libs.ktor.client.engine.js)
        }
        androidMain.dependencies {
            runtimeOnly(libs.ktor.client.engine.android)
        }
    }
}
