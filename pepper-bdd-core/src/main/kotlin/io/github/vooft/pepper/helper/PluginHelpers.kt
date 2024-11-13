package io.github.vooft.pepper.helper

import io.github.vooft.pepper.StepIdentifier
import io.github.vooft.pepper.dsl.PepperSpecDsl
import io.github.vooft.pepper.dsl.PepperSpecDslImpl
import io.github.vooft.pepper.dsl.ScenarioDsl
import io.github.vooft.pepper.testContainer

internal suspend fun <R> ScenarioDsl.StepContainer(id: String, block: suspend () -> R, arguments: List<StepArgument>): R =
    testContainer(id = id, testBlock = block, arguments = arguments)

@Suppress("detekt:LongParameterList")
internal fun PepperSpecDsl.addStep(
    scenarioTitle: String,
    stepId: String,
    prefix: String,
    indexInGroup: Int,
    indexInTest: Int,
    totalStepsInTest: Int,
    stepName: String
) {
    (this as PepperSpecDslImpl).addStep(
        scenarioTitle,
        StepIdentifier(
            id = stepId,
            prefix = prefix,
            indexInGroup = indexInGroup,
            indexInTest = indexInTest,
            totalStepsInTest = totalStepsInTest,
            name = stepName
        )
    )
}

internal data class StepArgument(val name: String, val type: String, val value: Any?)
