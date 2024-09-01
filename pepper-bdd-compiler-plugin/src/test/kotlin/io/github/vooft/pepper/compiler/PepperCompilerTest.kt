package io.github.vooft.pepper.compiler

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.junit.jupiter.api.Assertions.assertEquals

class PepperCompilerTest : FunSpec({
    test("test") {
        // language=kotlin
        val spec = """
            package io.github.vooft.pepper.dsl

            interface PepperSpecDsl
        """.trimIndent()

        // language=kotlin
        val dsl = """
            package io.github.vooft.pepper

            open class PepperSpec : io.github.vooft.pepper.dsl.PepperSpecDsl {
                init {
                    Given
                    println("hello")
                }
            }

            val io.github.vooft.pepper.dsl.PepperSpecDsl.Given: Unit get() = error("bla")
            val io.github.vooft.pepper.dsl.PepperSpecDsl.When: Unit get() = error("bla")
            val io.github.vooft.pepper.dsl.PepperSpecDsl.Then: Unit get() = error("bla")
        """.trimIndent()

        val result = compile(
            sourceFiles = listOf(
                SourceFile.kotlin(
                    name = "spec.kt",
                    contents = spec
                ),
                SourceFile.kotlin(
                    name = "dsl.kt",
                    contents = dsl
                )
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
