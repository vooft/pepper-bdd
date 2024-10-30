package io.github.vooft.pepper.dsl

import io.github.vooft.pepper.CurrentTestScope
import io.github.vooft.pepper.PepperRemainingSteps
import io.github.vooft.pepper.StepIdentifier
import io.kotest.common.KotestInternal
import io.kotest.core.source.sourceRef
import io.kotest.core.test.NestedTest
import io.kotest.core.test.TestType.Test
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

internal class PepperSpecDslImpl : PepperSpecDsl {

    private val lazyScenarios = mutableMapOf<String, LazyScenario>()
    val scenarios: Collection<Scenario> get() = lazyScenarios.values

    override fun Scenario(scenarioTitle: String, scenarioBody: suspend ScenarioDsl.() -> Unit) {
        assert(!lazyScenarios.containsKey(scenarioTitle)) { "Scenario with description $scenarioTitle already exists" }
        val dsl = ScenarioDslImpl()
        lazyScenarios[scenarioTitle] = LazyScenario(scenarioTitle) { dsl.scenarioBody() }
    }

    internal fun addStep(className: String, stepIdentifier: StepIdentifier) {
        val scenario = lazyScenarios[className] ?: error("Scenario with description $className not found")
        scenario.allSteps.add(StepIdentifier(id = stepId, prefix = prefix, name = stepName))
    }
}

internal class ScenarioDslImpl : ScenarioDsl

internal class LazyScenario(
    override val title: String,
    private val rawScenarioBody: suspend () -> Unit
) : Scenario {

    val allSteps: MutableList<StepIdentifier> = mutableListOf()

    override val hasSteps get() = allSteps.isNotEmpty()

    override val scenarioBody: suspend () -> Unit = {
        withContext(PepperRemainingSteps(allSteps.toMutableList())) {
            runCatching { rawScenarioBody }
                .onFailure { registerRemainingSteps() }
        }
    }
}

@OptIn(KotestInternal::class)
private suspend fun registerRemainingSteps() {
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
