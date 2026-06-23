plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
    id("com.gradleup.shadow") version "8.3.5"
}

application {
    mainClass.set("dev.genesis.cli.MainKt")
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("genesis")
    archiveClassifier.set("")
    archiveVersion.set("")
    mergeServiceFiles()
}

dependencies {
    implementation(project(":genesis-core"))
    implementation(project(":genesis-server"))
    implementation(project(":genesis-assets"))
    implementation(project(":genesis-search"))
    implementation(project(":genesis-i18n"))
    implementation(project(":genesis-islands"))

    implementation("com.github.ajalt.clikt:clikt:5.0.2")
    implementation("com.github.ajalt.mordant:mordant:3.0.1")
}
