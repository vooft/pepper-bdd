package io.github.vooft.pepper.reports

import io.github.vooft.pepper.reports.api.PepperStepPrefix
import io.github.vooft.pepper.reports.api.PepperTestScenarioDto
import io.github.vooft.pepper.reports.api.PepperTestStatus
import io.github.vooft.pepper.reports.api.PepperTestStatus.FAILED
import io.github.vooft.pepper.reports.api.PepperTestStatus.PASSED
import io.github.vooft.pepper.reports.api.PepperTestStepDto
import io.github.vooft.pepper.reports.api.PepperTestStepDto.StepArgumentDto
import io.github.vooft.pepper.reports.api.PepperTestSuiteDto
import io.github.vooft.pepper.reports.api.PepperTestSuiteDto.ScenarioSummaryDto
import io.github.vooft.pepper.reports.builder.LowLevelReportListener
import io.github.vooft.pepper.reports.builder.PepperScenarioBuilder
import io.github.vooft.pepper.reports.builder.PepperStepBuilder
import io.github.vooft.pepper.reports.builder.PepperStepBuilder.StepError
import io.github.vooft.pepper.reports.builder.sha1
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.toKotlinInstant
import java.time.Instant

private const val VERSION = 1

class ReportListenerAdapter(private val listener: PepperReportListener) : LowLevelReportListener {

    private val startedAt = Instant.now()

    private val scenarioSummariesMutex = Mutex()
    private val scenarioSummaries = mutableListOf<ScenarioSummaryDto>()

    @Volatile
    private lateinit var scenario: PepperScenarioBuilder

    val suite
        get() = PepperTestSuiteDto(
            id = PepperTestSuiteDto.SuiteId(scenarioSummaries.joinToString { it.id.value }.sha1()),
            version = VERSION,
            scenarios = scenarioSummaries,
            startedAt = startedAt.toKotlinInstant(),
            finishedAt = Instant.now().toKotlinInstant()
        )

    override suspend fun startScenario(className: String, name: String, tags: List<String>) {
        scenario = PepperScenarioBuilder(className = className, name = name, tags = tags)
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
            it.status = FAILED
            it.finishedAt = Instant.now()
        }
    }

    override suspend fun finishStepWithSuccess(result: Any?) {
        scenario.steps.last().let {
            it.result = result.toString()
            it.status = PASSED
            it.finishedAt = Instant.now()
        }
    }

    override suspend fun finishScenario() {
        scenario.finishedAt = Instant.now()
        scenarioSummariesMutex.withLock {
            scenarioSummaries.add(
                ScenarioSummaryDto(
                    id = PepperTestScenarioDto.ScenarioId(scenario.id),
                    name = scenario.name,
                    status = when (scenario.steps.all { it.status == PASSED }) {
                        true -> PASSED
                        false -> FAILED
                    }
                )
            )
        }
        listener.onScenarioFinished(scenario.toReport())
    }
}

private fun PepperScenarioBuilder.toReport() = PepperTestScenarioDto(
    id = PepperTestScenarioDto.ScenarioId(id),
    version = VERSION,
    className = className,
    name = name,
    tags = tags.map { PepperTestScenarioDto.ScenarioTag(it) },
    steps = steps.map { it.toReport() }.toMutableList(),
    startedAt = startedAt.toKotlinInstant(),
    finishedAt = requireNotNull(finishedAt) { "Missing finishedAt at scenario $name" }.toKotlinInstant()
)

private fun PepperStepBuilder.toReport() = PepperTestStepDto(
    id = PepperTestStepDto.StepId(id),
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
