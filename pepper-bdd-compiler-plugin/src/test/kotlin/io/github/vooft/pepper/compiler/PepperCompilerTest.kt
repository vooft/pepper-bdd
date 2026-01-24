package io.github.vooft.pepper.compiler

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import java.io.File

class PepperCompilerTest : FunSpec({
    test("single scenario") {
        val result = compile(
            sourceFiles = sharedSourceFiles + SourceFile.fromExisting(
                File("../pepper-bdd-sample/src/test/kotlin/io/github/vooft/pepper/sample/SimplePepperSpec.kt")
            )
        )

        println(result.generatedFiles.joinToString("\n"))

        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
    }

    test("two scenarios") {
        val result = compile(
            sourceFiles = sharedSourceFiles + SourceFile.fromExisting(
                File("../pepper-bdd-sample/src/test/kotlin/io/github/vooft/pepper/sample/TwoScenariosSpec.kt")
            )
        )

        println(result.generatedFiles.joinToString("\n"))

        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
    }

    test("scenario with examples") {
        val result = compile(
            sourceFiles = sharedSourceFiles + SourceFile.fromExisting(
                File("../pepper-bdd-sample/src/test/kotlin/io/github/vooft/pepper/sample/ExamplesSpec.kt")
            )
        )

        println(result.generatedFiles.joinToString("\n"))

        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
    }
})

private val sharedSourceFiles = listOf(
    SourceFile.fromExisting(
        File("../pepper-bdd-sample/src/test/kotlin/io/github/vooft/pepper/sample/Steps.kt")
    ),
    SourceFile.fromExisting(
        File("../pepper-bdd-core/src/main/kotlin/io/github/vooft/pepper/dsl/PepperSpecDsl.kt")
    ),
    SourceFile.fromExisting(
        File("../pepper-bdd-core/src/main/kotlin/io/github/vooft/pepper/dsl/PepperSpecDslImpl.kt")
    ),
    SourceFile.fromExisting(
        File("../pepper-bdd-core/src/main/kotlin/io/github/vooft/pepper/PepperSpec.kt")
    ),
    SourceFile.fromExisting(
        File("../pepper-bdd-core/src/main/kotlin/io/github/vooft/pepper/Internals.kt")
    ),
    SourceFile.fromExisting(
        File("../pepper-bdd-core/src/main/kotlin/io/github/vooft/pepper/helper/PluginHelpers.kt")
    ),
    SourceFile.fromExisting(
        File("../pepper-bdd-reports/pepper-bdd-reports-builder/src/main/kotlin/io/github/vooft/pepper/reports/builder/BuilderElements.kt")
    ),
    SourceFile.fromExisting(
        File(
            "../pepper-bdd-reports/pepper-bdd-reports-api/src/commonMain/kotlin/io/github/vooft/pepper/reports/api/PepperTestStatus.kt"
        )
    ),
    SourceFile.fromExisting(
        File(
            "../pepper-bdd-reports/pepper-bdd-reports-api/src/commonMain/kotlin/io/github/vooft/pepper/reports/api/PepperStepPrefix.kt"
        )
    ),
    SourceFile.fromExisting(
        File(
            "../pepper-bdd-reports/pepper-bdd-reports-builder/src/main/kotlin/io/github/vooft/pepper/reports/builder/PepperReportListenerElement.kt"
        )
    ),
    SourceFile.fromExisting(
        File(
            "../pepper-bdd-reports/pepper-bdd-reports-builder/src/main/kotlin/io/github/vooft/pepper/reports/builder/LowLevelReportListener.kt"
        )
    ),
)

fun compile(sourceFiles: List<SourceFile>, plugin: CompilerPluginRegistrar = PepperBddComponentRegistrar()): JvmCompilationResult =
    KotlinCompilation().apply {
        sources = sourceFiles
        compilerPluginRegistrars = listOf(plugin)
        inheritClassPath = true
    }.compile()

private fun SourceFile.Companion.fromExisting(file: File): SourceFile = new(file.name, file.readText())
