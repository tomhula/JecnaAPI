import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import gradle.kotlin.dsl.accessors._b20fb59bd10f42a048b449e6e24bcfd1.kotlin
import gradle.kotlin.dsl.accessors._b20fb59bd10f42a048b449e6e24bcfd1.mavenPublishing
import org.gradle.jvm.tasks.Jar

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("java-library")
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
}

kotlin {
    jvmToolchain(21)
}

val dokkaJecnaJavadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.named("dokkaJavadoc"))
    from(layout.buildDirectory.dir("dokka/javadoc"))
    archiveClassifier.set("javadoc")
}

val dokkaJecnaHtmlJar  by tasks.registering(Jar::class)  {
    dependsOn(tasks.named("dokkaHtml"))
    from(layout.buildDirectory.dir("dokka/html"))
    archiveClassifier.set("html-doc")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(group.toString(), rootProject.name, version.toString())
    configure(KotlinJvm(
        javadocJar = JavadocJar.Dokka(dokkaJecnaJavadocJar),
        sourcesJar = true
    ))

    pom {
        name.set("JecnaAPI")
        description.set("A library to access data from the SPSE Jecna web.")
        inceptionYear.set("2023")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/license/mit")
                distribution.set("https://opensource.org/license/mit")
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
            developerConnection.set("scm:git:ssh://git@github.com/username/mylibrary.git")
        }
    }
}
