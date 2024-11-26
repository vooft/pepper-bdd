package io.github.vooft.pepper.reports

import io.github.vooft.pepper.reports.api.PepperTestScenarioDto
import io.github.vooft.pepper.reports.api.PepperTestSuiteDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths

internal class WriteToFilesReportListener(private val parentPath: String) : PepperReportListener {

    init {
        val file = Paths.get(parentPath, PEPPER_JSON_FILE).toFile()
        if (file.parentFile.exists()) {
            file.parentFile.deleteRecursively()
        }
    }

    override suspend fun onScenarioFinished(scenario: PepperTestScenarioDto) {
        withContext(Dispatchers.IO) {
            File(parentPath).mkdirs()
            val json = Json.encodeToString(scenario)
            Paths.get(parentPath, "${scenario.id.value}.json").toFile().writeText(json)
        }
    }

    override suspend fun onSuiteFinished(suite: PepperTestSuiteDto) {
        withContext(Dispatchers.IO) {
            File(parentPath).mkdirs()
            val json = Json.encodeToString(suite)
            Paths.get(parentPath, PEPPER_JSON_FILE).toFile().writeText(json)
        }
    }
}

private const val PEPPER_JSON_FILE = "pepper.json"
