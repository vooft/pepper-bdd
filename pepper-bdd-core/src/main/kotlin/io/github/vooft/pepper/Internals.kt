package io.github.vooft.pepper

import io.github.vooft.pepper.reports.builder.PepperReportBuilder
import io.kotest.common.KotestInternal
import io.kotest.core.names.TestName
import io.kotest.core.source.sourceRef
import io.kotest.core.test.NestedTest
import io.kotest.core.test.TestScope
import io.kotest.core.test.TestType.Test
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

@OptIn(KotestInternal::class)
internal suspend fun <R> testContainer(id: String, testBlock: suspend () -> R, arguments: Map<String, Any?>): R {
    val step = retrieveRemainingSteps(id)
        .takeIf { it.isNotEmpty() }
        ?.removeFirst()
        ?: error("Step $id not found in the remaining steps")

    lateinit var result: StepResult<R>

    val testName = step.toTestName(substitutions = arguments)

    val reportBuilder = PepperReportBuilder.current()
    reportBuilder.addStep(testName.testName)
    arguments.forEach { (name, value) ->
        reportBuilder.addArgument(name, "bla", value.toString())
    }

    currentTestScope().registerTestCase(
        NestedTest(
            name = testName,
            disabled = false,
            config = null,
            type = Test,
            source = sourceRef()
        ) {
            withContext(CoroutineName("step: ${step.name}")) {
                result = try {
                    val successResult = testBlock()
                    reportBuilder.addResult(successResult)
                    StepResult.Success(successResult)
                } catch (t: Throwable) {
                    reportBuilder.addError(t)
                    StepResult.Error(t)
                }

                reportBuilder.finishStep()
                result.value
            }
        }
    )

    return result.value
}

private suspend fun retrieveRemainingSteps(currentStepId: String): MutableList<StepIdentifier> {
    val remainingSteps = requireNotNull(coroutineContext[PepperRemainingSteps]) { "Remaining steps are missing in the context" }.steps
    while (remainingSteps.isNotEmpty() && remainingSteps.first().id != currentStepId) {
        remainingSteps.removeFirst()
    }

    return remainingSteps
}

private suspend fun currentTestScope() = requireNotNull(coroutineContext[CurrentTestScope]) { "Test scope is missing in the context" }.scope

@OptIn(KotestInternal::class)
internal suspend fun registerRemainingSteps() {
    val remainingSteps = requireNotNull(coroutineContext[PepperRemainingSteps]) { "Remaining steps are missing in the context" }.steps
    val currentScope = requireNotNull(coroutineContext[CurrentTestScope]) { "Test scope is missing in the context" }.scope

    for (remainingStep in remainingSteps) {
        currentScope.registerTestCase(
            NestedTest(
                name = remainingStep.toTestName(mapOf()),
                disabled = true,
                config = null,
                type = Test,
                source = sourceRef()
            ) { }
        )
    }
}

internal data class StepIdentifier(val id: String, val prefix: String, val name: String) {
    fun toTestName(substitutions: Map<String, Any?>): TestName {
        val substituted = substitutions.entries.fold(name) { acc, (key, value) -> acc.replace("{$key}", value.toString()) }
        return TestName("$prefix: $substituted")
    }
}

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
