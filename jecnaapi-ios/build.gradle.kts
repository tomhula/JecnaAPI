plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":jecnaapi-jecna"))
            api(project(":jecnaapi-canteen"))
        }
    }

    cocoapods {
        name = "JecnaapiIOS"
        version = project.version.toString()
        summary = "JecnaAPI umbrella framework for iOS"
        homepage = "https://github.com/tomhula/JecnaAPI"
        license = "GNU GPLv3"
        framework {
            baseName = "JecnaapiIOS"
            isStatic = true
            export(project(":jecnaapi-jecna"))
            export(project(":jecnaapi-canteen"))
            transitiveExport = true
        }
    }
}
