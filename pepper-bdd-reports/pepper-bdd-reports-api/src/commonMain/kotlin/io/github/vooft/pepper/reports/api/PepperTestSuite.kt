package io.github.vooft.pepper.reports.api

import io.github.vooft.pepper.reports.api.PepperScenarioStatus.FAILED
import io.github.vooft.pepper.reports.api.PepperScenarioStatus.PASSED
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PepperTestSuite(val version: Int, val scenarios: List<String>, val startedAt: Instant, val finishedAt: Instant) {
    companion object
}

@Serializable
data class PepperTestScenario(
    val id: String,
    val version: Int,
    val className: String,
    val name: String,
    val steps: List<PepperTestStep>,
    val startedAt: Instant,
    val finishedAt: Instant
) {
    companion object
}

@Serializable
data class PepperTestStep(
    val id: String,
    val prefix: PepperStepPrefix,
    val name: String,
    val arguments: List<StepArgument>,
    val result: String?,
    val error: StepError?,
    val startedAt: Instant,
    val finishedAt: Instant
) {
    @Serializable
    data class StepArgument(val name: String, val type: String, val value: String) {
        companion object
    }

    @Serializable
    data class StepError(val message: String, val stacktrace: String) {
        companion object
    }

    companion object
}

val PepperTestStep.status get() = when (error) {
    null -> PASSED
    else -> FAILED
}

val PepperTestScenario.status get() = steps.firstOrNull { it.status == FAILED }?.status ?: PASSED
