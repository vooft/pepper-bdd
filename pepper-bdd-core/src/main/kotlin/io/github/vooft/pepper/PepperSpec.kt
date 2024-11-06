package io.github.vooft.pepper

import io.github.pepper.reports.builder.PepperReportBuilder
import io.github.vooft.pepper.dsl.PepperSpecDsl
import io.github.vooft.pepper.dsl.PepperSpecDslImpl
import io.kotest.core.names.TestName
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.addContainer
import kotlinx.coroutines.withContext

open class PepperSpec(scenarioBlock: PepperSpecDsl.() -> Unit) : FunSpec() {
    init {
        val dsl = PepperSpecDslImpl()
        dsl.scenarioBlock()

        assert(dsl.scenarios.isNotEmpty()) { "No scenarios found" }

        for (scenario in dsl.scenarios) {
            assert(scenario.hasSteps) { "No steps found for scenario ${scenario.key.scenarioTitle}" }

            addContainer(TestName("Scenario: ${scenario.key.title}"), false, null) {
                PepperReportBuilder.current().addScenario(this@PepperSpec::class.qualifiedName!!, scenario.key.title)

                try {
                    withContext(CurrentTestScope(this)) {
                        scenario.scenarioBody()
                    }
                } finally {
                    PepperReportBuilder.current().finishScenario()
                }
            }
        }
    }
}

@Target(AnnotationTarget.FUNCTION)
annotation class Step
