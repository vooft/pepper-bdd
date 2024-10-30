package io.github.vooft.pepper.helper

import io.github.vooft.pepper.StepIdentifier
import io.github.vooft.pepper.dsl.PepperSpecDsl
import io.github.vooft.pepper.dsl.PepperSpecDslImpl
import io.github.vooft.pepper.dsl.ScenarioDsl
import io.github.vooft.pepper.testContainer

internal suspend fun <R> ScenarioDsl.StepContainer(id: String, block: suspend () -> R): R = testContainer(id, block)

internal fun PepperSpecDsl.addStep(scenarioName: String, stepId: String, prefix: String, stepName: String) {
    (this as PepperSpecDslImpl).addStep(StepIdentifier(id = stepId, prefix = prefix, name = stepName))
}
