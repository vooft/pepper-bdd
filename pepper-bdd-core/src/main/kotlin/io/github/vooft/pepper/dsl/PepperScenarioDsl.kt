package io.github.vooft.pepper.dsl

interface PepperScenarioDsl {
    fun Scenario(scenarioTitle: String, scenarioBody: suspend ScenarioDsl<Nothing>.() -> Unit)
}

interface PepperScenarioOutlineDsl {
    fun <T> ScenarioOutline(scenarioTitle: String, scenarioBody: suspend StartScenarioOutlineExamplesDsl<T>.() -> Unit)
}

interface ScenarioDsl<T>

interface StartScenarioOutlineExamplesDsl<T> {
    fun Examples(block: ExamplesDsl<T>.() -> Unit)
}

interface ExamplesDsl<T> {
    operator fun String.invoke(block: () -> T)
}

interface StartScenarioOutlineBodyDsl<T> {
    infix fun Outline(scenarioBody: ScenarioDsl<T>.() -> Unit)
}

interface Scenario {
    val title: String
    val hasSteps: Boolean
    val scenarioBody: suspend () -> Unit
}

val ScenarioDsl<*>.Given: PepperPrefix get() = pepperFail()
val ScenarioDsl<*>.When: PepperPrefix get() = pepperFail()
val ScenarioDsl<*>.Then: PepperPrefix get() = pepperFail()

interface PepperPrefix

private fun pepperFail(): Nothing = error("This function should be replaced by a compiler plugin, please check your Gradle configuration")
