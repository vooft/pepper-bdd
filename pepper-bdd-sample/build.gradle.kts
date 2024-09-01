plugins {
    `pepper-bdd-base`

    // dependency is already added in the root build.gradle.kts
    // without it the `pepper` accessor is not generated
    id("io.github.vooft.pepper-bdd")
}

pepper {
}

dependencies {
    testImplementation(libs.kotest.runner)
}

configurations.configureEach {
    resolutionStrategy.dependencySubstitution {
        substitute(module("io.github.vooft:pepper-bdd-compiler-plugin"))
            .using(project(":pepper-bdd-compiler-plugin"))
    }
}
