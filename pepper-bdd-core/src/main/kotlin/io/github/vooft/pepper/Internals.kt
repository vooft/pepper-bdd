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
internal suspend fun <R> testContainer(id: String, prefix: String, stepName: String, testBlock: suspend () -> R): R {
    val remainingSteps = requireNotNull(coroutineContext[RemainingSteps]) { "Remaining steps are missing in the context" }.steps
    while (remainingSteps.isNotEmpty() && remainingSteps.first().id != id) {
        remainingSteps.removeFirst()
    }

    remainingSteps.takeIf { it.isNotEmpty() }?.removeFirst() ?: error("Step $id not found in the remaining steps")

    val currentScope = requireNotNull(coroutineContext[CurrentTestScope]) { "Test scope is missing in the context" }.scope
    lateinit var result: StepResult<R>

    currentScope.registerTestCase(
        NestedTest(
            name = TestName("$prefix: $stepName"),
            disabled = false,
            config = null,
            type = Test,
            source = sourceRef()
        ) {
            withContext(CoroutineName("step: $stepName")) {
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

internal data class StepIdentifier(val id: String, val name: String)

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

internal data class RemainingSteps(val steps: MutableList<StepIdentifier>) : AbstractCoroutineContextElement(RemainingSteps) {
    companion object Key : CoroutineContext.Key<RemainingSteps>

    override fun toString(): String = "RemainingSteps($steps)"
}
