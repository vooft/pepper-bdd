package io.github.vooft.pepper

import io.github.vooft.pepper.dsl.PepperSpecDsl
import io.github.vooft.pepper.dsl.PepperSpecDslImpl
import io.github.vooft.pepper.dsl.Scenario
import io.kotest.common.KotestInternal
import io.kotest.core.names.TestName
import io.kotest.core.source.sourceRef
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.addContainer
import io.kotest.core.test.NestedTest
import io.kotest.core.test.TestType.Test
import kotlinx.coroutines.withContext


open class PepperSpec(scenarioBlock: PepperSpecDsl.() -> Scenario) : FunSpec() {
    init {
        val dsl = PepperSpecDslImpl()
        val scenario = dsl.scenarioBlock()

        val remainingSteps = dsl.remainingSteps.toMutableList()
        addContainer(TestName("Scenario: ${scenario.name}"), false, null) {
            withContext(CurrentTestScope(this) + RemainingSteps(remainingSteps)) {
                scenario.scenarioBody()
            }

            if (remainingSteps.isEmpty()) {
                @OptIn(KotestInternal::class)
                registerTestCase(NestedTest(
                    name = TestName("<no steps found>"),
                    disabled = false,
                    config = null,
                    type = Test,
                    source = sourceRef(),
                    test = { throw AssertionError("No steps found, did you register pepper-bdd plugin correctly?") }
                ))
            }
        }
    }
}

@Target(AnnotationTarget.FUNCTION)
annotation class Step
