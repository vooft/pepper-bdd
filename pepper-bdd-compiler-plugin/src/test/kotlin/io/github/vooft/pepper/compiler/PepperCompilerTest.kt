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
        val target = """
            fun main() {
                val valGiven = kotlin.run {
                    println("given")
                    testGiven()
                }
            
                val valWhen = kotlin.run {
                    println("when")
                    testWhen(valGiven)
                }
            }
            
            fun testGiven() = 1
            fun testWhen(param: Int)= param + 1
        """.trimIndent()

        // language=kotlin
        val source = """
            fun main() {
                val valGiven = testGiven()
           
                val valWhen = testWhen(valGiven)
            }
            
            fun testGiven() = 1
            fun testWhen(param: Int)= param + 1
        """.trimIndent()

        val result = compile(
            sourceFile = SourceFile.kotlin(
                name = "main.kt",
                contents = source
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
