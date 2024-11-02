package io.github.vooft.pepper.dsl

interface PepperSpecDsl {
    fun Scenario(scenarioTitle: String, scenarioBody: suspend ScenarioDsl.() -> Unit)
    fun <T> ScenarioExamples(scenarioTitle: String, examplesBody: ExamplesDsl<T>.() -> Unit): ExamplesDslTerminal<T>
}

interface ScenarioDsl

interface ScenarioWithExampleDsl<T>: ScenarioDsl {
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
    val scenarioBody: suspend () -> Unit

    sealed interface ScenarioKey {
        val scenarioTitle: String
        val title: String
    }

    data class Simple(override val scenarioTitle: String): ScenarioKey {
        override val title: String get() = scenarioTitle
    }

    data class Example(override val scenarioTitle: String, val example: String): ScenarioKey {
        override val title: String get() = "$scenarioTitle: $example"
    }
}

val ScenarioDsl.Given: PepperPrefix get() = pepperFail()
val ScenarioDsl.When: PepperPrefix get() = pepperFail()
val ScenarioDsl.Then: PepperPrefix get() = pepperFail()

interface PepperPrefix

private fun pepperFail(): Nothing = error("This function should be replaced by a compiler plugin, please check your Gradle configuration")
