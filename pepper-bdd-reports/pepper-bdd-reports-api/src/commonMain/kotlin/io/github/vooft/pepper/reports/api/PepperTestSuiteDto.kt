package io.github.vooft.pepper.reports.api

import io.github.vooft.pepper.reports.api.PepperTestStatus.FAILED
import io.github.vooft.pepper.reports.api.PepperTestStatus.PASSED
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PepperTestSuiteDto(val version: Int, val scenarios: List<String>, val startedAt: Instant, val finishedAt: Instant) {
    companion object
}

@Serializable
data class PepperTestScenarioDto(
    val id: String,
    val version: Int,
    val className: String,
    val name: String,
    val steps: List<PepperTestStepDto>,
    val startedAt: Instant,
    val finishedAt: Instant
) {
    companion object
}

@Serializable
data class PepperTestStepDto(
    val id: String,
    val prefix: PepperStepPrefix,
    val index: Int,
    val name: String,
    val status: PepperTestStatus,
    val arguments: List<StepArgumentDto>,
    val result: String?,
    val error: StepErrorDto?,
    val startedAt: Instant,
    val finishedAt: Instant
) {
    @Serializable
    data class StepArgumentDto(val name: String, val type: String, val value: String) {
        companion object
    }

    @Serializable
    data class StepErrorDto(val message: String, val stacktrace: String) {
        companion object
    }

    companion object
}

val PepperTestScenarioDto.status get() = steps.firstOrNull { it.status == FAILED }?.status ?: PASSED
