plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(plugin(libs.plugins.kotlin.jvm))
    implementation(plugin(libs.plugins.dokka))
    implementation(plugin(libs.plugins.maven.publish))
}

// https://stackoverflow.com/a/79594463/9949986
// Transforms a Gradle Plugin alias from a Version Catalog
// into a valid dependency notation for buildSrc.
fun plugin(plugin: Provider<PluginDependency>): Provider<String> =
    plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
