package io.github.vooft.pepper.reports

import io.github.vooft.pepper.reports.api.PepperProjectReport
import io.github.vooft.pepper.reports.api.PepperScenarioStatus
import io.github.vooft.pepper.reports.api.PepperTestScenario
import io.github.vooft.pepper.reports.api.PepperTestStep
import io.github.vooft.pepper.reports.api.PepperTestStep.StepArgument
import io.github.vooft.pepper.reports.builder.BuilderElements
import io.github.vooft.pepper.reports.builder.PepperReportBuilder
import io.github.vooft.pepper.reports.builder.PepperReportBuilderElement
import io.kotest.core.extensions.ProjectExtension
import io.kotest.core.project.ProjectContext
import kotlinx.coroutines.withContext
import kotlinx.datetime.toKotlinInstant
import java.security.MessageDigest

class PepperBddExtension(private val callback: suspend (PepperProjectReport) -> Unit) : ProjectExtension {
    override suspend fun interceptProject(context: ProjectContext, callback: suspend (ProjectContext) -> Unit) {
        val builder = PepperReportBuilder()
        try {
            withContext(PepperReportBuilderElement(builder)) {
                callback(context)
            }
        } finally {
            builder.finishProject()
            callback(builder.toReport())
        }
    }
}

private fun PepperReportBuilder.toReport() = PepperProjectReport(
    version = 1,
    scenarios = project.scenarios.map { scenario ->
        PepperTestScenario(
            id = "${scenario.className}-${scenario.name}".sha1(),
            className = scenario.className,
            name = scenario.name,
            steps = scenario.steps.map { step ->
                PepperTestStep(
                    id = step.name.sha1(),
                    name = step.name,
                    arguments = step.arguments.map { argument ->
                        StepArgument(
                            name = argument.name,
                            type = argument.typeName,
                            value = argument.value
                        )
                    }.toMutableList(),
                    result = step.result,
                    error = step.error,
                    startedAt = step.startedAt.toKotlinInstant(),
                    finishedAt = requireNotNull(step.finishedAt) { "Missing finishedAt at step ${step.name}" }.toKotlinInstant()
                )
            }.toMutableList(),
            startedAt = scenario.startedAt.toKotlinInstant(),
            status = scenario.status.toApi(),
            finishedAt = requireNotNull(scenario.finishedAt) { "Missing finishedAt at scenario ${scenario.name}" }.toKotlinInstant()
        )
    }.toMutableList(),
    startedAt = project.startedAt.toKotlinInstant(),
    finishedAt = requireNotNull(project.finishedAt) { "Missing finishedAt at project" }.toKotlinInstant(),
)

private fun BuilderElements.PepperScenarioStatus.toApi() = when (this) {
    BuilderElements.PepperScenarioStatus.PASSED -> PepperScenarioStatus.PASSED
    BuilderElements.PepperScenarioStatus.FAILED -> PepperScenarioStatus.FAILED
}

private fun String.sha1() = MessageDigest.getInstance("SHA-1").digest(toByteArray()).joinToString("") { "%02x".format(it) }
