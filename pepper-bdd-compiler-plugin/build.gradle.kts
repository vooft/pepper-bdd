plugins {
    `pepper-bdd-base`
    `pepper-bdd-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.compiler.embeddable)

    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotlin.compile.testing)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
    }
}
