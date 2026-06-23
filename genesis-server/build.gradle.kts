plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(project(":genesis-core"))

    implementation("io.ktor:ktor-server-core:3.0.2")
    implementation("io.ktor:ktor-server-cio:3.0.2")
    implementation("io.ktor:ktor-server-websockets:3.0.2")
    implementation("io.ktor:ktor-server-html-builder:3.0.2")
    implementation("io.ktor:ktor-server-status-pages:3.0.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}
