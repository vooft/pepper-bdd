package io.github.vooft.pepper.helper

import io.github.vooft.pepper.StepIdentifier
import io.github.vooft.pepper.dsl.PepperSpecDsl
import io.github.vooft.pepper.dsl.PepperSpecDslImpl
import io.github.vooft.pepper.dsl.ScenarioDsl
import io.github.vooft.pepper.testContainer

internal suspend fun <R> ScenarioDsl.GivenContainer(id: String, stepName: String, block: suspend () -> R): R =
    testContainer(id, "Given", stepName, block)

internal suspend fun <R> ScenarioDsl.WhenContainer(id: String, stepName: String, block: suspend () -> R): R =
    testContainer(id, "When", stepName, block)

internal suspend fun <R> ScenarioDsl.ThenContainer(id: String, stepName: String, block: suspend () -> R): R =
    testContainer(id, "Then", stepName, block)

internal suspend fun <R> ScenarioDsl.AndContainer(id: String, stepName: String, block: suspend () -> R): R =
    testContainer(id, "And", stepName, block)

internal fun PepperSpecDsl.addStep(id: String, name: String) {
    (this as PepperSpecDslImpl).remainingSteps.add(StepIdentifier(id, name))
}
