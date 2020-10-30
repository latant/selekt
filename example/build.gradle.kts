plugins {
    kotlin("jvm")
    kotlin("kapt")
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(project(":core"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.3")
    kapt(project(":generator"))
}