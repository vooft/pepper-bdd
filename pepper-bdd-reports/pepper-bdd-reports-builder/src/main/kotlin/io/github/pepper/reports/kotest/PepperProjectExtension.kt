package io.github.pepper.reports.kotest

import io.github.pepper.reports.builder.PepperReportBuilder
import io.github.pepper.reports.builder.PepperReportBuilderElement
import io.kotest.core.extensions.ProjectExtension
import io.kotest.core.project.ProjectContext
import kotlinx.coroutines.withContext

class PepperProjectExtension : ProjectExtension {
    override suspend fun interceptProject(context: ProjectContext, callback: suspend (ProjectContext) -> Unit) {
        val builder = PepperReportBuilder()
        try {
            withContext(PepperReportBuilderElement(builder)) {
                callback(context)
            }
        } finally {
            builder.finishProject()
            println(builder)
        }
    }
}
