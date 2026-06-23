plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(project(":genesis-plugin-api"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}
