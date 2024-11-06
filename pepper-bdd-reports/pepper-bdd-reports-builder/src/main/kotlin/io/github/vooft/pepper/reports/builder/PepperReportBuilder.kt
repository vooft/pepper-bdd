package io.github.vooft.pepper.reports.builder

import io.github.pepper.reports.api.PepperProject
import io.github.pepper.reports.api.PepperTestScenario
import io.github.pepper.reports.api.PepperTestStep
import kotlinx.datetime.toKotlinInstant
import java.time.Instant
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class PepperReportBuilder {
    private val project = BuilderElements.PepperTestProject()

    fun addScenario(className: String, name: String) {
        val scenario = BuilderElements.PepperTestScenario(className, name)
        project.scenarios.add(scenario)
    }

    fun addStep(name: String) {
        val step = BuilderElements.PepperTestStep(name)
        project.scenarios.last().steps.add(step)
    }

    fun addArgument(name: String, typeName: String, value: String) {
        val argument = BuilderElements.PepperTestStep.StepArgument(name = name, typeName = typeName, value = value)
        project.scenarios.last().steps.last().arguments.add(argument)
    }

    fun addError(error: Throwable) {
        project.scenarios.last().steps.last().error = error.stackTraceToString()
    }

    fun addResult(result: Any?) {
        project.scenarios.last().steps.last().result = result?.toString()
    }

    fun finishStep() {
        project.scenarios.last().steps.last().finishedAt = Instant.now()
    }

    fun finishScenario() {
        project.scenarios.last().finishedAt = Instant.now()
    }

    fun finishProject() {
        project.finishedAt = Instant.now()
    }

    fun toReport() = PepperProject(
        version = 1,
        scenarios = project.scenarios.map { scenario ->
            PepperTestScenario(
                className = scenario.className,
                name = scenario.name,
                steps = scenario.steps.map { step ->
                    PepperTestStep(
                        name = step.name,
                        arguments = step.arguments.map { argument ->
                            PepperTestStep.StepArgument(
                                name = argument.name,
                                type = argument.typeName,
                                value = argument.value
                            )
                        }.toMutableList(),
                        result = step.result,
                        error = step.error,
                        startedAt = step.startedAt.toKotlinInstant(),
                        finishedAt = requireNotNull(step.finishedAt) { "Missing finishedAt at step ${step.name}"}.toKotlinInstant()
                    )
                }.toMutableList(),
                startedAt = scenario.startedAt.toKotlinInstant(),
                status = scenario.status.toApi(),
                finishedAt = requireNotNull(scenario.finishedAt) { "Missing finishedAt at scenario ${scenario.name}"}.toKotlinInstant()
            )
        }.toMutableList(),
        startedAt = project.startedAt.toKotlinInstant(),
        finishedAt = requireNotNull(project.finishedAt) { "Missing finishedAt at project"}.toKotlinInstant(),
    )

    companion object {
        suspend fun current() = requireNotNull(coroutineContext[PepperReportBuilderElement]) {
            "PepperReportBuilderElement is missing in the context"
        }.builder
    }
}

data class PepperReportBuilderElement(val builder: PepperReportBuilder) : AbstractCoroutineContextElement(PepperReportBuilderElement) {
    companion object Key : CoroutineContext.Key<PepperReportBuilderElement>

    override fun toString(): String = "PepperReportBuilderElement($builder)"
}

private fun BuilderElements.PepperScenarioStatus.toApi() = when (this) {
    BuilderElements.PepperScenarioStatus.PASSED -> io.github.pepper.reports.api.PepperScenarioStatus.PASSED
    BuilderElements.PepperScenarioStatus.FAILED -> io.github.pepper.reports.api.PepperScenarioStatus.FAILED
}
