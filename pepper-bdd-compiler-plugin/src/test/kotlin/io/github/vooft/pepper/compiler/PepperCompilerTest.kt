package io.github.vooft.pepper.compiler

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.File

class PepperCompilerTest : FunSpec({
    test("test") {
        val result = compile(
            sourceFiles = listOf(
                SourceFile.fromPath(File("/Users/vooft/_code/pepper-bdd/pepper-bdd-sample/src/test/kotlin/io/github/vooft/pepper/sample/Steps.kt")),
                SourceFile.fromPath(File("/Users/vooft/_code/pepper-bdd/pepper-bdd-core/src/main/kotlin/io/github/vooft/pepper/dsl/PepperSpecDsl.kt")),
                SourceFile.fromPath(File("/Users/vooft/_code/pepper-bdd/pepper-bdd-core/src/main/kotlin/io/github/vooft/pepper/PepperSpec.kt")),
                SourceFile.fromPath(File("/Users/vooft/_code/pepper-bdd/pepper-bdd-sample/src/test/kotlin/io/github/vooft/pepper/sample/PepperSampleSpec.kt"))
            )
        )

        println(result.generatedFiles.joinToString("\n"))

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
})

fun compile(
    sourceFiles: List<SourceFile>,
    plugin: CompilerPluginRegistrar = PepperBddComponentRegistrar(),
): JvmCompilationResult {
    return KotlinCompilation().apply {
        sources = sourceFiles
        compilerPluginRegistrars = listOf(plugin)
        inheritClassPath = true
    }.compile()
}

fun compile(
    sourceFile: SourceFile,
    plugin: CompilerPluginRegistrar = PepperBddComponentRegistrar(),
): JvmCompilationResult {
    return compile(listOf(sourceFile), plugin)
}
