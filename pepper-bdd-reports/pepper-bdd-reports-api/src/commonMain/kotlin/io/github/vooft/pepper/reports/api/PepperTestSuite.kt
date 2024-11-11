package io.github.vooft.pepper.reports.api

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
    val status: PepperScenarioStatus,
    val startedAt: Instant,
    val finishedAt: Instant
) {
    companion object
}

@Serializable
data class PepperTestStep(
    val id: String,
    val name: String,
    val arguments: List<StepArgument>,
    val result: String?,
    val error: String?,
    val startedAt: Instant,
    val finishedAt: Instant
) {
    @Serializable
    data class StepArgument(val name: String, val type: String, val value: String) {
        companion object
    }

    companion object
}
