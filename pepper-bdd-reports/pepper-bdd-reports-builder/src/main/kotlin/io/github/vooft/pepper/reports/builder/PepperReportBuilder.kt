package io.github.vooft.pepper.reports.builder

import java.time.Instant
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class PepperReportBuilder {
    internal val project = BuilderElements.PepperTestProject()

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

    companion object {
        suspend fun ifPresent(block: PepperReportBuilder.() -> Unit) {
            coroutineContext[PepperReportBuilderElement]?.builder?.block()
        }
    }
}

data class PepperReportBuilderElement(val builder: PepperReportBuilder) : AbstractCoroutineContextElement(PepperReportBuilderElement) {
    companion object Key : CoroutineContext.Key<PepperReportBuilderElement>

    override fun toString(): String = "PepperReportBuilderElement($builder)"
}
