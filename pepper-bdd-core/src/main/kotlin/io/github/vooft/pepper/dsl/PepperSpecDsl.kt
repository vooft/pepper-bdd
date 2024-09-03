package io.github.vooft.pepper.dsl

interface PepperSpecDsl {
    fun Scenario(description: String, scenarioBody: suspend ScenarioDsl.() -> Unit): Scenario
}

interface ScenarioDsl

interface Scenario {
    val name: String
    val scenarioBody: suspend () -> Unit
}

val ScenarioDsl.Given: Unit get() = pepperFail()
val ScenarioDsl.When: Unit get() = pepperFail()
val ScenarioDsl.Then: Unit get() = pepperFail()

private fun pepperFail(): Nothing = error("This function should be replaced by a compiler plugin, please check your Gradle configuration")
