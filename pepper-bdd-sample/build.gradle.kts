plugins {
    `pepper-bdd-jvm`

    // dependency is already added in the root build.gradle.kts
    // without it the `pepper` accessor is not generated
    id("io.github.vooft.pepper-bdd")
}

dependencies {
    testImplementation(project(":pepper-bdd-core"))
    testImplementation(project(":pepper-bdd-reports:pepper-bdd-reports-extension"))
    testImplementation(libs.kotest.runner.jvm)
    testImplementation(libs.kotlinx.serialization.json)
}

configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("io.github.vooft:pepper-bdd-compiler-plugin"))
            .using(project(":pepper-bdd-compiler-plugin"))
    }
}
