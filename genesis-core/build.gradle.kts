plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(project(":genesis-plugin-api"))
    api(project(":genesis-markdown"))
    api(project(":genesis-templates"))
    api(project(":genesis-islands"))
    api(project(":genesis-search"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}
