package io.github.vooft.pepper.helper

import io.github.vooft.pepper.StepIdentifier
import io.github.vooft.pepper.dsl.PepperScenarioDsl
import io.github.vooft.pepper.dsl.PepperScenarioDslImpl
import io.github.vooft.pepper.dsl.ScenarioDsl
import io.github.vooft.pepper.testContainer

internal suspend fun <R> ScenarioDsl.StepContainer(id: String, block: suspend () -> R): R = testContainer(id, block)

internal fun PepperScenarioDsl.addStep(scenarioTitle: String, stepId: String, prefix: String, stepName: String) {
    (this as PepperScenarioDslImpl).addStep(scenarioTitle, StepIdentifier(id = stepId, prefix = prefix, name = stepName))
}
