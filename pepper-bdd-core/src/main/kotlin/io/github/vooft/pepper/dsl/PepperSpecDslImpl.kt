package io.github.vooft.pepper.dsl

internal class PepperSpecDslImpl : PepperSpecDsl {
    override fun Scenario(description: String, scenarioBody: suspend ScenarioDsl.() -> Unit): Scenario {
        val dsl = ScenarioDslImpl()
        return ScenarioImpl(description) { dsl.scenarioBody() }
    }
}

internal class ScenarioDslImpl : ScenarioDsl

class ScenarioImpl(override val name: String, override val scenarioBody: suspend () -> Unit) : Scenario