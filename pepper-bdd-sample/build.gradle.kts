plugins {
    `pepper-bdd-base`

    // dependency is already added in the root build.gradle.kts
    // without it the `pepper` accessor is not generated
    id("io.github.vooft.pepper-bdd")
}

pepper {
}
