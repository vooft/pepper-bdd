plugins {
    `pepper-bdd-jvm`
    `pepper-bdd-publish`
}

dependencies {
    implementation(libs.kotlin.compiler.embeddable)

    testImplementation(libs.kotest.runner.jvm)
    testImplementation(libs.kotlin.compile.testing)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
    }
}
