package io.github.vooft.pepper.dsl

import io.github.vooft.pepper.CurrentTestScope
import io.github.vooft.pepper.RemainingSteps
import io.github.vooft.pepper.StepIdentifier
import io.kotest.common.KotestInternal
import io.kotest.core.source.sourceRef
import io.kotest.core.test.NestedTest
import io.kotest.core.test.TestType.Test
import kotlin.coroutines.coroutineContext

internal class PepperSpecDslImpl : PepperSpecDsl {

    internal val remainingSteps = mutableListOf<StepIdentifier>()

    override fun Scenario(description: String, scenarioBody: suspend ScenarioDsl.() -> Unit): Scenario {
        val dsl = ScenarioDslImpl()
        return ScenarioImpl(description) {
            try {
                dsl.scenarioBody()
            } catch (_: Throwable) {
                registerRemainingSteps()
            }
        }
    }

    @OptIn(KotestInternal::class)
    private suspend fun registerRemainingSteps() {
        val remainingSteps = requireNotNull(coroutineContext[RemainingSteps]) { "Remaining steps are missing in the context" }.steps
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
}

internal class ScenarioDslImpl : ScenarioDsl

class ScenarioImpl(override val name: String, override val scenarioBody: suspend () -> Unit) : Scenario
