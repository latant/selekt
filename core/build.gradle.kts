plugins {
    kotlin("jvm")
}

val sourcesJar = task<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

artifacts {
    archives(sourcesJar)
}

dependencies {
    implementation(kotlin("stdlib"))
}
