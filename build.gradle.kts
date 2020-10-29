import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("kapt") version "1.4.10"
    maven
}

group = "com.github.latant"
version = "0.1.1"

allprojects {
    repositories {
        mavenCentral()
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}