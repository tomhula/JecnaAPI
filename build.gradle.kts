plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.plugin.serialization)
    alias(libs.plugins.dokka)
    `java-library`
    `maven-publish`
}

group = "me.tomasan7"
version = "5.1.1"

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

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")

    dependencies {
        /* Kotlin standard library */
        implementation(kotlin("stdlib"))
        /* Align versions of all Kotlin components. */
        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    }

    group = rootProject.group
    version = rootProject.version

    tasks {
        val checkMainDoesntExist by registering {
            doLast {
                sourceSets.main.get().allSource.forEach { file ->
                    if (file.name in setOf("Main.kt", "Main.java"))
                        throw IllegalStateException("Main file found: $file")
                }
            }
        }
        publishToMavenLocal {
            dependsOn(clean, test, checkMainDoesntExist)
        }
    }

    java {
        withSourcesJar()
    }

    kotlin {
        jvmToolchain(21)
    }

    // https://kotlinlang.org/docs/dokka-gradle.html#build-javadoc-jar

    val dokkaJavadocJar by tasks.registering(Jar::class) {
        dependsOn(tasks.dokkaJavadoc)
        from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
        archiveClassifier.set("javadoc")
    }

    val dokkaHtmlJar by tasks.registering(Jar::class) {
        dependsOn(tasks.dokkaHtml)
        from(tasks.dokkaHtml.flatMap { it.outputDirectory })
        archiveClassifier.set("html-doc")
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                artifact(dokkaJavadocJar)
                artifact(dokkaHtmlJar)

                pom {
                    name.set("JecnaAPI")
                    description.set("A library to access data from the SPSE Jecna web.")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://www.opensource.org/licenses/mit-license.php")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("Tomasan7")
                            name.set("Tomáš Hůla")
                            email.set("tomashula06@gmail.com")
                        }
                    }
                }
            }
        }
    }
}
