import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("kapt") version "1.4.10"
}


allprojects {

    group = "com.github.latant"
    version = "0.1.1"

    repositories {
        mavenCentral()
    }

}

subprojects {

    apply(plugin = "maven")

}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}