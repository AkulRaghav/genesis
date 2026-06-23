// Genesis documentation site - built using Genesis itself
// This module doesn't produce a JAR; it's a content project.
plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":genesis-cli"))
}
