package io.github.pepper.reports.api

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PepperTestProject(
    val version: Int,
    val scenarios: List<PepperTestScenario>,
    val startedAt: Instant,
    val finishedAt: Instant
)

enum class PepperScenarioStatus {
    PASSED,
    FAILED
}

@Serializable
data class PepperTestScenario(
    val className: String,
    val name: String,
    val steps: List<PepperTestStep>,
    val status: PepperScenarioStatus,
    val startedAt: Instant,
    val finishedAt: Instant
)

@Serializable
data class PepperTestStep(
    val name: String,
    val arguments: List<StepArgument>,
    val error: String?,
    val startedAt: Instant,
    val finishedAt: Instant
) {
    @Serializable
    data class StepArgument(
        val name: String,
        val type: String,
        val value: String
    )
}
