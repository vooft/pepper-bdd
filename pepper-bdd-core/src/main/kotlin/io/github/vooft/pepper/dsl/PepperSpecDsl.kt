package io.github.vooft.pepper.dsl

interface PepperSpecDsl {
    fun Scenario(scenarioTitle: String, tags: List<String> = emptyList(), scenarioBody: suspend ScenarioDsl.() -> Unit)
    fun <T : PepperExample> ScenarioExamples(scenarioTitle: String, examplesBody: ExamplesDsl<T>.() -> Unit): ExamplesDslTerminal<T>
}

interface ScenarioDsl

interface PepperExample : TagsHolder

interface TagsHolder {
    val tags: List<String> get() = emptyList()
}

interface ScenarioWithExampleDsl<T> : ScenarioDsl {
    val example: T
}

fun interface ExamplesDsl<T> {
    operator fun String.invoke(block: () -> T)
}

fun interface ExamplesDslTerminal<T> {
    infix fun Outline(scenarioBody: suspend ScenarioWithExampleDsl<T>.() -> Unit)
}

interface Scenario {
    val key: ScenarioKey
    val hasSteps: Boolean
    val tags: List<String>
    val scenarioBody: suspend () -> Unit

    sealed interface ScenarioKey {
        val scenarioTitle: String
        val title: String
    }

    data class SimpleScenario(override val scenarioTitle: String) : ScenarioKey {
        override val title: String get() = scenarioTitle
    }

    data class ExampleScenario(override val scenarioTitle: String, val example: String) : ScenarioKey {
        override val title: String get() = "$scenarioTitle: $example"
    }
}

val ScenarioDsl.Given: PepperPrefix get() = prefixFail()
val ScenarioDsl.When: PepperPrefix get() = prefixFail()
val ScenarioDsl.Then: PepperPrefix get() = prefixFail()

interface PepperPrefix

private fun prefixFail(): Nothing = error("This function should be replaced by a compiler plugin, please check your Gradle configuration")
