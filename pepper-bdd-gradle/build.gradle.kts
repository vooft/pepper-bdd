plugins {
    `pepper-bdd-base`
    `pepper-bdd-publish`
    id("java-gradle-plugin")
    alias(libs.plugins.buildconfig)
}

gradlePlugin {
    plugins {
        create("pepperPlugin") {
            id = "io.github.vooft.pepper-bdd"
            implementationClass = "io.github.vooft.pepper.gradle.PepperBddGradleSubPlugin"
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
