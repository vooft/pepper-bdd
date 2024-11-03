package io.github.vooft.pepper.helper

import io.github.vooft.pepper.StepIdentifier
import io.github.vooft.pepper.dsl.PepperSpecDsl
import io.github.vooft.pepper.dsl.PepperSpecDslImpl
import io.github.vooft.pepper.dsl.ScenarioDsl
import io.github.vooft.pepper.testContainer

internal suspend fun <R> ScenarioDsl.StepContainer(id: String, block: suspend () -> R, arguments: Map<String, Any?>): R {
    return testContainer(id = id, testBlock = block, arguments = arguments)
}

internal fun PepperSpecDsl.addStep(scenarioTitle: String, stepId: String, prefix: String, stepName: String) {
    (this as PepperSpecDslImpl).addStep(scenarioTitle, StepIdentifier(id = stepId, prefix = prefix, name = stepName))
}
