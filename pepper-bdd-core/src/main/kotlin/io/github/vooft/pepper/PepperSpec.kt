package io.github.vooft.pepper

import io.github.vooft.pepper.PepperSpec.CapturedValue
import io.github.vooft.pepper.dsl.PepperSpecDsl
import io.github.vooft.pepper.dsl.PepperSpecDslImpl
import io.github.vooft.pepper.dsl.Scenario
import io.github.vooft.pepper.dsl.ScenarioDsl
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

    class CapturedValue<T>(val value: T)
}

data class CurrentTestScope(val scope: TestScope) : AbstractCoroutineContextElement(CurrentTestScope) {
    companion object Key : CoroutineContext.Key<CurrentTestScope>

    override fun toString(): String = "CurrentTestScope($scope)"
}

@OptIn(KotestInternal::class)
internal suspend fun <R> testContainer(prefix: String, stepName: String, testBlock: suspend () -> R): R {
    println("$prefix: $stepName")

    val currentScope = requireNotNull(coroutineContext[CurrentTestScope]) { "Test scope is missing in the context" }.scope
    lateinit var capturedValue: CapturedValue<R>

    currentScope.registerTestCase(
        NestedTest(
            name = TestName("$prefix: $stepName"),
            disabled = false,
            config = null,
            type = Test,
            source = sourceRef()
        ) { withContext(CoroutineName("step: $stepName")) { capturedValue = CapturedValue(testBlock()) } }
    )

    return capturedValue.value
}

internal suspend fun <R> ScenarioDsl.GivenContainer(stepName: String, block: suspend () -> R): R = testContainer("Given", stepName, block)

internal suspend fun <R> ScenarioDsl.WhenContainer(stepName: String, block: suspend () -> R): R = testContainer("When", stepName, block)

internal suspend fun <R> ScenarioDsl.ThenContainer(stepName: String, block: suspend () -> R): R = testContainer("Then", stepName, block)

internal suspend fun <R> ScenarioDsl.AndContainer(stepName: String, block: suspend () -> R): R = testContainer("And", stepName, block)

@Target(AnnotationTarget.FUNCTION)
annotation class Step
