package io.github.vooft.pepper

import io.github.vooft.pepper.dsl.ScenarioDsl
import io.kotest.common.KotestInternal
import io.kotest.core.names.TestName
import io.kotest.core.source.sourceRef
import io.kotest.core.test.NestedTest
import io.kotest.core.test.TestType.Test
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

internal suspend fun <R> ScenarioDsl.GivenContainer(id: String, stepName: String, block: suspend () -> R): R =
    testContainer(id, "Given", stepName, block)

internal suspend fun <R> ScenarioDsl.WhenContainer(id: String, stepName: String, block: suspend () -> R): R =
    testContainer(id, "When", stepName, block)

internal suspend fun <R> ScenarioDsl.ThenContainer(id: String, stepName: String, block: suspend () -> R): R =
    testContainer(id, "Then", stepName, block)

internal suspend fun <R> ScenarioDsl.AndContainer(id: String, stepName: String, block: suspend () -> R): R =
    testContainer(id, "And", stepName, block)

@OptIn(KotestInternal::class)
private suspend fun <R> testContainer(id: String, prefix: String, stepName: String, testBlock: suspend () -> R): R {
    println("$id: $prefix: $stepName")

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

private sealed class StepResult<R> {
    abstract val value: R

    data class Success<R>(override val value: R) : StepResult<R>()
    data class Error<R>(val error: Throwable) : StepResult<R>() {
        override val value: R
            get() = throw error
    }
}
