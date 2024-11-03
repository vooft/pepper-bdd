package io.github.vooft.pepper

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
    val remainingSteps = requireNotNull(coroutineContext[PepperRemainingSteps]) { "Remaining steps are missing in the context" }.steps
    while (remainingSteps.isNotEmpty() && remainingSteps.first().id != id) {
        remainingSteps.removeFirst()
    }

    val step = remainingSteps.takeIf { it.isNotEmpty() }?.removeFirst() ?: error("Step $id not found in the remaining steps")

    val currentScope = requireNotNull(coroutineContext[CurrentTestScope]) { "Test scope is missing in the context" }.scope
    lateinit var result: StepResult<R>

    currentScope.registerTestCase(
        NestedTest(
            name = step.toTestName(),
            disabled = false,
            config = null,
            type = Test,
            source = sourceRef()
        ) {
            withContext(CoroutineName("step: ${step.name}")) {
                result = try {
                    StepResult.Success(testBlock())
                } catch (t: Throwable) {
                    StepResult.Error(t)
                }

                result.value
            }
        }
    )

    return result.value
}

@OptIn(KotestInternal::class)
internal suspend fun registerRemainingSteps() {
    val remainingSteps = requireNotNull(coroutineContext[PepperRemainingSteps]) { "Remaining steps are missing in the context" }.steps
    val currentScope = requireNotNull(coroutineContext[CurrentTestScope]) { "Test scope is missing in the context" }.scope

    for (remainingStep in remainingSteps) {
        currentScope.registerTestCase(
            NestedTest(
                name = remainingStep.toTestName(),
                disabled = true,
                config = null,
                type = Test,
                source = sourceRef()
            ) { }
        )
    }
}

internal data class StepIdentifier(val id: String, val prefix: String, val name: String) {
    fun toTestName() = TestName("$prefix: $name")
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
