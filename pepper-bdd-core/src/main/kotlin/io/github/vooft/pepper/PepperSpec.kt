package io.github.vooft.pepper

import io.github.vooft.pepper.dsl.PepperSpecDsl
import io.github.vooft.pepper.dsl.PepperSpecDslImpl
import io.github.vooft.pepper.dsl.Scenario
import io.kotest.core.names.TestName
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.addContainer
import io.kotest.core.spec.style.scopes.addTest
import kotlinx.coroutines.withContext

open class PepperSpec(scenarioBlock: PepperSpecDsl.() -> Scenario) : FunSpec() {
    init {
        val dsl = PepperSpecDslImpl()
        val scenario = dsl.scenarioBlock()

        if (dsl.remainingSteps.isEmpty()) {
            addTest(TestName("No steps found"), false, null) {
                throw AssertionError("No steps found, did you register pepper-bdd plugin correctly?")
            }
        } else {
            val remainingSteps = dsl.remainingSteps.toMutableList()
            addContainer(TestName("Scenario: ${scenario.name}"), false, null) {
                withContext(CurrentTestScope(this) + RemainingSteps(remainingSteps)) {
                    scenario.scenarioBody()
                }
            }
        }
    }
}

@Target(AnnotationTarget.FUNCTION)
annotation class Step
