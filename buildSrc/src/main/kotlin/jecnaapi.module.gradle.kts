import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.SourcesJar

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.vanniktech.maven.publish")
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    // Signing is explicitly enabled by CI with RELEASE_SIGNING_ENABLED property.
    // signAllPublications()
    // Deliberately not specifying coordinates, because at this point, project.group and project.version are not set yet.
    // If it is not specified, it will be taken automatically by this publish plugin
    configure(
        KotlinMultiplatform(sourcesJar = SourcesJar.Sources())
    )

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
