plugins {
    `pepper-bdd-base`
    `pepper-bdd-publish`
    id("java-gradle-plugin")
}

gradlePlugin {
    plugins {
        create("pepperPlugin") {
            id = "io.github.vooft.pepper-bdd"
            implementationClass = "io.github.vooft.pepper.gradle.PepperBddGradleSubPlugin"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin.api)
}
