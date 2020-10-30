plugins {
    kotlin("jvm")
    maven
}

dependencies {
    implementation(kotlin("stdlib"))
}

val jar by tasks

artifacts {
    archives(jar)
}