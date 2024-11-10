package io.github.vooft.pepper.reports

import io.github.vooft.pepper.reports.api.PepperTestScenario
import io.github.vooft.pepper.reports.api.PepperTestStep
import io.github.vooft.pepper.reports.api.PepperTestStep.StepArgument
import io.github.vooft.pepper.reports.api.PepperTestSuite
import io.github.vooft.pepper.reports.builder.LowLevelReportListener
import io.github.vooft.pepper.reports.builder.PepperScenarioBuilder
import io.github.vooft.pepper.reports.builder.PepperStepBuilder
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

    val suite get() = PepperTestSuite(
        version = 1,
        scenarios = scenarioIds,
        startedAt = startedAt.toKotlinInstant(),
        finishedAt = Instant.now().toKotlinInstant()
    )

    override suspend fun startScenario(className: String, name: String) {
        scenario = PepperScenarioBuilder(className, name)
        scenarioIdsMutex.withLock { scenarioIds.add(scenario.id) }
    }

    override suspend fun startStep(name: String) {
        scenario.steps.add(PepperStepBuilder(name))
    }

    override suspend fun addArgument(name: String, typeName: String, value: String) {
        scenario.steps.last().arguments.add(PepperStepBuilder.StepArgument(name, typeName, value))
    }

    override suspend fun addError(error: Throwable) {
        scenario.steps.last().error = error.message
    }

    override suspend fun addResult(result: Any?) {
        scenario.steps.last().result = result.toString()
    }

    override suspend fun finishStep() {
        scenario.steps.last().finishedAt = Instant.now()
    }

    override suspend fun finishScenario() {
        scenario.finishedAt = Instant.now()
        listener.onScenarioFinished(scenario.toReport())
    }
}

private fun PepperScenarioBuilder.toReport() = PepperTestScenario(

    version = 1,

    id = id,
    className = className,
    name = name,
    steps = steps.map { it.toReport() }.toMutableList(),
    status = status,
    startedAt = startedAt.toKotlinInstant(),
    finishedAt = requireNotNull(finishedAt) { "Missing finishedAt at scenario $name" }.toKotlinInstant()
)

private fun PepperStepBuilder.toReport() = PepperTestStep(
    id = id,
    name = name,
    arguments = arguments.map { it.toReport() }.toMutableList(),
    result = result,
    error = error,
    startedAt = startedAt.toKotlinInstant(),
    finishedAt = requireNotNull(finishedAt) { "Missing finishedAt at step $name" }.toKotlinInstant()
)

private fun PepperStepBuilder.StepArgument.toReport() = StepArgument(
    name = name,
    type = typeName,
    value = value
)
