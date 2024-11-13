package io.github.vooft.pepper.reports.builder

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
}

data class PepperStepBuilder(
    val name: String,
    val arguments: MutableList<StepArgument> = mutableListOf(),
    var result: String? = null,
    var error: StepError? = null,
    val startedAt: Instant = Instant.now(),
    var finishedAt: Instant? = null
) {
    val id = name.sha1()

    data class StepArgument(val name: String, val typeName: String, val value: String)

    data class StepError(val message: String, val stacktrace: String)
}

private fun String.sha1() = MessageDigest.getInstance("SHA-1").digest(toByteArray()).joinToString("") { "%02x".format(it) }
