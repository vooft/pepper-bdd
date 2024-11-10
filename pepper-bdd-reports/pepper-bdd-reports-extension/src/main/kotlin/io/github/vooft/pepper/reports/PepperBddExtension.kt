package io.github.vooft.pepper.reports

import io.github.vooft.pepper.reports.builder.PepperReportListenerElement
import io.kotest.core.extensions.ProjectExtension
import io.kotest.core.project.ProjectContext
import kotlinx.coroutines.withContext

class PepperBddExtension(private val listener: PepperReportListener) : ProjectExtension {
    override suspend fun interceptProject(context: ProjectContext, callback: suspend (ProjectContext) -> Unit) {
        val listenerAdapter = ReportListenerAdapter(listener)
        try {
            withContext(PepperReportListenerElement(listenerAdapter)) {
                callback(context)
            }
        } finally {
            listener.onSuiteFinished(listenerAdapter.suite)
        }
    }

    companion object {
        fun writeToFiles(parentPath: String): PepperBddExtension = PepperBddExtension(WriteToFilesReportListener(parentPath))
    }
}
