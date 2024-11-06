package io.github.pepper.reports.builder

import java.time.Instant
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class PepperReportBuilder {
    private val project = Elements.PepperTestProject()

    fun addScenario(className: String, name: String) {
        val scenario = Elements.PepperTestScenario(className, name)
        project.scenarios.add(scenario)
    }

    fun addStep(name: String) {
        val step = Elements.PepperTestStep(name)
        project.scenarios.last().steps.add(step)
    }

    fun addArgument(name: String, typeName: String, value: String) {
        val argument = Elements.PepperTestStep.StepArgument(name = name, typeName = typeName, value = value)
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
