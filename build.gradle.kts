plugins {
    `pepper-bdd-base`
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("io.github.vooft:pepper-bdd-gradle")
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}

tasks.register("pepperPublishToMavenCentral") {
    dependsOn(
        subprojects.mapNotNull { it.tasks.findByName("publishAndReleaseToMavenCentral") } +
            gradle.includedBuilds.map { it.task(":publishAndReleaseToMavenCentral") }
    )
}

tasks.register("pepperPublishToMavenLocal") {
    dependsOn(
        subprojects.mapNotNull { it.tasks.findByName("publishToMavenLocal") } +
            gradle.includedBuilds.map { it.task(":publishToMavenLocal") }

    )
}
