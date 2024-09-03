package io.github.vooft.pepper.helper

import io.github.vooft.pepper.StepIdentifier
import io.github.vooft.pepper.dsl.PepperSpecDsl
import io.github.vooft.pepper.dsl.PepperSpecDslImpl
import io.github.vooft.pepper.dsl.ScenarioDsl
import io.github.vooft.pepper.testContainer

internal suspend fun <R> ScenarioDsl.StepContainer(id: String, block: suspend () -> R): R = testContainer(id, block)

internal fun PepperSpecDsl.addStep(id: String, prefix: String, name: String) {
    (this as PepperSpecDslImpl).remainingSteps.add(StepIdentifier(id = id, prefix = prefix, name = name))
}
