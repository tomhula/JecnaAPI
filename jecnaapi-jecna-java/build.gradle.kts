plugins {
    kotlin("jvm")
    id("jecnaapi.publish")
}

dependencies {
    api(project(":jecnaapi-jecna"))
    implementation(libs.kotlinx.coroutines.core)
}
