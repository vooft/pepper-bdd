package io.github.vooft.pepper

import io.github.vooft.pepper.dsl.PepperSpecDsl
import io.github.vooft.pepper.dsl.PepperSpecDslImpl
import io.github.vooft.pepper.dsl.Scenario
import io.kotest.common.KotestInternal
import io.kotest.core.names.TestName
import io.kotest.core.source.sourceRef
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.addContainer
import io.kotest.core.test.NestedTest
import io.kotest.core.test.TestScope
import io.kotest.core.test.TestType.Test
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

open class PepperSpec(scenarioBlock: PepperSpecDsl.() -> Scenario) : FunSpec() {
    init {
        val dsl = PepperSpecDslImpl()
        val scenario = dsl.scenarioBlock()

        addContainer(TestName("Scenario: ${scenario.name}"), false, null) {
            withContext(CurrentTestScope(this)) { scenario.scenarioBody() }
        }
    }
}

data class CurrentTestScope(val scope: TestScope) : AbstractCoroutineContextElement(CurrentTestScope) {
    companion object Key : CoroutineContext.Key<CurrentTestScope>

    override fun toString(): String = "CurrentTestScope($scope)"
}

@OptIn(KotestInternal::class)
internal suspend fun <R> testContainer(prefix: String, stepName: String, testBlock: suspend () -> R): R {
    println("$prefix: $stepName")

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

sealed class StepResult<R> {
    abstract val value: R

    data class Success<R>(override val value: R) : StepResult<R>()
    data class Error<R>(val error: Throwable) : StepResult<R>() {
        override val value: R
            get() = throw error
    }
}

@Target(AnnotationTarget.FUNCTION)
annotation class Step
