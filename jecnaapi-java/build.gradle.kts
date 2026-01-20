plugins {
    id("jecnaapi.module")
}

kotlin {
    jvm()
    sourceSets {
        jvmMain.dependencies {
            implementation(rootProject)
        }
    }
}
