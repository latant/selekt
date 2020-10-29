import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
}

group = "com.github.latant"
version = "0.1.1"

dependencies {
    implementation(kotlin("stdlib"))

    implementation(project(":core"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.3")
    kapt(project(":generator"))
}