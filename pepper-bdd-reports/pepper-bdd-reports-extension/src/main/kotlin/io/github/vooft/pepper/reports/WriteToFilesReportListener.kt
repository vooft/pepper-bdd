package io.github.vooft.pepper.reports

import io.github.vooft.pepper.reports.api.PepperTestScenario
import io.github.vooft.pepper.reports.api.PepperTestSuite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Paths

class WriteToFilesReportListener(private val parentPath: String) : PepperReportListener {

    init {
        val file = Paths.get(parentPath, PEPPER_JSON_FILE).toFile()
        require(!file.exists()) {
            "File $parentPath/$PEPPER_JSON_FILE already exists"
        }
    }

    override suspend fun onScenarioFinished(scenario: PepperTestScenario) {
        withContext(Dispatchers.IO) {
            File(parentPath).mkdirs()
            val json = Json.encodeToString(scenario)
            Paths.get(parentPath, "${scenario.id}.json").toFile().writeText(json)
        }
    }

    override suspend fun onSuiteFinished(suite: PepperTestSuite) {
        withContext(Dispatchers.IO) {
            File(parentPath).mkdirs()
            val json = Json.encodeToString(suite)
            Paths.get(parentPath, PEPPER_JSON_FILE).toFile().writeText(json)
        }
    }
}

private const val PEPPER_JSON_FILE = "pepper.json"
