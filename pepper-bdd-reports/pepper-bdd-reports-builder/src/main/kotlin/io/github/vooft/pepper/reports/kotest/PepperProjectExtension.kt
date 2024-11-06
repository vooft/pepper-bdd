package io.github.vooft.pepper.reports.kotest

import io.github.vooft.pepper.reports.builder.PepperReportBuilder
import io.github.vooft.pepper.reports.builder.PepperReportBuilderElement
import io.kotest.core.extensions.ProjectExtension
import io.kotest.core.project.ProjectContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PepperProjectExtension : ProjectExtension {
    override suspend fun interceptProject(context: ProjectContext, callback: suspend (ProjectContext) -> Unit) {
        val builder = PepperReportBuilder()
        try {
            withContext(PepperReportBuilderElement(builder)) {
                callback(context)
            }
        } finally {
            builder.finishProject()
            println("\n\n\n\n\n")
            println(Json.encodeToString(builder.toReport()))
            println("\n\n\n\n\n")
        }
    }
}
