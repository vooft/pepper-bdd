package io.github.vooft.pepper

import io.github.vooft.pepper.dsl.PepperScenarioDsl
import io.github.vooft.pepper.dsl.PepperScenarioDslImpl
import io.kotest.core.names.TestName
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.addContainer
import kotlinx.coroutines.withContext

open class PepperScenarioSpec(scenarioBlock: PepperScenarioDsl.() -> Unit) : FunSpec() {
    init {
        val dsl = PepperScenarioDslImpl()
        dsl.scenarioBlock()

        assert(dsl.scenarios.isNotEmpty()) { "No scenarios found" }

        for (scenario in dsl.scenarios) {
            assert(scenario.hasSteps) { "No steps found for scenario ${scenario.title}" }

            addContainer(TestName("Scenario: ${scenario.title}"), false, null) {
                withContext(CurrentTestScope(this)) {
                    scenario.scenarioBody()
                }
            }
        }
    }
}

@Target(AnnotationTarget.FUNCTION)
annotation class Step
