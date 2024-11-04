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
        jvmTest.dependencies {
            implementation(kotlin("reflect"))
            implementation(libs.kotest.runner.jvm)
            implementation(libs.kotest.assertions.json)
        }
    }
}
