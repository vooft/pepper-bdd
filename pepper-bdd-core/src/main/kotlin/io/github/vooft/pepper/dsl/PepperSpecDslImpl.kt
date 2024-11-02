package io.github.vooft.pepper.dsl

import io.github.vooft.pepper.PepperRemainingSteps
import io.github.vooft.pepper.StepIdentifier
import io.github.vooft.pepper.dsl.Scenario.Example
import io.github.vooft.pepper.dsl.Scenario.ScenarioKey
import io.github.vooft.pepper.dsl.Scenario.Simple
import io.github.vooft.pepper.registerRemainingSteps
import kotlinx.coroutines.withContext

internal class PepperSpecDslImpl : PepperSpecDsl {

    private val lazyScenarios = mutableMapOf<ScenarioKey, LazyScenario>()
    val scenarios: Collection<Scenario> get() = lazyScenarios.values

    private val stepsPerScenario = mutableMapOf<String, List<StepIdentifier>>()

    override fun Scenario(scenarioTitle: String, scenarioBody: suspend ScenarioDsl.() -> Unit) {
        val key = Simple(scenarioTitle)

        assert(!lazyScenarios.containsKey(key)) { "Scenario with description $scenarioTitle already exists" }

        val dsl = object : ScenarioDsl {}
        lazyScenarios[key] = LazyScenario(key) { dsl.scenarioBody() }
    }

    override fun <T> ScenarioExamples(scenarioTitle: String, examplesBody: ExamplesDsl<T>.() -> Unit): ExamplesDslTerminal<T> {
        val examples = mutableMapOf<String, T>()
        val examplesDsl = ExamplesDsl { examplesBlock ->
            val previousExample = examples.put(this, examplesBlock())
            assert(previousExample == null) { "Example with description $this already exists" }
        }
        examplesDsl.examplesBody()

        assert(examples.isNotEmpty()) { "Examples should not be empty for scenario $scenarioTitle" }

        return ExamplesDslTerminal { scenarioBody ->
            for ((exampleTitle, example) in examples) {
                val key = Example(scenarioTitle, exampleTitle)
                val titleWithExample = "$scenarioTitle: $exampleTitle"
                assert(!lazyScenarios.containsKey(key)) { "Scenario with description $titleWithExample already exists" }

                val dsl = object : ScenarioWithExampleDsl<T> {
                    override val example = example
                }
                val scenario = LazyScenario(key) { dsl.scenarioBody() }
                lazyScenarios[key] = scenario
            }
        }
    }

    internal fun addStep(scenarioTitle: String, stepIdentifier: StepIdentifier) {
        stepsPerScenario[scenarioTitle] = stepsPerScenario[scenarioTitle].orEmpty() + stepIdentifier
    }

    private inner class LazyScenario(override val key: ScenarioKey, private val rawScenarioBody: suspend () -> Unit) : Scenario {

        override val hasSteps get() = stepsPerScenario.containsKey(key.scenarioTitle)

        override val scenarioBody: suspend () -> Unit = {
            withContext(PepperRemainingSteps(stepsPerScenario.getValue(key.scenarioTitle).toMutableList())) {
                runCatching { rawScenarioBody() }
                    .onFailure { registerRemainingSteps() }
            }
        }
    }
}
