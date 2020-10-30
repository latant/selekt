plugins {
    kotlin("jvm")
    kotlin("kapt")
    maven
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation(project(":core"))
    implementation("com.squareup:kotlinpoet:1.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.2.0")
    compileOnly("com.google.auto.service:auto-service:1.0-rc7")
    kapt("com.google.auto.service:auto-service:1.0-rc7")
}

val jar by tasks

artifacts {
    archives(jar)
}