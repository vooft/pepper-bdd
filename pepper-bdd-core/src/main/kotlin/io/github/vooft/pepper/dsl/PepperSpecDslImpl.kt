package io.github.vooft.pepper.dsl

import io.github.vooft.pepper.PepperRemainingSteps
import io.github.vooft.pepper.StepIdentifier
import io.github.vooft.pepper.registerRemainingSteps
import kotlinx.coroutines.withContext

internal class PepperSpecDslImpl : PepperSpecDsl {

    private val lazyScenarios = mutableMapOf<String, LazyScenario>()
    val scenarios: Collection<Scenario> get() = lazyScenarios.values

    val stepsPerScenario = mutableMapOf<String, List<StepIdentifier>>()

    override fun Scenario(scenarioTitle: String, scenarioBody: suspend ScenarioDsl.() -> Unit) {
        assert(!lazyScenarios.containsKey(scenarioTitle)) { "Scenario with description $scenarioTitle already exists" }
        val dsl = object : ScenarioDsl {}
        lazyScenarios[scenarioTitle] = LazyScenario(scenarioTitle) { dsl.scenarioBody() }
    }

    override fun <T> ScenarioExamples(scenarioTitle: String, examplesBody: ExamplesDsl<T>.() -> Unit): ExamplesDslTerminal<T> {
        assert(!lazyScenarios.containsKey(scenarioTitle)) { "Scenario with description $scenarioTitle already exists" }

        val examples = mutableMapOf<String, T>()
        val examplesDsl = ExamplesDsl { examplesBlock ->
            val previousExample = examples.put(this, examplesBlock())
            assert(previousExample == null) { "Example with description $this already exists" }
        }
        examplesDsl.examplesBody()

        assert(examples.isNotEmpty()) { "Examples should not be empty for scenario $scenarioTitle" }

        // put fake lazy scenario to catch missing outline
        lazyScenarios[scenarioTitle] = LazyScenario(scenarioTitle) { }

        return ExamplesDslTerminal { scenarioBody ->
            lazyScenarios.remove(scenarioTitle)

            for ((exampleTitle, example) in examples) {
                val titleWithExample = "$scenarioTitle: $exampleTitle"
                assert(!lazyScenarios.containsKey(titleWithExample)) { "Scenario with description $titleWithExample already exists" }

                val dsl = object : ScenarioWithExampleDsl<T> {
                    override val example = example
                }
                val scenario = LazyScenario("$scenarioTitle: $exampleTitle") { dsl.scenarioBody() }
                lazyScenarios[scenario.title] = scenario
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
