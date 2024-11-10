package io.github.vooft.pepper.reports.builder

import io.github.vooft.pepper.reports.api.PepperScenarioStatus
import io.github.vooft.pepper.reports.api.PepperScenarioStatus.FAILED
import io.github.vooft.pepper.reports.api.PepperScenarioStatus.PASSED
import java.security.MessageDigest
import java.time.Instant

data class PepperScenarioBuilder(
    val className: String,
    val name: String,
    val steps: MutableList<PepperStepBuilder> = mutableListOf(),
    val startedAt: Instant = Instant.now(),
    var finishedAt: Instant? = null
) {

    val id = "$className-$name".sha1()

    val status: PepperScenarioStatus
        get() = when (steps.all { it.error == null }) {
            true -> PASSED
            false -> FAILED
        }
}

data class PepperStepBuilder(
    val name: String,
    val arguments: MutableList<StepArgument> = mutableListOf(),
    var result: String? = null,
    var error: String? = null,
    val startedAt: Instant = Instant.now(),
    var finishedAt: Instant? = null
) {
    val id = name.sha1()

    data class StepArgument(val name: String, val typeName: String, val value: String)
}

private fun String.sha1() = MessageDigest.getInstance("SHA-1").digest(toByteArray()).joinToString("") { "%02x".format(it) }
