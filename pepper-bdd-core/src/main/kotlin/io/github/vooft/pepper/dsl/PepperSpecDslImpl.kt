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

    val stepsPerScenario = mutableMapOf<String, List<StepIdentifier>>()

    override fun Scenario(scenarioTitle: String, scenarioBody: suspend ScenarioDsl<Nothing>.() -> Unit) {
        assert(!lazyScenarios.containsKey(scenarioTitle)) { "Scenario with description $scenarioTitle already exists" }
        val dsl = object : ScenarioDsl<Nothing> {
            override val example: Nothing get() = error("Example is not available in simple scenario")
        }

        lazyScenarios[scenarioTitle] = LazyScenario(scenarioTitle) { dsl.scenarioBody() }
    }

    override fun <T> ScenarioOutline(
        scenarioTitle: String,
        outlineDsl: ScenarioOutlineDsl<T>.() -> Unit
    ): ScenarioWithExampleStartDsl<T> {
        val examples = mutableMapOf<String, T>()
        val scenarioOutlineDsl = object : ScenarioOutlineDsl<T> {
            override fun Examples(block: ExamplesDsl<T>.() -> Unit) {
                val exampleDsl = object : ExamplesDsl<T> {
                    override fun String.invoke(block: () -> T) {
                        examples[this] = block()
                    }
                }
                exampleDsl.block()

                assert(examples.isNotEmpty()) { "No examples found for scenario outline $scenarioTitle" }
            }
        }

        scenarioOutlineDsl.outlineDsl()

        return object : ScenarioWithExampleStartDsl<T> {
            override suspend fun Outline(block: ScenarioDsl<T>.() -> Unit) {
                for ((exampleTitle, exampleValue) in examples) {
                    val dsl = object : ScenarioDsl<T> {
                        override val example: T get() = exampleValue
                    }
                    val fullTitle = "$scenarioTitle: $exampleTitle"
                    assert(!lazyScenarios.containsKey(fullTitle)) { "Scenario with description $fullTitle already exists" }
                    lazyScenarios[fullTitle] = LazyScenario(fullTitle) { dsl.block() }
                }
            }
        }
    }

    internal fun addStep(scenarioTitle: String, stepIdentifier: StepIdentifier) {
        stepsPerScenario[scenarioTitle] = stepsPerScenario[scenarioTitle].orEmpty() + stepIdentifier
    }

    private inner class LazyScenario(override val title: String, private val rawScenarioBody: suspend () -> Unit) : Scenario {

        override val hasSteps get() = stepsPerScenario[title].orEmpty().isNotEmpty()

        override val scenarioBody: suspend () -> Unit = {
            withContext(PepperRemainingSteps(stepsPerScenario.getValue(title).toMutableList())) {
                runCatching { rawScenarioBody() }
                    .onFailure { registerRemainingSteps() }
            }
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
