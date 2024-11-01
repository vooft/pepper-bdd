package io.github.vooft.pepper.dsl

interface PepperSpecDsl {
    fun Scenario(scenarioTitle: String, scenarioBody: suspend ScenarioDsl<Nothing>.() -> Unit)
    fun <T> ScenarioOutline(scenarioTitle: String, outlineDsl: ScenarioOutlineDsl<T>.() -> Unit): ScenarioWithExampleStartDsl<T>
}

interface ScenarioDsl<T> {
    val example: T
}

interface ScenarioOutlineDsl<T> {
    fun Examples(block: ExamplesDsl<T>.() -> Unit)
}

interface ExamplesDsl<T> {
    fun String.invoke(block: () -> T)
}

interface ScenarioWithExampleStartDsl<T> {
    suspend infix fun Outline(block: ScenarioDsl<T>.() -> Unit)
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
