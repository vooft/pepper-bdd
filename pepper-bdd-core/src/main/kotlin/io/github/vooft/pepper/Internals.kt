package io.github.vooft.pepper

import io.github.vooft.pepper.helper.StepArgument
import io.github.vooft.pepper.reports.builder.LowLevelReportListener
import io.kotest.common.KotestInternal
import io.kotest.core.names.TestName
import io.kotest.core.source.SourceRef
import io.kotest.core.spec.Spec
import io.kotest.core.test.NestedTest
import io.kotest.core.test.TestScope
import io.kotest.core.test.TestType.Test
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@OptIn(KotestInternal::class)
internal suspend fun <R> testContainer(id: String, testBlock: suspend () -> R, arguments: List<StepArgument>): R {
    val step = retrieveRemainingSteps(id)
        .takeIf { it.isNotEmpty() }
        ?.removeFirst()
        ?: error("Step $id not found in the remaining steps")

    lateinit var result: StepResult<R>

    val substituted = step.substitute(arguments)

    LowLevelReportListener.ifPresent {
        startStep(step.indexInScenario, step.prefix, substituted)

        arguments.forEach { addArgument(it.name, it.type, it.value.toString()) }
    }

    currentTestScope().registerTestCase(
        NestedTest(
            name = step.toTestName(substituted),
            disabled = false,
            config = null,
            type = Test,
            source = sourceRef()
        ) {
            withContext(CoroutineName("step: ${step.name}")) {
                result = try {
                    val successResult = testBlock()
                    LowLevelReportListener.ifPresent { finishStepWithSuccess(successResult) }
                    StepResult.Success(successResult)
                } catch (t: Throwable) {
                    LowLevelReportListener.ifPresent { finishStepWithError(t) }
                    StepResult.Error(t)
                }

                result.value
            }
        }
    )

    return result.value
}

private suspend fun retrieveRemainingSteps(currentStepId: String): MutableList<StepIdentifier> {
    val remainingSteps = requireNotNull(currentCoroutineContext()[PepperRemainingSteps]) {
        "Remaining steps are missing in the context"
    }.steps
    while (remainingSteps.isNotEmpty() && remainingSteps.first().id != currentStepId) {
        remainingSteps.removeFirst()
    }

    return remainingSteps
}

private suspend fun currentTestScope() = requireNotNull(currentCoroutineContext()[CurrentTestScope]) {
    "Test scope is missing in the context"
}.scope

@OptIn(KotestInternal::class)
internal suspend fun registerRemainingSteps() {
    val remainingSteps = requireNotNull(currentCoroutineContext()[PepperRemainingSteps]) {
        "Remaining steps are missing in the context"
    }.steps
    val currentScope = requireNotNull(currentCoroutineContext()[CurrentTestScope]) { "Test scope is missing in the context" }.scope

    for (remainingStep in remainingSteps) {
        LowLevelReportListener.ifPresent {
            skipStep(index = remainingStep.indexInScenario, prefix = remainingStep.prefix, name = remainingStep.name)
        }

        currentScope.registerTestCase(
            NestedTest(
                name = remainingStep.toTestName(remainingStep.name),
                disabled = true,
                config = null,
                type = Test,
                source = sourceRef()
            ) { }
        )
    }
}

internal data class StepIdentifier(
    val id: String,
    val prefix: String,
    val indexInGroup: Int,
    val indexInScenario: Int,
    val totalStepsInTest: Int,
    val name: String
) {
    fun toTestName(substituted: String): TestName = TestName(
        name = "${indexInScenario + 1}. ${replacedPrefix.capitalized}: $substituted",
        focus = false,
        bang = false,
        prefix = null,
        suffix = null,
        defaultAffixes = false
    )

    private val replacedPrefix
        get() = when (indexInGroup) {
            0 -> prefix
            else -> "AND"
        }
}

private fun StepIdentifier.substitute(substitutions: List<StepArgument>) =
    substitutions.fold(name) { acc, arg -> acc.replace("{${arg.name}}", arg.value.toString()) }

private sealed class StepResult<R> {
    abstract val value: R

    data class Success<R>(override val value: R) : StepResult<R>()
    data class Error<R>(val error: Throwable) : StepResult<R>() {
        override val value: R
            get() = throw error
    }
}

internal data class CurrentTestScope(val scope: TestScope) : AbstractCoroutineContextElement(CurrentTestScope) {
    companion object Key : CoroutineContext.Key<CurrentTestScope>

    override fun toString(): String = "CurrentTestScope($scope)"
}

internal data class PepperRemainingSteps(val steps: MutableList<StepIdentifier>) : AbstractCoroutineContextElement(PepperRemainingSteps) {
    companion object Key : CoroutineContext.Key<PepperRemainingSteps>

    override fun toString(): String = "RemainingSteps($steps)"
}

private val String.capitalized: String
    get() {
        val first = first().uppercase()
        val rest = drop(1).lowercase()
        return first + rest
    }

// this method is internal in kotest from kotest-6.x
@Suppress("detekt:CyclomaticComplexMethod")
private fun sourceRef(): SourceRef {
    val stack = Thread.currentThread().stackTrace
    if (stack.isEmpty()) return SourceRef.None

    val frame = stack.dropWhile {
        it.className.startsWith("java.") ||
            it.className.startsWith("javax.") ||
            it.className.startsWith("jdk.internal.") ||
            it.className.startsWith("com.sun") ||
            it.className.startsWith("kotlin.") ||
            it.className.startsWith("kotlinx.") ||
            it.className.startsWith("io.kotest.core.") ||
            it.className.startsWith("io.kotest.engine.")
    }.firstOrNull()

    // preference is given to the class name but we must try to find the enclosing spec
    val kclass = frame?.className?.let { fqn ->
        runCatching {
            var temp: KClass<*>? = Class.forName(fqn).kotlin
            while (temp != null && !temp.isSubclassOf(Spec::class)) {
                temp = temp.java.enclosingClass?.kotlin
            }
            temp
        }.getOrNull()
    }

    val lineNumber = frame?.lineNumber?.takeIf { it > 0 }

    return when {
        kclass == null -> SourceRef.None
        lineNumber == null -> SourceRef.ClassSource(kclass.java.name)
        else -> SourceRef.ClassLineSource(kclass.java.name, lineNumber)
    }
}
