package io.github.vooft.pepper.reports.builder

import io.github.vooft.pepper.reports.builder.BuilderElements.PepperScenarioStatus.FAILED
import io.github.vooft.pepper.reports.builder.BuilderElements.PepperScenarioStatus.PASSED
import java.time.Instant

object BuilderElements {
    data class PepperTestProject(
        val scenarios: MutableList<PepperTestScenario> = mutableListOf(),
        val startedAt: Instant = Instant.now(),
        var finishedAt: Instant? = null
    )

    enum class PepperScenarioStatus {
        PASSED,
        FAILED
    }

    data class PepperTestScenario(
        val className: String,
        val name: String,
        val steps: MutableList<PepperTestStep> = mutableListOf(),
        val startedAt: Instant = Instant.now(),
        var finishedAt: Instant? = null
    ) {
        val status: PepperScenarioStatus
            get() = when (steps.all { it.error == null }) {
                true -> PASSED
                false -> FAILED
            }
    }

    data class PepperTestStep(
        val name: String,
        val arguments: MutableList<StepArgument> = mutableListOf(),
        var result: String? = null,
        var error: String? = null,
        val startedAt: Instant = Instant.now(),
        var finishedAt: Instant? = null
    ) {
        data class StepArgument(val name: String, val typeName: String, val value: String)
    }
}
