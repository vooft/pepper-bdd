plugins {
    `pepper-bdd-base`
    `pepper-bdd-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.compiler.embeddable)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
    }
}
