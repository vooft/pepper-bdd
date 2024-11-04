plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinx.serialization)
    `pepper-bdd-base`
    `pepper-bdd-publish`
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
            api(libs.kotlinx.serialization.json)
        }
    }
}
