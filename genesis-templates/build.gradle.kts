plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(project(":genesis-plugin-api"))

    implementation("io.pebbletemplates:pebble:3.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}
