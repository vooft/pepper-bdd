plugins {
    `pepper-bdd-base`
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.plugin.publish)
}

gradlePlugin {
    website.set("https://github.com/vooft/pepper-bdd")
    vcsUrl.set("https://github.com/vooft/pepper-bdd")
    plugins {
        create("pepperPlugin") {
            id = "io.github.vooft.pepper-bdd"
            implementationClass = "io.github.vooft.pepper.gradle.PepperBddGradleSubPlugin"
            displayName = "Pepper BDD plugin"
            description = "Pepper BDD testing library for Kotlin Gradle plugin"
            tags.set(listOf("bdd", "kotlin", "testing", "kotest"))
        }
    }
}

buildConfig {
    buildConfigField("String", "VERSION", "\"${project.version}\"")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin.api)
}
