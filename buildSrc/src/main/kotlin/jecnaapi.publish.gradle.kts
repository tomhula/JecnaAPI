import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.SourcesJar

plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)

    // Dynamically configure the publishing format based on the applied Kotlin plugin
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        configure(KotlinMultiplatform(sourcesJar = SourcesJar.Sources()))
    }
    plugins.withId("org.jetbrains.kotlin.jvm") {
        configure(KotlinJvm(sourcesJar = SourcesJar.Sources()))
    }

    pom {
        name.set("JecnaAPI")
        description.set("A library to access data from the SPSE Jecna web.")
        inceptionYear.set("2023")
        url.set("https://github.com/tomhula/JecnaAPI/")
        licenses {
            license {
                name.set("GNU GPLv3")
                url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                distribution.set("https://www.gnu.org/licenses/gpl-3.0.html")
            }
        }
        developers {
            developer {
                id.set("tomhula")
                name.set("Tomáš Hůla")
                email.set("tomashula06@gmail.com")
                url.set("https://tomashula.cz")
            }
        }
        scm {
            url.set("https://github.com/tomhula/JecnaAPI/")
            connection.set("scm:git:git://github.com/tomhula/JecnaAPI.git")
            developerConnection.set("scm:git:git://github.com/tomhula/JecnaAPI.git")
        }
    }
}
