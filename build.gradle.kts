plugins {
    id("jecnaapi.module")
    alias(libs.plugins.plugin.serialization)
}

group = "io.github.tomhula"
version = project.properties["version"] ?: "SNAPSHOT"

allprojects {
    group = rootProject.group
    version = rootProject.version
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.jsoup)
    api(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)

    // Debugging only
    //implementation("io.ktor:ktor-client-logging-jvm:2.2.4")

    testImplementation(kotlin("test"))
}
