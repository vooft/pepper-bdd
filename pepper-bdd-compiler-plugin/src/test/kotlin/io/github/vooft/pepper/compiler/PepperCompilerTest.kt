package io.github.vooft.pepper.compiler

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.File

class PepperCompilerTest : FunSpec({
    test("single scenario") {
        val result = compile(
            sourceFiles = sharedSourceFiles + SourceFile.fromPath(
                File("../pepper-bdd-sample/src/test/kotlin/io/github/vooft/pepper/sample/SimplePepperSpec.kt")
            )
        )

        println(result.generatedFiles.joinToString("\n"))

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    test("two scenarios") {
        val result = compile(
            sourceFiles = sharedSourceFiles + SourceFile.fromPath(
                File("../pepper-bdd-sample/src/test/kotlin/io/github/vooft/pepper/sample/TwoScenariosSpec.kt")
            )
        )

        println(result.generatedFiles.joinToString("\n"))

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    test("scenario with examples") {
        val result = compile(
            sourceFiles = sharedSourceFiles + SourceFile.fromPath(
                File("../pepper-bdd-sample/src/test/kotlin/io/github/vooft/pepper/sample/ExamplesSpec.kt")
            )
        )

        println(result.generatedFiles.joinToString("\n"))

        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
})

private val sharedSourceFiles = listOf(
    SourceFile.fromPath(
        File("../pepper-bdd-sample/src/test/kotlin/io/github/vooft/pepper/sample/Steps.kt")
    ),
    SourceFile.fromPath(
        File("../pepper-bdd-core/src/main/kotlin/io/github/vooft/pepper/dsl/PepperSpecDsl.kt")
    ),
    SourceFile.fromPath(
        File("../pepper-bdd-core/src/main/kotlin/io/github/vooft/pepper/dsl/PepperSpecDslImpl.kt")
    ),
    SourceFile.fromPath(
        File("../pepper-bdd-core/src/main/kotlin/io/github/vooft/pepper/PepperSpec.kt")
    ),
    SourceFile.fromPath(
        File("../pepper-bdd-core/src/main/kotlin/io/github/vooft/pepper/Internals.kt")
    ),
    SourceFile.fromPath(
        File("../pepper-bdd-core/src/main/kotlin/io/github/vooft/pepper/helper/PluginHelpers.kt")
    ),
    SourceFile.fromPath(
        File("../pepper-bdd-reports/pepper-bdd-reports-builder/src/main/kotlin/io/github/vooft/pepper/reports/builder/BuilderElements.kt")
    ),
    SourceFile.fromPath(
        File(
            "../pepper-bdd-reports/pepper-bdd-reports-builder/src/main/kotlin/io/github/vooft/pepper/reports/builder/PepperReportBuilder.kt"
        )
    ),
)

fun compile(sourceFiles: List<SourceFile>, plugin: CompilerPluginRegistrar = PepperBddComponentRegistrar()): JvmCompilationResult =
    KotlinCompilation().apply {
        sources = sourceFiles
        compilerPluginRegistrars = listOf(plugin)
        inheritClassPath = true
    }.compile()

fun compile(sourceFile: SourceFile, plugin: CompilerPluginRegistrar = PepperBddComponentRegistrar()): JvmCompilationResult =
    compile(listOf(sourceFile), plugin)
