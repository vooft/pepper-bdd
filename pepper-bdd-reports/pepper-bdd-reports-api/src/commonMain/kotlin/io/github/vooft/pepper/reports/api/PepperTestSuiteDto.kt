package io.github.vooft.pepper.reports.api

import io.github.vooft.pepper.reports.api.PepperTestScenarioDto.ScenarioId
import io.github.vooft.pepper.reports.api.PepperTestStatus.FAILED
import io.github.vooft.pepper.reports.api.PepperTestStatus.PASSED
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class PepperTestSuiteDto(
    val id: SuiteId,
    val version: Int,
    val scenarios: List<ScenarioSummaryDto>,
    val startedAt: Instant,
    val finishedAt: Instant
) {
    @Serializable
    data class ScenarioSummaryDto(val id: ScenarioId, val name: String, val status: PepperTestStatus)

    @Serializable
    @JvmInline
    value class SuiteId(val value: String) {
        companion object
    }

    companion object
}

@Serializable
data class PepperTestScenarioDto(
    val id: ScenarioId,
    val version: Int,
    val className: String,
    val name: String,
    val tags: List<ScenarioTag>,
    val steps: List<PepperTestStepDto>,
    val startedAt: Instant,
    val finishedAt: Instant
) {
    @Serializable
    @JvmInline
    value class ScenarioId(val value: String) {
        companion object
    }

    @Serializable
    @JvmInline
    value class ScenarioTag(val value: String) {
        companion object
    }

    companion object
}

@Serializable
data class PepperTestStepDto(
    val id: StepId,
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

    @Serializable
    @JvmInline
    value class StepId(val value: String) {
        companion object
    }

    companion object
}

@Suppress("unused")
val PepperTestScenarioDto.status get() = steps.firstOrNull { it.status == FAILED }?.status ?: PASSED
