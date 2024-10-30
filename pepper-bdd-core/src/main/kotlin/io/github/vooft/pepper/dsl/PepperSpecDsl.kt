package io.github.vooft.pepper.dsl

interface PepperSpecDsl {
    fun Scenario(scenarioTitle: String, scenarioBody: suspend ScenarioDsl.() -> Unit)
}

interface ScenarioDsl

interface Scenario {
    val title: String
    val scenarioBody: suspend () -> Unit
}

val ScenarioDsl.Given: PepperPrefix get() = pepperFail()
val ScenarioDsl.When: PepperPrefix get() = pepperFail()
val ScenarioDsl.Then: PepperPrefix get() = pepperFail()

interface PepperPrefix

private fun pepperFail(): Nothing = error("This function should be replaced by a compiler plugin, please check your Gradle configuration")
