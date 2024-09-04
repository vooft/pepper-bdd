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
        gradle.includedBuilds.map { it.task(":publishAndReleaseToMavenCentral") } +
            subprojects.mapNotNull { it.tasks.findByName("publishAndReleaseToMavenCentral") }
    )
}

tasks.register("pepperPublishToMavenLocal") {
    dependsOn(
        gradle.includedBuilds.map { it.task(":publishToMavenLocal") } +
            subprojects.mapNotNull { it.tasks.findByName("publishToMavenLocal") }
    )
}
