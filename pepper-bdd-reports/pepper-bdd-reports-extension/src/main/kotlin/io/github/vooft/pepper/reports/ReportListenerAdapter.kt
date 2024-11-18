package io.github.vooft.pepper.reports

import io.github.vooft.pepper.reports.api.PepperStepPrefix
import io.github.vooft.pepper.reports.api.PepperTestScenarioDto
import io.github.vooft.pepper.reports.api.PepperTestStatus
import io.github.vooft.pepper.reports.api.PepperTestStepDto
import io.github.vooft.pepper.reports.api.PepperTestStepDto.StepArgumentDto
import io.github.vooft.pepper.reports.api.PepperTestSuiteDto
import io.github.vooft.pepper.reports.builder.LowLevelReportListener
import io.github.vooft.pepper.reports.builder.PepperScenarioBuilder
import io.github.vooft.pepper.reports.builder.PepperStepBuilder
import io.github.vooft.pepper.reports.builder.PepperStepBuilder.StepError
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.toKotlinInstant
import java.time.Instant

class ReportListenerAdapter(private val listener: PepperReportListener) : LowLevelReportListener {

    private val startedAt = Instant.now()

    private val scenarioIdsMutex = Mutex()
    private val scenarioIds = mutableListOf<String>()

    @Volatile
    private lateinit var scenario: PepperScenarioBuilder

    val suite get() = PepperTestSuiteDto(
        version = 1,
        scenarios = scenarioIds,
        startedAt = startedAt.toKotlinInstant(),
        finishedAt = Instant.now().toKotlinInstant()
    )

    override suspend fun startScenario(className: String, name: String) {
        scenario = PepperScenarioBuilder(className, name)
        scenarioIdsMutex.withLock { scenarioIds.add(scenario.id) }
    }

    override suspend fun startStep(index: Int, prefix: String, name: String) {
        scenario.steps.add(
            PepperStepBuilder(
                index = index,
                name = name,
                prefix = when (prefix.uppercase()) {
                    "GIVEN" -> PepperStepPrefix.GIVEN
                    "WHEN" -> PepperStepPrefix.WHEN
                    "THEN" -> PepperStepPrefix.THEN
                    else -> error("Unknown prefix $prefix")
                }
            )
        )
    }

    override suspend fun skipStep(index: Int, prefix: String, name: String) {
        scenario.steps.add(
            PepperStepBuilder(
                index = index,
                name = name,
                status = PepperTestStatus.SKIPPED,
                prefix = when (prefix.uppercase()) {
                    "GIVEN" -> PepperStepPrefix.GIVEN
                    "WHEN" -> PepperStepPrefix.WHEN
                    "THEN" -> PepperStepPrefix.THEN
                    else -> error("Unknown prefix $prefix")
                },
                finishedAt = Instant.now()
            )
        )
    }

    override suspend fun addArgument(name: String, typeName: String, value: String) {
        scenario.steps.last().arguments.add(PepperStepBuilder.StepArgument(name, typeName, value))
    }

    override suspend fun finishStepWithError(error: Throwable) {
        scenario.steps.last().also {
            it.error = StepError(error.message ?: "Unknown error", error.stackTraceToString())
            it.status = PepperTestStatus.FAILED
            it.finishedAt = Instant.now()
        }
    }

    override suspend fun finishStepWithSuccess(result: Any?) {
        scenario.steps.last().let {
            it.result = result.toString()
            it.status = PepperTestStatus.PASSED
            it.finishedAt = Instant.now()
        }
    }

    override suspend fun finishScenario() {
        scenario.finishedAt = Instant.now()
        listener.onScenarioFinished(scenario.toReport())
    }
}

private fun PepperScenarioBuilder.toReport() = PepperTestScenarioDto(

    version = 1,

    id = id,
    className = className,
    name = name,
    steps = steps.map { it.toReport() }.toMutableList(),
    startedAt = startedAt.toKotlinInstant(),
    finishedAt = requireNotNull(finishedAt) { "Missing finishedAt at scenario $name" }.toKotlinInstant()
)

private fun PepperStepBuilder.toReport() = PepperTestStepDto(
    id = id,
    index = index,
    prefix = prefix,
    name = name,
    status = requireNotNull(status) { "Missing status at step $name" },
    arguments = arguments.map { it.toReport() }.toMutableList(),
    result = result,
    error = error?.let { PepperTestStepDto.StepErrorDto(it.message, it.stacktrace) },
    startedAt = startedAt.toKotlinInstant(),
    finishedAt = requireNotNull(finishedAt) { "Missing finishedAt at step $name" }.toKotlinInstant()
)

private fun PepperStepBuilder.StepArgument.toReport() = StepArgumentDto(
    name = name,
    type = typeName,
    value = value
)
